<template>
  <div class="tag-form-container">
    <el-card class="tag-form-card">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑标签' : '创建标签' }}</h2>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="tagForm"
        :rules="formRules"
        label-width="100px"
        class="tag-form"
        v-loading="loading"
      >
        <!-- 基本信息 -->
        <div class="form-group">
          <div class="group-title">基本信息</div>
          
          <el-form-item label="标签名称" prop="name" required>
            <el-input 
              v-model="tagForm.name" 
              placeholder="请输入标签名称"
              maxlength="30"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="标签颜色" prop="color" required>
            <div class="color-picker-wrapper">
              <el-color-picker v-model="tagForm.color" show-alpha />
              <div class="color-preview" :style="{ backgroundColor: tagForm.color || '#409EFF' }">
                <span class="color-text" :style="{ color: getContrastColor(tagForm.color || '#409EFF') }">
                  {{ tagForm.name || '预览文本' }}
                </span>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="tagForm.description"
              type="textarea"
              :rows="3"
              placeholder="请描述标签用途（选填）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </div>
        
        <!-- 预设颜色 -->
        <div class="form-group">
          <div class="group-title">预设颜色</div>
          <div class="preset-colors">
            <div 
              v-for="(color, index) in presetColors" 
              :key="index"
              class="color-item"
              :class="{ 'active': tagForm.color === color }"
              :style="{ backgroundColor: color }"
              @click="tagForm.color = color"
            ></div>
          </div>
        </div>
        
        <!-- 按钮区域 -->
        <div class="form-actions">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Folder, Document } from '@element-plus/icons-vue'
import { createTag, updateTag, getTagById } from '@/api/tag'
import { useUserStore } from '@/stores/user'
import type { Tag } from '@/types/tag'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 表单数据
const tagForm = reactive<Partial<Tag>>({
  name: '',
  color: '#409EFF',
  description: ''
})

// 预设颜色
const presetColors = [
  // 蓝色系
  '#1976D2', // 靛蓝
  '#2196F3', // 天蓝
  '#03A9F4', // 浅蓝
  '#00BCD4', // 青色
  '#009688', // 墨绿
  '#4CAF50', // 绿色
  
  // 绿色系
  '#8BC34A', // 浅绿
  '#CDDC39', // 黄绿
  '#FFEB3B', // 黄色
  '#FFC107', // 琥珀
  '#FF9800', // 橙色
  '#FF5722', // 深橙
  
  // 红色系
  '#F44336', // 红色
  '#E91E63', // 粉红
  '#9C27B0', // 紫色
  '#673AB7', // 深紫
  '#3F51B5', // 靛青
  '#607D8B', // 蓝灰
  
  // 莫兰迪色系
  '#D8B5A5', // 莫兰迪粉
  '#B3A59D', // 莫兰迪灰
  '#BFD0C9', // 莫兰迪绿
  '#C8D0BE', // 莫兰迪浅绿
  '#EADCD1', // 莫兰迪米色
  '#AAB0AF', // 莫兰迪深灰
  
  // 其他色调
  '#795548', // 棕色
  '#9E9E9E', // 灰色
  '#455A64', // 深蓝灰
  '#212121', // 近黑色
]

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 30, message: '长度在1到30个字符之间', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请选择标签颜色', trigger: 'change' }
  ]
}

// 获取标签详情
const fetchTagDetail = async (tagId: string) => {
  loading.value = true
  try {
    const response = await getTagById(Number(tagId))
    if (response.data && response.data.code === 1) {
      const tagData = response.data.data
      if (tagData) {
        tagForm.name = tagData.name
        tagForm.color = tagData.color
        tagForm.description = tagData.description
      } else {
        ElMessage.error('获取标签详情失败')
        router.back()
      }
    } else {
      ElMessage.error('获取标签详情失败')
      router.back()
    }
  } catch (error) {
    console.error('获取标签详情失败:', error)
    ElMessage.error('获取标签详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 获取对比色（确保标签文本清晰可见）
const getContrastColor = (hexColor: string) => {
  // 处理空值
  if (!hexColor) return '#000000'
  
  // 处理带透明度的颜色值
  const color = hexColor.replace(/^rgba?\(|\s+|\)$/g, '').split(',')
  
  // 如果是十六进制颜色
  if (hexColor.startsWith('#')) {
    const r = parseInt(hexColor.substring(1, 3), 16)
    const g = parseInt(hexColor.substring(3, 5), 16)
    const b = parseInt(hexColor.substring(5, 7), 16)
    
    // 计算亮度
    const brightness = (r * 299 + g * 587 + b * 114) / 1000
    
    // 根据亮度返回黑色或白色
    return brightness > 128 ? '#000000' : '#ffffff'
  } 
  // 如果是rgba颜色
  else if (color.length >= 3) {
    const r = parseInt(color[0])
    const g = parseInt(color[1])
    const b = parseInt(color[2])
    
    // 计算亮度
    const brightness = (r * 299 + g * 587 + b * 114) / 1000
    
    // 根据亮度返回黑色或白色
    return brightness > 128 ? '#000000' : '#ffffff'
  }
  
  return '#000000' // 默认返回黑色
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 准备提交的数据
    const submitData = {
      name: tagForm.name,
      color: tagForm.color,
      description: tagForm.description
    }
    
    if (isEdit.value) {
      // 更新标签
      const tagId = route.params.id as string
      await updateTag(Number(tagId), submitData)
      ElMessage.success('更新标签成功')
    } else {
      // 创建标签
      await createTag(submitData)
      ElMessage.success('创建标签成功')
    }
    
    // 返回标签列表
    router.push('/tag/list')
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  router.back()
}

onMounted(async () => {
  // 检查用户权限
  if (userStore.userInfo?.role !== 1) {
    ElMessage.error('只有项目负责人才能创建或编辑标签')
    router.push('/tag/list')
    return
  }
  
  // 如果是编辑模式，获取标签详情
  if (isEdit.value) {
    const tagId = route.params.id as string
    await fetchTagDetail(tagId)
  }
})
</script>

<style scoped>
.tag-form-container {
  padding: 24px;
}

.tag-form-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-form {
  margin-top: 20px;
}

.form-group {
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

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
}

.color-preview {
  flex-grow: 1;
  height: 36px; /* 匹配颜色选择器的高度 */
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  position: relative;
  transition: all 0.3s;
  padding: 0 12px;
  word-break: break-word;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.color-text {
  z-index: 1;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.preset-colors {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-start;
}

.color-item {
  width: 38px;
  height: 38px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.color-item:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #dcdfe6;
}

.color-item.active {
  transform: scale(1.1);
  border-color: #fff;
  box-shadow: 0 0 0 2px var(--el-color-primary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 30px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  box-shadow: none;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  border-color: var(--el-color-primary);
}

:deep(.el-textarea__inner) {
  min-height: 80px !important;
  resize: none;
}

.empty-tasks {
  text-align: center;
  padding: 20px 0;
  color: #909399;
}
</style> 