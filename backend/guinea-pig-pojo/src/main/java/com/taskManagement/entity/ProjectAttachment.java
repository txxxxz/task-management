package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_project_attachment")
public class ProjectAttachment extends BaseEntity {
    private Long projectId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String md5;
} 