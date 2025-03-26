package com.taskManagement.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    
    @NotBlank(message = "Task name cannot be empty")
    private String name;
    
    private String description;
    
    @NotNull(message = "Project ID cannot be empty")
    private Long projectId;
    
    @NotNull(message = "Task status cannot be empty")
    private Integer status;
    
    @NotNull(message = "Task priority cannot be empty")
    private Integer priority;
    
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private Double estimatedHours;
    
    private List<Long> memberIds;  // 任务成员ID列表
    private List<Long> tagIds;     // 标签ID列表
    private List<String> members; // 添加任务成员列表字段
} 