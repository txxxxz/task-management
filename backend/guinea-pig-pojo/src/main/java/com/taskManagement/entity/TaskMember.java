package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务成员关系实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_task_member")
public class TaskMember {
    private Long id;
    private Long taskId;
    private Long userId;
    private LocalDateTime createTime;
} 