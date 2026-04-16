package com.travel.entity.dto;

import com.travel.entity.DestinationType;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 旅游推荐请求DTO
 * 用于接收前端传来的推荐查询参数
 */
@Data
public class RecommendationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（必需）
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 目的地类型筛选（可选）
     * SCENIC_SPOT: 景区
     * SCHOOL: 学校
     */
    private DestinationType type;

    /**
     * 搜索关键词（可选）
     * 支持名称、类别、关键字模糊匹配
     */
    private String keyword;

    /**
     * 需要返回的前K个结果（可选，默认10，最大50）
     * 核心算法将使用Top-K堆排序，时间复杂度O(n log k)
     */
    @Min(value = 1, message = "至少返回1条结果")
    @Max(value = 50, message = "最多返回50条结果")
    private Integer topK = 10;

    /**
     * 热度权重（可选，用于自定义排序策略）
     * 默认0.3，范围0-1
     */
    @Min(0)
    @Max(1)
    private Double heatWeight = 0.3;

    /**
     * 评分权重（可选）
     * 默认0.4，范围0-1
     */
    @Min(0)
    @Max(1)
    private Double ratingWeight = 0.4;

    /**
     * 兴趣匹配权重（可选）
     * 默认0.3，范围0-1
     */
    @Min(0)
    @Max(1)
    private Double interestWeight = 0.3;
}