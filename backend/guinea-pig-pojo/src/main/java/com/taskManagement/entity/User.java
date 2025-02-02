package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;  // 0-禁用，1-正常
    private Integer role;    // 0-普通成员，1-项目负责人，2-管理员
} 