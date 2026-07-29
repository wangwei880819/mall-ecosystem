<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📦 商品管理</h2>
      <el-button type="primary" @click="openAddProduct">+ 新增商品</el-button>
    </div>

    <div class="table-container">
    <el-table :data="products" border stripe>
      <el-table-column prop="productCode" label="商品编号" width="160" />
      <el-table-column label="商品图片" width="100">
        <template #default="{ row }">
          <img v-if="row.imageUrls || row.productImage" :src="getFirstImage(row)" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px" />
          <span v-else class="no-image">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" width="200" />
      <el-table-column prop="categoryName" label="商品分类" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.categoryName || row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="price" label="售价" width="100">
        <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="marketPrice" label="市场价" width="100">
        <template #default="{ row }"><span style="text-decoration:line-through;color:#999">¥{{ (row.marketPrice || 0).toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="salesCount" label="销量" width="80" />
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getAuditStatusType(row.status)">{{ getAuditStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="340">
        <template #default="{ row }">
          <el-button size="small" @click="editProduct(row)">编辑</el-button>
          <el-button size="small" @click="viewProduct(row)">查看</el-button>
          <el-button v-if="row.status === 'REJECTED'" size="small" type="warning" @click="resubmitProduct(row)">重新提交</el-button>
          <!-- 上架/下架按钮：只有审核通过(ON_SHELF/OFF_SHELF)状态才显示 -->
          <el-button v-if="row.status === 'ON_SHELF'" size="small" type="info" @click="toggleShelf(row)">下架</el-button>
          <el-button v-else-if="row.status === 'OFF_SHELF'" size="small" type="success" @click="toggleShelf(row)">上架</el-button>
          <!-- 未审核/审核中/已驳回：按钮置灰 -->
          <el-button v-else size="small" type="info" disabled>上架</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog v-model="showModal" :title="editingProduct ? '编辑商品' : '新增商品'" width="700px" :close-on-click-modal="false">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属商户" required>
              <el-select v-model="form.merchantId" placeholder="请选择商户">
                <el-option v-for="m in merchants" :key="m.id" :value="m.id" :label="m.merchantName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编号">
              <el-input v-model="form.productCode" :disabled="!!editingProduct" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品名称" required>
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品分类" required>
              <el-select v-model="form.categoryId" placeholder="请选择分类">
                <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.categoryName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" placeholder="请输入品牌名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品图片">
          <el-upload
            action="/api/product/upload"
            list-type="picture-card"
            :file-list="imageList"
            :on-success="handleImageUpload"
            :on-remove="handleImageRemove"
          >
            <div>
              <el-icon><Plus /></el-icon>
              <div style="margin-top: 6px">上传图片</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品介绍">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品介绍" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" required>
              <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="请输入售价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="市场价">
              <el-input-number v-model="form.marketPrice" :min="0" :precision="2" placeholder="请输入市场价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="会员价">
              <el-input-number v-model="form.vipPrice" :min="0" :precision="2" placeholder="请输入会员价" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="库存" required>
              <el-input-number v-model="form.stock" :min="0" placeholder="请输入库存" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="商品类型">
              <el-select v-model="form.productType">
                <el-option label="实物商品" value="PHYSICAL" />
                <el-option label="虚拟商品" value="VIRTUAL" />
                <el-option label="数字权益" value="DIGITAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品详情">
          <RichTextEditor v-model="form.detail" />
        </el-form-item>

        <el-form-item label="卖点标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="showViewDialog" title="商品详情" width="700px" :close-on-click-modal="false">
      <template v-if="viewProductData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品编号">{{ viewProductData.productCode }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ viewProductData.productName }}</el-descriptions-item>
          <el-descriptions-item label="商品分类">{{ viewProductData.categoryName || viewProductData.category }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ viewProductData.brand || '-' }}</el-descriptions-item>
          <el-descriptions-item label="售价">¥{{ (viewProductData.price || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="市场价">¥{{ (viewProductData.marketPrice || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ viewProductData.stock || 0 }}</el-descriptions-item>
          <el-descriptions-item label="销量">{{ viewProductData.salesCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(viewProductData.status)">{{ getStatusText(viewProductData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ viewProductData.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品介绍" :span="2">{{ viewProductData.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 审核记录 -->
        <div v-if="viewProductData.auditTime || viewProductData.auditor || viewProductData.rejectReason" class="audit-section">
          <h4 style="margin: 16px 0 8px; color: #333;">审核记录</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="审核时间">{{ viewProductData.auditTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审核人">{{ viewProductData.auditor || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="viewProductData.rejectReason" label="审核意见">
              <span style="color: #f56c6c;">{{ viewProductData.rejectReason }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑商品弹窗（含驳回提示） -->
    <el-dialog v-model="showEditWithRejectDialog" :title="resubmitTarget ? '重新提交商品' : '编辑商品'" width="700px" :close-on-click-modal="false">
      <el-alert v-if="resubmitTarget?.rejectReason" type="error" title="驳回原因" :closable="false" style="margin-bottom: 16px">
        <template #default>
          <p style="margin: 0;">{{ resubmitTarget.rejectReason }}</p>
          <p v-if="resubmitTarget.auditTime" style="margin: 4px 0 0; font-size: 12px; color: #909399;">
            审核时间：{{ resubmitTarget.auditTime }} | 审核人：{{ resubmitTarget.auditor || '系统' }}
          </p>
        </template>
      </el-alert>
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属商户" required>
              <el-select v-model="form.merchantId" placeholder="请选择商户">
                <el-option v-for="m in merchants" :key="m.id" :value="m.id" :label="m.merchantName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编号">
              <el-input v-model="form.productCode" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品名称" required>
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品分类" required>
              <el-select v-model="form.categoryId" placeholder="请选择分类">
                <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.categoryName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" placeholder="请输入品牌名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品图片">
          <el-upload
            action="/api/product/upload"
            list-type="picture-card"
            :file-list="imageList"
            :on-success="handleImageUpload"
          >
            <div>
              <el-icon><Plus /></el-icon>
              <div style="margin-top: 6px">上传图片</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品介绍">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品介绍" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" required>
              <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="请输入售价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="市场价">
              <el-input-number v-model="form.marketPrice" :min="0" :precision="2" placeholder="请输入市场价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="会员价">
              <el-input-number v-model="form.vipPrice" :min="0" :precision="2" placeholder="请输入会员价" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="库存" required>
              <el-input-number v-model="form.stock" :min="0" placeholder="请输入库存" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="商品类型">
              <el-select v-model="form.productType">
                <el-option label="实物商品" value="PHYSICAL" />
                <el-option label="虚拟商品" value="VIRTUAL" />
                <el-option label="数字权益" value="DIGITAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品详情">
          <RichTextEditor v-model="form.detail" />
        </el-form-item>

        <el-form-item label="卖点标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" @click="submitResubmit">重新提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import RichTextEditor from '../../components/RichTextEditor.vue'
import request from '../../utils/request'

const showModal = ref(false)
const showViewDialog = ref(false)
const showEditWithRejectDialog = ref(false)
const products = ref([])
const merchants = ref([])
const categories = ref([])
const imageList = ref([])
const editingProduct = ref(null)
const viewProductData = ref(null)
const resubmitTarget = ref(null)

const form = ref({
  id: null,
  productCode: '',
  productName: '',
  merchantId: null,
  categoryId: null,
  brand: '',
  price: 0,
  marketPrice: 0,
  vipPrice: 0,
  stock: 0,
  productImage: '',
  imageUrls: '',
  description: '',
  detail: '',
  productType: 'PHYSICAL',
  isOnShelf: false,
  tags: ''
})

const getStatusType = (status) => {
  const types = { ON_SHELF: 'success', PENDING: 'warning', OFF_SHELF: 'info', REJECTED: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const map = { ON_SHELF: '在售', PENDING: '待上架', OFF_SHELF: '已下架', REJECTED: '已驳回' }
  return map[status] || status
}

const getAuditStatusType = (status) => {
  const types = { PENDING: 'warning', AUDITING: '', APPROVED: 'success', REJECTED: 'danger' }
  // ON_SHELF/OFF_SHELF 说明已通过审核
  if (status === 'ON_SHELF' || status === 'OFF_SHELF') return 'success'
  return types[status] || 'info'
}

const getAuditStatusText = (status) => {
  const map = { PENDING: '待审核', AUDITING: '审核中', APPROVED: '已通过', REJECTED: '已驳回' }
  if (status === 'ON_SHELF' || status === 'OFF_SHELF') return '已通过'
  return map[status] || status
}

// 获取商品首张图片
const getFirstImage = (row) => {
  const urls = row?.imageUrls || row?.productImage
  if (!urls) return ''
  return urls.split(',')[0]
}

const openAddProduct = () => {
  // 清空所有缓存数据
  editingProduct.value = null
  imageList.value = []
  form.value = {
    id: null,
    productCode: '',
    productName: '',
    merchantId: null,
    categoryId: null,
    brand: '',
    price: 0,
    marketPrice: 0,
    vipPrice: 0,
    stock: 0,
    productImage: '',
    imageUrls: '',
    description: '',
    detail: '',
    productType: 'PHYSICAL',
    isOnShelf: false,
    tags: ''
  }
  showModal.value = true
}

const fetchProducts = async () => {
  try {
    const res = await request.get('/product/list')
    if (res.code === 200) {
      products.value = res.data?.list || res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch products:', e)
    products.value = []
  }
}

const fetchMerchants = async () => {
  try {
    const res = await request.get('/merchant')
    if (res.code === 200) {
      merchants.value = (res.data?.list || res.data || []).filter(m => m.onboardingStatus === 'APPROVED')
    }
  } catch (e) {
    console.error('Failed to fetch merchants:', e)
    merchants.value = [
      { id: 1, merchantName: '瑞幸咖啡（中国）有限公司' },
      { id: 2, merchantName: '上海寻梦信息技术有限公司' },
      { id: 3, merchantName: '深圳腾讯计算机系统有限公司' },
      { id: 4, merchantName: '阿里巴巴（中国）有限公司' },
      { id: 5, merchantName: '爱奇艺（北京）科技有限公司' }
    ]
  }
}

const fetchCategories = async () => {
  try {
    const res = await request.get('/product/categories')
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch categories:', e)
    categories.value = [
      { id: 1, categoryName: '视频娱乐' },
      { id: 2, categoryName: '音乐音频' },
      { id: 3, categoryName: '本地生活' },
      { id: 4, categoryName: '电商会员' },
      { id: 5, categoryName: '游戏充值' },
      { id: 6, categoryName: '话费充值' }
    ]
  }
}

const handleImageUpload = (response, file, fileList) => {
  if (response.code === 200) {
    const url = response.data
    // 追加到已有的 imageUrls
    const existing = form.value.imageUrls ? form.value.imageUrls.split(',').filter(u => u) : []
    existing.push(url)
    form.value.imageUrls = existing.join(',')
    // 更新 el-upload 的 file-list 预览，符合 Element Plus 格式要求
    imageList.value = existing.map((u, i) => ({
      uid: Date.now() + i,
      name: `图片${i + 1}`,
      url: u
    }))
  }
}

const handleImageRemove = (file, fileList) => {
  const url = file.url || file.response?.data
  const existing = form.value.imageUrls ? form.value.imageUrls.split(',').filter(u => u && u !== url) : []
  form.value.imageUrls = existing.join(',')
  // 更新 el-upload 的 file-list 预览
  imageList.value = existing.map((u, i) => ({
    uid: Date.now() + i,
    name: `图片${i + 1}`,
    url: u
  }))
}

const editProduct = (product) => {
  editingProduct.value = product
  const imgUrls = product.imageUrls || ''
  form.value = {
    id: product.id,
    productCode: product.productCode || '',
    productName: product.productName || '',
    merchantId: product.merchantId || null,
    categoryId: product.categoryId || null,
    brand: product.brand || '',
    price: product.price || 0,
    marketPrice: product.marketPrice || 0,
    vipPrice: product.vipPrice || 0,
    stock: product.stock || 0,
    productImage: product.productImage || '',
    imageUrls: imgUrls,
    description: product.description || '',
    detail: product.detail || '',
    productType: product.productType || 'PHYSICAL',
    isOnShelf: product.status === 'ON_SHELF',
    tags: product.tags || ''
  }
  // 初始化 imageList，符合 Element Plus 格式要求
  imageList.value = imgUrls ? imgUrls.split(',').filter(u => u).map((u, i) => ({
    uid: Date.now() + i,
    name: `图片${i + 1}`,
    url: u
  })) : []
  showModal.value = true
}

const viewProduct = async (product) => {
  viewProductData.value = product
  showViewDialog.value = true
}

const toggleShelf = async (product) => {
  try {
    const newStatus = product.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF'
    const res = await request.put(`/product/${product.id}`, {
      status: newStatus
    })
    if (res.code === 200) {
      ElMessage.success(newStatus === 'ON_SHELF' ? '上架成功' : '下架成功')
      await fetchProducts()
    }
  } catch (e) {
    console.error('Toggle shelf error:', e)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const resubmitProduct = (product) => {
  resubmitTarget.value = product
  editingProduct.value = product
  const imgUrls = product.imageUrls || ''
  form.value = {
    id: product.id,
    productCode: product.productCode || '',
    productName: product.productName || '',
    merchantId: product.merchantId || null,
    categoryId: product.categoryId || null,
    brand: product.brand || '',
    price: product.price || 0,
    marketPrice: product.marketPrice || 0,
    vipPrice: product.vipPrice || 0,
    stock: product.stock || 0,
    productImage: product.productImage || '',
    imageUrls: imgUrls,
    description: product.description || '',
    detail: product.detail || '',
    productType: product.productType || 'PHYSICAL',
    isOnShelf: false,
    tags: product.tags || ''
  }
  // 初始化 imageList，符合 Element Plus 格式要求
  imageList.value = imgUrls ? imgUrls.split(',').filter(u => u).map((u, i) => ({
    uid: Date.now() + i,
    name: `图片${i + 1}`,
    url: u
  })) : []
  showEditWithRejectDialog.value = true
}

const submitResubmit = async () => {
  if (!form.value.productName || !form.value.merchantId) {
    ElMessage.warning('请填写必填项')
    return
  }
  const postData = { ...form.value, status: 'PENDING' }
  delete postData.isOnShelf

  try {
    const res = await request.put(`/product/${form.value.id}`, postData)
    if (res.code === 200) {
      ElMessage.success('商品已重新提交，等待审核')
      showEditWithRejectDialog.value = false
      resubmitTarget.value = null
      await fetchProducts()
    }
  } catch (e) {
    console.error('Resubmit product error:', e)
    ElMessage.error('重新提交失败，请稍后重试')
  }
}

const submitProduct = async () => {
  if (!form.value.productName || !form.value.merchantId) {
    ElMessage.warning('请填写必填项')
    return
  }

  const postData = { ...form.value }
  delete postData.isOnShelf

  try {
    if (editingProduct.value) {
      const res = await request.put(`/product/${form.value.id}`, postData)
      if (res.code === 200) {
        ElMessage.success('商品更新成功')
        showModal.value = false
        await fetchProducts()
      }
    } else {
      const res = await request.post('/product', postData)
      if (res.code === 200) {
        ElMessage.success('商品已提交，等待审核')
        showModal.value = false
        await fetchProducts()
      }
    }
  } catch (e) {
    console.error('Submit product error:', e)
    ElMessage.error('提交失败，请稍后重试')
  }
}

onMounted(async () => {
  await Promise.all([fetchProducts(), fetchMerchants(), fetchCategories()])
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>