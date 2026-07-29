<template>
  <div class="page-container">
    <div class="page-header">
      <h2>🏠 首页配置</h2>
    </div>

    <el-card>
      <div class="config-section">
        <h3>首页推荐配置</h3>
        <el-form :model="homeConfig" label-width="150px">
          <el-form-item label="推荐商品数量">
            <el-input-number v-model="homeConfig.recommendCount" :min="1" :max="20" />
          </el-form-item>
          <el-form-item label="新品展示数量">
            <el-input-number v-model="homeConfig.newCount" :min="1" :max="20" />
          </el-form-item>
          <el-form-item label="热门商品数量">
            <el-input-number v-model="homeConfig.hotCount" :min="1" :max="20" />
          </el-form-item>
          <el-form-item label="首页标题">
            <el-input v-model="homeConfig.title" />
          </el-form-item>
          <el-form-item label="首页副标题">
            <el-input v-model="homeConfig.subtitle" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card>
      <div class="config-section">
        <h3>推荐商品管理</h3>
        <el-table :data="recommendProducts" border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column label="商品名称" width="200">
            <template #default="{ row }">
              <el-link :href="`/mall/product/${row.id}`" target="_blank">{{ row.productName }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100" />
          <el-table-column prop="isRecommend" label="推荐" width="80">
            <template #default="{ row }">
              <el-switch v-model="row.isRecommend" @change="toggleRecommend(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80">
            <template #default="{ row }">
              <el-input-number v-model="row.sortOrder" :min="0" @change="updateSort(row)" />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const homeConfig = ref({
  recommendCount: 8,
  newCount: 8,
  hotCount: 8,
  title: '商城',
  subtitle: '品质生活，尽在商城'
})

const recommendProducts = ref([])

const fetchConfig = async () => {
  try {
    const res = await fetch('/api/c-mall/config/home')
    const data = await res.json()
    if (data.code === 200) {
      homeConfig.value = { ...homeConfig.value, ...data.data }
    }
  } catch (error) {
    console.error('获取配置失败', error)
  }
}

const fetchRecommendProducts = async () => {
  try {
    const res = await fetch('/api/product/list?isRecommend=1')
    const data = await res.json()
    if (data.code === 200) {
      recommendProducts.value = data.data
    }
  } catch (error) {
    console.error('获取推荐商品失败', error)
  }
}

const saveConfig = async () => {
  try {
    const res = await fetch('/api/c-mall/config/home', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(homeConfig.value)
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('保存成功')
    }
  } catch (error) {
    console.error('保存失败', error)
  }
}

const toggleRecommend = async (row) => {
  try {
    const res = await fetch(`/api/product/${row.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ isRecommend: row.isRecommend })
    })
    const data = await res.json()
    if (data.code !== 200) {
      row.isRecommend = !row.isRecommend
    }
  } catch (error) {
    row.isRecommend = !row.isRecommend
    console.error('操作失败', error)
  }
}

const updateSort = async (row) => {
  try {
    await fetch(`/api/product/${row.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sortOrder: row.sortOrder })
    })
  } catch (error) {
    console.error('操作失败', error)
  }
}

onMounted(() => {
  fetchConfig()
  fetchRecommendProducts()
})
</script>
