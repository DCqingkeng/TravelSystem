package com.travel.service;

import com.travel.entity.TravelDiary;
import com.travel.entity.dto.DiaryCreateRequest;
import com.travel.entity.dto.DiaryRatingRequest;

import java.util.List;
import java.util.Map;

public interface TravelDiaryService {

    TravelDiary createDiary(Long userId, DiaryCreateRequest request);

    TravelDiary getDiaryDetail(Long diaryId, Long currentUserId, String ipAddress);

    /**
     * 精确查询日记名称（核心算法：哈希查找 O(1)）
     */
    TravelDiary getByTitleExact(String title);

    /**
     * 全文检索日记内容（核心算法：倒排索引）
     */
    List<TravelDiary> searchByContent(String keyword, int topK);

    List<TravelDiary> listDiariesByHeat(int topK);

    /**
     * 按目的地中文名查询日记，根据热度和评分排序
     * 核心算法：查找算法（目的地名称模糊匹配） + 排序算法（Top-K堆排序）
     */
    List<TravelDiary> searchDiariesByDestination(String destinationName, int topK);

    void rateDiary(Long userId, DiaryRatingRequest request);

    void buildSearchIndex();

    Map<String, Object> getDiaryStatistics(Long diaryId);
}