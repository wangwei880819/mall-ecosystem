<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useCartStore } from '@/stores/cartStore'
import { toast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const loginType = ref('password')
const phone = ref('')
const password = ref('')
const captcha = ref('')
const captchaCountdown = ref(0)
const loading = ref(false)

async function handleLogin() {
  if (!phone.value) { toast('请输入手机号'); return }
  if (loginType.value === 'password' && !password.value) { toast('请输入密码'); return }
  if (loginType.value === 'captcha' && !captcha.value) { toast('请输入验证码'); return }

  loading.value = true
  try {
    if (loginType.value === 'password') {
      await userStore.getUserInfo({ account: phone.value, password: password.value })
    } else {
      await userStore.getUserInfo({ account: phone.value, password: captcha.value })
    }
    toast('登录成功')
    const redirect = route.query.redirect
    setTimeout(() => router.push(redirect || '/'), 500)
  } catch (error) {
    toast(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function getCaptcha() {
  if (captchaCountdown.value > 0) return
  if (!phone.value) { toast('请先输入手机号'); return }
  captchaCountdown.value = 60
  const timer = setInterval(() => {
    captchaCountdown.value--
    if (captchaCountdown.value <= 0) clearInterval(timer)
  }, 1000)
  toast('验证码已发送')
}
</script>

<template>
  <div class="login-page">
    <!-- Background Area -->
    <div class="login-bg">
      <!-- Logo Area -->
      <div class="logo-area">
        <div class="logo-icon">🛒</div>
        <h1 class="logo-title">商城</h1>
        <p class="logo-subtitle">品质生活，尽在商城</p>
      </div>

      <!-- Login Card -->
      <div class="login-card">
        <!-- Tabs -->
        <div class="login-tabs">
          <span
            class="tab-item"
            :class="{ active: loginType === 'password' }"
            @click="loginType = 'password'"
          >密码登录</span>
          <span
            class="tab-item"
            :class="{ active: loginType === 'captcha' }"
            @click="loginType = 'captcha'"
          >验证码登录</span>
        </div>

        <!-- Form -->
        <div class="login-form">
          <!-- Phone Input -->
          <div class="input-wrapper">
            <span class="input-icon">📱</span>
            <input
              v-model="phone"
              type="tel"
              placeholder="请输入手机号"
              maxlength="11"
              class="form-input"
            />
          </div>

          <!-- Password Input -->
          <div v-if="loginType === 'password'" class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              class="form-input"
            />
          </div>

          <!-- Captcha Input -->
          <div v-if="loginType === 'captcha'" class="input-wrapper captcha-row">
            <div class="captcha-input-group">
              <span class="input-icon">📧</span>
              <input
                v-model="captcha"
                type="text"
                placeholder="请输入验证码"
                class="form-input"
              />
            </div>
            <button
              class="captcha-btn"
              :class="{ counting: captchaCountdown > 0 }"
              :disabled="captchaCountdown > 0"
              @click="getCaptcha"
            >
              {{ captchaCountdown > 0 ? `${captchaCountdown}s后重发` : '获取验证码' }}
            </button>
          </div>

          <!-- Login Button -->
          <button
            class="login-btn btn-primary"
            :class="{ disabled: loading }"
            :disabled="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登  录' }}
          </button>

          <!-- Register Link -->
          <div class="register-link">
            还没有账号？<span class="link-text" @click="router.push('/register')">立即注册 →</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
}

.login-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 80px;
}

/* Logo Area */
.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.logo-title {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6px;
}

.logo-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
  letter-spacing: 1px;
}

/* Login Card */
.login-card {
  width: calc(100% - 40px);
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 30px 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

/* Tabs */
.login-tabs {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 28px;
}

.tab-item {
  font-size: 16px;
  color: #999;
  padding-bottom: 8px;
  position: relative;
  cursor: pointer;
  transition: color 0.2s;
  font-weight: 500;
}

.tab-item.active {
  color: #667eea;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 2px;
}

/* Form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #f8f9fb;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  padding: 0 14px;
  transition: border-color 0.2s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  background: #fff;
}

.input-icon {
  font-size: 16px;
  margin-right: 10px;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  padding: 14px 0;
  font-size: 15px;
  border: none;
  background: transparent;
  color: #333;
}

.form-input::placeholder {
  color: #ccc;
}

/* Captcha Row */
.captcha-row {
  padding-right: 0;
  border: none;
  background: transparent;
  gap: 0;
}

.captcha-row:focus-within {
  background: transparent;
}

.captcha-input-group {
  display: flex;
  align-items: center;
  flex: 1;
  background: #f8f9fb;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  padding: 0 14px;
  transition: border-color 0.2s;
}

.captcha-input-group:focus-within {
  border-color: #667eea;
  background: #fff;
}

.captcha-btn {
  flex-shrink: 0;
  margin-left: 10px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #667eea;
  border-radius: 10px;
  color: #667eea;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
}

.captcha-btn:active {
  background: #667eea;
  color: #fff;
}

.captcha-btn.counting {
  color: #ccc;
  border-color: #e8e8e8;
  cursor: not-allowed;
}

/* Login Button */
.login-btn {
  margin-top: 8px;
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.login-btn:active {
  opacity: 0.85;
}

.login-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* Register Link */
.register-link {
  text-align: center;
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.link-text {
  color: #667eea;
  cursor: pointer;
  font-weight: 500;
}

.link-text:active {
  opacity: 0.7;
}
</style>
