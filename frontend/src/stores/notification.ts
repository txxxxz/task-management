import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Notification } from '@/types/notification'
import * as notificationApi from '@/api/notification'
import { useUserStore } from './user'

export const useNotificationStore = defineStore('notification', () => {
  // 通知列表
  const notifications = ref<Notification[]>([])
  // 通知总数
  const total = ref(0)
  // 未读通知数量
  const unreadCount = ref(0)
  // 是否加载中
  const loading = ref(false)

  // 获取通知列表
  const getNotifications = async (page = 1, pageSize = 10) => {
    loading.value = true
    try {
      const userStore = useUserStore()
      if (!userStore.userInfo?.id) return
      
      const res = await notificationApi.getUserNotifications(
        userStore.userInfo.id, 
        page, 
        pageSize
      )
      notifications.value = res.data.records
      total.value = res.data.total
      
      // 更新未读通知数量
      getUnreadCount()
    } catch (error) {
      console.error('Failed to fetch notifications:', error)
    } finally {
      loading.value = false
    }
  }

  // 获取未读通知数量
  const getUnreadCount = async () => {
    try {
      const userStore = useUserStore()
      if (!userStore.userInfo?.id) return
      
      const res = await notificationApi.getUnreadCount(userStore.userInfo.id)
      unreadCount.value = res.data
    } catch (error) {
      console.error('Failed to fetch unread count:', error)
    }
  }

  // 标记通知为已读
  const markAsRead = async (notificationId: number) => {
    try {
      const userStore = useUserStore()
      if (!userStore.userInfo?.id) return
      
      await notificationApi.markAsRead(notificationId, userStore.userInfo.id)
      
      // 更新本地状态
      const index = notifications.value.findIndex(n => n.id === notificationId)
      if (index !== -1) {
        notifications.value[index].isRead = 1
      }
      
      // 更新未读数量
      getUnreadCount()
    } catch (error) {
      console.error('Failed to mark notification as read:', error)
    }
  }

  // 标记所有通知为已读
  const markAllAsRead = async () => {
    try {
      const userStore = useUserStore()
      if (!userStore.userInfo?.id) return
      
      await notificationApi.markAllAsRead(userStore.userInfo.id)
      
      // 更新本地状态
      notifications.value = notifications.value.map(n => ({ ...n, isRead: 1 }))
      unreadCount.value = 0
    } catch (error) {
      console.error('Failed to mark all notifications as read:', error)
    }
  }

  return {
    notifications,
    total,
    unreadCount,
    loading,
    getNotifications,
    getUnreadCount,
    markAsRead,
    markAllAsRead
  }
}) 