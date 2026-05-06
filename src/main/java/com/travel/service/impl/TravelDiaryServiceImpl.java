package com.travel.service.impl;

import com.travel.entity.*;
import com.travel.entity.dto.DiaryCreateRequest;
import com.travel.entity.dto.DiaryRatingRequest;
import com.travel.mapper.*;
import com.travel.service.TravelDiaryService;
import com.travel.service.algorithm.HashIndex;
import com.travel.service.algorithm.HuffmanCompressor;
import com.travel.service.algorithm.InvertedIndex;
import com.travel.service.algorithm.TopKHeap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TravelDiaryServiceImpl implements TravelDiaryService {

    @Autowired
    private TravelDiaryMapper diaryMapper;
    @Autowired
    private DiaryMediaMapper mediaMapper;
    @Autowired
    private DiaryRatingMapper ratingMapper;
    @Autowired
    private DiaryViewMapper viewMapper;
    @Autowired
    private DestinationMapper destinationMapper;
    @Autowired
    private HuffmanCompressor compressor;
    @Autowired
    private InvertedIndex invertedIndex;
    @Autowired
    private HashIndex hashIndex;

    // 推荐得分权重：热度（浏览量）+ 评分
    private static final double WEIGHT_HEAT = 0.5;
    private static final double WEIGHT_RATING = 0.5;

    @Override
    public TravelDiary createDiary(Long userId, DiaryCreateRequest request) {
        String keywords = request.getKeywords();
        if (StringUtils.hasText(keywords)) {
            keywords = keywords.replace('，', ',');
            keywords = keywords.replaceAll("\\s*,\\s*", ",");
            keywords = keywords.replaceAll(",+", ",");
            keywords = keywords.replaceAll("^,|,$", "");
        }

        String rawContent = request.getContent();
        boolean isCompressed = Boolean.TRUE.equals(request.getCompress());
        String storeContent = rawContent;

        if (isCompressed && StringUtils.hasText(rawContent)) {
            storeContent = compressor.compress(rawContent);
        }

        TravelDiary diary = TravelDiary.builder()
                .userId(userId)
                .destinationId(request.getDestinationId())
                .title(request.getTitle())
                .content(storeContent)
                .contentCompressed(isCompressed ? 1 : 0)
                .keywords(keywords)
                .heatScore(0.0)
                .avgRating(5.0)
                .ratingCount(0)
                .travelDate(request.getTravelDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        diaryMapper.insert(diary);

        if (request.getMediaUrls() != null && !request.getMediaUrls().isEmpty()) {
            List<DiaryMedia> mediaList = new ArrayList<>();
            for (int i = 0; i < request.getMediaUrls().size(); i++) {
                String url = request.getMediaUrls().get(i);
                String type = url.matches(".*\\.(mp4|avi|mov)$") ? "VIDEO" : "IMAGE";
                mediaList.add(DiaryMedia.builder()
                        .diaryId(diary.getId())
                        .mediaType(type)
                        .mediaUrl(url)
                        .sortOrder(i)
                        .build());
            }
            mediaMapper.batchInsert(mediaList);
        }

        if (StringUtils.hasText(diary.getTitle())) {
            hashIndex.put(diary.getTitle(), diary.getId());
        }

        if (StringUtils.hasText(rawContent)) {
            asyncAddToContentIndex(diary.getId(), rawContent);
        }

        return diary;
    }

    @Override
    public TravelDiary getDiaryDetail(Long diaryId, Long currentUserId, String ipAddress) {
        TravelDiary diary = diaryMapper.selectById(diaryId);
        if (diary == null) return null;
        decompressIfNeeded(diary);
        recordViewAsync(diaryId, currentUserId, ipAddress);
        return diary;
    }

    @Override
    public TravelDiary getByTitleExact(String title) {
        if (!StringUtils.hasText(title)) return null;
        Long diaryId = hashIndex.get(title);
        TravelDiary diary = null;
        if (diaryId != null) {
            diary = diaryMapper.selectById(diaryId);
        }
        if (diary == null) {
            diary = diaryMapper.selectByTitleExact(title);
        }
        decompressIfNeeded(diary);
        return diary;
    }

    @Override
    public List<TravelDiary> searchByContent(String keyword, int topK) {
        if (!StringUtils.hasText(keyword)) return Collections.emptyList();
        List<Long> docIds = invertedIndex.search(keyword, 50);
        if (docIds.isEmpty()) return Collections.emptyList();

        List<TravelDiary> candidates = new ArrayList<>();
        for (Long id : docIds) {
            TravelDiary diary = diaryMapper.selectById(id);
            if (diary == null) continue;
            decompressIfNeeded(diary);
            diary.setCompositeScore(calculateCompositeScore(diary));
            candidates.add(diary);
        }

        TopKHeap<TravelDiary> heap = new TopKHeap<>(topK,
                Comparator.comparing(TravelDiary::getCompositeScore));
        for (TravelDiary d : candidates) {
            heap.offer(d);
        }
        List<TravelDiary> result = heap.getSortedResults();
        decompressList(result);
        return result;
    }

    @Override
    public List<TravelDiary> listDiariesByHeat(int topK) {
        List<TravelDiary> candidates = diaryMapper.selectRecent(100);
        TopKHeap<TravelDiary> heap = new TopKHeap<>(topK,
                Comparator.comparing(TravelDiary::getCompositeScore));
        for (TravelDiary diary : candidates) {
            diary.setCompositeScore(calculateCompositeScore(diary));
            heap.offer(diary);
        }
        List<TravelDiary> result = heap.getSortedResults();
        decompressList(result);
        return result;
    }

    @Override
    public List<TravelDiary> searchDiariesByDestination(String destinationName, int topK) {
        if (!StringUtils.hasText(destinationName)) {
            return Collections.emptyList();
        }

        List<Destination> destinations = destinationMapper.searchByKeyword(
                null, destinationName, 20);

        if (destinations == null || destinations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> destIds = destinations.stream()
                .map(Destination::getId)
                .distinct()
                .collect(Collectors.toList());

        List<TravelDiary> candidates = new ArrayList<>();
        for (Long destId : destIds) {
            List<TravelDiary> diaries = diaryMapper.selectByDestinationId(destId, 200);
            if (diaries != null) {
                candidates.addAll(diaries);
            }
        }

        Map<Long, TravelDiary> uniqueMap = new LinkedHashMap<>();
        for (TravelDiary d : candidates) {
            uniqueMap.putIfAbsent(d.getId(), d);
        }
        candidates = new ArrayList<>(uniqueMap.values());

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        TopKHeap<TravelDiary> topKHeap = new TopKHeap<>(topK,
                Comparator.comparing(TravelDiary::getCompositeScore));

        for (TravelDiary diary : candidates) {
            diary.setCompositeScore(calculateCompositeScore(diary));
            topKHeap.offer(diary);
        }

        List<TravelDiary> result = topKHeap.getSortedResults();
        decompressList(result);
        return result;
    }

    /**
     * 评分日记：修复后支持多用户累计评分
     */
    @Override
    public void rateDiary(Long userId, DiaryRatingRequest request) {
        Long diaryId = request.getDiaryId();

        // 查询该用户是否已对这篇日记评分
        DiaryRating existing = ratingMapper.selectByDiaryAndUser(diaryId, userId);

        if (existing != null) {
            // 更新已有评分
            existing.setScore(request.getScore());
            existing.setComment(request.getComment());
            ratingMapper.update(existing);
        } else {
            // 新增评分记录
            DiaryRating rating = DiaryRating.builder()
                    .diaryId(diaryId)
                    .userId(userId)
                    .score(request.getScore())
                    .comment(request.getComment())
                    .createdAt(LocalDateTime.now())
                    .build();
            ratingMapper.insert(rating);
        }

        // 重新计算该日记的所有评分的平均值和总人数
        Double avg = ratingMapper.selectAvgScore(diaryId);
        Integer count = ratingMapper.selectCountByDiaryId(diaryId);

        diaryMapper.updateRating(diaryId,
                avg != null ? avg : 5.0,
                count != null ? count : 0);
    }

    @Override
    public void buildSearchIndex() {
        List<TravelDiary> all = diaryMapper.selectRecent(1000);
        hashIndex.clear();
        invertedIndex.clear();

        for (TravelDiary diary : all) {
            if (StringUtils.hasText(diary.getTitle())) {
                hashIndex.put(diary.getTitle(), diary.getId());
            }

            String rawContent = null;
            if (diary.getContentCompressed() != null && diary.getContentCompressed() == 1
                    && StringUtils.hasText(diary.getContent())) {
                try {
                    rawContent = compressor.decompress(diary.getContent());
                } catch (Exception e) {
                    log.warn("日记内容解压失败，跳过全文索引，id={}", diary.getId());
                }
            } else {
                rawContent = diary.getContent();
            }

            if (StringUtils.hasText(rawContent)) {
                invertedIndex.addDocument(diary.getId(), rawContent);
            }
        }

        log.info("索引构建完成：名称哈希索引 {} 条，内容倒排索引 {} 篇", hashIndex.size(), all.size());
    }

    @Override
    public Map<String, Object> getDiaryStatistics(Long diaryId) {
        Integer viewCount = viewMapper.selectViewCount(diaryId);
        Integer ratingCount = ratingMapper.selectCountByDiaryId(diaryId);
        Double avgRating = ratingMapper.selectAvgScore(diaryId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("viewCount", viewCount != null ? viewCount : 0);
        stats.put("ratingCount", ratingCount != null ? ratingCount : 0);
        stats.put("avgRating", avgRating != null ? avgRating : 5.0);
        return stats;
    }

    /**
     * 热度只与浏览量有关：heat_score = views
     */
    @Async("taskExecutor")
    public void recordViewAsync(Long diaryId, Long userId, String ipAddress) {
        try {
            viewMapper.insert(diaryId, userId, ipAddress);
            Integer views = viewMapper.selectViewCount(diaryId);

            // 热度 = 浏览量（纯浏览量驱动，与评分无关）
            if (views != null) {
                double heat = views.doubleValue();
                diaryMapper.updateHeatScore(diaryId, heat);
            }
        } catch (Exception e) {
            log.error("记录浏览失败", e);
        }
    }

    @Async("taskExecutor")
    public void asyncAddToContentIndex(Long diaryId, String rawContent) {
        invertedIndex.addDocument(diaryId, rawContent);
    }

    /**
     * 统一解压日记内容
     */
    private void decompressIfNeeded(TravelDiary diary) {
        if (diary == null) return;
        if (diary.getContentCompressed() != null && diary.getContentCompressed() == 1
                && StringUtils.hasText(diary.getContent())) {
            try {
                diary.setContent(compressor.decompress(diary.getContent()));
            } catch (Exception e) {
                log.warn("日记内容解压失败，id={}", diary.getId());
            }
        }
    }

    private void decompressList(List<TravelDiary> list) {
        if (list == null) return;
        for (TravelDiary diary : list) {
            decompressIfNeeded(diary);
        }
    }

    /**
     * 综合推荐得分：结合热度（浏览量）和评分
     * heat_score 只反映浏览量，avg_rating 反映质量
     */
    private double calculateCompositeScore(TravelDiary diary) {
        if (diary == null) return 0.0;

        double heat = diary.getHeatScore() != null ? diary.getHeatScore() : 0.0;
        double rating = diary.getAvgRating() != null ? diary.getAvgRating() : 5.0;

        // 热度归一化（假设参考最大浏览量 10000 → 100分）
        double heatNormalized = Math.min(heat / 100.0, 100.0);

        // 评分归一化（1-5分 → 0-100分）
        double ratingNormalized = ((rating - 1.0) / 4.0) * 100.0;

        return WEIGHT_HEAT * heatNormalized + WEIGHT_RATING * ratingNormalized;
    }
}