import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('campus-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.success === false) {
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload?.data ?? payload
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  },
)

export default http
