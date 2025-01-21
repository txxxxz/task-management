<template>
  <div class="home-container">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>我的项目</span>
              <el-button text @click="$router.push('/projects')">查看全部</el-button>
            </div>
          </template>
          <div class="stat-value">{{ projectStore.projects.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>待办任务</span>
            </div>
          </template>
          <div class="stat-value">{{ pendingTasks }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>已完成任务</span>
            </div>
          </template>
          <div class="stat-value">{{ completedTasks }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="recent-tasks">
      <template #header>
        <div class="card-header">
          <span>最近任务</span>
        </div>
      </template>
      <el-table :data="recentTasks" style="width: 100%">
        <el-table-column prop="title" label="任务名称" min-width="200">
          <template #default="{ row }">
            <el-link
              type="primary"
              :underline="false"
              @click="$router.push(`/projects/${row.projectId}/tasks/${row.id}`)"
            >
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="project.name" label="所属项目" width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="截止日期" width="180">
          <template #default="{ row }">
            <span :class="{ 'text-danger': isOverdue(row.endTime) }">
              {{ formatDate(row.endTime) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useProjectStore } from '@/store/project'
import { useTaskStore } from '@/store/task'
import type { Task } from '@/types/models'

const projectStore = useProjectStore()
const taskStore = useTaskStore()

onMounted(async () => {
  await projectStore.fetchProjects()
  for (const project of projectStore.projects) {
    await taskStore.fetchTasks(project.id)
  }
})

const allTasks = computed(() => {
  return taskStore.tasks
})

const pendingTasks = computed(() => {
  return allTasks.value.filter(task => task.status === 0).length
})

const completedTasks = computed(() => {
  return allTasks.value.filter(task => task.status === 2).length
})

const recentTasks = computed(() => {
  return [...allTasks.value]
    .sort((a, b) => new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime())
    .slice(0, 10)
})

const getStatusType = (status: number): 'success' | 'warning' | 'info' | 'primary' | 'danger' | '' => {
  const types = {
    0: 'primary',
    1: 'warning',
    2: 'success'
  } as const
  return types[status as keyof typeof types] || ''
}

const getStatusText = (status: number) => {
  const texts = {
    0: '待处理',
    1: '进行中',
    2: '已完成'
  }
  return texts[status as keyof typeof texts]
}

const getPriorityType = (priority: number): 'success' | 'warning' | 'info' | 'primary' | 'danger' | '' => {
  const types = {
    0: 'info',
    1: 'primary',
    2: 'warning',
    3: 'danger'
  } as const
  return types[priority as keyof typeof types] || ''
}

const getPriorityText = (priority: number) => {
  const texts = {
    0: '低',
    1: '中',
    2: '高',
    3: '紧急'
  }
  return texts[priority as keyof typeof texts]
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const isOverdue = (date: string) => {
  return new Date(date).getTime() < Date.now()
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  height: 100%;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  text-align: center;
  padding: 24px 0;
}

.text-danger {
  color: #f56c6c;
}
</style> 