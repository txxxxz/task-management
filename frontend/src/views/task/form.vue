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
            <div class="group-title">Basic Info</div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="Task Name" prop="name" required>
                  <el-input 
                    v-model="taskForm.name" 
                    placeholder="Please enter the task name"
                    maxlength="50"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Priority" prop="priority" required>
                  <el-select
                    v-model="taskForm.priority"
                    placeholder="Please select the priority"
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
                <el-form-item label="Project Name" prop="projectId" required>
                  <el-select
                    v-model="taskForm.projectId"
                    placeholder="Please select the project"
                    style="width: 100%"
                    filterable
                    remote
                    :remote-method="searchProjects"
                    :loading="projectsLoading"
                    loading-text="Loading..."
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
                        <p>No projects found, please try another keyword</p>
                      </div>
                    </template>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Status" prop="status" required>
                  <el-select
                    v-model="taskForm.status"
                    placeholder="Please select the status"
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
            <div class="group-title">Time Setting</div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="Start Time" prop="startTime" required>
                  <el-date-picker
                    v-model="taskForm.startTime"
                    type="datetime"
                    placeholder="Please select the start time"
                    style="width: 100%"
                    value-format="YYYY-MM-DD HH:mm:ss"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Due Time" prop="dueTime" required>
                  <el-date-picker
                    v-model="taskForm.dueTime"
                    type="datetime"
                    placeholder="Please select the due time"
                    style="width: 100%"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disabled-date="disabledDueDate"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 标签分组 -->
          <div class="form-group">
            <div class="group-title">Tags</div>
            <el-form-item label="Tags" prop="tags">
              <el-select
                v-model="taskForm.tags"
                multiple
                filterable
                remote
                reserve-keyword
                placeholder="Please select or enter the tags"
                :remote-method="searchTagsLocal"
                :loading="tagsLoading"
                style="width: 100%"
                @visible-change="handleTagSelectVisibleChange"
              >
                <el-option
                  v-for="tag in tagOptions"
                  :key="typeof tag === 'string' ? tag : tag.id"
                  :label="typeof tag === 'string' ? tag : tag.name"
                  :value="typeof tag === 'string' ? tag : tag.id"
                >
                  <div class="tag-option">
                    <el-tag 
                      :style="{
                        backgroundColor: typeof tag === 'object' && tag.color ? tag.color + '22' : '#409EFF22',
                        borderColor: typeof tag === 'object' && tag.color ? tag.color : '#409EFF',
                        color: typeof tag === 'object' && tag.color ? tag.color : '#409EFF'
                      }"
                      size="small"
                    >
                      <span class="tag-text">{{ typeof tag === 'string' ? tag : tag.name }}</span>
                    </el-tag>
                    <span v-if="typeof tag === 'object' && tag.isNew" class="tag-new-hint">
                      (New)
                    </span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </div>

          <!-- 任务描述分组 -->
          <div class="form-group">
            <div class="group-title">Task Description</div>
            <el-form-item prop="description">
              <el-input
                v-model="taskForm.description"
                type="textarea"
                :rows="4"
                placeholder="Please describe the task details"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>

          <!-- 任务成员分组 -->
          <div class="form-group">
            <div class="group-title">Task Members</div>
            <el-form-item prop="members" required>
              <el-select
                v-model="taskForm.members"
                multiple
                filterable
                remote
                :remote-method="searchMembers"
                placeholder="Please select the task members"
                style="width: 100%"
                loading-text="Loading..."
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
                    <p>cannot find users, try another keyword</p>
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
              Click or drag files here to upload
              <em>Support uploading multiple files at the same time</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                Support any type of file, each file is not more than 50MB
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
          <h2>Confirm {{ isEdit ? 'Update' : 'Create' }} Task</h2>
          <p class="confirm-desc">
            Please confirm the following information is correct, and click "Submit" to {{ isEdit ? 'update' : 'create' }} the task.
          </p>
          
          <div class="task-summary">
            <el-descriptions title="Task Information" :column="2" border>
              <el-descriptions-item label="Task Name">{{ taskForm.name }}</el-descriptions-item>
              <el-descriptions-item label="Priority">
                {{ getPriorityLabel(taskForm.priority) }}
              </el-descriptions-item>
              <el-descriptions-item label="Project">
                {{ getProjectLabel(taskForm.projectId) }}
              </el-descriptions-item>
              <el-descriptions-item label="Status">
                {{ getStatusLabel(taskForm.status) }}
              </el-descriptions-item>
              <el-descriptions-item label="Start Time">{{ taskForm.startTime || 'Not set' }}</el-descriptions-item>
              <el-descriptions-item label="Due Time">{{ taskForm.dueTime }}</el-descriptions-item>
              <el-descriptions-item label="Tags">
                {{ formatTagsDisplay }}
              </el-descriptions-item>
              <el-descriptions-item label="Task Members">
                {{ taskForm.members.join(', ') || '无' }}
              </el-descriptions-item>
              <el-descriptions-item label="Attachments">
                {{ taskForm.attachments.length }} files
              </el-descriptions-item>
              <el-descriptions-item label="Task Description" :span="2">
                {{ taskForm.description || 'No description' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div class="action-buttons">
            <el-button @click="currentStep = 1">Back to Modify</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading">Submit</el-button>
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
import { getAllProjects } from '../../api/project'
import { getAllUsers } from '../../api/user'
import { getTagList, createTag, searchTags, getAllTags } from '@/api/tag'
import type { Tag, TagListResponse } from '@/types/tag'
import type { Project } from '@/types/task'
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
  { title: 'BasicInfo', component: 'BasicInfo' },
  { title: 'UploadFiles', component: 'UploadFiles' },
  { title: 'Finish', component: 'Finish' }
]

// 表单数据
const taskForm = reactive({
  name: '',
  description: '',
  priority: 2,
  status: 0,
  startTime: '',
  dueTime: '',
  tags: [] as any[],
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
  { value: 1, label: 'Low' },
  { value: 2, label: 'Medium' },
  { value: 3, label: 'High' },
  { value: 4, label: 'Critical' }
]

// 状态选项
const statusOptions = [
  { value: 0, label: 'Pending' },
  { value: 1, label: 'In Progress' },
  { value: 2, label: 'Finished' },
  { value: 3, label: 'Canceled' }
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
    { required: true, message: 'Please enter the task name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length must be between 2 and 50 characters', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: 'Please select the priority', trigger: 'change' }
  ],
  status: [
    { required: true, message: 'Please select the task status', trigger: 'change' }
  ],
  projectId: [
    { required: true, message: 'Please select the project', trigger: 'change' }
  ],
  dueTime: [
    { required: true, message: 'Please select the due time', trigger: 'change' }
  ],
  members: [
    { required: true, message: 'Please select at least one member', trigger: 'change' }
  ]
}

// 获取项目列表
const fetchProjects = async (keyword = '') => {
  projectsLoading.value = true
  try {
    const response = await getAllProjects()
    if (response.data && (response.data.code === 1 || response.data.code === 0 || response.data.code === 200)) {
      let projects = response.data.data || [];
      
      // 确保projects是一个数组，检查是否有items属性
      if (projects && typeof projects === 'object' && 'items' in projects) {
        projects = projects.items || [];
      }
      
      // 如果依然不是数组，则将其转换为空数组
      if (!Array.isArray(projects)) {
        console.warn('获取到的项目数据不是数组:', projects);
        projects = [];
      }
      
      // 如果有关键字，进行本地过滤
      if (keyword && projects.length > 0) {
        projects = projects.filter((project: Project) => 
          project.name.toLowerCase().includes(keyword.toLowerCase())
        );
      }
      
      projectOptions.value = projects.map((project: Project) => ({
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
          label: `${user.username} (${user.role === 0 ? 'Member' : 'Leader'})`,
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
    console.error('Get user list failed:', error)
    ElMessage.error('Get user list failed')
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
      ElMessage.error('Please complete the required information')
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
          ElMessage.error(`File upload failed: ${uploadError.response?.data?.message || uploadError.message}`)
          return
        }
      }
      
    } catch (error: any) {
      console.error('文件上传失败:', error)
      ElMessage.error(error.message || 'File upload failed')
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
      dueTime: taskForm.dueTime,
      tagIds: processedTags,
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
      ElMessage.success('Update successfully')
      // 更新成功后跳转到任务列表
      router.push('/list')
    } else {
      // 创建任务
      await createTask(submitData)
      ElMessage.success('Create successfully')
      // 创建成功后跳转到任务列表
      router.push('/list')
    }
  } catch (error: any) {
    console.error('Operation failed:', error)
    ElMessage.error(error.message || 'Operation failed')
  } finally {
    loading.value = false
  }
}

// 处理标签，将新创建的标签保存到数据库
const processTags = async (tags: (string)[], projectId?: string): Promise<string[]> => {
  if (!tags || !Array.isArray(tags)) {
    return []
  }
  
  const processedTags: string[] = []
  
  for (const tagId of tags) {
    if (typeof tagId === 'string') {
      // 检查是否是新建标签（带有特殊前缀）
      if (tagId.startsWith('new-')) {
        // 查找对应的标签对象
        const newTagObj = tagOptions.value.find(t => t.id === tagId);
        if (newTagObj && newTagObj.isNew) {
          // 解析标签名称，去掉"创建: "前缀
          const tagName = newTagObj.name.replace('创建: ', '');
          const tagColor = newTagObj.color || getRandomColor();
          
          try {
            const response = await createTag({
              name: tagName,
              color: tagColor,
              projectId
            });
            console.log('Create new tag response:', response);
            
            if (response.data && response.data.data && response.data.data.id) {
              processedTags.push(response.data.data.id);
              
              // 添加到标签选项中，以便下次直接选择
              // 同时更新当前列表中的这个临时标签
              const newTag = {
                id: response.data.data.id,
                name: tagName,
                color: tagColor,
                projectId
              };
              
              // 查找临时标签在选项中的位置
              const tempIndex = tagOptions.value.findIndex(t => t.id === tagId);
              if (tempIndex !== -1) {
                // 替换临时标签
                tagOptions.value.splice(tempIndex, 1, newTag);
                
                // 同时更新taskForm.tags中的ID值
                const formIndex = taskForm.tags.indexOf(tagId);
                if (formIndex !== -1) {
                  taskForm.tags.splice(formIndex, 1, response.data.data.id);
                }
              } else {
                // 如果找不到临时标签，就添加到列表末尾
                tagOptions.value.push(newTag);
              }
            }
          } catch (error) {
            console.error('Create tag failed:', error);
          }
        }
      } else {
        // 使用现有标签ID
        processedTags.push(tagId);
      }
    }
  }
  
  console.log('Processed tag IDs:', processedTags);
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
  router.push('/list')
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
      // 先尝试使用dueTime字段，如果没有则尝试使用deadline字段
      taskForm.dueTime = taskData.dueTime || taskData.deadline || ''
      
      // 特殊处理标签数据
      if (taskData.tags && Array.isArray(taskData.tags)) {
        // 如果返回的是标签对象数组，则直接使用
        if (taskData.tags.length > 0 && typeof taskData.tags[0] === 'object') {
          taskForm.tags = taskData.tags.map((tag: any) => ({
            id: tag.id,
            name: tag.name,
            color: tag.color || '#409EFF',
            projectId: tag.projectId
          }));
        } else {
          // 如果只是ID数组，则需要加载标签详细信息
          taskForm.tags = taskData.tags;
          // 尝试加载标签详情
          await fetchTags(taskData.projectId);
        }
      } else {
        taskForm.tags = [];
      }
      
      taskForm.members = taskData.members || []
      taskForm.projectId = taskData.projectId
      taskForm.attachments = taskData.attachments || []
    } else {
      ElMessage.error('Get task details failed')
      router.back()
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('Get task details failed')
    router.back()
  } finally {
    loading.value = false
  }
}

// 搜索标签
const searchTagsLocal = async (query: string) => {
  console.log('搜索标签，关键词:', query)
  if (query.trim() === '') {
    // 获取所有标签，不按项目ID过滤
    return fetchTags();
  }
  
  tagsLoading.value = true
  try {
    // 使用 getAllTags 获取所有标签，不按项目过滤
    const response = await getAllTags();
    console.log('获取所有标签响应:', response)
    
    if (response.data && response.data.code === 1) {
      const tagsData = response.data.data;
      if (tagsData && tagsData.items && Array.isArray(tagsData.items)) {
        // 在本地过滤标签
        const filteredTags = tagsData.items.filter((tag: { name: string }) => 
          tag.name.toLowerCase().includes(query.toLowerCase())
        );
        
        // 处理标签数据，确保color字段存在
        tagOptions.value = filteredTags.map((tag: { id: any, name: string, color?: string, projectId?: string }) => ({
          id: String(tag.id), // 确保id总是字符串类型
          name: tag.name,
          color: tag.color || '#409EFF', // 如果后端未返回颜色，使用默认颜色
          projectId: tag.projectId
        }));
        
        // 如果没有找到匹配的标签，允许创建新标签
        if (tagOptions.value.length === 0) {
          // 创建一个临时ID，用于新建标签
          const newTagId = `new-${Date.now()}`;
          tagOptions.value = [{ 
            id: newTagId,
            name: `创建: ${query}`, 
            color: '#409EFF', 
            projectId: taskForm.projectId,
            isNew: true // 添加标记，表示这是新建标签
          }];
        }
        
        console.log('搜索标签结果处理完成:', tagOptions.value);
      } else {
        console.warn('标签数据格式异常:', tagsData);
        const newTagId = `new-${Date.now()}`;
        tagOptions.value = [{ 
          id: newTagId,
          name: `创建: ${query}`, 
          color: '#409EFF', 
          projectId: taskForm.projectId,
          isNew: true
        }];
      }
    } else {
      console.warn('获取标签失败:', response.data?.message || '未知错误');
      const newTagId = `new-${Date.now()}`;
      tagOptions.value = [{ 
        id: newTagId,
        name: `创建: ${query}`, 
        color: '#409EFF', 
        projectId: taskForm.projectId,
        isNew: true
      }];
    }
  } catch (error) {
    console.error('搜索标签失败:', error);
    const newTagId = `new-${Date.now()}`;
    tagOptions.value = [{ 
      id: newTagId,
      name: `创建: ${query}`, 
      color: '#409EFF', 
      projectId: taskForm.projectId,
      isNew: true
    }];
  } finally {
    tagsLoading.value = false;
  }
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

// 获取标签选项的颜色，用于展示
const getTagColor = (tagId: string) => {
  // 如果是新建标签（以new-开头）或者是现有标签ID，尝试从标签选项中查找
  const tagObj = tagOptions.value.find(t => t.id === tagId);
  return tagObj?.color || '#409EFF';
};

// 获取标签选项的名称，用于展示
const getTagName = (tagId: string) => {
  const tagObj = tagOptions.value.find(t => t.id === tagId);
  return tagObj?.name || tagId;
};

// 处理标签下拉框的显示状态
const handleTagSelectVisibleChange = (visible: boolean) => {
  console.log('标签选择框显示状态变化:', visible);
  if (visible) {
    // 当标签下拉框显示时，加载所有标签选项，不依赖项目ID
    console.log('标签选择框显示，正在加载所有标签选项...');
    fetchTags(); // 不传入 projectId，获取所有标签
  }
}

// 监听项目ID变化，不再根据项目ID重新加载标签
watch(() => taskForm.projectId, (newVal) => {
  console.log('项目ID变化:', newVal);
  // 由于现在我们始终获取所有标签，这里不再需要基于项目ID重新获取标签
  // 如果将来需要根据项目ID过滤标签，可以在此处添加本地过滤逻辑
})

// 处理标签关闭
const handleTagClose = (tag: any) => {
  const index = taskForm.tags.findIndex((item: any) => 
    typeof item === 'string' 
      ? item === tag 
      : typeof tag === 'string'
        ? item.name === tag
        : String(item.id) === String(tag.id) // 确保ID比较时类型一致
  );
  if (index !== -1) {
    taskForm.tags.splice(index, 1);
  }
}

// 在任务摘要中格式化标签显示
const formatTagsDisplay = computed(() => {
  if (!taskForm.tags || taskForm.tags.length === 0) {
    return '无';
  }
  
  return taskForm.tags.map(tagId => getTagName(tagId)).join(', ');
})

onMounted(async () => {
  // 检查用户权限
  if (userStore.userInfo?.role !== 1) {
    ElMessage.error('Only project leaders can create or edit tasks')
    router.push('/list')
    return
  }

  // 获取项目列表
  await fetchProjects()
  
  // 获取用户列表
  await fetchUsers()
  
  // 初始化获取所有标签(不指定项目ID)
  await fetchTags()
  
  // 处理路由参数
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
  white-space: nowrap;
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
  padding: 30px;
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
  padding: 0 20px;
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
  margin: 2px 4px;
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
  transition: all 0.3s;
}

:deep(.el-select .el-select__tags .el-tag:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

:deep(.el-select .el-select__tags .el-tag .el-tag__close) {
  color: inherit;
  opacity: 0.8;
}

:deep(.el-select .el-select__tags .el-tag .el-tag__close:hover) {
  background-color: rgba(0, 0, 0, 0.1);
  color: inherit;
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

.tag-option .el-tag span {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-select__tags .el-tag--info) {
  background-color: var(--el-color-primary-light-9) !important;
  border-color: var(--el-color-primary-light-8) !important;
  color: var(--el-color-primary) !important;
}

.tag-new-hint {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
  white-space: nowrap;
}

.empty-tags {
  padding: 10px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

:deep(.el-select-dropdown__item:hover) {
  background-color: #f5f7fa;
}

:deep(.el-select__tags .el-tag) {
  margin: 2px 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
  border-radius: 4px;
  max-width: calc(100% - 20px);
  display: inline-flex;
  align-items: center;
  background-color: var(--el-color-primary-light-9) !important;
  border-color: var(--el-color-primary-light-8) !important;
  color: var(--el-color-primary) !important;
}

:deep(.el-select__tags .el-tag:hover) {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

:deep(.el-select__tags .el-tag .el-tag__close) {
  color: inherit;
  opacity: 0.8;
}

:deep(.el-select__tags .el-tag .el-tag__close:hover) {
  background-color: rgba(0, 0, 0, 0.1);
  color: inherit;
}

:deep(.el-select__tags .el-tag span) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

:deep(.el-select .el-input__inner) {
  height: auto;
  min-height: 40px;
}

:deep(.el-select__tags) {
  flex-wrap: wrap;
  margin: 4px 0;
}

.tag-option .el-tag .tag-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
  max-width: 150px;
}
</style> 