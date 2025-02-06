package com.taskManagement.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import com.taskManagement.constant.JwtClaimsConstant;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtil {

    /**
     * 生成JWT令牌
     * @param secretKey JWT密钥
     * @param ttlMillis 过期时间(毫秒)
     * @param claims 自定义信息
     * @return JWT令牌
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("TTL must be greater than 0");
        }

        try {
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            Date expiration = new Date(nowMillis + ttlMillis);

            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                    .compact();
        } catch (Exception e) {
            log.error("JWT token generation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create JWT token", e);
        }
    }

    /**
     * 解析JWT令牌
     * @param secretKey JWT密钥
     * @param token JWT令牌
     * @return Claims对象
     */
    public static Claims parseJWT(String secretKey, String token) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            return Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("JWT token parsing failed: {}", e.getMessage());
            throw new RuntimeException("Failed to parse JWT token", e);
        }
    }

    /**
     * 从token中获取用户ID
     */
    public static Long getUserIdFromToken(String secretKey, String token) {
        Claims claims = parseJWT(secretKey, token);
        return Long.valueOf(claims.get("userId").toString());
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
     * 验证token是否有效
     */
    public static boolean validateToken(String secretKey, String token) {
        if (secretKey == null || secretKey.isEmpty() || token == null || token.isEmpty()) {
            return false;
        }

        try {
            Claims claims = parseJWT(secretKey, token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired");
            return false;
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
