package com.travel.config;

import com.travel.service.TravelDiaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DiaryIndexInitializer implements CommandLineRunner {

    @Autowired
    private TravelDiaryService diaryService;

    @Override
    public void run(String... args) {
        log.info("系统启动：初始化日记索引...");
        diaryService.buildSearchIndex();
        log.info("系统启动：日记索引初始化完成");
    }
}