package com.taskManagement.vo;

import lombok.Data;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 电子邮件
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
    
    /**
     * 角色（0-普通成员，1-项目负责人，2-管理员）
     */
    private Integer role;
    
    /**
     * 创建时间
     */
    private String createTime;
} 