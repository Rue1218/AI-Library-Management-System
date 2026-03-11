<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">我的预约</h1>
        <p class="page-desc">查看和管理您的图书预约</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="fetchReservations" :loading="loading" class="refresh-btn">
        刷新数据
      </el-button>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card glass-card pending">
        <div class="stat-icon">
          <el-icon :size="32"><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ pendingCount }}</span>
          <span class="stat-label">待取书</span>
        </div>
      </div>
      <div class="stat-card glass-card available">
        <div class="stat-icon">
          <el-icon :size="32"><Select /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ availableCount }}</span>
          <span class="stat-label">可取书</span>
        </div>
      </div>
      <div class="stat-card glass-card expired">
        <div class="stat-icon">
          <el-icon :size="32"><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ expiredCount }}</span>
          <span class="stat-label">已过期</span>
        </div>
      </div>
    </div>
    
    <!-- 预约记录 -->
    <div class="content-card glass-card">
      <el-tabs v-model="activeTab" class="page-tabs">
        <el-tab-pane label="全部记录" name="all">
          <div v-if="reservations.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无预约记录">
              <template #image>
                <svg viewBox="0 0 160 160" class="empty-svg">
                  <rect x="30" y="40" width="100" height="90" rx="6" fill="#FEF3C7"/>
                  <rect x="42" y="25" width="24" height="105" rx="3" fill="#F59E0B" opacity="0.8"/>
                  <rect x="72" y="35" width="20" height="95" rx="3" fill="#FBBF24" opacity="0.7"/>
                  <circle cx="80" cy="90" r="18" fill="#fff" opacity="0.8"/>
                  <path d="M72 90 L78 96 L90 82" stroke="#F59E0B" stroke-width="3" fill="none"/>
                </svg>
              </template>
              <el-button type="primary" @click="$router.push('/user/books')">去预约图书</el-button>
            </el-empty>
          </div>
          <el-table v-else :data="reservations" stripe v-loading="loading" class="data-table">
            <el-table-column prop="reserveCode" label="预约编号" width="180">
              <template #default="{ row }">
                <span class="code-text">{{ row.reserveCode || 'RSV' + row.id }}</span>
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
            <el-table-column prop="reserveDate" label="预约日期" width="140" />
            <el-table-column prop="expireDate" label="取书截止" width="140">
              <template #default="{ row }">
                <span :class="{ 'expire-soon': isExpiringSoon(row) }">{{ row.expireDate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="warning" effect="light" round>待取书</el-tag>
                <el-tag v-else-if="row.status === 1" type="success" effect="light" round>可取书</el-tag>
                <el-tag v-else-if="row.status === 2" type="info" effect="light" round>已过期</el-tag>
                <el-tag v-else-if="row.status === 3" type="danger" effect="light" round>已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0 || row.status === 1" type="danger" link @click="handleCancel(row)">
                  <el-icon><Close /></el-icon> 取消
                </el-button>
                <span v-else class="no-action">-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="待取书" name="pending">
          <el-table :data="pendingReservations" stripe v-loading="loading" class="data-table">
            <el-table-column prop="reserveCode" label="预约编号" width="180">
              <template #default="{ row }">
                <span class="code-text">{{ row.reserveCode || 'RSV' + row.id }}</span>
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
            <el-table-column prop="reserveDate" label="预约日期" width="140" />
            <el-table-column prop="expireDate" label="取书截止" width="140" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link @click="handleCancel(row)">
                  <el-icon><Close /></el-icon> 取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 预约须知 -->
    <div class="info-card glass-card">
      <div class="info-header">
        <el-icon :size="20"><InfoFilled /></el-icon>
        <span>预约须知</span>
      </div>
      <ul class="info-list">
        <li>预约图书后，请于 <strong>3天内</strong> 到图书馆取书</li>
        <li>逾期未取书的预约将被自动取消</li>
        <li>每个用户同时最多预约 <strong>3本</strong> 图书</li>
        <li>如需取消预约，请在取书截止前操作</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Select, Warning, Close, Reading, InfoFilled, Refresh } from '@element-plus/icons-vue'
import { getMyReservations, cancelReservation } from '@/api/reservation'

const reservations = ref([])
const loading = ref(false)
const activeTab = ref('all')

const fetchReservations = async () => {
  loading.value = true
  try {
    const res = await getMyReservations()
    reservations.value = res || []
  } catch (e) {
    ElMessage.error('获取预约记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchReservations()
})

const pendingCount = computed(() => reservations.value.filter(r => r.status === 0).length)
const availableCount = computed(() => reservations.value.filter(r => r.status === 1).length)
const expiredCount = computed(() => reservations.value.filter(r => r.status === 2).length)

const pendingReservations = computed(() => reservations.value.filter(r => r.status === 0 || r.status === 1))

const isExpiringSoon = (row) => {
  if (!row.expireDate) return false
  const diff = (new Date(row.expireDate) - new Date()) / (1000 * 60 * 60 * 24)
  return diff <= 1
}

const handleCancel = (reservation) => {
  ElMessageBox.confirm(`确定要取消预约《${reservation.bookTitle}》吗？`, '取消预约', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelReservation(reservation.id)
      ElMessage.success('预约已取消')
      fetchReservations()
    } catch (e) {
      ElMessage.error('取消预约失败')
    }
  }).catch(() => {})
}
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
  background: linear-gradient(135deg, #FBBF24, #F59E0B) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.3);
}

/* 玻璃卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.1);
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  color: #D97706;
}

.stat-card.available .stat-icon {
  background: linear-gradient(135deg, #DCFCE7, #86EFAC);
  color: #16A34A;
}

.stat-card.expired .stat-icon {
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
  margin-bottom: 28px;
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
  color: #F59E0B;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #FBBF24, #F59E0B);
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
  color: #F59E0B;
}

.expire-soon {
  color: #DC2626;
  font-weight: 600;
}

.no-action {
  color: #CBD5E1;
}

/* 空状态 */
.empty-state {
  padding: 60px;
  text-align: center;
}

.empty-svg {
  width: 160px;
  height: 160px;
}

/* 须知卡片 */
.info-card {
  background: linear-gradient(135deg, rgba(254, 243, 199, 0.8), rgba(254, 252, 232, 0.6)) !important;
  border-color: rgba(245, 158, 11, 0.3) !important;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #B45309;
  margin-bottom: 20px;
}

.info-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-list li {
  padding: 14px 0;
  color: #92400E;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(245, 158, 11, 0.15);
}

.info-list li:last-child {
  border-bottom: none;
}

.info-list li::before {
  content: '•';
  color: #F59E0B;
  font-weight: bold;
  font-size: 18px;
}

.info-list li strong {
  color: #B45309;
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
