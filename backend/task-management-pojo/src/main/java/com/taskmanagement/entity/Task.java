package com.taskmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务实体类
 * 任务属于某个项目，可以关联多个标签
 * 
 * @author taskmanagement
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task")
public class Task extends BaseEntity {
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 所属项目ID
     */
    private Long projectId;
    
    /**
     * 任务状态：0-待处理，1-进行中，2-已完成，3-已取消
     */
    private Integer status;
    
    /**
     * 优先级：1-低，2-中，3-高，4-紧急
     */
    private Integer priority;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 实际完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * 负责人ID
     */
    private Long assigneeId;
    
    /**
     * 预计工时（小时）
     */
    private Double estimatedHours;
    
    /**
     * 实际工时（小时）
     */
    private Double actualHours;
} 