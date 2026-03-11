<template>
  <div class="page-container">
    <div class="bg-blob">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="page-header glass">
      <div class="header-left">
        <h2 class="page-title">数据统计</h2>
        <p class="page-desc">图书馆运营数据分析</p>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card glass">
        <div class="stat-icon books">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalBooks }}</span>
          <span class="stat-label">馆藏图书</span>
        </div>
        <div class="stat-trend" :class="stats.booksGrowth >= 0 ? 'up' : 'down'">
          <el-icon><TrendCharts /></el-icon>
          <span>{{ formatGrowth(stats.booksGrowth) }}</span>
        </div>
      </div>
      <div class="stat-card glass">
        <div class="stat-icon users">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalUsers }}</span>
          <span class="stat-label">注册读者</span>
        </div>
        <div class="stat-trend" :class="stats.usersGrowth >= 0 ? 'up' : 'down'">
          <el-icon><TrendCharts /></el-icon>
          <span>{{ formatGrowth(stats.usersGrowth) }}</span>
        </div>
      </div>
      <div class="stat-card glass">
        <div class="stat-icon borrows">
          <el-icon><Ticket /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalBorrows }}</span>
          <span class="stat-label">总借阅次数</span>
        </div>
        <div class="stat-trend" :class="stats.borrowsGrowth >= 0 ? 'up' : 'down'">
          <el-icon><TrendCharts /></el-icon>
          <span>{{ formatGrowth(stats.borrowsGrowth) }}</span>
        </div>
      </div>
      <div class="stat-card glass">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.overdueCount }}</span>
          <span class="stat-label">逾期未还</span>
        </div>
        <div class="stat-trend" :class="stats.overdueGrowth >= 0 ? 'down' : 'up'">
          <el-icon><TrendCharts /></el-icon>
          <span>{{ formatGrowth(stats.overdueGrowth) }}</span>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card glass">
        <div class="chart-header">
          <h3 class="chart-title">借阅趋势（近7天）</h3>
        </div>
        <div ref="trendChartRef" style="height: 320px;"></div>
      </div>
      <div class="chart-card glass">
        <div class="chart-header">
          <h3 class="chart-title">热门图书排行</h3>
        </div>
        <div ref="rankingChartRef" style="height: 320px;"></div>
      </div>
    </div>

    <div class="charts-row single">
      <div class="chart-card glass">
        <div class="chart-header">
          <h3 class="chart-title">图书分类占比</h3>
        </div>
        <div ref="categoryChartRef" style="height: 350px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, User, Ticket, Warning, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const API_BASE = 'http://localhost:8080/api'

const stats = ref({
  totalBooks: 0,
  totalUsers: 0,
  totalBorrows: 0,
  overdueCount: 0,
  booksGrowth: 0,
  usersGrowth: 0,
  borrowsGrowth: 0,
  overdueGrowth: 0
})

const trendChartRef = ref(null)
const rankingChartRef = ref(null)
const categoryChartRef = ref(null)

let trendChart = null
let rankingChart = null
let categoryChart = null

const formatGrowth = (value) => {
  if (value === null || value === undefined || value === 0) return '0%'
  const prefix = value > 0 ? '+' : ''
  return prefix + Math.round(value) + '%'
}

const fetchData = async () => {
  try {
    const token = localStorage.getItem('token')
    const res = await fetch(`${API_BASE}/statistics`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await res.json()
    console.log('统计数据返回:', result)
    
    if (result.code === 200) {
      const data = result.data
      console.log('数据详情:', data)
      stats.value.totalBooks = data.totalBooks
      stats.value.totalUsers = data.totalUsers
      stats.value.totalBorrows = data.totalBorrows
      stats.value.overdueCount = data.overdueCount
      stats.value.booksGrowth = data.booksGrowth
      stats.value.usersGrowth = data.usersGrowth
      stats.value.borrowsGrowth = data.borrowsGrowth
      stats.value.overdueGrowth = data.overdueGrowth
      
      console.log('增长率:', {
        booksGrowth: stats.value.booksGrowth,
        usersGrowth: stats.value.usersGrowth,
        borrowsGrowth: stats.value.borrowsGrowth,
        overdueGrowth: stats.value.overdueGrowth
      })
      
      renderTrendChart(data.borrowTrend || [])
      renderRankingChart(data.hotBooks || [])
      renderCategoryChart(data.categoryDistribution || [])
    } else {
      ElMessage.error(result.message || '统计数据加载失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('统计数据加载失败')
  }
}

const renderTrendChart = (trendData) => {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)

  const dates = trendData.map(d => d.date.slice(5))
  const counts = trendData.map(d => d.count)

  const option = {
    tooltip: { 
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#3B82F6',
      textStyle: { color: '#1E3A8A' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: dates,
      axisLine: { lineStyle: { color: '#E2E8F0' } },
      axisLabel: { color: '#64748B' }
    },
    yAxis: { 
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#F1F5F9' } },
      axisLabel: { color: '#64748B' }
    },
    series: [{
      data: counts,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#3B82F6', width: 3 },
      itemStyle: { color: '#3B82F6' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
          ]
        }
      }
    }]
  }
  trendChart.setOption(option)
}

const renderRankingChart = (hotBooks) => {
  if (!rankingChartRef.value) return
  if (!rankingChart) rankingChart = echarts.init(rankingChartRef.value)

  const sorted = hotBooks.map(book => ({
    title: book.title || '未知',
    count: book.count
  })).reverse()

  const option = {
    tooltip: { 
      trigger: 'axis', 
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#3B82F6',
      textStyle: { color: '#1E3A8A' }
    },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { 
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#F1F5F9' } },
      axisLabel: { color: '#64748B' }
    },
    yAxis: { 
      type: 'category', 
      data: sorted.map(s => s.title),
      axisLine: { lineStyle: { color: '#E2E8F0' } },
      axisLabel: { color: '#1E3A8A', fontSize: 12 }
    },
    series: [{
      data: sorted.map(s => s.count),
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [
            { offset: 0, color: '#60A5FA' },
            { offset: 1, color: '#3B82F6' }
          ]
        },
        borderRadius: [0, 8, 8, 0]
      }
    }]
  }
  rankingChart.setOption(option)
}

const renderCategoryChart = (categories) => {
  if (!categoryChartRef.value) return
  if (!categoryChart) categoryChart = echarts.init(categoryChartRef.value)

  const colors = ['#3B82F6', '#60A5FA', '#93C5FD', '#DBEAFE', '#2563EB', '#1D4ED8', '#1E40AF']
  const data = categories.map((c, i) => ({ value: c.value, name: c.name, itemStyle: { color: colors[i % colors.length] } }))

  const option = {
    tooltip: { 
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#3B82F6',
      textStyle: { color: '#1E3A8A' }
    },
    legend: { bottom: '0%', left: 'center', textStyle: { color: '#64748B' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 12,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#1E3A8A' },
        itemStyle: { shadowBlur: 20, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.2)' }
      },
      labelLine: { show: false },
      data
    }]
  }
  categoryChart.setOption(option)
}

const handleResize = () => {
  trendChart?.resize()
  rankingChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  rankingChart?.dispose()
  categoryChart?.dispose()
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
  opacity: 0.35;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  top: -150px;
  right: -100px;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #10B981, #34D399);
  top: 30%;
  left: -100px;
}

.blob-3 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #F59E0B, #FBBF24);
  bottom: -100px;
  right: 20%;
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
  margin-bottom: 24px;
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: 20px;
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
  height: 4px;
  background: linear-gradient(90deg, #3B82F6, #60A5FA);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(59, 130, 246, 0.15);
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
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.stat-trend.up {
  color: #059669;
  background: rgba(16, 185, 129, 0.1);
}

.stat-trend.down {
  color: #DC2626;
  background: rgba(239, 68, 68, 0.1);
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.charts-row.single {
  grid-template-columns: 1fr;
}

.chart-card {
  padding: 24px;
  border-radius: 24px;
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 16px 32px rgba(59, 130, 246, 0.12);
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 17px;
  font-weight: 600;
  color: #1E3A8A;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid,
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
