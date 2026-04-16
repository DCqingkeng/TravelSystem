package com.travel.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户兴趣实体类
 * 记录用户的偏好标签及权重
 * 对应数据库表：user_interest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID（关联用户表）
     */
    private Long userId;

    /**
     * 兴趣标签（如：摄影、美食、历史、徒步等）
     */
    private String interestTag;

    /**
     * 权重系数（范围0.1-5.0，默认1.0）
     * 权重越高表示用户对该兴趣越重视
     */
    private Double weight;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
