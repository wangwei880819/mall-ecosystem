<script setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { useCartStore } from '@/stores/cartStore'
import { useUserStore } from '@/stores/userStore'
import TabBar from '@/components/TabBar.vue'

const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

const showTabBar = computed(() => {
  const hideRoutes = ['Detail', 'Checkout', 'Pay', 'Login', 'Register', 'Orders', 'Address', 'AddressEdit']
  return !hideRoutes.includes(route.name)
})
</script>

<template>
  <keep-alive include="Home,Cart,Profile">
    <router-view :key="$route.fullPath" />
  </keep-alive>
  <TabBar v-if="showTabBar" />
</template>
