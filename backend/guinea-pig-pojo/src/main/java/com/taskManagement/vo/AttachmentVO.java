package com.taskManagement.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentVO {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private Long taskId;
    
    private UserVO uploader;
    private LocalDateTime createTime;
} 