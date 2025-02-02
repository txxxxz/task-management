package com.taskManagement.vo;

import lombok.Data;

@Data
public class TagVO {
    private Long id;
    private String name;
    private String color;
    private Long projectId;
    private String description;
} 