package com.travel.mapper;

import com.travel.entity.TravelDiary;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TravelDiaryMapper {

    @Insert("INSERT INTO travel_diary(user_id, destination_id, title, content, content_compressed, " +
            "cover_image, heat_score, avg_rating, rating_count, keywords, travel_date, created_at, updated_at) " +
            "VALUES(#{userId}, #{destinationId}, #{title}, #{content}, #{contentCompressed}, " +
            "#{coverImage}, #{heatScore}, #{avgRating}, #{ratingCount}, #{keywords}, #{travelDate}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TravelDiary diary);

    @Select("SELECT * FROM travel_diary WHERE id = #{id}")
    TravelDiary selectById(Long id);

    @Select("SELECT * FROM travel_diary WHERE title = #{title} LIMIT 1")
    TravelDiary selectByTitleExact(String title);

    @Select("SELECT * FROM travel_diary WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<TravelDiary> selectByUserId(Long userId);

    @Select("SELECT * FROM travel_diary WHERE destination_id = #{destinationId} ORDER BY heat_score DESC LIMIT #{limit}")
    List<TravelDiary> selectByDestinationId(@Param("destinationId") Long destinationId, @Param("limit") int limit);

    @Select("SELECT * FROM travel_diary WHERE (title LIKE CONCAT('%', #{keyword}, '%') OR keywords LIKE CONCAT('%', #{keyword}, '%')) ORDER BY heat_score DESC LIMIT #{limit}")
    List<TravelDiary> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);

    @Update("UPDATE travel_diary SET heat_score = #{heatScore} WHERE id = #{id}")
    void updateHeatScore(@Param("id") Long id, @Param("heatScore") Double heatScore);

    @Update("UPDATE travel_diary SET avg_rating = #{avgRating}, rating_count = #{ratingCount} WHERE id = #{id}")
    void updateRating(@Param("id") Long id, @Param("avgRating") Double avgRating, @Param("ratingCount") Integer ratingCount);

    @Select("SELECT * FROM travel_diary ORDER BY created_at DESC LIMIT #{limit}")
    List<TravelDiary> selectRecent(int limit);
}