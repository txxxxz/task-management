<template>
  <div class="profile">
    <div class="page-header">
      <h2 class="page-title">个人信息</h2>
    </div>

    <el-row :gutter="20">
      <!-- 左侧头像卡片 -->
      <el-col :span="8">
        <el-card class="avatar-card">
          <div class="avatar-wrapper">
            <el-avatar :size="120" :src="userInfo.avatar || defaultAvatar" />
            <div class="avatar-actions">
              <el-upload
                class="avatar-uploader"
                action="/api/auth/user/avatar"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :on-success="handleAvatarSuccess"
                :on-error="handleAvatarError"
                :with-credentials="true"
              >
                <el-button type="primary" size="small">更换头像</el-button>
              </el-upload>
            </div>
          </div>
          <div class="user-role">
            <el-tag :type="getRoleType(userInfo.role)">{{ getRoleText(userInfo.role) }}</el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧信息表单 -->
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicRules"
                label-width="100px"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="basicForm.username" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="basicForm.email" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="basicForm.phone" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateBasic">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
              >
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdatePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, changePassword } from '@/api/user'

const userStore = useUserStore()
const activeTab = ref('basic')
const basicFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 用户信息
const userInfo = computed(() => userStore.userInfo || {})
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 上传头像相关
const uploadHeaders = computed(() => ({
  Authorization: userStore.token || ''
}))

// 基本信息表单
const basicForm = reactive({
  username: '',
  email: '',
  phone: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 基本信息验证规则
const basicRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'))
        } else if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致!'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取角色类型
const getRoleType = (role: number) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'danger'
  } as const
  return types[role as keyof typeof types] || 'info'
}

// 获取角色文本
const getRoleText = (role: number) => {
  const texts = {
    0: '普通成员',
    1: '项目负责人',
    2: '管理员'
  }
  return texts[role as keyof typeof texts] || '未知角色'
}

// 头像上传前的验证
const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }

  return isJPG && isLt2M
}

// 头像上传成功
const handleAvatarSuccess = (response: any) => {
  if (response.code === 1) {
    userStore.setUserInfo({
      ...userInfo.value,
      avatar: response.data
    })
    ElMessage.success('头像更新成功')
  } else {
    ElMessage.error(response.msg || '头像更新失败')
  }
}

// 头像上传失败
const handleAvatarError = () => {
  ElMessage.error('头像上传失败，请重试')
}

// 更新基本信息
const handleUpdateBasic = async () => {
  if (!basicFormRef.value) return
  
  try {
    await basicFormRef.value.validate()
    const response = await updateUserInfo(basicForm)
    userStore.setUserInfo({
      ...userInfo.value,
      ...basicForm
    })
    ElMessage.success('个人信息更新成功')
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  }
}

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error: any) {
    ElMessage.error(error.message || '密码修改失败')
  }
}

// 初始化表单数据
const initFormData = () => {
  if (userInfo.value) {
    basicForm.username = userInfo.value.username || ''
    basicForm.email = userInfo.value.email || ''
    basicForm.phone = userInfo.value.phone || ''
  }
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.profile {
  padding: 20px;
}

.avatar-card {
  text-align: center;
}

.avatar-wrapper {
  padding: 20px 0;
}

.avatar-actions {
  margin-top: 20px;
}

.user-role {
  margin-top: 16px;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-tabs__nav) {
  margin-bottom: 20px;
}
</style> 