<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useMerchantStore } from '@/stores/merchant'
import { ElMessageBox, ElMessage } from 'element-plus'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const merchantStore = useMerchantStore()

const menuItems = [
  { path: '/dashboard', title: '仪表盘', icon: 'DataAnalysis' },
  { path: '/products', title: '我的商品', icon: 'Goods' },
  { path: '/orders', title: '我的订单', icon: 'Document' }
]

const currentPath = computed(() => route.path)

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    .then(() => {
      merchantStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
    .catch(() => {})
}
</script>

<template>
  <div class="merchant-layout">
    <aside class="merchant-sidebar">
      <div class="sidebar-logo">
        <h2>商户入驻平台</h2>
      </div>

      <div class="sidebar-menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: currentPath === item.path }"
        >
          <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
          <span class="menu-text">{{ item.title }}</span>
        </router-link>
      </div>

      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <main class="merchant-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
:deep(.el-icon) {
  font-size: 18px;
}
</style>
