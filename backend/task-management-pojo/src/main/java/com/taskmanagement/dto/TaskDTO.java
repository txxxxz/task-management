package com.taskmanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务数据传输对象
 */
@Data
public class TaskDTO {
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
     * 预计工时（小时）
     */
    private Double estimatedHours;
    
    /**
     * 标签ID列表
     */
    private List<Long> tagIds;
    
    /**
     * 参与者ID列表
     */
    private List<Long> participantIds;
    
    /**
     * 负责人ID
     */
    private Long assigneeId;
} 