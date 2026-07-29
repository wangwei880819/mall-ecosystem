<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import { useUserStore } from '@/stores/userStore'
import { getDetailAPI } from '@/apis/detail'
import { toast } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const product = ref({})
const loading = ref(true)
const activeImage = ref(0)
const activeTab = ref('detail')
const selectedSpec = ref('')
const selectedSpecIndex = ref(0)
const count = ref(1)
const isFavorited = ref(false)

// 检测是否为权益类/虚拟商品
const isDigitalGoods = computed(() => {
  const keywords = ['会员', '权益', '充值', '视频', '音乐', '数字', '卡密', '激活', '代充']
  const text = (product.value.name || '') + (product.value.desc || '') +
    (product.value.brand?.name || '')
  return keywords.some(k => text.includes(k))
})

// 检查登录状态
const isLogin = computed(() => !!userStore.userInfo?.token)

// 从 product.details.properties 提取规格选项
const specOptions = computed(() => {
  if (!product.value.details?.properties) return []
  const specProp = product.value.details.properties.find(
    p => p.name === '规格' || p.name === 'spec' || p.name === 'Spec'
  )
  if (specProp && specProp.values) {
    return specProp.values.split(',').map(v => v.trim()).filter(Boolean)
  }
  return []
})

// 主图列表
const mainPictures = computed(() => {
  if (product.value.mainPictures && product.value.mainPictures.length > 0) {
    return product.value.mainPictures
  }
  if (product.value.picture) {
    return [product.value.picture]
  }
  return []
})

// 商品属性（排除规格）
const detailProperties = computed(() => {
  if (!product.value.details?.properties) return []
  return product.value.details.properties.filter(
    p => p.name !== '规格' && p.name !== 'spec' && p.name !== 'Spec'
  )
})

const fetchProduct = async () => {
  loading.value = true
  try {
    const res = await getDetailAPI(route.params.id)
    product.value = res.result
    if (specOptions.value.length > 0) {
      selectedSpec.value = specOptions.value[0]
      selectedSpecIndex.value = 0
    } else {
      selectedSpec.value = '默认规格'
      selectedSpecIndex.value = 0
    }
  } catch (e) {
    toast('获取商品信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchProduct())

// 图片切换
const prevImage = () => {
  if (activeImage.value > 0) activeImage.value--
}
const nextImage = () => {
  if (activeImage.value < mainPictures.value.length - 1) activeImage.value++
}

// 规格选择
const selectSpec = (spec, index) => {
  selectedSpec.value = spec
  selectedSpecIndex.value = index
}

// 数量操作
const decreaseCount = () => {
  if (count.value > 1) count.value--
}
const increaseCount = () => {
  count.value++
}

// 切换收藏
const toggleFavorite = () => {
  isFavorited.value = !isFavorited.value
  toast(isFavorited.value ? '已收藏' : '已取消收藏')
}

// 加入购物车
const addToCart = () => {
  const cartItem = {
    id: product.value.id,
    name: product.value.name,
    picture: mainPictures.value[0] || product.value.picture,
    price: product.value.price,
    count: count.value,
    skuId: product.value.id,
    attrsText: selectedSpec.value || '默认',
    selected: true
  }
  if (isDigitalGoods.value) {
    cartItem.isDigital = true
    cartItem.rechargePhone = ''
  }
  cartStore.addCart(cartItem)
  toast('已加入购物车')
}

// 立即购买 - 直接跳转结算页，不加入购物车
const buyNow = () => {
  if (!isLogin.value) {
    router.push(`/login?redirect=/checkout`)
    return
  }
  const productData = {
    id: product.value.id,
    name: product.value.name,
    picture: mainPictures.value[0] || product.value.picture,
    price: product.value.price,
    count: count.value,
    skuId: product.value.id,
    attrsText: selectedSpec.value || '默认',
    isDigital: isDigitalGoods.value
  }
  router.push({
    path: '/checkout',
    query: { directBuy: JSON.stringify(productData) }
  })
}
</script>

<template>
  <div class="detail-page">
    <!-- Header -->
    <header class="page-header">
      <button class="back-btn" @click="router.back()">←</button>
      <span class="header-title">商品详情</span>
      <button class="cart-btn" @click="router.push('/cart')">
        🛒
        <span v-if="cartStore.allCount" class="cart-badge">{{ cartStore.allCount }}</span>
      </button>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else-if="product.id">
      <!-- Image Gallery -->
      <div class="image-gallery">
        <div class="image-wrapper">
          <img :src="mainPictures[activeImage]" alt="" class="main-image" />
          <button
            v-if="mainPictures.length > 1 && activeImage > 0"
            class="gallery-arrow left"
            @click="prevImage"
          >‹</button>
          <button
            v-if="mainPictures.length > 1 && activeImage < mainPictures.length - 1"
            class="gallery-arrow right"
            @click="nextImage"
          >›</button>
        </div>
        <div v-if="mainPictures.length > 1" class="gallery-dots">
          <span
            v-for="(_img, i) in mainPictures"
            :key="i"
            class="dot"
            :class="{ active: i === activeImage }"
            @click="activeImage = i"
          ></span>
        </div>
      </div>

      <!-- Info Section -->
      <div class="info-section card">
        <h1 class="product-name">{{ product.name }}</h1>
        <div class="price-row">
          <span class="current-price">¥{{ product.price }}</span>
          <span v-if="product.oldPrice" class="old-price">¥{{ product.oldPrice }}</span>
        </div>
        <div class="meta-row">
          <span v-if="product.salesCount" class="sales">已售 {{ product.salesCount }}</span>
          <span v-if="product.brand?.name" class="brand">品牌: {{ product.brand.name }}</span>
        </div>
        <div v-if="product.merchant?.name" class="merchant-row">
          <span class="merchant-icon">🏪</span>
          <span class="merchant-name">{{ product.merchant.name }}</span>
        </div>
      </div>

      <!-- Spec Selection -->
      <div class="spec-section card">
        <div class="section-label">规格选择</div>
        <div v-if="specOptions.length > 0" class="spec-chips">
          <span
            v-for="(spec, i) in specOptions"
            :key="i"
            class="spec-chip"
            :class="{ active: i === selectedSpecIndex }"
            @click="selectSpec(spec, i)"
          >{{ spec }}</span>
        </div>
        <div v-else class="spec-chips">
          <span class="spec-chip active">默认规格</span>
        </div>
        <div class="selected-tip">已选: {{ selectedSpec }}</div>
      </div>

      <!-- Quantity -->
      <div class="quantity-section card">
        <span class="section-label">数量</span>
        <div class="quantity-stepper">
          <button class="stepper-btn" :class="{ disabled: count <= 1 }" @click="decreaseCount">−</button>
          <span class="stepper-value">{{ count }}</span>
          <button class="stepper-btn" @click="increaseCount">+</button>
        </div>
      </div>

      <!-- Detail Tabs -->
      <div class="detail-section card">
        <div class="detail-tabs">
          <span
            class="tab-item"
            :class="{ active: activeTab === 'detail' }"
            @click="activeTab = 'detail'"
          >商品详情</span>
          <span
            class="tab-item"
            :class="{ active: activeTab === 'review' }"
            @click="activeTab = 'review'"
          >用户评价</span>
        </div>
        <div class="tab-content">
          <template v-if="activeTab === 'detail'">
            <!-- 商品详情（富文本） -->
            <div v-if="product.detail" class="detail-html" v-html="product.detail"></div>
            <ul v-if="detailProperties.length > 0" class="detail-props">
              <li v-for="prop in detailProperties" :key="prop.name">
                <span class="prop-name">{{ prop.name }}</span>
                <span class="prop-value">{{ prop.values }}</span>
              </li>
            </ul>
            <div v-if="product.details?.pictures?.length" class="detail-images">
              <img
                v-for="(img, i) in product.details.pictures"
                :key="i"
                :src="img"
                alt=""
                class="detail-img"
              />
            </div>
            <div v-if="!product.detail && !detailProperties.length && !product.details?.pictures?.length" class="empty-tab">
              暂无详情信息
            </div>
          </template>
          <template v-else>
            <div class="empty-tab">暂无用户评价</div>
          </template>
        </div>
      </div>

      <!-- Bottom spacer for fixed bar -->
      <div class="bottom-spacer"></div>
    </template>

    <!-- Error state -->
    <div v-else class="empty-state">
      <div class="empty-icon">😕</div>
      <div class="empty-text">商品信息加载失败</div>
    </div>

    <!-- Fixed Bottom Bar -->
    <div v-if="!loading && product.id" class="bottom-bar">
      <button class="bar-btn favorite-btn" @click="toggleFavorite">
        <span>{{ isFavorited ? '❤️' : '🤍' }}</span>
        <span class="bar-label">收藏</span>
      </button>
      <button class="bar-btn cart-btn-outline" @click="addToCart">
        <span>🛒</span>
        <span>加入购物车</span>
      </button>
      <button class="bar-btn buy-btn" @click="buyNow">
        立即购买
      </button>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
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
.cart-btn {
  position: relative;
  display: flex;
  align-items: center;
  padding: 6px;
  border: none;
  background: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
}
.cart-badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #e74c3c;
  color: #fff;
  border-radius: 8px;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
  transform: translate(4px, -4px);
}

/* ========== Loading ========== */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  color: #999;
  font-size: 15px;
}

/* ========== Image Gallery ========== */
.image-gallery {
  background: #fff;
}
.image-wrapper {
  position: relative;
  width: 100%;
  height: 375px;
  overflow: hidden;
}
.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.gallery-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 22px;
  line-height: 1;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gallery-arrow.left {
  left: 10px;
}
.gallery-arrow.right {
  right: 10px;
}
.gallery-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 10px 0;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ddd;
  cursor: pointer;
  transition: background 0.2s;
}
.dot.active {
  background: #667eea;
  width: 18px;
  border-radius: 3px;
}

/* ========== Card ========== */
.card {
  background: #fff;
  border-radius: 12px;
  margin: 10px 12px;
  padding: 16px;
  overflow: hidden;
}

/* ========== Info Section ========== */
.product-name {
  font-size: 18px;
  font-weight: 700;
  color: #222;
  line-height: 1.4;
  margin-bottom: 6px;
}
.product-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  line-height: 1.5;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}
.current-price {
  font-size: 24px;
  font-weight: 700;
  color: #e74c3c;
}
.old-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

/* Merchant */
.merchant-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}
.merchant-icon {
  font-size: 14px;
}
.merchant-name {
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
}

/* ========== Spec Section ========== */
.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}
.spec-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}
.spec-chip {
  display: inline-block;
  padding: 6px 16px;
  border: 1px solid #ddd;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.spec-chip.active {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.08);
  color: #667eea;
  font-weight: 600;
}
.selected-tip {
  font-size: 12px;
  color: #999;
}

/* ========== Quantity ========== */
.quantity-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.quantity-stepper {
  display: flex;
  align-items: center;
  gap: 0;
}
.stepper-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #ddd;
  background: #f8f9fb;
  color: #333;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border-radius: 50%;
}
.stepper-btn:active {
  background: #667eea;
  color: #fff;
  border-color: #667eea;
}
.stepper-btn.disabled {
  opacity: 0.4;
  pointer-events: none;
}
.stepper-value {
  min-width: 40px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #222;
  padding: 0 8px;
}

/* ========== Detail Tabs ========== */
.detail-tabs {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-bottom: 16px;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 15px;
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
  background: #667eea;
  border-radius: 2px;
}
.tab-content {
  min-height: 120px;
}
.detail-desc {
  font-size: 14px;
  color: #555;
  line-height: 1.8;
  margin-bottom: 14px;
  white-space: pre-wrap;
}
.detail-html {
  margin-bottom: 14px;
  font-size: 14px;
  color: #333;
  line-height: 1.8;
}
.detail-html :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}
.detail-html :deep(h1),
.detail-html :deep(h2),
.detail-html :deep(h3) {
  margin: 8px 0;
  color: #222;
}
.detail-html :deep(p) {
  margin: 6px 0;
}
.detail-props {
  display: flex;
  flex-wrap: wrap;
}
.detail-props li {
  width: 50%;
  display: flex;
  margin-bottom: 8px;
  font-size: 13px;
}
.prop-name {
  color: #999;
  min-width: 50px;
}
.prop-value {
  color: #666;
  flex: 1;
}
.detail-images {
  margin-top: 8px;
}
.detail-img {
  width: 100%;
  display: block;
  margin-bottom: 4px;
}
.empty-tab {
  text-align: center;
  padding: 30px 0;
  color: #ccc;
  font-size: 14px;
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
  padding: 8px 12px;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid #eee;
  gap: 8px;
}
.bar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: opacity 0.2s;
}
.bar-btn:active {
  opacity: 0.85;
}
.favorite-btn {
  flex-direction: column;
  background: none;
  color: #666;
  font-size: 20px;
  padding: 4px 8px;
  min-width: 48px;
}
.bar-label {
  font-size: 10px;
  font-weight: 400;
  color: #999;
}
.cart-btn-outline {
  flex: 1;
  padding: 12px 0;
  background: #fff;
  color: #667eea;
  border: 1px solid #667eea;
  border-radius: 22px;
  font-size: 14px;
}
.cart-btn-outline:active {
  background: #667eea;
  color: #fff;
}
.buy-btn {
  flex: 1;
  padding: 12px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 22px;
  font-size: 14px;
}
</style>
