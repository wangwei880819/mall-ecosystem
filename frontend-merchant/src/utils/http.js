import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use(config => {
  const token = localStorage.getItem('merchant_token')
  if (token) {
    config.headers['X-Merchant-Token'] = token
  }
  return config
})

http.interceptors.response.use(
  response => {
    const body = response.data
    if (body && body.code === 200) {
      return body.data
    }
    return Promise.reject(body)
  },
  error => {
    return Promise.reject(error)
  }
)

export default http
