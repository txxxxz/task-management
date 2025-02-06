package com.taskManagement.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * JWT配置属性
 */
@Component
@ConfigurationProperties(prefix = "guineapig.jwt")
@Data
@Validated
public class JwtProperties {

    /**
     * 管理端JWT配置
     */
    @NotBlank(message = "Admin secret key cannot be empty")
    private String adminSecretKey;
    
    @NotNull(message = "Admin token TTL must be specified")
    @Positive(message = "Admin token TTL must be positive")
    private long adminTtl;
    
    @NotBlank(message = "Admin token name cannot be empty")
    private String adminTokenName;

    /**
     * 用户端JWT配置
     */
    @NotBlank(message = "User secret key cannot be empty")
    private String userSecretKey;
    
    @NotNull(message = "User token TTL must be specified")
    @Positive(message = "User token TTL must be positive")
    private long userTtl;
    
    @NotBlank(message = "User token name cannot be empty")
    private String userTokenName;

    /**
     * Token头部信息
     */
    @NotBlank(message = "Token header cannot be empty")
    private String tokenHeader = "token";
}
