package com.travel.config;

import com.travel.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",           // 登录注册放行
                        "/api/destination/**",    // 景点查询允许游客
                        "/api/recommendation/**", // 推荐列表允许游客（可按需调整）
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error"
                );
    }
}
