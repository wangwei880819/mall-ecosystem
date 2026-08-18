<template>
  <div v-if="isCMallPage" class="cmall-layout">
    <router-view />
  </div>
  <div v-else-if="isLoginPage || isPlatformSelectPage">
    <router-view />
  </div>
  <div v-else class="app-layout">
    <header class="app-header">
      <div class="header-left">
        <span class="logo">🛒 商城生态运营系统</span>
      </div>
      <div class="header-right">
        <button @click="openCMall" class="mall-entry">
          🛒 进入C端商城
        </button>
        <span class="user-info">👤 {{ user?.realName || '管理员' }}</span>
        <button @click="handleLogout" class="logout-btn">退出</button>
      </div>
    </header>

    <aside class="app-sidebar">
      <nav class="sidebar-nav">
        <template v-for="menu in dynamicMenus" :key="menu.id">
          <template v-if="menu.children && menu.children.length > 0">
            <div class="nav-item" 
                 :class="{ active: isMenuActive(menu) }" 
                 @click="toggleMenu(menu.id)">
              <span class="nav-icon">{{ menu.icon }}</span>
              <span class="nav-text">{{ menu.name }}</span>
              <span class="nav-arrow">{{ expandedMenus.includes(menu.id) ? '▼' : '▶' }}</span>
            </div>
            <div v-if="expandedMenus.includes(menu.id)" class="nav-submenu">
              <router-link v-for="child in menu.children" :key="child.id" :to="child.path" class="nav-subitem" active-class="active">
                <span class="nav-icon">{{ child.icon }}</span>
                <span class="nav-text">{{ child.name }}</span>
              </router-link>
            </div>
          </template>
          <router-link v-else :to="menu.path" class="nav-item" active-class="active">
            <span class="nav-icon">{{ menu.icon }}</span>
            <span class="nav-text">{{ menu.name }}</span>
          </router-link>
        </template>
      </nav>
      <div class="sidebar-footer">
        <div class="env-info">v1.0.0</div>
      </div>
    </aside>

    <main class="app-content">
      <router-view />
    </main>

    <LogFloat />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import LogFloat from './components/LogFloat.vue'
import { useRouter, useRoute } from 'vue-router'
import request from './utils/request'

const router = useRouter()
const route = useRoute()

const dynamicMenus = ref([])
const expandedMenus = ref([])

const isLoginPage = computed(() => route.path === '/login')
const isPlatformSelectPage = computed(() => route.path === '/platform-select')
const isCMallPage = computed(() => route.path.startsWith('/mall'))

const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}')
  } catch {
    return {}
  }
})

let routeWatcher = null

onMounted(() => {
  loadMenus()
  routeWatcher = router.afterEach(() => {
    if (!isLoginPage.value && !isPlatformSelectPage.value && !isCMallPage.value) {
      loadMenus()
    }
  })
})

onUnmounted(() => {
  if (routeWatcher) {
    routeWatcher()
  }
})

const isMenuActive = (menu) => {
  if (menu.path && route.path.startsWith(menu.path)) return true
  if (menu.children) {
    return menu.children.some(child => route.path.startsWith(child.path))
  }
  return false
}

const toggleMenu = (menuId) => {
  const index = expandedMenus.value.indexOf(menuId)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(menuId)
  }
}

const loadMenus = async () => {
  expandedMenus.value = []
  const userId = user.value.id
  if (userId) {
    try {
      const res = await request.get('/auth/menus', { params: { userId } })
      if (res.code === 200 && res.data && Array.isArray(res.data)) {
        dynamicMenus.value = res.data
        dynamicMenus.value.forEach(menu => {
          if (isMenuActive(menu)) {
            expandedMenus.value.push(menu.id)
          }
        })
      } else {
        console.warn('Menu data format error, using fallback menus')
        loadFallbackMenus()
      }
    } catch (error) {
      console.error('Failed to load menus from API:', error)
      loadFallbackMenus()
    }
  } else {
    loadFallbackMenus()
  }
}

const loadFallbackMenus = () => {
  dynamicMenus.value = [
    { id: 1, name: '首页', path: '/portal', icon: '🏠', type: 'DIRECTORY', children: [] },
    { id: 2, name: '商户管理', path: '/merchant', icon: '🏢', type: 'DIRECTORY', children: [
      { id: 11, name: '商户列表', path: '/merchant/list', icon: '📋', type: 'MENU', children: [] },
      { id: 12, name: '资质审核', path: '/merchant/audit', icon: '✅', type: 'MENU', children: [] },
      { id: 32, name: '业务复审', path: '/merchant/business-audit', icon: '📋', type: 'MENU', children: [] },
      { id: 33, name: '合规终审', path: '/merchant/compliance-audit', icon: '🔍', type: 'MENU', children: [] },
      { id: 34, name: '合同签署', path: '/merchant/contract-audit', icon: '📝', type: 'MENU', children: [] },
      { id: 35, name: '支付进件', path: '/merchant/payment-audit', icon: '💳', type: 'MENU', children: [] },
      { id: 41, name: '合同管理', path: '/merchant/contract-manage', icon: '📑', type: 'MENU', children: [] }
    ]},
    { id: 3, name: '客户管理', path: '/customer', icon: '👥', type: 'DIRECTORY', children: [
      { id: 13, name: '客户列表', path: '/customer/list', icon: '📋', type: 'MENU', children: [] },
      { id: 14, name: '客户标签', path: '/customer/tags', icon: '🏷️', type: 'MENU', children: [] }
    ]},
    { id: 4, name: '商品管理', path: '/product', icon: '📦', type: 'DIRECTORY', children: [
      { id: 16, name: '商品列表', path: '/product/list', icon: '📋', type: 'MENU', children: [] },
      { id: 36, name: '一级选品审核', path: '/product/audit', icon: '✅', type: 'MENU', children: [] },
      { id: 37, name: '二级选品审核', path: '/product/audit-2', icon: '✅', type: 'MENU', children: [] },
      { id: 17, name: '分类管理', path: '/product/category', icon: '📂', type: 'MENU', children: [] },
      { id: 18, name: '库存管理', path: '/product/stock', icon: '📦', type: 'MENU', children: [] },
      { id: 19, name: '权益引入', path: '/product/benefit', icon: '🎁', type: 'MENU', children: [] }
    ]},
    { id: 5, name: '订单管理', path: '/order', icon: '📋', type: 'DIRECTORY', children: [
      { id: 19, name: '订单列表', path: '/order/list', icon: '📋', type: 'MENU', children: [] },
      { id: 20, name: '支付管理', path: '/order/pay', icon: '💳', type: 'MENU', children: [] },
      { id: 21, name: '退款管理', path: '/order/refund', icon: '💰', type: 'MENU', children: [] },
      { id: 9, name: '订单评价', path: '/order/evaluation', icon: '⭐', type: 'MENU', children: [] }
    ]},
    { id: 6, name: '财务管理', path: '/finance', icon: '💰', type: 'DIRECTORY', children: [
      { id: 22, name: '结算管理', path: '/finance/settlement', icon: '📊', type: 'MENU', children: [] },
      { id: 23, name: '发票管理', path: '/finance/invoice', icon: '📄', type: 'MENU', children: [] },
      { id: 24, name: '对账管理', path: '/finance/reconciliation', icon: '🔍', type: 'MENU', children: [] },
      { id: 42, name: '保证金管理', path: '/finance/deposit', icon: '💰', type: 'MENU', children: [] },
      { id: 43, name: '佣金配置', path: '/finance/commission', icon: '⚙️', type: 'MENU', children: [] }
    ]},
    { id: 7, name: '风险管理', path: '/risk', icon: '🛡️', type: 'DIRECTORY', children: [
      { id: 25, name: '规则管理', path: '/risk/rules', icon: '📋', type: 'MENU', children: [] },
      { id: 26, name: '风险告警', path: '/risk/alerts', icon: '🚨', type: 'MENU', children: [] },
      { id: 27, name: '交易监控', path: '/risk/monitor', icon: '📈', type: 'MENU', children: [] }
    ]},
    { id: 8, name: '系统管理', path: '/system', icon: '⚙️', type: 'DIRECTORY', children: [
      { id: 28, name: '用户管理', path: '/system/users', icon: '👥', type: 'MENU', children: [] },
      { id: 29, name: '角色管理', path: '/system/roles', icon: '🎭', type: 'MENU', children: [] },
      { id: 30, name: '菜单管理', path: '/system/menus', icon: '📑', type: 'MENU', children: [] },
      { id: 31, name: '接入平台', path: '/system/platforms', icon: '🔗', type: 'MENU', children: [] }
    ]},
    { id: 11, name: '招商CRM', path: '/crm', icon: '🎯', type: 'DIRECTORY', children: [
      { id: 44, name: '线索管理', path: '/crm/leads', icon: '🎯', type: 'MENU', children: [] }
    ]},
    { id: 10, name: 'AI+应用', path: '/ai', icon: '🤖', type: 'DIRECTORY', children: [
      { id: 40, name: '模型配置', path: '/ai/config', icon: '⚙️', type: 'MENU', children: [] }
    ]}
  ]
  
  dynamicMenus.value.forEach(menu => {
    if (isMenuActive(menu)) {
      expandedMenus.value.push(menu.id)
    }
  })
}

const mallUrl = ref('/admin/mall')

const fetchMallUrl = async () => {
  try {
    const res = await request.get('/auth/sso/platforms')
    if (res.code === 200 && res.data) {
      const mall = res.data.find(p => p.systemCode === 'C_MALL' || p.name?.includes('C端商城'))
      if (mall?.url) {
        mallUrl.value = mall.url
      }
    }
  } catch (e) {
    // 使用默认值
  }
}

const openCMall = () => {
  window.open(mallUrl.value, '_blank')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('currentPlatform')
  dynamicMenus.value = []
  router.push('/login')
}

onMounted(() => {
  loadMenus()
  fetchMallUrl()
})
</script>

<style scoped>
.cmall-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.mall-entry {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: #fff;
  padding: 5px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  margin-right: 16px;
  transition: all 0.2s;
  font-weight: 400;
}

.mall-entry:hover {
  border-color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.logout-btn {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  margin-left: 12px;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}
</style>