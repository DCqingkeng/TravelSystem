package com.travel.service.impl;

import com.travel.mapper.DestinationMapper;
import com.travel.service.DestinationHeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 目的地热度服务实现
 * 使用Redis SortedSet实现实时热度排行
 * 周期性同步到MySQL持久化
 */
@Slf4j
@Service("destinationHeatService")
public class DestinationHeatServiceImpl implements DestinationHeatService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DestinationMapper destinationMapper;

    // Redis键定义
    private static final String KEY_HEAT_REALTIME = "dest:heat:realtime";

    /**
     * 系统启动时初始化热度数据
     */
    @PostConstruct
    @Override
    public void initializeHeatData() {
        log.info("初始化目的地热度数据...");
        try {
            // 查询返回 List<Map>，手动转换为 Map<Long, Double>
            List<Map<String, Object>> list = destinationMapper.selectAllHeatScores();
            Map<Long, Double> heatMap = list.stream()
                    .collect(Collectors.toMap(
                            m -> (Long) m.get("id"),
                            m -> (Double) m.get("heatScore")
                    ));

            if (!heatMap.isEmpty()) {
                heatMap.forEach((id, score) -> {
                    redisTemplate.opsForZSet().add(KEY_HEAT_REALTIME, id.toString(), score);
                });
                log.info("热度数据初始化完成: 加载{}条记录", heatMap.size());
            }
        } catch (Exception e) {
            log.error("热度数据初始化失败", e);
        }
    }

    /**
     * 增加热度（实时更新到Redis）
     * 使用异步方式，避免阻塞主流程
     */
    @Override
    public void incrementHeatScore(Long destinationId, Double delta) {
        if (destinationId == null || delta == null || delta <= 0) {
            return;
        }

        try {
            // Redis ZINCRBY命令: 原子性增加
            Double newScore = redisTemplate.opsForZSet()
                    .incrementScore(KEY_HEAT_REALTIME, destinationId.toString(), delta);

            // 记录日志（每100次打印一次，避免日志过多）
            if (newScore != null && newScore % 100 < delta) {
                log.debug("热度更新: destId={}, delta={}, newScore={}",
                        destinationId, delta, newScore);
            }

            // 每100分同步一次到数据库（简单策略）
            if (newScore != null && newScore % 100 < delta) {
                syncSingleDestination(destinationId, newScore);
            }
        } catch (Exception e) {
            log.error("热度更新失败: destId={}", destinationId, e);
        }
    }

    /**
     * 获取热门目的地Top-K
     */
    @Override
    public Set<Long> getHotDestinations(int topK) {
        // 从Redis获取Top-K（ZREVRANGE: 降序排列）
        Set<ZSetOperations.TypedTuple<String>> hotSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KEY_HEAT_REALTIME, 0, topK - 1);

        if (hotSet == null || hotSet.isEmpty()) {
            return new HashSet<>();
        }

        // 转换为Long类型ID集合
        return hotSet.stream()
                .map(tuple -> {
                    String member = tuple.getValue();
                    return member != null ? Long.parseLong(member) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 获取目的地的热度排名（从高到低，1开始）
     */
    @Override
    public Long getHeatRank(Long destinationId) {
        // ZREVRANK: 返回元素的排名（0-based），null表示元素不存在
        Long rank = redisTemplate.opsForZSet()
                .reverseRank(KEY_HEAT_REALTIME, destinationId.toString());
        return rank != null ? rank + 1 : null; // 转换为1-based排名
    }

    /**
     * 定时任务：每小时同步热度到MySQL
     * 使用@Scheduled配置定时任务
     */
    @Override
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void syncHeatToDatabase() {
        log.info("开始同步热度数据到MySQL...");
        long start = System.currentTimeMillis();

        try {
            // 获取Redis中所有热度数据
            Set<ZSetOperations.TypedTuple<String>> allHeat = redisTemplate.opsForZSet()
                    .rangeWithScores(KEY_HEAT_REALTIME, 0, -1);

            if (allHeat == null || allHeat.isEmpty()) {
                log.info("无热度数据需要同步");
                return;
            }

            // 批量更新MySQL
            int updatedCount = 0;
            for (ZSetOperations.TypedTuple<String> tuple : allHeat) {
                if (tuple.getValue() == null || tuple.getScore() == null) continue;

                Long destId = Long.parseLong(tuple.getValue());
                Double heatScore = tuple.getScore();

                destinationMapper.updateHeatScore(destId, heatScore);
                updatedCount++;

                // 每100条打印一次进度
                if (updatedCount % 100 == 0) {
                    log.debug("已同步{}条热度记录", updatedCount);
                }
            }

            long cost = System.currentTimeMillis() - start;
            log.info("热度同步完成: 更新{}条记录, 耗时{}ms", updatedCount, cost);

        } catch (Exception e) {
            log.error("热度同步失败", e);
        }
    }

    /**
     * 同步单个目的地热度（立即同步）
     */
    private void syncSingleDestination(Long destinationId, Double heatScore) {
        try {
            destinationMapper.updateHeatScore(destinationId, heatScore);
        } catch (Exception e) {
            log.error("单条热度同步失败: destId={}", destinationId, e);
        }
    }
}