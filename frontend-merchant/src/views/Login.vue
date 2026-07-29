<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMerchantStore } from '@/stores/merchant'
import http from '@/utils/http'
import { ElMessage } from 'element-plus'
import { Phone, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const merchantStore = useMerchantStore()

const contactPhone = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!contactPhone.value) { ElMessage.warning('请输入手机号'); return }
  if (!password.value) { ElMessage.warning('请输入密码'); return }

  loading.value = true
  try {
    const res = await http.post('/merchant-portal/login', {
      contactPhone: contactPhone.value,
      password: password.value
    })
    merchantStore.setLogin(res)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    const msg = error?.response?.data?.message || error?.message || ''
    if (msg.includes('未注册') || msg.includes('不存在')) {
      ElMessage.error('该账号未注册，请先申请入驻')
    } else if (msg.includes('审核')) {
      ElMessage.error('您的入驻申请正在审核中，请耐心等待')
    } else {
      ElMessage.error(msg || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

function goRegister() {
  router.push('/register')
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="login-card">
        <div class="login-header">
          <h1>商户入驻平台</h1>
          <p>欢迎回来，请登录您的商户账号</p>
        </div>

        <div class="login-form">
          <div class="form-item">
            <el-input
              v-model="contactPhone"
              placeholder="请输入联系人手机号"
              size="large"
              :prefix-icon="Phone"
              clearable
            />
          </div>
          <div class="form-item">
            <el-input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </div>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn-submit"
            @click="handleLogin"
          >
            登 录
          </el-button>

          <div class="login-footer">
            <span class="register-link" @click="goRegister">申请入驻</span>
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
  background: linear-gradient(135deg, #6c5ce7, #a29bfe);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(108, 92, 231, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #999;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  width: 100%;
}

.login-btn-submit {
  width: 100%;
  height: 46px;
  font-size: 16px;
  background: linear-gradient(135deg, #6c5ce7, #a29bfe);
  border: none;
  border-radius: 8px;
}

.login-btn-submit:hover {
  opacity: 0.9;
}

.login-footer {
  text-align: center;
  margin-top: 4px;
}

.register-link {
  color: #6c5ce7;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
