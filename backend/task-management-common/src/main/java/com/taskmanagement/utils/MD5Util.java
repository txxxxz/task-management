package com.taskmanagement.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    private static final String SALT = "taskmanagement";

    public static String encrypt(String password) {
        try {
            // 1. 将密码和盐值拼接
            String input = password + SALT;
            
            // 2. 创建 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            // 3. 计算 MD5 值
            byte[] bytes = md.digest(input.getBytes());
            
            // 4. 将字节数组转换为十六进制字符串
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    result.append("0");
                }
                result.append(hex);
            }
            
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }
} 