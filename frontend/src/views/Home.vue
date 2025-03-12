<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>My Projects</span>
              <el-button type="primary" link>View All</el-button>
            </div>
          </template>
          <div class="stat-value">{{ stats.projectCount }}</div>
          <div class="stat-label">Projects in Progress</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>My Tasks</span>
              <el-button type="primary" link>View All</el-button>
            </div>
          </template>
          <div class="stat-value">{{ stats.taskCount }}</div>
          <div class="stat-label">Tasks Pending</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>Completed</span>
              <el-button type="primary" link>View All</el-button>
            </div>
          </template>
          <div class="stat-value">{{ stats.completedCount }}</div>
          <div class="stat-label">Completed Tasks</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="recent-tasks" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>Recent Tasks</span>
        </div>
      </template>
      <el-table :data="recentTasks" style="width: 100%">
        <el-table-column prop="name" label="Task Name" />
        <el-table-column prop="project" label="Project" />
        <el-table-column prop="deadline" label="Deadline" />
        <el-table-column prop="status" label="Status">
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
    name: 'Complete Homepage Design',
    project: 'Task Management System',
    deadline: '2024-03-20',
    status: 'IN_PROGRESS'
  },
  {
    name: 'User Management Module',
    project: 'Task Management System',
    deadline: '2024-03-25',
    status: 'PENDING'
  }
])

const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'primary' | 'danger' => {
  const statusMap: Record<string, 'success' | 'warning' | 'info' | 'primary' | 'danger'> = {
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'PENDING': 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'IN_PROGRESS': 'In Progress',
    'COMPLETED': 'Completed',
    'PENDING': 'Pending'
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