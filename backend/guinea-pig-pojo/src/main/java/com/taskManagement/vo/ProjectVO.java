package com.taskManagement.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    
    // 项目成员数量
    private Integer memberCount;
    
    // 项目成员列表
    private List<String> members;
    
    // 项目附件链接列表
    private List<String> attachments;
    
    // 当前用户是否为创建者
    private Boolean isCreator;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 