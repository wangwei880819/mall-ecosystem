<script setup>
import { ref, computed, onMounted } from 'vue'
import http from '@/utils/http'
import { useMerchantStore } from '@/stores/merchant'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import RichTextEditor from '@/components/RichTextEditor.vue'

const merchantStore = useMerchantStore()
const merchantId = computed(() => merchantStore.merchantInfo?.merchant?.id || '')
const selectedCategoryName = computed(() => {
  const cat = categories.value.find(c => (c.id || c.categoryId) === productForm.value.categoryId)
  return cat ? (cat.categoryName || cat.name || '') : ''
})
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

// 权益引入相关
const benefitDialogVisible = ref(false)
const benefitTab = ref('basic')
const benefitSubmitting = ref(false)
const benefitImageFiles = ref([])
const benefitForm = ref({
  benefitName: '',
  benefitType: 'MEMBERSHIP',
  faceValue: 0,
  price: 0,
  settlePrice: 0,
  validityType: 'DAYS_AFTER_RECEIVE',
  validityStart: '',
  validityEnd: '',
  validityDays: 30,
  usageRules: '',
  applicableScope: '',
  exchangeMethod: 'AUTO_BIND',
  stockTotal: 0,
  stockDailyLimit: 0,
  stockPerUser: 0,
  supplierName: '',
  supplierContact: '',
  refundPolicy: 'NO_REFUND',
  imageUrl: '',
  detailDesc: ''
})

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

// ==================== AI校对 ====================
const proofreading = ref(false)
const showProofreadDialog = ref(false)
const proofreadResult = ref(null)
const proofreadTarget = ref('detail')

const doProofread = async (target = 'detail') => {
  proofreadTarget.value = target
  const content = target === 'description' ? productForm.value.description : productForm.value.detail
  if (!content) { ElMessage.warning(target === 'description' ? '请输入商品描述内容' : '请输入商品详情内容'); return }
  proofreading.value = true
  try {
    // http.js 成功时返回 body.data（已解包），失败时 reject body
    const data = await http.post('/ai/proofread', { content })
    const raw = data?.raw || ''
    try {
      proofreadResult.value = JSON.parse(raw.replace(/```json\s*|```/g, '').trim())
    } catch {
      proofreadResult.value = { optimizedContent: raw, issues: [], summary: '' }
    }
    showProofreadDialog.value = true
  } catch (e) {
    // http.js reject 返回的就是 { code, message } 对象
    ElMessage.error(e?.message || e?.msg || '校对失败')
  } finally {
    proofreading.value = false
  }
}

const fillProofreadResult = () => {
  if (proofreadResult.value?.optimizedContent) {
    if (proofreadTarget.value === 'description') {
      productForm.value.description = proofreadResult.value.optimizedContent
    } else {
      productForm.value.detail = proofreadResult.value.optimizedContent
    }
    showProofreadDialog.value = false
    ElMessage.success('已回填到' + (proofreadTarget.value === 'description' ? '商品描述' : '商品详情'))
  }
}

// ==================== 价格摸排 ====================
const priceResearching = ref(false)
const showPriceResearchDialog = ref(false)
const priceResearchResult = ref(null)

const doPriceResearch = async () => {
  if (!productForm.value.price || productForm.value.price <= 0) {
    ElMessage.warning('请先输入售价')
    return
  }
  priceResearching.value = true
  try {
    // http.js 成功时返回 body.data（已解包），直接就是 AiPriceResearchResult 对象
    const data = await http.post('/ai/price-research', {
      price: productForm.value.price,
      productName: productForm.value.productName,
      category: selectedCategoryName.value
    })
    priceResearchResult.value = data
    showPriceResearchDialog.value = true
  } catch (e) {
    ElMessage.error(e?.message || e?.msg || '价格摸排失败')
  } finally {
    priceResearching.value = false
  }
}

const applySuggestedPrice = () => {
  if (priceResearchResult.value?.suggestedPrice) {
    productForm.value.price = priceResearchResult.value.suggestedPrice
    showPriceResearchDialog.value = false
    ElMessage.success('已套用建议售价 ¥' + priceResearchResult.value.suggestedPrice.toFixed(2))
  }
}

// ==================== 权益引入 ====================

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

function showBenefitDialog() {
  // 自动从登录商户信息填充供应商字段
  const info = merchantStore.merchantInfo
  const merchant = info?.merchant || info || {}
  const merchantName = merchant.merchantName || merchant.name || ''
  const merchantPhone = merchant.contactPhone || merchant.phone || ''
  const merchantEmail = merchant.email || ''
  const contactInfo = [merchantPhone, merchantEmail].filter(Boolean).join(' / ')
  
  benefitForm.value = {
    benefitName: '',
    benefitType: 'MEMBERSHIP',
    faceValue: 0,
    price: 0,
    settlePrice: 0,
    validityType: 'DAYS_AFTER_RECEIVE',
    validityStart: '',
    validityEnd: '',
    validityDays: 30,
    usageRules: '',
    applicableScope: '',
    exchangeMethod: 'AUTO_BIND',
    stockTotal: 0,
    stockDailyLimit: 0,
    stockPerUser: 0,
    supplierName: merchantName,
    supplierContact: contactInfo,
    refundPolicy: 'NO_REFUND',
    imageUrl: '',
    detailDesc: ''
  }
  benefitImageFiles.value = []
  benefitTab.value = 'basic'
  benefitDialogVisible.value = true
}

function onBenefitValidityTypeChange() {
  // 切换有效期类型时清空相关字段
  if (benefitForm.value.validityType !== 'FIXED_DATE') {
    benefitForm.value.validityStart = ''
    benefitForm.value.validityEnd = ''
  }
  if (benefitForm.value.validityType !== 'DAYS_AFTER_RECEIVE') {
    benefitForm.value.validityDays = 30
  }
}

function handleBenefitImageSuccess(response, file, fileList) {
  if (response && response.code === 200) {
    benefitForm.value.imageUrl = response.data
    benefitImageFiles.value = [{ uid: Date.now(), name: '封面图', url: response.data }]
  }
}

function handleBenefitImageRemove(file, fileList) {
  benefitForm.value.imageUrl = ''
  benefitImageFiles.value = []
}

async function handleBenefitCreate() {
  if (!benefitForm.value.benefitName) { ElMessage.warning('请输入权益名称'); return }
  if (!benefitForm.value.benefitType) { ElMessage.warning('请选择权益类型'); return }
  if (!benefitForm.value.price) { ElMessage.warning('请输入售价'); return }

  benefitSubmitting.value = true
  try {
    await http.post('/benefit', {
      ...benefitForm.value,
      merchantId: merchantId.value,
      price: parseFloat(benefitForm.value.price) || 0,
      faceValue: parseFloat(benefitForm.value.faceValue) || 0,
      settlePrice: parseFloat(benefitForm.value.settlePrice) || 0,
      stockTotal: parseInt(benefitForm.value.stockTotal) || 0,
      stockDailyLimit: parseInt(benefitForm.value.stockDailyLimit) || 0,
      stockPerUser: parseInt(benefitForm.value.stockPerUser) || 0,
      validityDays: parseInt(benefitForm.value.validityDays) || 0
    })
    ElMessage.success('权益入驻申请已提交，请等待审核')
    benefitDialogVisible.value = false
  } catch (error) {
    const msg = error?.response?.data?.message || error?.message || '提交失败'
    ElMessage.error(msg)
  } finally {
    benefitSubmitting.value = false
  }
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
      <div style="display:flex;gap:12px">
        <el-button type="primary" size="large" @click="showCreateDialog">
          <el-icon style="margin-right:6px"><Plus /></el-icon>
          申请商品入驻
        </el-button>
        <el-button type="success" size="large" @click="showBenefitDialog">
          <el-icon style="margin-right:6px"><Plus /></el-icon>
          权益引入
        </el-button>
      </div>
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
            <el-button type="warning" size="small" style="margin-top:4px" :loading="proofreading" @click="doProofread('description')">🤖 AI校对</el-button>
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
                <el-input-number v-model="productForm.price" :min="0" :precision="2" style="width:140px" placeholder="0.00" />
                <el-button type="warning" size="small" style="margin-left:8px" :loading="priceResearching" @click="doPriceResearch">💹 价格摸排</el-button>
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
            <el-button type="warning" size="small" style="margin-top:8px" :loading="proofreading" @click="doProofread('detail')">🤖 AI校对</el-button>
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

    <!-- 权益引入弹窗 -->
    <el-dialog v-model="benefitDialogVisible" title="权益引入" width="750px" :close-on-click-modal="false" destroy-on-close>
      <div class="form-steps">
        <div class="step-item" :class="{ active: benefitTab === 'basic' }" @click="benefitTab = 'basic'">
          <span class="step-num">1</span>
          <span class="step-text">基本信息</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item" :class="{ active: benefitTab === 'rule' }" @click="benefitTab = 'rule'">
          <span class="step-num">2</span>
          <span class="step-text">规则配置</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item" :class="{ active: benefitTab === 'stock' }" @click="benefitTab = 'stock'">
          <span class="step-num">3</span>
          <span class="step-text">库存信息</span>
        </div>
      </div>

      <el-form :model="benefitForm" label-width="100px" class="product-form">
        <!-- 第一步：基本信息 -->
        <div v-show="benefitTab === 'basic'" class="form-section">
          <div class="section-title">权益基本信息</div>
          <el-form-item label="权益名称" required>
            <el-input v-model="benefitForm.benefitName" placeholder="请输入权益名称，如：腾讯视频VIP月卡" maxlength="50" show-word-limit />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="权益类型" required>
                <el-select v-model="benefitForm.benefitType" style="width:100%">
                  <el-option label="会员权益" value="MEMBERSHIP" />
                  <el-option label="优惠券/代金券" value="COUPON" />
                  <el-option label="游戏点卡" value="GAME_POINTS" />
                  <el-option label="数字内容" value="DIGITAL_CONTENT" />
                  <el-option label="在线服务" value="SERVICE" />
                  <el-option label="保险/延保" value="INSURANCE" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="面值/原价">
                <el-input-number v-model="benefitForm.faceValue" :min="0" :precision="2" style="width:100%" placeholder="权益面值" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="售价" required>
                <el-input-number v-model="benefitForm.price" :min="0" :precision="2" style="width:100%" placeholder="实际销售价格" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结算价">
                <el-input-number v-model="benefitForm.settlePrice" :min="0" :precision="2" style="width:100%" placeholder="与供应商结算价" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="权益描述">
            <el-input v-model="benefitForm.detailDesc" type="textarea" :rows="3" placeholder="请描述权益的核心内容和价值" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="封面图片">
            <el-upload
              action="/api/product/upload"
              list-type="picture-card"
              :file-list="benefitImageFiles"
              :on-success="handleBenefitImageSuccess"
              :on-remove="handleBenefitImageRemove"
              :limit="1"
            >
              <div>
                <el-icon><Plus /></el-icon>
                <div style="margin-top:6px;font-size:12px">上传</div>
              </div>
            </el-upload>
          </el-form-item>
          <div class="form-nav">
            <el-button type="primary" @click="benefitTab = 'rule'">下一步：规则配置</el-button>
          </div>
        </div>

        <!-- 第二步：规则配置 -->
        <div v-show="benefitTab === 'rule'" class="form-section">
          <div class="section-title">使用规则与有效期</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="有效期类型" required>
                <el-select v-model="benefitForm.validityType" style="width:100%" @change="onBenefitValidityTypeChange">
                  <el-option label="固定日期" value="FIXED_DATE" />
                  <el-option label="领取后N天有效" value="DAYS_AFTER_RECEIVE" />
                  <el-option label="长期有效" value="DURATION" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item v-if="benefitForm.validityType === 'DAYS_AFTER_RECEIVE'" label="有效天数">
                <el-input-number v-model="benefitForm.validityDays" :min="1" style="width:100%" placeholder="领取后有效天数" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row v-if="benefitForm.validityType === 'FIXED_DATE'" :gutter="16">
            <el-col :span="12">
              <el-form-item label="有效期开始">
                <el-date-picker v-model="benefitForm.validityStart" type="datetime" placeholder="选择开始时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="有效期结束">
                <el-date-picker v-model="benefitForm.validityEnd" type="datetime" placeholder="选择结束时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="兑换方式" required>
                <el-select v-model="benefitForm.exchangeMethod" style="width:100%">
                  <el-option label="自动绑定账户" value="AUTO_BIND" />
                  <el-option label="兑换码" value="CODE" />
                  <el-option label="二维码核销" value="QR_CODE" />
                  <el-option label="人工发放" value="MANUAL" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="退款政策">
                <el-select v-model="benefitForm.refundPolicy" style="width:100%">
                  <el-option label="不可退款" value="NO_REFUND" />
                  <el-option label="有条件退款" value="CONDITIONAL" />
                  <el-option label="支持退款" value="FULL_REFUND" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="使用规则">
            <el-input v-model="benefitForm.usageRules" type="textarea" :rows="3" placeholder="如：不可与其他优惠叠加、仅限指定平台使用等" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="适用范围">
            <el-input v-model="benefitForm.applicableScope" type="textarea" :rows="2" placeholder="如：全平台通用 / 仅限XX平台 / 仅限指定品类" maxlength="200" show-word-limit />
          </el-form-item>

          <div class="form-nav">
            <el-button @click="benefitTab = 'basic'">上一步</el-button>
            <el-button type="primary" @click="benefitTab = 'stock'">下一步：库存信息</el-button>
          </div>
        </div>

        <!-- 第三步：库存信息 -->
        <div v-show="benefitTab === 'stock'" class="form-section">
          <div class="section-title">库存与供应商信息</div>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="总库存" required>
                <el-input-number v-model="benefitForm.stockTotal" :min="0" style="width:100%" placeholder="0" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="每日限兑">
                <el-input-number v-model="benefitForm.stockDailyLimit" :min="0" style="width:100%" placeholder="不限" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="每人限兑">
                <el-input-number v-model="benefitForm.stockPerUser" :min="0" style="width:100%" placeholder="不限" />
              </el-form-item>
            </el-col>
          </el-row>
          <div class="price-tips">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                每日限兑和每人限兑设为0表示不限制；总库存为0表示无限库存
              </template>
            </el-alert>
          </div>
          <el-row :gutter="16" style="margin-top:16px">
            <el-col :span="12">
              <el-form-item label="供应商名称">
                <el-input v-model="benefitForm.supplierName" placeholder="请输入供应商名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系方式">
                <el-input v-model="benefitForm.supplierContact" placeholder="供应商电话/邮箱" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-nav">
            <el-button @click="benefitTab = 'rule'">上一步</el-button>
            <el-button type="primary" :loading="benefitSubmitting" @click="handleBenefitCreate">提交权益入驻</el-button>
          </div>
        </div>
      </el-form>
    </el-dialog>

    <!-- AI校对结果弹窗 -->
    <el-dialog v-model="showProofreadDialog" title="🤖 AI校对结果" width="800px" :close-on-click-modal="false">
      <div v-loading="proofreading">
        <template v-if="proofreadResult">
          <div v-if="proofreadResult.issues && proofreadResult.issues.length > 0" style="margin-bottom:16px">
            <el-alert v-for="(issue, idx) in proofreadResult.issues" :key="idx" :title="issue.type" :description="`原文: ${issue.original} → 建议: ${issue.suggestion}（${issue.position}）`" type="warning" show-icon :closable="false" style="margin-bottom:8px" />
          </div>
          <div v-if="proofreadResult.summary" style="margin-bottom:16px">
            <h4>整体评价</h4>
            <p>{{ proofreadResult.summary }}</p>
          </div>
          <div v-if="proofreadResult.optimizedContent" style="background:#f5f7fa;padding:16px;border-radius:8px;margin-bottom:16px">
            <h4>优化后内容</h4>
            <div v-html="proofreadResult.optimizedContent" style="max-height:300px;overflow-y:auto"></div>
          </div>
        </template>
        <el-empty v-else-if="!proofreading" description="暂无校对结果" />
      </div>
      <template #footer>
        <el-button type="primary" @click="fillProofreadResult" :disabled="!proofreadResult?.optimizedContent">一键回填</el-button>
      </template>
    </el-dialog>

    <!-- 价格摸排结果弹窗 -->
    <el-dialog v-model="showPriceResearchDialog" title="💹 价格智能摸排" width="800px" :close-on-click-modal="false">
      <div v-loading="priceResearching">
        <template v-if="priceResearchResult">
          <el-alert
            :type="priceResearchResult.overall === 'REASONABLE' ? 'success' : priceResearchResult.overall === 'HIGH' ? 'warning' : 'info'"
            :closable="false"
            style="margin-bottom:16px"
          >
            <template #title>
              价格评分：{{ priceResearchResult.score }} 分 ·
              {{ priceResearchResult.overall === 'REASONABLE' ? '定价合理' : priceResearchResult.overall === 'HIGH' ? '定价偏高' : '定价偏低' }}
            </template>
          </el-alert>

          <el-row :gutter="16" style="margin-bottom:16px">
            <el-col :span="8">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <div style="font-size:13px;color:#999">我的售价</div>
                  <div style="font-size:26px;font-weight:700;color:#1a237e">¥{{ productForm.price?.toFixed(2) }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <div style="font-size:13px;color:#999">建议售价</div>
                  <div style="font-size:26px;font-weight:700;color:#4caf50">¥{{ priceResearchResult.suggestedPrice?.toFixed(2) }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <div style="font-size:13px;color:#999">建议区间</div>
                  <div style="font-size:18px;font-weight:600;color:#666">¥{{ priceResearchResult.priceLower?.toFixed(2) }} ~ ¥{{ priceResearchResult.priceUpper?.toFixed(2) }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <h4 style="margin:16px 0 8px;color:#333">各平台价格对比</h4>
          <el-table :data="priceResearchResult.competitors" border stripe size="small">
            <el-table-column prop="platform" label="平台" width="120" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column label="平台售价" width="150">
              <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>

          <el-alert type="info" :closable="false" style="margin-top:12px" show-icon>
            <template #title>{{ priceResearchResult.summary }}</template>
          </el-alert>
        </template>
        <el-empty v-else description="暂无摸排结果" />
      </div>
      <template #footer>
        <el-button type="primary" @click="applySuggestedPrice" :disabled="!priceResearchResult?.suggestedPrice">套用建议售价</el-button>
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
