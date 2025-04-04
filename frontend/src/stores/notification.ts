import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Notification } from '@/types/notification'
import * as notificationApi from '@/api/notification'
import { useUserStore } from './user'

export const useNotificationStore = defineStore('notification', () => {
  // Notification list
  const notifications = ref<Notification[]>([])
  // Total number of notifications
  const total = ref(0)
  // Unread notification count
  const unreadCount = ref(0)
  // Loading state
  const loading = ref(false)

  // Get notification list
  const getNotifications = async (page = 1, pageSize = 10) => {
    loading.value = true
    try {
      const userStore = useUserStore()
      if (!userStore.userInfo?.id) return
      
      const res = await notificationApi.getUserNotifications(
        Number(userStore.userInfo.id), 
        page, 
        pageSize
      )
      notifications.value = res.data.records
      total.value = res.data.total
      
      // Update unread notification count
      getUnreadCount()
    } catch (error) {
      console.error('Failed to fetch notifications:', error)
    } finally {
      loading.value = false
    }
  }

  // Get unread notification count
  const getUnreadCount = async () => {
    try {
      const userStore = useUserStore()
      if (!userStore.userInfo?.id) return
      
      const res = await notificationApi.getUnreadCount(Number(userStore.userInfo.id))
      unreadCount.value = res.data
    } catch (error) {
      console.error('Failed to fetch unread count:', error)
    }
  }

  // Mark notification as read
  const markAsRead = async (notificationId: number) => {
    try {
      const userStore = useUserStore()
      if (!userStore.userInfo?.id) return
      
      await notificationApi.markAsRead(notificationId, Number(userStore.userInfo.id))
      
      // Update local state
      const index = notifications.value.findIndex(n => n.id === notificationId)
      if (index !== -1) {
        notifications.value[index].isRead = 1
      }
      
      // Update unread count
      getUnreadCount()
    } catch (error) {
      console.error('Failed to mark notification as read:', error)
    }
  }

  // Mark all notifications as read
  const markAllAsRead = async () => {
    try {
      const userStore = useUserStore()
      if (!userStore.userInfo?.id) return
      
      await notificationApi.markAllAsRead(Number(userStore.userInfo.id))
      
      // Update local state
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