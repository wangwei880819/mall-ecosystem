<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'

const router = useRouter()

const phone = ref('')
const captcha = ref('')
const password = ref('')
const confirmPassword = ref('')
const nickname = ref('')
const captchaCountdown = ref(0)
const agreementChecked = ref(false)
const loading = ref(false)

// Validation errors
const errors = ref({
  phone: '',
  captcha: '',
  password: '',
  confirmPassword: '',
  agreement: ''
})

const defaultNickname = computed(() => {
  if (phone.value.length >= 11) {
    return '用户' + phone.value.slice(-4)
  }
  return '用户'
})

function validate() {
  errors.value = { phone: '', captcha: '', password: '', confirmPassword: '', agreement: '' }
  let valid = true

  if (!phone.value) {
    errors.value.phone = '请输入手机号'
    valid = false
  } else if (!/^1\d{10}$/.test(phone.value)) {
    errors.value.phone = '手机号格式不正确'
    valid = false
  }

  if (!captcha.value) {
    errors.value.captcha = '请输入验证码'
    valid = false
  }

  if (!password.value) {
    errors.value.password = '请输入密码'
    valid = false
  } else if (password.value.length < 6) {
    errors.value.password = '密码长度不能少于6位'
    valid = false
  }

  if (!confirmPassword.value) {
    errors.value.confirmPassword = '请确认密码'
    valid = false
  } else if (confirmPassword.value !== password.value) {
    errors.value.confirmPassword = '两次密码输入不一致'
    valid = false
  }

  if (!agreementChecked.value) {
    errors.value.agreement = '请阅读并同意用户协议和隐私政策'
    valid = false
  }

  return valid
}

const canSubmit = computed(() => {
  return phone.value && password.value && confirmPassword.value && captcha.value && agreementChecked.value
})

async function handleRegister() {
  if (!validate()) return
  if (!canSubmit.value) return

  loading.value = true
  try {
    const body = {
      phone: phone.value,
      password: password.value,
      nickname: nickname.value || defaultNickname.value
    }
    await httpInstance.post('/auth/register', body)
    toast('注册成功')
    setTimeout(() => router.push('/login'), 800)
  } catch (error) {
    toast(error.message || '注册失败')
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
  <div class="register-page">
    <div class="register-bg">
      <!-- Register Card -->
      <div class="register-card">
        <!-- Back Button -->
        <div class="back-row">
          <span class="back-btn" @click="router.back()">← 返回</span>
        </div>

        <h2 class="register-title">注册</h2>

        <!-- Form -->
        <div class="register-form">
          <!-- Phone -->
          <div class="field-group">
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
            <p v-if="errors.phone" class="field-error">{{ errors.phone }}</p>
          </div>

          <!-- Captcha -->
          <div class="field-group">
            <div class="captcha-row">
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
            <p v-if="errors.captcha" class="field-error">{{ errors.captcha }}</p>
          </div>

          <!-- Password -->
          <div class="field-group">
            <div class="input-wrapper">
              <span class="input-icon">🔒</span>
              <input
                v-model="password"
                type="password"
                placeholder="请输入密码（至少6位）"
                class="form-input"
              />
            </div>
            <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
          </div>

          <!-- Confirm Password -->
          <div class="field-group">
            <div class="input-wrapper">
              <span class="input-icon">🔒</span>
              <input
                v-model="confirmPassword"
                type="password"
                placeholder="请确认密码"
                class="form-input"
              />
            </div>
            <p v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</p>
          </div>

          <!-- Nickname -->
          <div class="field-group">
            <div class="input-wrapper">
              <span class="input-icon">👤</span>
              <input
                v-model="nickname"
                type="text"
                :placeholder="defaultNickname"
                class="form-input"
              />
            </div>
          </div>

          <!-- Agreement -->
          <div class="agreement-row" @click="agreementChecked = !agreementChecked">
            <span class="checkbox-icon" :class="{ checked: agreementChecked }">
              {{ agreementChecked ? '✅' : '⬜' }}
            </span>
            <span class="agreement-text">
              我已阅读并同意<span class="link-text">《用户协议》</span>和<span class="link-text">《隐私政策》</span>
            </span>
          </div>
          <p v-if="errors.agreement" class="field-error">{{ errors.agreement }}</p>

          <!-- Register Button -->
          <button
            class="register-btn btn-primary"
            :class="{ disabled: !canSubmit || loading }"
            :disabled="!canSubmit || loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注  册' }}
          </button>

          <!-- Login Link -->
          <div class="login-link">
            已有账号？<span class="link-text" @click="router.push('/login')">立即登录 →</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
}

.register-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
}

/* Register Card */
.register-card {
  width: calc(100% - 40px);
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 30px 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.back-row {
  margin-bottom: 8px;
}

.back-btn {
  font-size: 14px;
  color: #999;
  cursor: pointer;
}

.back-btn:active {
  color: #667eea;
}

.register-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin-bottom: 24px;
}

/* Form */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-group {
  display: flex;
  flex-direction: column;
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

.field-error {
  font-size: 12px;
  color: #e74c3c;
  margin-top: 4px;
  padding-left: 4px;
  line-height: 1.4;
}

/* Captcha Row */
.captcha-row {
  display: flex;
  align-items: center;
  gap: 0;
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

/* Agreement */
.agreement-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  cursor: pointer;
  padding: 2px 0;
}

.checkbox-icon {
  font-size: 18px;
  line-height: 1;
  flex-shrink: 0;
  margin-top: 1px;
}

.agreement-text {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}

.agreement-text .link-text {
  color: #667eea;
}

/* Register Button */
.register-btn {
  margin-top: 6px;
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

.register-btn:active {
  opacity: 0.85;
}

.register-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* Login Link */
.login-link {
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
