package com.travel.mapper;

import com.travel.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Insert("INSERT INTO user(username, password, nickname, created_at, updated_at) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{createdAt}, #{updatedAt})")
    void insert(User user);
}
