<template>
  <div class="page-container">
    <div class="bg-blob">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-header glass">
      <div class="header-left">
        <h2 class="page-title">系统设置</h2>
        <p class="page-desc">配置系统参数与选项</p>
      </div>
    </div>

    <div class="settings-content">
      <div class="settings-card glass">
        <div class="settings-section">
          <div class="section-header">
            <el-icon class="section-icon"><Ticket /></el-icon>
            <h3 class="section-title">借阅设置</h3>
          </div>
          
          <el-form label-width="140px" class="settings-form">
            <el-form-item label="最大借阅数量">
              <div class="setting-input">
                <el-input-number v-model="settings.maxBorrowCount" :min="1" :max="100" />
                <span class="setting-tip">读者最多可同时借阅的图书数量</span>
              </div>
            </el-form-item>
            
            <el-form-item label="借阅天数">
              <div class="setting-input">
                <el-input-number v-model="settings.borrowDays" :min="1" :max="365" />
                <span class="setting-tip">默认借阅天数</span>
              </div>
            </el-form-item>
            
            <el-form-item label="可续借次数">
              <div class="setting-input">
                <el-input-number v-model="settings.renewTimes" :min="0" :max="10" />
                <span class="setting-tip">读者可续借的次数</span>
              </div>
            </el-form-item>
            
            <el-form-item label="逾期罚款(元/天)">
              <div class="setting-input">
                <el-input-number v-model="settings.overdueFine" :min="0" :max="10" :precision="2" />
                <span class="setting-tip">逾期每天的罚款金额</span>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <el-divider />

        <div class="settings-section">
          <div class="section-header">
            <el-icon class="section-icon"><Calendar /></el-icon>
            <h3 class="section-title">预约设置</h3>
          </div>
          
          <el-form label-width="140px" class="settings-form">
            <el-form-item label="预约保留天数">
              <div class="setting-input">
                <el-input-number v-model="settings.reserveDays" :min="1" :max="30" />
                <span class="setting-tip">预约后保留图书的天数</span>
              </div>
            </el-form-item>
            
            <el-form-item label="取消预约次数限制">
              <div class="setting-input">
                <el-input-number v-model="settings.cancelReserveLimit" :min="0" :max="20" />
                <span class="setting-tip">读者每月最多取消预约的次数</span>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <el-divider />

        <div class="settings-section">
          <div class="section-header">
            <el-icon class="section-icon"><Setting /></el-icon>
            <h3 class="section-title">系统设置</h3>
          </div>
          
          <el-form label-width="140px" class="settings-form">
            <el-form-item label="系统名称">
              <el-input v-model="settings.systemName" placeholder="图书馆管理系统" />
            </el-form-item>
            
            <el-form-item label="管理员邮箱">
              <el-input v-model="settings.adminEmail" placeholder="admin@example.com" />
            </el-form-item>
            
            <el-form-item label="图书封面存储">
              <el-input v-model="settings.coverPath" disabled />
            </el-form-item>
            
            <el-form-item label="头像存储">
              <el-input v-model="settings.avatarPath" disabled />
            </el-form-item>
          </el-form>
        </div>

        <div class="form-actions">
          <el-button type="primary" class="save-btn" @click="handleSave">保存设置</el-button>
          <el-button class="reset-btn" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Ticket, Calendar, Setting } from '@element-plus/icons-vue'

const settings = reactive({
  maxBorrowCount: 5,
  borrowDays: 30,
  renewTimes: 2,
  overdueFine: 0.5,
  reserveDays: 3,
  cancelReserveLimit: 5,
  systemName: '图书馆管理系统',
  adminEmail: 'admin@library.com',
  coverPath: 'C:/Users/Teonya/OneDrive/Desktop/58/images/bookimages',
  avatarPath: 'C:/Users/Teonya/OneDrive/Desktop/58/images/userimages'
})

const defaultSettings = { ...settings }

const handleSave = () => {
  localStorage.setItem('systemSettings', JSON.stringify(settings))
  ElMessage.success('设置已保存')
}

const handleReset = () => {
  Object.assign(settings, defaultSettings)
  ElMessage.info('已重置为默认值')
}
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

.settings-content {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.settings-card {
  padding: 40px;
  border-radius: 24px;
}

.settings-section {
  padding: 0 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.section-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  color: #2563EB;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1E3A8A;
}

.settings-form :deep(.el-form-item__label) {
  color: #1E3A8A;
  font-weight: 600;
  font-size: 14px;
}

.settings-form :deep(.el-input__wrapper),
.settings-form :deep(.el-input-number .el-input__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.settings-form :deep(.el-input__wrapper:hover),
.settings-form :deep(.el-input__wrapper.is-focus),
.settings-form :deep(.el-input-number .el-input__wrapper:hover) {
  border-color: #3B82F6 !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.setting-input {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.setting-tip {
  font-size: 12px;
  color: #94A3B8;
}

.settings-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: #F1F5F9 !important;
  border-color: #E2E8F0 !important;
}

:deep(.el-divider) {
  border-color: rgba(59, 130, 246, 0.1);
  margin: 32px 0;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
  padding-top: 20px;
}

.save-btn {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border: none;
  padding: 12px 40px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
}

.reset-btn {
  padding: 12px 32px;
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: #3B82F6;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: rgba(59, 130, 246, 0.05);
  border-color: #3B82F6;
}
</style>
