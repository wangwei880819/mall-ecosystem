<script setup>
import { ref, computed, onMounted } from 'vue'
import http from '@/utils/http'
import { useMerchantStore } from '@/stores/merchant'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import RichTextEditor from '@/components/RichTextEditor.vue'

const merchantStore = useMerchantStore()
const merchantId = computed(() => merchantStore.merchantInfo?.merchant?.id || '')
const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitting = ref(false)
const statusFilter = ref('')
const activeTab = ref('basic')
const viewProduct = ref(null)

const productForm = ref({
  productName: '',
  categoryId: null,
  category: '',
  brand: '',
  price: 0,
  marketPrice: 0,
  vipPrice: 0,
  stock: 0,
  productType: 'PHYSICAL',
  description: '',
  detail: '',
  imageUrls: '',
  tags: ''
})

const categories = ref([])
const imageFiles = ref([])

// 状态映射
const statusMap = {
  'PENDING': '待审核',
  'AUDITING': '审核中',
  'REJECTED': '已驳回',
  'ON_SHELF': '已上架',
  'OFF_SHELF': '已下架'
}

const statusTagType = {
  'PENDING': 'warning',
  'AUDITING': '',
  'REJECTED': 'danger',
  'ON_SHELF': 'success',
  'OFF_SHELF': 'info'
}

// 审核状态映射（用于"审核状态"列）
const auditStatusMap = {
  'PENDING': '待审核',
  'AUDITING': '审核中',
  'REJECTED': '已驳回',
  'ON_SHELF': '已通过',
  'OFF_SHELF': '已通过'
}

const auditStatusType = {
  'PENDING': 'warning',
  'AUDITING': '',
  'REJECTED': 'danger',
  'ON_SHELF': 'success',
  'OFF_SHELF': 'success'
}

const handleImageSuccess = (response, file, fileList) => {
  if (response && response.code === 200) {
    const url = response.data
    const existing = productForm.value.imageUrls ? productForm.value.imageUrls.split(',').filter(u => u) : []
    existing.push(url)
    productForm.value.imageUrls = existing.join(',')
    imageFiles.value = existing.map((u, i) => ({
      uid: Date.now() + i,
      name: `图片${i + 1}`,
      url: u
    }))
  }
}

const handleImageRemove = (file, fileList) => {
  const url = file.url || file.response?.data
  const existing = productForm.value.imageUrls ? productForm.value.imageUrls.split(',').filter(u => u && u !== url) : []
  productForm.value.imageUrls = existing.join(',')
  imageFiles.value = existing.map((u, i) => ({
    uid: Date.now() + i,
    name: `图片${i + 1}`,
    url: u
  }))
}

async function loadProducts() {
  loading.value = true
  try {
    const params = { merchantId: merchantId.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await http.get('/merchant-portal/products', { params })
    products.value = res || []
  } catch (error) {
    // 静默处理
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await http.get('/product/categories')
    categories.value = res || []
  } catch (error) {
    // 静默处理
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
})

function showCreateDialog() {
  productForm.value = {
    productName: '',
    categoryId: null,
    category: '',
    brand: '',
    price: 0,
    marketPrice: 0,
    vipPrice: 0,
    stock: 0,
    productType: 'PHYSICAL',
    description: '',
    detail: '',
    imageUrls: '',
    tags: ''
  }
  imageFiles.value = []
  activeTab.value = 'basic'
  dialogVisible.value = true
}

function onCategoryChange(catId) {
  const cat = categories.value.find(c => c.id === catId || c.categoryId === catId)
  if (cat) {
    productForm.value.category = cat.categoryName || cat.name || ''
  }
}

async function handleCreate() {
  if (!productForm.value.productName) { ElMessage.warning('请输入商品名称'); return }
  if (!productForm.value.categoryId) { ElMessage.warning('请选择商品分类'); return }
  if (!productForm.value.price) { ElMessage.warning('请输入售价'); return }

  submitting.value = true
  try {
    await http.post('/merchant-portal/products', {
      ...productForm.value,
      merchantId: merchantId.value,
      price: parseFloat(productForm.value.price) || 0,
      marketPrice: parseFloat(productForm.value.marketPrice) || 0,
      vipPrice: parseFloat(productForm.value.vipPrice) || 0,
      stock: parseInt(productForm.value.stock) || 0
    })
    ElMessage.success('商品入驻申请已提交，请等待审核')
    dialogVisible.value = false
    loadProducts()
  } catch (error) {
    const msg = error?.response?.data?.message || error?.message || '提交失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}

function openViewDialog(product) {
  viewProduct.value = product
  viewDialogVisible.value = true
}

function getFirstImage(product) {
  if (product.imageUrls) {
    const urls = product.imageUrls.split(',').filter(u => u)
    return urls[0] || ''
  }
  return ''
}

function formatTime(time) {
  if (!time) return '-'
  return time
}
</script>

<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header-actions">
      <div>
        <h2>我的商品</h2>
        <p class="page-subtitle">管理您的商品列表，提交新品入驻申请</p>
      </div>
      <el-button type="primary" size="large" @click="showCreateDialog">
        <el-icon style="margin-right:6px"><Plus /></el-icon>
        申请商品入驻
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="card">
      <div class="search-bar">
        <span class="search-label">状态筛选：</span>
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:140px" @change="loadProducts">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="审核中" value="AUDITING" />
          <el-option label="已通过" value="ON_SHELF" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已下架" value="OFF_SHELF" />
        </el-select>
      </div>

      <!-- 商品表格 -->
      <div class="table-container">
        <el-table :data="products" v-loading="loading" stripe empty-text="暂无商品，点击上方按钮申请入驻">
          <el-table-column label="商品图片" width="90">
            <template #default="{ row }">
              <img v-if="getFirstImage(row)" :src="getFirstImage(row)" class="product-thumb" />
              <span v-else class="no-image">暂无</span>
            </template>
          </el-table-column>
          <el-table-column prop="productCode" label="商品编号" width="150" />
          <el-table-column prop="productName" label="商品名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="110">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.category || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="售价" width="100" sortable>
            <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80" sortable />
          <el-table-column label="审核状态" width="100">
            <template #default="{ row }">
              <el-tag :type="auditStatusType[row.status] || 'info'" size="small">
                {{ auditStatusMap[row.status] || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="商品状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType[row.status] || 'info'" size="small">
                {{ statusMap[row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openViewDialog(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 申请商品入驻弹窗 -->
    <el-dialog v-model="dialogVisible" title="申请商品入驻" width="750px" :close-on-click-modal="false" destroy-on-close>
      <!-- 步骤指示器 -->
      <div class="form-steps">
        <div class="step-item" :class="{ active: activeTab === 'basic' }" @click="activeTab = 'basic'">
          <span class="step-num">1</span>
          <span class="step-text">基本信息</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item" :class="{ active: activeTab === 'price' }" @click="activeTab = 'price'">
          <span class="step-num">2</span>
          <span class="step-text">价格库存</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item" :class="{ active: activeTab === 'media' }" @click="activeTab = 'media'">
          <span class="step-num">3</span>
          <span class="step-text">图片详情</span>
        </div>
      </div>

      <el-form :model="productForm" label-width="90px" class="product-form">
        <!-- 第一步：基本信息 -->
        <div v-show="activeTab === 'basic'" class="form-section">
          <div class="section-title">基本信息</div>
          <el-form-item label="商品名称" required>
            <el-input v-model="productForm.productName" placeholder="请输入商品名称，建议包含品牌+核心卖点" maxlength="50" show-word-limit />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="商品分类" required>
                <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width:100%" @change="onCategoryChange">
                  <el-option v-for="cat in categories" :key="cat.id || cat.categoryId" :label="cat.categoryName || cat.name" :value="cat.id || cat.categoryId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="品牌">
                <el-input v-model="productForm.brand" placeholder="请输入品牌名称" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="商品类型">
                <el-select v-model="productForm.productType" style="width:100%">
                  <el-option label="实物商品" value="PHYSICAL" />
                  <el-option label="虚拟商品" value="VIRTUAL" />
                  <el-option label="数字权益" value="DIGITAL" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="卖点标签">
                <el-input v-model="productForm.tags" placeholder="多个用逗号分隔" maxlength="100" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="商品描述">
            <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入商品描述，介绍商品核心卖点和特点" maxlength="200" show-word-limit />
          </el-form-item>

          <div class="form-nav">
            <el-button type="primary" @click="activeTab = 'price'">下一步：价格库存</el-button>
          </div>
        </div>

        <!-- 第二步：价格库存 -->
        <div v-show="activeTab === 'price'" class="form-section">
          <div class="section-title">价格与库存</div>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="售价" required>
                <el-input-number v-model="productForm.price" :min="0" :precision="2" style="width:100%" placeholder="0.00" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="市场价">
                <el-input-number v-model="productForm.marketPrice" :min="0" :precision="2" style="width:100%" placeholder="0.00" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="会员价">
                <el-input-number v-model="productForm.vipPrice" :min="0" :precision="2" style="width:100%" placeholder="0.00" />
              </el-form-item>
            </el-col>
          </el-row>
          <div class="price-tips">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                市场价建议为售价的1.2~3倍；会员价应低于售价，建议为售价的80%~95%
              </template>
            </el-alert>
          </div>
          <el-row :gutter="16" style="margin-top:16px">
            <el-col :span="8">
              <el-form-item label="库存" required>
                <el-input-number v-model="productForm.stock" :min="0" style="width:100%" placeholder="0" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-nav">
            <el-button @click="activeTab = 'basic'">上一步</el-button>
            <el-button type="primary" @click="activeTab = 'media'">下一步：图片详情</el-button>
          </div>
        </div>

        <!-- 第三步：图片详情 -->
        <div v-show="activeTab === 'media'" class="form-section">
          <div class="section-title">商品图片</div>
          <el-form-item label="商品图片">
            <el-upload
              action="/api/product/upload"
              list-type="picture-card"
              :file-list="imageFiles"
              :on-success="handleImageSuccess"
              :on-remove="handleImageRemove"
              :limit="5"
            >
              <div>
                <el-icon><Plus /></el-icon>
                <div style="margin-top:6px;font-size:12px">上传图片</div>
              </div>
            </el-upload>
            <div class="upload-hint">支持 JPG/PNG 格式，建议上传多角度展示图，最多5张</div>
          </el-form-item>

          <div class="section-title">商品详情</div>
          <el-form-item label="商品详情">
            <RichTextEditor v-model="productForm.detail" />
          </el-form-item>

          <div class="form-nav">
            <el-button @click="activeTab = 'price'">上一步</el-button>
            <el-button type="primary" :loading="submitting" @click="handleCreate">提交入驻申请</el-button>
          </div>
        </div>
      </el-form>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="商品详情" width="650px" :close-on-click-modal="false">
      <template v-if="viewProduct">
        <div class="view-product-header">
          <img v-if="getFirstImage(viewProduct)" :src="getFirstImage(viewProduct)" class="view-product-img" />
          <div class="view-product-info">
            <h3>{{ viewProduct.productName }}</h3>
            <p class="view-product-code">编号：{{ viewProduct.productCode }}</p>
            <el-tag :type="statusTagType[viewProduct.status] || 'info'" style="margin-top:4px">
              {{ statusMap[viewProduct.status] || viewProduct.status }}
            </el-tag>
          </div>
        </div>
        <el-descriptions :column="2" border style="margin-top:16px">
          <el-descriptions-item label="商品分类">{{ viewProduct.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ viewProduct.brand || '-' }}</el-descriptions-item>
          <el-descriptions-item label="售价">¥{{ (viewProduct.price || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="市场价">¥{{ (viewProduct.marketPrice || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ viewProduct.stock || 0 }}</el-descriptions-item>
          <el-descriptions-item label="销量">{{ viewProduct.salesCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="auditStatusType[viewProduct.status] || 'info'" size="small">
              {{ auditStatusMap[viewProduct.status] || '-' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(viewProduct.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="商品描述" :span="2">{{ viewProduct.description || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="viewProduct.rejectReason" class="reject-section">
          <el-alert type="error" title="驳回原因" :closable="false">
            <template #default>{{ viewProduct.rejectReason }}</template>
          </el-alert>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 1200px;
}

.page-subtitle {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.search-label {
  font-size: 13px;
  color: #888;
}

.product-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}

.no-image {
  color: #ccc;
  font-size: 12px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

/* 表单步骤指示器 */
.form-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
  padding: 0 40px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  opacity: 0.5;
  transition: opacity 0.2s;
}

.step-item.active {
  opacity: 1;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #dcdfe6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  transition: background 0.2s;
}

.step-item.active .step-num {
  background: #6c5ce7;
}

.step-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.step-item.active .step-text {
  color: #6c5ce7;
}

.step-line {
  width: 60px;
  height: 2px;
  background: #e4e7ed;
  margin: 0 12px;
}

/* 表单分区 */
.form-section {
  min-height: 200px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #6c5ce7;
}

.product-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.product-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #555;
}

.price-tips {
  margin-bottom: 8px;
}

.form-nav {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 查看详情 */
.view-product-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.view-product-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

.view-product-info h3 {
  font-size: 18px;
  color: #333;
  margin: 0 0 4px 0;
}

.view-product-code {
  font-size: 13px;
  color: #999;
  margin: 0 0 8px 0;
}

.reject-section {
  margin-top: 16px;
}
</style>
