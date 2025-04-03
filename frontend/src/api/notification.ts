import request from '@/utils/request'
import type { Notification } from '@/types/notification'

/**
 * 获取用户通知列表
 * @param userId 用户ID
 * @param page 页码
 * @param pageSize 每页大小
 * @returns 通知列表和总数
 */
export function getUserNotifications(userId: number, page: number = 1, pageSize: number = 10) {
  return request({
    url: '/api/notifications',
    method: 'get',
    params: {
      userId,
      page,
      pageSize
    }
  })
}

/**
 * 标记通知为已读
 * @param notificationId 通知ID
 * @param userId 用户ID
 * @returns 更新后的通知
 */
export function markAsRead(notificationId: number, userId: number) {
  return request({
    url: `/api/notifications/${notificationId}/read`,
    method: 'put',
    params: {
      userId
    }
  })
}

/**
 * 标记所有通知为已读
 * @param userId 用户ID
 * @returns 更新的通知数量
 */
export function markAllAsRead(userId: number) {
  return request({
    url: '/api/notifications/read-all',
    method: 'put',
    params: {
      userId
    }
  })
}

/**
 * 获取未读通知数量
 * @param userId 用户ID
 * @returns 未读通知数量
 */
export function getUnreadCount(userId: number) {
  return request({
    url: '/api/notifications/unread-count',
    method: 'get',
    params: {
      userId
    }
  })
}