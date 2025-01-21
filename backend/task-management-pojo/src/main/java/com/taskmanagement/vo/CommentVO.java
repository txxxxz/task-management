package com.taskmanagement.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论视图对象
 */
@Data
public class CommentVO {
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论类型：0-普通评论，1-状态变更，2-任务更新
     */
    private Integer type;
    
    /**
     * 是否包含附件
     */
    private Boolean hasAttachment;
    
    /**
     * 附件列表
     */
    private List<AttachmentVO> attachments;
    
    /**
     * 父评论信息（如果是回复）
     */
    private CommentVO parent;
    
    /**
     * 回复列表
     */
    private List<CommentVO> replies;
    
    /**
     * 评论者信息
     */
    private UserVO creator;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 