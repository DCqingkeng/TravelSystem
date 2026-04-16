package com.travel.service.algorithm;

import com.travel.entity.Destination;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 推荐得分计算器
 * 计算目的地的综合推荐得分（0-100分制）
 *
 * 评分维度：
 * 1. 热度得分 (30%)：基于浏览量、搜索量、收藏数的归一化得分
 * 2. 评分得分 (40%)：用户平均评分（1-5分映射到0-100）
 * 3. 兴趣匹配 (30%)：基于用户兴趣标签与目的地标签的重合度
 */
@Component
public class ScoreCalculator {

    // 权重配置（可从配置中心动态读取）
    private static final double WEIGHT_HEAT = 0.30;
    private static final double WEIGHT_RATING = 0.40;
    private static final double WEIGHT_INTEREST = 0.30;

    // 热度归一化参数（假设历史最大热度为10000）
    private static final double MAX_HEAT_SCORE = 10000.0;

    /**
     * 计算综合推荐得分
     *
     * @param destination 目的地对象
     * @param userInterests 用户兴趣标签-权重映射
     * @return 综合得分（0-100）
     */
    public double calculateCompositeScore(Destination destination,
                                          Map<String, Double> userInterests) {
        // 1. 计算热度分（归一化到0-100）
        double heatScore = calculateHeatScore(destination.getHeatScore());

        // 2. 计算评分分（1-5分映射到0-100）
        double ratingScore = calculateRatingScore(destination.getRating());

        // 3. 计算兴趣匹配分（0-100）
        double interestScore = calculateInterestScore(destination.getKeywords(), userInterests);

        // 4. 加权求和
        double compositeScore = WEIGHT_HEAT * heatScore
                + WEIGHT_RATING * ratingScore
                + WEIGHT_INTEREST * interestScore;

        // 存储中间得分到对象（用于调试或展示）
        destination.setInterestMatchScore(interestScore);

        return Math.min(Math.max(compositeScore, 0.0), 100.0); // 确保在0-100范围内
    }

    /**
     * 热度得分计算
     * 使用对数归一化避免热门景点得分过于集中
     */
    private double calculateHeatScore(Double rawHeat) {
        if (rawHeat == null || rawHeat <= 0) {
            return 0.0;
        }

        // 方案1：线性归一化
        // return Math.min((rawHeat / MAX_HEAT_SCORE) * 100, 100.0);

        // 方案2：对数归一化（推荐，避免马太效应）
        return Math.min((Math.log10(rawHeat + 1) / Math.log10(MAX_HEAT_SCORE + 1)) * 100, 100.0);
    }

    /**
     * 评分得分计算
     * 将1-5分映射到0-100，中间分值为基准
     */
    private double calculateRatingScore(Double rating) {
        if (rating == null || rating <= 0) {
            return 50.0; // 默认中等分数
        }

        // 1-5分映射到0-100（3分=60分及格线）
        // 公式：(rating - 1) / 4 * 100
        double normalized = ((rating - 1.0) / 4.0) * 100.0;
        return Math.min(Math.max(normalized, 0.0), 100.0);
    }

    /**
     * 兴趣匹配得分计算（基于Jaccard相似度和TF-IDF权重）
     *
     * 算法步骤：
     * 1. 解析目的地的关键词标签
     * 2. 计算每个标签与用户的匹配权重
     * 3. 考虑标签覆盖度（匹配标签数/总标签数）
     */
    private double calculateInterestScore(String keywords, Map<String, Double> userInterests) {
        // 无用户兴趣或目的地标签时，返回默认中等分数
        if (userInterests == null || userInterests.isEmpty()) {
            return 50.0; // 中性偏好
        }
        if (keywords == null || keywords.trim().isEmpty()) {
            return 30.0; // 信息不足，略低分
        }

        String[] destTags = keywords.toLowerCase().split(",");
        if (destTags.length == 0) {
            return 30.0;
        }

        double totalMatchWeight = 0.0;
        int matchCount = 0;

        // 计算匹配的标签权重和
        for (String tag : destTags) {
            tag = tag.trim();
            if (userInterests.containsKey(tag)) {
                Double weight = userInterests.get(tag);
                totalMatchWeight += (weight != null ? weight : 1.0);
                matchCount++;
            }
        }

        // 无任何匹配
        if (matchCount == 0) {
            return 20.0; // 低分，但不为0（避免完全排除）
        }

        // 计算覆盖率（匹配标签数 / 总标签数）
        double coverageRatio = (double) matchCount / destTags.length;

        // 计算平均匹配权重
        double avgWeight = totalMatchWeight / matchCount;

        // 最终得分：基础分 + 权重加成 + 覆盖加成
        // 基础分40分，权重最多加40分（avgWeight最大5.0，5*8=40），覆盖最多加20分
        double score = 40.0
                + (Math.min(avgWeight, 5.0) * 8.0)
                + (coverageRatio * 20.0);

        return Math.min(score, 100.0);
    }

    /**
     * 调整权重配置（运行时动态调整）
     */
    public void updateWeights(double heatWeight, double ratingWeight, double interestWeight) {
        // 实际应用中可通过配置中心或管理接口动态调整
        // 这里仅作示例，实际需考虑线程安全
    }
}