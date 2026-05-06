package com.travel.controller;

import com.travel.entity.TravelDiary;
import com.travel.entity.dto.DiaryCreateRequest;
import com.travel.entity.dto.DiaryListItem;
import com.travel.entity.dto.DiaryRatingRequest;
import com.travel.entity.dto.Result;
import com.travel.service.TravelDiaryService;
import com.travel.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diary")
@Tag(name = "旅游日记管理", description = "日记撰写、浏览、评分、检索")
@SecurityRequirement(name = "BearerAuth")
public class TravelDiaryController {

    @Autowired
    private TravelDiaryService diaryService;

    @PostMapping("/create")
    @Operation(summary = "撰写日记")
    public Result<TravelDiary> create(@RequestBody DiaryCreateRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "未登录");
        return Result.success(diaryService.createDiary(userId, request), "发布成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看日记详情", description = "返回完整内容（自动解压）")
    public Result<TravelDiary> detail(
            @PathVariable Long id,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        Long userId = UserContext.getUserId();
        TravelDiary diary = diaryService.getDiaryDetail(id, userId, ip);
        if (diary == null) return Result.error(404, "日记不存在");
        return Result.success(diary);
    }

    @GetMapping("/exact")
    @Operation(summary = "精确查询日记名称")
    public Result<TravelDiary> exactSearch(@RequestParam String title) {
        TravelDiary diary = diaryService.getByTitleExact(title);
        if (diary == null) return Result.error(404, "未找到该日记");
        return Result.success(diary);
    }

    /**
     * 列表接口：返回精简数据，不含完整正文
     */
    @GetMapping("/content-search")
    @Operation(summary = "全文检索日记内容")
    public Result<List<DiaryListItem>> contentSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int topK) {
        List<TravelDiary> list = diaryService.searchByContent(keyword, topK);
        return Result.success(convertToListItems(list));
    }

    @GetMapping("/hot")
    @Operation(summary = "热门日记推荐")
    public Result<List<DiaryListItem>> hot(@RequestParam(defaultValue = "10") int topK) {
        List<TravelDiary> list = diaryService.listDiariesByHeat(topK);
        return Result.success(convertToListItems(list));
    }

    @GetMapping("/search")
    @Operation(summary = "按目的地查询日记")
    public Result<List<DiaryListItem>> searchByDestination(
            @RequestParam String destinationName,
            @RequestParam(defaultValue = "10") int topK) {
        List<TravelDiary> list = diaryService.searchDiariesByDestination(destinationName, topK);
        return Result.success(convertToListItems(list));
    }

    @PostMapping("/rate")
    @Operation(summary = "评分日记")
    public Result<Void> rate(@RequestBody DiaryRatingRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error(401, "未登录");
        diaryService.rateDiary(userId, request);
        return Result.success(null, "评分成功");
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "日记统计")
    public Result<Map<String, Object>> statistics(@PathVariable Long id) {
        return Result.success(diaryService.getDiaryStatistics(id));
    }

    // ========== 私有转换方法 ==========

    private List<DiaryListItem> convertToListItems(List<TravelDiary> source) {
        if (source == null) return null;
        return source.stream().map(this::convertToListItem).collect(Collectors.toList());
    }

    private DiaryListItem convertToListItem(TravelDiary d) {
        DiaryListItem item = new DiaryListItem();
        item.setId(d.getId());
        item.setUserId(d.getUserId());
        item.setDestinationId(d.getDestinationId());
        item.setTitle(d.getTitle());
        item.setCoverImage(d.getCoverImage());
        item.setHeatScore(d.getHeatScore());
        item.setAvgRating(d.getAvgRating());
        item.setRatingCount(d.getRatingCount());
        item.setKeywords(d.getKeywords());
        item.setCreatedAt(d.getCreatedAt());

        // 生成摘要：从正文截取前100字（去掉HTML标签）
        if (StringUtils.hasText(d.getContent())) {
            String text = d.getContent().replaceAll("<[^>]+>", "");
            item.setBriefDesc(text.length() > 100 ? text.substring(0, 100) + "..." : text);
        }

        return item;
    }
}