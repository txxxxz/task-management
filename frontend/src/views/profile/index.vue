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
              <div class="stat-label">加入时间</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧信息表单 -->
      <el-col :span="16">
        <el-card class="info-card" shadow="hover">
          <el-tabs v-model="activeTab" class="custom-tabs">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input 
                    v-model="basicForm.username" 
                    placeholder="请输入用户名"
                    :prefix-icon="User"
                  />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input 
                    v-model="basicForm.email" 
                    placeholder="请输入邮箱"
                    :prefix-icon="Message"
                  />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input 
                    v-model="basicForm.phone" 
                    placeholder="请输入手机号"
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
                    保存修改
                  </el-button>
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
                class="profile-form"
              >
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    show-password
                    placeholder="请输入当前密码"
                    :prefix-icon="Lock"
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                    placeholder="请输入新密码"
                    :prefix-icon="Lock"
                  />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="请再次输入新密码"
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, changePassword } from '@/api/user'
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
  id: number;
  username: string;
  email: string;
  phone: string;
  avatar?: string;  // 改为可选属性
  role: number;
  status: number;
  createTime: string;
  updateTime: string;
}

// 修改 userInfo 的计算属性类型
const userInfo = computed<UserInfo>(() => userStore.userInfo || {
  id: 0,
  username: '',
  email: '',
  phone: '',
  avatar: '',
  role: 0,
  status: 0,
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
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
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
    ElMessage.success('头像更新成功')
  } else {
    ElMessage.error(response.msg || '头像更新失败')
  }
}

// 头像上传失败
const handleAvatarError = (error: any) => {
  console.error('Upload error:', error)
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