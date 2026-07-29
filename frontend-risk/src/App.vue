<template>
  <div class="risk-layout">
    <header class="risk-header">
      <div class="logo">🛡️ 风控稽核管理平台</div>
      <div class="header-right">
        <span>{{ realName }}</span>
        <el-button link style="color:#e0e0e0;margin-left:12px" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <aside class="risk-sidebar">
      <div class="sidebar-menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          active-class="active"
          exact-active-class="active"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span class="menu-text">{{ item.title }}</span>
        </router-link>
      </div>
    </aside>

    <main class="risk-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const realName = ref('系统管理员')

const menuItems = [
  { path: '/dashboard', title: '风控看板', icon: '📊' },
  { path: '/events', title: '风控事件', icon: '⚠️' },
  { path: '/rules', title: '规则管理', icon: '🔧' },
  { path: '/blacklist', title: '名单库', icon: '📋' },
  { path: '/disposals', title: '处置管理', icon: '⚡' },
  { path: '/analysis', title: '数据分析', icon: '📈' }
]

onMounted(() => {
  const name = localStorage.getItem('sso_realName')
  if (name) {
    realName.value = name
  }
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出吗？', '提示', { type: 'warning' })
    localStorage.removeItem('sso_token')
    localStorage.removeItem('sso_realName')
    window.location.href = 'http://localhost:5173/#/login'
  } catch {}
}
</script>
