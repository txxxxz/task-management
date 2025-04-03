<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
      <el-button type="text" @click="showNotifications">
        <el-icon><Bell /></el-icon>
      </el-button>
    </el-badge>
    
    <el-dialog
      v-model="dialogVisible"
      title="通知"
      width="500px"
      :before-close="handleClose"
    >
      <div class="notification-list">
        <div v-if="notifications.length === 0" class="empty-notification">
          暂无通知
        </div>
        <div v-else class="notification-items">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ 'unread': !notification.isRead }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-content">{{ notification.content }}</div>
            <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="markAllAsRead" v-if="notifications.length > 0">全部标记为已读</el-button>
          <el-button type="primary" @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Bell } from '@element-plus/icons-vue';
import type { Notification } from '../types/notification';
import { getUserNotifications, markAsRead, markAllAsRead as markAllNotificationsAsRead, getUnreadCount } from '../api/notification';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';

const dialogVisible = ref(false);
const notifications = ref<Notification[]>([]);
const unreadCount = ref(0);
let ws: WebSocket | null = null;

const initWebSocket = () => {
  const token = localStorage.getItem('token');
  if (!token) return;

  ws = new WebSocket(`ws://localhost:8080/ws/notifications?token=${token}`);
  
  ws.onopen = () => {
    console.log('WebSocket connected');
  };
  
  ws.onmessage = (event) => {
    const notification = JSON.parse(event.data);
    notifications.value.unshift(notification);
    unreadCount.value++;
  };
  
  ws.onclose = () => {
    console.log('WebSocket disconnected');
    // 尝试重新连接
    setTimeout(initWebSocket, 5000);
  };
};

const showNotifications = async () => {
  dialogVisible.value = true;
  try {
    const response = await getNotifications();
    notifications.value = response.data;
    unreadCount.value = notifications.value.filter(n => !n.isRead).length;
  } catch (error) {
    ElMessage.error('获取通知失败');
  }
};

const handleNotificationClick = async (notification: Notification) => {
  if (!notification.isRead) {
    try {
      await markAsRead(notification.id);
      notification.isRead = true;
      unreadCount.value--;
    } catch (error) {
      ElMessage.error('标记已读失败');
    }
  }
  // TODO: 根据通知类型跳转到相应页面
};

const markAllAsRead = async () => {
  try {
    await markAllNotificationsAsRead();
    notifications.value.forEach(n => n.isRead = true);
    unreadCount.value = 0;
  } catch (error) {
    ElMessage.error('标记全部已读失败');
  }
};

const handleClose = () => {
  dialogVisible.value = false;
};

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

onMounted(() => {
  initWebSocket();
  // 初始获取未读数量
  getUnreadCount().then(response => {
    unreadCount.value = response.data;
  });
});

onUnmounted(() => {
  if (ws) {
    ws.close();
  }
});
</script>

<style scoped>
.notification-bell {
  margin-right: 20px;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.notification-item.unread {
  background-color: #f0f9ff;
}

.notification-content {
  margin-bottom: 5px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.empty-notification {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style> 