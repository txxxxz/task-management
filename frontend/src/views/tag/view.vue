<template>
  <div class="tag-view-container">
    <el-card class="tag-view-card">
      <template #header>
        <div class="card-header">
          <h2>Tag Details</h2>
          <div class="header-actions" v-if="userStore.userInfo?.role === 1">
            <el-button type="primary" @click="handleEdit">
              <el-icon><Edit /></el-icon> Edit
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="tag-details" v-loading="loading">
        <!-- 基本信息 -->
        <div class="details-group">
          <div class="group-title">Basic Information</div>
          
          <div class="detail-item">
            <div class="detail-label">Tag Name</div>
            <div class="detail-value">
              <div class="tag-preview" :style="{ backgroundColor: tagData.color || '#409EFF' }">
                <span :style="{ color: getContrastColor(tagData.color || '#409EFF') }">
                  {{ tagData.name || 'No Name' }}
                </span>
              </div>
            </div>
          </div>
          
          <div class="detail-item">
            <div class="detail-label">Tag Color</div>
            <div class="detail-value">
              <div class="color-preview" :style="{ backgroundColor: tagData.color || '#409EFF' }"></div>
              <span class="color-code">{{ tagData.color || 'Not set' }}</span>
            </div>
          </div>
          
          <div class="detail-item">
            <div class="detail-label">Description</div>
            <div class="detail-value description-text">
              {{ tagData.description || 'No description' }}
            </div>
          </div>
          
          <div class="detail-item">
            <div class="detail-label">Creation Time</div>
            <div class="detail-value">
              {{ formatDate(tagData.createTime) }}
            </div>
          </div>
          
          <div class="detail-item" v-if="tagData.updateTime">
            <div class="detail-label">Last Updated</div>
            <div class="detail-value">
              {{ formatDate(tagData.updateTime) }}
            </div>
          </div>
        </div>
        
        <!-- 按钮区域 -->
        <div class="action-buttons">
          <el-button @click="handleBack">Back</el-button>
          <el-button type="primary" @click="handleEdit" v-if="userStore.userInfo?.role === 1">Edit</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Back } from '@element-plus/icons-vue'
import { getTagById } from '@/api/tag'
import { useUserStore } from '@/stores/user'
import { formatDate as formatDateUtil } from '@/utils/format'
import type { Tag } from '@/types/tag'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

// 标签数据
const tagData = reactive<Partial<Tag>>({
  id: undefined,
  name: '',
  color: '',
  description: '',
  createTime: '',
  updateTime: ''
})

// 获取标签详情
const fetchTagDetail = async (tagId: string) => {
  loading.value = true
  try {
    const response = await getTagById(Number(tagId))
    if (response && response.data && response.data.code === 1) {
      const data = response.data.data
      if (data) {
        // 更新标签数据
        tagData.id = data.id
        tagData.name = data.name
        tagData.color = data.color
        tagData.description = data.description
        tagData.createTime = data.createTime
        tagData.updateTime = data.updateTime
      } else {
        ElMessage.error('Failed to get tag details')
        router.push('/tag/list')
      }
    } else {
      ElMessage.error('Failed to get tag details')
      router.push('/tag/list')
    }
  } catch (error) {
    console.error('Failed to get tag details:', error)
    ElMessage.error('Failed to get tag details')
    router.push('/tag/list')
  } finally {
    loading.value = false
  }
}

// 获取对比色（确保标签文本清晰可见）
const getContrastColor = (hexColor: string) => {
  // 处理空值
  if (!hexColor) return '#000000'
  
  try {
    // 将十六进制颜色转换为RGB
    const r = parseInt(hexColor.substring(1, 3), 16)
    const g = parseInt(hexColor.substring(3, 5), 16)
    const b = parseInt(hexColor.substring(5, 7), 16)
    
    // 计算亮度
    const brightness = (r * 299 + g * 587 + b * 114) / 1000
    
    // 根据亮度返回黑色或白色
    return brightness > 128 ? '#000000' : '#ffffff'
  } catch (error) {
    return '#000000' // 出错时返回黑色
  }
}

// 格式化日期
const formatDate = (date: string | undefined): string => {
  if (!date) return 'Not set'
  return formatDateUtil(date)
}

// 返回列表
const handleBack = () => {
  router.push('/tag/list')
}

// 编辑标签
const handleEdit = () => {
  if (userStore.userInfo?.role !== 1) {
    ElMessage.warning('Only project leaders can edit tags')
    return
  }
  router.push(`/tag/edit/${tagData.id}`)
}

onMounted(async () => {
  // 获取标签详情
  const tagId = route.params.id as string
  if (tagId) {
    await fetchTagDetail(tagId)
  } else {
    ElMessage.error('Invalid tag ID')
    router.push('/tag/list')
  }
})
</script>

<style scoped>
.tag-view-container {
  padding: 24px;
}

.tag-view-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.tag-details {
  margin-top: 20px;
}

.details-group {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
}

.group-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
  padding-left: 8px;
  border-left: 4px solid var(--el-color-primary);
}

.detail-item {
  display: flex;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.detail-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
  margin-bottom: 0;
}

.detail-label {
  width: 120px;
  font-weight: 500;
  color: var(--el-text-color-regular);
  flex-shrink: 0;
}

.detail-value {
  flex-grow: 1;
  display: flex;
  align-items: center;
}

.description-text {
  white-space: pre-line;
  line-height: 1.6;
}

.tag-preview {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: 500;
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  margin-right: 12px;
  border: 1px solid #dcdfe6;
}

.color-code {
  font-family: monospace;
  color: var(--el-text-color-regular);
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 30px;
}
</style> 