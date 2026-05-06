package com.travel.entity.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DiaryCreateRequest {
    private Long destinationId;
    private String title;
    private String content;
    private String keywords;
    private LocalDate travelDate;
    private List<String> mediaUrls;  // 图片/视频URL列表
    private Boolean compress;        // 是否启用压缩存储
}
