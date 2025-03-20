<template>
  <div class="task-form-container">
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
          :model="taskForm"
          :rules="formRules"
          label-width="100px"
          class="task-form"
        >
          <!-- 基本信息分组 -->
          <div class="form-group">
            <div class="group-title">基本信息</div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="任务名称" prop="name" required>
                  <el-input 
                    v-model="taskForm.name" 
                    placeholder="请输入任务名称"
                    maxlength="50"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="优先级" prop="priority" required>
                  <el-select
                    v-model="taskForm.priority"
                    placeholder="请选择优先级"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="option in priorityOptions"
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
                <el-form-item label="所属项目" prop="projectId" required>
                  <el-select
                    v-model="taskForm.projectId"
                    placeholder="请选择所属项目"
                    style="width: 100%"
                    filterable
                    remote
                    :remote-method="searchProjects"
                    :loading="projectsLoading"
                    loading-text="加载中..."
                    :reserve-keyword="true"
                    default-first-option
                    clearable
                  >
                    <template #prefix>
                      <el-icon><Document /></el-icon>
                    </template>
                    <el-option
                      v-for="project in projectOptions"
                      :key="project.value"
                      :label="project.label"
                      :value="project.value"
                    />
                    <template #empty>
                      <div class="empty-projects">
                        <p>未找到项目，请尝试其他关键词</p>
                      </div>
                    </template>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status" required>
                  <el-select
                    v-model="taskForm.status"
                    placeholder="请选择任务状态"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="option in statusOptions"
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
            <div class="group-title">时间设置</div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开始时间" prop="startTime">
                  <el-date-picker
                    v-model="taskForm.startTime"
                    type="datetime"
                    placeholder="请选择开始时间"
                    style="width: 100%"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="截止时间" prop="dueTime" required>
                  <el-date-picker
                    v-model="taskForm.dueTime"
                    type="datetime"
                    placeholder="请选择截止时间"
                    style="width: 100%"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    :disabled-date="disabledDueDate"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 标签分组 -->
          <div class="form-group">
            <div class="group-title">标签</div>
            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="taskForm.tags"
                multiple
                filterable
                remote
                reserve-keyword
                placeholder="请选择或输入标签"
                :remote-method="searchTagsLocal"
                :loading="tagsLoading"
                style="width: 100%"
                @visible-change="handleTagSelectVisibleChange"
              >
                <el-option
                  v-for="tag in tagOptions"
                  :key="typeof tag === 'string' ? tag : tag.id"
                  :label="typeof tag === 'string' ? tag : tag.name"
                  :value="tag"
                >
                  <div class="tag-option">
                    <el-tag 
                      :color="typeof tag === 'object' && tag.color ? tag.color : ''" 
                      size="small" 
                      effect="plain"
                    >
                      {{ typeof tag === 'string' ? tag : tag.name }}
                    </el-tag>
                    <span v-if="typeof tag === 'object' && tag.id === 'new'" class="tag-new-hint">
                      (新建)
                    </span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </div>

          <!-- 任务描述分组 -->
          <div class="form-group">
            <div class="group-title">任务描述</div>
            <el-form-item prop="description">
              <el-input
                v-model="taskForm.description"
                type="textarea"
                :rows="4"
                placeholder="请描述任务详情"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>

          <!-- 任务成员分组 -->
          <div class="form-group">
            <div class="group-title">任务成员</div>
            <el-form-item prop="members" required>
              <el-select
                v-model="taskForm.members"
                multiple
                filterable
                remote
                :remote-method="searchMembers"
                placeholder="请选择任务成员"
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
            :file-list="taskForm.attachments"
            multiple
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              点击或拖拽文件到此处上传
              <em>支持同时上传多个文件</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持任意类型文件，每个文件不超过50MB
              </div>
            </template>
          </el-upload>
          
          <!-- 已上传文件列表 -->
          <div class="upload-list" v-if="taskForm.attachments.length > 0">
            <div v-for="(file, index) in taskForm.attachments" 
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
                  删除
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
          <h2>确认{{ isEdit ? '更新' : '创建' }}任务</h2>
          <p class="confirm-desc">
            请确认以下信息无误，点击"提交"按钮{{ isEdit ? '更新' : '创建' }}任务。
          </p>
          
          <div class="task-summary">
            <el-descriptions title="任务信息" :column="2" border>
              <el-descriptions-item label="任务名称">{{ taskForm.name }}</el-descriptions-item>
              <el-descriptions-item label="优先级">
                {{ getPriorityLabel(taskForm.priority) }}
              </el-descriptions-item>
              <el-descriptions-item label="所属项目">
                {{ getProjectLabel(taskForm.projectId) }}
              </el-descriptions-item>
              <el-descriptions-item label="任务状态">
                {{ getStatusLabel(taskForm.status) }}
              </el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ taskForm.startTime || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="截止时间">{{ taskForm.dueTime }}</el-descriptions-item>
              <el-descriptions-item label="标签">
                {{ taskForm.tags.join(', ') || '无' }}
              </el-descriptions-item>
              <el-descriptions-item label="任务成员">
                {{ taskForm.members.join(', ') || '无' }}
              </el-descriptions-item>
              <el-descriptions-item label="附件数量">
                {{ taskForm.attachments.length }}个文件
              </el-descriptions-item>
              <el-descriptions-item label="任务描述" :span="2">
                {{ taskForm.description || '无' }}
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { CircleCheckFilled, UploadFilled, Delete, Document, User, InfoFilled, PriceTag } from '@element-plus/icons-vue'
import { createTask, getTaskDetail, updateTask } from '../../api/task'
import { getProjectList } from '../../api/project'
import { getAllUsers } from '../../api/user'
import { getTagList, createTag, searchTags } from '@/api/tag'
import type { Tag } from '@/types/tag'
import { batchUploadTaskFiles } from '../../api/file'
import { useUserStore } from '../../stores/user'
import StepForm from '../../components/StepForm.vue'
import dayjs from 'dayjs'
import 'animate.css'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const currentStep = ref(0)
const membersLoading = ref(false)
const projectsLoading = ref(false)
const tagsLoading = ref(false)

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 步骤配置
const steps = [
  { title: '基本信息', component: 'BasicInfo' },
  { title: '上传文件', component: 'UploadFiles' },
  { title: '完成', component: 'Finish' }
]

// 表单数据
const taskForm = reactive({
  name: '',
  description: '',
  priority: 2,
  status: 0,
  startTime: '',
  dueTime: '',
  tags: [] as string[],
  members: [] as string[],
  attachments: [] as any[],
  projectId: ''
})

// 项目搜索关键词
const projectSearchKeyword = ref('')

// 成员搜索关键词
const memberSearchKeyword = ref('')

// 优先级选项
const priorityOptions = [
  { value: 1, label: '低' },
  { value: 2, label: '中' },
  { value: 3, label: '高' },
  { value: 4, label: '紧急' }
]

// 状态选项
const statusOptions = [
  { value: 0, label: '待办' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' }
]

// 项目选项
const projectOptions = ref<{ value: string, label: string }[]>([])

// 标签选项
const tagOptions = ref<Tag[]>([])

// 成员选项
const memberOptions = ref<{ value: string, label: string, avatar?: string }[]>([])

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择任务状态', trigger: 'change' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  dueTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ],
  members: [
    { required: true, message: '请至少选择一名成员', trigger: 'change' }
  ]
}

// 获取项目列表
const fetchProjects = async (keyword = '') => {
  projectsLoading.value = true
  try {
    const params = {
      keyword: keyword,
      page: 1,
      pageSize: 50
    }
    const response = await getProjectList(params)
    if (response.data && (response.data.code === 1 || response.data.code === 0 || response.data.code === 200)) {
      const projects = response.data.data.items
      projectOptions.value = projects.map(project => ({
        value: project.id.toString(),
        label: project.name
      }))
    } else {
      projectOptions.value = []
      console.warn('获取项目列表失败:', response)
    }
  } catch (error) {
    projectOptions.value = []
    console.error('获取项目列表失败:', error)
  } finally {
    projectsLoading.value = false
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

// 搜索项目
const searchProjects = async (query: string) => {
  projectSearchKeyword.value = query
  if (query) {
    await fetchProjects(query)
  } else {
    await fetchProjects()
  }
}

// 搜索用户
const searchMembers = async (query: string) => {
  memberSearchKeyword.value = query
  if (query) {
    await fetchUsers(query)
  } else {
    await fetchUsers()
  }
}

// 监听项目搜索关键词变化
watch(projectSearchKeyword, debounce((newVal: string) => {
  searchProjects(newVal)
}, 300))

// 监听成员搜索关键词变化
watch(memberSearchKeyword, debounce((newVal: string) => {
  searchMembers(newVal)
}, 300))

// 防抖函数
function debounce<T extends (...args: any[]) => any>(fn: T, delay: number) {
  let timer: number | null = null
  return function(this: any, ...args: Parameters<T>) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay) as unknown as number
  }
}

// 禁用截止日期
const disabledDueDate = (time: Date) => {
  if (!taskForm.startTime) return false
  return dayjs(time).isBefore(dayjs(taskForm.startTime))
}

// 处理文件变更
const handleFileChange = (file: any) => {
  console.log('文件变更:', file)
  // 确保文件对象包含raw属性（原始文件对象）
  if (file.raw) {
    // 如果已经有raw属性，使用它
    const existingFile = taskForm.attachments.find(f => 
      typeof f !== 'string' && f.uid === file.uid
    )
    if (!existingFile) {
      taskForm.attachments.push(file)
    }
  } else {
    // 如果没有raw属性，将整个文件对象作为raw
    file.raw = file
    taskForm.attachments.push(file)
  }
}

// 移除文件
const removeFile = (index: number) => {
  taskForm.attachments.splice(index, 1)
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
  const index = taskForm.attachments.findIndex(item => 
    typeof item !== 'string' && item.uid === file.uid
  )
  if (index !== -1) {
    taskForm.attachments.splice(index, 1)
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
    } catch (error) {
      ElMessage.error('请完成必填信息')
      return
    }
  } else if (currentStep.value === 1) {
    // 处理文件上传
    try {
      loading.value = true
      
      // 如果有文件，先上传文件
      if (taskForm.attachments.length > 0) {
        console.log('准备上传文件，数量:', taskForm.attachments.length)
        
        // 确保获取原始文件对象
        const files = taskForm.attachments.map(item => {
          if (typeof item === 'string') {
            // 如果是字符串（URL），跳过
            return null;
          }
          console.log('处理文件:', item.name, item)
          return item.raw || item
        }).filter(Boolean) as File[]; // 过滤掉 null 值并断言为 File[]
        
        console.log('处理后的文件对象:', files)
        
        try {
          const { data } = await batchUploadTaskFiles(files)
          console.log('文件上传响应:', data)
          
          if (data.code === 0 || data.code === 1 || data.code === 200) {
            // 上传成功，保存文件URL
            const fileUrls = data.data;
            // 将原始文件对象替换为上传后的URL
            taskForm.attachments = fileUrls;
            console.log('文件上传成功，URLs:', taskForm.attachments)
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
      
    } catch (error: any) {
      console.error('文件上传失败:', error)
      ElMessage.error(error.message || '文件上传失败')
      return
    } finally {
      loading.value = false
    }
  } else if (currentStep.value === 2) {
    // 当在最后一步点击完成按钮时，提交表单
    await handleSubmit();
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    loading.value = true
    
    // 处理标签，将新创建的标签保存到数据库
    const processedTags = await processTags(taskForm.tags, taskForm.projectId)
    
    // 准备提交的数据
    const submitData = {
      name: taskForm.name,
      description: taskForm.description,
      priority: taskForm.priority,
      status: taskForm.status,
      startTime: taskForm.startTime,
      deadline: taskForm.dueTime,
      tags: processedTags,
      members: taskForm.members,
      projectId: taskForm.projectId,
      attachments: Array.isArray(taskForm.attachments) 
        ? taskForm.attachments.map(item => typeof item === 'string' ? item : item.url || item.response?.data)
        : []
    }
    
    console.log('准备提交的数据:', submitData)
    
    const taskId = route.params.id as string
    
    if (taskId) {
      // 更新任务
      await updateTask(taskId, submitData)
      ElMessage.success('更新成功')
      // 更新成功后跳转到任务列表
      router.push('/task/list')
    } else {
      // 创建任务
      await createTask(submitData)
      ElMessage.success('创建成功')
      // 创建成功后跳转到任务列表
      router.push('/task/list')
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 处理标签，将新创建的标签保存到数据库
const processTags = async (tags: (Tag | string)[], projectId?: string): Promise<string[]> => {
  if (!tags || !Array.isArray(tags)) {
    return []
  }
  
  const processedTags: string[] = []
  
  for (const tag of tags) {
    if (typeof tag === 'string') {
      // 创建新标签
      try {
        const response = await createTag({
          name: tag,
          color: getRandomColor(),
          projectId
        })
        if (response.data && response.data.data && response.data.data.id) {
          processedTags.push(response.data.data.id)
        }
      } catch (error) {
        console.error('创建标签失败:', error)
      }
    } else if (tag.id === 'new') {
      // 创建新标签（从搜索结果中）
      const tagName = tag.name.replace('创建: ', '')
      try {
        const response = await createTag({
          name: tagName,
          color: getRandomColor(),
          projectId
        })
        if (response.data && response.data.data && response.data.data.id) {
          processedTags.push(response.data.data.id)
        }
      } catch (error) {
        console.error('创建标签失败:', error)
      }
    } else {
      // 使用现有标签ID
      processedTags.push(tag.id)
    }
  }
  
  return processedTags
}

// 生成随机颜色
const getRandomColor = () => {
  const colors = [
    '#f56c6c', '#e6a23c', '#409EFF', '#67c23a', '#909399',
    '#9c27b0', '#3f51b5', '#2196f3', '#00bcd4', '#009688',
    '#4caf50', '#8bc34a', '#cddc39', '#ffeb3b', '#ffc107'
  ]
  return colors[Math.floor(Math.random() * colors.length)]
}

// 获取优先级标签
const getPriorityLabel = (priority: number) => {
  const option = priorityOptions.find(opt => opt.value === priority)
  return option ? option.label : '未知'
}

// 获取状态标签
const getStatusLabel = (status: number) => {
  const option = statusOptions.find(opt => opt.value === status)
  return option ? option.label : '未知'
}

// 获取项目标签
const getProjectLabel = (projectId: string) => {
  const option = projectOptions.value.find(opt => opt.value === projectId)
  return option ? option.label : '未知'
}

// 查看任务
const handleCheckTask = () => {
  router.push('/task/list')
}

// 继续创建
const handleCreateAgain = () => {
  currentStep.value = 0
  Object.assign(taskForm, {
    name: '',
    description: '',
    priority: 2,
    status: 0,
    startTime: '',
    dueTime: '',
    tags: [],
    members: [],
    attachments: [],
    projectId: ''
  })
}

// 获取任务详情
const fetchTaskDetail = async (taskId: string) => {
  loading.value = true
  try {
    const response = await getTaskDetail(taskId)
    const taskData = response.data
    
    if (taskData) {
      taskForm.name = taskData.name || taskData.title || ''
      taskForm.description = taskData.description || ''
      // 确保优先级和状态是整数
      taskForm.priority = typeof taskData.priority === 'number' ? taskData.priority : 2 // 默认中等优先级
      taskForm.status = typeof taskData.status === 'number' ? taskData.status : 0 // 默认待办状态
      taskForm.startTime = taskData.startTime || ''
      taskForm.dueTime = taskData.deadline || ''
      taskForm.tags = taskData.tags || []
      taskForm.members = taskData.members || []
      taskForm.projectId = taskData.projectId
      taskForm.attachments = taskData.attachments || []
    } else {
      ElMessage.error('获取任务详情失败')
      router.back()
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 搜索标签
const searchTagsLocal = async (query: string) => {
  console.log('搜索标签，关键词:', query, '项目ID:', taskForm.projectId)
  if (query.trim() === '') {
    return fetchTags(taskForm.projectId)
  }
  
  tagsLoading.value = true
  try {
    const response = await searchTags(query, taskForm.projectId ? Number(taskForm.projectId) : undefined)
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      tagOptions.value = response.data.data
      
      // 如果没有找到匹配的标签，允许创建新标签
      if (tagOptions.value.length === 0) {
        tagOptions.value = [{ id: 'new', name: `创建: ${query}`, color: '#409EFF', projectId: taskForm.projectId }]
      }
    } else {
      console.warn('搜索标签响应格式不正确:', response)
      tagOptions.value = [{ id: 'new', name: `创建: ${query}`, color: '#409EFF', projectId: taskForm.projectId }]
    }
  } catch (error) {
    console.error('搜索标签失败:', error)
    tagOptions.value = [{ id: 'new', name: `创建: ${query}`, color: '#409EFF', projectId: taskForm.projectId }]
  } finally {
    tagsLoading.value = false
  }
}

// 获取标签选项
const fetchTags = async (projectId?: string) => {
  console.log('获取标签列表，项目ID:', projectId)
  tagsLoading.value = true
  try {
    const response = await getTagList({ projectId })
    if (response.data && response.data.data && Array.isArray(response.data.data.items)) {
      tagOptions.value = response.data.data.items
    } else {
      console.warn('获取标签列表响应格式不正确:', response)
      tagOptions.value = []
    }
  } catch (error) {
    console.error('获取标签列表失败:', error)
    tagOptions.value = []
  } finally {
    tagsLoading.value = false
  }
}

// 处理标签下拉框的显示状态
const handleTagSelectVisibleChange = (visible: boolean) => {
  if (visible) {
    // 当标签下拉框显示时，加载标签选项
    fetchTags(taskForm.projectId)
  }
}

// 监听项目ID变化，重新加载标签
watch(() => taskForm.projectId, (newVal) => {
  if (newVal) {
    fetchTags(newVal)
  } else {
    fetchTags()
  }
})

onMounted(async () => {
  // 检查用户权限
  if (userStore.userInfo?.role !== 1) {
    ElMessage.error('只有项目负责人才能创建或编辑任务')
    router.push('/task/list')
    return
  }

  // 获取项目列表
  await fetchProjects()
  
  // 获取用户列表
  await fetchUsers()
  
  // 获取标签列表
  await fetchTags()

  const taskId = route.params.id as string
  if (taskId) {
    await fetchTaskDetail(taskId)
  }
})
</script>

<style scoped>
.task-form-container {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.upload-container {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
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
  background: #f8f9fa;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
}

.confirm-icon {
  margin-bottom: 20px;
}

.confirm-desc {
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

.task-summary {
  text-align: left;
  margin: 20px 0;
}

.empty-projects {
  text-align: center;
  padding: 20px 0;
  color: #909399;
}

.tag-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tag-new-hint {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.empty-tags {
  padding: 10px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style> 