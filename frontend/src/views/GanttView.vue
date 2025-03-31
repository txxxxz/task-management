<template>
  <div class="gantt-container">
    <h1>任务甘特图</h1>
    <div class="gantt-placeholder">
      <el-empty description="甘特图功能正在开发中，敬请期待！">
        <template #image>
          <el-icon :size="60"><Calendar /></el-icon>
        </template>
        <el-button type="primary" @click="goToTaskList">返回任务列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Calendar } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const goToTaskList = () => {
  router.push('/list')
}
</script>

<style scoped>
.gantt-container {
  padding: 20px;
}

.gantt-placeholder {
  margin-top: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  border: 1px dashed #ccc;
  border-radius: 8px;
}
</style> 