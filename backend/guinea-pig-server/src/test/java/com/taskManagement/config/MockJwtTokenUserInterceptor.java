package com.taskManagement.config;

import com.taskManagement.constant.JwtClaimsConstant;
import com.taskManagement.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 测试环境使用的模拟JWT拦截器
 * 不检查令牌，直接设置用户ID为1
 */
@Component
@Slf4j
public class MockJwtTokenUserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果不是处理器方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        // 对于登录和注册请求直接放行
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/auth/login") || requestURI.contains("/auth/register")) {
            return true;
        }
        
        log.info("模拟JWT拦截器处理请求：{}", requestURI);
        
        // 获取token
        String token = request.getHeader("Authorization");
        log.info("接收到的Authorization头：{}", token);
        
        // 无论token如何，都设置用户ID，确保测试可以通过
        Long userId = 1L; // 默认用户ID，用于测试
        
        // 如果令牌是test-token-2，使用用户ID 2
        if (token != null && token.equals("test-token-2")) {
            userId = 2L;
        }
        
        log.info("模拟JWT验证通过，设置当前用户ID: {}", userId);
        
        // 将用户ID存入ThreadLocal
        BaseContext.setCurrentId(userId);
        
        // 始终返回true，不阻止任何请求
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 移除ThreadLocal中的用户ID
        BaseContext.removeCurrentId();
    }
} 