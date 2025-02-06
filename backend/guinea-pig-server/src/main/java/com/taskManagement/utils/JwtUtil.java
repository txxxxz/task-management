package com.taskManagement.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import com.taskManagement.properties.JwtProperties;


public class JwtUtil {

    /**
     * 生成JWT
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
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
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
        
        return jwt;
    }
    
      /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 得到DefaultJwtParser
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
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
        try {
            Claims claims = parseJWT(secretKey, token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
} 