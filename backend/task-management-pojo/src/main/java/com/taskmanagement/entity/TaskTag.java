package com.taskmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务-标签关联实体类
 * 
 * @author taskmanagement
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_tag")
public class TaskTag extends BaseEntity {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 标签ID
     */
    private Long tagId;
} 