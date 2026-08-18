<template>
  <div class="cmall-register">
    <div class="register-container">
      <div class="logo-section">
        <div class="logo">🛒</div>
        <h1>用户注册</h1>
        <p>加入商城，开启购物之旅</p>
      </div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <input v-model="phone" type="tel" placeholder="请输入手机号" class="form-input" />
        </div>

        <div class="form-group">
          <div class="sms-row">
            <input v-model="captchaCode" type="text" placeholder="请输入验证码" class="form-input" />
            <div class="captcha-image" @click="refreshCaptcha">
              <img :src="captchaUrl" alt="验证码" />
            </div>
          </div>
        </div>

        <div class="form-group">
          <input v-model="password" type="password" placeholder="请输入密码（6-20位）" class="form-input" />
        </div>

        <div class="form-group">
          <input v-model="confirmPassword" type="password" placeholder="请确认密码" class="form-input" />
        </div>

        <div class="form-group">
          <input v-model="nickname" type="text" placeholder="请输入昵称" class="form-input" />
        </div>

        <div class="form-group checkbox-group">
          <input type="checkbox" v-model="agreeTerms" id="agree" />
          <label for="agree">我已阅读并同意<a href="#">《用户协议》</a>和<a href="#">《隐私政策》</a></label>
        </div>

        <button type="submit" class="register-button" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>

      <div class="login-link">
        已有账号? <a href="/admin/mall/login">立即登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const phone = ref('')
const captchaCode = ref('')
const password = ref('')
const confirmPassword = ref('')
const nickname = ref('')
const agreeTerms = ref(false)
const loading = ref(false)
const captchaUrl = ref('')

const refreshCaptcha = () => {
  captchaUrl.value = `/api/captcha/image?timestamp=${Date.now()}`
}

onMounted(() => {
  refreshCaptcha()
})

const handleRegister = async () => {
  if (!phone.value) {
    alert('请输入手机号')
    return
  }
  if (!captchaCode.value) {
    alert('请输入验证码')
    return
  }
  if (!password.value) {
    alert('请输入密码')
    return
  }
  if (password.value.length < 6) {
    alert('密码长度至少6位')
    return
  }
  if (password.value !== confirmPassword.value) {
    alert('两次输入的密码不一致')
    return
  }
  if (!agreeTerms.value) {
    alert('请同意用户协议和隐私政策')
    return
  }
  
  loading.value = true
  
  try {
    const result = await fetch('/api/customer/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        phone: phone.value,
        captcha: captchaCode.value,
        password: password.value,
        nickname: nickname.value || '用户' + phone.value.substring(7)
      })
    }).then(res => res.json())
    
    if (result.code === 200) {
      alert('注册成功，请登录')
      router.push('/mall/login')
    } else {
      if (result.message && result.message.includes('验证码')) {
        refreshCaptcha()
      }
      alert(result.message || '注册失败')
    }
  } catch (error) {
    alert('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.cmall-register {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 450px;
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

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.checkbox-group a {
  color: #667eea;
  text-decoration: none;
}

.register-button {
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

.register-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #999;
  font-size: 14px;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
}
</style>