<template>
  <div class="task-detail-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="handleBack">
        <el-icon><Back /></el-icon>
        Back
      </el-button>
    </div>

    <!-- 加载状态 -->
    <el-loading
      v-model:loading="loading"
      :lock="true"
      text="Loading..."
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
          <h2>Task Details</h2>
          <div class="header-actions">
            <el-button type="primary" plain @click="handleSave" v-if="isLeader" :loading="saveLoading">
              <el-icon><Check /></el-icon>
              Save
            </el-button>
            <el-button type="info" plain @click="refreshTaskTags">
              <el-icon><Refresh /></el-icon>
              Refresh
            </el-button>
            <el-button type="danger" plain @click="handleDelete" v-if="isLeader">
              <el-icon><Delete /></el-icon>
              Delete
            </el-button>
          </div>
        </div>
      </template>

      <el-form :model="taskForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8" :md="8" :lg="8">
            <el-form-item label="Task Number">
              <el-input v-model="taskForm.number" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="8" :md="8" :lg="8">
            <el-form-item label="Priority">
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
            <el-form-item label="Status">
              <template v-if="isLeader">
                <el-select v-model="taskForm.status" style="width: 100%">
                  <el-option label="Pending" value="PENDING" />
                  <el-option label="In Progress" value="IN_PROGRESS" />
                  <el-option label="Completed" value="COMPLETED" />
                  <el-option label="Cancelled" value="CANCELLED" />
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

        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="Task Name">
              <el-input 
                v-model="taskForm.name" 
                :disabled="!isLeader"
                placeholder="Please enter the task name"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="Project">
              <el-input 
                v-model="taskForm.projectName" 
                disabled
                placeholder="No project selected"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :xs="24" :sm="8" :md="8" :lg="8">
            <el-form-item label="Create Time">
              <el-input v-model="taskForm.createTime" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8" :md="8" :lg="8">
            <el-form-item label="Start Time">
              <el-date-picker
                v-model="taskForm.startTime as string | Date"
                type="datetime"
                :disabled="!isLeader"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8" :md="8" :lg="8">
            <el-form-item label="Due Time">
              <el-date-picker
                v-model="taskForm.dueTime as string | Date"
                type="datetime"
                :disabled="!isLeader"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        
        <el-form-item label="Task Members">
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
            >
              <div class="member-option">
                <el-avatar :size="24" :src="member.avatar">{{ member.label.charAt(0) }}</el-avatar>
                <span>{{ member.label }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="Task Tags">
          <el-select
            v-model="taskForm.tags"
            multiple
            :disabled="!isLeader"
            style="width: 100%"
            collapse-tags
            collapse-tags-tooltip
            @change="(val: any[]) => console.log('标签选择改变:', val)"
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

        <el-form-item label="Task Description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="4"
            :disabled="!isLeader"
            placeholder="Please enter the task description"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务文件卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>Task Files</h2>
          <div class="header-actions">
            <el-button type="primary" @click="handleUpload">
              <el-icon><Upload /></el-icon>
              Upload Files
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="fileList" style="width: 100%">
        <el-table-column prop="name" label="File Name" min-width="200" />
        <el-table-column prop="uploader" label="Uploader" width="120" />
        <el-table-column prop="uploadTime" label="Upload Time" width="160" />
        <el-table-column prop="size" label="Size" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="200" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-tooltip content="View File" placement="top">
                <el-button type="primary" link @click="handleViewFile(row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="Delete File" placement="top">
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
        title="Upload Files"
        width="500px"
      >
        <el-upload
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="fileList"
          multiple
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            Drag and drop files here, or <em>click to upload</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              Support any file format, each file is not more than 100MB
            </div>
          </template>
        </el-upload>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="uploadDialogVisible = false">Cancel</el-button>
            <el-button type="primary" @click="handleManualUpload" :loading="uploadLoading">
              Start Upload
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>

    <!-- 评论区卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>Comments ({{ comments.length > 0 ? getTotalCommentCount() : 0 }})</h2>
          <div class="header-actions">
            <el-button size="small" @click="fetchComments">
              <el-icon><Refresh /></el-icon>
              Refresh Comments
            </el-button>
          </div>
        </div>
      </template>

      <div class="comments-container">
        <!-- 评论列表 -->
        <div class="comments-list">
          <template v-if="comments.length === 0">
            <el-empty description="No comments yet" />
          </template>
          <template v-else>
            <comment-tree
              v-for="comment in comments"
              :key="comment.id"
              :comment="comment"
              :level="0"
              :reply-map="commentReplyMap"
              @reply="handleReply"
              @delete="handleDeleteComment"
            ></comment-tree>
          </template>
        </div>

        <!-- 评论输入框 -->
        <div class="comment-input">
          <div v-if="replyToComment" class="reply-info">
            Reply to: {{ replyToComment.createUserName }}
            <el-button type="text" @click="cancelReply">Cancel Reply</el-button>
          </div>
          <el-input
            v-model="newComment.content"
            type="textarea"
            :rows="3"
            :placeholder="replyToComment ? `Reply to ${replyToComment.createUserName}...` : 'Write your comment...'"
          />
          <div class="comment-buttons">
            <el-button type="primary" @click="handleAddComment" :loading="commentLoading">
              {{ replyToComment ? 'Reply' : 'Add Comment' }}
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
  Back,
  Refresh
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import type { TaskDetail, TaskFile, TaskComment } from '../../types/task'
import type { Comment } from '../../types/models'
import { getTaskDetail, updateTask, deleteTask, getTaskComments, createComment, deleteComment, getTaskAttachments, deleteAttachment } from '../../api/task'
import { getAllUsers } from '../../api/user'
import { getTagList, getAllTags } from '@/api/tag'
import { getAllProjects, getProjectDetail } from '@/api/project'
import axios from 'axios'
import CommentTree from '../../components/CommentTree.vue'

// 定义评论类型
interface CommentData extends TaskComment {
  id: number;
  taskId: number;
  content: string;
  parentId?: number | null;
  createTime: string;
  createUser: number | string;
  createUserName: string;
  createUserAvatar: string;
  children?: CommentData[];
}

// 定义接口和常量
interface UpdateData {
  id?: number;
  name: string;
  description: string;
  startTime?: string;
  deadline?: string;
  priority: number;
  status: number;
  members: string[];
  tagIds: number[];
  projectId?: string;
}

// 常量和映射
const PRIORITY_MAP = {
  CRITICAL: 4,
  HIGH: 3,
  MEDIUM: 2,
  LOW: 1
} as const;

const PRIORITY_MAP_REVERSE = {
  4: 'CRITICAL',
  3: 'HIGH',
  2: 'MEDIUM',
  1: 'LOW'
} as const;

const STATUS_MAP = {
  PENDING: 0,
  IN_PROGRESS: 1,
  COMPLETED: 2,
  CANCELLED: 3
} as const;

const STATUS_MAP_REVERSE = {
  0: 'PENDING',
  1: 'IN_PROGRESS',
  2: 'COMPLETED',
  3: 'CANCELLED'
} as const;

const STATUS_LABELS = {
  PENDING: 'Pending',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled'
} as const;

const PRIORITY_TYPES = {
  CRITICAL: 'danger',
  HIGH: 'warning',
  MEDIUM: 'info',
  LOW: 'success'
} as const;

const STATUS_TYPES = {
  PENDING: 'info',
  IN_PROGRESS: 'warning',
  REVIEW: 'info',
  COMPLETED: 'success'
} as const;

// 工具函数
const formatDate = (date: string | Date | null): string => {
  if (!date) return '';
  try {
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
  } catch (e) {
    return '';
  }
};

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const formatTimeFromNow = (time: string): string => {
  try {
    return dayjs(time).fromNow();
  } catch (e) {
    return time || '未知时间';
  }
};

// 安全地从API响应中提取数据
const extractDataFromResponse = (response: any, fallback: any = null) => {
  if (!response) return fallback;
  
  if (response.data?.code === 1 && response.data?.data) {
    return response.data.data;
  }
  
  if (response.data) {
    return response.data;
  }
  
  return fallback;
};

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

// 评论相关的响应式变量
const comments = ref<CommentData[]>([])
const newComment = reactive({
  content: '',
  parentId: null as number | null
})
const replyToComment = ref<CommentData | null>(null)
const commentReplyMap = ref<Record<number, string>>({})

// 判断是否为leader
const isLeader = computed(() => userStore.userInfo?.role === 1)

// 上传请求头
const uploadHeaders = computed(() => ({ 'X-Requested-With': 'XMLHttpRequest' }))

// 任务表单数据
const taskForm = reactive({
  number: '',
  name: '',
  createTime: '',
  startTime: '' as string | Date | null,
  dueTime: '' as string | Date | null,
  priority: 'MEDIUM' as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW',
  status: 'PENDING' as 'PENDING' | 'IN_PROGRESS' | 'REVIEW' | 'COMPLETED',
  members: [] as string[],
  tags: [] as Array<string | { id: string, name: string }>,
  description: '',
  projectId: undefined as number | undefined,
  projectName: ''
})

// 成员选项 - 从后端获取
const memberOptions = ref<{ label: string, value: string, avatar?: string }[]>([])

// 标签选项 - 从后端获取
const tagOptions = ref<{ id: string, name: string, color?: string, projectId?: string }[]>([])

// 文件列表 - 从后端获取
const fileList = ref<TaskFile[]>([])

// 优先级选项
const priorityOptions = [
  { label: 'Critical', value: 'CRITICAL' },
  { label: 'High', value: 'HIGH' },
  { label: 'Medium', value: 'MEDIUM' },
  { label: 'Low', value: 'LOW' }
]

// 获取优先级标签类型
const getPriorityType = (priority: string): 'success' | 'warning' | 'info' | 'danger' => {
  return PRIORITY_TYPES[priority as keyof typeof PRIORITY_TYPES] || 'info';
}

// 获取状态标签类型
const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'danger' => {
  return STATUS_TYPES[status as keyof typeof STATUS_TYPES] || 'info';
}

// 获取状态显示标签
const getStatusLabel = (status: string): string => {
  return STATUS_LABELS[status as keyof typeof STATUS_LABELS] || '未知状态';
}

// 使用token的工具函数
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Authorization': `Bearer ${token}`,
    'token': token
  };
};

// 获取标签选项
const fetchTags = async (projectId?: string) => {
  tagsLoading.value = true
  try {
    const response = projectId 
      ? await getTagList({ projectId }) 
      : await getAllTags();
    
    const tagsData = extractDataFromResponse(response);
    const items = Array.isArray(tagsData) 
      ? tagsData 
      : (tagsData?.items || []);
    
    tagOptions.value = items.map((tag: any) => ({
      id: String(tag.id),
      name: tag.name,
      color: tag.color || '#409EFF',
      projectId: tag.projectId
    }));
  } catch (error) {
    ElMessage.error('Get tag list failed');
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
      keyword,
      role: '0,1',
      page: 1,
      pageSize: 50
    }
    const response = await getAllUsers(params)
    const userData = extractDataFromResponse(response);
    
    if (userData?.items && Array.isArray(userData.items)) {
      memberOptions.value = userData.items.map((user: any) => ({
        value: user.username,
        label: `${user.username} (${user.role === 0 ? 'Member' : 'Leader'})`,
        avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
      }));
    } else {
      memberOptions.value = [];
    }
  } catch (error) {
    ElMessage.error('Get user list failed');
    memberOptions.value = [];
  } finally {
    membersLoading.value = false;
  }
}

// 获取任务详情
const fetchTaskDetail = async () => {
  const taskId = route.params.id as string
  if (!taskId) {
    error.value = 'Task ID cannot be empty';
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const response = await getTaskDetail(taskId);
    const data = extractDataFromResponse(response);
    
    if (!data) {
      ElMessage.warning('No task data obtained');
      return;
    }
    
    // 处理日期时间
    const createTime = formatDate(data.startTime || data.createTime);
    let startTime = null;
    let dueTime = null;
    try {
      startTime = data.startTime ? dayjs(data.startTime).toDate() : null;
      dueTime = data.dueTime || data.deadline ? dayjs(data.dueTime || data.deadline).toDate() : null;
    } catch (e) {
      startTime = null;
      dueTime = null;
    }
    
    // 确保数据类型正确
    const priority = typeof data.priority === 'number' ? data.priority : parseInt(data.priority) || 2;
    const status = typeof data.status === 'number' ? data.status : parseInt(data.status) || 1;
    
    // 获取项目ID
    const projectId = data.projectId ? Number(data.projectId) : undefined;
    
    // 如果有项目ID，获取项目详情
    let projectName = 'No project selected';
    if (projectId) {
      try {
        const projectResponse = await getProjectDetail(projectId);
        const projectData = extractDataFromResponse(projectResponse);
        if (projectData && projectData.name) {
          projectName = projectData.name;
        }
      } catch (err) {
        console.error('获取项目详情失败:', err);
        projectName = 'Project details acquisition failed';
      }
    }
    
    // 更新任务表单数据
    Object.assign(taskForm, {
      number: data.id?.toString() || data.number || '',
      name: data.name || data.title || '',
      description: data.description || '',
      createTime,
      startTime,
      dueTime,
      priority: PRIORITY_MAP_REVERSE[priority as keyof typeof PRIORITY_MAP_REVERSE] || 'MEDIUM',
      status: STATUS_MAP_REVERSE[status as keyof typeof STATUS_MAP_REVERSE] || 'PENDING',
      members: processMembers(data.members),
      tags: processTags(data),
      projectId,
      projectName
    });
    
    // 加载评论和文件列表
    await Promise.all([
      fetchComments(),
      fetchAttachments(parseInt(taskId))
    ]);
  } catch (err: any) {
    error.value = err.message || 'Get task details failed';
    ElMessage.error(error.value || 'Get task details failed');
  } finally {
    loading.value = false;
  }
}

// 处理成员数据
const processMembers = (members: any[] | undefined): string[] => {
  if (!members || !Array.isArray(members)) return [];
  
  return members
    .map(member => typeof member === 'string' ? member : member?.username || '')
    .filter(Boolean);
}

// 处理标签数据
const processTags = (data: any): string[] => {
  // 首先检查tagIds字段
  if (Array.isArray(data.tagIds)) {
    return data.tagIds
      .map((tagId: any) => tagId?.toString() || '')
      .filter(Boolean);
  }
  
  // 如果没有tagIds字段，再使用tags字段
  if (Array.isArray(data.tags)) {
    return data.tags
      .map((tag: any) => {
        if (typeof tag === 'object' && tag !== null) {
          return tag.id?.toString() || '';
        }
        return tag?.toString() || '';
      })
      .filter(Boolean);
  }
  
  return [];
}

// 添加一个函数，用于调试和刷新任务标签
const refreshTaskTags = async () => {
  try {
    // 先重新获取标签列表
    await fetchTags(route.query.projectId as string);
    
    // 然后重新获取任务详情
    await fetchTaskDetail();
    
    ElMessage.success('Tags refreshed');
  } catch (err: any) {
    ElMessage.error('Refresh tags failed');
  }
}

// 保存任务修改
const handleSave = async () => {
  if (!isLeader.value) {
    ElMessage.warning('You do not have permission to modify task information');
    return;
  }

  saveLoading.value = true;
  try {
    // 日期处理
    const startTime = taskForm.startTime ? formatDate(taskForm.startTime) : undefined;
    const deadline = taskForm.dueTime ? formatDate(taskForm.dueTime) : undefined;
    
    // 将标签ID转换为数字类型
    const tagIds = taskForm.tags
      .map(tag => {
        const tagId = typeof tag === 'object' && tag !== null 
          ? tag.id
          : tag;
        return tagId ? Number(tagId) : null;
      })
      .filter((id): id is number => id !== null);
    
    // 构建更新数据
    const updateData: UpdateData = {
      id: parseInt(taskForm.number) || undefined,
      name: taskForm.name,
      description: taskForm.description,
      startTime,
      deadline,
      priority: PRIORITY_MAP[taskForm.priority] || 2,
      status: STATUS_MAP[taskForm.status as keyof typeof STATUS_MAP] || 1,
      members: taskForm.members,
      tagIds
    };
    
    // 更新任务
    await updateTask(route.params.id as string, updateData);
    ElMessage.success('Save successfully');
    
    // 刷新数据
    setTimeout(async () => {
      await fetchTags(route.query.projectId as string);
      await fetchTaskDetail();
    }, 500);
  } catch (err: any) {
    // 精简错误处理
    if (err.response) {
      ElMessage.error(`Save failed: ${err.response.status} - ${err.response.data?.message || 'Server error'}`);
    } else if (err.request) {
      ElMessage.error('Save failed: Server not responding');
    } else {
      ElMessage.error(err.message || 'Save failed');
    }
  } finally {
    saveLoading.value = false;
  }
}

// 文件上传相关方法
const handleUpload = () => {
  uploadDialogVisible.value = true;
};

const uploadFiles = ref<File[]>([]);

const handleFileChange = (file: any) => {
  uploadFiles.value.push(file.raw);
};

const handleManualUpload = async () => {
  if (uploadFiles.value.length === 0) {
    ElMessage.warning('Please select the file to upload');
    return;
  }

  uploadLoading.value = true;
  try {
    const formData = new FormData();
    uploadFiles.value.forEach(file => {
      formData.append('file', file);
    });

    const response = await axios.post(
      `/api/tasks/${route.params.id}/attachments`,
      formData,
      { headers: { ...getAuthHeaders(), 'Content-Type': 'multipart/form-data' } }
    );

    if (response.data.code === 1) {
      ElMessage.success('Upload successfully');
      uploadDialogVisible.value = false;
      uploadFiles.value = [];
      // 刷新文件列表
      await fetchAttachments(parseInt(route.params.id as string));
    } else {
      ElMessage.error(response.data.message || 'Upload failed');
    }
  } catch (error: any) {
    if (error.response?.status === 401) {
      ElMessage.error('Authentication failed, please log in again');
      userStore.logout();
      router.push('/login');
      return;
    }
    
    if (error.response?.data?.message?.includes('UserDisable')) {
      ElMessage.error('File storage service is temporarily unavailable, please contact the administrator');
    } else if (error.response?.status === 500) {
      ElMessage.error('Server internal error, please try again later');
    } else {
      ElMessage.error(error.message || 'Upload failed, please try again later');
    }
  } finally {
    uploadLoading.value = false;
  }
};

// 文件操作方法
const handleViewFile = (file: TaskFile) => {
  if (!file.url) {
    ElMessage.warning('File link is not available');
    return;
  }
  // 使用预览API打开文件
  window.open(`/api/files/preview?fileUrl=${encodeURIComponent(file.url)}`);
};

const handleDownloadFile = (file: TaskFile) => {
  if (!file.url) {
    ElMessage.warning('File link is not available');
    return;
  }
  
  try {
    // 使用下载API获取解密后的文件
    window.location.href = `/api/files/download?fileUrl=${encodeURIComponent(file.url)}`;
  } catch (err) {
    ElMessage.error('Download failed');
  }
};

const handleDeleteFile = async (file: TaskFile) => {
  if (!isLeader.value && file.uploader !== userStore.userInfo?.username) {
    ElMessage.warning('You do not have permission to delete this file');
    return;
  }

  try {
    await ElMessageBox.confirm('Confirm to delete this file?', 'Warning', {
      type: 'warning',
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel'
    });

    // 删除文件
    const taskId = parseInt(route.params.id as string);
    const fileId = parseInt(file.id);
    
    if(isNaN(taskId) || isNaN(fileId)) {
      throw new Error('Invalid task ID or file ID');
    }
    
    await deleteAttachment(taskId, fileId);
    ElMessage.success('Delete successfully');
    
    // 刷新文件列表
    await fetchAttachments(taskId);
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || 'Delete failed');
    }
  }
};

// 获取评论列表
const fetchComments = async () => {
  try {
    const taskId = parseInt(route.params.id as string);
    const response = await getTaskComments(taskId);
    let commentsData = extractDataFromResponse(response, []);
    
    // 确保commentsData是数组
    if (!Array.isArray(commentsData)) {
      commentsData = commentsData?.items || commentsData?.data || [];
    }
    
    // 打印原始数据便于调试
    console.log('原始评论数据:', commentsData);
    if (commentsData.length > 0) {
      console.log('第一条评论示例:', JSON.stringify(commentsData[0]));
    }
    
    // 默认头像URL
    const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
    
    // 构建评论树
    const idToComment = new Map<number, CommentData>();
    const rootComments: CommentData[] = [];
    
    // 第一步：处理每条评论
    commentsData.forEach((c: any) => {
      const commentId = Number(c.id);
      const parentId = c.parentId !== null ? Number(c.parentId) : null;
      
      // 处理嵌套的creator对象
      let userName = '';
      let userAvatar = '';
      let userId = 0;
      
      if (c.creator) {
        // 新的JSON格式使用creator对象
        userName = c.creator.username || '未知用户';
        userAvatar = c.creator.avatar || defaultAvatar;
        userId = c.creator.id || 0;
      } else {
        // 旧的格式可能使用不同的字段名
        userName = c.createUserName || c.create_user_name || 
                  c.userName || c.user_name || 
                  c.username || `用户${c.createUser || c.create_user || '未知'}`;
                  
        userAvatar = c.createUserAvatar || c.create_user_avatar || 
                    c.userAvatar || c.user_avatar || 
                    c.avatar || defaultAvatar;
                    
        userId = c.createUser || c.create_user || 0;
      }
      
      // 创建标准化的评论对象
      const comment: CommentData = {
        id: commentId,
        taskId: Number(c.taskId || c.task_id || taskId),
        content: c.content || '',
        parentId: parentId,
        createTime: c.createTime || c.create_time || new Date().toISOString(),
        createUser: userId,
        createUserName: userName,
        createUserAvatar: userAvatar,
        children: []
      };
      
      // 保存评论映射
      idToComment.set(commentId, comment);
      // 记录用户名，用于显示回复关系
      commentReplyMap.value[commentId] = comment.createUserName;
    });
    
    // 第二步：构建树结构
    idToComment.forEach(comment => {
      if (comment.parentId === null || !idToComment.has(Number(comment.parentId!))) {
        // 如果是顶级评论或找不到父评论，则作为根评论
        rootComments.push(comment);
      } else {
        // 否则添加到父评论的子评论列表
        const parent = idToComment.get(Number(comment.parentId!));
        if (parent && parent.children) {
          parent.children.push(comment);
        }
      }
    });
    
    // 第三步：按时间排序（新评论在前）
    const sortByTime = (items: CommentData[]): CommentData[] => {
      items.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime());
      items.forEach(item => {
        if (item.children && item.children.length > 0) {
          sortByTime(item.children);
        }
      });
      return items;
    };
    
    comments.value = sortByTime(rootComments);
  } catch (err) {
    console.error('获取评论失败:', err);
    ElMessage.error('Get comment list failed');
    comments.value = [];
  }
};

// 修改handleAddComment函数以支持多层级评论
const handleAddComment = async () => {
  if (!newComment.content.trim()) {
    ElMessage.warning('Please enter a comment');
    return;
  }

  commentLoading.value = true;
  try {
    const taskId = parseInt(route.params.id as string);
    const commentData = {
      content: newComment.content,
      parentId: newComment.parentId ? Number(newComment.parentId) : null,
      taskId: taskId
    };
    
    const response = await createComment(taskId, commentData);
    const createdComment = extractDataFromResponse(response);
    
    if (createdComment) {
      // 评论成功后，直接重新获取所有评论以确保数据一致性
      await fetchComments();
      ElMessage.success('Comment successfully');
    } else {
      ElMessage.warning('Comment submitted, but unable to get comment details');
      await fetchComments();
    }
    
    // 清空评论输入框和回复信息
    newComment.content = '';
    newComment.parentId = null;
    replyToComment.value = null;
  } catch (err: any) {
    ElMessage.error(err.message || 'Comment failed');
    await fetchComments();
  } finally {
    commentLoading.value = false;
  }
};

const handleReply = (comment: CommentData) => {
  replyToComment.value = comment;
  newComment.parentId = comment.id;
  newComment.content = '';
  
  // 聚焦评论输入框
  setTimeout(() => {
    const textarea = document.querySelector('.comment-input .el-textarea__inner') as HTMLTextAreaElement;
    if (textarea) {
      textarea.focus();
    }
  }, 100);
};

const cancelReply = () => {
  replyToComment.value = null;
  newComment.parentId = null;
};

const handleDeleteComment = async (comment: CommentData) => {
  if (!isLeader.value && String(comment.createUser) !== String(userStore.userInfo?.id)) {
    ElMessage.warning('You do not have permission to delete this comment');
    return;
  }

  try {
    await ElMessageBox.confirm('Confirm to delete this comment? All sub-comments will also be deleted!', 'Warning', {
      type: 'warning',
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel'
    });

    // 删除评论
    const taskId = parseInt(route.params.id as string);
    const commentId = comment.id;
    
    await deleteComment(taskId, commentId);
    ElMessage.success('Delete successfully');
    
    // 刷新评论列表
    await fetchComments();
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || 'Delete failed');
    }
  }
};

// 处理删除任务
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to delete this task? This action cannot be undone!',
      'Delete Confirmation',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    );
    
    loading.value = true;
    await deleteTask(route.params.id as string);
    ElMessage.success('删除成功');
    router.push('/list');
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败');
    }
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const handleBack = () => {
  router.back();
};

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
  await Promise.all([
    fetchTags(),
    fetchUsers()
  ])
  
  // 获取任务详情
  await fetchTaskDetail()
  await fetchComments()
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
    const response = await getTaskAttachments(taskId);
    const attachmentsData = extractDataFromResponse(response);
    
    // 标准化附件数据
    let items: any[] = [];
    if (Array.isArray(attachmentsData)) {
      items = attachmentsData;
    } else if (attachmentsData?.items && Array.isArray(attachmentsData.items)) {
      items = attachmentsData.items;
    }
    
    fileList.value = items.map(file => ({
      id: file.id?.toString() || '',
      name: file.fileName || file.filename || file.name || '未命名文件', 
      url: file.filePath || file.url || '',   
      size: file.fileSize || file.size || 0,
      uploader: typeof file.createUser === 'object' 
        ? file.createUser.username || '未知用户' 
        : file.createUserName || file.uploaderName || file.createUser?.toString() || '未知用户', 
      uploadTime: formatDate(file.createTime || file.uploadTime || file.updateTime)
    }));
  } catch (err) {
    ElMessage.error('Get file list failed');
    fileList.value = [];
  }
}

// 格式化时间
const formatTime = (time: string) => formatTimeFromNow(time);

// 获取评论总数（包括所有子评论）
const getTotalCommentCount = () => {
  let total = 0;
  
  // 递归计算评论数量
  const countComments = (commentsList: CommentData[]) => {
    if (!commentsList) return 0;
    
    let count = 0;
    for (const comment of commentsList) {
      count++; // 计算当前评论
      if (comment.children && comment.children.length > 0) {
        count += countComments(comment.children); // 递归计算子评论
      }
    }
    return count;
  };
  
  return countComments(comments.value);
};
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
  padding: 10px 0;
}

.comment-wrapper {
  margin-bottom: 20px;
}

.comments-list {
  margin-bottom: 20px;
}

.comment-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background-color: #f9f9f9;
  margin-bottom: 15px;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.username {
  font-weight: 500;
  margin-left: 10px;
  color: #333;
}

.time {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}

.comment-content {
  margin: 10px 0;
  line-height: 1.5;
  color: #303133;
  word-break: break-all;
}

.comment-actions {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  margin-top: 10px;
}

.comment-input {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.reply-info {
  background-color: #f8f8f8;
  padding: 8px 15px;
  border-radius: 4px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-buttons {
  margin-top: 10px;
  text-align: right;
}

/* 子评论样式 */
.comment-replies {
  margin-left: 20px;
  margin-top: 15px;
  padding-left: 15px;
  border-left: 2px solid #dcdfe6;
}

.child-comment {
  background-color: #f0f2f5;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
}

.reply-target {
  color: #409EFF;
  font-weight: 500;
  margin-right: 5px;
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