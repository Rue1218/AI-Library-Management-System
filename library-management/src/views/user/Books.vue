<template>
  <div class="books-page">
    <!-- Hero搜索区域 -->
    <div class="hero-section">
      <div class="hero-bg">
        <div class="liquid-shape shape-1"></div>
        <div class="liquid-shape shape-2"></div>
        <div class="liquid-shape shape-3"></div>
        <div class="hero-pattern"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">探索知识的海洋</h1>
        <p class="hero-subtitle">海量藏书，随心借阅，发现更多精彩</p>
        
        <div class="search-wrapper">
          <div class="search-glass">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索书名、作者、ISBN..."
              size="large"
              clearable
              @keyup.enter="handleSearch"
              class="hero-search"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch" class="search-btn">搜索</el-button>
          </div>
        </div>
        
        <div class="quick-tags">
          <span class="tag-label">热门：</span>
          <span 
            v-for="tag in quickTags" 
            :key="tag" 
            class="tag-item"
            @click="searchByTag(tag)"
          >
            {{ tag }}
          </span>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-section">
      <div class="content-container">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <div class="filter-left">
            <el-select 
              v-model="searchForm.category" 
              placeholder="全部分类" 
              clearable 
              @change="handleSearch"
              class="filter-select liquid-select"
            >
              <el-option
                v-for="cat in categories"
                :key="cat"
                :label="cat"
                :value="cat"
              />
            </el-select>
            
            <el-select 
              v-model="searchForm.status" 
              placeholder="借阅状态" 
              clearable 
              @change="handleSearch"
              class="filter-select liquid-select"
            >
              <el-option label="可借" value="available" />
              <el-option label="已借出" value="unavailable" />
            </el-select>
          </div>
          
          <div class="filter-right">
            <span class="result-count">共 {{ total }} 本图书</span>
            <el-radio-group v-model="searchForm.sort" size="small" @change="handleSearch" class="sort-group">
              <el-radio-button value="relevance">相关度</el-radio-button>
              <el-radio-button value="latest">最新</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-grid">
          <div v-for="i in 8" :key="i" class="skeleton-card">
            <div class="skeleton-cover"></div>
            <div class="skeleton-info">
              <div class="skeleton-line title"></div>
              <div class="skeleton-line author"></div>
              <div class="skeleton-line meta"></div>
            </div>
          </div>
        </div>

        <!-- 图书列表 -->
        <div v-else-if="books.length > 0" class="books-grid">
          <div 
            v-for="book in books" 
            :key="book.id" 
            class="book-card"
            @click="showBookDetail(book)"
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
                  <rect x="10" y="32" width="55" height="2" fill="#0891B2" opacity="0.2"/>
                  <rect x="10" y="39" width="45" height="2" fill="#0891B2" opacity="0.2"/>
                  <rect x="10" y="50" width="40" height="30" rx="2" fill="#0891B2" opacity="0.15"/>
                </svg>
              </div>
              <div class="book-badge" :class="{ available: book.availableStock > 0 }">
                {{ book.availableStock > 0 ? '可借' : '已借出' }}
              </div>
            </div>
            
            <div class="book-info">
              <h3 class="book-title">{{ book.title }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-publisher">{{ book.publisher }}</p>
              <div class="book-meta">
                <span class="meta-item">
                  <el-icon><Collection /></el-icon>
                  {{ book.availableStock }}/{{ book.totalStock }}
                </span>
                <span class="meta-item" v-if="book.category">
                  <el-icon><Folder /></el-icon>
                  {{ book.category }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 120 120" fill="none">
              <circle cx="60" cy="60" r="50" fill="#f1f5f9"/>
              <path d="M40 45h40v5H40zM40 55h40v5H40zM40 65h25v5H40zM40 75h30v5H40z" fill="#cbd5e1"/>
              <circle cx="85" cy="35" r="15" fill="#0891B2" opacity="0.2"/>
              <path d="M80 35l5 5 10-10" stroke="#0891B2" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3>未找到相关图书</h3>
          <p>试试其他关键词或调整筛选条件</p>
          <el-button type="primary" @click="resetSearch">重新搜索</el-button>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="searchForm.page"
            v-model:page-size="searchForm.pageSize"
            :page-sizes="[12, 24, 36, 48]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Collection, Folder } from '@element-plus/icons-vue'
import { getBooks, getBookCategories } from '@/api/books'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const books = ref([])
const categories = ref([])
const total = ref(0)

const quickTags = ['Python', '算法', '文学', '历史', '科幻']

const searchForm = reactive({
  keyword: '',
  category: '',
  status: '',
  sort: 'relevance',
  page: 1,
  pageSize: 12
})

const fetchBooks = async () => {
  loading.value = true
  try {
    const params = {
      page: searchForm.page,
      pageSize: searchForm.pageSize
    }
    
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    
    if (searchForm.category) {
      params.category = searchForm.category
    }
    
    const res = await getBooks(params)
    
    let bookList = res.records || []
    
    if (searchForm.status === 'available') {
      bookList = bookList.filter(b => b.availableStock > 0)
    } else if (searchForm.status === 'unavailable') {
      bookList = bookList.filter(b => b.availableStock <= 0)
    }
    
    if (searchForm.sort === 'latest') {
      bookList.sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
    }
    
    books.value = bookList
    total.value = res.total || 0
  } catch (e) {
    console.error('获取图书列表失败', e)
    ElMessage.error('获取图书列表失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getBookCategories()
    categories.value = res || []
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

const handleSearch = () => {
  searchForm.page = 1
  fetchBooks()
}

const handlePageChange = () => {
  fetchBooks()
}

const handleSizeChange = () => {
  searchForm.page = 1
  fetchBooks()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = ''
  searchForm.sort = 'relevance'
  searchForm.page = 1
  fetchBooks()
}

const searchByTag = (tag) => {
  searchForm.keyword = tag
  handleSearch()
}

const showBookDetail = (book) => {
  router.push(`/user/book/${book.id}`)
}

onMounted(() => {
  if (route.query.keyword) {
    searchForm.keyword = route.query.keyword
  }
  fetchCategories()
  fetchBooks()
})
</script>

<style scoped>
.books-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%);
}

/* Hero区域 - 液态渐变 */
.hero-section {
  position: relative;
  padding: 80px 32px 60px;
  overflow: hidden;
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
  filter: blur(80px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: #38BDF8;
  top: -200px;
  left: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: #7DD3FC;
  bottom: -150px;
  right: -50px;
  animation-delay: -3s;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: #0EA5E9;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 40%);
}

.hero-content {
  position: relative;
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  font-size: 52px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 12px;
  letter-spacing: 4px;
  text-shadow: 0 4px 20px rgba(0,0,0,0.2);
}

.hero-subtitle {
  font-size: 20px;
  color: rgba(255,255,255,0.85);
  margin-bottom: 40px;
}

.search-wrapper {
  max-width: 640px;
  margin: 0 auto 28px;
}

.search-glass {
  display: flex;
  gap: 12px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.hero-search :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.95);
  border: none;
  border-radius: 14px;
  box-shadow: none;
}

.hero-search :deep(.el-input__inner) {
  font-size: 16px;
  color: #0C4A6E;
}

.search-icon {
  color: #0EA5E9;
  font-size: 20px;
}

.search-btn {
  height: 40px;
  background: linear-gradient(135deg, #38BDF8, #0EA5E9) !important;
  border: none !important;
  padding: 0 28px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.4);
  transition: all 0.3s;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.5);
}

.quick-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.tag-label {
  color: rgba(255,255,255,0.75);
  font-size: 14px;
}

.tag-item {
  padding: 8px 18px;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  background: rgba(255,255,255,0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

/* 内容区域 */
.content-section {
  padding: 40px 32px;
}

.content-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* 筛选栏 - 液态玻璃 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(14, 165, 233, 0.1);
}

.filter-left {
  display: flex;
  gap: 14px;
}

.filter-select {
  width: 150px;
}

.liquid-select :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 12px;
  box-shadow: none;
}

.liquid-select :deep(.el-select__wrapper:hover) {
  border-color: rgba(14, 165, 233, 0.4);
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.result-count {
  color: #0369A1;
  font-size: 15px;
  font-weight: 500;
}

.sort-group :deep(.el-radio-button__inner) {
  border: none;
  background: transparent;
  color: #64748B;
}

.sort-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  color: #fff;
  border-radius: 12px;
}

/* 图书网格 */
.books-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28px;
}

.book-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.08);
}

.book-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(14, 165, 233, 0.2);
  border-color: rgba(14, 165, 233, 0.3);
}

.book-cover {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.book-card:hover .cover-image img {
  transform: scale(1.05);
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
  width: 80px;
  height: 110px;
}

.book-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  padding: 6px 14px;
  background: rgba(239, 68, 68, 0.9);
  backdrop-filter: blur(10px);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 14px;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.book-badge.available {
  background: rgba(34, 197, 94, 0.9);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.book-info {
  padding: 18px;
}

.book-title {
  font-size: 16px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 14px;
  color: #0369A1;
  margin-bottom: 4px;
}

.book-publisher {
  font-size: 12px;
  color: #7DD3FC;
  margin-bottom: 14px;
}

.book-meta {
  display: flex;
  gap: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #64748B;
}

.meta-item .el-icon {
  color: #0EA5E9;
}

/* 加载骨架屏 */
.loading-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28px;
}

.skeleton-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
}

.skeleton-cover {
  height: 220px;
  background: linear-gradient(90deg, #E0F2FE 25%, #BAE6FD 50%, #E0F2FE 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: 18px;
}

.skeleton-line {
  height: 14px;
  background: linear-gradient(90deg, #E0F2FE 25%, #BAE6FD 50%, #E0F2FE 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
  margin-bottom: 12px;
}

.skeleton-line.title { width: 80%; }
.skeleton-line.author { width: 60%; }
.skeleton-line.meta { width: 40%; }

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 100px 20px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 24px;
}

.empty-icon {
  width: 140px;
  height: 140px;
  margin: 0 auto 28px;
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 700;
  color: #0369A1;
  margin-bottom: 10px;
}

.empty-state p {
  color: #64748B;
  margin-bottom: 28px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 48px;
}

.pagination-wrapper :deep(.el-pagination) {
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 8px;
  border-radius: 16px;
}

.pagination-wrapper :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
}

.pagination-wrapper :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  color: #fff;
}

/* 响应式 */
@media (max-width: 1200px) {
  .books-grid, .loading-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 900px) {
  .books-grid, .loading-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .hero-section { padding: 50px 20px 40px; }
  .hero-title { font-size: 32px; }
  .hero-subtitle { font-size: 16px; }
  .content-section { padding: 24px 20px; }
  .filter-bar { flex-direction: column; gap: 16px; }
  .filter-left { flex-wrap: wrap; width: 100%; }
  .filter-select { flex: 1; min-width: 130px; }
  .books-grid, .loading-grid { grid-template-columns: 1fr; }
}
</style>
