<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'

const router = useRouter()
const userStore = useUserStore()

const addresses = ref([])
const loading = ref(false)

async function fetchAddresses() {
  loading.value = true
  try {
    const res = await httpInstance.get('/address')
    addresses.value = res.result || []
  } catch (e) {
    // error handled by http interceptor
  } finally {
    loading.value = false
  }
}

function goAdd() {
  router.push('/address/edit')
}

function goEdit(id) {
  router.push(`/address/edit/${id}`)
}

async function handleDelete(address) {
  if (!confirm('确定删除该收货地址吗？')) return
  try {
    await httpInstance.delete(`/address/${address.id}`)
    toast('已删除')
    fetchAddresses()
  } catch (e) {
    // error handled by http interceptor
  }
}

onMounted(() => {
  if (!userStore.userInfo?.token) {
    toast('请先登录')
    router.push('/login')
    return
  }
  fetchAddresses()
})
</script>

<template>
  <div class="address-page">
    <!-- Header -->
    <header class="page-header">
      <button class="back-btn" @click="router.back()">←</button>
      <span class="header-title">收货地址</span>
      <button class="header-add-btn" @click="goAdd">+ 新增</button>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <!-- Address List -->
    <template v-else-if="addresses.length > 0">
      <div
        v-for="address in addresses"
        :key="address.id"
        class="address-card card fade-in-up"
      >
        <!-- Contact Info -->
        <div class="contact-row">
          <span class="contact-name">{{ address.name }}</span>
          <span class="contact-phone">{{ address.phone }}</span>
          <span v-if="address.isDefault === 1" class="default-chip">默认</span>
        </div>

        <!-- Full Address -->
        <div class="address-detail">
          {{ address.province }}{{ address.city }}{{ address.district }}{{ address.address }}
        </div>

        <!-- Actions -->
        <div class="address-actions">
          <button class="action-btn edit-btn" @click="goEdit(address.id)">编辑</button>
          <button class="action-btn delete-btn" @click="handleDelete(address)">删除</button>
        </div>
      </div>
    </template>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-icon">📍</div>
      <div class="empty-text">暂无收货地址</div>
      <button class="btn-primary-empty" @click="goAdd">添加新地址</button>
    </div>
  </div>
</template>

<style scoped>
.address-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 20px;
}

/* ========== Header ========== */
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  min-height: 48px;
}
.back-btn {
  display: flex;
  align-items: center;
  padding: 6px;
  border: none;
  background: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
}
.header-title {
  flex: 1;
  text-align: center;
}
.header-add-btn {
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.header-add-btn:active {
  background: rgba(255, 255, 255, 0.35);
}

/* ========== Loading ========== */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

/* ========== Card ========== */
.card {
  background: #fff;
  border-radius: 12px;
  margin: 12px 16px;
  overflow: hidden;
}

/* ========== Address Card ========== */
.address-card {
  padding: 16px;
}

.contact-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.contact-name {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}
.contact-phone {
  font-size: 14px;
  color: #666;
}
.default-chip {
  padding: 2px 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 11px;
  border-radius: 10px;
  font-weight: 500;
}

.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 14px;
  padding-right: 40px;
}

.address-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.action-btn {
  padding: 6px 18px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
}
.edit-btn {
  color: #667eea;
  border-color: #667eea;
}
.edit-btn:active {
  background: #667eea;
  color: #fff;
}
.delete-btn {
  color: #e74c3c;
  border-color: #e74c3c;
}
.delete-btn:active {
  background: #e74c3c;
  color: #fff;
}

/* ========== Empty State ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #ccc;
}
.empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
  opacity: 0.5;
}
.empty-text {
  font-size: 15px;
  color: #999;
  margin-bottom: 24px;
}
.btn-primary-empty {
  padding: 12px 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 25px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn-primary-empty:active {
  opacity: 0.85;
}

/* ========== Animation ========== */
.fade-in-up {
  animation: fadeInUp 0.3s ease-out;
}
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
