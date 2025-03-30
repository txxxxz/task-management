package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task")
public class Task extends BaseEntity {
    private String name;
    private String description;
    private Long projectId;
    private Integer status;          // 0-待处理，1-进行中，2-已完成，3-已取消
    private Integer priority;        // 1-低，2-中，3-高，4-紧急
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private LocalDateTime completedTime;
    private Integer commentCount;    // 评论数量仍需保留，其他功能可能依赖此字段
} 