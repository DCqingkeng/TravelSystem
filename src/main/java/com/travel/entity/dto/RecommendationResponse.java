package com.travel.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 推荐结果响应DTO
 * 比Destination实体更精简，只返回前端需要的字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目的地ID
     */
    private Long id;

    /**
     * 目的地名称
     */
    private String name;

    /**
     * 类型中文名（"景区"或"学校"）
     */
    private String typeName;

    /**
     * 类别标签（如：自然风光、历史文化、985高校等）
     */
    private String category;

    /**
     * 平均评分（1-5分）
     */
    private Double rating;

    /**
     * 热度值（用于展示热度标识，如"热度: 9999"）
     */
    private Double heatScore;

    /**
     * 距离当前位置的距离文本（如"距您 2.5km"）
     */
    private String distanceText;

    /**
     * 关键词标签列表（从keywords字符串拆分）
     */
    private List<String> tags;

    /**
     * 经度（用于地图展示）
     */
    private Double longitude;

    /**
     * 纬度（用于地图展示）
     */
    private Double latitude;

    /**
     * 简要描述（列表页展示，限制长度）
     */
    private String briefDesc;

    /**
     * 综合推荐得分（0-100，可选返回用于调试）
     */
    private Double compositeScore;

    /**
     * 兴趣匹配度（0-100，展示用户兴趣匹配程度）
     */
    private Double interestMatchScore;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 推荐理由（可前端展示，如"符合您的摄影兴趣"、"热度超高"）
     */
    private String recommendReason;
}