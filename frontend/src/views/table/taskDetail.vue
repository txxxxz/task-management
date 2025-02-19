<template>
  <div class="task-detail-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/list' }">任务列表</el-breadcrumb-item>
      <el-breadcrumb-item>任务详情</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="handleBack">
        <el-icon><Back /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 加载状态 -->
    <el-loading
      v-model:loading="loading"
      :lock="true"
      text="加载中..."
      background="rgba(255, 255, 255, 0.7)"
    />

    <!-- 错误提示 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
      class="error-alert"
    />

    <!-- 任务基本信息卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>任务详情</h2>
          <div class="header-actions">
            <el-button type="primary" plain @click="handleSave" v-if="isLeader" :loading="saveLoading">
              <el-icon><Check /></el-icon>
              保存修改
            </el-button>
            <el-button type="danger" plain @click="handleDelete" v-if="isLeader">
              <el-icon><Delete /></el-icon>
              删除任务
            </el-button>
          </div>
        </div>
      </template>

      <el-form :model="taskForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="任务编号">
              <el-input v-model="taskForm.number" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建时间">
              <el-input v-model="taskForm.createTime" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级">
              <template v-if="isLeader">
                <el-select v-model="taskForm.priority" style="width: 100%">
                  <el-option
                    v-for="item in priorityOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </template>
              <template v-else>
                <el-tag :type="getPriorityType(taskForm.priority)" effect="light">
                  {{ taskForm.priority }}
                </el-tag>
              </template>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="任务名称">
              <el-input 
                v-model="taskForm.name" 
                :disabled="!isLeader"
                placeholder="请输入任务名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="taskForm.dueTime"
                type="datetime"
                :disabled="!isLeader"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务成员">
          <el-select
            v-model="taskForm.members"
            multiple
            :disabled="!isLeader"
            style="width: 100%"
          >
            <el-option
              v-for="member in memberOptions"
              :key="member.value"
              :label="member.label"
              :value="member.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="任务标签">
          <el-select
            v-model="taskForm.tags"
            multiple
            :disabled="!isLeader"
            style="width: 100%"
          >
            <el-option
              v-for="tag in tagOptions"
              :key="tag.value"
              :label="tag.label"
              :value="tag.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="任务描述">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="4"
            :disabled="!isLeader"
            placeholder="请输入任务描述"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务文件卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>任务文件</h2>
          <div class="header-actions">
            <el-button type="primary" @click="handleUpload">
              <el-icon><Upload /></el-icon>
              上传文件
            </el-button>
            <el-button @click="handleBatchUpload">
              <el-icon><Document /></el-icon>
              批量导入
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="fileList" style="width: 100%">
        <el-table-column prop="name" label="文件名" min-width="200" />
        <el-table-column prop="uploader" label="上传者" width="120" />
        <el-table-column prop="uploadTime" label="上传时间" width="160" />
        <el-table-column prop="size" label="大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-tooltip content="查看文件" placement="top">
                <el-button type="primary" link @click="handleViewFile(row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="下载文件" placement="top">
                <el-button type="primary" link @click="handleDownloadFile(row)">
                  <el-icon><Download /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除文件" placement="top">
                <el-button 
                  type="danger" 
                  link 
                  @click="handleDeleteFile(row)"
                  v-if="isLeader || row.uploader === userStore.userInfo?.username"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 文件上传对话框 -->
      <el-dialog
        v-model="uploadDialogVisible"
        title="上传文件"
        width="500px"
      >
        <el-upload
          class="upload-demo"
          drag
          action="/api/upload"
          :headers="uploadHeaders"
          :data="{ taskId: taskForm.number }"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :loading="uploadLoading"
          multiple
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持任意格式文件，单个文件不超过100MB
            </div>
          </template>
        </el-upload>
      </el-dialog>
    </el-card>

    <!-- 评论区卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>评论区 ({{ comments.length }})</h2>
        </div>
      </template>

      <div class="comments-container">
        <!-- 评论列表 -->
        <div class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="40" :src="comment.avatar">
              {{ comment.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="comment-content">
              <div class="comment-header">
                <span class="username">{{ comment.username }}</span>
                <span class="time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-actions">
                <el-button type="primary" link @click="handleReply(comment)">
                  回复
                </el-button>
                <el-button 
                  type="danger" 
                  link 
                  @click="handleDeleteComment(comment)"
                  v-if="isLeader || comment.username === userStore.userInfo?.username"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 评论输入框 -->
        <div class="comment-input">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
          />
          <div class="comment-buttons">
            <el-button type="primary" @click="handleAddComment" :loading="commentLoading">
              发表评论
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check,
  Upload,
  Document,
  View,
  Download,
  Delete,
  UploadFilled,
  Back
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import type { TaskDetail, TaskFile, TaskComment } from '@/types/task'
import { getTaskDetail, updateTask, deleteTask } from '@/api/task'
import { uploadFile, deleteFile } from '@/api/file'
import { addComment, deleteComment } from '@/api/comment'

dayjs.extend(relativeTime)

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const uploadDialogVisible = ref(false)

// 加载状态
const loading = ref(false)
const uploadLoading = ref(false)
const saveLoading = ref(false)
const commentLoading = ref(false)

// 错误状态
const error = ref<string | null>(null)

// 判断是否为leader
const isLeader = computed(() => {
  return userStore.userInfo?.role === 1
})

// 上传请求头
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('token')
}))

// 任务表单数据
const taskForm = reactive({
  number: '1',
  name: 'Fix Login Page Authentication Bug',
  createTime: '2024-12-12',
  dueTime: '2025-02-01',
  priority: 'HIGH' as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW',
  members: ['Tom', 'John', 'Amy'],
  tags: ['Bug', 'Login'],
  description: 'Fix the login authentication bug by identifying root cause, debugging backend/frontend, ensuring secure data handling, testing extensively, improving error messages, and documenting changes effectively.'
})

// 成员选项
const memberOptions = [
  { label: 'Tom', value: 'Tom' },
  { label: 'John', value: 'John' },
  { label: 'Amy', value: 'Amy' },
  { label: 'Jack', value: 'Jack' }
]

// 标签选项
const tagOptions = [
  { label: 'Bug', value: 'Bug' },
  { label: 'Login', value: 'Login' },
  { label: '功能开发', value: '功能开发' },
  { label: '性能优化', value: '性能优化' }
]

// 文件列表
const fileList = ref([
  {
    name: 'File1.tx',
    uploader: 'Tom',
    uploadTime: '2024-01-15 09:00:00',
    size: 1024 * 1024 * 2 // 2MB
  },
  {
    name: 'Code.p',
    uploader: 'Tom',
    uploadTime: '2024-01-15 09:00:00',
    size: 1024 * 512 // 512KB
  }
])

// 评论列表
const comments = ref([
  {
    id: 1,
    username: 'John',
    avatar: '',
    content: 'I will do some research~',
    createTime: '2024-01-15 10:00:00'
  },
  {
    id: 2,
    username: 'Amy',
    avatar: '',
    content: 'I know how to fix it, don\'t worry~',
    createTime: '2024-01-15 11:00:00'
  }
])

// 新评论内容
const newComment = ref('')

// 优先级选项
const priorityOptions = [
  { label: '紧急', value: 'CRITICAL' },
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' }
]

// 获取优先级标签类型
const getPriorityType = (priority: string): 'success' | 'warning' | 'info' | 'danger' => {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'info',
    'LOW': 'success'
  }
  return types[priority] || 'info'
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// 模拟数据
const mockData = {
  taskDetail: {
    id: '1',
    number: 'TASK-001',
    name: '前端项目重构',
    description: '对现有前端项目进行重构，提高代码质量和性能',
    createTime: '2024-01-15 09:00:00',
    dueTime: '2024-02-15 18:00:00',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    members: ['Tom', 'John', 'Amy'],
    tags: ['重构', '前端', '性能优化'],
    files: [
      {
        id: '1',
        name: 'architecture.pdf',
        url: 'https://example.com/files/1',
        size: 2048576, // 2MB
        uploader: 'Tom',
        uploadTime: '2024-01-15 10:00:00'
      },
      {
        id: '2',
        name: 'api-doc.md',
        url: 'https://example.com/files/2',
        size: 524288, // 512KB
        uploader: 'John',
        uploadTime: '2024-01-15 11:00:00'
      }
    ],
    comments: [
      {
        id: 1,
        username: 'Tom',
        avatar: '',
        content: '我已经开始进行代码审查了',
        createTime: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        username: 'John',
        avatar: '',
        content: '性能测试结果已经更新',
        createTime: '2024-01-15 11:30:00'
      }
    ]
  }
}

// 使用模拟数据初始化表单
const initMockData = () => {
  const { taskDetail } = mockData
  Object.assign(taskForm, {
    number: taskDetail.number,
    name: taskDetail.name,
    createTime: taskDetail.createTime,
    dueTime: taskDetail.dueTime,
    priority: taskDetail.priority,
    members: taskDetail.members,
    tags: taskDetail.tags,
    description: taskDetail.description
  })
  
  fileList.value = taskDetail.files
  comments.value = taskDetail.comments
}

// 获取任务详情
const fetchTaskDetail = async () => {
  const taskId = route.params.id as string
  if (!taskId) {
    error.value = '任务ID不能为空'
    return
  }

  loading.value = true
  error.value = null

  try {
    const response = await getTaskDetail(taskId)
    Object.assign(taskForm, response.data)
  } catch (err: any) {
    // 如果API调用失败，使用模拟数据
    console.warn('使用模拟数据')
    initMockData()
    error.value = null // 清除错误提示
  } finally {
    loading.value = false
  }
}

// 保存任务修改
const handleSave = async () => {
  if (!isLeader.value) {
    ElMessage.warning('您没有权限修改任务信息')
    return
  }

  saveLoading.value = true
  try {
    const updateData = {
      name: taskForm.name,
      description: taskForm.description,
      dueTime: taskForm.dueTime,
      priority: taskForm.priority,
      members: taskForm.members,
      tags: taskForm.tags
    }
    await updateTask(route.params.id as string, updateData)
    ElMessage.success('保存成功')
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

// 文件上传相关方法
const handleUpload = () => {
  uploadDialogVisible.value = true
}

const handleBatchUpload = () => {
  // TODO: 实现批量上传逻辑
}

const beforeUpload = (file: File) => {
  const maxSize = 100 * 1024 * 1024 // 100MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }

  // 检查文件名是否合法
  const invalidChars = /[\\\\/:*?"<>|]/g
  if (invalidChars.test(file.name)) {
    ElMessage.error('文件名不能包含特殊字符')
    return false
  }

  return true
}

const handleUploadSuccess = async (response: any) => {
  try {
    // 上传到阿里云OSS
    const formData = new FormData()
    formData.append('file', response.file)
    formData.append('taskId', taskForm.number)
    
    uploadLoading.value = true
    await uploadFile(formData)
    
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    await fetchTaskDetail() // 刷新文件列表
  } catch (err: any) {
    ElMessage.error(err.message || '上传失败')
  } finally {
    uploadLoading.value = false
  }
}

const handleUploadError = (err: any) => {
  ElMessage.error(err.message || '上传失败')
}

// 文件操作方法
const handleViewFile = (file: TaskFile) => {
  if (!file.url) {
    ElMessage.warning('文件链接不可用')
    return
  }
  window.open(file.url)
}

const handleDownloadFile = (file: TaskFile) => {
  if (!file.url) {
    ElMessage.warning('文件链接不可用')
    return
  }
  
  try {
    const link = document.createElement('a')
    link.href = file.url
    link.download = file.name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    ElMessage.error('下载失败')
  }
}

const handleDeleteFile = async (file: TaskFile) => {
  if (!isLeader.value && file.uploader !== userStore.userInfo?.username) {
    ElMessage.warning('您没有权限删除此文件')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该文件吗？', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    await deleteFile(file.id)
    ElMessage.success('删除成功')
    await fetchTaskDetail() // 刷新文件列表
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// 评论相关方法
const handleAddComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentLoading.value = true
  try {
    await addComment({
      taskId: route.params.id as string,
      content: newComment.value
    })

    comments.value.push({
      id: comments.value.length + 1,
      username: userStore.userInfo?.username || 'Unknown',
      avatar: userStore.userInfo?.avatar || '',
      content: newComment.value,
      createTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
    })
    newComment.value = ''
    ElMessage.success('评论成功')
  } catch (err: any) {
    ElMessage.error(err.message || '评论失败')
  } finally {
    commentLoading.value = false
  }
}

const handleReply = (comment: TaskComment) => {
  newComment.value = '@' + comment.username + ' '
}

const handleDeleteComment = async (comment: TaskComment) => {
  if (!isLeader.value && comment.username !== userStore.userInfo?.username) {
    ElMessage.warning('您没有权限删除此评论')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该评论吗？', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    await deleteComment(comment.id)
    const index = comments.value.findIndex(c => c.id === comment.id)
    if (index !== -1) {
      comments.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// 处理删除任务
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该任务吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    loading.value = true
    await deleteTask(route.params.id as string)
    ElMessage.success('删除成功')
    router.push('/list')
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      fetchTaskDetail()
    }
  },
  { immediate: true }
)

// 在组件挂载时获取数据
onMounted(fetchTaskDetail)
</script>

<style scoped>
.task-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.detail-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 评论区样式 */
.comments-container {
  padding: 0 20px;
}

.comments-list {
  margin-bottom: 20px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.comment-content {
  flex: 1;
}

.comment-header {
  margin-bottom: 8px;
}

.username {
  font-weight: 500;
  margin-right: 8px;
}

.time {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.comment-text {
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.comment-input {
  margin-top: 20px;
}

.comment-buttons {
  margin-top: 12px;
  text-align: right;
}

/* 上传对话框样式 */
.upload-demo {
  text-align: center;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

:deep(.el-upload__tip) {
  margin-top: 8px;
  text-align: center;
}

/* 表单样式优化 */
:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: var(--el-fill-color-light);
}

/* 文件表格样式 */
:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
  --el-table-row-hover-bg-color: var(--el-fill-color-lighter);
  border-radius: 4px;
  overflow: hidden;
}

:deep(.el-table th) {
  font-weight: 600;
  background-color: var(--el-fill-color-light);
}

/* 按钮样式优化 */
.el-button-group .el-button--link {
  padding: 4px 8px;
}

.el-button-group .el-button--link:hover {
  opacity: 0.8;
}

.back-button {
  margin-bottom: 16px;
}

.error-alert {
  margin-bottom: 16px;
}

/* 加载状态样式 */
:deep(.el-loading-spinner) {
  .el-loading-text {
    color: var(--el-color-primary);
  }
}

.breadcrumb {
  margin-bottom: 16px;
}

:deep(.el-breadcrumb__inner) {
  color: var(--el-text-color-regular);
  &.is-link {
    color: var(--el-color-primary);
    &:hover {
      color: var(--el-color-primary-light-3);
    }
  }
}
</style> 