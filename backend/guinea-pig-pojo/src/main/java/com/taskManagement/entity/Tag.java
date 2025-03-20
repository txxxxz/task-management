package com.taskManagement.entity;

import java.time.LocalDateTime;
import java.util.List;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_tag")
public class Tag extends BaseEntity {
    private String name;
    
    @TableField(exist = false)
    private List<Long> taskIds;
    
    private String description;
    
    private String color;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Long createUser;
    
    private Long updateUser;
} 