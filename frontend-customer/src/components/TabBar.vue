<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

const tabs = [
  { key: 'Home', label: '首页', icon: '🏠', path: '/' },
  { key: 'Cart', label: '购物车', icon: '🛒', path: '/cart' },
  { key: 'Profile', label: '我的', icon: '👤', path: '/profile' }
]

const activeTab = computed(() => {
  const match = tabs.find(t => t.key === route.name)
  return match ? match.key : 'Home'
})

const cartBadge = computed(() => cartStore.allCount || 0)

function switchTab(tab) {
  router.push(tab.path)
}
</script>

<template>
  <nav class="tab-bar">
    <div
      v-for="tab in tabs"
      :key="tab.key"
      class="tab-item"
      :class="{ active: activeTab === tab.key }"
      @click="switchTab(tab)"
    >
      <span class="tab-icon">{{ tab.icon }}</span>
      <span>{{ tab.label }}</span>
      <span v-if="tab.key === 'Cart' && cartBadge > 0" class="badge">{{ cartBadge > 99 ? '99+' : cartBadge }}</span>
    </div>
  </nav>
</template>
