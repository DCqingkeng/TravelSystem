package com.travel.service;

import com.travel.entity.Destination;
import com.travel.entity.DestinationType;

import java.util.List;
import java.util.Map;

/**
 * 旅游推荐服务接口
 * 提供基于热度、评分、兴趣的Top-K推荐功能
 */
public interface RecommendationService {

    /**
     * 获取Top-K推荐目的地（核心算法：基于最小堆的不完全排序）
     *
     * @param userId  用户ID（用于个性化兴趣匹配）
     * @param type    目的地类型筛选（景区/学校），可为null
     * @param keyword 搜索关键词（名称/类别/关键字），可为null
     * @param topK    需要返回的前K个结果
     * @return Top-K推荐列表（按综合得分降序排列）
     */
    List<Destination> recommendTopK(Long userId, DestinationType type, String keyword, int topK);

    /**
     * 获取推荐目的地（完全排序版本，用于算法对比测试）
     *
     * @param userId  用户ID
     * @param type    目的地类型
     * @param keyword 搜索关键词
     * @param limit   返回数量限制
     * @return 排序后的推荐列表
     */
    List<Destination> recommendByFullSort(Long userId, DestinationType type, String keyword, int limit);

    /**
     * 更新用户兴趣模型（基于浏览历史）
     *
     * @param userId        用户ID
     * @param destinationId 浏览的目的地ID
     * @param action        行为类型：VIEW(浏览), FAVORITE(收藏), RATING(评分)
     * @param weight        行为权重
     */
    void updateUserInterest(Long userId, Long destinationId, String action, Double weight);

    /**
     * 获取推荐算法的性能对比报告
     *
     * @param userId 测试用户ID
     * @return 包含不同算法执行时间和加速比的报告
     */
    Map<String, Object> getAlgorithmPerformanceReport(Long userId);

    /**
     * 刷新推荐缓存（数据变化时调用）
     */
    void refreshRecommendationCache();
}
