package com.taskmanagement.vo;

import lombok.Data;

/**
 * 标签视图对象
 */
@Data
public class TagVO {
    /**
     * 标签ID
     */
    private Long id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签颜色（十六进制颜色码）
     */
    private String color;
    
    /**
     * 标签描述
     */
    private String description;
    
    /**
     * 使用该标签的任务数量
     */
    private Integer taskCount;
} 