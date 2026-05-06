package com.travel.controller;

import com.travel.entity.User;
import com.travel.entity.dto.LoginRequest;
import com.travel.entity.dto.RegisterRequest;
import com.travel.entity.dto.Result;
import com.travel.mapper.UserMapper;
import com.travel.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "登录与注册")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Result.error(400, "用户名和密码不能为空");
        }

        if (userMapper.findByUsername(request.getUsername()) != null) {
            return Result.error(409, "用户名已存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        return Result.success(null, "注册成功");
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(401, "密码错误");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());

        return Result.success(data, "登录成功");
    }
}