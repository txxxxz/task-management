<template>
  <div class="not-found">
    <el-result
      icon="warning"
      title="404"
      sub-title="Sorry, the page you are looking for does not exist."
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/')">Back to Home</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--el-bg-color);
}
</style> 