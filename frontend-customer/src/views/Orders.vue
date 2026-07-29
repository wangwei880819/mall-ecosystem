<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'

const router = useRouter()
const userStore = useUserStore()

const orders = ref([])
const activeTab = ref(0)
const loading = ref(false)

const tabs = [
  { label: '全部', value: 0 },
  { label: '待支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const statusMap = {
  'CREATED': '待支付',
  'PAID': '已支付',
  'SHIPPED': '已发货',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消'
}

const statusBadgeClass = {
  'CREATED': 'badge-red',
  'PAID': 'badge-blue',
  'SHIPPED': 'badge-orange',
  'COMPLETED': 'badge-green',
  'CANCELLED': 'badge-gray'
}

const tabToStatus = { 0: '', 1: 'CREATED', 2: 'PAID', 3: 'COMPLETED', 4: 'CANCELLED' }

const filteredOrders = computed(() => {
  if (activeTab.value === 0) return orders.value
  const status = tabToStatus[activeTab.value]
  if (status === 'PAID') {
    return orders.value.filter(o => o.status === 'PAID' || o.status === 'SHIPPED')
  }
  return orders.value.filter(o => o.status === status)
})

async function fetchOrders() {
  loading.value = true
  try {
    const params = {}
    if (userStore.userInfo.phone) {
      params.customerPhone = userStore.userInfo.phone
    }
    const res = await httpInstance.get('/orders', { params })
    const list = res.result || []
    // 确保每个order都有items数组
    orders.value = list.map(order => ({
      ...order,
      items: order.items || (order.productName ? [{
        productName: order.productName,
        productImage: order.productImage || '',
        imageUrl: order.productImage || '',
        price: order.price || 0,
        quantity: order.quantity || 1
      }] : []),
      imageUrl: order.productImage || ''
    }))
  } catch (e) {
    // error handled by http interceptor
  } finally {
    loading.value = false
  }
}

function switchTab(index) {
  activeTab.value = index
}

function goPay(orderId) {
  router.push(`/pay?orderId=${orderId}`)
}

function goHome() {
  router.push('/')
}

async function cancelOrder(order) {
  if (!confirm('确定要取消该订单吗？')) return
  try {
    await httpInstance.put(`/orders/${order.id}/cancel`)
    toast('订单已取消')
    fetchOrders()
  } catch (e) {
    // error handled by http interceptor
  }
}

async function confirmReceive(order) {
  if (!confirm('确认已收到商品吗？')) return
  try {
    await httpInstance.put(`/orders/${order.id}/receive`)
    toast('已确认收货')
    fetchOrders()
  } catch (e) {
    // error handled by http interceptor
  }
}

async function remindShip(order) {
  try {
    await httpInstance.put(`/orders/${order.id}/remind`)
    toast('已提醒发货')
  } catch (e) {
    // error handled by http interceptor
  }
}

async function deleteOrder(order) {
  if (!confirm('确定要删除该订单吗？')) return
  try {
    await httpInstance.delete(`/orders/${order.id}`)
    toast('订单已删除')
    fetchOrders()
  } catch (e) {
    // error handled by http interceptor
  }
}

function rebuyOrder(order) {
  router.push('/')
  toast('已加入购物车')
}

function goReview(order) {
  router.push(`/review?orderId=${order.id}`)
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="orders-page">
    <!-- Header -->
    <header class="page-header">
      <button class="back-btn" @click="router.back()">←</button>
      <span class="header-title">我的订单</span>
      <span class="header-right"></span>
    </header>

    <!-- Tab Bar -->
    <div class="tab-bar-wrapper">
      <div
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <!-- Order List -->
    <template v-else-if="filteredOrders.length > 0">
      <div
        v-for="order in filteredOrders"
        :key="order.id"
        class="order-card card fade-in-up"
      >
        <!-- Order Header -->
        <div class="order-header">
          <span class="order-code">订单号: {{ order.orderCode }}</span>
          <span class="status-badge" :class="statusBadgeClass[order.status]">
            {{ statusMap[order.status] || order.status }}
          </span>
        </div>

        <!-- Order Items -->
        <div
          v-for="item in order.items"
          :key="item.id || item.skuId"
          class="order-item"
        >
          <img :src="item.imageUrl || item.productImage" alt="" class="item-img" />
          <div class="item-info">
            <div class="item-name ellipsis-2">{{ item.productName }}</div>
            <div class="item-price-row">
              <span class="item-price">¥{{ item.price }}</span>
              <span class="item-quantity">x{{ item.quantity }}</span>
            </div>
          </div>
        </div>

        <!-- Order Footer -->
        <div class="order-footer">
          <span class="order-total">
            共{{ order.items.reduce((sum, it) => sum + it.quantity, 0) }}件商品
            合计: <span class="total-amount">¥{{ order.orderAmount }}</span>
          </span>
        </div>

        <!-- Action Buttons -->
        <div class="order-actions">
          <!-- CREATED: 取消订单 + 去支付 -->
          <template v-if="order.status === 'CREATED'">
            <button class="btn-danger" @click="cancelOrder(order)">取消订单</button>
            <button class="btn-primary-sm" @click="goPay(order.id)">去支付</button>
          </template>

          <!-- PAID: 提醒发货 -->
          <template v-else-if="order.status === 'PAID'">
            <button class="btn-outline" @click="remindShip(order)">提醒发货</button>
          </template>

          <!-- SHIPPED: 确认收货 -->
          <template v-else-if="order.status === 'SHIPPED'">
            <button class="btn-primary-sm" @click="confirmReceive(order)">确认收货</button>
          </template>

          <!-- COMPLETED: 评价 + 再次购买 -->
          <template v-else-if="order.status === 'COMPLETED'">
            <button class="btn-outline" @click="goReview(order)">评价</button>
            <button class="btn-primary-sm" @click="rebuyOrder(order)">再次购买</button>
          </template>

          <!-- CANCELLED: 删除订单 -->
          <template v-else-if="order.status === 'CANCELLED'">
            <button class="btn-danger" @click="deleteOrder(order)">删除订单</button>
          </template>
        </div>
      </div>
    </template>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-icon">📦</div>
      <div class="empty-text">暂无相关订单</div>
      <button class="btn-primary-empty" @click="goHome">去逛逛</button>
    </div>
  </div>
</template>

<style scoped>
.orders-page {
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
.header-right {
  width: 36px;
}

/* ========== Tab Bar ========== */
.tab-bar-wrapper {
  position: sticky;
  top: 48px;
  z-index: 99;
  display: flex;
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 8px;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
}
.tab-item.active {
  color: #667eea;
  font-weight: 600;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 2px;
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

/* ========== Order Card ========== */
.order-card {
  padding: 0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px 0;
}
.order-code {
  font-size: 13px;
  color: #666;
}
.status-badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 500;
}
.badge-red {
  background: #fef0f0;
  color: #e74c3c;
}
.badge-blue {
  background: #ecf5ff;
  color: #409eff;
}
.badge-orange {
  background: #fdf6ec;
  color: #e6a23c;
}
.badge-green {
  background: #f0f9eb;
  color: #67c23a;
}
.badge-gray {
  background: #f5f5f5;
  color: #999;
}

/* ========== Order Item ========== */
.order-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;
}
.order-item:last-of-type {
  border-bottom: none;
}
.item-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: #f5f5f5;
}
.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.item-name {
  font-size: 14px;
  color: #222;
  line-height: 1.4;
}
.item-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item-price {
  font-size: 15px;
  color: #e74c3c;
  font-weight: 600;
}
.item-quantity {
  font-size: 13px;
  color: #999;
}

/* ========== Order Footer ========== */
.order-footer {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
  border-top: 1px solid #f5f5f5;
}
.order-total {
  font-size: 13px;
  color: #666;
}
.total-amount {
  font-size: 15px;
  color: #e74c3c;
  font-weight: 600;
}

/* ========== Action Buttons ========== */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 16px 14px;
}

.btn-primary-sm {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn-primary-sm:active {
  opacity: 0.85;
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 20px;
  background: #fff;
  color: #667eea;
  border: 1px solid #667eea;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-outline:active {
  background: #667eea;
  color: #fff;
}

.btn-danger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 20px;
  background: #fff;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-danger:active {
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
