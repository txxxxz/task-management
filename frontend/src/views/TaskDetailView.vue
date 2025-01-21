<template>
  <div class="task-detail">
    <div class="page-header">
      <h1 class="page-title">任务详情</h1>
    </div>
    <el-card v-if="task">
      <template #header>
        <div class="card-header">
          <span>{{ task.name }}</span>
          <el-tag :type="getStatusType(task.status)" class="status-tag">
            {{ getStatusText(task.status) }}
          </el-tag>
        </div>
      </template>
      <div class="task-info">
        <p class="description">{{ task.description }}</p>
        <div class="meta">
          <el-tag :type="getPriorityType(task.priority)" class="priority-tag">
            {{ getPriorityText(task.priority) }}
          </el-tag>
          <span class="time">
            {{ task.startTime }} - {{ task.endTime }}
          </span>
        </div>
      </div>
    </el-card>
    <el-empty v-else description="任务不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { Task } from '@/types/models'
import { getStatusType, getStatusText, getPriorityType, getPriorityText } from '@/utils/status'

const route = useRoute()
const task = ref<Task>()

const loadTask = async () => {
  try {
    // TODO: 实现获取任务详情的 API 调用
    // task.value = await taskApi.getTaskDetail(route.params.taskId)
  } catch (error) {
    ElMessage.error('获取任务详情失败')
  }
}

onMounted(() => {
  loadTask()
})
</script>

<style scoped>
.task-detail {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-info {
  .description {
    margin-bottom: 16px;
    color: #606266;
    white-space: pre-wrap;
  }

  .meta {
    display: flex;
    align-items: center;
    gap: 16px;
    color: #909399;
  }

  .time {
    font-size: 14px;
  }
}
</style> 