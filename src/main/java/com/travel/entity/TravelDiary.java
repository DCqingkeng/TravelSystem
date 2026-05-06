package com.travel.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelDiary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long destinationId;
    private String title;
    private String content;           // 日记内容（可能为压缩后的Base64字符串）
    private Integer contentCompressed; // 0-原文 1-压缩
    private String coverImage;
    private Double heatScore;
    private Double avgRating;
    private Integer ratingCount;
    private String keywords;
    private LocalDate travelDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 动态计算字段（不持久化）
    private transient Double compositeScore;
    private transient Double interestMatchScore;
}
