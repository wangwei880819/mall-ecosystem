<template>
  <div class="cmall-index">
    <header class="cmall-header">
      <div class="header-top">
        <div class="logo">🛒 商城</div>
        <div class="search-bar">
          <input v-model="searchKeyword" type="text" placeholder="搜索商品" class="search-input" />
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>
        <button class="cart-btn" @click="$router.push('/mall/cart')">
          🛒
          <span class="cart-count" v-if="cartCount > 0">{{ cartCount }}</span>
        </button>
      </div>
      
      <nav class="category-nav">
        <button v-for="cat in categories" :key="cat.id" 
                :class="{ active: currentCategory === cat.id }"
                @click="currentCategory = cat.id">
          {{ cat.name }}
        </button>
      </nav>
    </header>

    <main class="cmall-main">
      <div class="banner-section">
        <div class="banner">
          <div class="banner-content">
            <h2>限时特惠</h2>
            <p>全场满99减20</p>
            <button class="banner-btn">立即抢购</button>
          </div>
        </div>
      </div>

      <div class="products-section">
        <div class="section-header">
          <h2>{{ currentCategoryName }}</h2>
          <div class="sort-options">
            <button :class="{ active: sortType === 'default' }" @click="sortType = 'default'">默认</button>
            <button :class="{ active: sortType === 'price-asc' }" @click="sortType = 'price-asc'">价格升序</button>
            <button :class="{ active: sortType === 'price-desc' }" @click="sortType = 'price-desc'">价格降序</button>
          </div>
        </div>

        <div class="products-grid">
          <div class="product-card" v-for="product in filteredProducts" :key="product.id" @click="$router.push(`/mall/product/${product.id}`)">
            <img :src="product.image || 'https://via.placeholder.com/200'" :alt="product.name" class="product-image" />
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-footer">
              <span class="product-price">¥{{ product.price }}</span>
              <button class="add-cart-btn" @click.stop="addToCart(product)">加入购物车</button>
            </div>
          </div>
        </div>

        <div class="no-results" v-if="filteredProducts.length === 0">
          <p>暂无商品</p>
        </div>
      </div>
    </main>

    <footer class="cmall-footer">
      <div class="footer-nav">
        <button @click="$router.push('/mall')">首页</button>
        <button @click="$router.push('/mall/cart')">购物车</button>
        <button @click="$router.push('/mall/orders')">订单</button>
        <button @click="$router.push('/mall/profile')">我的</button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const searchKeyword = ref('')
const currentCategory = ref(null)
const sortType = ref('default')

const categories = ref([])

const products = ref([])

const cartCount = computed(() => {
  const saved = localStorage.getItem('cart')
  if (!saved) return 0
  const cart = JSON.parse(saved)
  return cart.reduce((sum, item) => sum + item.quantity, 0)
})

const currentCategoryName = computed(() => {
  const cat = categories.value.find(c => c.id === currentCategory.value)
  return cat ? cat.name : '全部商品'
})

const filteredProducts = computed(() => {
  let result = [...products.value]
  
  if (currentCategory.value !== null) {
    result = result.filter(p => p.categoryId === currentCategory.value)
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(p => 
      p.name.toLowerCase().includes(keyword) || 
      p.description.toLowerCase().includes(keyword)
    )
  }
  
  if (sortType.value === 'price-asc') {
    result.sort((a, b) => a.price - b.price)
  } else if (sortType.value === 'price-desc') {
    result.sort((a, b) => b.price - a.price)
  }
  
  return result
})

const handleSearch = () => {
}

const addToCart = (product) => {
  const saved = localStorage.getItem('cart')
  const cart = saved ? JSON.parse(saved) : []
  
  const existing = cart.find(item => item.id === product.id)
  if (existing) {
    existing.quantity++
  } else {
    cart.push({
      id: product.id,
      name: product.name,
      price: product.price,
      image: product.image,
      spec: '默认规格',
      quantity: 1,
      selected: true
    })
  }
  
  localStorage.setItem('cart', JSON.stringify(cart))
  alert('已加入购物车')
}

onMounted(async () => {
  try {
    const result = await fetch('/api/product/list')
      .then(res => res.json())
    if (result.code === 200) {
      products.value = result.data
    }
  } catch (error) {
    console.error('获取商品失败', error)
  }
})
</script>

<style scoped>
.cmall-index {
  min-height: 100vh;
  background: #f5f5f5;
}

.cmall-header {
  background: white;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-top {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  gap: 15px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
}

.search-bar {
  flex: 1;
  display: flex;
  max-width: 400px;
}

.search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px 0 0 20px;
  font-size: 14px;
}

.search-btn {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 0 20px 20px 0;
  cursor: pointer;
}

.cart-btn {
  position: relative;
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
}

.cart-count {
  position: absolute;
  top: -8px;
  right: -10px;
  background: #e74c3c;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
}

.category-nav {
  display: flex;
  padding: 10px 20px;
  gap: 15px;
  overflow-x: auto;
}

.category-nav button {
  padding: 8px 16px;
  background: #f5f5f5;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.category-nav button.active {
  background: #667eea;
  color: white;
}

.cmall-main {
  padding: 20px;
}

.banner-section {
  margin-bottom: 20px;
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 30px;
  color: white;
}

.banner-content h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
}

.banner-content p {
  margin: 0 0 20px 0;
  font-size: 16px;
}

.banner-btn {
  padding: 12px 30px;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}

.products-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
}

.sort-options button {
  padding: 6px 12px;
  background: #f5f5f5;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  margin-left: 5px;
  cursor: pointer;
}

.sort-options button.active {
  background: #667eea;
  color: white;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fafafa;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.product-name {
  margin: 10px;
  font-size: 16px;
  color: #333;
}

.product-desc {
  margin: 0 10px 10px;
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
}

.product-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 18px;
}

.add-cart-btn {
  padding: 6px 15px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 15px;
  font-size: 13px;
  cursor: pointer;
}

.no-results {
  text-align: center;
  padding: 40px;
  color: #999;
}

.cmall-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #eee;
}

.footer-nav {
  display: flex;
}

.footer-nav button {
  flex: 1;
  padding: 15px;
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
}

.footer-nav button.active {
  color: #667eea;
}
</style>