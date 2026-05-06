package com.travel.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.entity.dto.Result;
import com.travel.util.JwtUtil;
import com.travel.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = JwtUtil.parseToken(token);
            if (userId != null) {
                UserContext.setUserId(userId);
                return true;
            }
        }

        // Token 无效，返回 401
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(401, "未登录或Token已过期");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear(); // 必须清理，防止内存泄漏
    }
}
