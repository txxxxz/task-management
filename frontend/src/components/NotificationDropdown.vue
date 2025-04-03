<template>
  <div class="notification-dropdown">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <el-button 
        class="notification-icon" 
        :icon="Bell" 
        circle 
        @click="openNotificationDrawer" 
      />
    </el-badge>
    
    <el-drawer
      v-model="drawerVisible"
      title="Notification Center"
      size="350px"
      :destroy-on-close="false"
    >
      <div class="notification-actions">
        <span>{{ unreadCount }} Unread</span>
        <el-button type="primary" link @click="handleMarkAllAsRead">
          Mark All as Read
        </el-button>
      </div>
      
      <el-divider />
      
      <div v-if="loading" class="notification-loading">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="notifications.length === 0" class="notification-empty">
        <el-empty description="No Notifications" />
      </div>
      
      <el-scrollbar height="calc(100vh - 200px)">
        <div v-for="item in notifications" :key="item.id" class="notification-item" :class="{ 'is-read': item.isRead === 1 }">
          <div class="notification-item-header">
            <div class="notification-type">
              <el-tag size="small" :type="item.type === 'task_update' ? 'primary' : 'success'">
                {{ item.type === 'task_update' ? 'Task Update' : 'Comment Mention' }}
              </el-tag>
            </div>
            <div class="notification-time">
              {{ formatTime(item.createTime) }}
            </div>
          </div>
          
          <div class="notification-content">
            {{ formatNotificationContent(item.content) }}
          </div>
          
          <div class="notification-actions">
            <el-button 
              v-if="item.isRead === 0" 
              type="primary" 
              link 
              @click="handleMarkAsRead(item.id)"
            >
              Mark as Read
            </el-button>
            <el-button 
              v-if="item.type === 'task_update'" 
              type="primary" 
              link 
              @click="goToTask(item.relatedId)"
            >
              View Task
            </el-button>
            <el-button 
              v-else-if="item.type === 'comment_mention'" 
              type="primary" 
              link 
              @click="goToComment(item.relatedId)"
            >
              View Comment
            </el-button>
          </div>
        </div>
      </el-scrollbar>
      
      <el-pagination
        v-if="total > 10"
        :total="total"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        layout="prev, pager, next"
        background
        class="notification-pagination"
        @current-change="handlePageChange"
      />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const notificationStore = useNotificationStore()
const { notifications, total, unreadCount, loading } = storeToRefs(notificationStore)

const drawerVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 打开通知抽屉
const openNotificationDrawer = () => {
  drawerVisible.value = true
  fetchNotifications()
}

// 获取通知列表
const fetchNotifications = () => {
  notificationStore.getNotifications(currentPage.value, pageSize.value)
}

// 页码变化处理
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchNotifications()
}

// 标记为已读
const handleMarkAsRead = async (notificationId: number) => {
  try {
    await notificationStore.markAsRead(notificationId)
    ElMessage.success('Marked as read successfully')
  } catch (error) {
    console.error('Failed to mark as read:', error)
    ElMessage.error('Failed to mark as read, please try again')
  }
}

// 全部标记为已读
const handleMarkAllAsRead = async () => {
  try {
    await notificationStore.markAllAsRead()
    ElMessage.success('All notifications marked as read')
  } catch (error) {
    console.error('Failed to mark all as read:', error)
    ElMessage.error('Operation failed, please try again')
  }
}

// 跳转到任务详情
const goToTask = (taskId: number) => {
  router.push(`/detail/${taskId}`)
  drawerVisible.value = false
}

// 跳转到评论所在任务
const goToComment = (commentId: number) => {
  // 需要先根据评论ID获取对应的任务ID，这里简化处理
  router.push(`/task/commentId=${commentId}`)
  drawerVisible.value = false
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  
  if (date.isSame(now, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(now.subtract(1, 'day'), 'day')) {
    return 'Yesterday ' + date.format('HH:mm')
  } else if (date.isAfter(now.subtract(7, 'day'))) {
    return date.format('dddd HH:mm')
  } else {
    return date.format('YYYY-MM-DD HH:mm')
  }
}

// 每5分钟自动刷新未读通知数量
let timer: number
onMounted(() => {
  notificationStore.getUnreadCount()
  timer = window.setInterval(() => {
    notificationStore.getUnreadCount()
  }, 300000) // 5分钟
})

// 监听抽屉打开状态，关闭时重置页码
watch(drawerVisible, (val) => {
  if (!val) {
    currentPage.value = 1
  }
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

// 格式化通知内容，处理 [null] 情况
const formatNotificationContent = (content: string): string => {
  if (!content) return '';
  
  // 替换 [null] 为 [Unnamed Task]
  content = content.replace(/\[null\]/g, '[Unnamed Task]');
  
  // 检查是否是任务更新通知并提取任务ID
  if (content.includes('任务') && content.includes('状态更新为')) {
    // 将中文通知转为英文格式
    content = content.replace(/任务\s*\[(.*?)\]\s*状态更新为\s*(.*)/, 'Task [$1] status updated to $2');
    
    // 尝试提取方括号内的任何内容
    const bracketPattern = /\[(.*?)\]/g;
    return content.replace(bracketPattern, (match, taskNameInBracket) => {
      if (!taskNameInBracket || taskNameInBracket === 'null' || taskNameInBracket === 'undefined') {
        return '[Unnamed Task]';
      }
      return match; // 保持原样
    });
  }
  
  // 将其他中文通知内容替换为英文
  content = content.replace(/用户\s*\[(.*?)\]\s*在任务\s*\[(.*?)\]\s*中提到了你/, 'User [$1] mentioned you in task [$2]');
  
  return content;
}
</script>

<style scoped>
.notification-dropdown {
  display: inline-block;
}

.notification-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: var(--el-fill-color-lighter);
}

.notification-item.is-read {
  opacity: 0.7;
}

.notification-item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.notification-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.notification-content {
  margin-bottom: 8px;
  line-height: 1.5;
}

.notification-loading, .notification-empty {
  padding: 20px;
}

.notification-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style> 