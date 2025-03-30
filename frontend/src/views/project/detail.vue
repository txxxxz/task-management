<template>
  <div class="project-detail-container">
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

    <!-- 项目基本信息卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>项目详情</h2>
          <div class="header-actions">
            <el-button type="primary" plain @click="handleSave" v-if="isLeader" :loading="saveLoading">
              <el-icon><Check /></el-icon>
              保存修改
            </el-button>
            <el-button type="danger" plain @click="handleDelete" v-if="isLeader">
              <el-icon><Delete /></el-icon>
              删除项目
            </el-button>
          </div>
        </div>
      </template>

      <el-form :model="projectForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8" :md="8" :lg="8">
            <el-form-item label="项目编号">
              <el-input v-model="projectForm.id" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="8" :md="8" :lg="8">
            <el-form-item label="优先级">
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
            <el-form-item label="状态">
              <template v-if="isLeader">
                <el-select v-model="projectForm.status" style="width: 100%">
                  <el-option label="筹备中" value="IN_PREPARATION" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已归档" value="ARCHIVED" />
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

        <el-form-item label="项目名称">
          <el-input 
            v-model="projectForm.name" 
            :disabled="!isLeader"
            placeholder="请输入项目名称"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="8">
            <el-form-item label="创建时间">
              <el-input v-model="projectForm.createTime" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="projectForm.startTime"
                type="datetime"
                :disabled="!isLeader"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8">
            <el-form-item label="截止日期">
              <el-date-picker
                v-model="projectForm.endTime"
                type="datetime"
                :disabled="!isLeader"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="项目负责人">
          <el-input 
            v-model="projectForm.creatorName" 
            disabled
            placeholder="项目负责人"
          />
        </el-form-item>
        
        <el-form-item label="项目成员">
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

        <el-form-item label="项目详情">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="4"
            :disabled="!isLeader"
            placeholder="请输入项目详情"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 项目文件卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>项目文件</h2>
          <div class="header-actions">
            <el-button type="primary" @click="handleUpload">
              <el-icon><Upload /></el-icon>
              上传文件
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
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="uploadFiles"
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
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="uploadDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleManualUpload" :loading="uploadLoading">
              开始上传
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>

    <!-- 任务列表卡片 -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>任务列表</h2>
          <div class="header-actions">
            <el-button type="primary" @click="handleAddTask">
              <el-icon><Plus /></el-icon>
              新增任务
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="taskList" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="任务名称" min-width="180" />
        <el-table-column label="任务状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusType(row.status)">
              {{ getTaskStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskPriorityType(row.priority)">
              {{ getTaskPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="deadline" label="截止时间" width="160" />
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-tooltip content="查看任务" placement="top">
                <el-button type="primary" link @click="handleViewTask(row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除任务" placement="top">
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
        description="暂无任务" 
        :image-size="100"
      >
        <el-button type="primary" @click="handleAddTask">添加任务</el-button>
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
  PENDING: 1,
  IN_PROGRESS: 2,
  REVIEW: 3,
  COMPLETED: 4
};

const STATUS_MAP_REVERSE = {
  0: '筹备中',
  1: '进行中',
  2: '已完成',
  3: '已归档'
};
//0-筹备中，1-进行中，2-已完成，3-已归档
const STATUS_LABELS = {
  IN_PREPARATION: '筹备中',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  ARCHIVED: '已归档'
};

const PRIORITY_TYPES = {
  CRITICAL: 'danger',
  HIGH: 'warning',
  MEDIUM: 'info',
  LOW: 'success'
};

const STATUS_TYPES = {
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
  startTime: null as string | null,
  endTime: null as string | null,
  createTime: '' as string,
  priority: 'MEDIUM' as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW',
  status: 'PENDING' as 'PENDING' | 'IN_PROGRESS' | 'REVIEW' | 'COMPLETED',
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
  { label: '紧急', value: 'CRITICAL' },
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' }
]

// 任务列表 - 从后端获取
const taskList = ref<Task[]>([])
const taskLoading = ref(false)

// 获取优先级标签类型
const getPriorityType = (priority: string): string => {
  return PRIORITY_TYPES[priority as keyof typeof PRIORITY_TYPES] || 'info';
}

// 获取状态标签类型
const getStatusType = (status: string): string => {
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
        label: `${user.username} (${user.role === 0 ? '成员' : '负责人'})`,
        avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        role: user.role
      }));
    } else {
      memberOptions.value = [];
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败');
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
    const response = await getProjectDetail(projectId);
    const data = extractDataFromResponse(response);
    
    if (!data) {
      ElMessage.warning('未获取到项目数据');
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
      status: STATUS_MAP_REVERSE[status as keyof typeof STATUS_MAP_REVERSE] || 'PENDING',
      creatorName: data.creator?.username || '',
      members: Array.isArray(data.members) ? data.members.map((m: any) => 
        typeof m === 'string' ? m : m?.username || ''
      ).filter(Boolean) : []
    });
    
    // 加载文件列表
    await fetchAttachments(parseInt(projectId));
    
    // 加载任务列表
    await fetchTasks(projectId);
  } catch (err: any) {
    error.value = err.message || '获取项目详情失败';
    ElMessage.error(error.value || '获取项目详情失败');
  } finally {
    loading.value = false;
  }
}

// 保存项目修改
const handleSave = async () => {
  if (!isLeader.value) {
    ElMessage.warning('您没有权限修改项目信息');
    return;
  }

  saveLoading.value = true;
  try {
    // 构建更新数据，确保与ProjectForm接口兼容
    const updateData: any = {
      id: parseInt(projectForm.id) || undefined,
      name: projectForm.name,
      description: projectForm.description,
      startTime: projectForm.startTime || undefined, // 字符串类型，YYYY-MM-DD HH:mm:ss格式
      endTime: projectForm.endTime || undefined,     // 使用endTime字段
      priority: PRIORITY_MAP[projectForm.priority] || 2,
      status: STATUS_MAP[projectForm.status] || 1,
      members: projectForm.members
    };
    
    // 更新项目
    await updateProject(route.params.id as string, updateData);
    ElMessage.success('保存成功');
    
    // 刷新数据
    setTimeout(() => {
      fetchProjectDetail();
    }, 500);
  } catch (err: any) {
    if (err.response) {
      ElMessage.error(`保存失败: ${err.response.status} - ${err.response.data?.message || '服务器错误'}`);
    } else if (err.request) {
      ElMessage.error('保存失败: 服务器没有响应');
    } else {
      ElMessage.error(err.message || '保存失败');
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
    ElMessage.warning('请选择要上传的文件');
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
      ElMessage.success('上传成功');
      uploadDialogVisible.value = false;
      uploadFiles.value = [];
      // 刷新文件列表
      await fetchAttachments(parseInt(route.params.id as string));
    } else {
      ElMessage.error(response.data.message || '上传失败');
    }
  } catch (error: any) {
    if (error.response?.status === 401) {
      ElMessage.error('认证失败，请重新登录');
      userStore.logout();
      router.push('/login');
      return;
    }
    
    ElMessage.error(error.message || '上传失败，请稍后重试');
  } finally {
    uploadLoading.value = false;
  }
};

// 文件操作方法
const handleViewFile = (file: ProjectFile) => {
  if (!file.url) {
    ElMessage.warning('文件链接不可用');
    return;
  }
  // 使用预览API打开文件
  window.open(`/api/files/preview?fileUrl=${encodeURIComponent(file.url)}`);
};

const handleDownloadFile = (file: ProjectFile) => {
  if (!file.url) {
    ElMessage.warning('文件链接不可用');
    return;
  }
  
  try {
    // 使用下载API获取解密后的文件
    window.location.href = `/api/files/download?fileUrl=${encodeURIComponent(file.url)}`;
  } catch (err) {
    ElMessage.error('下载失败');
  }
};

const handleDeleteFile = async (file: ProjectFile) => {
  if (!isLeader.value && file.uploader !== userStore.userInfo?.username) {
    ElMessage.warning('您没有权限删除此文件');
    return;
  }

  try {
    await ElMessageBox.confirm('确定要删除此文件吗？', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });

    // 删除文件
    const projectId = parseInt(route.params.id as string);
    const fileId = parseInt(file.id);
    
    if(isNaN(projectId) || isNaN(fileId)) {
      throw new Error('项目ID或文件ID无效');
    }
    
    await deleteAttachment(projectId, fileId);
    ElMessage.success('删除成功');
    
    // 刷新文件列表
    await fetchAttachments(projectId);
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败');
    }
  }
};

// 处理删除项目
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除此项目吗？此操作不可撤销！',
      '警告',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    );
    
    loading.value = true;
    await deleteProject(route.params.id as string);
    ElMessage.success('删除成功');
    router.push('/project/list');
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败');
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
    ElMessage.error('获取文件列表失败');
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
const TASK_STATUS_LABELS = {
  0: '待处理',
  1: '进行中',
  2: '已完成',
  3: '已取消'
}

const TASK_STATUS_TYPES = {
  0: 'info',
  1: 'warning',
  2: 'success',
  3: 'danger'
}

const TASK_PRIORITY_LABELS = {
  1: '低',
  2: '中',
  3: '高',
  4: '紧急'
}

const TASK_PRIORITY_TYPES = {
  1: 'success',
  2: 'info',
  3: 'warning',
  4: 'danger'
}

// 获取任务状态标签
const getTaskStatusLabel = (status: number): string => {
  return TASK_STATUS_LABELS[status as keyof typeof TASK_STATUS_LABELS] || '未知状态';
}

// 获取任务状态类型
const getTaskStatusType = (status: number): string => {
  return TASK_STATUS_TYPES[status as keyof typeof TASK_STATUS_TYPES] || 'info';
}

// 获取任务优先级标签
const getTaskPriorityLabel = (priority: number): string => {
  return TASK_PRIORITY_LABELS[priority as keyof typeof TASK_PRIORITY_LABELS] || '未知优先级';
}

// 获取任务优先级类型
const getTaskPriorityType = (priority: number): string => {
  return TASK_PRIORITY_TYPES[priority as keyof typeof TASK_PRIORITY_TYPES] || 'info';
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
    ElMessage.error('获取任务列表失败');
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
      '确定要删除此任务吗？此操作不可撤销！',
      '警告',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    );
    
    await deleteTask(task.id.toString());
    ElMessage.success('删除成功');
    
    // 刷新任务列表
    await fetchTasks(route.params.id as string);
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败');
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
