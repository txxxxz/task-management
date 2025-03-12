package com.taskManagement.utils;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileEncryptionUtil {

    /**
     * 加密文件内容
     *
     * @param file 原始文件
     * @param key  加密密钥
     * @return 加密后的文件输入流
     */
    public static InputStream encryptFile(MultipartFile file, String key) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // 构建AES密钥
        SecretKeySpec secretKey = generateKey(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        try (InputStream inputStream = file.getInputStream();
             CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
    
    /**
     * 生成固定长度的密钥
     */
    private static SecretKeySpec generateKey(String key) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        keyBytes = sha.digest(keyBytes);
        byte[] truncatedBytes = new byte[16]; // AES需要16字节密钥
        System.arraycopy(keyBytes, 0, truncatedBytes, 0, truncatedBytes.length);
        return new SecretKeySpec(truncatedBytes, "AES");
    }
    
    /**
     * 生成加密后的文件名
     */
    public static String generateEncryptedFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomStr = Base64.encodeBase64URLSafeString((originalFilename + timestamp).getBytes())
                .substring(0, 8);
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return "encrypted_" + randomStr + extension;
    }
} 