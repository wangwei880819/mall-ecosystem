<template>
  <div class="platform-container">
    <div class="platform-header">
      <div class="user-info">
        <div class="avatar">👤</div>
        <div class="info">
          <div class="name">{{ userStore.userInfo?.realName || '管理员' }}</div>
          <div class="desc">请选择要登录的平台</div>
        </div>
      </div>
      <el-button link @click="handleLogout">退出登录</el-button>
    </div>

    <div class="platform-grid" v-loading="loading">
      <div
        v-for="platform in userPlatforms"
        :key="platform.id"
        class="platform-card"
        :class="{ active: currentPlatform?.id === platform.id }"
        @click="selectPlatform(platform)"
      >
        <div class="platform-icon">{{ platform.icon || '🔗' }}</div>
        <div class="platform-name">{{ platform.name }}</div>
        <div class="platform-desc">{{ platform.desc || '已接入平台' }}</div>
        <div v-if="currentPlatform?.id === platform.id" class="platform-selected">
          <el-icon><Check /></el-icon>
          当前登录
        </div>
      </div>
    </div>

    <div class="platform-footer">
      <el-button @click="handleBack">返回</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!currentPlatform" :loading="confirmLoading">
        进入平台
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import request from '../../utils/request'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const confirmLoading = ref(false)
const currentPlatform = ref(null)
const allPlatforms = ref([])

// 计算该用户有权限登录的平台
const userPlatforms = computed(() => {
  let userPlatformIds = userStore.userInfo?.platforms || []
  if (typeof userPlatformIds === 'string') {
    try {
      userPlatformIds = JSON.parse(userPlatformIds)
    } catch {
      userPlatformIds = []
    }
  }
  if (!Array.isArray(userPlatformIds)) {
    userPlatformIds = []
  }
  const ecPlatform = allPlatforms.value.find(p => p.id === 1) || { 
    id: 1, 
    name: '生态合作平台', 
    icon: '🏢', 
    desc: '商城生态运营系统', 
    status: 'ACTIVE'
  }
  const filtered = allPlatforms.value.filter(p => 
    userPlatformIds.includes(p.id) && p.status === 'ACTIVE'
  )
  if (!filtered.find(p => p.id === 1)) {
    return [ecPlatform, ...filtered]
  }
  return filtered
})

// 获取所有接入平台
const fetchAllPlatforms = async () => {
  loading.value = true
  try {
    const res = await request.get('/auth/sso/platforms')
    if (res.code === 200) {
      allPlatforms.value = res.data || []
    }
  } catch (e) {
    console.error('获取平台列表失败:', e)
    allPlatforms.value = []
  } finally {
    loading.value = false
  }
}

const selectPlatform = (platform) => {
  currentPlatform.value = platform
}

const handleConfirm = async () => {
  if (!currentPlatform.value) {
    return
  }

  confirmLoading.value = true
  try {
    userStore.setCurrentPlatform(currentPlatform.value)
    // 外部平台（如风控稽核管理平台）通过URL+Token进行SSO单点登录
    if (currentPlatform.value.url && currentPlatform.value.id !== 1) {
      const token = userStore.token
      const realName = userStore.userInfo?.realName || '系统管理员'
      const redirectUrl = `${currentPlatform.value.url}/?sso_token=${encodeURIComponent(token)}&realName=${encodeURIComponent(realName)}`
      ElMessage.success(`正在跳转到 ${currentPlatform.value.name}`)
      setTimeout(() => {
        window.open(redirectUrl, '_blank')
      }, 500)
    } else {
      ElMessage.success('登录成功')
      setTimeout(() => {
        router.push('/portal')
      }, 500)
    }
  } catch (e) {
    ElMessage.error('登录失败，请重试')
  } finally {
    confirmLoading.value = false
  }
}

const handleBack = async () => {
  try {
    await ElMessageBox.confirm('确定要返回登录页面吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    router.push('/login')
  } catch (e) {
    // 用户取消
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  } catch (e) {
    // 用户取消
  }
}

onMounted(async () => {
  await fetchAllPlatforms()
  await refreshUserPlatforms()
  const savedPlatform = localStorage.getItem('currentPlatform')
  if (savedPlatform) {
    const parsed = JSON.parse(savedPlatform)
    const platform = allPlatforms.value.find(p => p.id === parsed.id)
    if (platform && userStore.userInfo?.platforms?.includes(platform.id)) {
      currentPlatform.value = platform
    }
  }
})

const refreshUserPlatforms = async () => {
  const userId = userStore.userInfo?.id
  try {
    const res = await request.get('/auth/rbac/user/platforms', {
      params: { userId: userId || 1 }
    })
    if (res.code === 200 && res.data) {
      let platforms = res.data
      if (typeof platforms === 'string') {
        try {
          platforms = JSON.parse(platforms)
        } catch {
          platforms = []
        }
      }
      const updatedUserInfo = {
        ...userStore.userInfo,
        platforms: Array.isArray(platforms) && platforms.length > 0 ? platforms : [1]
      }
      userStore.setUserInfo(updatedUserInfo)
    }
  } catch (e) {
    console.error('Failed to refresh user platforms:', e)
  }
}
</script>

<style scoped>
.platform-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 40px;
}

.platform-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1000px;
  margin: 0 auto 40px;
  padding: 24px;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.info .name {
  font-size: 20px;
  font-weight: 600;
}

.info .desc {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 4px;
}

.platform-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.platform-card {
  background: #fff;
  border-radius: 14px;
  padding: 32px 24px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
  position: relative;
  border: 3px solid transparent;
}

.platform-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  border-color: rgba(102, 126, 234, 0.4);
}

.platform-card.active {
  border: 3px solid #67c23a;
  box-shadow: 0 8px 30px rgba(103, 194, 58, 0.3);
}

.platform-icon {
  font-size: 56px;
  margin-bottom: 12px;
  display: block;
}

.platform-name {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.platform-desc {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
}

.platform-selected {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #67c23a;
  color: #fff;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
}

.platform-footer {
  max-width: 1000px;
  margin: 48px auto 0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
