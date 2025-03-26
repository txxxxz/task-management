package com.taskManagement.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 项目附件DTO
 */
@Data
public class ProjectAttachmentDTO {
    private Long id;
    private Long projectId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String md5;
    private Long createUser;
    private LocalDateTime createTime;
    private Long updateUser;
    private LocalDateTime updateTime;
}
