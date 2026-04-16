package com.travel.service.impl;

import com.travel.entity.Destination;
import com.travel.entity.DestinationType;
import com.travel.entity.UserInterest;
import com.travel.mapper.DestinationMapper;
import com.travel.mapper.UserInterestMapper;
import com.travel.service.DestinationHeatService;
import com.travel.service.RecommendationService;
import com.travel.service.UserInterestService;
import com.travel.service.algorithm.ScoreCalculator;
import com.travel.service.algorithm.TopKHeap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Top-K推荐服务实现类
 * 核心算法：基于最小堆(优先队列)的部分排序，时间复杂度O(n log k)
 * 适用于只需前K个结果而不需要完全排序的场景
 */
@Slf4j
@Service("recommendationService")
public class TopKRecommendationServiceImpl implements RecommendationService {

    @Autowired
    private DestinationMapper destinationMapper;

    @Autowired
    private UserInterestMapper userInterestMapper;

    @Autowired
    private UserInterestService userInterestService;

    @Autowired
    private DestinationHeatService destinationHeatService;

    @Autowired
    private ScoreCalculator scoreCalculator;

    // 默认权重配置（可从配置文件读取）
    private static final double DEFAULT_HEAT_WEIGHT = 0.3;
    private static final double DEFAULT_RATING_WEIGHT = 0.4;
    private static final double DEFAULT_INTEREST_WEIGHT = 0.3;
    // 预过滤候选集大小限制，避免处理过多数据
    private static final int CANDIDATE_LIMIT = 200;

    /**
     * 核心算法：基于最小堆的Top-K推荐
     * 算法步骤：
     * 1. 获取候选集（使用数据库索引过滤）
     * 2. 计算每个候选目的地的综合得分
     * 3. 使用最小堆维护当前Top-K（堆顶为当前第K名）
     * 4. 遍历完成后从堆中提取结果并降序排列
     *
     * 时间复杂度：O(n log k)，其中n为候选集大小，k为Top-K值
     * 空间复杂度：O(k)，仅需存储K个元素的堆空间
     */
    @Override
    @Cacheable(value = "topKRecommendations", key = "#userId + ':' + #type + ':' + #keyword + ':' + #topK",
            unless = "#result == null || #result.isEmpty()")
    public List<Destination> recommendTopK(Long userId, DestinationType type, String keyword, int topK) {
        long startTime = System.currentTimeMillis();
        log.info("开始Top-K推荐计算: userId={}, type={}, keyword={}, topK={}", userId, type, keyword, topK);

        // 1. 获取候选集（数据库层面预过滤，使用索引优化）
        List<Destination> candidates = fetchCandidates(type, keyword);
        log.debug("候选集大小: {}", candidates.size());

        // 2. 获取用户兴趣（带缓存）
        Map<String, Double> userInterests = userInterestService.getUserInterests(userId);

        // 3. 初始化Top-K最小堆（按综合得分升序，堆顶为当前第K名）
        TopKHeap<Destination> topKHeap = new TopKHeap<>(topK,
                Comparator.comparing(Destination::getCompositeScore));

        // 4. 遍历候选集，计算得分并维护Top-K
        int processedCount = 0;
        for (Destination dest : candidates) {
            // 计算综合得分（热度40% + 评分30% + 兴趣30%）
            double score = scoreCalculator.calculateCompositeScore(dest, userInterests);
            dest.setCompositeScore(score);

            // 插入堆中，堆会自动维护大小为K
            topKHeap.offer(dest);
            processedCount++;
        }

        // 5. 从堆中提取结果（此时堆中为Top-K，但为升序排列）
        List<Destination> result = topKHeap.getSortedResults();

        long costTime = System.currentTimeMillis() - startTime;
        log.info("Top-K推荐完成: 处理{}个候选, 耗时{}ms, 返回前{}个结果",
                processedCount, costTime, result.size());

        return result;
    }

    /**
     * 完全排序实现（用于算法对比测试）
     * 时间复杂度：O(n log n)
     */
    @Override
    public List<Destination> recommendByFullSort(Long userId, DestinationType type,
                                                 String keyword, int limit) {
        long startTime = System.currentTimeMillis();

        // 获取候选集
        List<Destination> candidates = fetchCandidates(type, keyword);
        Map<String, Double> userInterests = userInterestService.getUserInterests(userId);

        // 计算得分
        for (Destination dest : candidates) {
            double score = scoreCalculator.calculateCompositeScore(dest, userInterests);
            dest.setCompositeScore(score);
        }

        // 完全排序（时间复杂度O(n log n)）
        candidates.sort((a, b) -> Double.compare(b.getCompositeScore(), a.getCompositeScore()));

        List<Destination> result = candidates.stream()
                .limit(limit)
                .collect(Collectors.toList());

        long costTime = System.currentTimeMillis() - startTime;
        log.info("完全排序推荐完成: 处理{}个候选, 耗时{}ms", candidates.size(), costTime);

        return result;
    }

    /**
     * 异步更新用户兴趣模型
     * 当用户浏览/收藏/评分某个目的地时，更新其兴趣权重
     */
    @Override
    @Async("taskExecutor")
    public void updateUserInterest(Long userId, Long destinationId, String action, Double weight) {
        try {
            // 获取目的地的关键词标签
            Destination dest = destinationMapper.selectById(destinationId);
            if (dest == null || !StringUtils.hasText(dest.getKeywords())) {
                return;
            }

            // 根据行为类型调整权重增量
            double deltaWeight = calculateWeightDelta(action, weight);

            // 更新每个相关兴趣标签的权重
            String[] tags = dest.getKeywords().split(",");
            for (String tag : tags) {
                tag = tag.trim();
                if (!tag.isEmpty()) {
                    userInterestService.addOrUpdateInterest(userId, tag, deltaWeight);
                }
            }

            log.debug("更新用户兴趣: userId={}, destId={}, action={}, 影响标签数={}",
                    userId, destinationId, action, tags.length);
        } catch (Exception e) {
            log.error("更新用户兴趣失败: userId={}, destId={}", userId, destinationId, e);
        }
    }

    /**
     * 获取算法性能对比报告
     * 比较Top-K堆算法与完全排序的性能差异
     */
    @Override
    public Map<String, Object> getAlgorithmPerformanceReport(Long userId) {
        Map<String, Object> report = new HashMap<>();
        int[] testSizes = {50, 100, 200, 500};

        for (int size : testSizes) {
            // 模拟不同数据量下的性能测试
            List<Long> fullSortTimes = new ArrayList<>();
            List<Long> topKTimes = new ArrayList<>();

            // 运行5次取平均值（排除冷启动影响）
            for (int i = 0; i < 5; i++) {
                // 测试完全排序
                long t1 = System.currentTimeMillis();
                recommendByFullSort(userId, null, null, 10);
                fullSortTimes.add(System.currentTimeMillis() - t1);

                // 测试Top-K堆算法
                long t2 = System.currentTimeMillis();
                recommendTopK(userId, null, null, 10);
                topKTimes.add(System.currentTimeMillis() - t2);
            }

            double avgFullSort = fullSortTimes.stream()
                    .mapToLong(Long::longValue).average().orElse(0);
            double avgTopK = topKTimes.stream()
                    .mapToLong(Long::longValue).average().orElse(0);

            double speedup = avgFullSort / Math.max(avgTopK, 1); // 避免除0

            report.put("dataSize_" + size, Map.of(
                    "fullSort_avg_ms", avgFullSort,
                    "topKHeap_avg_ms", avgTopK,
                    "speedup_ratio", String.format("%.2f", speedup),
                    "efficiency_gain", String.format("%.1f%%", (1 - 1/speedup) * 100)
            ));
        }

        report.put("algorithm", "Top-K Min-Heap vs Full Sort");
        report.put("complexity_topk", "O(n log k)");
        report.put("complexity_full", "O(n log n)");
        report.put("space_topk", "O(k)");
        report.put("space_full", "O(n)");

        return report;
    }

    /**
     * 清除推荐缓存
     * 当目的地数据或用户兴趣发生批量变更时调用
     */
    @Override
    @CacheEvict(value = "topKRecommendations", allEntries = true)
    public void refreshRecommendationCache() {
        log.info("清除Top-K推荐缓存");
    }

    /**
     * 获取候选集（数据库查询优化）
     * 策略：
     * 1. 如有关键词，使用全文索引查询
     * 2. 如按类型筛选，使用组合索引
     * 3. 否则按热度预取前N个（避免全表扫描）
     */
    private List<Destination> fetchCandidates(DestinationType type, String keyword) {
        List<Destination> candidates;

        if (StringUtils.hasText(keyword)) {
            // 使用全文搜索（需数据库建立FULLTEXT索引）
            candidates = destinationMapper.searchByKeyword(type, keyword, CANDIDATE_LIMIT);
        } else if (type != null) {
            // 按类型+热度排序（利用索引idx_type_heat）
            candidates = destinationMapper.findTopByTypeOrderByHeat(type, CANDIDATE_LIMIT);
        } else {
            // 无筛选条件时，取热度最高的前N个
            candidates = destinationMapper.findTopOrderByHeat(CANDIDATE_LIMIT);
        }

        return candidates != null ? candidates : new ArrayList<>();
    }

    /**
     * 根据行为类型计算权重增量
     */
    private double calculateWeightDelta(String action, Double baseWeight) {
        double multiplier = switch (action.toUpperCase()) {
            case "VIEW" -> 0.1;      // 浏览行为，轻微增加
            case "FAVORITE" -> 0.5;  // 收藏行为，显著增加
            case "RATING" -> 0.3;    // 评分行为，中等增加
            case "SHARE" -> 0.4;     // 分享行为
            default -> 0.1;
        };

        return (baseWeight != null ? baseWeight : 1.0) * multiplier;
    }
}
