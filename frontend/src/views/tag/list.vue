<template>
  <div class="tag-list-container">
    <!-- 搜索筛选区域 -->
    <div class="filter-section">
      <el-form :model="filterForm" class="filter-form">
        <div class="filter-row">
          <div class="filter-item">
            <el-form-item label="标签名称">
              <el-input v-model="filterForm.name" placeholder="请输入标签名称" clearable />
            </el-form-item>
          </div>
          <div class="filter-item">
            <el-form-item label="创建日期">
              <el-date-picker
                v-model="filterForm.createDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </div>
        </div>
        <!-- 查询和重置按钮 -->
        <div class="filter-buttons">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="operation-section">
      <div class="left-buttons">
        <el-button type="primary" @click="handleNewTag" v-if="userStore.userInfo?.role === 1">
          <el-icon><Plus /></el-icon> 新建标签
        </el-button>
      </div>
      <div class="right-buttons">
        <el-button @click="handleDownload">
          <el-icon><Download /></el-icon> 导出
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <el-table
      :data="tagList"
      style="width: 100%"
      border
      stripe
      v-loading="loading"
    >
      <el-table-column prop="id" label="标签ID" width="120" />
      <el-table-column prop="name" label="标签名称" min-width="150">
        <template #default="{ row }">
          <div class="tag-name-cell">
            <el-tag :style="{ backgroundColor: row.color, color: getContrastColor(row.color) }">
              {{ row.name }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="标签颜色" width="120">
        <template #default="{ row }">
          <div class="color-preview" :style="{ backgroundColor: row.color }" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            @click="handleEdit(row)"
            v-if="userStore.userInfo?.role === 1"
          >
            编辑
          </el-button>
          <el-button
            type="danger"
            link
            @click="handleDelete(row)"
            v-if="userStore.userInfo?.role === 1"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getTagList, deleteTag } from '@/api/tag'
import { formatDate as formatDateUtil } from '@/utils/format'
import type { Tag } from '@/types/tag'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 筛选表单数据
const filterForm = reactive({
  name: '',
  createDateRange: [] as string[]
})

// 数据加载状态
const loading = ref(false)

// 表格数据
const tagList = ref<Tag[]>([])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 从后端获取标签列表
const fetchTagList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: any = {
      keyword: filterForm.name,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 添加日期范围（如果有）
    if (filterForm.createDateRange && filterForm.createDateRange.length === 2) {
      params.startTime = filterForm.createDateRange[0]
      params.endTime = filterForm.createDateRange[1]
    }
    
    const response = await getTagList(params)
    const { data } = response.data
    
    // 处理响应数据
    if (data) {
      tagList.value = data.items || []
      total.value = data.total || 0
    }
  } catch (error) {
    console.error('获取标签列表失败', error)
    ElMessage.error('获取标签列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取对比色（确保标签文本清晰可见）
const getContrastColor = (hexColor: string) => {
  // 处理空值
  if (!hexColor) return '#000000'
  
  try {
    // 将十六进制颜色转换为RGB
    const r = parseInt(hexColor.substring(1, 3), 16)
    const g = parseInt(hexColor.substring(3, 5), 16)
    const b = parseInt(hexColor.substring(5, 7), 16)
    
    // 计算亮度
    const brightness = (r * 299 + g * 587 + b * 114) / 1000
    
    // 根据亮度返回黑色或白色
    return brightness > 128 ? '#000000' : '#ffffff'
  } catch (error) {
    return '#000000' // 出错时返回黑色
  }
}

// 格式化日期
const formatDate = (date: string | undefined): string => {
  if (!date) return '未设置'
  return formatDateUtil(date)
}

// 事件处理方法
const handleSearch = () => {
  currentPage.value = 1 // 重置为第一页
  fetchTagList()
}

const handleReset = () => {
  // 重置筛选表单
  filterForm.name = ''
  filterForm.createDateRange = []
  
  // 重新加载数据
  currentPage.value = 1
  fetchTagList()
}

const handleNewTag = () => {
  router.push('/tag/create')
}

const handleEdit = (row: Tag) => {
  router.push(`/tag/edit/${row.id}`)
}

const handleDelete = (row: Tag) => {
  ElMessageBox.confirm(
    `确定要删除标签 "${row.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteTag(Number(row.id))
      ElMessage.success('删除成功')
      fetchTagList() // 刷新列表
    } catch (error) {
      console.error('删除标签失败', error)
      ElMessage.error('删除标签失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

const handleDownload = () => {
  ElMessage.info('导出功能即将上线')
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchTagList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchTagList()
}

onMounted(async () => {
  // 加载标签列表
  fetchTagList()
})
</script>

<style scoped>
/* 整体容器 */
.tag-list-container {
  padding: 20px;
  background-color: #f5f7fa;
}

/* 搜索筛选区域 */
.filter-section {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.filter-item {
  flex: 1 1 200px;
  min-width: 280px;
}

.filter-buttons {
  text-align: right;
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 0;
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--el-text-color-regular);
  padding-right: 12px;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-date-editor.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover),
:deep(.el-date-editor.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

:deep(.el-select),
:deep(.el-date-editor) {
  width: 100%;
}

/* 按钮样式 */
.filter-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.filter-buttons .el-button {
  padding: 8px 24px;
  font-weight: 500;
}

/* 操作按钮区域 */
.operation-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.left-buttons, .right-buttons {
  display: flex;
  gap: 10px;
}

/* 表格区域 */
:deep(.el-table) {
  margin-top: 20px;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
  font-weight: 600;
}

/* 标签名称单元格 */
.tag-name-cell {
  display: flex;
  align-items: center;
}

/* 颜色预览 */
.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  margin: 0 auto;
  border: 1px solid #eee;
}

/* 分页 */
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  margin-top: 20px;
}
</style> 