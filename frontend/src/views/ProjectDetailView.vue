<template>
  <div class="project-detail">
    <div class="page-header">
      <h1 class="page-title">项目详情</h1>
    </div>
    <el-card v-if="project">
      <template #header>
        <div class="card-header">
          <span>{{ project.name }}</span>
          <el-tag :type="getStatusType(project.status)" class="status-tag">
            {{ getStatusText(project.status) }}
          </el-tag>
        </div>
      </template>
      <div class="project-info">
        <p class="description">{{ project.description }}</p>
        <div class="meta">
          <el-tag :type="getPriorityType(project.priority)" class="priority-tag">
            {{ getPriorityText(project.priority) }}
          </el-tag>
          <span class="time">
            {{ project.startTime }} - {{ project.endTime }}
          </span>
        </div>
      </div>
    </el-card>
    <el-empty v-else description="项目不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { Project } from '@/types/models'
import { getStatusType, getStatusText, getPriorityType, getPriorityText } from '@/utils/status'

const route = useRoute()
const project = ref<Project>()

const loadProject = async () => {
  try {
    // TODO: 实现获取项目详情的 API 调用
    // project.value = await projectApi.getProjectDetail(route.params.id)
  } catch (error) {
    ElMessage.error('获取项目详情失败')
  }
}

onMounted(() => {
  loadProject()
})
</script>

<style scoped>
.project-detail {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-info {
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