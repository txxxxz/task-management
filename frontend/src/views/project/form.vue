<template>
  <div class="project-form-container">
    <StepForm
      :steps="steps"
      v-model:currentStep="currentStep"
      :loading="loading"
      :showFooter="currentStep !== 2"
      @previous="handlePrevious"
      @next="handleNext"
    >
      <!-- 步骤1: 基本信息 -->
      <template #step-0>
        <el-form
          ref="formRef"
          :model="projectForm"
          :rules="formRules"
          label-width="100px"
          class="project-form"
        >
          <!-- 基本信息分组 -->
          <div class="form-group">
            <div class="group-title">Basic Information</div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="Project Name" prop="name" required>
                  <el-input 
                    v-model="projectForm.name" 
                    placeholder="Please enter the project name"
                    maxlength="50"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Priority" prop="priority" required>
                  <el-select
                    v-model="projectForm.priority"
                    placeholder="Please select the priority"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="option in getPriorityOptions()"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="Project Status" prop="status" required>
                  <el-select
                    v-model="projectForm.status"
                    placeholder="Please select the project status"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="option in getStatusOptions()"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 时间设置分组 -->
          <div class="form-group">
            <div class="group-title">Time Settings</div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="Start Time" prop="startTime" required>
                  <el-date-picker
                    v-model="projectForm.startTime"
                    type="datetime"
                    placeholder="Please select the start date"
                    style="width: 100%"
                    value-format="YYYY-MM-DD HH:mm:ss"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="End Time" prop="endTime" required>
                  <el-date-picker
                    v-model="projectForm.endTime"
                    type="datetime"
                    placeholder="Please select the end date"
                    style="width: 100%"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disabled-date="disabledEndDate"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 项目描述分组 -->
          <div class="form-group">
            <div class="group-title">Project Description</div>
            <el-form-item prop="description">
              <el-input
                v-model="projectForm.description"
                type="textarea"
                :rows="4"
                placeholder="Please enter the project description"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>

          <!-- 项目成员分组 -->
          <div class="form-group">
            <div class="group-title">项目成员</div>
            <el-form-item prop="members">
              <el-select
                v-model="projectForm.members"
                multiple
                filterable
                placeholder="请选择项目成员"
                style="width: 100%"
                loading-text="加载中..."
                :loading="membersLoading"
                :reserve-keyword="true"
                default-first-option
                clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
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
                <template #empty>
                  <div class="empty-members">
                    <p>未找到成员，请尝试其他关键词</p>
                  </div>
                </template>
              </el-select>
            </el-form-item>
          </div>
        </el-form>
      </template>

      <!-- 步骤2: 上传文件 -->
      <template #step-1>
        <div class="upload-container">
          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-remove="beforeFileRemove"
            :file-list="projectForm.attachments"
            multiple
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              Click or drag files here to upload
              <em>Support multiple file uploads at the same time</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                Support any type of file, each file cannot exceed 50MB
              </div>
            </template>
          </el-upload>
          
          <!-- 已上传文件列表 -->
          <div class="upload-list" v-if="projectForm.attachments.length > 0">
            <div v-for="(file, index) in projectForm.attachments" 
                 :key="index" 
                 class="upload-item animate__animated animate__fadeInDown"
            >
              <div class="file-info">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ typeof file === 'string' ? file.split('/').pop() : file.name }}</span>
                <span class="file-size" v-if="typeof file !== 'string' && file.size">{{ formatFileSize(file.size) }}</span>
              </div>
              <div class="file-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="removeFile(index)"
                  :icon="Delete"
                >
                  Delete
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 步骤3: 完成 -->
      <template #step-2>
        <div class="finish-container">
          <div class="confirm-icon">
            <el-icon :size="64" color="#409EFF"><InfoFilled /></el-icon>
          </div>
          <h2>确认{{ isEdit ? '更新' : '创建' }}项目</h2>
          <p class="confirm-desc">
            请确认以下信息无误，点击"提交"按钮{{ isEdit ? '更新' : '创建' }}项目。
          </p>
          
          <div class="project-summary">
            <el-descriptions title="项目信息" :column="2" border>
              <el-descriptions-item label="项目名称">{{ projectForm.name }}</el-descriptions-item>
              <el-descriptions-item label="优先级">
                {{ getPriorityLabel(projectForm.priority) }}
              </el-descriptions-item>
              <el-descriptions-item label="项目状态">
                {{ getStatusLabel(projectForm.status) }}
              </el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ projectForm.startTime }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ projectForm.endTime }}</el-descriptions-item>
              <el-descriptions-item label="项目成员">
                {{ projectForm.members.join(', ') || '无' }}
              </el-descriptions-item>
              <el-descriptions-item label="附件数量">
                {{ projectForm.attachments.length }}个文件
              </el-descriptions-item>
              <el-descriptions-item label="项目描述" :span="2">
                {{ projectForm.description || '无' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div class="action-buttons">
            <el-button @click="currentStep = 1">返回修改</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          </div>
        </div>
      </template>
    </StepForm>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineEmits, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { CircleCheckFilled, UploadFilled, Delete, Document, User, InfoFilled } from '@element-plus/icons-vue'
import { createProject, getProjectDetail, updateProject, getProjectMembers, batchUploadProjectFiles } from '../../api/project'
import { useUserStore } from '../../stores/user'
import StepForm from '../../components/StepForm.vue'
import dayjs from 'dayjs'
import { 
  getStatusOptions, 
  getPriorityOptions,
  type ProjectStatus,
  type ProjectPriority
} from '../../utils/status'
import 'animate.css'
import { getAllUsers } from '../../api/user'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const currentStep = ref(0)

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 步骤配置
const steps = [
  { title: 'Basic Information', component: 'BasicInfo' },
  { title: 'Upload Files', component: 'UploadFiles' },
  { title: 'Finish', component: 'Finish' }
]

// 表单数据
const projectForm = reactive({
  name: '',
  description: '',
  startTime: '',
  endTime: '',
  members: [] as string[],
  attachments: [] as any[],
  status: 0,
  priority: 1
})

// 成员选项
const memberOptions = ref<{ value: string, label: string, avatar?: string }[]>([])
const membersLoading = ref(false)

// 搜索用户
const searchMembers = async (query: string) => {
  if (!memberOptions.value.length || query === '') {
    // 如果还没有加载成员选项或是清空了查询，就加载所有用户
    await fetchUsers()
    return
  }
  
  membersLoading.value = true
  try {
    console.log('搜索成员，关键词:', query)
    const response = await getAllUsers({
      keyword: query,
      role: '0,1', // 只查询角色为0和1的用户
      page: 1,
      pageSize: 20
    })
    
    console.log('搜索成员响应:', response)
    console.log('code类型:', typeof response.data.code, '值:', response.data.code)
    console.log('数据结构:', response.data.data)
    
    // 正确处理响应结构
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
    ElMessage.error('搜索用户失败')
    console.error('搜索用户失败:', error)
  } finally {
    membersLoading.value = false
  }
}

// 获取初始用户列表
const fetchUsers = async () => {
  membersLoading.value = true
  try {
    console.log('获取所有用户')
    const response = await getAllUsers({
      role: '0,1', // 只查询角色为0和1的用户
      page: 1,
      pageSize: 20
    })
    
    console.log('获取用户响应:', response)
    console.log('code类型:', typeof response.data.code, '值:', response.data.code)
    console.log('数据结构:', response.data.data)
    
    // 正确处理响应结构
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
    ElMessage.error('获取用户列表失败')
    console.error('获取用户列表失败:', error)
  } finally {
    membersLoading.value = false
  }
}

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: 'Please enter the project name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length must be between 2 and 50 characters', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: 'Cannot exceed 200 characters', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: 'Please select the start date', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: 'Please select the end date', trigger: 'change' }
  ]
}

// 禁用结束日期
const disabledEndDate = (time: Date) => {
  if (!projectForm.startTime) return false
  return dayjs(time).isBefore(dayjs(projectForm.startTime))
}

// 处理文件变更
const handleFileChange = (file: any) => {
  console.log('文件变更:', file)
  // 确保文件对象包含raw属性（原始文件对象）
  if (file.raw) {
    // 如果已经有raw属性，使用它
    const existingFile = projectForm.attachments.find(f => 
      typeof f !== 'string' && f.uid === file.uid
    )
    if (!existingFile) {
      projectForm.attachments.push(file)
    }
  } else {
    // 如果没有raw属性，将整个文件对象作为raw
    file.raw = file
    projectForm.attachments.push(file)
  }
}

// 移除文件
const removeFile = (index: number) => {
  projectForm.attachments.splice(index, 1)
}

// 处理上一步
const handlePrevious = () => {
  // 可以在这里添加额外的逻辑
}

// 处理下一步
const handleNext = async () => {
  if (currentStep.value === 0) {
    if (!formRef.value) return
    try {
      await formRef.value.validate()
      // 验证成功后，进入第二步
      // 不需要手动增加步骤，StepForm 组件会自动处理
    } catch (error) {
      ElMessage.error('请完成必填信息')
      return
    }
  } else if (currentStep.value === 1) {
    // 第二步验证文件上传，然后进入第三步
    try {
      loading.value = true
      
      // 如果有文件，先上传文件
      if (projectForm.attachments.length > 0) {
        console.log('准备上传文件，数量:', projectForm.attachments.length)
        
        // 确保获取原始文件对象
        const files = projectForm.attachments.map(item => {
          if (typeof item === 'string') {
            // 如果是字符串（URL），跳过
            return null;
          }
          console.log('处理文件:', item.name, item)
          return item.raw || item
        }).filter(Boolean) as File[]; // 过滤掉 null 值并断言为 File[]
        
        console.log('处理后的文件对象:', files)
        
        try {
          const { data } = await batchUploadProjectFiles(files)
          console.log('文件上传响应:', data)
          
          if (data.code === 0 || data.code === 1 || data.code === 200) {
            // 上传成功，保存文件URL
            const fileUrls = data.data;
            // 将原始文件对象替换为上传后的URL
            projectForm.attachments = fileUrls;
            console.log('文件上传成功，URLs:', projectForm.attachments)
          } else {
            ElMessage.error(data.message || '文件上传失败')
            return
          }
        } catch (uploadError: any) {
          console.error('文件上传请求失败:', uploadError)
          console.error('错误详情:', uploadError.response?.data || uploadError.message)
          ElMessage.error(`文件上传失败: ${uploadError.response?.data?.message || uploadError.message}`)
          return
        }
      }
      
      // 文件上传成功后，进入第三步（确认页面）
      // 不需要手动增加步骤，StepForm 组件会自动处理
    } catch (error: any) {
      console.error('文件上传失败:', error)
      ElMessage.error(error.message || '文件上传失败')
      return
    } finally {
      loading.value = false
    }
  } else if (currentStep.value === 2) {
    // 第三步点击下一步时，调用handleSubmit提交表单
    await handleSubmit()
  }
}

// 处理提交
const handleSubmit = async () => {
  try {
    loading.value = true
    
    // 准备提交的数据
    const submitData = {
      name: projectForm.name,
      description: projectForm.description,
      startTime: projectForm.startTime,
      endTime: projectForm.endTime,
      members: projectForm.members,
      status: projectForm.status,
      priority: projectForm.priority,
      attachments: Array.isArray(projectForm.attachments) 
        ? projectForm.attachments.map(item => typeof item === 'string' ? item : item.url || item.response?.data)
        : []
    }
    
    console.log('准备提交的数据:', submitData)
    
    const projectId = Number(route.params.id)
    
    if (projectId) {
      // 更新项目
      const { data } = await updateProject(projectId, submitData)
      if (data.code === 0 || data.code === 1 || data.code === 200) {
        ElMessage.success('更新成功')
        // 更新成功后跳转到项目列表
        router.push('/project/list')
      } else {
        ElMessage.error(data.message || '更新失败')
      }
    } else {
      // 创建项目
      const { data } = await createProject(submitData)
      if (data.code === 0 || data.code === 1 || data.code === 200) {
        ElMessage.success('创建成功')
        // 创建成功后跳转到项目列表
        router.push('/project/list')
      } else {
        ElMessage.error(data.message || '创建失败')
      }
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 获取优先级标签
const getPriorityLabel = (priority: number) => {
  const options = getPriorityOptions()
  const option = options.find(opt => opt.value === priority)
  return option ? option.label : '未知'
}

// 获取状态标签
const getStatusLabel = (status: number) => {
  const options = getStatusOptions()
  const option = options.find(opt => opt.value === status)
  return option ? option.label : '未知'
}

// 查看项目
const handleCheckProject = () => {
  router.push('/project/list')
}

// 继续创建
const handleCreateAgain = () => {
  currentStep.value = 0
  Object.assign(projectForm, {
    name: '',
    description: '',
    startTime: '',
    endTime: '',
    members: [],
    attachments: [],
    status: 0,
    priority: 1
  })
}

// 获取项目详情
const fetchProjectDetail = async (projectId: number) => {
  loading.value = true
  try {
    const { data } = await getProjectDetail(projectId)
    if (data.code === 0 && data.data) {
      const projectData = data.data;
      
      projectForm.name = projectData.name;
      projectForm.description = projectData.description;
      projectForm.startTime = projectData.startTime;
      projectForm.endTime = projectData.endTime;
      
      // 处理项目成员
      if (Array.isArray(projectData.members)) {
        projectForm.members = projectData.members;
      } else {
        // 如果不是数组，尝试获取项目成员
        try {
          const membersResponse = await getProjectMembers(projectId);
          if (membersResponse.data.code === 0 && Array.isArray(membersResponse.data.data)) {
            projectForm.members = membersResponse.data.data;
          }
        } catch (error) {
          console.error('获取项目成员失败:', error);
          projectForm.members = [];
        }
      }
      
      projectForm.attachments = projectData.attachments || [];
      // 如果 attachments 是字符串数组，确保它们被正确处理
      if (Array.isArray(projectForm.attachments) && projectForm.attachments.length > 0) {
        console.log('项目附件:', projectForm.attachments);
      }
      projectForm.status = projectData.status;
      projectForm.priority = projectData.priority;
    } else {
      ElMessage.error(data.message || '获取项目详情失败');
      router.back();
    }
  } catch (error) {
    console.error('获取项目详情失败:', error);
    ElMessage.error('获取项目详情失败');
    router.back();
  } finally {
    loading.value = false;
  }
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  }
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

// 文件上传前的验证
const beforeFileRemove = (file: any) => {
  return ElMessageBox.confirm(
    `确定移除 ${file.name}？`
  ).then(
    () => true,
    () => false
  )
}

// 处理文件移除
const handleFileRemove = (file: any) => {
  const index = projectForm.attachments.findIndex(item => 
    typeof item !== 'string' && item.uid === file.uid
  )
  if (index !== -1) {
    projectForm.attachments.splice(index, 1)
  }
}

// 初始化时获取用户列表
fetchUsers()

onMounted(async () => {
  // 检查用户权限
  if (userStore.userInfo?.role !== 1) {
    ElMessage.error('只有项目负责人才能创建或编辑项目')
    router.push('/project/list')
    return
  }

  // 获取用户列表
  await fetchUsers()

  const projectId = route.params.id as string
  if (projectId) {
    await fetchProjectDetail(Number(projectId))
  }
})

// 额外添加一些控制台调试代码，帮助排查问题
watch(memberOptions, (newVal) => {
  console.log('成员选项已更新:', newVal.length, '个选项')
}, { immediate: true, deep: true })
</script>

<style scoped>
.project-form-container {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.upload-container {
  text-align: center;
  padding: 40px;
}

.upload-area {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.upload-list {
  margin-top: 20px;
  text-align: left;
  max-width: 600px;
  margin: 20px auto 0;
}

.upload-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 4px;
  background: #f5f7fa;
  margin-bottom: 8px;
  transition: all 0.3s;
}

.upload-item:hover {
  background: #e6e8eb;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  color: #303133;
  font-size: 14px;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.finish-container {
  text-align: center;
  padding: 40px;
}

.success-icon {
  margin-bottom: 20px;
}

.success-desc {
  color: #606266;
  margin: 20px 0 30px;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.member-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
}

:deep(.el-steps) {
  max-width: 800px;
  margin: 0 auto 40px;
}

:deep(.el-select-dropdown__item) {
  padding: 0 12px;
}

:deep(.el-upload__tip) {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
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

.member-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-select-dropdown__item) {
  padding: 8px 12px;
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
  min-height: 120px !important;
  resize: none;
}

.upload-container {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
}

.finish-container {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 40px;
}

.empty-members {
  text-align: center;
  padding: 20px 0;
  color: #909399;
}

:deep(.el-select-dropdown__list) {
  padding: 8px 0;
}

:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 8px 12px;
}

:deep(.el-select-dropdown__item.selected) {
  font-weight: normal;
  background-color: #f0f9eb;
}

:deep(.el-select-dropdown__item:hover) {
  background-color: #f5f7fa;
}

:deep(.el-select .el-select__tags .el-tag) {
  background-color: #ecf5ff;
  border-color: #d9ecff;
  color: #409eff;
}

:deep(.el-select .el-input__inner) {
  height: auto;
  min-height: 40px;
}
</style> 