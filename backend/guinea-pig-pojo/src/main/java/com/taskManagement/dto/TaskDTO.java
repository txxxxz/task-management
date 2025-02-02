package com.taskManagement.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    
    @NotBlank(message = "任务标题不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "所属项目不能为空")
    private Long projectId;
    
    @NotNull(message = "任务状态不能为空")
    private Integer status;
    
    @NotNull(message = "任务优先级不能为空")
    private Integer priority;
    
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private Double estimatedHours;
    
    private List<Long> memberIds;  // 任务成员ID列表
    private List<Long> tagIds;     // 标签ID列表
} 