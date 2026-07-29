<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()

const orderId = computed(() => route.query.orderId)
const orderInfo = ref(null)
const loading = ref(false)
const paying = ref(false)
const remainingSeconds = ref(0)
const countdownText = ref('')
let countdownTimer = null

function formatCountdown(seconds) {
  if (seconds <= 0) return '00:00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function startCountdown(createTime) {
  const created = new Date(createTime).getTime()
  const deadline = created + 30 * 60 * 1000 // 30 minutes

  function tick() {
    const now = Date.now()
    const remain = Math.max(0, Math.floor((deadline - now) / 1000))
    remainingSeconds.value = remain
    countdownText.value = formatCountdown(remain)

    if (remain <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }

  tick()
  countdownTimer = setInterval(tick, 1000)
}

async function fetchOrderDetail() {
  if (!orderId.value) {
    toast('订单号不存在')
    return
  }

  loading.value = true
  try {
    const res = await httpInstance.get(`/orders/${orderId.value}`)
    orderInfo.value = res.result
    if (orderInfo.value.createTime) {
      startCountdown(orderInfo.value.createTime)
    }
  } catch (error) {
    toast(error.message || '获取订单信息失败')
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  if (paying.value) return
  paying.value = true
  try {
    await httpInstance.post(`/orders/${orderId.value}/pay`)
    toast('支付成功')
    setTimeout(() => router.push('/orders'), 800)
  } catch (error) {
    toast(error.message || '支付失败')
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  fetchOrderDetail()
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<template>
  <div class="pay-page">
    <!-- Header -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">←</button>
      <span>支付</span>
      <span class="header-right"></span>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="empty-state">
      <span class="empty-icon">⏳</span>
      <span class="empty-text">正在加载订单信息...</span>
    </div>

    <!-- Content -->
    <div v-else-if="orderInfo" class="pay-content fade-in-up">
      <!-- Success Icon -->
      <div class="success-section">
        <div class="success-icon">✅</div>
        <p class="success-title">订单已生成</p>
      </div>

      <!-- Order Card -->
      <div class="order-card card">
        <div class="order-row">
          <span class="order-label">订单号</span>
          <span class="order-value">{{ orderInfo.orderNo || orderInfo.id || orderId }}</span>
        </div>
        <div class="order-row">
          <span class="order-label">应付金额</span>
          <span class="order-price">¥{{ Number(orderInfo.payAmount || orderInfo.totalAmount || 0).toFixed(2) }}</span>
        </div>
        <div class="order-row">
          <span class="order-label">支付方式</span>
          <span class="order-value">微信支付<span class="demo-tag">演示模式</span></span>
        </div>

        <button
          class="pay-btn btn-primary"
          :class="{ disabled: paying }"
          :disabled="paying"
          @click="handlePay"
        >
          {{ paying ? '支付中...' : '去支付（演示模式）' }}
        </button>
      </div>

      <!-- Countdown -->
      <div class="countdown-section">
        <p v-if="remainingSeconds > 0" class="countdown-row">
          ⏱ 请在 <span class="countdown-time">{{ countdownText }}</span> 内完成支付
        </p>
        <p class="countdown-hint">超时订单将自动取消</p>
      </div>

      <!-- View Orders -->
      <div class="view-orders" @click="router.push('/orders')">
        <span>查看订单</span>
        <span class="arrow">›</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pay-page {
  min-height: 100vh;
  background: #f5f6fa;
}

/* Success Section */
.success-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px 28px;
}

.success-icon {
  font-size: 64px;
  margin-bottom: 16px;
  background: #e8f8e8;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

/* Order Card */
.order-card {
  padding: 20px;
}

.order-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.order-row:last-of-type {
  border-bottom: none;
  margin-bottom: 20px;
}

.order-label {
  font-size: 14px;
  color: #999;
}

.order-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.order-price {
  font-size: 20px;
  color: #e74c3c;
  font-weight: 700;
}

/* Pay Button */
.pay-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.pay-btn:active {
  opacity: 0.85;
}

.pay-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* Countdown */
.countdown-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 20px 10px;
}

.countdown-row {
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.countdown-time {
  color: #e74c3c;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.countdown-hint {
  font-size: 12px;
  color: #ccc;
}

/* View Orders */
.view-orders {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 16px;
  margin: 0 16px;
  font-size: 15px;
  color: #667eea;
  cursor: pointer;
  font-weight: 500;
}

.view-orders:active {
  opacity: 0.7;
}

.view-orders .arrow {
  font-size: 20px;
}

.demo-tag {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 6px;
  font-size: 11px;
  color: #f56c6c;
  background: #fef0f0;
  border: 1px solid #f56c6c;
  border-radius: 3px;
  vertical-align: middle;
}
</style>
