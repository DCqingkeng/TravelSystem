package com.travel.mapper;

import com.travel.entity.DiaryRating;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiaryRatingMapper {

    @Insert("INSERT INTO diary_rating(diary_id, user_id, score, comment, created_at) " +
            "VALUES(#{diaryId}, #{userId}, #{score}, #{comment}, #{createdAt})")
    void insert(DiaryRating rating);

    // 关键修复：第二个参数 @Param 原来是 "tag"，改为 "userId"
    @Select("SELECT * FROM diary_rating WHERE diary_id = #{diaryId} AND user_id = #{userId}")
    DiaryRating selectByDiaryAndUser(@Param("diaryId") Long diaryId, @Param("userId") Long userId);

    @Select("SELECT * FROM diary_rating WHERE diary_id = #{diaryId}")
    List<DiaryRating> selectByDiaryId(Long diaryId);

    @Update("UPDATE diary_rating SET score = #{score}, comment = #{comment} WHERE id = #{id}")
    void update(DiaryRating rating);

    @Select("SELECT AVG(score) FROM diary_rating WHERE diary_id = #{diaryId}")
    Double selectAvgScore(Long diaryId);

    @Select("SELECT COUNT(*) FROM diary_rating WHERE diary_id = #{diaryId}")
    Integer selectCountByDiaryId(Long diaryId);
}