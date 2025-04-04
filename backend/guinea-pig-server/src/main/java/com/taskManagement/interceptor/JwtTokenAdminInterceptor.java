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
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT令牌校验的拦截器（管理员）
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private Environment environment;

    /**
     * 校验jwt
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是处理器方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 检查是否为测试环境，如果是测试环境则自动通过认证
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equals(profile)) {
                log.info("测试环境，自动通过管理员JWT验证");
                // 将管理员ID设置为1
                BaseContext.setCurrentId(1L);
                return true;
            }
        }

        // 获取请求头中的token
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        
        // 如果token为空
        if (token == null || token.isEmpty()) {
            log.warn("Admin token is missing in request header");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing admin authentication token");
            return false;
        }

        try {
            log.info("Validating admin JWT token: {}", token);
            
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getAdminSecretKey(), token)) {
                log.warn("Invalid or expired admin token");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Invalid or expired admin token");
                return false;
            }

            // 解析token
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            
            // 获取管理员ID
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("Authenticated admin ID: {}", empId);
            
            // 获取用户角色
            Integer role = claims.get("role") != null ?
                    Integer.valueOf(claims.get("role").toString()) : null;

            if (role == null || role < 2) { // 假设2是管理员角色
                log.warn("User does not have admin role");
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("Access denied: Admin role required");
                return false;
            }

            log.info("Admin role verified: {}", role);
            
            // 将管理员ID存入ThreadLocal
            BaseContext.setCurrentId(empId);
            
            return true;
            
        } catch (ExpiredJwtException e) {
            log.warn("Admin JWT token has expired: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Admin token has expired");
            return false;
            
        } catch (SignatureException e) {
            log.error("Admin JWT signature validation failed: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid admin token signature");
            return false;
            
        } catch (Exception e) {
            log.error("Admin JWT validation error: {}", e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write("Internal server error during admin authentication");
            return false;
        }
    }

    /**
     * 请求完成后清理ThreadLocal
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
