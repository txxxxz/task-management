package com.taskManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDTO {
    private Long id;
    
    @NotBlank(message = "Project name cannot be empty")
    private String name;
    
    private String description;
    
    @NotNull(message = "Project status cannot be empty")
    private Integer status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @NotNull(message = "Project priority cannot be empty")
    private Integer priority;
    
    /**
     * 项目成员用户名列表
     */
    private List<String> members;
    
    /**
     * 项目附件列表
     */
    private List<String> attachments;
} 