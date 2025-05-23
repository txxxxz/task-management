<template>
  <div class="project-detail-container">
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

    <!-- 项目基本信息卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>Project Details</h2>
          <div class="header-actions">
            <el-button type="primary" plain @click="handleSave" v-if="isLeader" :loading="saveLoading">
              <el-icon><Check /></el-icon>
              Save Changes
            </el-button>
            <el-button type="danger" plain @click="handleDelete" v-if="isLeader">
              <el-icon><Delete /></el-icon>
              Delete Project
            </el-button>
          </div>
        </div>
      </template>

      <el-form :model="projectForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8" :md="8" :lg="8">
            <el-form-item label="Project Number">
              <el-input v-model="projectForm.id" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="8" :md="8" :lg="8">
            <el-form-item label="Priority">
              <template v-if="isLeader">
                <el-select v-model="projectForm.priority" style="width: 100%">
                  <el-option
                    v-for="item in priorityOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </template>
              <template v-else>
                <el-tag :type="getPriorityType(projectForm.priority)" effect="light">
                  {{ projectForm.priority }}
                </el-tag>
              </template>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="8" :md="8" :lg="8">
            <el-form-item label="Status">
              <template v-if="isLeader">
                <el-select v-model="projectForm.status" style="width: 100%">
                  <el-option label="In preparation" value="IN_PREPARATION" />
                  <el-option label="In progress" value="IN_PROGRESS" />
                  <el-option label="Completed" value="COMPLETED" />
                  <el-option label="Archived" value="ARCHIVED" />
                </el-select>
              </template>
              <template v-else>
                <el-tag :type="getStatusType(projectForm.status)" effect="light">
                  {{ getStatusLabel(projectForm.status) }}
                </el-tag>
              </template>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Project Name">
          <el-input 
            v-model="projectForm.name" 
            :disabled="!isLeader"
            placeholder="Please enter the project name"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="8">
            <el-form-item label="Create Time">
              <el-input v-model="projectForm.createTime" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8">
            <el-form-item label="Start Time">
              <el-date-picker
                v-model="projectForm.startTime"
                type="datetime"
                :disabled="!isLeader"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8">
            <el-form-item label="End Time">
              <el-date-picker
                v-model="projectForm.endTime"
                type="datetime"
                :disabled="!isLeader"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Project Leader">
          <el-input 
            v-model="projectForm.creatorName" 
            disabled
            placeholder="Project Leader"
          />
        </el-form-item>
        
        <el-form-item label="Project Members">
          <template v-if="isLeader">
            <el-select
              v-model="projectForm.members"
              multiple
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
          </template>
          <template v-else>
            <div class="members-display">
              <template v-if="projectForm.members.length">
                <div class="member-item" v-for="memberName in projectForm.members" :key="memberName">
                  <el-avatar :size="24">{{ memberName.charAt(0) }}</el-avatar>
                  <span class="member-name">{{ memberName }}</span>
                </div>
              </template>
              <span v-else class="no-members">暂无成员</span>
            </div>
          </template>
        </el-form-item>

        <el-form-item label="Project Description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="4"
            :disabled="!isLeader"
            placeholder="Please enter the project description"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 项目文件卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>Project Files</h2>
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
              <el-tooltip content="Download File" placement="top">
                <el-button type="primary" link @click="handleDownloadFile(row)">
                  <el-icon><Download /></el-icon>
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
          :file-list="uploadFiles"
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
            <el-button @click="uploadDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleManualUpload" :loading="uploadLoading">
              Start Upload
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>

    <!-- 任务列表卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>Task List</h2>
          <div class="header-actions">
            <el-button type="primary" @click="handleAddTask">
              <el-icon><Plus /></el-icon>
              Add Task
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="taskList" style="width: 100%">
        <el-table-column type="index" label="Number" width="70" align="center" />
        <el-table-column prop="name" label="Task Name" min-width="180" />
        <el-table-column label="Task Status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusType(row.status)">
              {{ getTaskStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Priority" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskPriorityType(row.priority)">
              {{ getTaskPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="Start Time" width="160" />
        <el-table-column prop="deadline" label="End Time" width="160" />
        <el-table-column label="Actions" width="160" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-tooltip content="View Task" placement="top">
                <el-button type="primary" link @click="handleViewTask(row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="Delete Task" placement="top">
                <el-button 
                  type="danger" 
                  link 
                  @click="handleDeleteTask(row)"
                  v-if="isLeader || row.creator?.username === userStore.userInfo?.username"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 任务为空时显示 -->
      <el-empty 
        v-if="taskList.length === 0" 
        description="No tasks yet" 
        :image-size="100"
      >
        <el-button type="primary" @click="handleAddTask">Add Task</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check, Upload, View, Download, Delete, UploadFilled, Back,
  Plus
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import axios from 'axios'
// 定义本地ProjectFile接口，以解决类型问题
interface ProjectFile {
  id: string;
  name: string;
  url: string;
  size: number;
  uploader: string;
  uploadTime: string;
}

// 定义任务接口
interface Task {
  id: number;
  name: string;
  description: string;
  status: number;
  priority: number;
  startTime: string;
  deadline: string;
  projectId: number;
  creator?: {
    id: number;
    username: string;
  };
}

import { getProjectDetail, updateProject, deleteProject, getProjectAttachments, deleteAttachment } from '../../api/project'
import { getAllUsers } from '../../api/user'
import { getProjectTasks, deleteTask } from '../../api/task'

// 常量和映射
const PRIORITY_MAP = {
  CRITICAL: 4,
  HIGH: 3,
  MEDIUM: 2,
  LOW: 1
};

const PRIORITY_MAP_REVERSE = {
  4: 'CRITICAL',
  3: 'HIGH',
  2: 'MEDIUM',
  1: 'LOW'
};

const STATUS_MAP = {
  IN_PREPARATION: 0,
  IN_PROGRESS: 1,
  COMPLETED: 2,
  ARCHIVED: 3
};

const STATUS_MAP_REVERSE = {
  0: 'IN_PREPARATION',
  1: 'IN_PROGRESS',
  2: 'COMPLETED',
  3: 'ARCHIVED'
};

const STATUS_LABELS = {
  IN_PREPARATION: 'In preparation',
  IN_PROGRESS: 'In progress',
  COMPLETED: 'Completed',
  ARCHIVED: 'Archived'
};

const PRIORITY_TYPES: Record<string, 'danger' | 'warning' | 'info' | 'success'> = {
  CRITICAL: 'danger',
  HIGH: 'warning',
  MEDIUM: 'info',
  LOW: 'success'
};

const STATUS_TYPES: Record<string, 'danger' | 'warning' | 'info' | 'success'> = {
  PENDING: 'info',
  IN_PROGRESS: 'warning',
  REVIEW: 'info',
  COMPLETED: 'success'
};

// 工具函数
const formatDate = (date: string | Date | null): string => {
  if (!date) return '';
  try {
    // 使用LocalDateTime格式(YYYY-MM-DD HH:mm:ss)
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

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const uploadDialogVisible = ref(false)

// 加载状态
const loading = ref(false)
const uploadLoading = ref(false)
const saveLoading = ref(false)
const membersLoading = ref(false)

// 错误状态
const error = ref<string | null>(null)

// 判断是否为leader
const isLeader = computed(() => userStore.userInfo?.role === 1)

// 项目表单数据
const projectForm = reactive({
  id: '',
  name: '',
  startTime: '',
  endTime: '',
  createTime: '' as string,
  priority: 'MEDIUM' as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW',
  status: 'IN_PREPARATION' as 'IN_PREPARATION' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED',
  creatorName: '',
  members: [] as string[],
  description: ''
})

// 成员选项 - 从后端获取
const memberOptions = ref<{ label: string, value: string, avatar?: string, role: number }[]>([])

// 文件列表 - 从后端获取
const fileList = ref<ProjectFile[]>([])
const uploadFiles = ref<File[]>([])

// 优先级选项
const priorityOptions = [
  { label: 'Critical', value: 'CRITICAL' },
  { label: 'High', value: 'HIGH' },
  { label: 'Medium', value: 'MEDIUM' },
  { label: 'Low', value: 'LOW' }
]

// 任务列表 - 从后端获取
const taskList = ref<Task[]>([])
const taskLoading = ref(false)

// 获取优先级对应的类型
const getPriorityType = (priority: string): 'info' | 'warning' | 'success' | 'danger' => {
  if (!priority) return 'info';
  return PRIORITY_TYPES[priority] || 'info';
};

// 获取状态对应的类型
const getStatusType = (status: string): 'info' | 'warning' | 'success' | 'danger' => {
  if (!status) return 'info';
  // 使用默认状态类型映射（与TASK_STATUS_TYPES一致）
  const statusMap: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
    IN_PREPARATION: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    ARCHIVED: 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取状态显示标签
const getStatusLabel = (status: string): string => {
  if (!status) return '未知状态';
  return STATUS_LABELS[status as keyof typeof STATUS_LABELS] || '未知状态';
};

// 使用token的工具函数
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Authorization': `Bearer ${token}`,
    'token': token
  };
};

// 获取用户列表
const fetchUsers = async () => {
  membersLoading.value = true
  try {
    const params = {
      keyword: '',
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
        avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        role: user.role
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

// 获取项目详情
const fetchProjectDetail = async () => {
  const projectId = route.params.id as string
  if (!projectId) {
    error.value = '项目ID不能为空';
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const response = await getProjectDetail(parseInt(projectId));
    const data = extractDataFromResponse(response);
    
    if (!data) {
      ElMessage.warning('No project data obtained');
      return;
    }
    
    // 处理日期
    const startTime = data.startTime || null;
    const endTime = data.endTime || null;
    const createTime = data.createTime || '';
    
    // 确保数据类型正确
    const priority = typeof data.priority === 'number' ? data.priority : parseInt(data.priority) || 2;
    const status = typeof data.status === 'number' ? data.status : parseInt(data.status) || 1;
    
    // 更新项目表单数据
    Object.assign(projectForm, {
      id: data.id?.toString() || '',
      name: data.name || '',
      description: data.description || '',
      startTime,
      endTime,
      createTime: formatDate(createTime),
      priority: PRIORITY_MAP_REVERSE[priority as keyof typeof PRIORITY_MAP_REVERSE] || 'MEDIUM',
      status: STATUS_MAP_REVERSE[status as keyof typeof STATUS_MAP_REVERSE] || 'IN_PREPARATION',
      creatorName: data.creator?.username || '',
      members: Array.isArray(data.members) ? data.members.map((m: any) => 
        typeof m === 'string' ? m : m?.username || ''
      ).filter(Boolean) : []
    });
    
    console.log('Received project data:', data); // 添加日志
    console.log('Processed project form:', projectForm); // 添加日志
    
    // 加载文件列表
    await fetchAttachments(parseInt(projectId));
    
    // 加载任务列表
    await fetchTasks(projectId);
  } catch (err: any) {
    error.value = err.message || 'Get project details failed';
    ElMessage.error(error.value || 'Get project details failed');
  } finally {
    loading.value = false;
  }
}

// 保存项目修改
const handleSave = async () => {
  if (!isLeader.value) {
    ElMessage.warning('You do not have permission to modify project information');
    return;
  }

  saveLoading.value = true;
  try {
    // 构建更新数据，确保与ProjectForm接口兼容
    const updateData: any = {
      id: parseInt(projectForm.id) || undefined,
      name: projectForm.name,
      description: projectForm.description,
      startTime: projectForm.startTime || undefined,
      endTime: projectForm.endTime || undefined,
      priority: PRIORITY_MAP[projectForm.priority] || 2,
      status: STATUS_MAP[projectForm.status],
      members: projectForm.members
    };

    console.log('Sending update data:', updateData); // 添加日志以便调试
    
    // 更新项目
    await updateProject(route.params.id as string, updateData);
    ElMessage.success('Save successfully');
    
    // 刷新数据
    setTimeout(() => {
      fetchProjectDetail();
    }, 500);
  } catch (err: any) {
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
  uploadFiles.value = [];
};

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
      `/api/projects/${route.params.id}/attachments`,
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
    
    ElMessage.error(error.message || 'Upload failed, please try again later');
  } finally {
    uploadLoading.value = false;
  }
};

// 文件操作方法
const handleViewFile = (file: ProjectFile) => {
  if (!file.url) {
    ElMessage.warning('The file link is not available');
    return;
  }
  // 使用预览API打开文件
  window.open(`/api/files/preview?fileUrl=${encodeURIComponent(file.url)}`);
};

const handleDownloadFile = (file: ProjectFile) => {
  if (!file.url) {
    ElMessage.warning('The file link is not available');
    return;
  }
  
  try {
    // 使用下载API获取解密后的文件
    window.location.href = `/api/files/download?fileUrl=${encodeURIComponent(file.url)}`;
  } catch (err) {
    ElMessage.error('Download failed');
  }
};

const handleDeleteFile = async (file: ProjectFile) => {
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
    const projectId = parseInt(route.params.id as string);
    const fileId = parseInt(file.id);
    
    if(isNaN(projectId) || isNaN(fileId)) {
      throw new Error('Invalid project ID or file ID');
    }
    
    await deleteAttachment(projectId, fileId);
    ElMessage.success('Delete successfully');
    
    // 刷新文件列表
    await fetchAttachments(projectId);
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || 'Delete failed');
    }
  }
};

// 处理删除项目
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      'Confirm to delete this project? This action cannot be undone!',
      'Warning',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    );
    
    loading.value = true;
    await deleteProject(route.params.id as string);
    ElMessage.success('Delete successfully');
    router.push('/project/list');
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || 'Delete failed');
    }
  } finally {
    loading.value = false;
  }
};

// 获取文件列表
const fetchAttachments = async (projectId: number) => {
  try {
    const response = await getProjectAttachments(projectId);
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

// 返回上一页
const handleBack = () => {
  router.push('/project/list');
};

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      fetchProjectDetail()
    }
  },
  { immediate: true }
)

// 在组件挂载时获取数据
onMounted(async () => {
  // 初始化获取成员
  await fetchUsers()
  
  // 获取项目详情
  await fetchProjectDetail()
})

// 任务状态和优先级映射
const TASK_STATUS_LABELS: Record<number, string> = {
  0: 'Pending',
  1: 'In Progress',
  2: 'Completed',
  3: 'Cancelled'
}

const TASK_STATUS_TYPES: Record<number, 'info' | 'warning' | 'success' | 'danger'> = {
  0: 'info',
  1: 'warning',
  2: 'success',
  3: 'danger'
}

const TASK_PRIORITY_LABELS: Record<number, string> = {
  1: 'Low',
  2: 'Medium',
  3: 'High',
  4: 'Critical'
}

const TASK_PRIORITY_TYPES: Record<number, 'info' | 'warning' | 'success' | 'danger'> = {
  1: 'success',
  2: 'info',
  3: 'warning',
  4: 'danger'
}

// 获取任务状态标签
const getTaskStatusLabel = (status: number): string => {
  return TASK_STATUS_LABELS[status] || '未知状态';
}

// 获取任务状态类型
const getTaskStatusType = (status: number): 'info' | 'warning' | 'success' | 'danger' => {
  return TASK_STATUS_TYPES[status] || 'info';
}

// 获取任务优先级标签
const getTaskPriorityLabel = (priority: number): string => {
  return TASK_PRIORITY_LABELS[priority] || '未知优先级';
}

// 获取任务优先级类型
const getTaskPriorityType = (priority: number): 'info' | 'warning' | 'success' | 'danger' => {
  return TASK_PRIORITY_TYPES[priority] || 'info';
}

// 获取项目的任务列表
const fetchTasks = async (projectId: string) => {
  taskLoading.value = true;
  try {
    const response = await getProjectTasks(projectId);
    const data = extractDataFromResponse(response);
    
    if (data?.items && Array.isArray(data.items)) {
      taskList.value = data.items.map((task: any) => ({
        id: task.id,
        name: task.name,
        description: task.description || '',
        status: task.status,
        priority: task.priority,
        startTime: formatDate(task.startTime),
        deadline: formatDate(task.deadline),
        projectId: task.projectId,
        creator: task.creator
      }));
    } else if (data && Array.isArray(data)) {
      taskList.value = data.map((task: any) => ({
        id: task.id,
        name: task.name,
        description: task.description || '',
        status: task.status,
        priority: task.priority,
        startTime: formatDate(task.startTime),
        deadline: formatDate(task.deadline),
        projectId: task.projectId,
        creator: task.creator
      }));
    } else {
      taskList.value = [];
    }
  } catch (err) {
    ElMessage.error('Get task list failed');
    taskList.value = [];
  } finally {
    taskLoading.value = false;
  }
};

// 查看任务详情
const handleViewTask = (task: Task) => {
  router.push(`/task/detail/${task.id}`);
};

// 删除任务
const handleDeleteTask = async (task: Task) => {
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
    
    await deleteTask(task.id.toString());
    ElMessage.success('Delete successfully');
    
    // 刷新任务列表
    await fetchTasks(route.params.id as string);
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || 'Delete failed');
    }
  }
};

// 新增任务
const handleAddTask = () => {
  router.push(`/task/create?projectId=${route.params.id}`);
};
</script>

<style scoped>
.project-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  white-space: nowrap;
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
  padding-left: 50px;
  padding-right: 50px;
  padding-top: 30px;
  padding-bottom: 30px;

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
  .project-detail-container {
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

/* 成员选项样式 */
.member-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
}

.members-display {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.member-name {
  font-size: 14px;
  color: #606266;
}

.no-members {
  color: #909399;
  font-size: 14px;
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

.back-button {
  margin-bottom: 16px;
}

.error-alert {
  margin-bottom: 16px;
}

/* 任务列表空状态样式 */
:deep(.el-empty) {
  padding: 30px 0;
}

/* 任务状态标签样式 */
:deep(.el-tag) {
  padding: 2px 8px;
  font-weight: 500;
}
</style>
