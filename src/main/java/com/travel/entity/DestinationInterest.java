package com.travel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 景点-兴趣标签匹配实体（预计算表）
 * 用于加速推荐算法中的兴趣匹配计算
 * 对应数据库表：destination_interest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationInterest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 景点ID
     */
    private Long destinationId;

    /**
     * 兴趣标签
     */
    private String interestTag;

    /**
     * 匹配得分（0-100）
     * 表示该景点与该兴趣标签的相关程度
     */
    private Double matchScore;
}
