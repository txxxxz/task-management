package com.taskManagement.controller;

import com.taskManagement.dto.NotificationDTO;
import com.taskManagement.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 获取当前用户的通知列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 通知列表和总数
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = notificationService.getUserNotifications(userId, page, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 标记通知为已读
     * @param id 通知ID
     * @param userId 用户ID
     * @return 更新后的通知
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Long id,
            @RequestParam Long userId) {
        NotificationDTO notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(notification);
    }

    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     * @return 更新的通知数量
     */
    @PutMapping("/read-all")
    public ResponseEntity<Integer> markAllAsRead(@RequestParam Long userId) {
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 获取未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@RequestParam Long userId) {
        int count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
}