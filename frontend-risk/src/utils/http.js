import axios from 'axios'

const http = axios.create({
  baseURL: '/api/risk',
  timeout: 15000
})

http.interceptors.request.use(config => {
  const token = localStorage.getItem('sso_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  response => {
    const body = response.data
    if (body && body.code === 200) {
      return body.data
    }
    return body
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default http
