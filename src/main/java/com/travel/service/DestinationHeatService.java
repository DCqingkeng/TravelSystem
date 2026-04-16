package com.travel.service;

import java.util.Set;

/**
 * 目的地热度计算与更新服务
 * 支持实时热度更新和周期性持久化
 */
public interface DestinationHeatService {

    /**
     * 增加目的地热度值（异步实时更新到Redis）
     *
     * @param destinationId 目的地ID
     * @param delta         热度增量
     */
    void incrementHeatScore(Long destinationId, Double delta);

    /**
     * 批量获取热门目的地（实时排行榜）
     *
     * @param topK 前K个
     * @return 目的地ID集合
     */
    Set<Long> getHotDestinations(int topK);

    /**
     * 同步Redis热度数据到MySQL（定时任务调用）
     */
    void syncHeatToDatabase();

    /**
     * 获取目的地的实时热度排名
     *
     * @param destinationId 目的地ID
     * @return 排名（从1开始）
     */
    Long getHeatRank(Long destinationId);

    /**
     * 初始化热度数据（系统启动时调用）
     */
    void initializeHeatData();
}
