<template>
  <div class="page-container">
    <div class="bg-blob">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-header glass">
      <div class="header-left">
        <h2 class="page-title">分类管理</h2>
        <p class="page-desc">管理图书分类体系</p>
      </div>
      <el-button class="add-btn" type="primary" :icon="Plus" @click="handleAdd">
        新增分类
      </el-button>
    </div>

    <div class="table-card glass">
      <el-table :data="tableData" style="width: 100%" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="id-badge">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="180">
          <template #default="{ row }">
            <div class="category-name">
              <el-icon class="category-icon"><Folder /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="parentId" label="父分类ID" width="120" />
        <el-table-column prop="sort" label="排序" width="100">
          <template #default="{ row }">
            <span class="sort-badge">{{ row.sort }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bookCount" label="图书数量" width="120">
          <template #default="{ row }">
            <span class="book-count">{{ row.bookCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button size="small" type="success" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="480px" 
      class="category-dialog"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="category-icon-preview">
          <div class="icon-wrapper">
            <el-icon :size="48"><Folder /></el-icon>
          </div>
        </div>
        
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入分类名称" size="large">
              <template #prefix>
                <el-icon><Folder /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="排序权重" prop="sort">
            <div class="sort-input">
              <el-slider v-model="form.sort" :min="0" :max="100" show-input />
            </div>
            <div class="form-tip">数值越大排序越靠前</div>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="viewDialogVisible" 
      title="分类图书列表" 
      width="900px" 
      class="category-dialog"
    >
      <div class="view-dialog-content">
        <el-table :data="bookList" v-loading="bookLoading" class="book-table">
          <el-table-column prop="id" label="ID" width="70">
            <template #default="{ row }">
              <span class="id-cell">{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="isbn" label="ISBN" width="140" />
          <el-table-column prop="title" label="书名" min-width="200">
            <template #default="{ row }">
              <div class="book-title-cell">{{ row.title }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="publisher" label="出版社" width="140" />
          <el-table-column prop="totalStock" label="总库存" width="90" align="center">
            <template #default="{ row }">
              <span class="stock-cell">{{ row.totalStock }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="availableStock" label="可借" width="90" align="center">
            <template #default="{ row }">
              <span class="stock-cell available">{{ row.availableStock }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Folder } from '@element-plus/icons-vue'
import { getCategories, addCategory, updateCategory, deleteCategory, getBooksByCategory } from '@/api/category'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const bookLoading = ref(false)
const bookList = ref([])
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)

const form = ref({
  id: null,
  name: '',
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCategories()
    tableData.value = res || []
  } catch (e) {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增分类'
  form.value = { id: null, name: '', sort: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleView = async (row) => {
  viewDialogVisible.value = true
  bookLoading.value = true
  try {
    const res = await getBooksByCategory(row.id)
    bookList.value = res || []
  } catch (e) {
    ElMessage.error('获取图书列表失败')
  } finally {
    bookLoading.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCategory(id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateCategory(form.value)
      ElMessage.success('更新成功')
    } else {
      await addCategory(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  position: relative;
  overflow: hidden;
  min-height: 100vh;
}

.bg-blob {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.4;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #60A5FA, #3B82F6);
  top: -150px;
  left: -100px;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #34D399, #10B981);
  bottom: -100px;
  right: -100px;
}

.glass {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-radius: 20px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1E3A8A;
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: #64748B;
}

.add-btn {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border: none;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
}

.table-card {
  padding: 24px;
  border-radius: 20px;
  position: relative;
  z-index: 1;
}

.table-card :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(59, 130, 246, 0.05);
  --el-table-row-hover-bg-color: rgba(59, 130, 246, 0.05);
  --el-table-border-color: rgba(59, 130, 246, 0.1);
}

.table-card :deep(.el-table th.el-table__cell) {
  background: rgba(59, 130, 246, 0.08);
  color: #1E3A8A;
  font-weight: 600;
}

.table-card :deep(.el-table td.el-table__cell) {
  padding: 16px 0;
}

.id-badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
}

.category-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-icon {
  color: #3B82F6;
  font-size: 18px;
}

.sort-badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
  border-radius: 8px;
  font-weight: 500;
}

.book-count {
  font-weight: 600;
  color: #1E3A8A;
}

.glass-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(30px) !important;
  -webkit-backdrop-filter: blur(30px) !important;
  border-radius: 24px !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(59, 130, 246, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.4) !important;
  overflow: hidden;
}

.glass-dialog :deep(.el-dialog::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.08) 0%, rgba(52, 211, 153, 0.05) 100%);
  pointer-events: none;
}

.glass-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  padding: 24px 28px;
  position: relative;
  margin-right: 0;
}

.glass-dialog :deep(.el-dialog__title) {
  color: #1E3A8A;
  font-weight: 700;
  font-size: 18px;
}

.glass-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
}

.glass-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748B;
  font-size: 18px;
  transition: all 0.2s;
}

.glass-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #10B981;
  transform: rotate(90deg);
}

.glass-dialog :deep(.el-dialog__body) {
  padding: 28px;
}

.glass-dialog :deep(.el-form-item__label) {
  color: #1E3A8A;
  font-weight: 600;
  font-size: 14px;
}

.glass-dialog :deep(.el-input__wrapper),
.glass-dialog :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
  border-radius: 12px !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.glass-dialog :deep(.el-input__wrapper:hover),
.glass-dialog :deep(.el-textarea__inner:hover) {
  border-color: rgba(59, 130, 246, 0.4) !important;
}

.glass-dialog :deep(.el-input__wrapper.is-focus),
.glass-dialog :deep(.el-textarea__inner:focus) {
  border-color: #10B981 !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1) !important;
}

.glass-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid rgba(59, 130, 246, 0.1);
  padding: 20px 28px;
}

.glass-dialog :deep(.el-button) {
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.glass-dialog :deep(.el-button--primary) {
  background: linear-gradient(135deg, #10B981, #059669);
  border: none;
  box-shadow: 0 4px 14px rgba(16, 185, 129, 0.3);
}

.glass-dialog :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

.glass-dialog :deep(.el-button:not(.el-button--primary)) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: #10B981;
}

.glass-dialog :deep(.el-button:not(.el-button--primary):hover) {
  background: rgba(16, 185, 129, 0.05);
  border-color: rgba(16, 185, 129, 0.3);
}

/* 分类对话框新样式 */
.category-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 24px !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 
    0 25px 60px -12px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(16, 185, 129, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.5) !important;
  overflow: hidden;
}

.category-dialog :deep(.el-dialog__header) {
  padding: 24px 28px;
  margin: 0;
  border-bottom: 1px solid rgba(16, 185, 129, 0.1);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.05) 0%, rgba(52, 211, 153, 0.03) 100%);
}

.category-dialog :deep(.el-dialog__title) {
  color: #1E3A8A;
  font-weight: 700;
  font-size: 20px;
}

.category-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
}

.category-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748B;
  font-size: 18px;
  transition: all 0.3s;
}

.category-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #10B981;
  transform: rotate(90deg);
}

.category-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-content {
  padding: 32px;
}

.category-icon-preview {
  display: flex;
  justify-content: center;
  margin-bottom: 28px;
}

.icon-wrapper {
  width: 90px;
  height: 90px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(52, 211, 153, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10B981;
  box-shadow: 0 8px 24px rgba(16, 185, 129, 0.2);
}

.category-dialog :deep(.el-form-item__label) {
  color: #1E3A8A;
  font-weight: 600;
  font-size: 14px;
  padding: 0 0 8px 0;
  line-height: 1;
}

.category-dialog :deep(.el-input__wrapper),
.category-dialog :deep(.el-input-number .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(16, 185, 129, 0.2) !important;
  border-radius: 12px !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.category-dialog :deep(.el-input__wrapper:hover),
.category-dialog :deep(.el-input-number .el-input__wrapper:hover) {
  border-color: rgba(16, 185, 129, 0.4) !important;
}

.category-dialog :deep(.el-input__wrapper.is-focus) {
  border-color: #10B981 !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1) !important;
}

.category-dialog :deep(.el-input__prefix) {
  color: #10B981;
}

.sort-input {
  padding: 0 8px;
}

.category-dialog :deep(.el-slider__runway) {
  background: rgba(16, 185, 129, 0.2);
}

.category-dialog :deep(.el-slider__bar) {
  background: linear-gradient(90deg, #10B981, #34D399);
}

.category-dialog :deep(.el-slider__button) {
  border-color: #10B981;
}

.form-tip {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 6px;
}

.category-dialog :deep(.el-dialog__footer) {
  padding: 20px 28px;
  border-top: 1px solid rgba(16, 185, 129, 0.1);
  background: rgba(16, 185, 129, 0.02);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  padding: 10px 24px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #10B981, #059669);
  border: none;
  box-shadow: 0 4px 14px rgba(16, 185, 129, 0.3);
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

.dialog-footer .el-button:not(.el-button--primary) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(16, 185, 129, 0.2);
  color: #10B981;
}

.dialog-footer .el-button:not(.el-button--primary):hover {
  background: rgba(16, 185, 129, 0.05);
  border-color: rgba(16, 185, 129, 0.3);
}

/* 查看图书列表对话框 */
.view-dialog-content {
  padding: 24px;
}

.category-dialog :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(16, 185, 129, 0.05);
  --el-table-row-hover-bg-color: rgba(16, 185, 129, 0.05);
  --el-table-border-color: rgba(16, 185, 129, 0.1);
  border-radius: 12px;
  overflow: hidden;
}

.category-dialog :deep(.el-table th.el-table__cell) {
  background: rgba(16, 185, 129, 0.08);
  color: #1E3A8A;
  font-weight: 600;
}

.category-dialog :deep(.el-table td.el-table__cell) {
  padding: 14px 0;
}

.id-cell {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
}

.book-title-cell {
  font-weight: 500;
  color: #1E3A8A;
}

.stock-cell {
  font-weight: 600;
  color: #1E3A8A;
}

.stock-cell.available {
  color: #10B981;
}
</style>
