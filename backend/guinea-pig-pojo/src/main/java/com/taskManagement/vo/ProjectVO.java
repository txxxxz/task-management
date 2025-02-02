package com.taskManagement.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectVO {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer priority;
    
    private UserVO creator;
    private List<TaskVO> tasks;
    private List<TagVO> tags;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 