package com.taskManagement.interceptor;

import com.taskManagement.constant.JwtClaimsConstant;
import com.taskManagement.context.BaseContext;
import com.taskManagement.properties.JwtProperties;
import com.taskManagement.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是处理器方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取请求头中的token
        String token = request.getHeader(jwtProperties.getUserTokenName());
        
        // 如果token为空
        if (token == null || token.isEmpty()) {
            log.warn("Token is missing in request header");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing authentication token");
            return false;
        }

        try {
            log.info("Validating JWT token: {}", token);
            
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                log.warn("Invalid or expired token");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Invalid or expired token");
                return false;
            }

            // 解析token
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            
            // 获取用户ID
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("User ID from token: {}", userId);
            
            // 获取用户角色
            Integer role = Integer.valueOf(claims.get("role").toString());
            log.info("User role: {}", role);
            
            // 将用户ID存入ThreadLocal
            BaseContext.setCurrentId(userId);
            
            return true;
            
        } catch (ExpiredJwtException e) {
            log.error("Token has expired", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token has expired");
            return false;
        } catch (SignatureException e) {
            log.error("Invalid token signature", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid token signature");
            return false;
        } catch (Exception e) {
            log.error("Error validating token", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Error validating token");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
