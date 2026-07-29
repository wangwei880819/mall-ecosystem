<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import { useUserStore } from '@/stores/userStore'
import { toast } from '@/utils/toast'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const isLogin = ref(false)

onMounted(() => {
  if (userStore.userInfo && userStore.userInfo.token) {
    isLogin.value = true
    cartStore.updateNewList()
  }
})

const handleCheckout = () => {
  if (cartStore.selectedCount === 0) {
    toast('请选择商品')
    return
  }
  router.push('/checkout')
}

const handleAdd = (item) => {
  cartStore.addCart({ skuId: item.skuId, count: 1 })
}

const handleMinus = (item) => {
  if (item.count <= 1) {
    cartStore.delCart(item.skuId)
  } else {
    cartStore.addCart({ skuId: item.skuId, count: -1 })
  }
}

const handleDelete = async (item) => {
  try {
    await cartStore.delCart(item.skuId)
    toast('已删除')
  } catch (e) {
    toast('删除失败，请稍后重试')
  }
}

const shippingFee = () => {
  if (cartStore.selectedCount === 0) return 0
  return cartStore.selectedPrice >= 99 ? 0 : 10
}

const checkoutTotal = () => {
  return cartStore.selectedPrice + shippingFee()
}
</script>

<template>
  <div class="page-mobile cart-page">
    <!-- Header -->
    <div class="page-header">
      <span class="back-btn" @click="$router.back()">〈</span>
      <span>购物车</span>
      <span class="header-right"></span>
    </div>

    <!-- Empty State -->
    <div v-if="cartStore.cartList.length === 0" class="empty-state">
      <div class="empty-icon">🛒</div>
      <div class="empty-text">购物车是空的</div>
      <button class="btn-primary" style="width: 200px" @click="router.push('/')">去逛逛</button>
    </div>

    <!-- Cart Items -->
    <div v-else class="cart-content">
      <!-- Select All Bar -->
      <div class="select-all-bar card">
        <label class="checkbox-wrapper">
          <input
            type="checkbox"
            :checked="cartStore.isAll"
            @change="cartStore.allCheck($event.target.checked)"
          />
          <span class="checkmark"></span>
          <span class="select-all-text">全选</span>
        </label>
      </div>

      <!-- Cart List -->
      <div class="cart-list">
        <div
          v-for="item in cartStore.cartList"
          :key="item.skuId"
          class="cart-item card"
        >
          <div class="cart-item-body">
            <label class="checkbox-wrapper" @click.stop>
              <input
                type="checkbox"
                :checked="item.selected"
                @change="cartStore.singleCheck(item.skuId, $event.target.checked)"
              />
              <span class="checkmark"></span>
            </label>
            <img :src="item.picture" class="cart-item-img" alt="" />
            <div class="cart-item-info">
              <p class="cart-item-name ellipsis-2">{{ item.name }}</p>
              <p class="cart-item-price">¥{{ item.price }}</p>
              <div class="cart-item-actions">
                <div class="quantity-stepper">
                  <button class="stepper-btn" @click="handleMinus(item)">-</button>
                  <span class="stepper-val">{{ item.count }}</span>
                  <button class="stepper-btn" @click="handleAdd(item)">+</button>
                </div>
                <button class="delete-btn" @click="handleDelete(item)">🗑</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom Summary Bar -->
      <div class="bottom-bar">
        <div class="bottom-summary">
          <div class="summary-line">
            <span>已选 <em>{{ cartStore.selectedCount }}</em> 件</span>
            <span>合计: <em class="total-price">¥{{ cartStore.selectedPrice.toFixed(2) }}</em></span>
          </div>
          <div class="shipping-line">
            <span>运费: {{ shippingFee() === 0 ? '免运费' : '¥' + shippingFee().toFixed(2) }}</span>
          </div>
        </div>
        <button
          class="checkout-btn"
          :class="{ disabled: cartStore.selectedCount === 0 }"
          @click="handleCheckout"
        >
          结算(¥{{ checkoutTotal().toFixed(2) }})
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  padding-bottom: 100px;
}

.cart-content {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 48px);
}

.select-all-bar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 0;
}

.select-all-text {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
}

.checkbox-wrapper input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.checkmark {
  width: 22px;
  height: 22px;
  border: 2px solid #d0d0d0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.checkbox-wrapper input:checked + .checkmark {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
}

.checkbox-wrapper input:checked + .checkmark::after {
  content: '✓';
  color: #fff;
  font-size: 13px;
  font-weight: bold;
}

.cart-list {
  flex: 1;
}

.cart-item {
  margin-bottom: 8px;
}

.cart-item-body {
  display: flex;
  padding: 14px 12px;
  gap: 10px;
  align-items: flex-start;
}

.cart-item-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: #f5f5f5;
}

.cart-item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cart-item-name {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
}

.cart-item-price {
  font-size: 16px;
  color: #e74c3c;
  font-weight: 600;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.quantity-stepper {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
}

.stepper-btn {
  width: 32px;
  height: 32px;
  background: #f8f8f8;
  border: none;
  color: #333;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  padding: 0;
}

.stepper-btn:active {
  background: #e8e8e8;
}

.stepper-val {
  width: 40px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  user-select: none;
}

.delete-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  font-size: 18px;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  transition: color 0.2s;
}

.delete-btn:active {
  color: #e74c3c;
}

/* Bottom Bar */
.bottom-bar {
  position: fixed;
  bottom: 56px;
  left: 0;
  right: 0;
  z-index: 150;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.06);
  padding: 10px 16px;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 12px;
}

.bottom-summary {
  flex: 1;
  min-width: 0;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  margin-bottom: 2px;
}

.summary-line em {
  font-style: normal;
  font-weight: 600;
  color: #333;
}

.total-price {
  color: #e74c3c !important;
  font-size: 16px;
}

.shipping-line {
  font-size: 12px;
  color: #999;
}

.checkout-btn {
  flex-shrink: 0;
  padding: 12px 28px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity 0.2s;
}

.checkout-btn:active {
  opacity: 0.85;
}

.checkout-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
