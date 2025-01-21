package com.taskmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 项目实体类
 * 项目是最高层级的组织单位，包含多个任务
 * 项目中的任务可以使用项目内定义的标签进行标记
 * 
 * @author taskmanagement
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_project")
public class Project extends BaseEntity {
    
    /**
     * 项目名称
     */
    private String name;
    
    /**
     * 项目描述
     */
    private String description;
    
    /**
     * 项目状态：0-筹备中，1-进行中，2-已完成，3-已归档
     */
    private Integer status;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 项目优先级：1-低，2-中，3-高，4-紧急
     */
    private Integer priority;
} 