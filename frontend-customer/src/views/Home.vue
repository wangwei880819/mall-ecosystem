<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'

const router = useRouter()

// ========== 搜索相关 ==========
const searchKeyword = ref('')
const searchFocused = ref(false)
const searchHistory = ref([])
const HIST_KEY = 'mall_search_history'

function loadHistory() {
  try {
    searchHistory.value = JSON.parse(localStorage.getItem(HIST_KEY) || '[]')
  } catch { searchHistory.value = [] }
}

function saveHistory(kw) {
  if (!kw.trim()) return
  const h = searchHistory.value.filter(h => h !== kw)
  h.unshift(kw)
  if (h.length > 10) h.pop()
  searchHistory.value = h
  localStorage.setItem(HIST_KEY, JSON.stringify(h))
}

function clearHistory() {
  searchHistory.value = []
  localStorage.removeItem(HIST_KEY)
}

let searchTimer = null
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    doSearch()
  }, 400)
}

function doSearch() {
  const kw = searchKeyword.value.trim()
  if (kw) saveHistory(kw)
  searchFocused.value = false
  page.value = 0
  products.value = []
  fetchProducts(true)
}

// ========== 分类相关 ==========
const categoryTree = ref([])
const activeCategoryId = ref(null)
const activeCategoryName = ref('')

// 提取所有二级分类作为标签
const categoryTags = computed(() => {
  const tags = []
  for (const parent of categoryTree.value) {
    if (parent.children) {
      for (const child of parent.children) {
        tags.push({ id: child.id, name: child.name, parentId: child.parentId })
      }
    }
  }
  return tags
})

async function fetchCategoryTree() {
  try {
    const res = await httpInstance.get('/product-categories/tree')
    categoryTree.value = res.result || []
  } catch { /* ignore */ }
}

function selectTag(tag) {
  if (tag === null) {
    // 全部
    activeCategoryId.value = null
    activeCategoryName.value = ''
  } else {
    activeCategoryId.value = tag.id
    activeCategoryName.value = tag.name
  }
  page.value = 0
  products.value = []
  fetchProducts(true)
}

// ========== 商品列表相关 ==========
const products = ref([])
const totalProducts = ref(0)
const loading = ref(true)
const initialLoading = ref(true)
const page = ref(0)
const pageSize = 20

async function fetchProducts(reset = false) {
  if (reset) {
    page.value = 0
    products.value = []
  }
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize,
      status: 'ON_SHELF'
    }
    if (activeCategoryName.value) params.category = activeCategoryName.value
    if (searchKeyword.value.trim()) params.keyword = searchKeyword.value.trim()

    const res = await httpInstance.get('/products', { params })
    const data = res.result
    if (data) {
      const list = data.list || []
      if (reset || page.value === 0) {
        products.value = list
      } else {
        products.value = [...products.value, ...list]
      }
      totalProducts.value = data.total || 0
    }
  } catch (e) {
    if (reset || products.value.length === 0) toast('加载失败，请重试')
  } finally {
    loading.value = false
    initialLoading.value = false
  }
}

function loadMore() {
  if (loading.value || products.value.length >= totalProducts.value) return
  page.value++
  fetchProducts(false)
}

function retry() {
  initialLoading.value = true
  fetchProducts(true)
}

// ========== 滚动触底 ==========
function onScroll(e) {
  const el = e.target
  if (el.scrollHeight - el.scrollTop - el.clientHeight < 80) {
    loadMore()
  }
}

// ========== 导航 ==========
function goDetail(id) {
  router.push(`/detail/${id}`)
}

// ========== 生命周期 ==========
onMounted(async () => {
  loadHistory()
  await fetchCategoryTree()
  await fetchProducts(true)
})
</script>

<template>
  <div class="home-page">
    <!-- ===== 搜索区域（固定） ===== -->
    <div class="search-section">
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索商品名称或关键词"
          class="search-input"
          @input="onSearchInput"
          @focus="searchFocused = true"
          @keyup.enter="doSearch"
        />
        <button
          v-if="searchKeyword"
          class="search-clear"
          @click="searchKeyword = ''; doSearch()"
        >×</button>
        <button class="search-btn" @click="doSearch">搜索</button>
      </div>

      <!-- 搜索历史下拉 -->
      <div v-if="searchFocused && searchHistory.length > 0" class="search-dropdown">
        <div class="dropdown-header">
          <span class="dropdown-title">搜索历史</span>
          <span class="dropdown-clear" @click="clearHistory">清空</span>
        </div>
        <div class="dropdown-list">
          <span
            v-for="h in searchHistory"
            :key="h"
            class="history-tag"
            @click="searchKeyword = h; doSearch()"
          >{{ h }}</span>
        </div>
      </div>

      <!-- 点击遮罩关闭下拉 -->
      <div
        v-if="searchFocused"
        class="search-mask"
        @click="searchFocused = false"
      ></div>
    </div>

    <!-- ===== 分类标签栏（固定） ===== -->
    <div class="category-bar-wrapper">
      <div class="category-bar">
        <span
          class="cat-tag"
          :class="{ active: !activeCategoryId }"
          @click="selectTag(null)"
        >全部</span>
        <span
          v-for="tag in categoryTags"
          :key="tag.id"
          class="cat-tag"
          :class="{ active: activeCategoryId === tag.id }"
          @click="selectTag(tag)"
        >{{ tag.name }}</span>
      </div>
    </div>

    <!-- ===== 商品列表（可滚动） ===== -->
    <div class="content-area" @scroll="onScroll">
      <!-- 首次加载中 -->
      <div v-if="initialLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载商品...</p>
      </div>

      <!-- 加载失败 -->
      <div v-else-if="!initialLoading && products.length === 0 && !loading" class="error-state">
        <div class="error-icon">😕</div>
        <p class="error-text">暂无符合条件的商品</p>
        <button class="retry-btn" @click="retry">重新加载</button>
      </div>

      <!-- 商品网格 -->
      <template v-else>
        <div class="product-grid" v-if="products.length > 0">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="goDetail(product.id)"
          >
            <div class="product-img-wrap">
              <img :src="product.picture" :alt="product.name" />
              <div v-if="product.stock !== undefined && product.stock <= 0" class="sold-out-mask">
                <span>已售罄</span>
              </div>
            </div>
            <div class="product-info">
              <p class="product-name">{{ product.name }}</p>
              <div class="product-bottom">
                <span class="product-price">¥{{ product.price }}</span>
                <span class="product-sales" v-if="product.salesCount">已售{{ product.salesCount }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="loading && products.length > 0">
          <div class="mini-spinner"></div>
          <span>加载中...</span>
        </div>
        <div
          class="load-more no-more"
          v-if="!loading && products.length > 0 && products.length >= totalProducts"
        >— 已加载全部商品 —</div>
      </template>

      <!-- 底部留白（tabBar） -->
      <div class="bottom-spacer"></div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6fa;
  overflow: hidden;
}

/* =============================================
   Search Section
   ============================================= */
.search-section {
  position: relative;
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 12px 12px 14px;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 22px;
  padding: 0 14px;
  height: 40px;
}

.search-icon {
  font-size: 16px;
  margin-right: 8px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
  background: transparent;
  min-width: 0;
}

.search-input::placeholder {
  color: #bbb;
}

.search-clear {
  padding: 0 6px;
  border: none;
  background: none;
  font-size: 18px;
  color: #bbb;
  cursor: pointer;
  line-height: 1;
}

.search-btn {
  flex-shrink: 0;
  padding: 6px 18px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  margin-left: 8px;
}

.search-btn:active {
  opacity: 0.85;
}

/* Search Dropdown */
.search-dropdown {
  position: absolute;
  top: 100%;
  left: 12px;
  right: 12px;
  background: #fff;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  z-index: 50;
  padding: 12px 14px;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dropdown-title {
  font-size: 13px;
  color: #999;
}

.dropdown-clear {
  font-size: 12px;
  color: #667eea;
  cursor: pointer;
}

.dropdown-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag {
  display: inline-block;
  padding: 4px 14px;
  background: #f5f6fa;
  border-radius: 14px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.history-tag:active {
  background: #e8e9f5;
}

.search-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 49;
  background: transparent;
}

/* =============================================
   Category Bar
   ============================================= */
.category-bar-wrapper {
  position: relative;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.category-bar {
  display: flex;
  gap: 0;
  overflow-x: auto;
  padding: 10px 12px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.category-bar::-webkit-scrollbar {
  display: none;
}

.cat-tag {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  padding: 6px 16px;
  font-size: 13px;
  border-radius: 16px;
  background: #f5f6fa;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 10px;
  white-space: nowrap;
}

.cat-tag:active {
  transform: scale(0.95);
}

.cat-tag.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-weight: 600;
}

.cat-tag:first-child {
  margin-left: 0;
}

/* =============================================
   Content Area
   ============================================= */
.content-area {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* =============================================
   Product Grid
   ============================================= */
.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 12px;
}

.product-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.15s;
}

.product-card:active {
  transform: scale(0.97);
}

.product-img-wrap {
  width: 100%;
  padding-top: 100%;
  background: #f8f8f8;
  position: relative;
  overflow: hidden;
}

.product-img-wrap img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sold-out-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sold-out-mask span {
  display: inline-block;
  padding: 4px 14px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 13px;
  border-radius: 12px;
}

.product-info {
  padding: 10px 12px 12px;
}

.product-name {
  font-size: 13px;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-bottom {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.product-price {
  font-size: 16px;
  font-weight: 700;
  color: #e74c3c;
}

.product-sales {
  font-size: 11px;
  color: #bbb;
}

/* =============================================
   States
   ============================================= */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100px 20px;
  color: #999;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e0e0e0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
}

.error-icon {
  font-size: 52px;
  margin-bottom: 12px;
  opacity: 0.6;
}

.error-text {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

.retry-btn {
  padding: 10px 36px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 22px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.retry-btn:active {
  opacity: 0.85;
}

/* =============================================
   Load More
   ============================================= */
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  font-size: 13px;
  color: #999;
}

.mini-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #e0e0e0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.no-more {
  color: #ccc;
}

/* =============================================
   Bottom Spacer
   ============================================= */
.bottom-spacer {
  height: 60px;
}
</style>
