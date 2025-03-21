package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_project")
public class Project extends BaseEntity {
    private String name;
    private String description;
    private Integer status;      // 0-筹备中，1-进行中，2-已完成，3-已归档
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer priority;    // 1-低，2-中，3-高，4-紧急
} 