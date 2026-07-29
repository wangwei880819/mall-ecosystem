<template>
  <div class="cmall-profile">
    <div class="profile-header">
      <div class="user-info">
        <img :src="customer.avatar || 'https://via.placeholder.com/80'" :alt="customer.nickname" class="avatar" />
        <div class="info">
          <h2>{{ customer.nickname || '未登录' }}</h2>
          <span class="vip-badge" :class="customer.vipLevel">{{ vipLabel }}</span>
        </div>
      </div>
      <button class="login-btn" v-if="!isLoggedIn" @click="$router.push('/mall/login')">登录</button>
    </div>

    <div class="stats-card">
      <div class="stat-item">
        <span class="stat-value">{{ customer.orderCount || 0 }}</span>
        <span class="stat-label">订单数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">¥{{ customer.totalAmount || '0.00' }}</span>
        <span class="stat-label">消费金额</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ customer.vipLevel === 'VIP' ? 'VIP' : customer.vipLevel === 'SVIP' ? 'SVIP' : '普通' }}</span>
        <span class="stat-label">会员等级</span>
      </div>
    </div>

    <div class="menu-card">
      <div class="menu-item" @click="$router.push('/mall/orders')">
        <span class="menu-icon">📋</span>
        <span class="menu-text">我的订单</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="$router.push('/mall/address')">
        <span class="menu-icon">📍</span>
        <span class="menu-text">收货地址</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item">
        <span class="menu-icon">🎫</span>
        <span class="menu-text">优惠券</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item">
        <span class="menu-icon">❤️</span>
        <span class="menu-text">我的收藏</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item">
        <span class="menu-icon">👣</span>
        <span class="menu-text">浏览记录</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item">
        <span class="menu-icon">⚙️</span>
        <span class="menu-text">账户设置</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <div class="menu-card" v-if="isLoggedIn">
      <div class="menu-item" @click="handleLogout">
        <span class="menu-icon">🚪</span>
        <span class="menu-text">退出登录</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const customer = ref({})

const isLoggedIn = computed(() => localStorage.getItem('customer_token'))

const vipLabel = computed(() => {
  const level = customer.value.vipLevel
  if (level === 'SVIP') return '超级VIP'
  if (level === 'VIP') return 'VIP会员'
  return '普通会员'
})

onMounted(async () => {
  const token = localStorage.getItem('customer_token')
  const customerId = localStorage.getItem('customer_id')
  
  if (token && customerId) {
    try {
      const result = await fetch(`/api/customer/auth/profile?customerId=${customerId}`)
        .then(res => res.json())
      if (result.code === 200) {
        customer.value = result.data
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }
})

const handleLogout = () => {
  localStorage.removeItem('customer_token')
  localStorage.removeItem('customer_id')
  customer.value = {}
  router.push('/mall/login')
}
</script>

<style scoped>
.cmall-profile {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 30px;
}

.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.5);
}

.info h2 {
  color: white;
  margin: 0 0 5px 0;
  font-size: 24px;
}

.vip-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  color: white;
}

.vip-badge.NORMAL {
  background: rgba(255, 255, 255, 0.3);
}

.vip-badge.VIP {
  background: #f1c40f;
  color: #333;
}

.vip-badge.SVIP {
  background: linear-gradient(135deg, #f1c40f 0%, #e67e22 100%);
}

.login-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid white;
  border-radius: 20px;
  color: white;
  cursor: pointer;
}

.stats-card {
  display: flex;
  background: white;
  margin: -20px 15px 15px;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.menu-card {
  background: white;
  margin: 15px;
  border-radius: 12px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 20px;
  margin-right: 15px;
}

.menu-text {
  flex: 1;
  color: #333;
}

.menu-arrow {
  color: #ccc;
  font-size: 20px;
}
</style>