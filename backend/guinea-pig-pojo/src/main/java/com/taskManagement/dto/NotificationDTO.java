package com.taskManagement.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NotificationDTO {
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 通知类型：task_update-任务更新, comment_mention-评论@
     */
    private String type;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 接收通知的用户ID
     */
    private Long userId;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 关联ID（如任务ID或评论ID）
     */
    private Long relatedId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    
} 