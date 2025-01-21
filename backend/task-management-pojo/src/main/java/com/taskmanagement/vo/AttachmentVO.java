package com.taskmanagement.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 附件视图对象
 */
@Data
public class AttachmentVO {
    /**
     * 附件ID
     */
    private Long id;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 上传者信息
     */
    private UserVO creator;
    
    /**
     * 上传时间
     */
    private LocalDateTime createTime;
} 