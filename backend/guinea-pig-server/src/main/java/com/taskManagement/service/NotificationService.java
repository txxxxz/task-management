package com.taskManagement.service;

import com.taskManagement.dto.NotificationDTO;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 */
public interface NotificationService {
    
    /**
     * 创建任务更新通知
     * @param taskId 任务ID
     * @param content 通知内容
     * @param userId 接收用户ID
     * @return 创建的通知
     */
    NotificationDTO createTaskUpdateNotification(Long taskId, String content, Long userId);
    
    /**
     * 创建评论@通知
     * @param commentId 评论ID
     * @param content 通知内容
     * @param userId 接收用户ID
     * @return 创建的通知
     */
    NotificationDTO createCommentMentionNotification(Long commentId, String content, Long userId);
    
    /**
     * 获取用户通知列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 通知列表和总数
     */
    Map<String, Object> getUserNotifications(Long userId, Integer page, Integer pageSize);
    
    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 更新后的通知
     */
    NotificationDTO markAsRead(Long notificationId, Long userId);
    
    /**
     * 标记用户所有通知为已读
     * @param userId 用户ID
     * @return 更新的通知数量
     */
    int markAllAsRead(Long userId);
    
    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 从评论内容中提取被@的用户名列表
     * @param content 评论内容
     * @return 用户名列表
     */
    List<String> extractMentionedUsers(String content);
}