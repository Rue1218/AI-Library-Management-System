<template>
  <div class="book-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-glass">
        <el-button text @click="router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
    </div>

    <div class="book-content">
      <!-- 图书信息卡片 -->
      <div class="book-info-card glass-card">
        <div class="book-header">
          <div class="book-cover">
            <div v-if="book.coverUrl" class="cover-image">
              <img :src="book.coverUrl" :alt="book.title" />
            </div>
            <div v-else class="cover-placeholder large">
              <svg viewBox="0 0 120 160" class="book-svg">
                <rect x="8" y="8" width="104" height="144" rx="6" fill="#e0f2fe"/>
                <rect x="15" y="22" width="90" height="4" fill="#0891B2" opacity="0.3"/>
                <rect x="15" y="35" width="75" height="3" fill="#0891B2" opacity="0.2"/>
                <rect x="15" y="45" width="82" height="3" fill="#0891B2" opacity="0.2"/>
                <rect x="15" y="55" width="68" height="3" fill="#0891B2" opacity="0.2"/>
                <rect x="15" y="70" width="60" height="45" rx="3" fill="#0891B2" opacity="0.15"/>
              </svg>
            </div>
          </div>
          
          <div class="book-info">
            <h1 class="book-title">{{ book.title }}</h1>
            
            <div class="book-meta">
              <div class="meta-item">
                <span class="meta-label">作者</span>
                <span class="meta-value">{{ book.author }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">出版社</span>
                <span class="meta-value">{{ book.publisher }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">出版日期</span>
                <span class="meta-value">{{ book.publishDate }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">ISBN</span>
                <span class="meta-value">{{ book.isbn }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">分类</span>
                <span class="meta-value tag">{{ book.category }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">位置</span>
                <span class="meta-value">{{ book.location }}</span>
              </div>
            </div>
            
            <div class="stock-section">
              <div class="stock-card">
                <span class="stock-num">{{ book.totalStock }}</span>
                <span class="stock-text">总库存</span>
              </div>
              <div class="stock-card available">
                <span class="stock-num">{{ book.availableStock }}</span>
                <span class="stock-text">可借</span>
              </div>
            </div>
            
            <div class="action-buttons">
              <el-button 
                v-if="book.availableStock > 0" 
                type="primary" 
                size="large" 
                @click="handleBorrow"
                class="action-btn borrow-btn"
              >
                <el-icon><Reading /></el-icon>
                立即借阅
              </el-button>
              <el-button 
                v-else 
                type="info" 
                size="large" 
                disabled
                class="action-btn"
              >
                暂无库存
              </el-button>
              <el-button 
                size="large" 
                @click="handleReserve" 
                :disabled="book.availableStock > 0"
                class="action-btn reserve-btn"
              >
                <el-icon><Calendar /></el-icon>
                预约此书
              </el-button>
            </div>
          </div>
        </div>
        
        <div class="description-section">
          <h3 class="section-title">
            <el-icon><Document /></el-icon>
            图书简介
          </h3>
          <p class="description-text">{{ book.description || '暂无简介' }}</p>
        </div>
      </div>

      <!-- 评分卡片 -->
      <div class="rating-card glass-card">
        <div class="rating-header">
          <h3 class="section-title">
            <el-icon><Star /></el-icon>
            图书评分
          </h3>
          <el-button type="primary" size="small" @click="showCommentDialog = true" class="comment-btn">
            <el-icon><Edit /></el-icon>
            发表评论
          </el-button>
        </div>
        
        <div class="rating-content">
          <div class="rating-left">
            <div class="rating-score">{{ ratingData.averageRating.toFixed(1) }}</div>
            <el-rate v-model="ratingData.averageRating" disabled show-score text-color="#F59E0B" class="rating-stars" />
            <div class="rating-count">{{ ratingData.totalComments }} 条评价</div>
          </div>
          
          <div class="rating-right">
            <div class="rating-bar" v-for="n in 5" :key="n">
              <span class="bar-label">{{ 6 - n }} 星</span>
              <el-progress 
                :percentage="getRatingPercentage(6 - n)" 
                :stroke-width="10"
                :show-text="false"
                color="#F59E0B"
                class="rating-progress"
              />
              <span class="bar-count">{{ getRatingCount(6 - n) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 评论卡片 -->
      <div class="comments-card glass-card">
        <div class="comments-header">
          <h3 class="section-title">
            <el-icon><ChatDotRound /></el-icon>
            用户评论
          </h3>
        </div>
        
        <div v-if="comments.length === 0" class="empty-comments">
          <svg viewBox="0 0 120 120" class="empty-icon">
            <circle cx="60" cy="60" r="50" fill="#E0F2FE"/>
            <path d="M40 50h40M40 60h40M40 70h25" stroke="#0EA5E9" stroke-width="3" stroke-linecap="round"/>
          </svg>
          <p>暂无评论，快来抢沙发吧~</p>
        </div>
        
        <div v-else class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              <el-avatar :size="48" :src="comment.userAvatar" class="avatar">
                {{ comment.username?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-username">{{ comment.username }}</span>
                <el-rate v-model="comment.rating" disabled size="small" class="comment-rating" />
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              
              <div class="comment-text">{{ comment.content }}</div>
              
              <div class="comment-footer">
                <div 
                  class="like-button" 
                  :class="{ active: comment.isLiked, animating: comment.animating }"
                  @click="handleLike(comment)"
                >
                  <svg class="heart-icon" viewBox="0 0 24 24" fill="none">
                    <path 
                      class="heart-path"
                      d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" 
                      :fill="comment.isLiked ? '#EF4444' : 'none'"
                      :stroke="comment.isLiked ? '#EF4444' : '#94A3B8'"
                      stroke-width="2"
                    />
                  </svg>
                  <span class="like-count">{{ comment.likeCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评论弹窗 -->
    <el-dialog v-model="showCommentDialog" title="发表评论" width="520px" class="comment-dialog">
      <el-form :model="commentForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="commentForm.rating" :max="5" allow-half />
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input 
            v-model="commentForm.content" 
            type="textarea" 
            :rows="5" 
            placeholder="分享你的阅读体验..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCommentDialog = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, Star, Reading, Calendar, Document, ChatDotRound } from '@element-plus/icons-vue'
import { getBookDetail } from '@/api/books'
import { getComments, getBookRating, addComment, likeComment, unlikeComment } from '@/api/comment'
import { borrowBook } from '@/api/borrow'
import { reserveBook } from '@/api/reservation'
import { getToken, isTokenExpired } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const book = ref({})
const comments = ref([])
const ratingData = ref({
  averageRating: 0,
  totalComments: 0,
  rating5: 0,
  rating4: 0,
  rating3: 0,
  rating2: 0,
  rating1: 0
})
const showCommentDialog = ref(false)
const submitting = ref(false)

const commentForm = reactive({
  bookId: null,
  rating: 5,
  content: '',
  parentId: null
})

const fetchBookDetail = async () => {
  try {
    const bookId = route.params.id
    const res = await getBookDetail(bookId)
    book.value = res
    commentForm.bookId = Number(bookId)
  } catch (e) {
    ElMessage.error('获取图书详情失败')
  }
}

const fetchComments = async () => {
  try {
    const bookId = route.params.id
    const list = await getComments(bookId)
    comments.value = list.map(c => ({ ...c, animating: false }))
  } catch (e) {
    console.error('获取评论失败', e)
  }
}

const fetchRating = async () => {
  try {
    const bookId = route.params.id
    ratingData.value = await getBookRating(bookId)
  } catch (e) {
    console.error('获取评分失败', e)
  }
}

const getRatingPercentage = (rating) => {
  if (ratingData.value.totalComments === 0) return 0
  return Math.round(getRatingCount(rating) / ratingData.value.totalComments * 100)
}

const getRatingCount = (rating) => {
  switch (rating) {
    case 5: return ratingData.value.rating5 || 0
    case 4: return ratingData.value.rating4 || 0
    case 3: return ratingData.value.rating3 || 0
    case 2: return ratingData.value.rating2 || 0
    case 1: return ratingData.value.rating1 || 0
    default: return 0
  }
}

const submitComment = async () => {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    submitting.value = true
    await addComment(commentForm)
    ElMessage.success('评论成功')
    showCommentDialog.value = false
    commentForm.content = ''
    commentForm.rating = 5
    await Promise.all([fetchComments(), fetchRating()])
  } catch (e) {
    ElMessage.error(e.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

const handleLike = async (comment) => {
  if (comment.animating) return
  
  try {
    if (comment.isLiked) {
      await unlikeComment(comment.id)
    } else {
      await likeComment(comment.id)
      comment.animating = true
      setTimeout(() => { comment.animating = false }, 600)
    }
    comment.isLiked = !comment.isLiked
    comment.likeCount = comment.isLiked ? (comment.likeCount || 0) + 1 : (comment.likeCount || 1) - 1
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleBorrow = async () => {
  const token = getToken()
  if (!token || isTokenExpired(token)) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  const bookId = route.params.id
  try {
    await borrowBook(Number(bookId))
    ElMessage.success('借阅成功')
    await fetchBookDetail()
  } catch (e) {
    ElMessage.error(e.message || '借阅失败')
  }
}

const handleReserve = async () => {
  const token = getToken()
  if (!token || isTokenExpired(token)) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await reserveBook(book.value.id)
    ElMessage.success('预约成功')
  } catch (e) {
    ElMessage.error(e.message || '预约失败')
  }
}

onMounted(() => {
  fetchBookDetail()
  fetchComments()
  fetchRating()
})
</script>

<style scoped>
.book-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%);
  padding-bottom: 48px;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  padding: 16px 0;
  margin-bottom: 32px;
}

.header-glass {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #0369A1;
  font-weight: 500;
}

.back-btn:hover {
  color: #0EA5E9;
}

/* 内容区域 */
.book-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* 玻璃卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.1);
}

/* 图书信息 */
.book-header {
  display: flex;
  gap: 40px;
}

.book-cover {
  flex-shrink: 0;
}

.cover-image img {
  width: 220px;
  height: 300px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.2);
}

.cover-placeholder.large {
  width: 220px;
  height: 300px;
  background: linear-gradient(135deg, #E0F2FE, #BAE6FD);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.book-svg {
  width: 120px;
  height: 160px;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 32px;
  font-weight: 800;
  color: #0C4A6E;
  margin-bottom: 24px;
  line-height: 1.3;
}

.book-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 13px;
  color: #64748B;
}

.meta-value {
  font-size: 15px;
  color: #0C4A6E;
  font-weight: 500;
}

.meta-value.tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(14, 165, 233, 0.1);
  color: #0EA5E9;
  border-radius: 12px;
  font-size: 13px;
}

/* 库存 */
.stock-section {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
}

.stock-card {
  padding: 20px 32px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 16px;
  text-align: center;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.stock-card.available {
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.2);
}

.stock-num {
  display: block;
  font-size: 36px;
  font-weight: 800;
  color: #DC2626;
}

.stock-card.available .stock-num {
  color: #16A34A;
}

.stock-text {
  font-size: 13px;
  color: #64748B;
}

/* 按钮 */
.action-buttons {
  display: flex;
  gap: 14px;
}

.action-btn {
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.borrow-btn {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9) !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.35);
}

.borrow-btn:hover {
  box-shadow: 0 8px 28px rgba(14, 165, 233, 0.45);
}

.reserve-btn {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(14, 165, 233, 0.3) !important;
  color: #0EA5E9;
}

/* 简介 */
.description-section {
  margin-top: 32px;
  padding-top: 28px;
  border-top: 1px solid rgba(14, 165, 233, 0.15);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #0369A1;
  margin-bottom: 16px;
}

.description-text {
  font-size: 15px;
  line-height: 1.8;
  color: #475569;
}

/* 评分 */
.rating-header, .comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.comment-btn {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9) !important;
  border: none !important;
}

.rating-content {
  display: flex;
  gap: 48px;
  align-items: center;
}

.rating-left {
  text-align: center;
}

.rating-score {
  font-size: 56px;
  font-weight: 800;
  color: #F59E0B;
  line-height: 1;
}

.rating-stars {
  margin: 12px 0 8px;
}

.rating-count {
  font-size: 14px;
  color: #64748B;
}

.rating-right {
  flex: 1;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.bar-label {
  font-size: 13px;
  color: #64748B;
  width: 40px;
}

.bar-count {
  font-size: 13px;
  color: #64748B;
  width: 30px;
  text-align: right;
}

.rating-progress :deep(.el-progress-bar__outer) {
  background-color: rgba(245, 158, 11, 0.15);
}

/* 评论 */
.empty-comments {
  text-align: center;
  padding: 48px;
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin-bottom: 16px;
}

.empty-comments p {
  color: #64748B;
  font-size: 15px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(14, 165, 233, 0.1);
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.avatar {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  color: #fff;
  font-weight: 600;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.comment-username {
  font-weight: 600;
  color: #0C4A6E;
}

.comment-rating :deep(.el-rate__icon) {
  font-size: 14px !important;
}

.comment-time {
  font-size: 13px;
  color: #94A3B8;
  margin-left: auto;
}

.comment-text {
  font-size: 15px;
  line-height: 1.7;
  color: #475569;
  margin-bottom: 12px;
}

.comment-footer {
  display: flex;
  gap: 16px;
}

/* 点赞按钮 - 心形动画 */
.like-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.like-button:hover {
  background: rgba(239, 68, 68, 0.08);
  border-color: rgba(239, 68, 68, 0.2);
  transform: scale(1.05);
}

.like-button.active {
  background: transparent;
  border-color: transparent;
}

.heart-icon {
  width: 20px;
  height: 20px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.like-button.animating .heart-icon {
  animation: heartBeat 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  15% { transform: scale(1.3); }
  30% { transform: scale(0.95); }
  45% { transform: scale(1.15); }
  60% { transform: scale(0.98); }
  75% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.like-count {
  font-size: 14px;
  font-weight: 600;
  color: #94A3B8;
  transition: color 0.3s;
}

.like-button.active .like-count {
  color: #EF4444;
}

/* 旧样式移除
.like-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #94A3B8;
  cursor: pointer;
  transition: color 0.2s;
}

.like-btn:hover,
.like-btn.active {
  color: #F59E0B;
}
*/

/* 响应式 */
@media (max-width: 900px) {
  .book-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .book-meta {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .rating-content {
    flex-direction: column;
  }
}
</style>
