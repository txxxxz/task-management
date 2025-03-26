package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务附件实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_attachment")
public class TaskAttachment extends BaseEntity {
    private Long taskId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    
} 