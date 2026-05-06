package com.travel.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日记列表项（精简版）
 * 用于列表页展示，不返回完整正文，减少传输量
 */
@Data
public class DiaryListItem {

    private Long id;
    private Long userId;
    private Long destinationId;
    private String title;
    private String coverImage;
    private Double heatScore;
    private Double avgRating;
    private Integer ratingCount;
    private String keywords;

    /**
     * 正文摘要（列表页预览用，由后端从正文截取）
     */
    private String briefDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
