package com.taskmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体类
 * 评论只能针对任务进行评论，支持状态变更等系统评论
 * 支持评论的回复功能和附件上传
 * 
 * @author taskmanagement
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_comment")
public class Comment extends BaseEntity {
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 父评论ID（用于回复）
     */
    private Long parentId;
    
    /**
     * 评论类型：0-普通评论，1-状态变更，2-任务更新
     */
    private Integer type;
    
    /**
     * 是否包含附件：0-否，1-是
     */
    private Integer hasAttachment;
} 