<template>
  <div class="page-container">
    <div class="bg-blob">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-header glass">
      <div class="header-left">
        <h2 class="page-title">图书管理</h2>
        <p class="page-desc">管理图书馆藏图书信息</p>
      </div>
      <el-button class="add-btn" type="primary" :icon="Plus" @click="handleAdd">
        新增图书
      </el-button>
    </div>

    <div class="search-card glass">
      <div class="search-bar">
        <div class="search-input-wrapper">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索书名/作者/ISBN"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </div>
        <el-select
          v-model="categoryFilter"
          placeholder="分类筛选"
          clearable
          @change="handleSearch"
        >
          <el-option
            v-for="(cat, index) in categories"
            :key="index"
            :label="cat"
            :value="cat"
          />
        </el-select>
        <el-button class="search-btn" type="primary" @click="handleSearch">查询</el-button>
      </div>
    </div>

    <div class="table-card glass">
      <el-table :data="tableData.records" style="width: 100%" stripe v-loading="loading">
        <el-table-column label="封面" width="80">
          <template #default="scope">
            <div class="book-cover">
              <el-avatar v-if="scope.row.coverUrl" :src="scope.row.coverUrl" :size="50" shape="square" />
              <el-avatar v-else :size="50" shape="square">
                <el-icon><Reading /></el-icon>
              </el-avatar>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="isbn" label="ISBN" width="145" />
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column prop="publisher" label="出版社" width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="totalStock" label="总库存" width="90" />
        <el-table-column prop="availableStock" label="可借" width="90">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.availableStock < 3 }">{{ row.availableStock }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="tableData.total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      @close="resetForm"
      class="book-dialog"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <el-form :model="bookForm" :rules="formRules" ref="formRef" label-position="top">
          <div class="form-header">
            <div class="cover-section">
              <el-upload
                class="cover-uploader"
                :show-file-list="false"
                :before-upload="beforeCoverUpload"
                accept="image/*"
              >
                <img v-if="bookForm.coverUrl" :src="bookForm.coverUrl" class="cover-preview" />
                <div v-else class="cover-placeholder">
                  <el-icon class="upload-icon"><Plus /></el-icon>
                  <span>上传封面</span>
                </div>
                <div v-if="bookForm.coverUrl" class="cover-overlay">
                  <el-icon><Camera /></el-icon>
                </div>
              </el-upload>
              <div class="cover-actions" v-if="bookForm.coverUrl">
                <el-button type="danger" size="small" circle @click="handleRemoveCover">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div class="form-fields">
              <el-form-item label="ISBN" prop="isbn">
                <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
              </el-form-item>
              
              <el-form-item label="书名" prop="title">
                <el-input v-model="bookForm.title" placeholder="请输入书名" />
              </el-form-item>
              
              <el-form-item label="作者" prop="author">
                <el-input v-model="bookForm.author" placeholder="请输入作者" />
              </el-form-item>
            </div>
          </div>
          
          <div class="form-row">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
            </el-form-item>
            
            <el-form-item label="出版日期" prop="publishDate">
              <el-date-picker
                v-model="bookForm.publishDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="分类" prop="category">
              <el-select v-model="bookForm.category" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="(cat, index) in categories"
                  :key="index"
                  :label="cat"
                  :value="cat"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="存放位置" prop="location">
              <el-input v-model="bookForm.location" placeholder="如：A区-3楼-01" />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="总库存" prop="totalStock">
              <el-input-number v-model="bookForm.totalStock" :min="1" :max="9999" style="width: 100%" />
            </el-form-item>
            
            <el-form-item label="可借数量" prop="availableStock">
              <el-input-number v-model="bookForm.availableStock" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </div>
          
          <el-form-item label="图书简介" prop="description">
            <el-input v-model="bookForm.description" type="textarea" :rows="3" placeholder="请输入图书简介（选填）" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Reading, Camera, Delete } from '@element-plus/icons-vue'
import { getBooks, createBook, updateBook, deleteBook, getBookCategories } from '@/api/books'
import { uploadImage, deleteImage } from '@/api/upload'

const loading = ref(false)
const searchKeyword = ref('')
const categoryFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const tableData = reactive({ total: 0, records: [] })
const categories = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增图书')
const formRef = ref(null)
const isEdit = ref(false)
const currentBookId = ref(null)

const pendingCoverFile = ref(null)
const originalCoverUrl = ref('')

const bookForm = reactive({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishDate: '',
  category: '',
  location: '',
  totalStock: 1,
  availableStock: 1,
  coverUrl: '',
  description: ''
})

const formRules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  publisher: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  totalStock: [{ required: true, message: '请输入总库存', trigger: 'blur' }],
  availableStock: [{ required: true, message: '请输入可借数量', trigger: 'blur' }]
}

const fetchCategories = async () => {
  try {
    const res = await getBookCategories()
    categories.value = res.data || res
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

const fetchBooks = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchKeyword.value || undefined,
      category: categoryFilter.value || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await getBooks(params)
    tableData.total = res.total
    tableData.records = res.records
  } catch (e) {
    console.error('获取图书列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchBooks()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchBooks()
}

const resetForm = () => {
  isEdit.value = false
  currentBookId.value = null
  bookForm.isbn = ''
  bookForm.title = ''
  bookForm.author = ''
  bookForm.publisher = ''
  bookForm.publishDate = ''
  bookForm.category = ''
  bookForm.location = ''
  bookForm.totalStock = 1
  bookForm.availableStock = 1
  bookForm.coverUrl = ''
  bookForm.description = ''
  pendingCoverFile.value = null
  originalCoverUrl.value = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增图书'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  currentBookId.value = row.id
  bookForm.isbn = row.isbn
  bookForm.title = row.title
  bookForm.author = row.author
  bookForm.publisher = row.publisher
  bookForm.publishDate = row.publishDate
  bookForm.category = row.category
  bookForm.location = row.location
  bookForm.totalStock = row.totalStock
  bookForm.availableStock = row.availableStock
  bookForm.coverUrl = row.coverUrl
  bookForm.description = row.description
  originalCoverUrl.value = row.coverUrl
  dialogTitle.value = '编辑图书'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    let coverUrl = bookForm.coverUrl
    
    if (pendingCoverFile.value) {
      const res = await uploadImage(pendingCoverFile.value, 'book')
      if (res) {
        coverUrl = res
      }
    }
    
    const data = { ...bookForm, coverUrl }
    
    if (isEdit.value) {
      if (bookForm.coverUrl !== originalCoverUrl.value && originalCoverUrl.value) {
        try {
          await deleteImage(originalCoverUrl.value)
        } catch (e) {
          console.error('删除旧封面失败', e)
        }
      }
      await updateBook(currentBookId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createBook(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchBooks()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该图书吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteBook(id)
    ElMessage.success('删除成功')
    fetchBooks()
  }).catch(() => {})
}

const handleRemoveCover = async () => {
  if (isEdit.value && originalCoverUrl.value && bookForm.coverUrl === originalCoverUrl.value) {
    try {
      await deleteImage(originalCoverUrl.value)
    } catch (e) {
      console.error('删除封面失败', e)
    }
  }
  bookForm.coverUrl = ''
  pendingCoverFile.value = null
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  pendingCoverFile.value = file
  bookForm.coverUrl = URL.createObjectURL(file)
  return false
}

onMounted(() => {
  fetchCategories()
  fetchBooks()
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
  right: -100px;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #93C5FD, #60A5FA);
  bottom: -100px;
  left: -100px;
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

.search-card {
  padding: 20px 24px;
  border-radius: 20px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input-wrapper {
  flex: 1;
  max-width: 400px;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94A3B8;
  z-index: 1;
}

.search-input-wrapper :deep(.el-input__wrapper) {
  padding-left: 36px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  box-shadow: none;
}

.search-input-wrapper :deep(.el-input__wrapper:hover),
.search-input-wrapper :deep(.el-input__wrapper.is-focus) {
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-bar :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.search-btn {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border: none;
  border-radius: 12px;
  padding: 10px 20px;
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

.book-cover {
  border-radius: 8px;
  overflow: hidden;
}

.low-stock {
  color: #EF4444;
  font-weight: 600;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.pagination :deep(.el-pagination) {
  --el-pagination-bg-color: rgba(255, 255, 255, 0.8);
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.8);
  --el-pagination-hover-color: #3B82F6;
}

.cover-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cover-uploader {
  width: 120px;
  height: 160px;
  border: 2px dashed rgba(59, 130, 246, 0.3);
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
}

.cover-uploader:hover {
  border-color: #3B82F6;
  background: rgba(59, 130, 246, 0.05);
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #94A3B8;
}

.cover-tip {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
  color: #64748B;
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
  height: 120px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(147, 197, 253, 0.05) 100%);
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
  color: #3B82F6;
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
.glass-dialog :deep(.el-textarea__inner),
.glass-dialog :deep(.el-select .el-input__wrapper),
.glass-dialog :deep(.el-date-editor.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
  border-radius: 12px !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.glass-dialog :deep(.el-input__wrapper:hover),
.glass-dialog :deep(.el-textarea__inner:hover),
.glass-dialog :deep(.el-select .el-input__wrapper:hover),
.glass-dialog :deep(.el-date-editor.el-input__wrapper:hover) {
  border-color: rgba(59, 130, 246, 0.4) !important;
}

.glass-dialog :deep(.el-input__wrapper.is-focus),
.glass-dialog :deep(.el-textarea__inner:focus),
.glass-dialog :deep(.el-select .el-input.is-focus),
.glass-dialog :deep(.el-date-editor.el-input__wrapper.is-focus) {
  border-color: #3B82F6 !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.glass-dialog :deep(.el-input-number .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
  border-radius: 12px !important;
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
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border: none;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);
}

.glass-dialog :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.glass-dialog :deep(.el-button:not(.el-button--primary)) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: #3B82F6;
}

.glass-dialog :deep(.el-button:not(.el-button--primary):hover) {
  background: rgba(59, 130, 246, 0.05);
  border-color: rgba(59, 130, 246, 0.3);
}

/* 图书对话框新样式 */
.book-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 24px !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 
    0 25px 60px -12px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(59, 130, 246, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.5) !important;
  overflow: hidden;
}

.book-dialog :deep(.el-dialog__header) {
  padding: 24px 28px;
  margin: 0;
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(147, 197, 253, 0.03) 100%);
}

.book-dialog :deep(.el-dialog__title) {
  color: #1E3A8A;
  font-weight: 700;
  font-size: 20px;
}

.book-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
}

.book-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748B;
  font-size: 18px;
  transition: all 0.3s;
}

.book-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #3B82F6;
  transform: rotate(90deg);
}

.book-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-content {
  padding: 28px;
}

.form-header {
  display: flex;
  gap: 28px;
  margin-bottom: 24px;
}

.cover-section {
  flex-shrink: 0;
}

.cover-uploader {
  width: 140px;
  height: 180px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 2px dashed rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.03) 0%, rgba(147, 197, 253, 0.05) 100%);
}

.cover-uploader:hover {
  border-color: #3B82F6;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #94A3B8;
}

.cover-placeholder .upload-icon {
  font-size: 32px;
}

.cover-placeholder span {
  font-size: 12px;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 14px;
}

.cover-overlay .el-icon {
  font-size: 24px;
  color: #fff;
}

.cover-uploader:hover .cover-overlay {
  opacity: 1;
}

.cover-actions {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.form-fields {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-fields :deep(.el-form-item) {
  margin-bottom: 0;
}

.form-fields :deep(.el-form-item__content) {
  margin-left: 0 !important;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 4px;
}

.book-dialog :deep(.el-form-item__label) {
  color: #1E3A8A;
  font-weight: 600;
  font-size: 13px;
  padding: 0 0 6px 0;
  line-height: 1;
}

.book-dialog :deep(.el-input__wrapper),
.book-dialog :deep(.el-textarea__inner),
.book-dialog :deep(.el-select .el-input__wrapper),
.book-dialog :deep(.el-date-editor.el-input__wrapper),
.book-dialog :deep(.el-input-number .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.book-dialog :deep(.el-input__wrapper:hover),
.book-dialog :deep(.el-textarea__inner:hover),
.book-dialog :deep(.el-select .el-input__wrapper:hover),
.book-dialog :deep(.el-date-editor.el-input__wrapper:hover) {
  border-color: rgba(59, 130, 246, 0.4) !important;
}

.book-dialog :deep(.el-input__wrapper.is-focus),
.book-dialog :deep(.el-textarea__inner:focus),
.book-dialog :deep(.el-select .el-input.is-focus),
.book-dialog :deep(.el-date-editor.el-input__wrapper.is-focus) {
  border-color: #3B82F6 !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.book-dialog :deep(.el-dialog__footer) {
  padding: 20px 28px;
  border-top: 1px solid rgba(59, 130, 246, 0.1);
  background: rgba(59, 130, 246, 0.02);
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
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border: none;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.dialog-footer .el-button:not(.el-button--primary) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: #3B82F6;
}

.dialog-footer .el-button:not(.el-button--primary):hover {
  background: rgba(59, 130, 246, 0.05);
  border-color: rgba(59, 130, 246, 0.3);
}

@media (max-width: 768px) {
  .form-header {
    flex-direction: column;
    align-items: center;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
