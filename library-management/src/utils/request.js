import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken, isTokenExpired } from '@/utils/auth'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: Number(import.meta.env.VITE_API_TIMEOUT) || 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token && !isTokenExpired(token)) {
      config.headers['Authorization'] = `Bearer ${token}`
    } else if (token) {
      clearAuth()
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        clearAuth()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    console.error('响应错误:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      clearAuth()
      router.push('/login')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
