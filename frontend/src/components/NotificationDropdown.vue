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
import { getCommentById } from '@/api/comment'
import axios from 'axios'

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
  try {
    // 查找当前通知项（可能包含任务信息）
    const notification = notifications.value.find(item => 
      item.type === 'comment_mention' && item.relatedId === commentId
    );
    
    // 从通知内容解析任务名称
    if (notification) {
      // 方法1: 提取方括号中的任务名，然后从taskUpdate通知中查找匹配项
      const taskNameMatch = notification.content.match(/\[(.*?)\]/);
      if (taskNameMatch && taskNameMatch[1]) {
        const taskName = taskNameMatch[1];
        const taskUpdateNotification = notifications.value.find(item => 
          item.type === 'task_update' && item.content.includes(`[${taskName}]`)
        );
        
        if (taskUpdateNotification) {
          console.log(`找到任务更新通知，任务ID: ${taskUpdateNotification.relatedId}`);
          // 使用URL片段(fragment)直接定位到评论区域
          router.push(`/detail/${taskUpdateNotification.relatedId}?commentId=${commentId}#comment-${commentId}`);
          drawerVisible.value = false;
          return;
        }
      }
      
      // 方法2: 尝试从通知内容中直接提取数字，可能是任务ID
      const idMatch = notification.content.match(/task\s+#?(\d+)|任务\s*(\d+)|\[.*?(\d+).*?\]/);
      if (idMatch) {
        const taskId = idMatch[1] || idMatch[2] || idMatch[3];
        if (taskId) {
          console.log(`从通知内容中提取到可能的任务ID: ${taskId}`);
          router.push(`/detail/${taskId}?commentId=${commentId}#comment-${commentId}`);
          drawerVisible.value = false;
          return;
        }
      }
      
      // 方法3: 看现有的其他通知中是否有包含相同关键词的task_update类型
      // 从当前通知内容中提取关键词（除了用户名和通用词汇外的部分）
      const content = notification.content;
      const keywords = content.split(/\s+/).filter(word => {
        // 过滤掉常见的词汇，只保留可能与任务相关的关键词
        return word.length > 2 && 
               !['commented', 'mention', 'mentioned', 'you', 'task', 'in', 'on', 'the', 'your'].includes(word.toLowerCase()) &&
               !/^[@\[\]()]/.test(word); // 过滤掉以特殊字符开头的词
      });
      
      if (keywords.length > 0) {
        // 查找包含这些关键词的任务更新通知
        const matchingTaskNotification = notifications.value.find(item => {
          if (item.type !== 'task_update') return false;
          return keywords.some(keyword => item.content.toLowerCase().includes(keyword.toLowerCase()));
        });
        
        if (matchingTaskNotification) {
          console.log(`通过关键词匹配找到任务，ID: ${matchingTaskNotification.relatedId}`);
          router.push(`/detail/${matchingTaskNotification.relatedId}?commentId=${commentId}#comment-${commentId}`);
          drawerVisible.value = false;
          return;
        }
      }
    }
    
    // 如果都失败了，跳转到任务列表
    console.log('Cannot determine the task belongs to which task, redirecting to task list');
    ElMessage.info('Cannot determine the task belongs to which task, redirecting to task list');
    router.push('/list');
    drawerVisible.value = false;
  } catch (error) {
    console.error('处理评论跳转出错:', error);
    ElMessage.error('Processing comment jump error, redirecting to task list');
    router.push('/list');
    drawerVisible.value = false;
  }
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
  
  // 替换 [null] 或 [undefined] 为 [Unnamed Task]
  content = content.replace(/\[(null|undefined)\]/g, '[Unnamed Task]');
  
  // 尝试提取方括号内的任何内容
  const bracketPattern = /\[(.*?)\]/g;
  return content.replace(bracketPattern, (match, contentInBracket) => {
    if (!contentInBracket || contentInBracket === 'null' || contentInBracket === 'undefined') {
      return '[Unnamed Task]';
    }
    return match; // 保持原样
  });
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