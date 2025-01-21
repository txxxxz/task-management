<template>
  <div class="project-list">
    <div class="page-header">
      <h2>项目管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        新建项目
      </el-button>
    </div>

    <el-table :data="projects" style="width: 100%">
      <el-table-column prop="name" label="项目名称" />
      <el-table-column prop="description" label="项目描述" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑项目对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEdit ? '编辑项目' : '新建项目'"
      width="500px"
    >
      <el-form
        ref="projectForm"
        :model="projectForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Project {
  id: number
  name: string
  description: string
  status: string
  createTime: string
}

const projects = ref<Project[]>([])
const showCreateDialog = ref(false)
const isEdit = ref(false)

const projectForm = reactive({
  id: '',
  name: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' }
  ]
}

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'ON_HOLD': 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'ON_HOLD': '已暂停'
  }
  return statusMap[status] || status
}

const handleEdit = (row: Project) => {
  isEdit.value = true
  Object.assign(projectForm, row)
  showCreateDialog.value = true
}

const handleDelete = (row: Project) => {
  ElMessageBox.confirm(
    '确定要删除该项目吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // TODO: 实现删除逻辑
    ElMessage.success('删除成功')
  })
}

const handleSubmit = async () => {
  try {
    // TODO: 实现提交逻辑
    showCreateDialog.value = false
    ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.project-list {
  padding: 20px;
}

.page-header {
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