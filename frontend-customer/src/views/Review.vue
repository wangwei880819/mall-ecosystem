<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()

const orderId = computed(() => route.query.orderId)
const orderInfo = ref(null)
const loading = ref(false)
const submitting = ref(false)

// 每个商品对应的评价数据: { productId: { rating: 0, content: '' } }
const reviews = ref({})

async function fetchOrderDetail() {
  if (!orderId.value) {
    toast('订单号不存在')
    router.back()
    return
  }
  loading.value = true
  try {
    const res = await httpInstance.get(`/orders/${orderId.value}`)
    orderInfo.value = res.result
    // 初始化评价数据
    const items = orderInfo.value.items || []
    items.forEach(item => {
      const key = item.productId || item.skuId || item.id
      reviews.value[key] = { rating: 0, content: '' }
    })
  } catch (e) {
    toast('获取订单信息失败')
  } finally {
    loading.value = false
  }
}

function setRating(key, star) {
  if (reviews.value[key]) {
    reviews.value[key].rating = star
  }
}

async function handleSubmit() {
  const items = orderInfo.value.items || []
  // 校验是否所有商品都已评分
  const unreviewed = items.filter(item => {
    const key = item.productId || item.skuId || item.id
    return !reviews.value[key] || reviews.value[key].rating === 0
  })
  if (unreviewed.length > 0) {
    toast('请为所有商品评分')
    return
  }

  submitting.value = true
  try {
    const evaluations = items.map(item => {
      const key = item.productId || item.skuId || item.id
      return httpInstance.post('/evaluations', {
        orderId: orderId.value,
        productId: item.productId || item.skuId || item.id,
        productName: item.productName,
        rating: reviews.value[key].rating,
        content: reviews.value[key].content || ''
      })
    })
    await Promise.all(evaluations)
    toast('评价成功')
    setTimeout(() => router.push('/orders'), 800)
  } catch (e) {
    toast('评价提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<template>
  <div class="review-page">
    <!-- Header -->
    <header class="page-header">
      <button class="back-btn" @click="router.back()">←</button>
      <span class="header-title">商品评价</span>
      <span class="header-right"></span>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <!-- Content -->
    <template v-else-if="orderInfo">
      <!-- Order Info -->
      <div class="order-info card fade-in-up">
        <span class="order-label">订单号</span>
        <span class="order-code">{{ orderInfo.orderCode }}</span>
      </div>

      <!-- Review Items -->
      <div
        v-for="item in (orderInfo.items || [])"
        :key="item.productId || item.skuId || item.id"
        class="review-card card fade-in-up"
      >
        <div class="review-item-header">
          <img :src="item.imageUrl || item.productImage" alt="" class="item-img" />
          <div class="item-info">
            <div class="item-name ellipsis-2">{{ item.productName }}</div>
            <div class="item-price">¥{{ item.price }}</div>
          </div>
        </div>

        <!-- Star Rating -->
        <div class="star-rating">
          <span class="rating-label">评分</span>
          <div class="stars">
            <span
              v-for="star in 5"
              :key="star"
              class="star"
              :class="{ active: star <= (reviews[item.productId || item.skuId || item.id]?.rating || 0) }"
              @click="setRating(item.productId || item.skuId || item.id, star)"
            >★</span>
          </div>
        </div>

        <!-- Review Text -->
        <div class="review-textarea">
          <span class="rating-label">评价</span>
          <textarea
            v-model="reviews[item.productId || item.skuId || item.id].content"
            placeholder="请分享您对商品的看法（选填）"
            rows="3"
            maxlength="500"
          ></textarea>
          <span class="char-count">
            {{ (reviews[item.productId || item.skuId || item.id]?.content || '').length }}/500
          </span>
        </div>
      </div>

      <!-- Submit -->
      <div class="submit-section fade-in-up">
        <button
          class="submit-btn"
          :class="{ disabled: submitting }"
          :disabled="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交评价' }}
        </button>
      </div>
    </template>

    <!-- No Order -->
    <div v-else class="empty-state">
      <div class="empty-icon">📋</div>
      <div class="empty-text">订单信息不存在</div>
      <button class="btn-primary-empty" @click="router.push('/orders')">返回订单</button>
    </div>
  </div>
</template>

<style scoped>
.review-page {
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

/* ========== Order Info ========== */
.order-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
}
.order-label {
  font-size: 13px;
  color: #999;
}
.order-code {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

/* ========== Review Card ========== */
.review-card {
  padding: 16px;
}

.review-item-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
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
.item-price {
  font-size: 15px;
  color: #e74c3c;
  font-weight: 600;
}

/* ========== Star Rating ========== */
.star-rating {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.rating-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  flex-shrink: 0;
}
.stars {
  display: flex;
  gap: 6px;
}
.star {
  font-size: 28px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.15s, transform 0.15s;
  user-select: none;
}
.star.active {
  color: #ffa900;
}
.star:active {
  transform: scale(1.2);
}

/* ========== Review Text ========== */
.review-textarea {
  position: relative;
}
.review-textarea .rating-label {
  display: block;
  margin-bottom: 8px;
}
.review-textarea textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  resize: none;
  outline: none;
  background: #fafafa;
  box-sizing: border-box;
}
.review-textarea textarea:focus {
  border-color: #667eea;
  background: #fff;
}
.review-textarea textarea::placeholder {
  color: #ccc;
}
.char-count {
  position: absolute;
  right: 12px;
  bottom: 8px;
  font-size: 12px;
  color: #ccc;
}

/* ========== Submit ========== */
.submit-section {
  padding: 20px 16px;
}
.submit-btn {
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
.submit-btn:active {
  opacity: 0.85;
}
.submit-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
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

/* ========== Text Ellipsis ========== */
.ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
