<template>
  <div class="create-task-container">
    <!-- 步骤导航 -->
    <el-steps :active="currentStep" finish-status="success" class="task-steps">
      <el-step title="基本信息" />
      <el-step title="上传文件" />
      <el-step title="完成" />
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
        <el-form-item label="任务名称" prop="title" required>
          <el-input v-model="taskForm.title" placeholder="请输入任务名称" />
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
            :reserve-keyword="false"
            placeholder="请选择标签或输入新标签"
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
                预设标签
              </span>
            </el-option>
            <template #empty>
              <div style="padding: 8px 12px;">
                <span style="color: var(--el-text-color-secondary);">
                  输入新标签名称并按回车键创建
                </span>
              </div>
            </template>
          </el-select>
          <div class="form-item-tip">
            提示：可以输入新的标签名称并按回车键创建
          </div>
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

    <!-- 步骤3：完成 -->
    <div v-show="currentStep === 2" class="finish-container">
      <div class="success-icon">
        <el-icon :size="64" color="#67C23A"><CircleCheckFilled /></el-icon>
      </div>
      <h2>创建成功</h2>
      <div class="action-buttons">
        <el-button type="primary" @click="handleCheckTask">查看任务</el-button>
        <el-button @click="handleCreateAgain">继续创建</el-button>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="form-buttons">
      <el-button v-if="currentStep > 0" @click="previousStep">上一步</el-button>
      <el-button 
        v-if="currentStep < 2" 
        type="primary" 
        @click="nextStep"
      >
        下一步
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, UploadFilled, Collection } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const currentStep = ref(0)

// 检查用户权限
onMounted(() => {
  if (userStore.userInfo?.role !== 1) {
    ElMessage.error('只有管理员可以创建任务')
    router.push('/list')
  }
})

// 表单数据
const taskForm = reactive({
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
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  dueTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && dayjs(value).isBefore(dayjs())) {
          callback(new Error('截止时间不能早于当前时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  members: [
    { required: true, message: '请选择至少一名参与成员', trigger: 'change' }
  ]
})

// 默认标签列表
const defaultTags = [
  'Bug',
  'Login Page',
  '功能开发',
  '性能优化',
  'UI设计',
  '文档'
]

// 标签选项（包括用户新建的标签）
const tagOptions = ref([...defaultTags])

// 判断是否为默认标签
const isDefaultTag = (tag: string) => defaultTags.includes(tag)

// 处理标签下拉框显示/隐藏
const handleTagDropdownToggle = (visible: boolean) => {
  if (visible) {
    // 当下拉框打开时，可以在这里从后端获取最新的标签列表
    console.log('标签下拉框打开')
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
const handleFileChange = (file: any) => {
  taskForm.attachments.push(file)
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
      ElMessage.error('请完善必填信息')
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