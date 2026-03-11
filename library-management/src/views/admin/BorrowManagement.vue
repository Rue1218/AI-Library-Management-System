<template>
  <div class="page-container">
    <div class="bg-blob">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-header glass">
      <div class="header-left">
        <h2 class="page-title">借阅管理</h2>
        <p class="page-desc">管理读者借阅记录</p>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-item glass">
        <div class="stat-icon borrowing">
          <el-icon><Ticket /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.borrowing }}</span>
          <span class="stat-label">借阅中</span>
        </div>
      </div>
      <div class="stat-item glass">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.overdue }}</span>
          <span class="stat-label">已逾期</span>
        </div>
      </div>
      <div class="stat-item glass">
        <div class="stat-icon returned">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.returned }}</span>
          <span class="stat-label">已归还</span>
        </div>
      </div>
      <div class="stat-item glass">
        <div class="stat-icon total">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">总记录</span>
        </div>
      </div>
    </div>

    <div class="table-card glass">
      <el-table :data="tableData" style="width: 100%" stripe v-loading="loading">
        <el-table-column prop="borrowCode" label="借阅编号" width="160">
          <template #default="{ row }">
            <span class="code-text">{{ row.borrowCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32">{{ row.userName?.charAt(0) }}</el-avatar>
              <span>{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="bookTitle" label="图书名称" min-width="200">
          <template #default="{ row }">
            <span class="book-title">{{ row.bookTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="borrowDate" label="借阅日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120">
          <template #default="{ row }">
            <span :class="{ 'overdue-date': isOverdue(row) }">{{ row.dueDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="returnDate" label="归还日期" width="120">
          <template #default="{ row }">
            <span v-if="row.returnDate">{{ row.returnDate }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" class="status-tag borrowing">借阅中</el-tag>
            <el-tag v-else-if="row.status === 1" class="status-tag returned">已归还</el-tag>
            <el-tag v-else-if="row.status === 2" class="status-tag overdue">逾期</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overdueFine" label="逾期费用(元)" width="120">
          <template #default="{ row }">
            <span v-if="row.status === 1 && row.overdueFine" class="fine-amount">¥{{ row.overdueFine }}</span>
            <span v-else-if="row.status === 0 && new Date(row.dueDate) < new Date()" class="fine-amount">¥{{ Math.ceil((new Date() - new Date(row.dueDate)) / (1000 * 60 * 60 * 24)) * 0.5 }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              size="small"
              type="success"
              class="return-btn"
              @click="handleReturn(row.id)"
            >
              还书
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Ticket, Warning, CircleCheck, Document } from '@element-plus/icons-vue'
import { getBorrowRecords, returnBook } from '@/api/borrow'

const loading = ref(false)
const tableData = ref([])

const stats = computed(() => {
  const now = new Date()
  const total = tableData.value.length
  const borrowing = tableData.value.filter(r => r.status === 0).length
  const overdue = tableData.value.filter(r => r.status === 2 || (r.status === 0 && new Date(r.dueDate) < now)).length
  const returned = tableData.value.filter(r => r.status === 1).length
  return { total, borrowing, overdue, returned }
})

const isOverdue = (row) => {
  if (row.status === 2) return true
  if (row.status === 0 && new Date(row.dueDate) < new Date()) return true
  return false
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getBorrowRecords()
    tableData.value = res
  } catch (e) {
    ElMessage.error('获取借阅记录失败')
  } finally {
    loading.value = false
  }
}

const handleReturn = (recordId) => {
  ElMessageBox.confirm('确认归还该图书？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await returnBook(recordId)
    ElMessage.success('归还成功')
    fetchRecords()
  }).catch(() => {})
}

onMounted(() => {
  fetchRecords()
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
  background: linear-gradient(135deg, #F59E0B, #F97316);
  top: -150px;
  right: -100px;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #60A5FA, #3B82F6);
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

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.stat-icon.borrowing {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  color: #D97706;
}

.stat-icon.overdue {
  background: linear-gradient(135deg, #FEE2E2, #FECACA);
  color: #DC2626;
}

.stat-icon.returned {
  background: linear-gradient(135deg, #D1FAE5, #A7F3D0);
  color: #059669;
}

.stat-icon.total {
  background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  color: #2563EB;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1E3A8A;
}

.stat-label {
  font-size: 13px;
  color: #64748B;
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
  padding: 14px 0;
}

.code-text {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: #3B82F6;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.book-title {
  font-weight: 500;
  color: #1E3A8A;
}

.overdue-date {
  color: #DC2626;
  font-weight: 500;
}

.status-tag {
  border-radius: 8px;
  font-weight: 500;
}

.status-tag.borrowing {
  background: rgba(245, 158, 11, 0.1);
  color: #D97706;
  border: none;
}

.status-tag.returned {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
  border: none;
}

.status-tag.overdue {
  background: rgba(239, 68, 68, 0.1);
  color: #DC2626;
  border: none;
}

.fine-amount {
  font-weight: 600;
  color: #DC2626;
}

.text-muted {
  color: #94A3B8;
}

.return-btn {
  background: linear-gradient(135deg, #10B981, #059669);
  border: none;
  border-radius: 8px;
  padding: 6px 14px;
  transition: all 0.3s ease;
}

.return-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
