package com.travel.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 目的地实体类（景区/学校）
 * 对应数据库表：destination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Destination implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 目的地名称
     */
    private String name;

    /**
     * 类型：SCENIC_SPOT(景区) / SCHOOL(学校)
     */
    private DestinationType type;

    /**
     * 类别标签（如：自然风光、历史文化、985高校等）
     */
    private String category;

    /**
     * 热度得分（基于浏览量、收藏数等计算，范围0-10000）
     */
    private Double heatScore;

    /**
     * 平均评分（1-5分）
     */
    private Double rating;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 关键词标签，逗号分隔（如：古建筑,摄影,樱花）
     */
    private String keywords;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 综合推荐得分（动态计算，不持久化到数据库）
     * 用于Top-K算法中的排序比较
     * @JsonIgnore 防止序列化到前端时暴露中间计算值
     */
    @JsonIgnore
    private transient Double compositeScore;

    /**
     * 与用户兴趣的匹配度（动态计算，0-100）
     */
    @JsonIgnore
    private transient Double interestMatchScore;

    /**
     * 距离当前位置的距离（米，动态计算）
     * 用于附近推荐场景
     */
    @JsonIgnore
    private transient Double distance;
}
