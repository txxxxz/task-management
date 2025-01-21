<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>我的项目</span>
              <el-button type="primary" link>查看全部</el-button>
            </div>
          </template>
          <div class="stat-value">{{ stats.projectCount }}</div>
          <div class="stat-label">个进行中的项目</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>我的任务</span>
              <el-button type="primary" link>查看全部</el-button>
            </div>
          </template>
          <div class="stat-value">{{ stats.taskCount }}</div>
          <div class="stat-label">个待处理的任务</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>已完成</span>
              <el-button type="primary" link>查看全部</el-button>
            </div>
          </template>
          <div class="stat-value">{{ stats.completedCount }}</div>
          <div class="stat-label">个已完成的任务</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="recent-tasks" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>最近的任务</span>
        </div>
      </template>
      <el-table :data="recentTasks" style="width: 100%">
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="project" label="所属项目" />
        <el-table-column prop="deadline" label="截止日期" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const stats = ref({
  projectCount: 5,
  taskCount: 12,
  completedCount: 8
})

const recentTasks = ref([
  {
    name: '完成首页设计',
    project: '任务管理系统',
    deadline: '2024-03-20',
    status: 'IN_PROGRESS'
  },
  {
    name: '用户管理模块',
    project: '任务管理系统',
    deadline: '2024-03-25',
    status: 'PENDING'
  }
])

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'PENDING': 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'PENDING': '待处理'
  }
  return statusMap[status] || status
}
</script>

<style scoped>
.home {
  padding: 20px;
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
  color: #409EFF;
  margin: 10px 0;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}
</style> 