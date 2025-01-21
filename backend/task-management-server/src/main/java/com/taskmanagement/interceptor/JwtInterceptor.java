package com.taskmanagement.interceptor;

import com.taskmanagement.utils.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果是OPTIONS请求，直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从请求头中获取token
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 验证token
            if (JwtUtil.validateToken(token)) {
                // 将用户ID存入request属性中
                request.setAttribute("userId", JwtUtil.getUserIdFromToken(token));
                return true;
            }
        }

        // token无效，返回401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
} 