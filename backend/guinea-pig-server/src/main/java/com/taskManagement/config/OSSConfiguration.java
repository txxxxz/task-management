package com.taskManagement.config;

import com.taskManagement.utils.AliOssUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建AliOSSUtil对象
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "guineapig.alioss")
public class OSSConfiguration {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    @Bean
    public AliOssUtil aliOssUtil() {
        return new AliOssUtil(endpoint, accessKeyId, accessKeySecret, bucketName);
    }
}
