<template>
  <div class="cmall-login">
    <div class="login-container">
      <div class="logo-section">
        <div class="logo">🛒</div>
        <h1>商城</h1>
        <p>一站式购物体验</p>
      </div>
      
      <div class="tabs">
        <button :class="{ active: loginType === 'password' }" @click="loginType = 'password'">密码登录</button>
        <button :class="{ active: loginType === 'code' }" @click="loginType = 'code'">验证码登录</button>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <input v-model="phone" type="tel" placeholder="请输入手机号" class="form-input" />
        </div>
        
        <div class="form-group" v-if="loginType === 'password'">
          <input v-model="password" type="password" placeholder="请输入密码" class="form-input" />
        </div>
        
        <div class="form-group" v-else>
          <div class="sms-row">
            <input v-model="captchaCode" type="text" placeholder="请输入验证码" class="form-input" />
            <div class="captcha-image" @click="refreshCaptcha">
              <img :src="captchaUrl" alt="验证码" />
            </div>
          </div>
        </div>

        <div class="form-group" v-if="loginType === 'password'">
          <a href="#" class="forgot-link">忘记密码?</a>
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="other-login">
        <span>其他登录方式</span>
        <div class="social-buttons">
          <button class="social-btn wechat" @click="handleWechatLogin">微信登录（功能开发中）</button>
        </div>
      </div>

      <div class="register-link">
        还没有账号? <a href="/admin/mall/register">立即注册</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginType = ref('password')
const phone = ref('')
const password = ref('')
const captchaCode = ref('')
const loading = ref(false)
const captchaUrl = ref('')

const refreshCaptcha = () => {
  captchaUrl.value = `/api/captcha/image?timestamp=${Date.now()}`
}

onMounted(() => {
  refreshCaptcha()
})

const handleLogin = async () => {
  if (!phone.value) {
    alert('请输入手机号')
    return
  }
  
  loading.value = true
  
  try {
    const url = loginType.value === 'password' ? '/api/customer/auth/login' : '/api/customer/auth/login/code'
    const body = loginType.value === 'password' 
      ? { phone: phone.value, password: password.value }
      : { phone: phone.value, captcha: captchaCode.value }
    
    const result = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    }).then(res => res.json())
    
    if (result.code === 200) {
      localStorage.setItem('customer_token', result.data.token)
      localStorage.setItem('customer_id', result.data.customer.id)
      router.push('/mall')
    } else {
      if (result.message && result.message.includes('验证码')) {
        refreshCaptcha()
      }
      alert(result.message || '登录失败')
    }
  } catch (error) {
    alert('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleWechatLogin = () => {
  alert('微信登录功能开发中')
}
</script>

<style scoped>
.cmall-login {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 64px;
  margin-bottom: 10px;
}

.logo-section h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 5px 0;
}

.logo-section p {
  color: #999;
  margin: 0;
}

.tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.tabs button {
  flex: 1;
  padding: 12px;
  border: none;
  background: none;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  position: relative;
}

.tabs button.active {
  color: #667eea;
  font-weight: bold;
}

.tabs button.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 20%;
  right: 20%;
  height: 2px;
  background: #667eea;
}

.form-group {
  margin-bottom: 16px;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
}

.sms-row {
  display: flex;
  gap: 10px;
}

.sms-row .form-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 44px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.forgot-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
}

.login-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 10px;
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.other-login {
  text-align: center;
  margin-top: 20px;
}

.other-login span {
  color: #999;
  font-size: 14px;
}

.social-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 15px;
}

.social-btn {
  padding: 12px 30px;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  cursor: pointer;
}

.social-btn.wechat {
  background: #07c160;
  color: white;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #999;
  font-size: 14px;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
}
</style>