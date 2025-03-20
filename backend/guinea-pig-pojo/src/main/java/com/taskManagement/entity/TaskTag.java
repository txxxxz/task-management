package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_tag_rel")
public class TaskTag extends BaseEntity {
    private Long taskId;
    private Long tagId;
} 