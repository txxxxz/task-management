<template>
  <div class="task-detail-container">
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
          <el-col :xs="24" :sm="8" :md="8" :lg="8">
            <el-form-item label="任务编号">
              <el-input v-model="taskForm.number" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="8" :md="8" :lg="8">
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
          <el-col :xs="12" :sm="8" :md="8" :lg="8">
            <el-form-item label="状态">
              <template v-if="isLeader">
                <el-select v-model="taskForm.status" style="width: 100%">
                  <el-option label="待处理" value="PENDING" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="待审核" value="REVIEW" />
                  <el-option label="已完成" value="COMPLETED" />
                </el-select>
              </template>
              <template v-else>
                <el-tag :type="getStatusType(taskForm.status)" effect="light">
                  {{ getStatusLabel(taskForm.status) }}
                </el-tag>
              </template>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务名称">
          <el-input 
            v-model="taskForm.name" 
            :disabled="!isLeader"
            placeholder="请输入任务名称"
          />
        </el-form-item>


        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="创建时间">
              <el-input v-model="taskForm.createTime" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="taskForm.dueTime as string | Date"
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
            collapse-tags
            collapse-tags-tooltip
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
            collapse-tags
            collapse-tags-tooltip
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
          :action="`/api/tasks/${route.params.id}/attachments`"
          :headers="uploadHeaders"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          multiple
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持任意文件格式，单个文件不超过100MB
            </div>
          </template>
        </el-upload>
      </el-dialog>
    </el-card>

    <!-- 评论区卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>评论 ({{ comments.length }})</h2>
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
import { useUserStore } from '../../stores/user'
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
import type { TaskDetail, TaskFile, TaskComment } from '../../types/task'
import { getTaskDetail, updateTask, deleteTask, getTaskComments, createComment, deleteComment, getTaskAttachments, deleteAttachment } from '../../api/task'

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
  number: '',
  name: '',
  createTime: '',
  dueTime: '' as string | Date | null,
  priority: 'MEDIUM' as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW',
  status: 'PENDING' as 'PENDING' | 'IN_PROGRESS' | 'REVIEW' | 'COMPLETED',
  members: [] as string[],
  tags: [] as string[],
  description: ''
})

// 成员选项 - 硬编码默认选项，后续可从后端获取
const memberOptions = ref<{ label: string, value: string }[]>([
  { label: 'Tom', value: 'Tom' },
  { label: 'John', value: 'John' },
  { label: 'Amy', value: 'Amy' },
  { label: 'Jack', value: 'Jack' }
])

// 标签选项 - 硬编码默认选项，后续可从后端获取
const tagOptions = ref<{ label: string, value: string }[]>([
  { label: 'Bug', value: 'Bug' },
  { label: 'Login', value: 'Login' },
  { label: '功能开发', value: '功能开发' },
  { label: '性能优化', value: '性能优化' }
])

// 文件列表 - 从后端获取
const fileList = ref<TaskFile[]>([])

// 评论列表 - 从后端获取
const comments = ref<TaskComment[]>([])

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

// 获取状态标签类型
const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'danger' => {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'REVIEW': 'info',
    'COMPLETED': 'success'
  }
  return types[status] || 'info'
}

// 获取状态显示标签
const getStatusLabel = (status: string): string => {
  const labels: Record<string, string> = {
    'PENDING': '待处理',
    'IN_PROGRESS': '进行中',
    'REVIEW': '待审核',
    'COMPLETED': '已完成'
  }
  return labels[status] || '未知状态'
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
    console.log('获取任务详情返回:', response) // 调试日志
    
    // 将API返回的数据映射到表单
    let data = response.data
    if (!data && (response as any).result) {
      data = (response as any).result // 兼容返回result的情况
    }
    
    if (data) {
      console.log('解析后的任务数据:', data) // 调试日志
      
      // 优先级转换（API返回数字，转为字符串）
      const priorityMap: Record<number, string> = {
        4: 'CRITICAL',
        3: 'HIGH',
        2: 'MEDIUM',
        1: 'LOW'
      }
      
      // 状态转换（API返回数字，转为字符串）
      const statusMap: Record<number, string> = {
        1: 'PENDING',
        2: 'IN_PROGRESS',
        3: 'REVIEW',
        4: 'COMPLETED'
      }
      
      // 确保数据类型正确
      const priority = typeof data.priority === 'number' ? data.priority : parseInt(data.priority) || 2
      const status = typeof data.status === 'number' ? data.status : parseInt(data.status) || 1
      
      // 处理日期时间 - 支持多种可能的日期字段名
      const createTime = data.createTime ? dayjs(data.createTime).format('YYYY-MM-DD HH:mm:ss') : ''
      
      // 截止时间可能有多种字段名
      const dueTimeValue = data.dueTime || data.deadline || data.endTime
      let dueTime: Date | null = null
      if (dueTimeValue) {
        try {
          dueTime = dayjs(dueTimeValue).toDate()
        } catch (e) {
          console.error('日期转换错误:', e)
        }
      }
      
      // 更新任务表单数据
      taskForm.number = data.id || data.number || ''
      taskForm.name = data.name || data.title || '' // 支持name或title字段
      taskForm.description = data.description || ''
      taskForm.createTime = createTime
      taskForm.dueTime = dueTime
      taskForm.priority = (priorityMap[priority] || 'MEDIUM') as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
      taskForm.status = (statusMap[status] || 'PENDING') as 'PENDING' | 'IN_PROGRESS' | 'REVIEW' | 'COMPLETED'
      
      // 处理成员数据 - 成员可能是字符串数组或对象数组
      if (Array.isArray(data.members)) {
        if (data.members.length > 0) {
          if (typeof data.members[0] === 'string') {
            taskForm.members = data.members
          } else if (typeof data.members[0] === 'object' && data.members[0]?.username) {
            taskForm.members = data.members.map(m => m.username)
          }
        } else {
          taskForm.members = []
        }
      } else if (data.memberIds && Array.isArray(data.memberIds)) {
        taskForm.members = data.memberIds
      } else {
        taskForm.members = []
      }
      
      // 处理标签数据 - 标签可能是字符串数组或对象数组
      if (Array.isArray(data.tags)) {
        if (data.tags.length > 0) {
          if (typeof data.tags[0] === 'string') {
            taskForm.tags = data.tags
          } else if (typeof data.tags[0] === 'object' && data.tags[0]?.name) {
            taskForm.tags = data.tags.map(t => t.name)
          }
        } else {
          taskForm.tags = []
        }
      } else if (data.tagIds && Array.isArray(data.tagIds)) {
        taskForm.tags = data.tagIds
      } else {
        taskForm.tags = []
      }
      
      // 如果API返回了文件列表，更新文件列表
      if (data.files && Array.isArray(data.files)) {
        fileList.value = data.files.map((file: any) => ({
          id: file.id || '',
          name: file.name || '',
          url: file.url || '',
          size: typeof file.size === 'number' ? file.size : 0,
          uploader: file.uploader || '未知',
          uploadTime: file.uploadTime || ''
        }))
      } else if (data.attachments && Array.isArray(data.attachments)) {
        // 支持attachments字段
        fileList.value = data.attachments.map((attachment: any) => {
          // 如果是字符串，可能只是文件URL
          if (typeof attachment === 'string') {
            return {
              id: '', // 没有ID信息
              name: attachment.split('/').pop() || '未知文件', // 从URL提取文件名
              url: attachment,
              size: 0, // 无法获取大小
              uploader: '未知',
              uploadTime: ''
            }
          } else {
            // 如果是对象
            return {
              id: attachment.id || '',
              name: attachment.name || attachment.fileName || '',
              url: attachment.url || '',
              size: typeof attachment.size === 'number' ? attachment.size : 0,
              uploader: attachment.uploader || attachment.creator || '未知',
              uploadTime: attachment.uploadTime || attachment.createTime || ''
            }
          }
        })
      } else {
        fileList.value = []
      }
      
      // 如果API返回了评论列表，更新评论列表
      if (data.comments && Array.isArray(data.comments)) {
        comments.value = data.comments.map((comment: any) => ({
          id: comment.id || 0,
          username: comment.username || comment.author || '未知用户',
          avatar: comment.avatar || '',
          content: comment.content || comment.text || '',
          createTime: comment.createTime || comment.time || ''
        }))
      } else {
        // 尝试加载评论列表
        try {
          const commentsResponse = await getTaskComments(parseInt(taskId))
          if (commentsResponse.data && Array.isArray(commentsResponse.data)) {
            comments.value = commentsResponse.data
          } else {
            comments.value = []
          }
        } catch (err) {
          console.error('获取评论列表失败:', err)
          comments.value = []
        }
      }
      
      // 文件和评论可能需要单独加载
      try {
        const filesResponse = await getTaskAttachments(parseInt(taskId))
        if (filesResponse.data && Array.isArray(filesResponse.data)) {
          fileList.value = filesResponse.data.map(file => ({
            id: file.id.toString() || '',
            name: file.name || '',
            url: file.url || '',
            size: typeof file.size === 'number' ? file.size : 0,
            uploader: file.uploader || '未知',
            uploadTime: file.createTime || ''
          }))
        }
      } catch (err) {
        console.error('获取文件列表失败:', err)
        // 如果之前已有文件列表，则保留
      }
      
      // 尝试加载评论列表
      try {
        const commentsResponse = await getTaskComments(parseInt(taskId))
        if (commentsResponse.data && Array.isArray(commentsResponse.data)) {
          comments.value = commentsResponse.data.map(comment => ({
            id: comment.id,
            username: comment.username || '未知用户',
            avatar: comment.avatar || '',
            content: comment.content || '',
            createTime: comment.createTime || ''
          }))
        }
      } catch (err) {
        console.error('获取评论列表失败:', err)
        comments.value = []
      }
      
      // API可能会返回成员选项，但如果没有返回，则使用默认值
      if ((data as any).memberOptions && Array.isArray((data as any).memberOptions)) {
        memberOptions.value = (data as any).memberOptions
      }
      
      // API可能会返回标签选项，但如果没有返回，则使用默认值
      if ((data as any).tagOptions && Array.isArray((data as any).tagOptions)) {
        tagOptions.value = (data as any).tagOptions
      }
    } else {
      ElMessage.warning('未获取到任务数据')
    }
  } catch (err: any) {
    console.error('获取任务详情失败:', err)
    error.value = err.message || '获取任务详情失败'
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
    // 将优先级字符串转换为数字
    const priorityMap: Record<string, number> = {
      'CRITICAL': 4,
      'HIGH': 3,
      'MEDIUM': 2,
      'LOW': 1
    }
    
    // 将状态字符串转换为数字
    const statusMap: Record<string, number> = {
      'PENDING': 1,
      'IN_PROGRESS': 2,
      'REVIEW': 3,
      'COMPLETED': 4
    }
    
    // 日期处理
    let deadline = null
    if (taskForm.dueTime) {
      try {
        deadline = dayjs(taskForm.dueTime).format('YYYY-MM-DD HH:mm:ss')
      } catch (e) {
        console.error('日期格式化错误:', e)
      }
    }
    
    const updateData = {
      id: parseInt(taskForm.number) || undefined,
      name: taskForm.name,
      description: taskForm.description,
      deadline,  // 使用deadline字段，与后端一致
      priority: priorityMap[taskForm.priority] || 2, // 默认为中等优先级
      status: statusMap[taskForm.status] || 1, // 默认为待处理
      members: taskForm.members,
      tags: taskForm.tags,
      projectId: parseInt(route.query.projectId as string) || 12 // 使用URL中的项目ID或默认值
    }
    
    console.log('更新任务数据:', updateData) // 调试日志
    const response = await updateTask(route.params.id as string, updateData)
    console.log('更新任务响应:', response) // 调试日志
    
    ElMessage.success('保存成功')
    
    // 刷新任务数据
    await fetchTaskDetail()
  } catch (err: any) {
    console.error('保存任务失败:', err)
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
  ElMessage.info('批量导入功能正在开发中')
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

  uploadLoading.value = true
  return true
}

const handleUploadSuccess = async (response: any) => {
  uploadLoading.value = false
  try {
    // 处理后端返回结果
    if (response.code === 0 || response.status === 200 || response.success) {
      ElMessage.success('上传成功')
      uploadDialogVisible.value = false
      
      // 刷新文件列表
      try {
        const response = await getTaskAttachments(parseInt(route.params.id as string))
        if (response.data && Array.isArray(response.data)) {
          fileList.value = response.data.map(file => ({
            id: file.id.toString() || '',
            name: file.name || '',
            url: file.url || '',
            size: typeof file.size === 'number' ? file.size : 0,
            uploader: file.uploader || '未知',
            uploadTime: file.createTime || ''
          }))
        }
      } catch (err) {
        console.error('获取文件列表失败:', err)
      }
    } else {
      throw new Error(response.msg || response.message || '上传失败')
    }
  } catch (err: any) {
    ElMessage.error(err.message || '上传失败')
  }
}

const handleUploadError = (err: any) => {
  uploadLoading.value = false
  const errMsg = err.message || (typeof err === 'string' ? err : '上传失败')
  ElMessage.error(errMsg)
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
    await ElMessageBox.confirm('确定要删除此文件吗？', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    // 删除文件
    await deleteAttachment(parseInt(route.params.id as string), parseInt(file.id))
    
    ElMessage.success('删除成功')
    
    // 刷新文件列表
    try {
      const response = await getTaskAttachments(parseInt(route.params.id as string))
      if (response.data && Array.isArray(response.data)) {
        fileList.value = response.data.map(file => ({
          id: file.id.toString() || '',
          name: file.name || '',
          url: file.url || '',
          size: typeof file.size === 'number' ? file.size : 0,
          uploader: file.uploader || '未知',
          uploadTime: file.createTime || ''
        }))
      }
    } catch (err) {
      console.error('获取文件列表失败:', err)
    }
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
    // 添加评论
    await createComment(parseInt(route.params.id as string), newComment.value)
    
    ElMessage.success('评论成功')
    newComment.value = '' // 清空评论输入框
    
    // 刷新评论列表
    try {
      const response = await getTaskComments(parseInt(route.params.id as string))
      if (response.data && Array.isArray(response.data)) {
        comments.value = response.data.map(comment => ({
          id: comment.id,
          username: comment.username || '未知用户',
          avatar: comment.avatar || '',
          content: comment.content || '',
          createTime: comment.createTime || ''
        }))
      }
    } catch (err) {
      console.error('获取评论列表失败:', err)
    }
    
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
    await ElMessageBox.confirm('确定要删除此评论吗？', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    // 删除评论
    await deleteComment(parseInt(route.params.id as string), comment.id)
    
    ElMessage.success('删除成功')
    
    // 刷新评论列表
    try {
      const response = await getTaskComments(parseInt(route.params.id as string))
      if (response.data && Array.isArray(response.data)) {
        comments.value = response.data.map(comment => ({
          id: comment.id,
          username: comment.username || '未知用户',
          avatar: comment.avatar || '',
          content: comment.content || '',
          createTime: comment.createTime || ''
        }))
      }
    } catch (err) {
      console.error('获取评论列表失败:', err)
    }
    
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
      '确定要删除此任务吗？此操作不可撤销！',
      '警告',
      {
        confirmButtonText: '删除',
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
onMounted(async () => {
  await fetchTaskDetail()
})
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
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.detail-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background-color: #f9f9f9;
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
  color: var(--el-text-color-primary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 表单样式优化 */
:deep(.el-form) {
  padding: 10px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: var(--el-fill-color-light);
}

/* 优先级和状态标签样式 */
:deep(.el-tag) {
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .task-detail-container {
    padding: 10px;
  }
  
  .detail-card {
    margin-bottom: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  :deep(.el-form-item__label) {
    padding-bottom: 4px;
  }
}

/* 表单元素间距优化 */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-date-editor) {
  width: 100% !important;
}

/* 多行文本区域样式 */
:deep(.el-textarea__inner) {
  min-height: 100px;
  line-height: 1.6;
}

/* 表单行间距 */
.el-row {
  margin-bottom: 8px;
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