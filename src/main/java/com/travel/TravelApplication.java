package com.travel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.travel.mapper") // 必须保留
public class TravelApplication {

    public static void main(String[] args) {
        // 启动SpringBoot
        SpringApplication.run(TravelApplication.class, args);
    }
}
