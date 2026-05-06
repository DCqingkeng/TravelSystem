package com.travel.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DiaryViewMapper {

    @Insert("INSERT INTO diary_view(diary_id, user_id, ip_address) VALUES(#{diaryId}, #{userId}, #{ipAddress})")
    void insert(@Param("diaryId") Long diaryId, @Param("userId") Long userId, @Param("ipAddress") String ipAddress);

    @Select("SELECT COUNT(*) FROM diary_view WHERE diary_id = #{diaryId}")
    Integer selectViewCount(Long diaryId);
}
