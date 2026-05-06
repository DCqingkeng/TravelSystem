package com.travel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryMedia implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long diaryId;
    private String mediaType;  // IMAGE / VIDEO
    private String mediaUrl;
    private Integer sortOrder;
}
