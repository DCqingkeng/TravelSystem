package com.travel.mapper;

import com.travel.entity.UserInterest;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserInterestMapper {

    @Select("SELECT * FROM user_interest WHERE user_id = #{userId}")
    List<UserInterest> selectByUserId(Long userId);

    @Select("SELECT * FROM user_interest WHERE user_id = #{userId} AND interest_tag = #{tag}")
    UserInterest selectByUserIdAndTag(@Param("userId") Long userId, @Param("tag") String tag);

    @Insert("INSERT INTO user_interest(user_id, interest_tag, weight, created_at, updated_at) " +
            "VALUES(#{userId}, #{interestTag}, #{weight}, #{createdAt}, #{updatedAt})")
    void insert(UserInterest interest);

    @Update("UPDATE user_interest SET weight = #{weight}, updated_at = #{updatedTime} " +
            "WHERE user_id = #{userId} AND interest_tag = #{tag}")
    void updateWeight(@Param("userId") Long userId, @Param("tag") String tag,
                      @Param("weight") Double weight, @Param("updatedTime") LocalDateTime updatedTime);

    @Delete("DELETE FROM user_interest WHERE user_id = #{userId} AND interest_tag = #{tag}")
    void deleteByUserIdAndTag(@Param("userId") Long userId, @Param("tag") String tag);
}
