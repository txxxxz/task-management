package com.taskManagement.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunOSSConfig {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    
    @Value("${aliyun.oss.urlPrefix}")
    private String urlPrefix;
    
    @Value("${aliyun.oss.encryption-key}")
    private String encryptionKey;
    
    @Value("${aliyun.oss.project-folder}")
    private String projectFolder;

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    public String getBucketName() {
        return bucketName;
    }
    
    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    public String getEncryptionKey() {
        return encryptionKey;
    }
    
    public String getProjectFolder() {
        return projectFolder;
    }
} 