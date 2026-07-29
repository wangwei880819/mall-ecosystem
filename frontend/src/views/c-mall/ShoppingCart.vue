<template>
  <div class="cmall-cart">
    <div class="cart-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <h1>购物车</h1>
      <span class="item-count">{{ cartItems.length }}件商品</span>
    </div>

    <div class="cart-content" v-if="cartItems.length > 0">
      <div class="cart-items">
        <div class="cart-item" v-for="item in cartItems" :key="item.id">
          <input type="checkbox" v-model="item.selected" class="item-checkbox" />
          <img :src="item.image" :alt="item.name" class="item-image" />
          <div class="item-info">
            <h3 class="item-name">{{ item.name }}</h3>
            <p class="item-spec">{{ item.spec }}</p>
            <div class="item-footer">
              <span class="item-price">¥{{ item.price }}</span>
              <div class="quantity-control">
                <button @click="decreaseQuantity(item)">-</button>
                <span>{{ item.quantity }}</span>
                <button @click="increaseQuantity(item)">+</button>
              </div>
            </div>
          </div>
          <button class="delete-btn" @click="removeItem(item)">删除</button>
        </div>
      </div>

      <div class="cart-summary">
        <div class="summary-row">
          <span>已选商品</span>
          <span>{{ selectedCount }}件</span>
        </div>
        <div class="summary-row">
          <span>商品金额</span>
          <span>¥{{ totalPrice }}</span>
        </div>
        <div class="summary-row">
          <span>运费</span>
          <span>¥{{ shippingFee }}</span>
        </div>
        <div class="summary-row total">
          <span>合计</span>
          <span>¥{{ finalTotal }}</span>
        </div>
      </div>

      <div class="cart-actions">
        <button class="settle-button" :disabled="selectedCount === 0" @click="handleSettle">
          结算({{ selectedCount }})
        </button>
      </div>
    </div>

    <div class="empty-cart" v-else>
      <div class="empty-icon">🛒</div>
      <p>购物车是空的</p>
      <button class="go-shopping" @click="$router.push('/mall')">去逛逛</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartItems = ref([])

onMounted(() => {
  const saved = localStorage.getItem('cart')
  if (saved) {
    cartItems.value = JSON.parse(saved)
  }
})

watch(cartItems, (newVal) => {
  localStorage.setItem('cart', JSON.stringify(newVal))
}, { deep: true })

const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
})

const totalPrice = computed(() => {
  return cartItems.value.filter(item => item.selected).reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
})

const shippingFee = computed(() => {
  return parseFloat(totalPrice.value) >= 99 ? '0.00' : '10.00'
})

const finalTotal = computed(() => {
  return (parseFloat(totalPrice.value) + parseFloat(shippingFee.value)).toFixed(2)
})

const increaseQuantity = (item) => {
  item.quantity++
}

const decreaseQuantity = (item) => {
  if (item.quantity > 1) {
    item.quantity--
  }
}

const removeItem = (item) => {
  const index = cartItems.value.indexOf(item)
  if (index > -1) {
    cartItems.value.splice(index, 1)
  }
}

const handleSettle = () => {
  const selectedItems = cartItems.value.filter(item => item.selected)
  if (selectedItems.length === 0) {
    alert('请选择商品')
    return
  }
  router.push({ path: '/mall', query: { checkout: 'true' } })
}
</script>

<style scoped>
.cmall-cart {
  min-height: 100vh;
  background: #f5f5f5;
}

.cart-header {
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
  color: #333;
}

.cart-header h1 {
  flex: 1;
  margin: 0;
  font-size: 20px;
}

.item-count {
  color: #667eea;
  font-weight: bold;
}

.cart-content {
  padding: 20px;
}

.cart-items {
  background: white;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-checkbox {
  width: 20px;
  height: 20px;
  margin-right: 15px;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  margin-right: 15px;
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

.item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 18px;
}

.quantity-control {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 20px;
}

.quantity-control button {
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  font-size: 18px;
  cursor: pointer;
}

.quantity-control span {
  min-width: 40px;
  text-align: center;
}

.delete-btn {
  padding: 8px 15px;
  background: #f5f5f5;
  border: none;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
}

.cart-summary {
  background: white;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.summary-row.total {
  font-weight: bold;
  font-size: 18px;
  color: #e74c3c;
  border-top: 1px solid #eee;
  margin-top: 10px;
  padding-top: 15px;
}

.cart-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 15px 20px;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.1);
}

.settle-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
}

.settle-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-cart {
  text-align: center;
  padding: 100px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-cart p {
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