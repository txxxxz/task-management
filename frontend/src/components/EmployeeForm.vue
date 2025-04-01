<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="80px"
    class="employee-form"
  >
    <el-form-item label="Name" prop="name">
      <el-input v-model="form.name" placeholder="Please enter the name" />
    </el-form-item>
    <el-form-item label="Email" prop="email">
      <el-input v-model="form.email" placeholder="Please enter the email" />
    </el-form-item>
    <el-form-item label="Phone" prop="phone">
      <el-input v-model="form.phone" placeholder="Please enter the phone number" />
    </el-form-item>
    <el-form-item label="Role" prop="role">
      <el-select v-model="form.role" placeholder="Please select the role">
        <el-option label="Admin" value="Admin" />
        <el-option label="Leader" value="Leader" />
        <el-option label="Member" value="Member" />
      </el-select>
    </el-form-item>
    <el-form-item label="状态" prop="status">
      <el-switch
        v-model="form.status"
        :active-value="true"
        :inactive-value="false"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitForm">确定</el-button>
      <el-button @click="$emit('cancel')">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import type { Employee, EmployeeForm } from '../types/employee'
import { createEmployee, updateEmployee } from '../api/employee'

const props = defineProps<{
  employee?: Partial<Employee>
}>()

const emit = defineEmits<{
  (e: 'success'): void
  (e: 'cancel'): void
}>()

const formRef = ref<FormInstance>()
const form = reactive<EmployeeForm>({
  name: '',
  email: '',
  phone: '',
  role: 'Member',
  status: true
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

watch(
  () => props.employee,
  (newVal) => {
    if (newVal) {
      Object.assign(form, newVal)
    }
  },
  { immediate: true }
)

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        if (props.employee?.id) {
          await updateEmployee(props.employee.id, form)
          ElMessage.success('更新成功')
        } else {
          await createEmployee(form)
          ElMessage.success('创建成功')
        }
        emit('success')
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      }
    } else {
      console.error('表单验证失败:', fields)
    }
  })
}
</script>

<style scoped>
.employee-form {
  padding: 20px;
}
</style> 