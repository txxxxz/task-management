package com.taskManagement.dto;

import java.time.LocalDateTime;

import lombok.Data;
/**
 * 任务附件DTO
 */
@Data
public class TaskAttachmentDTO {
    private Long id;
    private Long taskId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private Long createUser;
    private LocalDateTime createTime;
}
