package com.taskManagement.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class TagVO {
    private Long id;
    private String name;
    private String description;
    private String color;
    private List<Long> taskIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
} 