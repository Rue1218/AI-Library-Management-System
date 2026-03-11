<template>
  <div class="dashboard-container">
    <div class="bg-decoration">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">管理控制台</h1>
        <p class="welcome-desc">欢迎回来，管理员</p>
      </div>
      <div class="welcome-actions">
        <el-button class="action-btn primary" :icon="Plus" @click="router.push('/admin/books')">添加图书</el-button>
        <el-button
          class="action-btn refresh-btn"
          :icon="Refresh"
          :loading="refreshing"
          :disabled="refreshing"
          @click="refreshDashboard()"
        >
          {{ refreshing ? '刷新中...' : '刷新数据' }}
        </el-button>
      </div>
    </div>

    <div class="stats-grid">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="stat-card glass"
      >
        <template v-if="showStatsSkeleton">
          <div class="skeleton-icon shimmer"></div>
          <div class="stat-info">
            <span class="skeleton-line value shimmer"></span>
            <span class="skeleton-line label shimmer"></span>
          </div>
          <div class="skeleton-pill shimmer"></div>
        </template>
        <template v-else>
          <transition name="content-fade" mode="out-in">
            <div :key="statsRenderKey" class="stat-card-content">
              <div class="stat-icon" :class="card.iconClass">
                <el-icon><component :is="card.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ card.value }}</span>
                <span class="stat-label">{{ card.label }}</span>
              </div>
              <div class="stat-trend" :class="card.trendClass">
                <el-icon><TrendCharts /></el-icon>
                <span>{{ formatGrowth(card.growth) }}</span>
              </div>
            </div>
          </transition>
        </template>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card glass">
        <div class="chart-header">
          <h3 class="chart-title">借阅趋势（近7天）</h3>
          <el-button text type="primary" @click="handleQuickClick('/admin/statistics')">查看详情</el-button>
        </div>
        <div class="chart-content" @click="handleQuickClick('/admin/statistics')">
          <div v-if="showChartsSkeleton" class="chart-skeleton">
            <div class="skeleton-chart-area shimmer"></div>
            <div class="chart-labels skeleton-labels">
              <span v-for="day in skeletonDays" :key="day" class="skeleton-label shimmer"></span>
            </div>
          </div>
          <transition v-else name="content-fade" mode="out-in">
            <div :key="chartsRenderKey" class="chart-placeholder">
              <svg viewBox="0 0 400 200" class="chart-svg" v-if="borrowTrend.length === 0">
                <defs>
                  <linearGradient id="chartGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:#3B82F6;stop-opacity:0.4" />
                    <stop offset="100%" style="stop-color:#3B82F6;stop-opacity:0" />
                  </linearGradient>
                </defs>
                <polyline points="0,160 50,140 100,150 150,120 200,100 250,80 300,90 350,60 400,40" 
                  fill="none" stroke="#3B82F6" stroke-width="3"/>
                <polygon points="0,160 50,140 100,150 150,120 200,100 250,80 300,90 350,60 400,40 400,200 0,200" 
                  fill="url(#chartGrad)"/>
              </svg>
              <svg viewBox="0 0 400 200" class="chart-svg" v-else>
                <defs>
                  <linearGradient id="chartGradDynamic" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:#3B82F6;stop-opacity:0.4" />
                    <stop offset="100%" style="stop-color:#3B82F6;stop-opacity:0" />
                  </linearGradient>
                </defs>
                <polyline :points="getTrendPoints()" 
                  fill="none" stroke="#3B82F6" stroke-width="3"/>
                <polygon :points="getTrendArea()" 
                  fill="url(#chartGradDynamic)"/>
              </svg>
              <div class="chart-labels">
                <span v-for="day in borrowTrend" :key="day.date">{{ formatDate(day.date) }}</span>
                <span v-if="borrowTrend.length === 0">周一</span>
                <span v-if="borrowTrend.length === 0">周二</span>
                <span v-if="borrowTrend.length === 0">周三</span>
                <span v-if="borrowTrend.length === 0">周四</span>
                <span v-if="borrowTrend.length === 0">周五</span>
                <span v-if="borrowTrend.length === 0">周六</span>
                <span v-if="borrowTrend.length === 0">周日</span>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <div class="chart-card glass">
        <div class="chart-header">
          <h3 class="chart-title">热门图书排行</h3>
          <el-button text type="primary" @click="handleQuickClick('/admin/statistics')">查看详情</el-button>
        </div>
        <div class="chart-content" @click="handleQuickClick('/admin/statistics')">
          <div v-if="showChartsSkeleton" class="book-rank skeleton-rank">
            <div v-for="item in 5" :key="item" class="rank-item">
              <span class="skeleton-rank-num shimmer"></span>
              <div class="rank-info">
                <span class="skeleton-line rank-title-line shimmer"></span>
                <span class="skeleton-line rank-count-line shimmer"></span>
              </div>
              <div class="rank-bar shimmer"></div>
            </div>
          </div>
          <transition v-else name="content-fade" mode="out-in">
            <div :key="chartsRenderKey" class="book-rank">
              <div v-for="(book, index) in hotBooks" :key="book.title" class="rank-item">
                <span class="rank-num" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                <div class="rank-info">
                  <span class="rank-title">{{ book.title }}</span>
                  <span class="rank-count">{{ book.count }}次借阅</span>
                </div>
                <div class="rank-bar">
                  <div class="rank-fill" :style="{ width: book.percent + '%' }"></div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <div class="quick-section">
      <h3 class="section-title">快捷操作</h3>
      <div class="quick-grid">
        <div class="quick-card glass" @click="$router.push('/admin/books')">
          <div class="quick-icon">
            <el-icon><Reading /></el-icon>
          </div>
          <span class="quick-label">图书管理</span>
        </div>
        <div class="quick-card glass" @click="$router.push('/admin/borrow')">
          <div class="quick-icon">
            <el-icon><Ticket /></el-icon>
          </div>
          <span class="quick-label">借阅管理</span>
        </div>
        <div class="quick-card glass" @click="$router.push('/admin/users')">
          <div class="quick-icon">
            <el-icon><User /></el-icon>
          </div>
          <span class="quick-label">读者管理</span>
        </div>
        <div class="quick-card glass" @click="$router.push('/admin/statistics')">
          <div class="quick-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <span class="quick-label">数据统计</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Reading, User, Ticket, Warning, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import { getAdminStats, getBorrowTrend, getHotBooks } from '@/api/borrow'

const router = useRouter()
const ANIMATION_DURATION = 600

const stats = ref({
  books: 0,
  users: 0,
  borrows: 0,
  overdue: 0,
  booksGrowth: 0,
  usersGrowth: 0,
  borrowsGrowth: 0,
  overdueGrowth: 0
})
const displayStats = ref({
  books: 0,
  users: 0,
  borrows: 0,
  overdue: 0
})

const hotBooks = ref([])
const borrowTrend = ref([])
const refreshing = ref(false)
const hasLoaded = ref(false)
const statsRenderKey = ref(0)
const chartsRenderKey = ref(0)
const skeletonDays = ['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun']

const showStatsSkeleton = computed(() => refreshing.value && !hasLoaded.value)
const showChartsSkeleton = computed(() => refreshing.value && !hasLoaded.value)
const statCards = computed(() => [
  {
    key: 'books',
    icon: Reading,
    iconClass: 'books',
    value: displayStats.value.books,
    label: '馆藏图书',
    growth: stats.value.booksGrowth,
    trendClass: stats.value.booksGrowth >= 0 ? 'up' : 'down'
  },
  {
    key: 'users',
    icon: User,
    iconClass: 'users',
    value: displayStats.value.users,
    label: '注册读者',
    growth: stats.value.usersGrowth,
    trendClass: stats.value.usersGrowth >= 0 ? 'up' : 'down'
  },
  {
    key: 'borrows',
    icon: Ticket,
    iconClass: 'borrows',
    value: displayStats.value.borrows,
    label: '借阅次数',
    growth: stats.value.borrowsGrowth,
    trendClass: stats.value.borrowsGrowth >= 0 ? 'up' : 'down'
  },
  {
    key: 'overdue',
    icon: Warning,
    iconClass: 'overdue',
    value: displayStats.value.overdue,
    label: '逾期未还',
    growth: stats.value.overdueGrowth,
    trendClass: stats.value.overdueGrowth >= 0 ? 'down' : 'up'
  }
])

const formatGrowth = (value) => {
  if (value === null || value === undefined || value === 0) return '0%'
  const prefix = value > 0 ? '+' : ''
  return prefix + Math.round(value) + '%'
}

const fetchStats = async () => {
  try {
    const data = await getAdminStats()
    stats.value = {
      books: data.books || 0,
      users: data.users || 0,
      borrows: data.borrows || 0,
      overdue: data.overdue || 0,
      booksGrowth: data.booksGrowth || 0,
      usersGrowth: data.usersGrowth || 0,
      borrowsGrowth: data.borrowsGrowth || 0,
      overdueGrowth: data.overdueGrowth || 0
    }
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

const fetchHotBooks = async () => {
  try {
    const books = await getHotBooks(5)
    const maxCount = books.length > 0 ? books[0].count : 1
    hotBooks.value = books.map(book => ({
      title: book.title,
      count: book.count,
      percent: Math.round((book.count / maxCount) * 100)
    }))
  } catch (e) {
    console.error('获取热门图书失败', e)
  }
}

const fetchBorrowTrend = async () => {
  try {
    const data = await getBorrowTrend()
    borrowTrend.value = data || []
  } catch (e) {
    console.error('获取借阅趋势失败', e)
  }
}

const refreshDashboard = async (showMessage = true) => {
  if (refreshing.value) return

  refreshing.value = true
  try {
    await Promise.all([
      fetchStats(),
      fetchHotBooks(),
      fetchBorrowTrend()
    ])

    if (showMessage) {
      ElMessage.success('数据已刷新')
    }
    hasLoaded.value = true
    statsRenderKey.value += 1
    chartsRenderKey.value += 1
  } catch (e) {
    if (showMessage) {
      ElMessage.error('刷新失败，请稍后重试')
    }
  } finally {
    refreshing.value = false
  }
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return (date.getMonth() + 1) + '-' + date.getDate()
}

const getMaxCount = () => {
  if (borrowTrend.value.length === 0) return 1
  return Math.max(...borrowTrend.value.map(d => d.count || 0), 1)
}

const getTrendPoints = () => {
  if (borrowTrend.value.length === 0) return ''
  const max = getMaxCount()
  const width = 400
  const height = 200
  const padding = 20
  return borrowTrend.value.map((d, i) => {
    const x = padding + (i * (width - 2 * padding) / (borrowTrend.value.length - 1))
    const y = height - padding - ((d.count || 0) / max) * (height - 2 * padding)
    return `${x},${y}`
  }).join(' ')
}

const getTrendArea = () => {
  if (borrowTrend.value.length === 0) return ''
  const max = getMaxCount()
  const width = 400
  const height = 200
  const padding = 20
  const points = borrowTrend.value.map((d, i) => {
    const x = padding + (i * (width - 2 * padding) / (borrowTrend.value.length - 1))
    const y = height - padding - ((d.count || 0) / max) * (height - 2 * padding)
    return `${x},${y}`
  })
  const lastX = padding + ((borrowTrend.value.length - 1) * (width - 2 * padding) / (borrowTrend.value.length - 1))
  return points.join(' ') + ` ${lastX},${height} ${padding},${height}`
}

const handleQuickClick = (path) => {
  router.push(path)
}

onMounted(() => {
  refreshDashboard(false)
})
</script>

<style scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 24px;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.blob-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #60A5FA, #3B82F6);
  top: -100px;
  right: -100px;
  animation: float 20s ease-in-out infinite;
}

.blob-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #93C5FD, #60A5FA);
  bottom: 20%;
  left: -50px;
  animation: float 15s ease-in-out infinite reverse;
}

.blob-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #DBEAFE, #93C5FD);
  top: 50%;
  right: 20%;
  animation: float 18s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.welcome-section {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(219, 234, 254, 0.25) 100%);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: #1E3A8A;
  margin-bottom: 8px;
}

.welcome-desc {
  font-size: 15px;
  color: #3B82F6;
  opacity: 0.8;
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.7);
  color: #1E3A8A;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
}

.action-btn.primary {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border-color: #3B82F6;
  color: #fff;
}

.action-btn.primary:hover {
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
}

.refresh-btn:deep(.el-icon.is-loading) {
  color: currentColor;
}

.stat-card-content {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
}

.shimmer {
  position: relative;
  overflow: hidden;
  background: rgba(191, 219, 254, 0.55);
}

.shimmer::after {
  content: '';
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.7), transparent);
  animation: shimmer 1.3s ease-in-out infinite;
}

@keyframes shimmer {
  100% { transform: translateX(100%); }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
  position: relative;
  z-index: 1;
}

.stat-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #3B82F6, #60A5FA);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.3);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:nth-child(2)::before { background: linear-gradient(90deg, #10B981, #34D399); }
.stat-card:nth-child(3)::before { background: linear-gradient(90deg, #F59E0B, #FBBF24); }
.stat-card:nth-child(4)::before { background: linear-gradient(90deg, #EF4444, #F87171); }

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
}

.stat-card.books .stat-icon {
  background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  color: #2563EB;
}

.stat-card.users .stat-icon {
  background: linear-gradient(135deg, #D1FAE5, #A7F3D0);
  color: #059669;
}

.stat-card.borrows .stat-icon {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  color: #D97706;
}

.stat-card.overdue .stat-icon {
  background: linear-gradient(135deg, #FEE2E2, #FECACA);
  color: #DC2626;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.skeleton-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  flex-shrink: 0;
}

.skeleton-line {
  display: block;
  border-radius: 999px;
}

.skeleton-line.value {
  width: 72px;
  height: 28px;
  margin-bottom: 10px;
}

.skeleton-line.label {
  width: 96px;
  height: 14px;
}

.skeleton-pill {
  width: 60px;
  height: 28px;
  border-radius: 999px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1E3A8A;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #64748B;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.5);
}

.stat-trend.up {
  color: #059669;
}

.stat-trend.down {
  color: #DC2626;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 28px;
  position: relative;
  z-index: 1;
}

.chart-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 24px;
  padding: 24px;
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 20px 40px rgba(59, 130, 246, 0.12);
  border-color: rgba(59, 130, 246, 0.2);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 17px;
  font-weight: 600;
  color: #1E3A8A;
}

.chart-content {
  min-height: 240px;
  cursor: pointer;
  border-radius: 12px;
  transition: background 0.3s ease;
}

.chart-content:hover {
  background: rgba(59, 130, 246, 0.03);
}

.chart-placeholder {
  height: 100%;
}

.chart-skeleton {
  height: 100%;
}

.skeleton-chart-area {
  width: 100%;
  height: 180px;
  border-radius: 18px;
}

.skeleton-labels {
  gap: 10px;
}

.skeleton-label {
  width: 36px;
  height: 10px;
  border-radius: 999px;
}

.chart-svg {
  width: 100%;
  height: 180px;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
  font-size: 12px;
  color: #94A3B8;
  margin-top: 8px;
}

.book-rank {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.rank-item:hover {
  background: rgba(59, 130, 246, 0.05);
}

.rank-num {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #64748B;
  background: #F1F5F9;
  transition: all 0.3s ease;
}

.rank-num.top {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  color: #fff;
}

.skeleton-rank-num {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  flex-shrink: 0;
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-title-line {
  width: 72%;
  height: 14px;
  margin-bottom: 8px;
}

.rank-count-line {
  width: 42%;
  height: 11px;
}

.rank-title {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #1E3A8A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-count {
  font-size: 12px;
  color: #94A3B8;
}

.rank-bar {
  width: 80px;
  height: 6px;
  background: #E2E8F0;
  border-radius: 3px;
  overflow: hidden;
}

.rank-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #60A5FA);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.content-fade-enter-active,
.content-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.content-fade-enter-from,
.content-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

.quick-section {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 24px;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #1E3A8A;
  margin-bottom: 20px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.quick-card:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(59, 130, 246, 0.3);
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(59, 130, 246, 0.15);
}

.quick-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  color: #2563EB;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  margin-bottom: 12px;
  transition: transform 0.3s ease;
}

.quick-card:hover .quick-icon {
  transform: scale(1.1) rotate(5deg);
}

.quick-label {
  font-size: 14px;
  font-weight: 500;
  color: #1E3A8A;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .stats-grid,
  .charts-row,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
