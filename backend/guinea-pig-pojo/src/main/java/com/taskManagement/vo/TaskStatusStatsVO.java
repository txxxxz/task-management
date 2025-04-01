package com.taskManagement.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务状态统计VO
 * 用于服务层内部封装统计结果
 */
@Data
public class TaskStatusStatsVO {
    /**
     * 统计日期
     */
    private LocalDateTime date;
    
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

    /**
     * 已取消任务数量
     */
    private Integer cancelled;
    
    /**
     * 总任务数量
     */
    private Integer total;
    
    /**
     * 完成率
     */
    private Double completionRate;
    
    
} 