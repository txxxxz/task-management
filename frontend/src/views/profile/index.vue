<template>
  <div class="profile-container">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>

    <el-row :gutter="30">
      <!-- 左侧头像卡片 -->
      <el-col :span="8">
        <el-card class="profile-card" shadow="hover">
          <div class="profile-header">
            <div class="avatar-wrapper">
              <el-avatar 
                :size="150" 
                :src="userInfo.avatar || defaultAvatar" 
                class="profile-avatar"
              />
              <div class="avatar-overlay">
                <el-upload
                  class="avatar-uploader"
                  action="/api/auth/user/avatar"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleAvatarError"
                  name="file"
                  :data="{ userId: userInfo.id }"
                >
                  <el-button type="primary" class="upload-btn" round>
                    <el-icon class="upload-icon"><Upload /></el-icon>
                    更换头像
                  </el-button>
                </el-upload>
              </div>
            </div>
            <h3 class="profile-name">{{ userInfo.username }}</h3>
            <div class="profile-role">
              <el-tag :type="getRoleType(userInfo.role)" effect="dark" round>
                <el-icon><User /></el-icon>
                {{ getRoleText(userInfo.role) }}
              </el-tag>
            </div>
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <div class="stat-value">{{ formatDate(userInfo.createTime) }}</div>
              <div class="stat-label">Join Time</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧信息表单 -->
      <el-col :span="16">
        <el-card class="info-card" shadow="hover">
          <el-tabs v-model="activeTab" class="custom-tabs">
            <!-- 基本信息 -->
            <el-tab-pane label="Basic Information" name="basic">
              <el-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="Username" prop="username">
                  <el-input 
                    v-model="basicForm.username" 
                    placeholder="Please enter the username"
                    :prefix-icon="User"
                  />
                </el-form-item>
                <el-form-item label="Email" prop="email">
                  <el-input 
                    v-model="basicForm.email" 
                    placeholder="Please enter the email"
                    :prefix-icon="Message"
                  />
                </el-form-item>
                <el-form-item label="Phone" prop="phone">
                  <el-input 
                    v-model="basicForm.phone" 
                    placeholder="Please enter the phone number"
                    :prefix-icon="Phone"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="handleUpdateBasic"
                    class="submit-btn"
                    :icon="Check"
                    round
                  >
                    Save Changes
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="Change Password" name="password">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="Current Password" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    show-password
                    placeholder="Please enter the current password"
                    :prefix-icon="Lock"
                  />
                </el-form-item>
                <el-form-item label="New Password" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                    placeholder="Please enter the new password"
                    :prefix-icon="Lock"
                  />
                </el-form-item>
                <el-form-item label="Confirm New Password" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="Please enter the new password again"
                    :prefix-icon="Lock"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="handleUpdatePassword"
                    class="submit-btn"
                    :icon="Key"
                    round
                  >
                    Change Password
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { updateUserInfo, changePassword } from '../../api/user'
import { 
  User, 
  Message, 
  Phone, 
  Lock, 
  Key, 
  Check, 
  Upload 
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('basic')
const basicFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 在 script 开始处添加 UserInfo 接口定义
interface UserInfo {
  id: string;
  username: string;
  email: string;
  phone: string;
  avatar?: string;  // 改为可选属性
  role: number;
  status: boolean;  // 修改为 boolean 类型，与 userStore 中保持一致
  createTime: string;
  updateTime: string;
}

// 修改 userInfo 的计算属性类型
const userInfo = computed<UserInfo>(() => userStore.userInfo || {
  id: '',
  username: '',
  email: '',
  phone: '',
  avatar: '',
  role: 0,
  status: false,  // 修改为 boolean 默认值
  createTime: '',
  updateTime: ''
})

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

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
    { required: true, message: 'Please enter the username', trigger: 'blur' },
    { min: 3, max: 20, message: 'Length must be between 3 and 20 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter the email address', trigger: 'blur' },
    { type: 'email', message: 'Please enter the correct email address', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: 'Please enter the phone number', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: 'Please enter the correct phone number', trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: 'Please enter the current password', trigger: 'blur' },
    { min: 6, max: 20, message: 'Length must be between 6 and 20 characters', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: 'Please enter the new password', trigger: 'blur' },
    { min: 6, max: 20, message: 'Length must be between 6 and 20 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please enter the new password again', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === '') {
          callback(new Error('Please enter the new password again'))
        } else if (value !== passwordForm.newPassword) {
          callback(new Error('The two passwords are inconsistent!'))
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
    0: 'Member',
    1: 'Project Leader',
    2: 'Admin'
  }
  return texts[role as keyof typeof texts] || 'Unknown role'
}

// 头像上传前的验证
const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('The avatar can only be JPG or PNG format!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('The avatar size cannot exceed 2MB!')
    return false
  }

  return true
}

// 头像上传成功
const handleAvatarSuccess = (response: any) => {
  console.log('Upload response:', response)
  if (response.code === 1) {
    userStore.setUserInfo({
      ...userInfo.value,
      avatar: response.data
    })
    ElMessage.success('Avatar updated successfully')
  } else {
    ElMessage.error(response.msg || 'Avatar update failed')
  }
}

// 头像上传失败
const handleAvatarError = (error: any) => {
  console.error('Upload error:', error)
  ElMessage.error('Avatar upload failed, please try again')
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
    ElMessage.success('Personal information updated successfully')
  } catch (error: any) {
    ElMessage.error(error.message || 'Update failed')
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
    ElMessage.success('Password changed successfully')
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error: any) {
    ElMessage.error(error.message || 'Password change failed')
  }
}

// 初始化表单数据
const initFormData = () => {
  if (userInfo.value) {
    basicForm.username = userInfo.value.username
    basicForm.email = userInfo.value.email
    basicForm.phone = userInfo.value.phone
  }
}

// 监听userInfo变化，更新表单数据
watch(() => userInfo.value, (newVal) => {
  if (newVal) {
    basicForm.username = newVal.username
    basicForm.email = newVal.email
    basicForm.phone = newVal.phone
  }
}, { immediate: true })

// 在script部分添加日期格式化函数
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  min-height: calc(100vh - 120px);
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 24px;
  color: #2c3e50;
  font-weight: 600;
  margin: 0;
}

.profile-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.profile-header {
  text-align: center;
  padding: 30px 20px;
}

.avatar-wrapper {
  position: relative;
  width: 150px;
  height: 150px;
  margin: 0 auto 20px;
  border-radius: 50%;
  overflow: hidden;
}

.profile-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.upload-btn {
  background: transparent;
  border: 2px solid #fff;
  color: #fff;
}

.profile-name {
  font-size: 24px;
  color: #2c3e50;
  margin: 15px 0;
  font-weight: 600;
}

.profile-role {
  margin: 10px 0;
}

.profile-stats {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}

.stat-item {
  text-align: center;
  padding: 0 20px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.info-card {
  border-radius: 15px;
  min-height: 500px;
}

.custom-tabs {
  padding: 20px;
}

.profile-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px 0;
}

.submit-btn {
  width: 140px;
  height: 40px;
  font-size: 16px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

:deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 3px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 25px;
}

:deep(.el-tabs__item.is-active) {
  font-weight: 600;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

/* 添加一些新的样式 */
.upload-icon {
  margin-right: 5px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.4);
}

:deep(.el-tabs__item:hover) {
  color: var(--el-color-primary);
}

.profile-card:hover, .info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
</style> 