import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('sso_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 风控看板
export const getDashboard = () => request.get('/risk/dashboard')

// 风控事件
export const getEvents = (params) => request.get('/risk/events', { params })
export const getEventDetail = (id) => request.get(`/risk/events/${id}`)

// 规则管理
export const getRules = (params) => request.get('/risk/rules', { params })
export const updateRule = (id, data) => request.put(`/risk/rules/${id}`, data)
export const toggleRule = (id) => request.put(`/risk/rules/${id}/toggle`)

// 名单库
export const getBlackList = (params) => request.get('/risk/blacklist', { params })
export const addBlackItem = (data) => request.post('/risk/blacklist', data)
export const removeBlackItem = (id) => request.delete(`/risk/blacklist/${id}`)

// 处置管理
export const getDisposals = (params) => request.get('/risk/disposals', { params })
export const executeDisposal = (data) => request.post('/risk/disposals/execute', data)

// 数据分析
export const getAnalysis = (params) => request.get('/risk/analysis', { params })
export const getAuditLogs = (params) => request.get('/risk/audit-logs', { params })

export default request
