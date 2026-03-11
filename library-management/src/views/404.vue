<template>
  <div class="not-found">
    <h1>404</h1>
    <p>抱歉，您访问的页面不存在</p>
    <el-button type="primary" @click="$router.push('/login')">返回登录页</el-button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}

.not-found h1 {
  font-size: 8rem;
  margin: 0;
  color: #4F46E5;
  line-height: 1;
}

.not-found p {
  font-size: 1.2rem;
  color: #666;
  margin: 1rem 0 2rem;
}
</style>