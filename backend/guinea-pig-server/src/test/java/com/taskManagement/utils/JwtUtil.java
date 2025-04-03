package com.taskManagement.utils;

import java.util.Map;

/**
 * JWT工具类的测试替代品
 * 用于测试环境，避免使用真实的JWT处理
 */
public class JwtUtil {
    
    /**
     * 创建JWT令牌
     * @param secretKey 密钥
     * @param ttl 有效期
     * @param claims 载荷
     * @return 令牌
     */
    public static String createJWT(String secretKey, long ttl, Map<String, Object> claims) {
        return "test-token-" + (claims != null ? claims.get("userId") : "unknown");
    }
    
    /**
     * 验证JWT令牌
     * @param secretKey 密钥
     * @param token 令牌
     * @return 是否有效
     */
    public static boolean validateToken(String secretKey, String token) {
        // 在测试中由mock控制返回值
        return true;
    }
    
    /**
     * 从JWT令牌获取用户ID
     * @param secretKey 密钥
     * @param token 令牌
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String secretKey, String token) {
        // 在测试中由mock控制返回值
        return 1L;
    }
} 