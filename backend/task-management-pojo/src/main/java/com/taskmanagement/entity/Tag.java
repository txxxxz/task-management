package com.taskmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签实体类
 * 标签用于标记任务，一个任务可以有多个标签，一个标签也可以被多个任务使用
 * 
 * @author taskmanagement
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_tag")
public class Tag extends BaseEntity {
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签颜色（十六进制颜色码）
     */
    private String color;
    
    /**
     * 所属项目ID
     */
    private Long projectId;
    
    /**
     * 标签描述
     */
    private String description;
} 