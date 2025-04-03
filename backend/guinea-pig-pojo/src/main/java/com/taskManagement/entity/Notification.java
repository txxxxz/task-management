package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_notification")
public class Notification extends BaseEntity {
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
     * 忽略BaseEntity中不需要的字段
     */
    @TableField(exist = false)
    private Long createUser;
    
    @TableField(exist = false)
    private Long updateUser;
} 