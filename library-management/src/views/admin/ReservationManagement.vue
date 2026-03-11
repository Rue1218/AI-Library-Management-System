<template>
  <div class="page-container">
    <div class="bg-blob">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-header glass">
      <div class="header-left">
        <h2 class="page-title">预约管理</h2>
        <p class="page-desc">管理读者图书预约</p>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-item glass">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
      </div>
      <div class="stat-item glass">
        <div class="stat-icon available">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.available }}</span>
          <span class="stat-label">可取书</span>
        </div>
      </div>
      <div class="stat-item glass">
        <div class="stat-icon completed">
          <el-icon><Select /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.completed }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </div>
      <div class="stat-item glass">
        <div class="stat-icon cancelled">
          <el-icon><Close /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.cancelled }}</span>
          <span class="stat-label">已取消</span>
        </div>
      </div>
    </div>

    <div class="table-card glass">
      <el-table :data="tableData" style="width: 100%" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="id-badge">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="130">
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
        <el-table-column prop="reserveDate" label="预约日期" width="130" />
        <el-table-column prop="expireDate" label="截止日期" width="130">
          <template #default="{ row }">
            <span :class="{ 'expired': isExpired(row) }">{{ row.expireDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" class="status-tag pending">等待中</el-tag>
            <el-tag v-else-if="row.status === 1" class="status-tag available">可取书</el-tag>
            <el-tag v-else-if="row.status === 2" class="status-tag cancelled">已取消</el-tag>
            <el-tag v-else-if="row.status === 3" class="status-tag completed">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === 1"
              size="small"
              type="success"
              class="action-btn complete"
              @click="handleComplete(row.id)"
            >
              确认取书
            </el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="danger"
              class="action-btn cancel"
              @click="handleCancel(row.id)"
            >
              取消
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
import { Clock, CircleCheck, Select, Close } from '@element-plus/icons-vue'
import { getAllReservations, completeReservation, cancelReservation } from '@/api/reservation'

const loading = ref(false)
const tableData = ref([])

const stats = computed(() => {
  return {
    pending: tableData.value.filter(r => r.status === 0).length,
    available: tableData.value.filter(r => r.status === 1).length,
    completed: tableData.value.filter(r => r.status === 3).length,
    cancelled: tableData.value.filter(r => r.status === 2).length
  }
})

const isExpired = (row) => {
  return row.status === 0 && new Date(row.expireDate) < new Date()
}

const fetchReservations = async () => {
  loading.value = true
  try {
    const res = await getAllReservations()
    tableData.value = res
  } catch (e) {
    ElMessage.error('获取预约记录失败')
  } finally {
    loading.value = false
  }
}

const handleComplete = (id) => {
  ElMessageBox.confirm('确认读者已取书？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    await completeReservation(id)
    ElMessage.success('操作成功')
    fetchReservations()
  }).catch(() => {})
}

const handleCancel = (id) => {
  ElMessageBox.confirm('确定要取消该预约吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await cancelReservation(id)
    ElMessage.success('已取消')
    fetchReservations()
  }).catch(() => {})
}

onMounted(() => {
  fetchReservations()
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
  background: linear-gradient(135deg, #8B5CF6, #A78BFA);
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

.stat-icon.pending {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  color: #D97706;
}

.stat-icon.available {
  background: linear-gradient(135deg, #D1FAE5, #A7F3D0);
  color: #059669;
}

.stat-icon.completed {
  background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  color: #2563EB;
}

.stat-icon.cancelled {
  background: linear-gradient(135deg, #FEE2E2, #FECACA);
  color: #DC2626;
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

.id-badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(139, 92, 246, 0.1);
  color: #8B5CF6;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
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

.expired {
  color: #DC2626;
  font-weight: 500;
}

.status-tag {
  border-radius: 8px;
  font-weight: 500;
}

.status-tag.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #D97706;
  border: none;
}

.status-tag.available {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
  border: none;
}

.status-tag.completed {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
  border: none;
}

.status-tag.cancelled {
  background: rgba(239, 68, 68, 0.1);
  color: #DC2626;
  border: none;
}

.action-btn {
  border-radius: 8px;
  padding: 6px 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn.complete {
  background: linear-gradient(135deg, #10B981, #059669);
  border: none;
}

.action-btn.complete:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.action-btn.cancel {
  background: transparent;
  border: 1px solid #EF4444;
  color: #DC2626;
}

.action-btn.cancel:hover {
  background: #EF4444;
  color: #fff;
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
