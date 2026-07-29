<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'

const router = useRouter()

const categories = ref([])
const allProducts = ref([])
const loading = ref(true)

const categoryEmojis = ['📱', '💻', '👗', '🍜', '🏠', '🎮', '📚', '🎵', '⚽', '💄', '🧸', '🎁', '👟', '👜', '🍰', '🚗']

// Group products by category
const groupedProducts = computed(() => {
  const map = {}
  allProducts.value.forEach(p => {
    const catId = p.categoryId || p.cateId || 'other'
    if (!map[catId]) map[catId] = { products: [] }
    map[catId].products.push(p)
  })
  // Fill in category names from categories list
  categories.value.forEach(cat => {
    if (map[cat.id]) {
      map[cat.id].name = cat.name
      map[cat.id].emoji = categoryEmojis[categories.value.indexOf(cat) % categoryEmojis.length]
    }
  })
  // Only return groups that have matching categories and products
  return Object.entries(map)
    .filter(([, group]) => group.name && group.products.length)
    .map(([id, group]) => ({ id, ...group }))
})

async function fetchData() {
  loading.value = true
  try {
    const [catRes, prodRes] = await Promise.all([
      httpInstance.get('/products/categories'),
      httpInstance.get('/products')
    ])
    categories.value = catRes.result || []
    // /products 返回按分类分组的数据，需要从每个分类的goods中提取商品
    const categoryGroups = prodRes.result || []
    allProducts.value = categoryGroups.flatMap(cat => cat.goods || [])
  } catch (e) {
    toast('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/detail/${id}`)
}

function goCategory(id) {
  router.push(`/category/sub/${id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="page-mobile category-page">
    <!-- Header -->
    <header class="page-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <span class="header-title-text">全部分类</span>
      <span class="header-right"></span>
    </header>

    <!-- Category Grid -->
    <div class="category-grid-wrap" v-if="categories.length">
      <div class="category-grid">
        <div
          v-for="(cat, idx) in categories"
          :key="cat.id"
          class="category-item"
          @click="goCategory(cat.id)"
        >
          <span class="cat-emoji">{{ categoryEmojis[idx % categoryEmojis.length] }}</span>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- Products grouped by category -->
    <section
      class="cat-product-section"
      v-for="group in groupedProducts"
      :key="group.id"
    >
      <div class="section-head" @click="goCategory(group.id)">
        <span class="section-head-emoji">{{ group.emoji }}</span>
        <h3 class="section-head-title">{{ group.name }}</h3>
        <span class="section-head-arrow">›</span>
      </div>
      <div class="horizontal-scroll">
        <div
          v-for="product in group.products"
          :key="product.id"
          class="product-card-h"
          @click="goDetail(product.id)"
        >
          <div class="product-img-wrap-h">
            <img :src="product.picture || product.imageUrl || product.mainImage" :alt="product.name" />
          </div>
          <div class="product-info-h">
            <p class="product-name-h ellipsis">{{ product.name }}</p>
            <p class="product-price-h">¥{{ product.price }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Empty State -->
    <div class="empty-state" v-if="!loading && !groupedProducts.length">
      <span class="empty-icon">📂</span>
      <p class="empty-text">暂无分类商品</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-wrap">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
  </div>
</template>

<style scoped>
.category-page {
  position: relative;
}

/* --- Header --- */
.header-title-text {
  font-size: 17px;
  font-weight: 600;
}

/* --- Category Grid (4 cols) --- */
.category-grid-wrap {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px 0 8px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  row-gap: 16px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.15s;
}

.category-item:active {
  transform: scale(0.95);
}

.cat-emoji {
  font-size: 32px;
  line-height: 1;
}

.cat-name {
  font-size: 13px;
  color: #333;
  text-align: center;
}

/* --- Category Product Section --- */
.cat-product-section {
  margin: 0 16px 16px;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 0 10px;
  cursor: pointer;
}

.section-head-emoji {
  font-size: 18px;
}

.section-head-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.section-head-arrow {
  font-size: 20px;
  color: #ccc;
  font-weight: 300;
}

/* --- Horizontal scroll product row --- */
.horizontal-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 4px;
}

.horizontal-scroll::-webkit-scrollbar {
  display: none;
}

.product-card-h {
  flex-shrink: 0;
  width: 140px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}

.product-card-h:active {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: scale(0.97);
}

.product-img-wrap-h {
  width: 140px;
  height: 120px;
  overflow: hidden;
}

.product-img-wrap-h img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.product-info-h {
  padding: 8px 10px 10px;
}

.product-name-h {
  font-size: 13px;
  color: #333;
  line-height: 1.3;
  margin-bottom: 4px;
}

.product-price-h {
  font-size: 15px;
  font-weight: bold;
  color: #e74c3c;
  line-height: 1;
}

/* --- Loading --- */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
  gap: 12px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e8e8e8;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
