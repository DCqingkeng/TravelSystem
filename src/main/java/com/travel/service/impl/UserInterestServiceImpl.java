package com.travel.service.impl;

import com.travel.entity.UserInterest;
import com.travel.mapper.UserInterestMapper;
import com.travel.service.UserInterestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户兴趣服务实现
 * 管理用户的偏好标签，支持基于行为的自动兴趣建模
 */
@Slf4j
@Service("userInterestService")
public class UserInterestServiceImpl implements UserInterestService {

    @Autowired
    private UserInterestMapper userInterestMapper;

    /**
     * 获取用户兴趣Map（带缓存）
     * Cacheable: 缓存键为userId，缓存名称为userInterests
     */
    @Override
    @Cacheable(value = "userInterests", key = "#userId", unless = "#result == null")
    public Map<String, Double> getUserInterests(Long userId) {
        List<UserInterest> interests = getUserInterestList(userId);

        return interests.stream()
                .collect(Collectors.toMap(
                        UserInterest::getInterestTag,
                        UserInterest::getWeight,
                        (existing, replacement) -> existing + replacement, // 处理重复标签
                        HashMap::new
                ));
    }

    @Override
    public List<UserInterest> getUserInterestList(Long userId) {
        return userInterestMapper.selectByUserId(userId);
    }

    /**
     * 添加或更新兴趣（带缓存更新）
     * CachePut: 方法执行后更新缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = "userInterests", key = "#userId")
    public void addOrUpdateInterest(Long userId, String interestTag, Double weight) {
        if (interestTag == null || interestTag.trim().isEmpty()) {
            return;
        }

        interestTag = interestTag.trim().toLowerCase(); // 统一小写存储

        // 查询是否已存在
        UserInterest existing = userInterestMapper.selectByUserIdAndTag(userId, interestTag);

        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            // 新增兴趣
            UserInterest interest = UserInterest.builder()
                    .userId(userId)
                    .interestTag(interestTag)
                    .weight(Math.min(weight, 5.0)) // 权重上限5.0
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userInterestMapper.insert(interest);
            log.info("新增用户兴趣: userId={}, tag={}, weight={}", userId, interestTag, weight);
        } else {
            // 更新权重（累加或替换策略，此处采用平滑累加）
            double newWeight = Math.min(existing.getWeight() + weight * 0.3, 5.0);
            userInterestMapper.updateWeight(userId, interestTag, newWeight, now);
            log.debug("更新用户兴趣: userId={}, tag={}, newWeight={}", userId, interestTag, newWeight);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "userInterests", key = "#userId")
    public void removeInterest(Long userId, String interestTag) {
        userInterestMapper.deleteByUserIdAndTag(userId, interestTag);
        log.info("删除用户兴趣: userId={}, tag={}", userId, interestTag);
    }

    /**
     * 基于行为计算兴趣
     * 根据用户对目的地的行为（浏览、收藏等）反推兴趣标签
     */
    @Override
    @Transactional
    public void calculateInterestFromBehavior(Long userId, Long destinationId, String action) {
        // 实际实现中，这里会查询目的地的标签，并调用addOrUpdateInterest
        // 为避免循环依赖，此逻辑在TopKRecommendationServiceImpl中实现
        log.debug("触发兴趣计算: userId={}, destId={}, action={}", userId, destinationId, action);
    }

    @Override
    @CacheEvict(value = "userInterests", key = "#userId")
    public void clearInterestCache(Long userId) {
        log.info("清除用户兴趣缓存: userId={}", userId);
    }
}
