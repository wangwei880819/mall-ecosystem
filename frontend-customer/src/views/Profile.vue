<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useCartStore } from '@/stores/cartStore'
import { toast } from '@/utils/toast'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isLogin = computed(() => {
  return userStore.userInfo && userStore.userInfo.token
})

const maskedPhone = computed(() => {
  const phone = userStore.userInfo?.phone || ''
  if (phone.length >= 11) {
    return phone.slice(0, 3) + '****' + phone.slice(7)
  }
  return phone
})

const nickname = computed(() => {
  return userStore.userInfo?.nickname || userStore.userInfo?.account || '用户'
})

const vipLevel = computed(() => {
  return userStore.userInfo?.vipLevel || 'NORMAL'
})

const avatar = computed(() => {
  return userStore.userInfo?.avatar || ''
})

const orderCount = computed(() => {
  return userStore.userInfo?.orderCount || userStore.userInfo?.totalOrders || 0
})

const totalSpend = computed(() => {
  const val = userStore.userInfo?.totalSpend || userStore.userInfo?.totalAmount || 0
  return Number(val).toFixed(2)
})

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.clearUserInfo()
    cartStore.clearCart()
    router.push('/login')
  }
}

const handleMenuClick = (type) => {
  switch (type) {
    case 'orders':
      router.push('/orders')
      break
    case 'address':
      router.push('/address')
      break
    case 'favorites':
      toast('功能开发中')
      break
    case 'settings':
      toast('功能开发中')
      break
  }
}
</script>

<template>
  <div class="page-mobile profile-page">
    <!-- Header -->
    <div class="page-header">
      <span></span>
      <span>我的</span>
      <span class="header-right"></span>
    </div>

    <!-- Not Logged In -->
    <div v-if="!isLogin" class="not-login">
      <div class="login-card card">
        <div class="avatar-placeholder">
          <span>👤</span>
        </div>
        <p class="login-hint">登录后可查看个人信息</p>
        <button class="btn-primary login-btn" @click="router.push('/login')">登录/注册</button>
      </div>
    </div>

    <!-- Logged In -->
    <div v-else class="logged-in fade-in-up">
      <!-- User Info Card -->
      <div class="user-card">
        <div class="user-card-inner">
          <div class="avatar-circle">
            <img v-if="avatar" :src="avatar" alt="" />
            <span v-else>👤</span>
          </div>
          <div class="user-details">
            <div class="user-name-row">
              <span class="nickname">{{ nickname }}</span>
              <span class="vip-badge">{{ vipLevel }}</span>
            </div>
            <p class="user-phone">{{ maskedPhone }}</p>
          </div>
        </div>
      </div>

      <!-- Stats Row -->
      <div class="stats-row card">
        <div class="stat-item">
          <span class="stat-value">{{ orderCount }}</span>
          <span class="stat-label">全部订单</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">¥{{ totalSpend }}</span>
          <span class="stat-label">消费金额</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ vipLevel }}</span>
          <span class="stat-label">会员等级</span>
        </div>
      </div>

      <!-- Menu List -->
      <div class="menu-list card">
        <div class="menu-item" @click="handleMenuClick('orders')">
          <div class="menu-left">
            <span class="menu-icon">📋</span>
            <span class="menu-label">我的订单</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>
        <div class="menu-item" @click="handleMenuClick('address')">
          <div class="menu-left">
            <span class="menu-icon">📍</span>
            <span class="menu-label">收货地址</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>
        <div class="menu-item" @click="handleMenuClick('favorites')">
          <div class="menu-left">
            <span class="menu-icon">❤️</span>
            <span class="menu-label">我的收藏</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>
        <div class="menu-item" @click="handleMenuClick('settings')">
          <div class="menu-left">
            <span class="menu-icon">⚙️</span>
            <span class="menu-label">账号设置</span>
          </div>
          <span class="menu-arrow">›</span>
        </div>
        <div class="menu-item menu-item-logout" @click="handleLogout">
          <div class="menu-left">
            <span class="menu-icon">🚪</span>
            <span class="menu-label">退出登录</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding-bottom: 60px;
  background: #f5f6fa;
}

/* Not Logged In */
.not-login {
  padding-top: 40px;
}

.login-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 24px 30px;
}

.avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #f0edf6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin-bottom: 16px;
}

.login-hint {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

.login-btn {
  width: 200px;
}

/* User Info Card */
.user-card {
  margin: 0 16px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.user-card-inner {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 28px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.nickname {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.vip-badge {
  padding: 2px 10px;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 11px;
  border-radius: 10px;
  font-weight: 500;
}

.user-phone {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

/* Stats Row */
.stats-row {
  display: flex;
  align-items: center;
  padding: 18px 0;
  margin-top: 16px;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: #f0f0f0;
}

/* Menu List */
.menu-list {
  margin-top: 16px;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f8f8f8;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #f8f9fb;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-icon {
  font-size: 20px;
  width: 28px;
  text-align: center;
}

.menu-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.menu-arrow {
  font-size: 22px;
  color: #ccc;
  font-weight: 300;
}

.menu-item-logout {
  margin-top: 8px;
  border-top: 8px solid #f5f6fa;
  border-bottom: none;
}

.menu-item-logout .menu-label {
  color: #999;
}
</style>
