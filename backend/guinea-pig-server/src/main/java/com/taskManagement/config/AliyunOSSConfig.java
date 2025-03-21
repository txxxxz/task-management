package com.taskManagement.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunOSSConfig {

    @Value("${guineapig.alioss.endpoint}")
    private String endpoint;

    @Value("${guineapig.alioss.access-key-id}")
    private String accessKeyId;

    @Value("${guineapig.alioss.access-key-secret}")
    private String accessKeySecret;

    @Value("${guineapig.alioss.bucket-name}")
    private String bucketName;
    
    @Value("${guineapig.alioss.url-prefix}")
    private String urlPrefix;
    
    @Value("${guineapig.alioss.encryption-key}")
    private String encryptionKey;

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
} 