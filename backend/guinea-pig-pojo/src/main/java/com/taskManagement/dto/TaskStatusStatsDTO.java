package com.taskManagement.dto;

import lombok.Data;

/**
 * 任务状态统计DTO
 * 用于向前端返回每日任务状态统计数据
 */
@Data
public class TaskStatusStatsDTO {
    /**
     * 星期几
     */
    private String day;
    
    /**
     * 待处理任务数量
     */
    private Integer pending;
    
    /**
     * 进行中任务数量
     */
    private Integer inProgress;
    
    /**
     * 已完成任务数量
     */
    private Integer completed;
} 