package com.travel.entity.dto;

import lombok.Data;

@Data
public class DiaryRatingRequest {
    private Long diaryId;
    private Double score;   // 1-5
    private String comment;
}
