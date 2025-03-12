<template>
  <div class="create-task-container">
    <!-- 步骤导航 -->
    <el-steps :active="currentStep" finish-status="success" class="task-steps">
      <el-step title="Basic Information" />
      <el-step title="Upload Files" />
      <el-step title="Finish" />
    </el-steps>

    <!-- 步骤1：基本信息 -->
    <div v-show="currentStep === 0">
      <el-form
        ref="formRef"
        :model="taskForm"
        :rules="rules"
        label-width="120px"
        class="task-form"
      >
        <el-form-item label="Task Name" prop="title" required>
          <el-input v-model="taskForm.title" placeholder="Please enter the task name" />
        </el-form-item>

        <el-form-item label="Priority" prop="priority" required>
          <el-select v-model="taskForm.priority" placeholder="Please select the priority" style="width: 100%">
            <el-option label="Low" value="LOW" />
            <el-option label="Medium" value="MEDIUM" />
            <el-option label="High" value="HIGH" />
            <el-option label="Critical" value="CRITICAL" />
          </el-select>
        </el-form-item>

        <el-form-item label="Due Time" prop="dueTime" required>
          <el-date-picker
            v-model="taskForm.dueTime"
            type="datetime"
            placeholder="Please select the due time"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="Tags" prop="tags">
          <el-select
            v-model="taskForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            :reserve-keyword="false"
            placeholder="Please select the tags or enter a new tag"
            style="width: 100%"
            @visible-change="handleTagDropdownToggle"
          >
            <template #prefix>
              <el-icon><Collection /></el-icon>
            </template>
            <el-option
              v-for="tag in tagOptions"
              :key="tag"
              :label="tag"
              :value="tag"
            >
              <span style="float: left">{{ tag }}</span>
              <span 
                v-if="isDefaultTag(tag)"
                style="float: right; color: var(--el-text-color-secondary); font-size: 13px"
              >
                Default Tags
              </span>
            </el-option>
            <template #empty>
              <div style="padding: 8px 12px;">
                <span style="color: var(--el-text-color-secondary);">
                  Enter a new tag name and press Enter to create
                </span>
              </div>
            </template>
          </el-select>
          <div class="form-item-tip">
            Tips: You can enter a new tag name and press Enter to create
          </div>
        </el-form-item>

        <el-form-item label="Task Description" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="4"
            placeholder="Please describe the task"
          />
        </el-form-item>

        <el-form-item label="Members" prop="members">
          <el-select
            v-model="taskForm.members"
            multiple
            filterable
            placeholder="Please select the members"
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
      </el-form>
    </div>

    <!-- 步骤2：上传文件 -->
    <div v-show="currentStep === 1" class="upload-container">
      <el-upload
        class="upload-area"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        multiple
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          Click or drag files here to upload
        </div>
      </el-upload>
      
      <!-- 已上传文件列表 -->
      <div class="upload-list" v-if="taskForm.attachments.length > 0">
        <div v-for="(file, index) in taskForm.attachments" :key="index" class="upload-item">
          <span>{{ file.name }}</span>
          <el-button type="danger" size="small" @click="removeFile(index)">
            Delete
          </el-button>
        </div>
      </div>
    </div>

    <!-- 步骤3：完成 -->
    <div v-show="currentStep === 2" class="finish-container">
      <div class="success-icon">
        <el-icon :size="64" color="#67C23A"><CircleCheckFilled /></el-icon>
      </div>
      <h2>Task created successfully</h2>
      <div class="action-buttons">
        <el-button type="primary" @click="handleCheckTask">View Task</el-button>
        <el-button @click="handleCreateAgain">Continue Creating</el-button>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="form-buttons">
      <el-button v-if="currentStep > 0" @click="previousStep">Previous Step</el-button>
      <el-button 
        v-if="currentStep < 2" 
        type="primary" 
        @click="nextStep"
      >
          Next Step
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, UploadFilled, Collection } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const currentStep = ref(0)

// 检查用户权限
onMounted(() => {
  if (userStore.userInfo?.role !== 1) {
      ElMessage.error('Only the administrator can create tasks')
    router.push('/list')
  }
})

interface TaskForm {
  title: string
  description: string
  priority: string
  dueTime: string
  tags: string[]
  members: string[]
  attachments: File[]
}

// 表单数据
const taskForm = reactive<TaskForm>({
  title: '',
  description: '',
  priority: '',
  dueTime: '',
  tags: [],
  members: [],
  attachments: []
})

// 表单验证规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: 'Please enter the task name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length must be between 2 and 50 characters', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: 'Please select the priority', trigger: 'change' }
  ],
  dueTime: [
    { required: true, message: 'Please select the due time', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && dayjs(value).isBefore(dayjs())) {
          callback(new Error('The due time cannot be earlier than the current time'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  members: [
    { required: true, message: 'Please select at least one member', trigger: 'change' }
  ]
})

// 默认标签列表
const defaultTags = [
  'Bug',
  'Login Page',
  'Feature Development',
  'Performance Optimization',
  'UI Design',
  'Documentation'
]

// 标签选项（包括用户新建的标签）
const tagOptions = ref([...defaultTags])

// 判断是否为默认标签
const isDefaultTag = (tag: string) => defaultTags.includes(tag)

// 处理标签下拉框显示/隐藏
const handleTagDropdownToggle = (visible: boolean) => {
  if (visible) {
    // 当下拉框打开时，可以在这里从后端获取最新的标签列表
    console.log('Tag dropdown opened')
  }
}

// 成员选项
const memberOptions = [
  { value: 'John', label: 'John' },
  { value: 'Amy', label: 'Amy' },
  { value: 'Tom', label: 'Tom' },
  { value: 'Jack', label: 'Jack' }
]

// 处理文件变更
const handleFileChange = (uploadFile: UploadFile) => {
  taskForm.attachments.push(uploadFile.raw as File)
}

// 移除文件
const removeFile = (index: number) => {
  taskForm.attachments.splice(index, 1)
}

// 下一步
const nextStep = async () => {
  if (currentStep.value === 0) {
    if (!formRef.value) return
    try {
      await formRef.value.validate()
      currentStep.value++
    } catch (error) {
      ElMessage.error('Please complete the required information')
      return
    }
  } else if (currentStep.value === 1) {
    currentStep.value++
  }
}

// 上一步
const previousStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 查看任务
const handleCheckTask = () => {
  router.push('/table/update')
}

// 继续创建
const handleCreateAgain = () => {
  currentStep.value = 0
  Object.assign(taskForm, {
    title: '',
    description: '',
    priority: '',
    dueTime: '',
    tags: [],
    members: [],
    attachments: []
  })
}
</script>

<style scoped>
.create-task-container {
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.task-steps {
  margin-bottom: 40px;
}

.task-form {
  max-width: 800px;
  margin: 0 auto;
}

.form-buttons {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  gap: 20px;
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
}

.upload-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.finish-container {
  text-align: center;
  padding: 40px;
}

.success-icon {
  margin-bottom: 20px;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
}

:deep(.el-steps) {
  max-width: 800px;
  margin: 0 auto 40px;
}

.form-item-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  line-height: 1.4;
}
</style> 