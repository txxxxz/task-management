package com.taskManagement.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT工具类的测试替代品
 * 用于测试环境，避免使用真实的JWT处理
 */
public class JwtUtil {
    
    // 静态映射表，保存token和userId的对应关系
    private static Map<String, Long> tokenToUserIdMap = new ConcurrentHashMap<>();
    private static Map<Long, String> userIdToTokenMap = new ConcurrentHashMap<>();
    private static Map<String, Map<String, Object>> tokenToClaimsMap = new ConcurrentHashMap<>();
    
    // 初始化一些测试数据（模拟已经存在的用户token）
    static {
        // 初始化一些测试用户的token
        Map<String, Object> testUserClaims = new HashMap<>();
        testUserClaims.put("userId", 7L);
        testUserClaims.put("username", "testUser");
        testUserClaims.put("role", 0);
        String testUserToken = "test-token-7";
        tokenToUserIdMap.put(testUserToken, 7L);
        userIdToTokenMap.put(7L, testUserToken);
        tokenToClaimsMap.put(testUserToken, testUserClaims);
        
        // 初始化管理员用户的token
        Map<String, Object> adminClaims = new HashMap<>();
        adminClaims.put("userId", 8L);
        adminClaims.put("username", "adminUser");
        adminClaims.put("role", 2);
        String adminToken = "test-token-8";
        tokenToUserIdMap.put(adminToken, 8L);
        userIdToTokenMap.put(8L, adminToken);
        tokenToClaimsMap.put(adminToken, adminClaims);
    }
    
    /**
     * 创建JWT令牌
     * @param secretKey 密钥
     * @param ttl 有效期
     * @param claims 载荷
     * @return 令牌
     */
    public static String createJWT(String secretKey, long ttl, Map<String, Object> claims) {
        if (claims == null || !claims.containsKey("userId")) {
            throw new RuntimeException("创建JWT令牌失败：claims中缺少userId");
        }
        
        Long userId = Long.valueOf(claims.get("userId").toString());
        // 使用固定格式的token
        String token = "test-token-" + userId;
        
        // 保存token和userId的映射关系
        tokenToUserIdMap.put(token, userId);
        userIdToTokenMap.put(userId, token);
        
        // 保存token和claims的映射关系（拷贝claims避免后续修改影响）
        Map<String, Object> claimsCopy = new HashMap<>(claims);
        tokenToClaimsMap.put(token, claimsCopy);
        
        return token;
    }
    
    /**
     * 验证JWT令牌
     * @param secretKey 密钥
     * @param token 令牌
     * @return 是否有效
     */
    public static boolean validateToken(String secretKey, String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // 特殊处理invalid-token，用于测试无效token
        if ("invalid-token".equals(token)) {
            return false;
        }
        
        // 验证token是否存在于映射表中
        return tokenToUserIdMap.containsKey(token);
    }
    
    /**
     * 从JWT令牌获取用户ID
     * @param secretKey 密钥
     * @param token 令牌
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String secretKey, String token) {
        if (!validateToken(secretKey, token)) {
            throw new RuntimeException("无效的token");
        }
        
        return tokenToUserIdMap.get(token);
    }
    
    /**
     * 解析JWT令牌
     * @param secretKey JWT密钥
     * @param token JWT令牌
     * @return Claims对象
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 验证token有效性
        if (!validateToken(secretKey, token)) {
            throw new RuntimeException("无效的token");
        }
        
        // 创建Claims对象
        DefaultClaims claims = new DefaultClaims();
        
        // 从映射表中获取用户信息
        Map<String, Object> userClaims = tokenToClaimsMap.get(token);
        if (userClaims != null) {
            for (Map.Entry<String, Object> entry : userClaims.entrySet()) {
                claims.put(entry.getKey(), entry.getValue());
            }
        } else {
            // 如果没有找到对应的claims，使用userId创建基本claims
            Long userId = tokenToUserIdMap.get(token);
            claims.put("userId", userId);
            claims.put("username", "test-user-" + userId);
            claims.put("role", userId == 8L ? 2 : 0); // 8是管理员，其他是普通用户
        }
        
        // 设置基本JWT字段
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + 3600000)); // 1小时后过期
        
        return claims;
    }
    
    /**
     * 从token中获取用户名
     */
    public static String getUsernameFromToken(String secretKey, String token) {
        Claims claims = parseJWT(secretKey, token);
        return claims.get("username").toString();
    }

    /**
     * 从token中获取用户角色
     */
    public static Integer getRoleFromToken(String secretKey, String token) {
        Claims claims = parseJWT(secretKey, token);
        return Integer.valueOf(claims.get("role").toString());
    }
    
    /**
     * 仅用于测试：清除所有token映射
     */
    public static void clearAllTokens() {
        tokenToUserIdMap.clear();
        userIdToTokenMap.clear();
        tokenToClaimsMap.clear();
    }
} 