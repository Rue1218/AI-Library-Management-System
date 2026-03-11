<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">我的借阅</h1>
        <p class="page-desc">管理您的借阅记录</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="fetchRecords" :loading="loading" class="refresh-btn">
        刷新数据
      </el-button>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card glass-card borrowing">
        <div class="stat-icon">
          <el-icon :size="32"><Reading /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ borrowingCount }}</span>
          <span class="stat-label">借阅中</span>
        </div>
      </div>
      <div class="stat-card glass-card returned">
        <div class="stat-icon">
          <el-icon :size="32"><Select /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ returnedCount }}</span>
          <span class="stat-label">已归还</span>
        </div>
      </div>
      <div class="stat-card glass-card overdue">
        <div class="stat-icon">
          <el-icon :size="32"><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ overdueCount }}</span>
          <span class="stat-label">逾期</span>
        </div>
      </div>
    </div>
    
    <!-- 借阅记录 -->
    <div class="content-card glass-card">
      <el-tabs v-model="activeTab" class="page-tabs">
        <el-tab-pane label="全部记录" name="all">
          <el-table :data="paginatedRecords" stripe v-loading="loading" class="data-table">
            <el-table-column prop="borrowCode" label="借阅编号" width="180">
              <template #default="{ row }">
                <span class="code-text">{{ row.borrowCode || 'BK' + row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bookTitle" label="图书" min-width="200">
              <template #default="{ row }">
                <div class="book-cell">
                  <el-icon class="book-icon"><Reading /></el-icon>
                  <span>{{ row.bookTitle || '图书 #' + row.bookId }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="borrowDate" label="借阅日期" width="140" />
            <el-table-column prop="dueDate" label="应还日期" width="140">
              <template #default="{ row }">
                <span :class="{ 'overdue-text': isOverdue(row) }">{{ row.dueDate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="returnDate" label="归还日期" width="140">
              <template #default="{ row }">
                <span v-if="row.returnDate" class="returned-text">{{ row.returnDate }}</span>
                <span v-else class="pending-text">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="primary" effect="light" round>借阅中</el-tag>
                <el-tag v-else-if="row.status === 1" type="success" effect="light" round>已归还</el-tag>
                <el-tag v-else-if="row.status === 2" type="danger" effect="light" round>逾期</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" type="primary" link @click="handleReturn(row)">
                  <el-icon><Check /></el-icon> 归还
                </el-button>
                <el-button v-if="row.status === 0" type="info" link @click="handleRenew(row)">
                  <el-icon><RefreshRight /></el-icon> 续借
                </el-button>
                <el-button v-if="row.status === 1" type="danger" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
                <span v-if="row.status !== 0" class="action-divider">|</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="records.length"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="借阅中" name="borrowing">
          <el-table :data="paginatedBorrowingRecords" stripe v-loading="loading" class="data-table">
            <el-table-column prop="borrowCode" label="借阅编号" width="180">
              <template #default="{ row }">
                <span class="code-text">{{ row.borrowCode || 'BK' + row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bookTitle" label="图书" min-width="200">
              <template #default="{ row }">
                <div class="book-cell">
                  <el-icon class="book-icon"><Reading /></el-icon>
                  <span>{{ row.bookTitle || '图书 #' + row.bookId }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="borrowDate" label="借阅日期" width="140" />
            <el-table-column prop="dueDate" label="应还日期" width="140" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleReturn(row)">
                  <el-icon><Check /></el-icon> 归还
                </el-button>
                <el-button type="info" link @click="handleRenew(row)">
                  <el-icon><RefreshRight /></el-icon> 续借
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="borrowingPage"
              v-model:page-size="borrowingPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="borrowingRecords.length"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="handleBorrowingSizeChange"
              @current-change="handleBorrowingCurrentChange"
            />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="已归还" name="returned">
          <el-table :data="paginatedReturnedRecords" stripe v-loading="loading" class="data-table">
            <el-table-column prop="borrowCode" label="借阅编号" width="180">
              <template #default="{ row }">
                <span class="code-text">{{ row.borrowCode || 'BK' + row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bookTitle" label="图书" min-width="200">
              <template #default="{ row }">
                <div class="book-cell">
                  <el-icon class="book-icon"><Reading /></el-icon>
                  <span>{{ row.bookTitle || '图书 #' + row.bookId }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="borrowDate" label="借阅日期" width="140" />
            <el-table-column prop="returnDate" label="归还日期" width="140" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="returnedPage"
              v-model:page-size="returnedPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="returnedRecords.length"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="handleReturnedSizeChange"
              @current-change="handleReturnedCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 空状态 -->
    <div v-if="!loading && records.length === 0" class="empty-card glass-card">
      <el-empty description="暂无借阅记录">
        <template #image>
          <svg viewBox="0 0 200 160" class="empty-svg">
            <rect x="40" y="30" width="120" height="100" rx="8" fill="#e0f2fe"/>
            <rect x="55" y="15" width="25" height="115" rx="4" fill="#0891B2" opacity="0.8"/>
            <rect x="85" y="25" width="20" height="105" rx="4" fill="#22D3EE" opacity="0.7"/>
            <rect x="110" y="35" width="35" height="95" rx="4" fill="#0891B2" opacity="0.6"/>
          </svg>
        </template>
        <el-button type="primary" @click="$router.push('/user/books')">去借阅图书</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Reading, Select, Warning, RefreshRight, Check, Delete } from '@element-plus/icons-vue'
import { getBorrowRecords, renewBook, returnBook, deleteBorrowRecord } from '@/api/borrow'

const route = useRoute()
const loading = ref(false)
const records = ref([])
const activeTab = ref('all')

const currentPage = ref(1)
const pageSize = ref(10)
const borrowingPage = ref(1)
const borrowingPageSize = ref(10)
const returnedPage = ref(1)
const returnedPageSize = ref(10)

const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return records.value.slice(start, end)
})

const paginatedBorrowingRecords = computed(() => {
  const start = (borrowingPage.value - 1) * borrowingPageSize.value
  const end = start + borrowingPageSize.value
  return borrowingRecords.value.slice(start, end)
})

const paginatedReturnedRecords = computed(() => {
  const start = (returnedPage.value - 1) * returnedPageSize.value
  const end = start + returnedPageSize.value
  return returnedRecords.value.slice(start, end)
})

const handleSizeChange = () => {
  currentPage.value = 1
}

const handleCurrentChange = () => {}

const handleBorrowingSizeChange = () => {
  borrowingPage.value = 1
}

const handleBorrowingCurrentChange = () => {}

const handleReturnedSizeChange = () => {
  returnedPage.value = 1
}

const handleReturnedCurrentChange = () => {}

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getBorrowRecords()
    console.log('Borrow records:', res)
    records.value = res || []
  } catch (e) {
    console.error('Fetch records error:', e)
    ElMessage.error('获取借阅记录失败')
  } finally {
    loading.value = false
  }
}

const borrowingCount = computed(() => records.value.filter(r => r.status === 0).length)
const returnedCount = computed(() => records.value.filter(r => r.status === 1).length)
const overdueCount = computed(() => records.value.filter(r => r.status === 2).length)

const borrowingRecords = computed(() => records.value.filter(r => r.status === 0))
const returnedRecords = computed(() => records.value.filter(r => r.status === 1))

const isOverdue = (row) => row.status === 0 && row.dueDate && new Date(row.dueDate) < new Date()

const handleRenew = (record) => {
  ElMessageBox.confirm(`确定要续借《${record.bookTitle}》吗？`, '续借', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await renewBook(record.id)
      ElMessage.success('续借成功')
      fetchRecords()
    } catch (e) {
      ElMessage.error('续借失败')
    }
  }).catch(() => {})
}

const handleReturn = async (record) => {
  try {
    await ElMessageBox.confirm(`确定要归还《${record.bookTitle}》吗？`, '归还', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await returnBook(record.id)
    ElMessage.success('归还成功')
    fetchRecords()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('归还失败')
    }
  }
}

const handleDelete = async (record) => {
  try {
    await ElMessageBox.confirm(`确定要删除该借阅记录吗？`, '删除记录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteBorrowRecord(record.id)
    ElMessage.success('删除成功')
    fetchRecords()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

watch(() => route.path, () => {
  fetchRecords()
})

onMounted(() => { fetchRecords() })
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.page-title {
  font-size: 32px;
  font-weight: 800;
  color: #0369A1;
  margin-bottom: 6px;
}

.page-desc {
  font-size: 15px;
  color: #0369A1;
  opacity: 0.7;
}

.refresh-btn {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.3);
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.1);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(14, 165, 233, 0.2);
}

.stat-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card.borrowing .stat-icon {
  background: linear-gradient(135deg, #DBEAFE, #93C5FD);
  color: #2563EB;
}

.stat-card.returned .stat-icon {
  background: linear-gradient(135deg, #DCFCE7, #86EFAC);
  color: #16A34A;
}

.stat-card.overdue .stat-icon {
  background: linear-gradient(135deg, #FEE2E2, #FCA5A5);
  color: #DC2626;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  color: #0C4A6E;
}

.stat-label {
  font-size: 15px;
  color: #0369A1;
  margin-top: 4px;
}

/* 内容卡片 */
.content-card {
  padding: 24px;
}

.page-tabs {
  margin-bottom: 16px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  color: #64748B;
}

:deep(.el-tabs__item.is-active) {
  color: #0EA5E9;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #38BDF8, #0EA5E9);
}

.data-table {
  margin-top: 16px;
}

.code-text {
  font-family: 'Monaco', monospace;
  color: #0369A1;
}

.book-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.book-icon {
  color: #0EA5E9;
}

.overdue-text {
  color: #DC2626;
  font-weight: 600;
}

.returned-text {
  color: #16A34A;
}

.pending-text {
  color: #94A3B8;
}

.no-action {
  color: #CBD5E1;
}

.action-divider {
  color: #CBD5E1;
  margin: 0 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: #fff;
  --el-pagination-text-color: #0369A1;
  --el-pagination-button-bg-color: #fff;
  --el-pagination-hover-color: #0EA5E9;
}

/* 空状态 */
.empty-card {
  padding: 60px;
  text-align: center;
}

.empty-svg {
  width: 200px;
  height: 160px;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
