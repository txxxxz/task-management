package com.taskManagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskManagement.dto.NotificationDTO;
import com.taskManagement.entity.Notification;
import com.taskManagement.entity.User;
import com.taskManagement.mapper.NotificationMapper;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper, UserMapper userMapper) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
    }

    /**
     * 创建任务更新通知
     */
    @Override
    @Transactional
    public NotificationDTO createTaskUpdateNotification(Long taskId, String content, Long userId) {
        Notification notification = new Notification();
        notification.setType("task_update");
        notification.setContent(content);
        notification.setUserId(userId);
        notification.setIsRead(0);
        notification.setRelatedId(taskId);
        
        notificationMapper.insert(notification);
        
        NotificationDTO notificationDTO = new NotificationDTO();
        BeanUtils.copyProperties(notification, notificationDTO);
        return notificationDTO;
    }

    /**
     * 创建评论@通知
     */
    @Override
    @Transactional
    public NotificationDTO createCommentMentionNotification(Long commentId, String content, Long userId) {
        Notification notification = new Notification();
        notification.setType("comment_mention");
        notification.setContent(content);
        notification.setUserId(userId);
        notification.setIsRead(0);
        notification.setRelatedId(commentId);
        
        notificationMapper.insert(notification);
        
        NotificationDTO notificationDTO = new NotificationDTO();
        BeanUtils.copyProperties(notification, notificationDTO);
        return notificationDTO;
    }

    /**
     * 获取用户通知列表
     */
    @Override
    public Map<String, Object> getUserNotifications(Long userId, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        Page<Notification> paging = new Page<>(page, pageSize);
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        
        Page<Notification> notificationPage = notificationMapper.selectPage(paging, queryWrapper);
        
        List<NotificationDTO> notificationDTOList = notificationPage.getRecords().stream()
                .map(notification -> {
                    NotificationDTO dto = new NotificationDTO();
                    BeanUtils.copyProperties(notification, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        
        result.put("total", notificationPage.getTotal());
        result.put("records", notificationDTOList);
        return result;
    }

    /**
     * 标记通知为已读
     */
    @Override
    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, 1);
        
        notificationMapper.update(null, updateWrapper);
        
        Notification notification = notificationMapper.selectById(notificationId);
        NotificationDTO notificationDTO = new NotificationDTO();
        BeanUtils.copyProperties(notification, notificationDTO);
        return notificationDTO;
    }

    /**
     * 标记用户所有通知为已读
     */
    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        
        return notificationMapper.update(null, updateWrapper);
    }

    /**
     * 获取用户未读通知数量
     */
    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        
        return Math.toIntExact(notificationMapper.selectCount(queryWrapper));
    }

    /**
     * 从评论内容中提取被@的用户名列表
     */
    @Override
    public List<String> extractMentionedUsers(String content) {
        List<String> mentionedUsers = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return mentionedUsers;
        }
        
        // 匹配@username格式
        Pattern pattern = Pattern.compile("@([\\w-]+)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            mentionedUsers.add(matcher.group(1));
        }
        
        return mentionedUsers;
    }
}