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
            >
              <div class="member-option">
                <el-avatar :size="24" :src="member.avatar">{{ member.label.charAt(0) }}</el-avatar>
                <span>{{ member.label }}</span>
              </div>
            </el-option>
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
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            >
              <div class="tag-option">
                <el-tag 
                  :style="{
                    backgroundColor: tag.color ? tag.color + '22' : '#409EFF22',
                    borderColor: tag.color || '#409EFF',
                    color: tag.color || '#409EFF'
                  }"
                  size="small"
                >
                  <span class="tag-text">{{ tag.name }}</span>
                </el-tag>
              </div>
            </el-option>
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
          <h2>评论</h2>
        </div>
      </template>

      <div class="comments-container">
        <!-- 评论列表，使用递归组件渲染树状结构 -->
        <div class="comments-list">
          <template v-if="comments.length === 0">
            <el-empty description="暂无评论" />
          </template>
          <template v-else>
            <div v-for="comment in comments" :key="comment.id">
              <comment-item 
                :comment="comment" 
                @reply="handleReply" 
                @delete="handleDeleteComment"
              />
            </div>
          </template>
        </div>

        <!-- 评论输入框 -->
        <div class="comment-input">
          <div v-if="replyToComment" class="reply-info">
            回复给：{{ replyToComment.creator.username }}
            <el-button type="text" @click="cancelReply">取消回复</el-button>
          </div>
          <el-input
            v-model="newComment.content"
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
import type { TaskDetail, TaskFile } from '../../types/task'
import type { Comment } from '../../types/models'
import { getTaskDetail, updateTask, deleteTask, getTaskComments, createComment, deleteComment, getTaskAttachments, deleteAttachment } from '../../api/task'
import { getAllUsers } from '../../api/user'
import { getTagList, getAllTags } from '@/api/tag'

// 定义CommentItem组件
const CommentItem = {
  name: 'CommentItem',
  props: {
    comment: {
      type: Object as () => Comment,
      required: true
    }
  },
  setup(props, { emit }) {
    const userStore = useUserStore()
    
    // 判断是否为leader
    const isLeader = computed(() => {
      return userStore.userInfo?.role === 1
    })
    
    // 格式化时间
    const formatTime = (time: string) => {
      return dayjs(time).fromNow()
    }
    
    return {
      userStore,
      isLeader,
      formatTime
    }
  },
  emits: ['reply', 'delete'],
  template: `
    <div class="comment-item">
      <el-avatar :size="40" :src="comment.creator.avatar">
        {{ comment.creator.username?.charAt(0).toUpperCase() }}
      </el-avatar>
      <div class="comment-content">
        <div class="comment-header">
          <span class="username">{{ comment.creator.username }}</span>
          <span class="time">{{ formatTime(comment.createTime) }}</span>
        </div>
        <div class="comment-text">{{ comment.content }}</div>
        <div class="comment-actions">
          <el-button type="primary" link @click="$emit('reply', comment)">
            回复
          </el-button>
          <el-button 
            type="danger" 
            link 
            @click="$emit('delete', comment)"
            v-if="isLeader || comment.creator.username === userStore.userInfo?.username"
          >
            删除
          </el-button>
        </div>
        
        <!-- 递归渲染子评论 -->
        <div v-if="comment.children && comment.children.length > 0" class="children-comments">
          <div v-for="childComment in comment.children" :key="childComment.id" class="child-comment">
            <comment-item 
              :comment="childComment" 
              @reply="$emit('reply', childComment)" 
              @delete="$emit('delete', childComment)"
            />
          </div>
        </div>
      </div>
    </div>
  `
}

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
const membersLoading = ref(false)
const tagsLoading = ref(false)

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

// 成员选项 - 从后端获取
const memberOptions = ref<{ label: string, value: string, avatar?: string }[]>([])

// 标签选项 - 从后端获取
const tagOptions = ref<{ id: string, name: string, color?: string, projectId?: string }[]>([])

// 文件列表 - 从后端获取
const fileList = ref<TaskFile[]>([])

// 评论列表 - 从后端获取，使用树状结构
const comments = ref<any[]>([])

// 新评论内容和回复信息
const newComment = reactive({
  content: '',
  parentId: null as number | null
})
const replyToComment = ref<any>(null)

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

// 获取标签选项
const fetchTags = async (projectId?: string) => {
  console.log('获取标签列表，项目ID:', projectId)
  tagsLoading.value = true
  try {
    let response;
    if (projectId) {
      // 如果有项目 ID，获取特定项目的标签
      response = await getTagList({ projectId })
    } else {
      // 如果没有项目 ID，获取所有标签
      response = await getAllTags()
    }
    
    console.log('标签列表响应:', response)
    
    // 更完善的响应处理
    if (response?.data) {
      if (response.data.code === 1) {
        // 直接接收后端返回的标签数据
        const tagsData = response.data.data;
        
        // 所有接口现在都统一返回 { total, items } 格式
        if (tagsData && tagsData.items && Array.isArray(tagsData.items)) {
          // 处理标签数据格式，确保color字段存在
          tagOptions.value = tagsData.items.map((tag) => ({
            id: String(tag.id), // 确保id总是字符串类型
            name: tag.name,
            color: tag.color || '#409EFF', // 如果后端未返回颜色，使用默认颜色
            projectId: tag.projectId
          }));
          console.log('标签列表处理完成:', tagOptions.value);
        } else {
          console.warn('标签数据格式异常:', tagsData);
          tagOptions.value = [];
        }
      } else {
        console.warn('获取标签列表失败:', response.data.message || '未知错误');
        tagOptions.value = [];
      }
    } else {
      console.warn('获取标签列表响应异常:', response);
      tagOptions.value = [];
    }
  } catch (error) {
    console.error('获取标签列表失败:', error);
    tagOptions.value = [];
  } finally {
    tagsLoading.value = false;
  }
}

// 获取用户列表
const fetchUsers = async (keyword = '') => {
  membersLoading.value = true
  try {
    const params = {
      keyword: keyword,
      role: '0,1', // 只查询角色为0和1的用户
      page: 1,
      pageSize: 50
    }
    const response = await getAllUsers(params)
    
    if (response && response.data && response.data.code === 1 && response.data.data) {
      const { items } = response.data.data
      if (Array.isArray(items)) {
        memberOptions.value = items.map(user => ({
          value: user.username,
          label: `${user.username} (${user.role === 0 ? '成员' : '负责人'})`,
          avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }))
      } else {
        memberOptions.value = []
        console.warn('返回的items不是数组:', items)
      }
    } else {
      memberOptions.value = []
      console.warn('API响应格式不符合预期:', response)
    }
  } catch (error) {
    memberOptions.value = []
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    membersLoading.value = false
  }
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
    console.log('开始获取任务详情, taskId:', taskId)
    const response = await getTaskDetail(taskId)
    console.log('任务详情原始响应:', JSON.stringify(response, null, 2))
    
    // 将API返回的数据映射到表单
    let data = response.data?.data || response.data
    if (!data && (response as any).result) {
      data = (response as any).result // 兼容返回result的情况
    }
    
    console.log('解析后的任务数据:', JSON.stringify(data, null, 2))
    
    if (data) {
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
      
      console.log('转换前的优先级和状态:', { priority, status })
      
      // 处理日期时间 - 支持多种可能的日期字段名
      const createTime = data.createTime ? dayjs(data.createTime).format('YYYY-MM-DD HH:mm:ss') : ''
      
      // 截止时间可能有多种字段名
      const dueTimeValue = data.dueTime || data.deadline
      let dueTime: Date | null = null
      if (dueTimeValue) {
        try {
          dueTime = dayjs(dueTimeValue).toDate()
        } catch (e) {
          console.error('日期转换错误:', e)
        }
      }
      
      console.log('处理后的日期:', { createTime, dueTime })
      
      // 更新任务表单数据
      const formData = {
        number: data.id?.toString() || data.number || '',
        name: data.name || data.title || '',
        description: data.description || '',
        createTime,
        dueTime,
        priority: (priorityMap[priority] || 'MEDIUM') as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW',
        status: (statusMap[status] || 'PENDING') as 'PENDING' | 'IN_PROGRESS' | 'REVIEW' | 'COMPLETED',
        members: [],
        tags: []
      }
      
      // 处理成员数据 - 确保members是字符串数组
      if (Array.isArray(data.members)) {
        formData.members = data.members.map((member: any) => {
          return typeof member === 'string' ? member : member?.username || member
        }).filter(Boolean)
      }
      
      // 处理标签数据 - 确保tags是字符串数组
      if (Array.isArray(data.tags)) {
        formData.tags = data.tags.map((tag: any) => {
          return typeof tag === 'string' ? tag : tag?.name || tag
        }).filter(Boolean)
      }
      
      console.log('更新表单数据:', formData)
      
      // 使用Object.assign更新表单数据
      Object.assign(taskForm, formData)
      
      console.log('更新后的表单数据:', taskForm)
      
      // 加载评论和文件列表
      await fetchComments()
      await fetchAttachments(parseInt(taskId))
    } else {
      console.warn('未获取到任务数据:', response)
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
      deadline: deadline || undefined,  // 使用undefined替代null
      priority: priorityMap[taskForm.priority] || 2, // 默认为中等优先级
      status: statusMap[taskForm.status] || 1, // 默认为待处理
      members: taskForm.members,
      tags: taskForm.tags,
      projectId: route.query.projectId as string || undefined // 使用URL中的项目ID
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
  console.log('上传文件响应:', response)
  
  try {
    // 处理不同格式的后端响应
    const isSuccess = response.code === 1 || 
                    response.code === 0 || 
                    response.status === 200 || 
                    response.success === true;
    
    if (isSuccess) {
      ElMessage.success('上传成功')
      uploadDialogVisible.value = false
      
      // 刷新文件列表
      const taskId = parseInt(route.params.id as string)
      if (!isNaN(taskId)) {
        console.log('刷新文件列表, taskId:', taskId)
        await fetchAttachments(taskId)
      } else {
        console.error('无效的任务ID:', route.params.id)
      }
    } else {
      const errorMsg = response.msg || response.message || '上传失败'
      console.error('上传失败:', errorMsg)
      throw new Error(errorMsg)
    }
  } catch (err: any) {
    console.error('处理上传响应时出错:', err)
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
    const taskId = parseInt(route.params.id as string)
    const fileId = parseInt(file.id)
    
    console.log('准备删除文件:', { taskId, fileId, fileName: file.name })
    
    if(isNaN(taskId) || isNaN(fileId)) {
      throw new Error('任务ID或文件ID无效')
    }
    
    const response = await deleteAttachment(taskId, fileId)
    console.log('删除文件响应:', response)
    
    ElMessage.success('删除成功')
    
    // 刷新文件列表
    console.log('刷新文件列表, taskId:', taskId)
    await fetchAttachments(taskId)
  } catch (err: any) {
    if (err !== 'cancel') {
      console.error('删除文件失败:', err)
      ElMessage.error(err.message || '删除失败')
    }
  }
}

// 获取评论列表
const fetchComments = async () => {
  try {
    const response = await getTaskComments(parseInt(route.params.id as string))
    if (response.data && Array.isArray(response.data)) {
      comments.value = response.data
    } else {
      comments.value = []
    }
  } catch (err) {
    console.error('获取评论列表失败:', err)
    comments.value = []
  }
}

// 评论相关方法
const handleAddComment = async () => {
  if (!newComment.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentLoading.value = true
  try {
    // 构建评论数据
    const commentData = {
      content: newComment.content,
      parentId: newComment.parentId
    }
    
    // 添加评论
    await createComment(parseInt(route.params.id as string), commentData)
    
    ElMessage.success('评论成功')
    
    // 清空评论输入框和回复信息
    newComment.content = ''
    newComment.parentId = null
    replyToComment.value = null
    
    // 刷新评论列表
    await fetchComments()
  } catch (err: any) {
    ElMessage.error(err.message || '评论失败')
  } finally {
    commentLoading.value = false
  }
}

const handleReply = (comment: any) => {
  replyToComment.value = comment
  newComment.parentId = comment.id
  newComment.content = ''
  
  // 聚焦评论输入框
  setTimeout(() => {
    const textarea = document.querySelector('.comment-input .el-textarea__inner') as HTMLTextAreaElement
    if (textarea) {
      textarea.focus()
    }
  }, 100)
}

const cancelReply = () => {
  replyToComment.value = null
  newComment.parentId = null
}

const handleDeleteComment = async (comment: any) => {
  if (!isLeader.value && comment.creator.username !== userStore.userInfo?.username) {
    ElMessage.warning('您没有权限删除此评论')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除此评论吗？所有子评论也将被删除！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    // 删除评论
    await deleteComment(parseInt(route.params.id as string), comment.id)
    
    ElMessage.success('删除成功')
    
    // 刷新评论列表
    await fetchComments()
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
  // 初始化获取标签和成员
  await fetchTags()
  await fetchUsers()
  
  // 获取任务详情
  await fetchTaskDetail()
  await fetchComments()  // 单独获取评论列表
})

interface TaskAttachment {
  id: number;
  fileName: string;
  filePath: string;
  fileSize: number;
  createUser: string;
  createTime: string;
}

// 获取文件列表
const fetchAttachments = async (taskId: number) => {
  try {
    console.log('获取任务附件列表, taskId:', taskId)
    const response = await getTaskAttachments(taskId)
    console.log('附件列表原始响应:', JSON.stringify(response, null, 2))
    
    if (response?.data?.code === 1 && response.data.data) {
      const responseData = response.data.data
      const attachmentsData = Array.isArray(responseData) ? responseData : 
                             (responseData.items || []) as TaskAttachment[]
      
      console.log('解析后的附件数据:', JSON.stringify(attachmentsData, null, 2))
      
      // 检查每个文件的字段
      attachmentsData.forEach((file, index) => {
        console.log(`文件 ${index + 1} 的字段:`, {
          id: file.id,
          fileName: file.fileName,
          filePath: file.filePath,
          fileSize: file.fileSize,
          createUser: file.createUser,
          createTime: file.createTime
        })
      })
      
      fileList.value = attachmentsData.map(file => {
        const mappedFile = {
          id: file.id?.toString() || '',
          name: file.fileName || '', 
          url: file.filePath || '',   
          size: file.fileSize || 0,
          uploader: file.createUser?.toString() || '未知', 
          uploadTime: dayjs(file.createTime).format('YYYY-MM-DD HH:mm:ss') || ''
        }
        console.log('映射后的文件对象:', mappedFile)
        return mappedFile
      })
      
      console.log('最终的文件列表:', JSON.stringify(fileList.value, null, 2))
    } else {
      console.warn('附件数据格式异常或为空:', response)
      fileList.value = []
    }
  } catch (err) {
    console.error('获取文件列表失败:', err)
    fileList.value = []
  }
}
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

:deep(.comment-item) {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.comment-content) {
  flex: 1;
}

:deep(.comment-header) {
  margin-bottom: 8px;
}

:deep(.username) {
  font-weight: 500;
  margin-right: 8px;
}

:deep(.time) {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

:deep(.comment-text) {
  line-height: 1.6;
  margin-bottom: 8px;
}

:deep(.comment-actions) {
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

/* 子评论样式 */
:deep(.children-comments) {
  margin-left: 20px;
  margin-top: 15px;
  border-left: 2px solid var(--el-border-color-lighter);
  padding-left: 20px;
}

:deep(.child-comment) {
  margin-bottom: 12px;
}

/* 回复信息提示 */
.reply-info {
  margin-bottom: 10px;
  padding: 6px 10px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

/* 选项样式 */
.member-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
}

.tag-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 5px 0;
}

.tag-option .el-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 10px;
  margin-right: 5px;
  border-radius: 4px;
  max-width: calc(100% - 70px);
  min-width: 50px;
  flex: 0 1 auto;
}

.tag-option .el-tag .tag-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
  max-width: 150px;
}

:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 8px 12px;
}

:deep(.el-select .el-select__tags .el-tag) {
  margin: 2px 4px;
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
  transition: all 0.3s;
}
</style> 