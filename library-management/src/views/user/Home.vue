<template>
  <div class="home-container">
    <!-- Hero区域 -->
    <div class="hero-section">
      <div class="hero-bg">
        <div class="liquid-shape shape-1"></div>
        <div class="liquid-shape shape-2"></div>
        <div class="liquid-shape shape-3"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">欢迎来到智典书屋</h1>
        <p class="hero-subtitle">探索海量藏书，开启智慧之旅</p>
        <el-button type="primary" size="large" round class="hero-btn" @click="$router.push('/user/books')">
          探索图书
        </el-button>
      </div>
      <div class="hero-stats">
        <div class="stat-box" v-for="stat in heroStats" :key="stat.label">
          <span class="stat-number">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>
    
    <!-- 快捷入口 - 悬浮卡片 -->
    <div class="quick-section">
      <div class="quick-grid">
        <div 
          class="quick-card" 
          v-for="link in quickLinks" 
          :key="link.title" 
          @click="handleQuickClick(link.path)"
        >
          <div class="quick-icon" :style="{ background: link.gradient }">
            <el-icon :size="32"><component :is="link.icon" /></el-icon>
          </div>
          <div class="quick-content">
            <span class="quick-title">{{ link.title }}</span>
            <span class="quick-desc">{{ link.description }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容 -->
    <div class="main-section">
      <!-- 借阅统计卡片 -->
      <div class="stats-card glass-card">
        <div class="stats-header">
          <h3 class="section-title">我的借阅</h3>
          <el-button text type="primary" @click="$router.push('/user/borrow')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon borrowing">
              <el-icon :size="28"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ borrowCount }}</span>
              <span class="stat-label">当前借阅</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon reserved">
              <el-icon :size="28"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ reserveCount }}</span>
              <span class="stat-label">预约图书</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon overdue">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ overdueCount }}</span>
              <span class="stat-label">逾期图书</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 热门推荐 -->
      <div class="section-card glass-card">
        <div class="section-header">
          <h3 class="section-title">热门推荐</h3>
          <el-button text type="primary" @click="$router.push('/user/books')">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="books-grid">
          <div 
            v-for="book in recommendedBooks" 
            :key="book.id" 
            class="book-card"
            @click="$router.push(`/user/book/${book.id}`)"
          >
            <div class="book-cover">
              <div v-if="book.coverUrl" class="cover-image">
                <img :src="book.coverUrl" :alt="book.title" />
              </div>
              <div v-else class="cover-placeholder">
                <svg viewBox="0 0 80 110" class="book-svg">
                  <rect x="5" y="5" width="70" height="100" rx="4" fill="#e0f2fe"/>
                  <rect x="10" y="15" width="60" height="3" fill="#0891B2" opacity="0.3"/>
                  <rect x="10" y="25" width="50" height="2" fill="#0891B2" opacity="0.2"/>
                  <rect x="10" y="50" width="40" height="30" rx="2" fill="#0891B2" opacity="0.15"/>
                </svg>
              </div>
              <div class="book-badge" :class="{ available: book.availableStock > 0 }">
                {{ book.availableStock > 0 ? '可借' : '已借出' }}
              </div>
            </div>
            <div class="book-info">
              <h4 class="book-title">{{ book.title }}</h4>
              <p class="book-author">{{ book.author }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 数字资源 -->
      <div class="section-card glass-card">
        <div class="section-header">
          <h3 class="section-title">数字资源</h3>
        </div>
        <div class="resources-grid">
          <div 
            v-for="resource in digitalResources" 
            :key="resource.title" 
            class="resource-card"
            @click="handleResourceClick(resource)"
          >
            <div class="resource-icon">
              <el-icon :size="36"><component :is="resource.icon" /></el-icon>
            </div>
            <h4 class="resource-title">{{ resource.title }}</h4>
            <p class="resource-desc">{{ resource.description }}</p>
          </div>
        </div>
      </div>

      <!-- 最近动态 -->
      <div class="section-card glass-card">
        <div class="section-header">
          <h3 class="section-title">最近动态</h3>
        </div>
        <div class="activity-list">
          <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
            <div class="activity-icon" :class="activity.type">
              <el-icon :size="20"><component :is="getActivityIcon(activity.type)" /></el-icon>
            </div>
            <div class="activity-content">
              <p class="activity-text">{{ activity.text }}</p>
              <span class="activity-time">{{ activity.time }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 通知公告 -->
      <div class="section-card glass-card">
        <div class="section-header">
          <h3 class="section-title">通知公告</h3>
          <el-button text type="primary">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="notice-list">
          <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="handleNoticeClick(notice)">
            <div class="notice-tag" :class="notice.type">{{ notice.tag }}</div>
            <div class="notice-content">
              <h4 class="notice-title">{{ notice.title }}</h4>
              <p class="notice-desc">{{ notice.description }}</p>
            </div>
            <span class="notice-time">{{ notice.time }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部 -->
    <div class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h4 class="footer-title">关于我们</h4>
          <p class="footer-text">智典书屋致力于为读者提供优质的图书借阅服务</p>
        </div>
        <div class="footer-section">
          <h4 class="footer-title">联系我们</h4>
          <p class="footer-text">地址：图书馆路123号</p>
          <p class="footer-text">电话：010-12345678</p>
          <p class="footer-text">邮箱：library@example.com</p>
        </div>
        <div class="footer-section follow-us">
          <h4 class="footer-title">关注我们</h4>
          <div class="qrcode">
            <el-icon :size="40"><Picture /></el-icon>
            <span>扫码关注</span>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2024 智典书屋. All rights reserved.</p>
      </div>
    </div>
    
    <!-- 回到顶部 -->
    <transition name="fade">
      <div v-if="showBackTop" class="back-top" @click="scrollToTop">
        <el-icon :size="24"><Top /></el-icon>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, Reading, Calendar, Warning, ArrowRight, Document, 
  Collection, VideoPlay, Notebook, Tickets, InfoFilled, Picture, Top
} from '@element-plus/icons-vue'
import { getUserStats } from '@/api/borrow'
import { getReserveCount } from '@/api/reservation'
import { getHotBooks } from '@/api/borrow'

const router = useRouter()

const borrowCount = ref(0)
const reserveCount = ref(0)
const overdueCount = ref(0)
const recommendedBooks = ref([])

const heroStats = [
  { value: '50,000+', label: '藏书量' },
  { value: '1,200+', label: '种类' },
  { value: '10,000+', label: '读者' },
  { value: '100+', label: '期刊' }
]

const quickLinks = [
  { title: '馆藏检索', description: '搜索图书资源', icon: 'Search', gradient: 'linear-gradient(135deg, #FEF3C7, #FDE68A)', path: '/user/books' },
  { title: '我的借阅', description: '查看借阅记录', icon: 'Reading', gradient: 'linear-gradient(135deg, #DCFCE7, #86EFAC)', path: '/user/borrow' },
  { title: '我的预约', description: '管理预约', icon: 'Calendar', gradient: 'linear-gradient(135deg, #FCE7F3, #F9A8D4)', path: '/user/reserve' },
  { title: '个人中心', description: '修改资料', icon: 'InfoFilled', gradient: 'linear-gradient(135deg, #DBEAFE, #93C5FD)', path: '/user/profile' }
]

const digitalResources = ref([
  { 
    title: '电子图书', 
    description: '10万+电子书在线阅读', 
    icon: 'Notebook'
  },
  { 
    title: '电子期刊', 
    description: '海量期刊论文资源', 
    icon: 'Document'
  },
  { 
    title: '音视频', 
    description: '讲座视频与音频资源', 
    icon: 'VideoPlay'
  },
  { 
    title: '特色数据库', 
    description: '专题数据库资源', 
    icon: 'Collection'
  }
])

const resourceTypeMap = {
  '电子图书': 'ebook',
  '电子期刊': 'journal',
  '音视频': 'media',
  '特色数据库': 'database'
}

const handleResourceClick = (resource) => {
  const type = resourceTypeMap[resource.title]
  if (type) {
    router.push(`/user/resources/${type}`)
  }
}

const recentActivities = ref([
  { type: 'borrow', text: '借阅《深入理解计算机系统》', time: '2小时前' },
  { type: 'reserve', text: '预约《百年孤独》', time: '昨天' },
  { type: 'return', text: '归还《JavaScript高级程序设计》', time: '3天前' }
])

const notices = ref([
  { id: 1, type: 'info', tag: '通知', title: '春节期间开放时间调整', description: '春节期间图书馆开放时间有所调整，请读者留意。', time: '2024-01-20' },
  { id: 2, type: 'event', tag: '活动', title: '世界读书日系列活动', description: '4月23日世界读书日系列活动火热进行中，欢迎参加。', time: '2024-04-15' },
  { id: 3, type: 'new', tag: '新书', title: '新书上架通知', description: '本期新书上架共计200余种，欢迎读者借阅。', time: '2024-04-10' }
])

const showBackTop = ref(false)

const handleScroll = () => {
  showBackTop.value = window.scrollY > 400
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleNoticeClick = (notice) => {
  console.log('点击公告:', notice)
}

const fetchStats = async () => {
  try {
    const stats = await getUserStats()
    borrowCount.value = stats.borrowingCount || 0
    overdueCount.value = stats.overdueCount || 0
  } catch (e) {
    console.error('获取借阅统计失败', e)
  }
}

const fetchReserveCount = async () => {
  try {
    const count = await getReserveCount()
    reserveCount.value = count || 0
  } catch (e) {
    console.error('获取预约统计失败', e)
  }
}

const fetchRecommendedBooks = async () => {
  try {
    const res = await getHotBooks(8)
    recommendedBooks.value = res || []
  } catch (e) {
    console.error('获取推荐图书失败', e)
    recommendedBooks.value = []
  }
}

onMounted(() => {
  fetchStats()
  fetchReserveCount()
  fetchRecommendedBooks()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const handleQuickClick = (path) => router.push(path)

const getActivityIcon = (type) => {
  const icons = { borrow: 'Reading', return: 'Select', reserve: 'Calendar' }
  return icons[type] || 'InfoFilled'
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%);
}

/* Hero区域 */
.hero-section {
  position: relative;
  padding: 100px 32px 80px;
  overflow: hidden;
  text-align: center;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0EA5E9 0%, #0284C7 50%, #0369A1 100%);
  overflow: hidden;
}

.liquid-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.4;
  animation: float 10s ease-in-out infinite;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: #38BDF8;
  top: -250px;
  left: -150px;
}

.shape-2 {
  width: 500px;
  height: 500px;
  background: #7DD3FC;
  bottom: -200px;
  right: -100px;
  animation-delay: -4s;
}

.shape-3 {
  width: 400px;
  height: 400px;
  background: #0EA5E9;
  top: 30%;
  right: 20%;
  animation-delay: -7s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1) rotate(0deg); }
  33% { transform: translate(30px, -40px) scale(1.05) rotate(5deg); }
  66% { transform: translate(-20px, 20px) scale(0.95) rotate(-5deg); }
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 16px;
  letter-spacing: 4px;
  text-shadow: 0 4px 30px rgba(0,0,0,0.2);
}

.hero-subtitle {
  font-size: 22px;
  color: rgba(255,255,255,0.85);
  margin-bottom: 40px;
}

.hero-btn {
  padding: 16px 48px;
  font-size: 18px;
  font-weight: 600;
  background: rgba(255,255,255,0.2) !important;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255,255,255,0.3) !important;
  color: #fff !important;
  transition: all 0.3s;
}

.hero-btn:hover {
  background: rgba(255,255,255,0.3) !important;
  transform: translateY(-3px);
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}

.hero-stats {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  gap: 80px;
  margin-top: 60px;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  text-shadow: 0 2px 20px rgba(0,0,0,0.15);
}

.stat-label {
  font-size: 16px;
  color: rgba(255,255,255,0.75);
  margin-top: 8px;
}

/* 快捷入口 */
.quick-section {
  max-width: 1200px;
  margin: -40px auto 0;
  padding: 0 24px;
  position: relative;
  z-index: 10;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.12);
}

.quick-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 48px rgba(14, 165, 233, 0.25);
  border-color: rgba(14, 165, 233, 0.3);
}

.quick-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0C4A6E;
  flex-shrink: 0;
}

.quick-content {
  display: flex;
  flex-direction: column;
}

.quick-title {
  font-size: 17px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 4px;
}

.quick-desc {
  font-size: 13px;
  color: #0369A1;
}

/* 主要内容 */
.main-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px;
}

/* 玻璃卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 28px;
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.1);
}

/* 统计卡片 */
.stats-card {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.9), rgba(2, 132, 199, 0.95)) !important;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.stats-header .el-button {
  color: rgba(255,255,255,0.9);
}

.stats-header .el-button:hover {
  background: rgba(255,255,255,0.15) !important;
  border-radius: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.borrowing { background: rgba(255,255,255,0.25); color: #FEF3C7; }
.stat-icon.reserved { background: rgba(255,255,255,0.25); color: #FCE7F3; }
.stat-icon.overdue { background: rgba(255,255,255,0.25); color: #FEE2E2; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
}

.stat-label {
  font-size: 15px;
  color: rgba(255,255,255,0.8);
  margin-top: 4px;
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.section-header .section-title {
  color: #0369A1;
}

.section-header .el-button {
  color: #0EA5E9;
}

/* 图书网格 */
.books-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.book-card {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.book-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(14, 165, 233, 0.2);
}

.book-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #E0F2FE, #BAE6FD);
  display: flex;
  align-items: center;
  justify-content: center;
}

.book-svg {
  width: 60px;
  height: 85px;
}

.book-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 5px 12px;
  background: rgba(239, 68, 68, 0.9);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 12px;
}

.book-badge.available {
  background: rgba(34, 197, 94, 0.9);
}

.book-info {
  padding: 16px;
}

.book-title {
  font-size: 15px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author {
  font-size: 13px;
  color: #0369A1;
}

/* 数字资源 */
.resources-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.resource-card {
  padding: 28px 20px;
  background: linear-gradient(135deg, rgba(224, 242, 254, 0.5), rgba(186, 230, 253, 0.3));
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.resource-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.2);
  border-color: rgba(14, 165, 233, 0.3);
}

.resource-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  background: rgba(255,255,255,0.8);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0EA5E9;
}

.resource-title {
  font-size: 16px;
  font-weight: 700;
  color: #0369A1;
  margin-bottom: 8px;
}

.resource-desc {
  font-size: 13px;
  color: #64748B;
}

/* 活动列表 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 14px;
  transition: all 0.2s;
}

.activity-item:hover {
  background: rgba(255, 255, 255, 0.8);
}

.activity-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-icon.borrow { background: #DBEAFE; color: #2563EB; }
.activity-icon.return { background: #DCFCE7; color: #16A34A; }
.activity-icon.reserve { background: #FEF3C7; color: #D97706; }

.activity-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-text {
  font-size: 15px;
  font-weight: 500;
  color: #0C4A6E;
}

.activity-time {
  font-size: 13px;
  color: #94A3B8;
}

/* 资源详情弹窗 */
.resource-detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.resource-detail-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #0EA5E9, #0284C7);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.resource-detail-info h3 {
  font-size: 22px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 8px;
}

.resource-detail-info p {
  font-size: 14px;
  color: #64748B;
}

.resource-detail-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #0369A1;
  margin-bottom: 16px;
}

.resource-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.resource-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: rgba(224, 242, 254, 0.3);
  border-radius: 10px;
  margin-bottom: 10px;
}

.resource-name {
  font-size: 14px;
  color: #0C4A6E;
}

.resource-detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #E0F2FE;
}

/* 响应式 */
@media (max-width: 1024px) {
  .quick-grid { grid-template-columns: repeat(2, 1fr); }
  .books-grid, .resources-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .hero-title { font-size: 32px; }
  .hero-subtitle { font-size: 16px; }
  .hero-stats { gap: 30px; flex-wrap: wrap; }
  .stat-number { font-size: 28px; }
  .quick-grid { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: 1fr; }
  .books-grid, .resources-grid { grid-template-columns: 1fr 1fr; }
}

/* 通知公告 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.notice-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateX(8px);
}

.notice-tag {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.notice-tag.info { background: #DBEAFE; color: #2563EB; }
.notice-tag.event { background: #FEF3C7; color: #D97706; }
.notice-tag.new { background: #DCFCE7; color: #16A34A; }

.notice-content {
  flex: 1;
  min-width: 0;
}

.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: #0C4A6E;
  margin-bottom: 4px;
}

.notice-desc {
  font-size: 13px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-time {
  font-size: 12px;
  color: #94A3B8;
  flex-shrink: 0;
}

/* 底部 */
.footer {
  background: linear-gradient(135deg, #0C4A6E 0%, #0369A1 100%);
  color: #fff;
  padding: 60px 24px 24px;
  margin-top: 40px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr 200px;
  gap: 48px;
}

.footer-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 20px;
}

.footer-text {
  font-size: 14px;
  color: rgba(255,255,255,0.75);
  margin-bottom: 8px;
}

.follow-us {
  text-align: center;
}

.qrcode {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: rgba(255,255,255,0.1);
  border-radius: 16px;
}

.qrcode span {
  font-size: 13px;
  color: rgba(255,255,255,0.8);
}

.footer-bottom {
  max-width: 1200px;
  margin: 40px auto 0;
  padding-top: 24px;
  border-top: 1px solid rgba(255,255,255,0.15);
  text-align: center;
}

.footer-bottom p {
  font-size: 13px;
  color: rgba(255,255,255,0.6);
}

/* 回到顶部 */
.back-top {
  position: fixed;
  right: 32px;
  bottom: 80px;
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #0EA5E9, #0284C7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.4);
  transition: all 0.3s;
  z-index: 100;
}

.back-top:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.5);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    text-align: center;
  }
  .back-top {
    right: 16px;
    bottom: 32px;
    width: 44px;
    height: 44px;
  }
}
</style>
