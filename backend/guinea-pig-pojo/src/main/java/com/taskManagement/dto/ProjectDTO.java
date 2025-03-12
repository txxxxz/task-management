package com.taskManagement.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectDTO {
    private Long id;
    
    @NotBlank(message = "项目名称不能为空")
    private String name;
    
    private String description;
    
    @NotNull(message = "项目状态不能为空")
    private Integer status;
    
    private LocalDate startTime;
    private LocalDate endTime;
    
    @NotNull(message = "项目优先级不能为空")
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