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
          <el-form-item label="任务名称" prop="name" required>
            <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
          </el-form-item>

          <el-form-item label="优先级" prop="priority" required>
            <el-select v-model="taskForm.priority" placeholder="请选择优先级" style="width: 100%">
              <el-option label="低" value="LOW" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="高" value="HIGH" />
              <el-option label="紧急" value="CRITICAL" />
            </el-select>
          </el-form-item>

          <el-form-item label="截止时间" prop="dueTime" required>
            <el-date-picker
              v-model="taskForm.dueTime"
              type="datetime"
              placeholder="请选择截止时间"
              style="width: 100%"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>

          <el-form-item label="标签" prop="tags">
            <el-select
              v-model="taskForm.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或创建标签"
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

          <el-form-item label="任务描述" prop="description">
            <el-input
              v-model="taskForm.description"
              type="textarea"
              :rows="4"
              placeholder="请描述任务"
            />
          </el-form-item>

          <el-form-item label="参与成员" prop="members">
            <el-select
              v-model="taskForm.members"
              multiple
              filterable
              placeholder="请选择参与成员"
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
          <h2>创建成功</h2>
          <div class="action-buttons">
            <el-button type="primary" @click="handleCheckTask">查看任务</el-button>
            <el-button @click="handleCreateAgain">继续创建</el-button>
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

// 表单数据
const taskForm = reactive({
  name: '',
  description: '',
  priority: '',
  dueTime: '',
  tags: [] as string[],
  members: [] as string[],
  attachments: [] as any[]
})

// 标签选项
const tagOptions = ref(['Bug', 'Login Page', '功能开发', '性能优化', 'UI设计', '文档'])

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
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  dueTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ],
  members: [
    { required: true, message: '请选择至少一名参与成员', trigger: 'change' }
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
      ElMessage.error('请完善必填信息')
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
        ElMessage.success('更新成功')
      } else {
        await createTask(taskForm)
        ElMessage.success('创建成功')
      }
    } catch (error: any) {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
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
    priority: '',
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