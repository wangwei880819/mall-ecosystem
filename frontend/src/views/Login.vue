<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand-section">
        <div class="brand-logo">🛒</div>
        <h1 class="brand-title">商城生态运营系统</h1>
        <p class="brand-desc">统一身份认证平台</p>
      </div>
      <div class="features">
        <div class="feature-item">
          <div class="feature-icon">🔒</div>
          <div class="feature-content">
            <h4>安全认证</h4>
            <p>多重加密保护，确保账户安全</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">⚡</div>
          <div class="feature-content">
            <h4>快速登录</h4>
            <p>一键登录，无缝接入各平台</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🔗</div>
          <div class="feature-content">
            <h4>统一认证</h4>
            <p>SSO单点登录，一次认证全系统通行</p>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h2>系统登录</h2>
          <p>请输入您的账号信息</p>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" style="width:100%" @click="handleLogin" :loading="loading">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loginForm = ref({
  username: '',
  password: ''
})
const loading = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
  } catch (e) {
    return
  }

  loading.value = true
  try {
    const res = await request.post('/auth/login', loginForm.value)
    if (res.code === 200 && res.data) {
      userStore.setToken(res.data.token)
      await loadUserPlatforms(res.data.user)
      
      ElMessage.success('登录成功')
      router.push('/platform-select')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error('登录失败：' + (e.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

const loadUserPlatforms = async (userData) => {
  let platforms = []
  try {
    const res = await request.get('/auth/rbac/user/platforms', {
      params: { userId: userData?.id || 1 }
    })
    if (res.code === 200) {
      platforms = res.data || []
      if (typeof platforms === 'string') {
        try {
          platforms = JSON.parse(platforms)
        } catch {
          platforms = []
        }
      }
    }
  } catch (e) {
    console.error('Failed to load user platforms:', e)
    platforms = []
  }
  
  const userInfo = {
    id: userData?.id || 1,
    username: userData?.username || loginForm.value.username,
    realName: userData?.realName || '系统管理员',
    role: userData?.role || 'ADMIN',
    platforms: Array.isArray(platforms) && platforms.length > 0 ? platforms : [1]
  }
  userStore.setUserInfo(userInfo)
}

onMounted(async () => {
  // 检查是否已经登录，直接跳转到平台选择页
  if (userStore.token) {
    router.push('/platform-select')
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  background: #f5f7fa;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 80px;
  color: white;
}

.brand-section {
  margin-bottom: 60px;
}

.brand-logo {
  font-size: 64px;
  margin-bottom: 20px;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.features {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.feature-icon {
  font-size: 28px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
}

.feature-content h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.feature-content p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  background: #fff;
  border-radius: 12px;
  padding: 48px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-header h2 {
  font-size: 22px;
  color: #333;
  font-weight: 700;
  margin-bottom: 6px;
}

.login-header p {
  font-size: 14px;
  color: #999;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-password {
  font-size: 13px;
  color: #1a237e;
  text-decoration: none;
}
</style>
