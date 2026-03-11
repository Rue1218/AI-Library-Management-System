<template>
  <div class="resources-page">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2 class="page-title">{{ pageTitle }}</h2>
      </div>
    </div>

    <div class="resources-content">
      <div class="resource-card" v-for="item in resourceItems" :key="item.name">
        <div class="resource-icon">
          <el-icon :size="40"><Link /></el-icon>
        </div>
        <div class="resource-info">
          <h3 class="resource-name">{{ item.name }}</h3>
          <p class="resource-desc">{{ item.description }}</p>
        </div>
        <div class="resource-action">
          <el-tag :type="item.available ? 'success' : 'info'" effect="dark">
            {{ item.available ? '可访问' : '暂不可用' }}
          </el-tag>
          <el-button 
            type="primary" 
            :disabled="!item.available"
            @click="handleAccess(item)"
          >
            访问
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Link } from '@element-plus/icons-vue'

const route = useRoute()

const resourceData = {
  ebook: {
    title: '电子图书',
    items: [
      { name: '知网电子书', description: '收录海量电子书资源，涵盖各学科领域', url: 'https://navi.cnki.net/knavi', available: true },
      { name: '超星电子书', description: '国内最大的电子图书资源库', url: 'https://www.chaoxing.com', available: true },
      { name: '方正电子书', description: '专业电子书数字出版平台', url: 'https://www.founder.com', available: true },
      { name: 'Springer电子图书', description: '国际知名学术电子书出版商', url: 'https://link.springer.com', available: false },
      { name: 'ProQuest电子书', description: '全球学术电子书资源库', url: 'https://www.proquest.com', available: false }
    ]
  },
  journal: {
    title: '电子期刊',
    items: [
      { name: '中国知网CNKI', description: '国内最大的学术期刊数据库', url: 'https://www.cnki.net', available: true },
      { name: '万方数据', description: '综合性学术资源平台', url: 'https://www.wanfangdata.com.cn', available: true },
      { name: '维普期刊', description: '中文科技期刊资源库', url: 'https://www.cqvip.com', available: true },
      { name: 'Elsevier期刊', description: '国际顶尖学术期刊出版商', url: 'https://www.sciencedirect.com', available: false },
      { name: 'Wiley期刊', description: '国际学术期刊出版平台', url: 'https://onlinelibrary.wiley.com', available: false }
    ]
  },
  media: {
    title: '音视频',
    items: [
      { name: '超星学术视频', description: '海量学术讲座视频资源', url: 'https://video.chaoxing.com', available: true },
      { name: '新东方多媒体', description: '外语学习视频课程', url: 'https://www.neworiental.org', available: true },
      { name: '网上报告厅', description: '各类学术报告视频', url: 'https://report.chaoxing.com', available: true },
      { name: '爱课程网', description: '中国大学MOOC平台', url: 'https://www.icourses.cn', available: true },
      { name: '网易公开课', description: '国内外名校公开课程', url: 'https://open.163.com', available: true }
    ]
  },
  database: {
    title: '特色数据库',
    items: [
      { name: '古籍特藏数据库', description: '珍贵古籍文献数字化资源', url: '#', available: true },
      { name: '地方志数据库', description: '全国地方志文献资源', url: '#', available: true },
      { name: '学位论文数据库', description: '硕博学位论文资源库', url: 'https://navi.cnki.net/knavi/detail?degree=DMS&catalogId=ccmld', available: true },
      { name: '会议论文数据库', description: '国内外学术会议论文', url: 'https://navi.cnki.net/knavi/detail?degree=CPCD&catalogId=ccmld', available: true },
      { name: '专利数据库', description: '国内外专利文献资源', url: 'https://patents.google.com', available: true }
    ]
  }
}

const pageTitle = computed(() => {
  const type = route.params.type
  return resourceData[type]?.title || '数字资源'
})

const resourceItems = computed(() => {
  const type = route.params.type
  return resourceData[type]?.items || []
})

const handleAccess = (item) => {
  if (item.url && item.url !== '#') {
    window.open(item.url, '_blank')
  } else {
    ElMessage.info('该资源暂不支持外链访问，请前往图书馆访问')
  }
}
</script>

<style scoped>
.resources-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%);
  padding: 24px;
}

.page-header {
  max-width: 1200px;
  margin: 0 auto 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #0C4A6E;
}

.resources-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resource-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  transition: all 0.3s;
}

.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.15);
}

.resource-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #0EA5E9, #0284C7);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
}

.resource-name {
  font-size: 18px;
  font-weight: 600;
  color: #0C4A6E;
  margin-bottom: 6px;
}

.resource-desc {
  font-size: 14px;
  color: #64748B;
}

.resource-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 768px) {
  .resource-card {
    flex-direction: column;
    text-align: center;
  }
  .resource-action {
    flex-direction: column;
    width: 100%;
  }
  .resource-action .el-button {
    width: 100%;
  }
}
</style>
