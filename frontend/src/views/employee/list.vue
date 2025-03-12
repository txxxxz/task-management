<template>
  <div class="employee-list">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" class="search-form" inline>
      <el-form-item label="Name">
        <el-input v-model="queryParams.username" placeholder="Please enter the name" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="Email">
        <el-input v-model="queryParams.email" placeholder="Please enter the email" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="Role">
        <el-select v-model="queryParams.role" placeholder="Please select the role" clearable>
          <el-option label="Admin" value="admin" />
          <el-option label="Leader" value="leader" />
          <el-option label="Member" value="member" />
        </el-select>
      </el-form-item>
      <el-form-item label="Status">
        <el-select v-model="queryParams.status" placeholder="Please select the status" clearable>
          <el-option label="Enabled" :value="true" />
          <el-option label="Disabled" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增员工</el-button>
    </div>

    <!-- 员工列表 -->
    <el-table v-loading="loading" :data="employeeList" border style="width: 100%">
      <el-table-column prop="username" label="Name" width="120" />
      <el-table-column prop="email" label="Email" width="180" />
      <el-table-column prop="phone" label="Phone" width="120" />
      <el-table-column prop="role" label="Role" width="100">
        <template #default="{ row }">
          <el-tag :type="getRoleTagType(row.role)">
            {{ getRoleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="true"
            :inactive-value="false"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" width="180" />
      <el-table-column label="Operation" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">Edit</el-button>
          <el-button type="danger" link @click="handleDelete(row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="Name" prop="username">
          <el-input v-model="form.username" placeholder="Please enter the name" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="form.phone" placeholder="Please enter the phone" />
        </el-form-item>
        <el-form-item label="Role" prop="role">
          <el-select v-model="form.role" placeholder="Please select the role">
            <el-option label="Admin" value="admin" />
            <el-option label="Leader" value="leader" />
            <el-option label="Member" value="member" />
          </el-select>
        </el-form-item>
        <el-form-item label="Password" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="Please enter the password" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-switch v-model="form.status" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleSubmit">Confirm</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getEmployeeList,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  updateEmployeeStatus
} from '@/api/employee'
import type { Employee, EmployeeFormData, EmployeeQueryParams } from '@/types/employee'

// 查询参数
const queryParams = reactive<EmployeeQueryParams>({})
const loading = ref(false)
const employeeList = ref<Employee[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = reactive<EmployeeFormData & { id?: number }>({
  username: '',
  email: '',
  phone: '',
  role: 'member',
  status: true,
  password: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: 'Please enter the name', trigger: 'blur' },
    { min: 2, max: 20, message: 'Length must be between 2 and 20 characters', trigger: 'blur' }
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
    { min: 6, max: 20, message: 'Length must be between 6 and 20 characters', trigger: 'blur' }
  ]
}

// 获取员工列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getEmployeeList({
      ...queryParams,
      page: page.value,
      pageSize: pageSize.value
    })
    employeeList.value = res.data
    total.value = res.total
  } catch (error) {
    console.error(error)
    ElMessage.error('Failed to get employee list')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  getList()
}

// 重置搜索
const resetQuery = () => {
  Object.keys(queryParams).forEach(key => {
    queryParams[key as keyof EmployeeQueryParams] = undefined
  })
  handleSearch()
}

// 新增员工
const handleAdd = () => {
  resetForm()
  dialogTitle.value = 'Add Employee'
  dialogVisible.value = true
}

// 编辑员工
const handleEdit = (row: Employee) => {
  Object.assign(form, row)
  form.password = undefined
  dialogTitle.value = 'Edit Employee'
  dialogVisible.value = true
}

// 删除员工
const handleDelete = (row: Employee) => {
  ElMessageBox.confirm('Are you sure you want to delete this employee?', 'Warning', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteEmployee(row.id)
      ElMessage.success('Delete successfully')
      getList()
    } catch (error) {
      ElMessage.error('Delete failed')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateEmployee(form.id, form)
          ElMessage.success('Update successfully')
        } else {
          await createEmployee(form)
          ElMessage.success('Create successfully')
        }
        dialogVisible.value = false
        getList()
      } catch (error) {
        ElMessage.error(form.id ? 'Update failed' : 'Create failed')
      }
    }
  })
}

// 更新状态
const handleStatusChange = async (row: Employee) => {
  try {
    await updateEmployeeStatus(row.id, row.status)
    ElMessage.success('Status updated successfully')
  } catch (error) {
    row.status = !row.status // 恢复状态
    ElMessage.error('Status update failed')
  }
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  getList()
}

const handleCurrentChange = (val: number) => {
  page.value = val
  getList()
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    username: '',
    email: '',
    phone: '',
    role: 'member',
    status: true,
    password: '',
    id: undefined
  })
}

// 角色标签类型
const getRoleTagType = (role: string) => {
  const types = {
    admin: 'danger',
    leader: 'warning',
    member: 'info'
  }
  return types[role as keyof typeof types]
}

// 角色标签文本
const getRoleLabel = (role: string) => {
  const labels = {
    admin: '管理员',
    leader: '组长',
    member: '成员'
  }
  return labels[role as keyof typeof labels]
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.employee-list {
  padding: 20px;
}

.search-form {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-tag) {
  text-transform: capitalize;
}
</style> 