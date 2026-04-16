package com.travel.controller;

import com.travel.entity.Destination;
import com.travel.entity.DestinationType;
import com.travel.entity.dto.Result;
import com.travel.mapper.DestinationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 目的地管理控制器
 * 提供目的地详情查询、搜索等基础功能
 */
@RestController
@RequestMapping("/api/destination")
@Tag(name = "目的地管理", description = "景区/学校信息查询")
public class DestinationController {

    @Autowired
    private DestinationMapper destinationMapper;

    /**
     * 根据ID获取目的地详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取目的地详情")
    public Result<Destination> getDestinationById(
            @Parameter(description = "目的地ID", required = true) @PathVariable Long id) {

        Destination destination = destinationMapper.selectById(id);
        if (destination == null) {
            return Result.error(404, "目的地不存在");
        }
        return Result.success(destination);
    }

    /**
     * 按类型获取热门目的地（直接查询，不经过推荐算法）
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门目的地", description = "按热度排序返回，直接查询数据库")
    public Result<List<Destination>> getHotDestinations(
            @Parameter(description = "类型：SCENIC_SPOT/SCHOOL") @RequestParam(required = false) DestinationType type,
            @Parameter(description = "数量限制，默认10") @RequestParam(defaultValue = "10") Integer limit) {

        List<Destination> list;
        if (type != null) {
            list = destinationMapper.findTopByTypeOrderByHeat(type, limit);
        } else {
            list = destinationMapper.findTopOrderByHeat(limit);
        }

        return Result.success(list);
    }

    /**
     * 关键词搜索目的地
     */
    @GetMapping("/search")
    @Operation(summary = "搜索目的地", description = "按名称/关键词/类别模糊搜索")
    public Result<List<Destination>> searchDestinations(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "类型筛选") @RequestParam(required = false) DestinationType type,
            @Parameter(description = "结果数量限制，默认20") @RequestParam(defaultValue = "20") Integer limit) {

        List<Destination> list = destinationMapper.searchByKeyword(type, keyword, limit);
        return Result.success(list);
    }
}