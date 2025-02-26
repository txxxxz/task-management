<template>
  <div class="user-management">
    <div class="header">
      <h2>User Management</h2>
      <el-button type="primary" @click="showBatchRegisterDialog">Batch Register</el-button>
    </div>

    <!-- 用户列表表格 -->
    <el-table :data="users" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="Username" width="120" />
      <el-table-column prop="email" label="Email" width="180" />
      <el-table-column prop="role" label="Role" width="120">
        <template #default="{ row }">
          <el-select v-model="row.role" @change="handleRoleChange(row)">
            <el-option label="Admin" value="admin" />
            <el-option label="User" value="user" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status" width="120">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            @change="handleStatusChange(row)"
            :active-text="'Enable'"
            :inactive-text="'Disable'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" width="180" />
      <el-table-column label="Actions" width="120">
        <template #default="{ row }">
          <el-button type="text" @click="viewUserDetails(row)">View Details</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批量注册对话框 -->
    <el-dialog v-model="batchRegisterVisible" title="Batch Register Users" width="600px">
      <el-form :model="batchRegisterForm" label-width="100px">
        <el-form-item label="User List">
          <el-upload
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
            accept=".xlsx,.xls"
          >
            <el-button type="primary">Select Excel File</el-button>
            <template #tip>
              <div class="el-upload__tip">
                Please upload an Excel file containing username, email, password, role, and other information
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchRegisterVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitBatchRegister">Confirm</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="userDetailsVisible" title="User Details" width="500px">
      <div v-if="selectedUser">
        <p><strong>ID: </strong>{{ selectedUser.id }}</p>
        <p><strong>Username: </strong>{{ selectedUser.username }}</p>
        <p><strong>Email: </strong>{{ selectedUser.email }}</p>
        <p><strong>Role: </strong>{{ selectedUser.role }}</p>
        <p><strong>Status: </strong>{{ selectedUser.status ? 'Enabled' : 'Disabled' }}</p>
        <p><strong>Create Time: </strong>{{ selectedUser.createTime }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllUsers, batchRegisterUsers, updateUserStatus, updateUserRole, getUserDetails } from '@/api/user'
import type { UserInfo } from '@/types/user'
import * as XLSX from 'xlsx'
import type { UploadFile } from 'element-plus'

const users = ref<UserInfo[]>([])
const batchRegisterVisible = ref(false)
const userDetailsVisible = ref(false)
const selectedUser = ref<UserInfo | null>(null)
const batchRegisterForm = ref({
  file: null as File | null
})

interface BatchRegisterUser {
  username: string
  email: string
  password: string
  role: string
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    const response = await getAllUsers()
    users.value = response.data
  } catch (error) {
    ElMessage.error('Failed to get user list')
  }
}

// 处理角色变更
const handleRoleChange = async (user: UserInfo) => {
  try {
    await updateUserRole(user.id, user.role.toString())
    ElMessage.success('Role updated successfully')
  } catch (error) {
    ElMessage.error('Role update failed')
  }
}

// 处理状态变更
const handleStatusChange = async (user: UserInfo) => {
  try {
    await updateUserStatus(user.id, user.status)
    ElMessage.success('Status updated successfully')
  } catch (error) {
    ElMessage.error('Status update failed')
  }
}

// 查看用户详情
const viewUserDetails = async (user: UserInfo) => {
  try {
    const response = await getUserDetails(user.id)
    selectedUser.value = response.data
    userDetailsVisible.value = true
  } catch (error) {
    ElMessage.error('Failed to get user details')
  }
}

// 显示批量注册对话框
const showBatchRegisterDialog = () => {
  batchRegisterVisible.value = true
}

// 处理文件上传
const handleFileChange = (uploadFile: UploadFile) => {
  batchRegisterForm.value.file = uploadFile.raw as File
}

// 提交批量注册
const submitBatchRegister = async () => {
  if (!batchRegisterForm.value.file) {
    ElMessage.warning('Please select an Excel file')
    return
  }

  try {
    const reader = new FileReader()
    reader.onload = async (e) => {
      const data = new Uint8Array(e.target?.result as ArrayBuffer)
      const workbook = XLSX.read(data, { type: 'array' })
      const worksheet = workbook.Sheets[workbook.SheetNames[0]]
      const users = XLSX.utils.sheet_to_json(worksheet) as BatchRegisterUser[]

      await batchRegisterUsers({ users })
      ElMessage.success('Batch registration successful')
      batchRegisterVisible.value = false
      fetchUsers()
    }
    reader.readAsArrayBuffer(batchRegisterForm.value.file)
  } catch (error) {
    ElMessage.error('Batch registration failed')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 