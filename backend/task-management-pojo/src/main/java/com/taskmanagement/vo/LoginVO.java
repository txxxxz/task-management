package com.taskmanagement.vo;

import lombok.Data;

/**
 * 登录响应视图对象
 */
@Data
public class LoginVO {
    /**
     * 用户token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserVO user;
} 