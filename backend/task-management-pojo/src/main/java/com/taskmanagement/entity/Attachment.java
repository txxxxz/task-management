package com.taskmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 附件实体类
 * 附件只能关联到任务
 * 
 * @author taskmanagement
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_attachment")
public class Attachment extends BaseEntity {
    
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
     * 所属任务ID
     */
    private Long taskId;
    
    /**
     * 文件MD5值
     */
    private String md5;
} 