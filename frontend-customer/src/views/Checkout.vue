<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import { useUserStore } from '@/stores/userStore'
import { createOrderAPI } from '@/apis/checkout'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

const address = ref(null)
const addressLoading = ref(true)
const paymentMethod = ref('wechat')
const submitting = ref(false)
const rechargePhone = ref('')

// 直接购买商品（从商品详情页立即购买进入）
const directBuyItem = ref(null)

// 获取选中的购物车商品
const selectedItems = computed(() => {
  if (directBuyItem.value) {
    return [directBuyItem.value]
  }
  return cartStore.cartList.filter(item => item.selected)
})

// 检测是否为虚拟/权益类商品
const isDigitalOrder = computed(() =>
  selectedItems.value.some(item => item.isDigital)
)

// 计算金额
const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + item.price * item.count, 0)
)

const shippingFee = computed(() => {
  if (selectedItems.value.length === 0 || isDigitalOrder.value) return 0
  return totalAmount.value >= 99 ? 0 : 10
})

const discount = computed(() => 0)

const finalAmount = computed(() =>
  Math.max(0, totalAmount.value + shippingFee.value - discount.value)
)

// 获取默认地址（仅实物商品需要）
const fetchAddress = async () => {
  if (isDigitalOrder.value) {
    addressLoading.value = false
    return
  }
  addressLoading.value = true
  try {
    const res = await httpInstance({ url: '/address' })
    const list = res.result || []
    const defaultAddr = list.find(item => item.isDefault === 1)
    address.value = defaultAddr || (list.length > 0 ? list[0] : null)
  } catch (e) {
    address.value = null
  } finally {
    addressLoading.value = false
  }
}

// 手机号格式验证
const isValidPhone = computed(() => {
  if (!rechargePhone.value) return false
  return /^1[3-9]\d{9}$/.test(rechargePhone.value)
})

onMounted(() => {
  // 检查是否为直接购买模式
  if (route.query.directBuy) {
    try {
      directBuyItem.value = JSON.parse(route.query.directBuy)
      directBuyItem.value.selected = true
    } catch (e) {
      console.error('解析直接购买参数失败:', e)
    }
  }

  if (selectedItems.value.length === 0) {
    toast('请先选择商品')
    router.replace('/cart')
    return
  }
  fetchAddress()
})

// 切换支付方式
const selectPayment = (method) => {
  paymentMethod.value = method
}

// 提交订单
const submitOrder = async () => {
  if (selectedItems.value.length === 0) {
    toast('没有可提交的商品')
    return
  }

  // 虚拟商品：验证手机号
  if (isDigitalOrder.value) {
    if (!rechargePhone.value.trim()) {
      toast('请输入充值手机号')
      return
    }
    if (!isValidPhone.value) {
      toast('请输入正确的手机号')
      return
    }
  }

  // 实物商品：验证收货地址
  if (!isDigitalOrder.value) {
    if (!address.value) {
      toast('请添加收货地址')
      return
    }
  }

  submitting.value = true
  try {
    const res = await createOrderAPI({
      items: selectedItems.value.map(item => ({
        skuId: item.skuId || item.id,
        count: item.count,
        rechargePhone: isDigitalOrder.value ? rechargePhone.value : '',
        isDigital: item.isDigital || false
      })),
      addressId: isDigitalOrder.value ? null : address.value?.id,
      paymentMethod: paymentMethod.value,
      customerPhone: userStore.userInfo?.phone || ''
    })
    const orderId = res.result?.orderId || res.result?.id
    if (orderId) {
      if (!directBuyItem.value) {
        selectedItems.value.forEach(item => {
          cartStore.delCart(item.skuId)
        })
      }
      router.push(`/pay?orderId=${orderId}`)
    } else {
      toast('创建订单失败')
    }
  } catch (e) {
    // 错误已在 http 拦截器中处理
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="checkout-page">
    <!-- Header -->
    <header class="page-header">
      <button class="back-btn" @click="router.back()">←</button>
      <span class="header-title">确认订单</span>
      <span class="header-right"></span>
    </header>

    <!-- 虚拟商品：充值手机号 -->
    <div v-if="isDigitalOrder" class="phone-section card">
      <div class="section-title">充值账号</div>
      <div class="phone-input-wrapper">
        <span class="phone-icon">📱</span>
        <input
          v-model="rechargePhone"
          type="tel"
          maxlength="11"
          placeholder="请输入需要充值的手机号"
          class="phone-input"
        />
      </div>
      <p class="phone-tip">请仔细核对手机号，充值成功后不可退款</p>
    </div>

    <!-- 实物商品：收货地址 -->
    <div v-if="!isDigitalOrder" class="address-section card" @click="router.push('/address')">
      <div class="address-header">
        <span class="address-icon">📍</span>
        <span class="address-label">收货地址</span>
        <span class="address-arrow">›</span>
      </div>
      <div v-if="addressLoading" class="address-loading">加载中...</div>
      <template v-else-if="address">
        <div class="address-info">
          <div class="address-contact">
            <span class="address-name">{{ address.receiverName || address.name }}</span>
            <span class="address-phone">{{ address.receiverPhone || address.phone }}</span>
          </div>
          <div class="address-detail">
            {{ address.province || '' }}{{ address.city || '' }}{{ address.district || '' }} {{ address.detailAddress || address.address }}
          </div>
        </div>
        <div class="address-action">选择其他地址</div>
      </template>
      <div v-else class="address-empty" @click.stop="router.push('/address')">
        请添加收货地址
      </div>
    </div>

    <!-- Order Items -->
    <div v-if="selectedItems.length > 0" class="items-section card">
      <div class="section-title">商品信息</div>
      <div
        v-for="item in selectedItems"
        :key="item.skuId || item.id"
        class="order-item"
      >
        <img :src="item.picture" alt="" class="item-img" />
        <div class="item-info">
          <div class="item-name ellipsis-2">{{ item.name }}</div>
          <div class="item-spec" v-if="item.attrsText && item.attrsText !== '默认'">
            规格: {{ item.attrsText }}
          </div>
          <div v-if="item.isDigital" class="item-tag-digital">虚拟商品</div>
          <div class="item-price-row">
            <span class="item-price">¥{{ item.price }}</span>
            <span class="item-count">x{{ item.count }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-icon">🛒</div>
      <div class="empty-text">没有选中的商品</div>
    </div>

    <!-- Order Summary -->
    <div v-if="selectedItems.length > 0" class="summary-section card">
      <div class="summary-row">
        <span class="summary-label">商品总额</span>
        <span class="summary-value">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
      <div v-if="!isDigitalOrder" class="summary-row">
        <span class="summary-label">运费</span>
        <span class="summary-value">
          <template v-if="shippingFee === 0">¥0.00 (免运费)</template>
          <template v-else>¥{{ shippingFee.toFixed(2) }}</template>
        </span>
      </div>
      <div v-if="discount > 0" class="summary-row discount-row">
        <span class="summary-label">爱豆抵扣</span>
        <span class="summary-value discount-value">-¥{{ discount.toFixed(2) }}</span>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-row total-row">
        <span class="summary-label">实付金额</span>
        <span class="summary-value final-price">¥{{ finalAmount.toFixed(2) }}</span>
      </div>
    </div>

    <!-- Payment Method -->
    <div v-if="selectedItems.length > 0" class="payment-section card">
      <div class="section-title">支付方式</div>
      <div
        class="payment-option"
        :class="{ active: paymentMethod === 'wechat' }"
        @click="selectPayment('wechat')"
      >
        <span class="payment-icon">💳</span>
        <span class="payment-name">微信支付</span>
        <span class="payment-radio" :class="{ checked: paymentMethod === 'wechat' }"></span>
      </div>
      <div
        class="payment-option"
        :class="{ active: paymentMethod === 'alipay' }"
        @click="selectPayment('alipay')"
      >
        <span class="payment-icon">💳</span>
        <span class="payment-name">支付宝</span>
        <span class="payment-radio" :class="{ checked: paymentMethod === 'alipay' }"></span>
      </div>
    </div>

    <!-- Bottom spacer -->
    <div v-if="selectedItems.length > 0" class="bottom-spacer"></div>

    <!-- Fixed Bottom Bar -->
    <div v-if="selectedItems.length > 0" class="bottom-bar">
      <div class="bottom-total">
        <span class="total-label">合计:</span>
        <span class="total-price">¥{{ finalAmount.toFixed(2) }}</span>
      </div>
      <button
        class="submit-btn"
        :class="{ disabled: submitting }"
        :disabled="submitting"
        @click="submitOrder"
      >
        {{ submitting ? '提交中...' : '提交订单' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.checkout-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 70px;
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

/* ========== Card ========== */
.card {
  background: #fff;
  border-radius: 12px;
  margin: 10px 12px;
  padding: 16px;
  overflow: hidden;
}

/* ========== Phone Section (虚拟商品) ========== */
.phone-section .section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}
.phone-input-wrapper {
  display: flex;
  align-items: center;
  background: #f8f9fb;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  padding: 0 14px;
  margin-bottom: 8px;
}
.phone-icon {
  font-size: 18px;
  margin-right: 10px;
  flex-shrink: 0;
}
.phone-input {
  flex: 1;
  padding: 14px 0;
  font-size: 15px;
  border: none;
  background: transparent;
  color: #333;
  outline: none;
}
.phone-input::placeholder {
  color: #ccc;
}
.phone-tip {
  font-size: 12px;
  color: #e74c3c;
  line-height: 1.5;
}

/* ========== Address Section ========== */
.address-section {
  cursor: pointer;
}
.address-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.address-icon {
  font-size: 16px;
  margin-right: 6px;
}
.address-label {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.address-arrow {
  font-size: 20px;
  color: #ccc;
}
.address-loading {
  color: #999;
  font-size: 14px;
  padding: 8px 0;
}
.address-info {
  margin-bottom: 8px;
}
.address-contact {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 4px;
}
.address-name {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}
.address-phone {
  font-size: 14px;
  color: #666;
}
.address-detail {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}
.address-action {
  font-size: 12px;
  color: #667eea;
  text-align: right;
}
.address-empty {
  text-align: center;
  padding: 16px 0;
  color: #667eea;
  font-size: 15px;
  font-weight: 500;
}

/* ========== Items Section ========== */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}
.order-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}
.order-item:last-child {
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
}
.item-name {
  font-size: 14px;
  color: #222;
  line-height: 1.4;
  margin-bottom: 4px;
}
.item-spec {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.item-tag-digital {
  display: inline-block;
  padding: 2px 8px;
  background: #fef0f0;
  color: #e74c3c;
  font-size: 11px;
  border-radius: 4px;
  margin-bottom: 4px;
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
.item-count {
  font-size: 13px;
  color: #999;
}

/* ========== Summary Section ========== */
.summary-section {
  padding-top: 14px;
  padding-bottom: 14px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}
.summary-label {
  font-size: 14px;
  color: #666;
}
.summary-value {
  font-size: 14px;
  color: #333;
}
.discount-row .discount-value {
  color: #667eea;
}
.summary-divider {
  height: 1px;
  background: #eee;
  margin: 8px 0;
}
.total-row .summary-label {
  font-size: 15px;
  font-weight: 600;
  color: #222;
}
.final-price {
  font-size: 18px;
  font-weight: 700;
  color: #e74c3c;
}

/* ========== Payment Section ========== */
.payment-option {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}
.payment-option:last-child {
  border-bottom: none;
}
.payment-icon {
  font-size: 20px;
  margin-right: 10px;
}
.payment-name {
  flex: 1;
  font-size: 15px;
  color: #333;
}
.payment-radio {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 50%;
  transition: all 0.2s;
  position: relative;
}
.payment-radio.checked {
  border-color: #667eea;
  background: #667eea;
}
.payment-radio.checked::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 10px;
  height: 10px;
  background: #fff;
  border-radius: 50%;
}

/* ========== Bottom spacer ========== */
.bottom-spacer {
  height: 70px;
}

/* ========== Fixed Bottom Bar ========== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  padding: 10px 16px;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid #eee;
  gap: 12px;
}
.bottom-total {
  flex: 1;
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.total-label {
  font-size: 14px;
  color: #333;
}
.total-price {
  font-size: 20px;
  font-weight: 700;
  color: #e74c3c;
}
.submit-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 22px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
  white-space: nowrap;
}
.submit-btn:active {
  opacity: 0.85;
}
.submit-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
