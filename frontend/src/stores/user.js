import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user') || '{}'))
  const currentPlatform = ref(JSON.parse(localStorage.getItem('currentPlatform') || 'null'))

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('user', JSON.stringify(info))
  }

  const setCurrentPlatform = (platform) => {
    currentPlatform.value = platform
    localStorage.setItem('currentPlatform', JSON.stringify(platform))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    currentPlatform.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('currentPlatform')
  }

  return {
    token,
    userInfo,
    currentPlatform,
    setToken,
    setUserInfo,
    setCurrentPlatform,
    logout
  }
})
