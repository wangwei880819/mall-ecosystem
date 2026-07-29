<template>
  <div class="cmall-product-detail">
    <header class="detail-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <h1>商品详情</h1>
      <button class="cart-btn" @click="$router.push('/mall/cart')">🛒</button>
    </header>

    <main class="detail-main" v-if="product">
      <div class="product-images">
        <img :src="product.image || 'https://via.placeholder.com/400'" :alt="product.name" class="main-image" />
      </div>

      <div class="product-info">
        <h2 class="product-name">{{ product.name }}</h2>
        <p class="product-desc">{{ product.description }}</p>
        
        <div class="price-section">
          <span class="current-price">¥{{ product.price }}</span>
          <span class="vip-price" v-if="product.vipPrice">会员价 ¥{{ product.vipPrice }}</span>
        </div>

        <div class="stock-info">
          <span>库存: {{ product.stock || 100 }}件</span>
        </div>

        <div class="spec-section">
          <h3>规格选择</h3>
          <div class="spec-options">
            <button v-for="spec in specOptions" :key="spec" 
                    :class="{ active: selectedSpec === spec }"
                    @click="selectedSpec = spec">
              {{ spec }}
            </button>
          </div>
        </div>

        <div class="quantity-section">
          <span>数量</span>
          <div class="quantity-control">
            <button @click="quantity > 1 && quantity--">-</button>
            <span>{{ quantity }}</span>
            <button @click="quantity < (product.stock || 100) && quantity++">+</button>
          </div>
        </div>
      </div>

      <div class="product-tabs">
        <button :class="{ active: activeTab === 'detail' }" @click="activeTab = 'detail'">商品详情</button>
        <button :class="{ active: activeTab === 'reviews' }" @click="activeTab = 'reviews'">用户评价</button>
      </div>

      <div class="tab-content">
        <div v-if="activeTab === 'detail'" class="detail-content">
          <h3>商品描述</h3>
          <p>{{ product.description }}</p>
          <h3>规格参数</h3>
          <table class="spec-table">
            <tr><td>品牌</td><td>{{ product.brand || '未知品牌' }}</td></tr>
            <tr><td>产地</td><td>{{ product.origin || '中国' }}</td></tr>
            <tr><td>材质</td><td>{{ product.material || '其他' }}</td></tr>
            <tr><td>重量</td><td>{{ product.weight || '未知' }}</td></tr>
          </table>
        </div>
        <div v-if="activeTab === 'reviews'" class="reviews-content">
          <div class="review-item" v-for="review in reviews" :key="review.id">
            <div class="review-header">
              <span class="reviewer">{{ review.nickname }}</span>
              <span class="review-date">{{ review.date }}</span>
            </div>
            <p class="review-content">{{ review.content }}</p>
            <div class="review-rating">
              <span v-for="i in 5" :key="i">⭐</span>
            </div>
          </div>
          <div class="no-reviews" v-if="reviews.length === 0">
            <p>暂无评价</p>
          </div>
        </div>
      </div>
    </main>

    <footer class="detail-footer">
      <div class="footer-actions">
        <button class="action-btn">收藏</button>
        <button class="action-btn" @click="addToCart">加入购物车</button>
        <button class="buy-now" @click="buyNow">立即购买</button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const product = ref(null)
const selectedSpec = ref('默认规格')
const quantity = ref(1)
const activeTab = ref('detail')

const specOptions = []

const reviews = ref([])

onMounted(async () => {
  const productId = route.params.id
  try {
    const result = await fetch(`/api/product/${productId}`)
      .then(res => res.json())
    if (result.code === 200) {
      product.value = result.data
    }
  } catch (error) {
    console.error('获取商品详情失败', error)
  }
})

const addToCart = () => {
  if (!product.value) return
  
  const saved = localStorage.getItem('cart')
  const cart = saved ? JSON.parse(saved) : []
  
  const existing = cart.find(item => item.id === product.value.id && item.spec === selectedSpec.value)
  if (existing) {
    existing.quantity += quantity.value
  } else {
    cart.push({
      id: product.value.id,
      name: product.value.name,
      price: product.value.price,
      image: product.value.image,
      spec: selectedSpec.value,
      quantity: quantity.value,
      selected: true
    })
  }
  
  localStorage.setItem('cart', JSON.stringify(cart))
  alert('已加入购物车')
}

const buyNow = () => {
  addToCart()
  setTimeout(() => {
    window.location.href = '/mall/cart'
  }, 500)
}
</script>

<style scoped>
.cmall-product-detail {
  min-height: 100vh;
  background: #f5f5f5;
}

.detail-header {
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

.detail-header h1 {
  flex: 1;
  margin: 0;
  font-size: 20px;
}

.cart-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
}

.detail-main {
  padding: 20px;
}

.product-images {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.main-image {
  width: 100%;
  max-height: 400px;
  object-fit: contain;
}

.product-info {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #333;
}

.product-desc {
  margin: 0 0 20px 0;
  color: #666;
  line-height: 1.5;
}

.price-section {
  margin-bottom: 15px;
}

.current-price {
  font-size: 32px;
  font-weight: bold;
  color: #e74c3c;
}

.vip-price {
  font-size: 16px;
  color: #667eea;
  margin-left: 15px;
}

.stock-info {
  color: #999;
  margin-bottom: 20px;
}

.spec-section {
  margin-bottom: 20px;
}

.spec-section h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-options button {
  padding: 8px 20px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.spec-options button.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.quantity-control {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 20px;
}

.quantity-control button {
  width: 35px;
  height: 35px;
  border: none;
  background: none;
  font-size: 20px;
  cursor: pointer;
}

.quantity-control span {
  min-width: 40px;
  text-align: center;
}

.product-tabs {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 0 20px;
  margin-bottom: 20px;
}

.product-tabs button {
  flex: 1;
  padding: 15px;
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  position: relative;
}

.product-tabs button.active {
  color: #667eea;
  font-weight: bold;
}

.product-tabs button.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 2px;
  background: #667eea;
}

.tab-content {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 80px;
}

.detail-content h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
}

.detail-content p {
  color: #666;
  line-height: 1.8;
  margin: 0 0 20px 0;
}

.spec-table {
  width: 100%;
  border-collapse: collapse;
}

.spec-table td {
  padding: 10px;
  border-bottom: 1px solid #f5f5f5;
}

.spec-table td:first-child {
  color: #999;
  width: 30%;
}

.reviews-content {
  padding: 10px 0;
}

.review-item {
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.reviewer {
  font-weight: bold;
}

.review-date {
  color: #999;
  font-size: 14px;
}

.review-content {
  color: #666;
  line-height: 1.5;
}

.review-rating {
  margin-top: 10px;
}

.no-reviews {
  text-align: center;
  padding: 40px;
  color: #999;
}

.detail-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 15px 20px;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.1);
}

.footer-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  padding: 14px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}

.buy-now {
  flex: 2;
  padding: 14px;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
}
</style>