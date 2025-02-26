<template>
  <div class="task-form-container">
    <StepForm
      :steps="steps"
      v-model:currentStep="currentStep"
      :loading="loading"
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
          <el-form-item label="Task Name" prop="name" required>
            <el-input v-model="taskForm.name" placeholder="Please enter the task name" />
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
              placeholder="Please select or create tags"
              style="width: 100%"
            >
              <el-option
                v-for="tag in tagOptions"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="Description" prop="description">
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
            multiple
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              点击或拖拽文件到此处上传
            </div>
          </el-upload>
          
          <!-- 已上传文件列表 -->
          <div class="upload-list" v-if="taskForm.attachments.length > 0">
            <div v-for="(file, index) in taskForm.attachments" :key="index" class="upload-item">
              <span>{{ file.name }}</span>
              <el-button type="danger" size="small" @click="removeFile(index)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 步骤3: 完成 -->
      <template #step-2>
        <div class="finish-container">
          <div class="success-icon">
            <el-icon :size="64" color="#67C23A"><CircleCheckFilled /></el-icon>
          </div>
          <h2>Create successfully</h2>
          <div class="action-buttons">
            <el-button type="primary" @click="handleCheckTask">View Task</el-button>
            <el-button @click="handleCreateAgain">Continue Create</el-button>
          </div>
        </div>
      </template>
    </StepForm>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { CircleCheckFilled, UploadFilled } from '@element-plus/icons-vue'
import { createTask, getTaskDetail, updateTask } from '@/api/task'
import { useUserStore } from '@/stores/user'
import StepForm from '@/components/StepForm.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const currentStep = ref(0)

// 步骤配置
const steps = [
  { title: '基本信息', component: 'BasicInfo' },
  { title: '上传文件', component: 'UploadFiles' },
  { title: '完成', component: 'Finish' }
]

type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

interface TaskForm {
  name: string
  description: string
  priority: Priority
  dueTime: string
  tags: string[]
  members: string[]
  attachments: any[]
}

// 表单数据
const taskForm = reactive<TaskForm>({
  name: '',
  description: '',
  priority: 'LOW',
  dueTime: '',
  tags: [],
  members: [],
  attachments: []
})

// 标签选项
const tagOptions = ref(['Bug', 'Login Page', 'Feature Development', 'Performance Optimization', 'UI Design', 'Documentation'])

// 成员选项
const memberOptions = [
  { value: 'John', label: 'John' },
  { value: 'Amy', label: 'Amy' },
  { value: 'Tom', label: 'Tom' },
  { value: 'Jack', label: 'Jack' }
]

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: 'Please enter the task name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length must be between 2 and 50 characters', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: 'Please select the priority', trigger: 'change' }
  ],
  dueTime: [
    { required: true, message: 'Please select the due time', trigger: 'change' }
  ],
  members: [
    { required: true, message: 'Please select at least one member', trigger: 'change' }
  ]
}

// 处理文件变更
const handleFileChange = (file: any) => {
  taskForm.attachments.push(file)
}

// 移除文件
const removeFile = (index: number) => {
  taskForm.attachments.splice(index, 1)
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
    } catch (error) {
      ElMessage.error('Please complete the required information')
      return
    }
  } else if (currentStep.value === 1) {
    // 处理文件上传
  } else if (currentStep.value === 2) {
    // 提交表单
    try {
      loading.value = true
      const taskId = route.params.id as string
      if (taskId) {
        await updateTask(taskId, taskForm)
        ElMessage.success('Update successfully')
      } else {
        await createTask(taskForm)
        ElMessage.success('Create successfully')
      }
    } catch (error: any) {
      console.error('Operation failed:', error)
      ElMessage.error(error.message || 'Operation failed')
      return
    } finally {
      loading.value = false
    }
  }
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
    priority: 'LOW',
    dueTime: '',
    tags: [],
    members: [],
    attachments: []
  })
}

onMounted(() => {
  // 检查用户权限
  if (userStore.userInfo?.role !== 1) {
    ElMessage.error('只有项目负责人可以创建任务')
    router.push('/list')
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
</style> 