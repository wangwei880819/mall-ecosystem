import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMerchantStore = defineStore('merchant', () => {
  const token = ref(localStorage.getItem('merchant_token') || '')
  const merchantInfo = ref(JSON.parse(localStorage.getItem('merchant_info') || 'null'))

  function isLoggedIn() {
    return !!token.value
  }

  function setLogin(data) {
    token.value = data.token
    const info = data.merchant || data.merchantInfo || data
    merchantInfo.value = info
    localStorage.setItem('merchant_token', data.token)
    localStorage.setItem('merchant_info', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    merchantInfo.value = null
    localStorage.removeItem('merchant_token')
    localStorage.removeItem('merchant_info')
  }

  return {
    token,
    merchantInfo,
    isLoggedIn,
    setLogin,
    logout
  }
})
