<template>
  <div class="cmall-orders">
    <header class="orders-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <h1>我的订单</h1>
    </header>

    <div class="tabs">
      <button v-for="tab in tabs" :key="tab.key" 
              :class="{ active: currentTab === tab.key }"
              @click="currentTab = tab.key">
        {{ tab.label }}
      </button>
    </div>

    <div class="orders-list" v-if="orders.length > 0">
      <div class="order-card" v-for="order in filteredOrders" :key="order.id">
        <div class="order-header">
          <span class="order-code">订单号: {{ order.orderCode }}</span>
          <span class="order-status" :class="order.status">{{ statusLabels[order.status] }}</span>
        </div>
        
        <div class="order-items">
          <div class="order-item" v-for="item in order.items" :key="item.id">
            <img :src="item.image" :alt="item.name" class="item-image" />
            <div class="item-info">
              <h3 class="item-name">{{ item.name }}</h3>
              <p class="item-spec">{{ item.spec }}</p>
              <div class="item-price-row">
                <span class="item-price">¥{{ item.price }}</span>
                <span class="item-quantity">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <span class="total-price">合计: ¥{{ order.totalAmount }}</span>
          <div class="order-actions">
            <button v-if="order.status === 'PENDING_PAY'" @click="handlePay(order)">去支付</button>
            <button v-if="order.status === 'PAID'" @click="handleConfirm(order)">确认收货</button>
            <button v-if="order.status === 'SHIPPED'" @click="handleConfirm(order)">确认收货</button>
            <button v-if="order.status === 'COMPLETED'" @click="handleReview(order)">评价</button>
            <button v-if="order.status === 'PENDING_PAY'" @click="handleCancel(order)">取消订单</button>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-orders" v-else>
      <div class="empty-icon">📋</div>
      <p>暂无订单</p>
      <button class="go-shopping" @click="$router.push('/mall')">去逛逛</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const currentTab = ref('all')
const orders = ref([])

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'pending_pay', label: '待支付' },
  { key: 'paid', label: '待发货' },
  { key: 'shipped', label: '待收货' },
  { key: 'completed', label: '已完成' },
]

const statusLabels = {
  'PENDING_PAY': '待支付',
  'PAID': '已支付/待发货',
  'SHIPPED': '已发货/待收货',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消',
  'REFUNDING': '退款中',
}

const filteredOrders = computed(() => {
  if (currentTab.value === 'all') return orders.value
  
  const statusMap = {
    'pending_pay': 'PENDING_PAY',
    'paid': 'PAID',
    'shipped': 'SHIPPED',
    'completed': 'COMPLETED',
  }
  
  return orders.value.filter(o => o.status === statusMap[currentTab.value])
})

onMounted(async () => {
  const customerId = localStorage.getItem('customer_id')
  if (!customerId) return
  
  try {
    const result = await fetch(`/api/order/list?customerId=${customerId}`)
      .then(res => res.json())
    if (result.code === 200) {
      orders.value = result.data
    }
  } catch (error) {
    console.error('获取订单失败', error)
  }
})

const handlePay = (order) => {
  alert('支付功能开发中')
}

const handleConfirm = (order) => {
  alert('确认收货功能开发中')
}

const handleReview = (order) => {
  alert('评价功能开发中')
}

const handleCancel = (order) => {
  alert('取消订单功能开发中')
}
</script>

<style scoped>
.cmall-orders {
  min-height: 100vh;
  background: #f5f5f5;
}

.orders-header {
  background: white;
  padding: 15px 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.back-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
}

.orders-header h1 {
  flex: 1;
  margin: 0;
  font-size: 20px;
}

.tabs {
  display: flex;
  background: white;
  padding: 10px 20px;
  gap: 10px;
  overflow-x: auto;
}

.tabs button {
  padding: 8px 16px;
  background: #f5f5f5;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.tabs button.active {
  background: #667eea;
  color: white;
}

.orders-list {
  padding: 20px;
}

.order-card {
  background: white;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}

.order-code {
  font-size: 14px;
  color: #666;
}

.order-status {
  font-size: 14px;
  font-weight: bold;
}

.order-status.PENDING_PAY {
  color: #e74c3c;
}

.order-status.PAID {
  color: #3498db;
}

.order-status.SHIPPED {
  color: #f39c12;
}

.order-status.COMPLETED {
  color: #27ae60;
}

.order-status.CANCELLED {
  color: #999;
}

.order-items {
  margin-bottom: 15px;
}

.order-item {
  display: flex;
  gap: 15px;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.item-spec {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #999;
}

.item-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  color: #e74c3c;
  font-weight: bold;
}

.item-quantity {
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f5f5f5;
}

.total-price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.order-actions button {
  padding: 8px 20px;
  background: #f5f5f5;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.order-actions button:nth-child(1) {
  background: #667eea;
  color: white;
}

.empty-orders {
  text-align: center;
  padding: 100px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-orders p {
  color: #999;
  margin: 0 0 20px 0;
}

.go-shopping {
  padding: 12px 40px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  cursor: pointer;
}
</style>