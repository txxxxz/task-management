package com.taskmanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目数据传输对象
 */
@Data
public class ProjectDTO {
    /**
     * 项目名称
     */
    private String name;
    
    /**
     * 项目描述
     */
    private String description;
    
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