import axios from 'axios'
import { useUserStore } from '@/stores/userStore'
import router from '@/router'

const httpInstance = axios.create({
  baseURL: '/api/c-mall',
  timeout: 50000
})

// 请求拦截器 - 附加JWT Token
httpInstance.interceptors.request.use(config => {
  const userStore = useUserStore()
  const token = userStore.userInfo?.token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, e => Promise.reject(e))

// 响应拦截器 - 统一处理 data → result 转换
httpInstance.interceptors.response.use(res => {
  const data = res.data
  if (data && data.data !== undefined) {
    data.result = data.data
    delete data.data
  }
  return data
}, e => {
  // 401 Token失效处理
  if (e.response?.status === 401) {
    const userStore = useUserStore()
    userStore.clearUserInfo()
    router.push('/login')
  }
  return Promise.reject(e)
})

export default httpInstance
