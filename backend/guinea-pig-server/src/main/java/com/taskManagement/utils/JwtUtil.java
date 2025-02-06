package com.taskManagement.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET_KEY = "guineapig";
    
    /**
     * 生成JWT
     */
    public static String createJWT(long ttlMillis, Map<String, Object> claims) {
        // 指定签名的时候使用的签名算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        
        // 生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        
        // 创建JWT的构建器
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(nowMillis + ttlMillis))
                .signWith(signatureAlgorithm, SECRET_KEY)
                .compact();
        
        return jwt;
    }
    
    /**
     * 解析JWT
     */
    public static Claims parseJWT(String jwt) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(jwt)
                .getBody();
    }
    
    /**
     * 从token中获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseJWT(token);
        return Long.valueOf(claims.get("userId").toString());
    }
    
    /**
     * 从token中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = parseJWT(token);
        return claims.get("username").toString();
    }
    
    /**
     * 从token中获取用户角色
     */
    public static Integer getRoleFromToken(String token) {
        Claims claims = parseJWT(token);
        return Integer.valueOf(claims.get("role").toString());
    }
    
    /**
     * 验证token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseJWT(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
} 