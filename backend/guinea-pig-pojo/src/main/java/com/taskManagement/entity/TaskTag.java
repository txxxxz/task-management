package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_task_tag_rel")
public class TaskTag {
    private Long id;
    private Long taskId;
    private Long tagId;
    private LocalDateTime createTime;
} 