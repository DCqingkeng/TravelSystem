package com.travel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryRating implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long diaryId;
    private Long userId;
    private Double score;
    private String comment;
    private LocalDateTime createdAt;
}