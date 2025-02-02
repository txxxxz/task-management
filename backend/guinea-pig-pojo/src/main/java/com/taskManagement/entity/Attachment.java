package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_attachment")
public class Attachment extends BaseEntity {
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private Long taskId;
    private String md5;
} 