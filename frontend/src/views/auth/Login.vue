<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="title">Welcome to login</h2>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="0"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="User"
            placeholder="Please enter username"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            prefix-icon="Lock"
            type="password"
            placeholder="Please enter password"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-radio-group v-model="loginForm.role" class="role-select">
            <el-radio :label="0">Normal member</el-radio>
            <el-radio :label="1">Project manager</el-radio>
            <el-radio :label="2">Admin</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-button" @click="handleLogin">
            Login
          </el-button>
        </el-form-item>

        <div class="register-link">
          No account yet?
          <router-link to="/register">Register now</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const rememberMe = ref(false)

const loginForm = ref({
  username: '',
  password: '',
  role: 0
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' }
  ],
  role: [
    { required: true, message: 'Please select role', trigger: 'change' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userStore.login(
          loginForm.value.username,
          loginForm.value.password,
          loginForm.value.role
        )
        
        if (rememberMe.value) {
          localStorage.setItem('rememberedUsername', loginForm.value.username)
        } else {
          localStorage.removeItem('rememberedUsername')
        }
      } catch (error: any) {
        ElMessage.error(error.message || 'Login failed')
      }
    }
  })
}

onMounted(() => {
  // 如果之前记住了用户名，自动填充
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginForm.value.username = rememberedUsername
    rememberMe.value = true
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  color: #333;
  font-size: 28px;
  margin-bottom: 30px;
  font-weight: 600;
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  .el-input {
    --el-input-hover-border: #66a6ff;
    --el-input-focus-border: #66a6ff;
  }
}

.role-select {
  width: 100%;
  display: flex;
  justify-content: space-around;
  
  .el-radio {
    margin-right: 0;
  }
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
  border: none;
  
  &:hover {
    opacity: 0.9;
  }
}

.register-link {
  text-align: center;
  margin-top: 16px;
  color: #666;
  
  a {
    color: #66a6ff;
    text-decoration: none;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  
  &:hover {
    box-shadow: 0 0 0 1px #66a6ff inset;
  }
  
  &.is-focus {
    box-shadow: 0 0 0 1px #66a6ff inset;
  }
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #66a6ff;
  border-color: #66a6ff;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #66a6ff;
}

:deep(.el-radio__inner:hover) {
  border-color: #66a6ff;
}
</style> 