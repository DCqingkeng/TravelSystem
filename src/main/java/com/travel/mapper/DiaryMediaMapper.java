package com.travel.mapper;

import com.travel.entity.DiaryMedia;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DiaryMediaMapper {

    @Insert("INSERT INTO diary_media(diary_id, media_type, media_url, sort_order) " +
            "VALUES(#{diaryId}, #{mediaType}, #{mediaUrl}, #{sortOrder})")
    void insert(DiaryMedia media);

    @Select("SELECT * FROM diary_media WHERE diary_id = #{diaryId} ORDER BY sort_order")
    List<DiaryMedia> selectByDiaryId(Long diaryId);

    @Insert("<script>" +
            "INSERT INTO diary_media(diary_id, media_type, media_url, sort_order) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.diaryId}, #{item.mediaType}, #{item.mediaUrl}, #{item.sortOrder})" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("list") List<DiaryMedia> list);
}
