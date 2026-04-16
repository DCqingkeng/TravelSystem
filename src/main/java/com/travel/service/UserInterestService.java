package com.travel.service;

import com.travel.entity.UserInterest;

import java.util.List;
import java.util.Map;

/**
 * 用户兴趣管理服务接口
 */
public interface UserInterestService {

    /**
     * 获取用户的兴趣标签及其权重
     *
     * @param userId 用户ID
     * @return 兴趣标签->权重的映射
     */
    Map<String, Double> getUserInterests(Long userId);

    /**
     * 获取用户兴趣列表
     *
     * @param userId 用户ID
     * @return 兴趣列表
     */
    List<UserInterest> getUserInterestList(Long userId);

    /**
     * 添加或更新用户兴趣
     *
     * @param userId      用户ID
     * @param interestTag 兴趣标签
     * @param weight      权重
     */
    void addOrUpdateInterest(Long userId, String interestTag, Double weight);

    /**
     * 删除用户兴趣
     *
     * @param userId      用户ID
     * @param interestTag 兴趣标签
     */
    void removeInterest(Long userId, String interestTag);

    /**
     * 基于用户行为自动计算兴趣权重
     *
     * @param userId        用户ID
     * @param destinationId 目的地ID
     * @param action        行为类型
     */
    void calculateInterestFromBehavior(Long userId, Long destinationId, String action);

    /**
     * 清除用户兴趣缓存
     *
     * @param userId 用户ID
     */
    void clearInterestCache(Long userId);
}
