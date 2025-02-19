<template>
  <div class="project-form-container">
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
          :model="projectForm"
          :rules="formRules"
          label-width="100px"
          class="project-form"
        >
          <!-- 基本信息分组 -->
          <div class="form-group">
            <div class="group-title">基本信息</div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目名称" prop="name" required>
                  <el-input 
                    v-model="projectForm.name" 
                    placeholder="请输入项目名称"
                    maxlength="50"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="优先级" prop="priority" required>
                  <el-select
                    v-model="projectForm.priority"
                    placeholder="请选择优先级"
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
                <el-form-item label="项目状态" prop="status" required>
                  <el-select
                    v-model="projectForm.status"
                    placeholder="请选择项目状态"
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
            <div class="group-title">时间设置</div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开始时间" prop="startTime" required>
                  <el-date-picker
                    v-model="projectForm.startTime"
                    type="date"
                    placeholder="选择开始日期"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束时间" prop="endTime" required>
                  <el-date-picker
                    v-model="projectForm.endTime"
                    type="date"
                    placeholder="选择结束日期"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                    :disabled-date="disabledEndDate"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 项目描述分组 -->
          <div class="form-group">
            <div class="group-title">项目描述</div>
            <el-form-item prop="description">
              <el-input
                v-model="projectForm.description"
                type="textarea"
                :rows="4"
                placeholder="请输入项目描述"
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
              >
                <el-option
                  v-for="member in memberOptions"
                  :key="member.value"
                  :label="member.label"
                  :value="member.value"
                >
                  <div class="member-option">
                    <el-avatar :size="24">{{ member.label.charAt(0) }}</el-avatar>
                    <span>{{ member.label }}</span>
                  </div>
                </el-option>
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
              点击或拖拽文件到此处上传
              <em>支持多个文件同时上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持任意类型文件，单个文件不超过50MB
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
                <span class="file-name">{{ file.name }}</span>
                <span class="file-size">{{ formatFileSize(file.size) }}</span>
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
          <div class="success-icon animate__animated animate__bounceIn">
            <el-icon :size="64" color="#67C23A"><CircleCheckFilled /></el-icon>
          </div>
          <h2 class="animate__animated animate__fadeInUp">创建成功</h2>
          <p class="success-desc animate__animated animate__fadeInUp animate__delay-1s">
            项目已成功创建，您可以开始添加任务了
          </p>
          <div class="action-buttons animate__animated animate__fadeInUp animate__delay-2s">
            <el-button type="primary" @click="handleCheckProject">查看项目</el-button>
            <el-button @click="handleCreateAgain">继续创建</el-button>
          </div>
        </div>
      </template>
    </StepForm>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { CircleCheckFilled, UploadFilled, Delete, Document } from '@element-plus/icons-vue'
import { createProject, getProjectDetail, updateProject } from '@/api/project'
import { useUserStore } from '@/stores/user'
import StepForm from '@/components/StepForm.vue'
import dayjs from 'dayjs'
import { 
  getStatusOptions, 
  getPriorityOptions,
  type ProjectStatus,
  type ProjectPriority
} from '@/utils/status'
import 'animate.css'

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
  { title: '基本信息', component: 'BasicInfo' },
  { title: '上传文件', component: 'UploadFiles' },
  { title: '完成', component: 'Finish' }
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
const memberOptions = [
  { value: 'John', label: 'John' },
  { value: 'Amy', label: 'Amy' },
  { value: 'Tom', label: 'Tom' },
  { value: 'Jack', label: 'Jack' }
]

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '不能超过 200 个字符', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

// 禁用结束日期
const disabledEndDate = (time: Date) => {
  if (!projectForm.startTime) return false
  return dayjs(time).isBefore(dayjs(projectForm.startTime))
}

// 处理文件变更
const handleFileChange = (file: any) => {
  projectForm.attachments.push(file)
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
      const projectId = Number(route.params.id)
      if (projectId) {
        const { data } = await updateProject(projectId, projectForm)
        if (data.code === 0) {
          ElMessage.success('更新成功')
        } else {
          ElMessage.error(data.message || '更新失败')
          return
        }
      } else {
        const { data } = await createProject(projectForm)
        if (data.code === 0) {
          ElMessage.success('创建成功')
        } else {
          ElMessage.error(data.message || '创建失败')
          return
        }
      }
      router.push('/project/list')
    } catch (error: any) {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
      return
    } finally {
      loading.value = false
    }
  }
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
    projectForm.name = data.data.name
    projectForm.description = data.data.description
    projectForm.startTime = data.data.startTime
    projectForm.endTime = data.data.endTime
    projectForm.members = data.data.members
    projectForm.attachments = data.data.attachments || []
    projectForm.status = data.data.status
    projectForm.priority = data.data.priority
  } catch (error) {
    console.error('获取项目详情失败:', error)
    ElMessage.error('获取项目详情失败')
    router.back()
  } finally {
    loading.value = false
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
  const index = projectForm.attachments.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    projectForm.attachments.splice(index, 1)
  }
}

onMounted(async () => {
  // 检查用户权限
  if (userStore.userInfo?.role !== 1) {
    ElMessage.error('只有项目负责人可以创建或编辑项目')
    router.push('/project/list')
    return
  }

  const projectId = route.params.id as string
  if (projectId) {
    await fetchProjectDetail(Number(projectId))
  }
})
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
</style> 