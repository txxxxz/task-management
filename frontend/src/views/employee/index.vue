<template>
  <div class="employee-container">
    <div class="page-header">
      <h2 class="page-title">Employee Management</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="Name">
          <el-input v-model="queryParams.name" placeholder="Please enter the name" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="Email">
          <el-input v-model="queryParams.email" placeholder="Please enter the email" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="Role">
          <el-select v-model="queryParams.role" placeholder="Please select the role" clearable>
            <el-option label="Admin" :value="2" />
            <el-option label="Project Leader" :value="1" />
            <el-option label="Member" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="queryParams.status" placeholder="Please select the status" clearable>
            <el-option label="Enabled" :value="true" />
            <el-option label="Disabled" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">Search</el-button>
          <el-button @click="resetQuery" :icon="RefreshRight">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮和表格 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-operations">
        <el-button type="primary" @click="handleAdd" :icon="Plus" class="add-button">Add Employee</el-button>
      </div>

      <el-table v-loading="loading" :data="employeeList" border style="width: 100%">
        <el-table-column prop="username" label="Name" min-width="100" />
        <el-table-column prop="email" label="Email" min-width="180" />
        <el-table-column prop="phone" label="Phone" min-width="150" />
        <el-table-column label="Role" min-width="150">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" effect="dark" round>
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Status" min-width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="true"
              :inactive-value="false"
              @change="() => handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="Create Time" min-width="180" />
        <el-table-column label="Operation" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" :icon="Edit">Edit</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :icon="Delete">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="employee-form"
    >
      <el-form-item label="Name" prop="username">
        <el-input v-model="form.username" placeholder="Please enter the name" />
      </el-form-item>
      <el-form-item label="Email" prop="email">
        <el-input v-model="form.email" placeholder="Please enter the email" />
      </el-form-item>
      <el-form-item label="Phone" prop="phone">
        <el-input v-model="form.phone" placeholder="Please enter the phone" />
      </el-form-item>
      <el-form-item label="Role" prop="role">
        <el-input v-model="form.role" placeholder="Please enter the role" />
      </el-form-item>
      <el-form-item label="Password" prop="password" v-if="isAdd">
        <el-input v-model="form.password" type="password" placeholder="Please enter the password" show-password />
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-switch
          v-model="form.status"
          :active-value="true"
          :inactive-value="false"
          />
        </el-form-item>
        <el-form-item label="Role" prop="role">
          <el-select v-model="form.role" placeholder="Please select the role">
            <el-option label="Admin" :value="2" />
            <el-option label="Project Leader" :value="1" />
            <el-option label="Member" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="Password" prop="password" v-if="isAdd">
          <el-input v-model="form.password" type="password" placeholder="Please enter the password" show-password />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="true"
            :inactive-value="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">Confirm</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Search,
  RefreshRight,
  Plus,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import {
  getEmployeeList,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  toggleEmployeeStatus
} from '@/api/employee'
import { Employee, EmployeeQuery, EmployeeForm } from '@/types/employee'

// 定义接口
interface LocalEmployee {
  id: string | number;
  username: string;
  email: string;
  phone: string;
  role: number;
  status: boolean;
  createTime: string;
  updateTime: string;
}

interface QueryParams {
  name?: string;
  email?: string;
  role?: string;
  status?: boolean;
  page: number;
  pageSize: number;
}

// 数据定义
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const employeeList = ref<LocalEmployee[]>([])
const total = ref(0)
const formRef = ref<FormInstance>()
const isAdd = ref(true)

// 查询参数
const queryParams = reactive<QueryParams>({
  page: 1,
  pageSize: 10
})

// 表单数据
const form = reactive({
  id: '',
  username: '',
  email: '',
  phone: '',
  role: 0,
  status: true,
  password: ''
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: 'Please enter the name', trigger: 'blur' },
    { min: 2, max: 20, message: 'Length between 2 and 20 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter the email', trigger: 'blur' },
    { type: 'email', message: 'Please enter the correct email address', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: 'Please enter the phone number', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: 'Please enter the correct phone number', trigger: 'blur' }
  ],
  role: [
    { required: true, message: 'Please select the role', trigger: 'change' }
  ],
  password: [
    { required: true, message: 'Please enter the password', trigger: 'blur' },
    { min: 6, max: 20, message: 'Length between 6 and 20 characters', trigger: 'blur' }
  ]
}

// 获取角色标签类型
const getRoleTagType = (role: number) => {
  const types: Record<number, 'info' | 'warning' | 'danger'> = {
    0: 'info',
    1: 'warning',
    2: 'danger'
  }
  return types[role] || 'info'
}

// 获取角色文本
const getRoleText = (role: number) => {
  const texts = {
    0: '成员',
    1: '组长',
    2: '管理员'
  }
  return texts[role as keyof typeof texts] || '未知'
}

// 获取员工列表
const fetchEmployeeList = async () => {
  loading.value = true
  try {
    const response = await getEmployeeList(queryParams as EmployeeQuery)
    if (response && response.data) {
      // 类型转换
      employeeList.value = (response.data.items || []).map(item => ({
        id: item.id,
        username: item.name,
        email: item.email,
        phone: item.phone,
        role: getRoleNumber(item.role),
        status: item.status,
        createTime: item.createdAt,
        updateTime: item.createdAt
      }))
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  } finally {
    loading.value = false
  }
}

// 获取角色数字值
const getRoleNumber = (role: string): number => {
  const roleMap: Record<string, number> = {
    'Admin': 2,
    'Leader': 1,
    'Member': 0
  }
  return roleMap[role] || 0
}

// 获取角色字符串
const getRoleString = (role: number): string => {
  const roleMap: Record<number, string> = {
    2: 'Admin',
    1: 'Leader',
    0: 'Member'
  }
  return roleMap[role] || 'Member'
}

// 搜索处理
const handleSearch = () => {
  queryParams.page = 1
  fetchEmployeeList()
}

// 重置查询
const resetQuery = () => {
  queryParams.name = undefined
  queryParams.email = undefined
  queryParams.role = undefined
  queryParams.status = undefined
  queryParams.page = 1
  fetchEmployeeList()
}

// 添加员工
const handleAdd = () => {
  isAdd.value = true
  dialogTitle.value = 'Add Employee'
  resetForm()
  dialogVisible.value = true
}

// 编辑员工
const handleEdit = (row: LocalEmployee) => {
  isAdd.value = false
  dialogTitle.value = 'Edit Employee'
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.id = ''
  form.username = ''
  form.email = ''
  form.phone = ''
  form.role = 0
  form.status = true
  form.password = ''
}

// 删除员工
const handleDelete = (row: LocalEmployee) => {
  ElMessageBox.confirm('Are you sure you want to delete this employee? This action is irreversible', 'Warning', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteEmployee(Number(row.id))
      ElMessage.success('Delete successfully')
      fetchEmployeeList()
    } catch (error) {
      console.error('Delete employee failed:', error)
      ElMessage.error('Delete employee failed')
    }
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 处理状态改变
const handleStatusChange = async (row: LocalEmployee) => {
  try {
    await toggleEmployeeStatus(Number(row.id), row.status)
    ElMessage.success('Status updated successfully')
  } catch (error) {
    console.error('Update employee status failed:', error)
    ElMessage.error('Update employee status failed')
    row.status = !row.status // 恢复状态
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isAdd.value) {
          // 将表单数据转换为符合API要求的格式
          const employeeData = {
            username: form.username,
            email: form.email,
            phone: form.phone,
            role: String(form.role), // 转为字符串
            status: form.status,
            password: form.password
          }
          await createEmployee(employeeData as any)
          ElMessage.success('Add employee successfully')
        } else {
          // 更新员工信息
          const updateData = {
            username: form.username,
            email: form.email,
            phone: form.phone,
            role: String(form.role), // 转为字符串
            status: form.status
          }
          await updateEmployee(Number(form.id), updateData as any)
          ElMessage.success('Update employee successfully')
        }
        dialogVisible.value = false
        fetchEmployeeList()
      } catch (error) {
        console.error('Save employee information failed:', error)
        ElMessage.error('Save employee information failed')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 分页处理
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  fetchEmployeeList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchEmployeeList()
}

onMounted(() => {
  fetchEmployeeList()
})
</script>

<style scoped>
.employee-container {
  padding: 20px;
  min-height: calc(100vh - 120px);
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #2c3e50;
  font-weight: 600;
  margin: 0;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 10px;
}

.table-card {
  border-radius: 10px;
}

.table-operations {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
}

.add-button {
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
  border: none;
  padding: 10px 20px;
  transition: all 0.3s ease;
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.4);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.employee-form .el-input,
.employee-form .el-select {
  width: 100%;
}

:deep(.el-tag) {
  padding: 4px 10px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header-wrapper) {
  background-color: var(--el-color-primary-light-9);
}

:deep(.el-table th) {
  background-color: var(--el-color-primary-light-9);
}

:deep(.el-table .el-table__cell) {
  padding: 12px 0;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #eee;
  padding: 15px 20px;
}

:deep(.el-dialog__body) {
  padding: 20px 30px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #eee;
  padding: 15px 20px;
}
</style> 