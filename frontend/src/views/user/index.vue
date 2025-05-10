<template>
  <div class="user-management">
    <div class="page-header">
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryParams" class="search-form">
        <div class="search-grid">
          <!-- 第一行 -->
          <el-form-item label="Username">
            <el-input v-model="queryParams.username" placeholder="Please enter the username" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          
          <el-form-item label="Email">
            <el-input v-model="queryParams.email" placeholder="Please enter the email" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          
          <el-form-item label="Phone">
            <el-input v-model="queryParams.phone" placeholder="Please enter the phone" clearable @keyup.enter="handleSearch" />
          </el-form-item>

          <!-- 第二行 -->
          <el-form-item label="Role">
            <el-select v-model="queryParams.role" placeholder="Please select the role" clearable class="full-width">
              <el-option label="Admin" :value="2" />
              <el-option label="Project Leader" :value="1" />
              <el-option label="Member" :value="0" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="Status">
            <el-select v-model="queryParams.status" placeholder="Please select the status" clearable class="full-width">
              <el-option label="Enabled" :value="1" />
              <el-option label="Disabled" :value="0" />
            </el-select>
          </el-form-item>
          
          <div class="button-container">
            <el-button type="primary" @click="handleSearch" :icon="Search">Search</el-button>
            <el-button @click="resetQuery" :icon="RefreshRight">Reset</el-button>
          </div>
        </div>
      </el-form>
    </el-card>

    <!-- 操作按钮和表格 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-operations">
        <el-button type="primary" @click="handleAdd" :icon="Plus" class="add-button">C</el-button>
      </div>

      <el-table v-loading="loading" :data="userList" border style="width: 100%">
        <el-table-column prop="username" label="Username" min-width="100" />
        <el-table-column prop="email" label="Email" min-width="180" />
        <el-table-column prop="phone" label="Phone" min-width="150" />
        <el-table-column label="Role" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" effect="dark">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Status" min-width="120">
          <template #default="{ row }">
            <div class="status-switch-wrapper">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                :loading="row.statusLoading"
                @change="handleStatusChange(row, $event)"
              />
              <span
                class="status-text"
                :class="{ 'status-enabled': row.status === 1, 'status-disabled': row.status === 0 }" >
                {{ row.status === 1 ? 'Enabled' : 'Disabled' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="Create Time" min-width="180" />
        <el-table-column label="Action" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" :icon="Edit"></el-button>
            <el-button type="danger" link @click="handleDelete(row)" :icon="Delete"></el-button>
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
        class="user-form"
      >
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" placeholder="Please enter the username" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="Please enter the email" />
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="form.phone" placeholder="Please enter the phone" />
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
          <el-radio-group v-model="form.status">
            <el-radio :label="1">Enabled</el-radio>
            <el-radio :label="0">Disabled</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Search,
  RefreshRight,
  Plus,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatusById } from '@/api/user'
import type { UserDetail, UserQuery, UserForm, UserRegisterData } from '@/api/user'

// 用户列表项接口，添加statusLoading字段
interface UserListItem extends UserDetail {
  statusLoading?: boolean;
}

// 数据定义
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const userList = ref<UserListItem[]>([])
const total = ref(0)
const formRef = ref<FormInstance>()
const isAdd = ref(true)

// 查询参数
const queryParams = reactive<UserQuery>({
  page: 1,
  pageSize: 10
})

// 表单数据
const form = reactive<UserForm>({
  username: '',
  email: '',
  phone: '',
  role: 0,
  status: 1,
  password: ''
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
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
  const texts: Record<number, string> = {
    0: 'Member',
    1: 'Project Leader',
    2: 'Admin'
  }
  return texts[role] || '未知角色'
}

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    console.log('API返回数据:', res)
    if (res.code === 1) {
      // 使用items而不是records
      userList.value = (res.data.items || []).map((item: any) => ({
        ...item,
        statusLoading: false
      }))
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || 'Failed to get user list')
    }
  } catch (error) {
    console.error('Failed to get user list', error)
  } finally {
    loading.value = false
  }
}

// 处理状态变更
const handleStatusChange = async (row: UserListItem, value: any) => {
  // 确保value是数字类型
  const statusValue = Number(value);
  
  // 防止重复点击
  if (row.statusLoading) return

  // 先恢复原状态
  const oldStatus = statusValue === 1 ? 0 : 1
  row.status = oldStatus

  // 确认操作
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to ${statusValue === 1 ? 'enable' : 'disable'} user "${row.username}"?`,
      'Operation Confirmation',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: statusValue === 1 ? 'success' : 'warning'
      }
    )

    // 用户确认后，再次改变状态并发送请求
    row.status = statusValue
    row.statusLoading = true
    const res = await updateUserStatusById(row.id, statusValue)
    
    if (res.code === 1) {
      ElMessage.success(`User ${statusValue === 1 ? 'enabled' : 'disabled'} successfully`)
    } else {
      // 操作失败，恢复原状态
      row.status = oldStatus
      ElMessage.error(res.msg || 'Status update failed')
    }
  } catch (error) {
    // 用户取消操作或者请求出错，状态已经恢复为原状态
    if (error !== 'cancel' && error !== 'close') {
      console.error('Failed to update user status', error)
      ElMessage.error('Status update failed')
    }
  } finally {
    row.statusLoading = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.username = undefined
  queryParams.email = undefined
  queryParams.phone = undefined
  queryParams.role = undefined
  queryParams.status = undefined
  queryParams.page = 1
  getList()
}

// 新增用户
const handleAdd = () => {
  form.username = ''
  form.email = ''
  form.phone = ''
  form.role = 0
  form.status = 1
  form.password = ''
  
  isAdd.value = true
  dialogTitle.value = 'Add User'
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: UserDetail) => {
  isAdd.value = false
  dialogTitle.value = 'Edit User'
  
  form.username = row.username
  form.email = row.email
  form.phone = row.phone || ''
  form.role = row.role
  form.status = row.status
  form.password = '' // 不回显密码
  
  dialogVisible.value = true
}

// 删除用户
const handleDelete = (row: UserDetail) => {
  ElMessageBox.confirm('Are you sure you want to delete this user?', 'Warning', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteUser(row.id)
      if (res.code === 1) {
        ElMessage.success('Delete successfully')
        getList()
      } else {
        ElMessage.error(res.msg || 'Delete failed')
      }
    } catch (error) {
      console.error('删除用户失败', error)
    }
  }).catch(() => {
    // 取消删除，不做操作
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (isAdd.value) {
        // 创建用户时，转换为UserRegisterDTO格式（只包含username、password、email、phone字段）
        const registerData: UserRegisterData = {
          username: form.username,
          password: form.password || '',
          email: form.email,
          phone: form.phone || ''
        }
        const res = await createUser(registerData)
        if (res.code === 1) {
          ElMessage.success('Add successfully')
          dialogVisible.value = false
          getList()
        } else {
          ElMessage.error(res.msg || 'Add failed')
        }
      } else {
        // 编辑时需要用户ID，从当前选中的行获取
        const currentUser = userList.value.find(u => u.username === form.username)
        if (!currentUser) {
          ElMessage.error('Get user information failed')
          return
        }
        
        // 只传递后端接受的字段：username、email、phone
        const updateData = {
          username: form.username,
          email: form.email,
          phone: form.phone
        }
        
        const res = await updateUser(currentUser.id, updateData)
        if (res.code === 1) {
          ElMessage.success('Update successfully')
          dialogVisible.value = false
          getList()
        } else {
          ElMessage.error(res.msg || 'Update failed')
        }
      }
    } catch (error) {
      console.error('提交表单失败', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// 分页相关
const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  getList()
}

const handleCurrentChange = (current: number) => {
  queryParams.page = current
  getList()
}

// 页面加载时获取数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  width: 100%;
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.search-grid .el-form-item {
  margin-bottom: 0;
  margin-right: 0;
}

.button-container {
  grid-column: 3;
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
  margin-bottom: 0;
}

.button-container .el-button + .el-button {
  margin-left: 10px;
}

.full-width {
  width: 100%;
}

.table-card {
  margin-bottom: 20px;
}

.table-operations {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.user-form {
  max-width: 100%;
}

/* 自定义el-switch样式 */
:deep(.el-switch__label) {
  font-size: 12px;
  color: #606266;
}

:deep(.el-switch__label--left) {
  margin-right: 8px;
}

:deep(.el-switch__label--right) {
  margin-left: 8px;
}

:deep(.el-switch.is-checked .el-switch__label--right) {
  color: #409EFF;
}

:deep(.el-switch:not(.is-checked) .el-switch__label--left) {
  color: #F56C6C;
}

:deep(.status-switch.el-switch.is-checked .el-switch__core) {
  background-color: #13ce66;
  border-color: #13ce66;
}

:deep(.status-switch.el-switch:not(.is-checked) .el-switch__core) {
  background-color: #ff4949;
  border-color: #ff4949;
}

/* 确保表单项标签对齐 */
:deep(.el-form-item__label) {
  width: 80px;
  text-align: right;
  padding-right: 12px;
  box-sizing: border-box;
}

:deep(.el-input__inner) {
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .search-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .button-container {
    grid-column: 2;
  }
}

@media (max-width: 768px) {
  .search-grid {
    grid-template-columns: 1fr;
  }
  
  .button-container {
    grid-column: 1;
  }
}
</style> 