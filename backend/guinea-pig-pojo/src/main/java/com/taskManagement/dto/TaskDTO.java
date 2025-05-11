package com.taskManagement.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonAlias({"dueTime"})
    @JsonProperty("dueTime")
    private LocalDateTime deadline;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    private Integer commentCount;

    private Long createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Long updateUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


    
    private List<Long> memberIds;  // 任务成员ID列表
    private List<Long> tagIds;     // 标签ID列表
    private List<String> members; // 添加任务成员列表字段
    
    private List<String> attachments; // 任务附件URL列表


} 