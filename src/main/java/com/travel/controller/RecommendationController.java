package com.travel.controller;

import com.travel.entity.Destination;
import com.travel.entity.DestinationType;
import com.travel.entity.dto.RecommendationRequest;
import com.travel.entity.dto.Result;
import com.travel.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 旅游推荐控制器
 * 处理目的地推荐、算法测试、用户行为记录等接口
 */
@RestController
@RequestMapping("/api/recommendation")
@Tag(name = "旅游推荐", description = "目的地Top-K推荐与算法分析")
@Validated
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 核心接口：获取Top-K推荐目的地
     * 使用基于最小堆的部分排序算法，时间复杂度O(n log k)
     */
    @GetMapping("/top-destinations")
    @Operation(
            summary = "获取Top-K推荐目的地",
            description = "基于热度、评分、用户兴趣的综合推荐。核心算法：最小堆部分排序O(n log k)"
    )
    public Result<List<Destination>> getTopDestinations(
            @Parameter(description = "用户ID", required = true, example = "1")
            @RequestParam Long userId,

            @Parameter(description = "类型：SCENIC_SPOT/SCHOOL")
            @RequestParam(required = false) DestinationType type,

            @Parameter(description = "搜索关键词（如：摄影、古建筑、美食）", example = "摄影")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "返回前K个结果（默认10，最大50）", example = "10")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "至少返回1条结果")
            @Max(value = 50, message = "最多返回50条结果") Integer topK) {

        long startTime = System.currentTimeMillis();

        List<Destination> recommendations = recommendationService.recommendTopK(
                userId, type, keyword, topK);

        long costTime = System.currentTimeMillis() - startTime;

        return Result.success(recommendations)
                .setExtra("algorithm", "Top-K Min-Heap")
                .setExtra("time_complexity", "O(n log k)")
                .setExtra("cost_ms", costTime)
                .setExtra("result_count", recommendations.size());
    }

    /**
     * 算法性能对比测试
     * 对比 Top-K堆算法 vs 完全排序的性能差异
     */
    @GetMapping("/performance-test")
    @Operation(
            summary = "算法性能对比",
            description = "测试不同数据量下Top-K堆算法与完全排序的时间差异，验证O(n log k)效率优势"
    )
    public Result<Map<String, Object>> compareAlgorithms(
            @Parameter(description = "测试用户ID", required = true, example = "1")
            @RequestParam Long userId) {

        Map<String, Object> report = recommendationService.getAlgorithmPerformanceReport(userId);
        return Result.success(report);
    }

    /**
     * 记录用户行为并更新兴趣模型
     * 当用户浏览、收藏、评分某个目的地时调用
     */
    @PostMapping("/record-behavior")
    @Operation(
            summary = "记录用户行为",
            description = "记录浏览/收藏/评分行为，自动更新用户兴趣权重和目的地热度（异步处理）"
    )
    public Result<Void> recordBehavior(
            @Parameter(description = "用户ID", required = true, example = "1")
            @RequestParam Long userId,

            @Parameter(description = "目的地ID", required = true, example = "1")
            @RequestParam Long destinationId,

            @Parameter(description = "行为类型：VIEW(浏览)/FAVORITE(收藏)/RATING(评分)/SHARE(分享)",
                    schema = @Schema(allowableValues = {"VIEW", "FAVORITE", "RATING", "SHARE"}),
                    example = "VIEW")
            @RequestParam String action,

            @Parameter(description = "行为权重（默认1.0，评分时可传具体分值）", example = "1.0")
            @RequestParam(defaultValue = "1.0") Double weight) {

        recommendationService.updateUserInterest(userId, destinationId, action, weight);
        return Result.success(null, "行为记录成功，兴趣模型已更新");
    }

    /**
     * POST方式获取推荐（适用于复杂参数场景）
     */
    @PostMapping("/recommend")
    @Operation(summary = "POST方式获取推荐", description = "适用于前端需要传递复杂参数或自定义权重的场景")
    public Result<List<Destination>> recommendByPost(
            @Valid @RequestBody RecommendationRequest request) {

        long startTime = System.currentTimeMillis();

        List<Destination> recommendations = recommendationService.recommendTopK(
                request.getUserId(),
                request.getType(),
                request.getKeyword(),
                Math.min(request.getTopK(), 50));

        long costTime = System.currentTimeMillis() - startTime;

        return Result.success(recommendations)
                .setExtra("cost_ms", costTime);
    }

    /**
     * 刷新推荐缓存
     * 当管理员批量更新数据后调用
     */
    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新推荐缓存", description = "清除所有推荐缓存，下次请求重新计算（管理员维护用）")
    public Result<Void> refreshCache() {
        recommendationService.refreshRecommendationCache();
        return Result.success(null, "推荐缓存已刷新");
    }
}