<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="title">Register</h2>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        label-width="0"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            prefix-icon="User"
            placeholder="Please enter username"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            prefix-icon="Lock"
            type="password"
            placeholder="Please enter password"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            prefix-icon="Lock"
            type="password"
            placeholder="Please confirm password"
            show-password
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            prefix-icon="Message"
            placeholder="Please enter email"
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            prefix-icon="Phone"
            placeholder="Please enter phone number"
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-radio-group v-model="registerForm.role" class="role-select">
            <el-radio :label="0">Member</el-radio>
            <el-radio :label="1">Leader</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="register-button" @click="handleRegister">
            Register
          </el-button>
        </el-form-item>

        <div class="login-link">
          Already have an account?
          <router-link to="/login">Login now</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  role: 0
})

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('Please enter password again'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('The two passwords do not match!'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 20, message: 'Length between 3 and 20 characters', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, max: 20, message: 'Length between 6 and 20 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter email address', trigger: 'blur' },
    { type: 'email', message: 'Please enter correct email address', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: 'Please enter phone number', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: 'Please enter correct phone number', trigger: 'blur' }
  ],
  role: [
    { required: true, message: 'Please select role', trigger: 'change' }
  ]
})

const registerFormRef = ref<FormInstance>()

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const { confirmPassword, ...registerData } = registerForm.value
        await register(registerData)
        ElMessage.success('Registration successful')
        router.push('/login')
      } catch (error: any) {
        ElMessage.error(error.message || 'Registration failed')
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
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

.register-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  .el-input {
    --el-input-hover-border: #764ba2;
    --el-input-focus-border: #764ba2;
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

.register-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  
  &:hover {
    opacity: 0.9;
  }
}

.login-link {
  text-align: center;
  margin-top: 16px;
  color: #666;
  
  a {
    color: #764ba2;
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
    box-shadow: 0 0 0 1px #764ba2 inset;
  }
  
  &.is-focus {
    box-shadow: 0 0 0 1px #764ba2 inset;
  }
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #764ba2;
  border-color: #764ba2;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #764ba2;
}

:deep(.el-radio__inner:hover) {
  border-color: #764ba2;
}
</style> 