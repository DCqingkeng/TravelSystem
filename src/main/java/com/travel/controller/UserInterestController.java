package com.travel.controller;

import com.travel.entity.UserInterest;
import com.travel.entity.dto.Result;
import com.travel.service.UserInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户兴趣管理控制器
 * 管理用户的偏好标签，支持兴趣增删改查
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户兴趣管理", description = "用户偏好标签管理")
public class UserInterestController {

    @Autowired
    private UserInterestService userInterestService;

    /**
     * 获取用户的兴趣列表
     */
    @GetMapping("/{userId}/interests")
    @Operation(summary = "获取用户兴趣列表")
    public Result<List<UserInterest>> getUserInterests(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {

        List<UserInterest> interests = userInterestService.getUserInterestList(userId);
        return Result.success(interests);
    }

    /**
     * 获取用户兴趣映射（标签->权重）
     */
    @GetMapping("/{userId}/interest-map")
    @Operation(summary = "获取用户兴趣权重映射")
    public Result<Map<String, Double>> getUserInterestMap(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {

        Map<String, Double> interestMap = userInterestService.getUserInterests(userId);
        return Result.success(interestMap);
    }

    /**
     * 添加或更新用户兴趣
     */
    @PostMapping("/{userId}/interests")
    @Operation(summary = "添加/更新用户兴趣", description = "如果标签已存在则累加权重")
    public Result<Void> addUserInterest(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "兴趣标签", required = true) @RequestParam String interestTag,
            @Parameter(description = "权重，默认1.0") @RequestParam(defaultValue = "1.0") Double weight) {

        userInterestService.addOrUpdateInterest(userId, interestTag, weight);
        return Result.success(null, "兴趣添加成功");
    }

    /**
     * 删除用户兴趣
     */
    @DeleteMapping("/{userId}/interests")
    @Operation(summary = "删除用户兴趣")
    public Result<Void> removeUserInterest(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "兴趣标签", required = true) @RequestParam String interestTag) {

        userInterestService.removeInterest(userId, interestTag);
        return Result.success(null, "兴趣删除成功");
    }

    /**
     * 清除用户兴趣缓存
     */
    @PostMapping("/{userId}/clear-cache")
    @Operation(summary = "清除用户兴趣缓存")
    public Result<Void> clearUserCache(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {

        userInterestService.clearInterestCache(userId);
        return Result.success(null, "缓存已清除");
    }
}