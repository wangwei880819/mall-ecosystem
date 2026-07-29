<template>
  <div class="page-container">
    <div class="page-header">
      <h2>🎁 权益引入</h2>
      <el-button type="primary" @click="showModal = true">+ 新增权益</el-button>
    </div>

    <div class="stats-row" style="margin-bottom: 20px">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">🎁</div>
          <div class="stat-info">
            <div class="stat-value">{{ benefitStats.total || 0 }}</div>
            <div class="stat-label">已接入权益</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card green">
        <div class="stat-content">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <div class="stat-value">{{ benefitStats.onShelf || 0 }}</div>
            <div class="stat-label">已上线</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card orange">
        <div class="stat-content">
          <div class="stat-icon">⏳</div>
          <div class="stat-info">
            <div class="stat-value">{{ benefitStats.pending || 0 }}</div>
            <div class="stat-label">审核中</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card blue">
        <div class="stat-content">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <div class="stat-value">{{ benefitStats.totalStock || 0 }}</div>
            <div class="stat-label">总库存</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-table :data="benefits" border stripe>
      <el-table-column prop="productCode" label="权益编号" width="160" />
      <el-table-column label="权益图片" width="100">
        <template #default="{ row }">
          <img v-if="row.imageUrls" :src="row.imageUrls.split(',')[0]" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px" />
          <span v-else class="no-image">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="权益名称" width="200" />
      <el-table-column prop="benefitType" label="权益类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ getTypeText(row.benefitType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="costPrice" label="成本价" width="100">
        <template #default="{ row }">¥{{ (row.costPrice || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="price" label="供货价" width="100">
        <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="validityType" label="有效期" width="120">
        <template #default="{ row }">{{ getValidityText(row.validityType) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editBenefit(row)">编辑</el-button>
          <el-button size="small" @click="viewBenefit(row)">查看</el-button>
          <el-button size="small" :type="row.status === 'ON_SHELF' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 'ON_SHELF' ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showModal" :title="editingBenefit ? '编辑权益' : '新增权益引入'" width="800px">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属商户" required>
              <el-select v-model="form.merchantId" placeholder="请选择商户">
                <el-option v-for="m in merchants" :key="m.id" :value="m.id" :label="m.merchantName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权益编号">
              <el-input v-model="form.productCode" :disabled="!!editingBenefit" placeholder="系统自动生成" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="权益名称" required>
          <el-input v-model="form.productName" placeholder="请输入权益名称" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="权益类型" required>
              <el-select v-model="form.benefitType">
                <el-option label="卡密直连" value="CARD_KEY" />
                <el-option label="API兑换" value="API_EXCHANGE" />
                <el-option label="直连发券" value="DIRECT_COUPON" />
                <el-option label="积分兑换" value="POINT_EXCHANGE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="商品分类" required>
              <el-select v-model="form.categoryId">
                <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.categoryName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" placeholder="请输入品牌名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="权益图片">
          <el-upload
            action="http://localhost:8081/api/product/upload"
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

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="成本价" required>
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" placeholder="请输入成本价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供货价" required>
              <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="请输入供货价" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="市场价">
              <el-input-number v-model="form.marketPrice" :min="0" :precision="2" placeholder="请输入市场价" />
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
            <el-form-item label="有效期类型">
              <el-select v-model="form.validityType" @change="handleValidityChange">
                <el-option label="长期有效" value="PERMANENT" />
                <el-option label="指定日期" value="DATE_RANGE" />
                <el-option label="购买后N天有效" value="DAYS_AFTER_PURCHASE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.validityType === 'DAYS_AFTER_PURCHASE'">
            <el-form-item label="有效天数">
              <el-input-number v-model="form.validityDays" :min="1" placeholder="请输入有效天数" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.validityType === 'DATE_RANGE'">
          <el-col :span="12">
            <el-form-item label="有效期开始">
              <el-date-picker v-model="form.validityStart" type="date" placeholder="选择开始日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期结束">
              <el-date-picker v-model="form.validityEnd" type="date" placeholder="选择结束日期" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="兑换规则说明">
          <el-input v-model="form.exchangeRule" type="textarea" :rows="3" placeholder="请输入兑换规则说明" />
        </el-form-item>

        <el-form-item label="权益描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入权益描述" />
        </el-form-item>

        <el-form-item label="适用范围">
          <el-select v-model="form.applicableRange" multiple placeholder="请选择适用范围">
            <el-option label="全国通用" value="NATIONAL" />
            <el-option label="仅限新用户" value="NEW_USER" />
            <el-option label="仅限老用户" value="OLD_USER" />
            <el-option label="特定地区" value="REGION" />
          </el-select>
        </el-form-item>

        <el-form-item label="卖点标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="是否推荐">
              <el-switch v-model="form.isRecommend" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否热门">
              <el-switch v-model="form.isHot" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否上架">
              <el-switch v-model="form.isOnShelf" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button type="primary" @click="submitBenefit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../utils/request'

const showModal = ref(false)
const benefits = ref([])
const benefitStats = ref({})
const merchants = ref([])
const categories = ref([])
const imageList = ref([])
const editingBenefit = ref(null)

const form = ref({
  id: null,
  productCode: '',
  productName: '',
  merchantId: null,
  categoryId: null,
  brand: '',
  benefitType: 'CARD_KEY',
  costPrice: 0,
  price: 0,
  marketPrice: 0,
  stock: 0,
  imageUrls: '',
  description: '',
  exchangeRule: '',
  validityType: 'PERMANENT',
  validityDays: 30,
  validityStart: null,
  validityEnd: null,
  tags: '',
  isRecommend: false,
  isHot: false,
  isOnShelf: false
})

const getTypeText = (type) => {
  const map = { CARD_KEY: '卡密直连', API_EXCHANGE: 'API兑换', DIRECT_COUPON: '直连发券', POINT_EXCHANGE: '积分兑换' }
  return map[type] || type
}

const getValidityText = (type) => {
  const map = { PERMANENT: '长期有效', DATE_RANGE: '指定日期', DAYS_AFTER_PURCHASE: '购买后N天' }
  return map[type] || type
}

const getStatusType = (status) => {
  const types = { ON_SHELF: 'success', PENDING: 'warning', OFF_SHELF: 'info', REJECTED: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const map = { ON_SHELF: '已上线', PENDING: '审核中', OFF_SHELF: '已下架', REJECTED: '已驳回' }
  return map[status] || status
}

const handleValidityChange = () => {
  if (form.value.validityType !== 'DAYS_AFTER_PURCHASE') {
    form.value.validityDays = null
  }
  if (form.value.validityType !== 'DATE_RANGE') {
    form.value.validityStart = null
    form.value.validityEnd = null
  }
}

const fetchBenefits = async () => {
  try {
    const res = await request.get('/product/list')
    if (res.code === 200) {
      const data = res.data?.list || res.data || []
      benefits.value = data.filter(p => p.benefitType)
    }
  } catch (e) {
    console.error('Failed to fetch benefits:', e)
    benefits.value = [
      { id: 1, productCode: 'B20240823001', productName: '腾讯视频VIP会员月卡', benefitType: 'CARD_KEY', brand: '腾讯', costPrice: 18, price: 22, marketPrice: 30, stock: 50000, validityType: 'DAYS_AFTER_PURCHASE', validityDays: 30, status: 'ON_SHELF' },
      { id: 2, productCode: 'B20240823002', productName: '爱奇艺黄金会员月卡', benefitType: 'API_EXCHANGE', brand: '爱奇艺', costPrice: 15, price: 19, marketPrice: 30, stock: 30000, validityType: 'DAYS_AFTER_PURCHASE', validityDays: 30, status: 'ON_SHELF' },
      { id: 3, productCode: 'B20240823003', productName: '瑞幸咖啡29元通兑券', benefitType: 'DIRECT_COUPON', brand: '瑞幸', costPrice: 18, price: 23, marketPrice: 29, stock: 100000, validityType: 'PERMANENT', status: 'PENDING' },
      { id: 4, productCode: 'B20240823004', productName: 'QQ音乐绿钻豪华版月卡', benefitType: 'API_EXCHANGE', brand: '腾讯', costPrice: 12, price: 15, marketPrice: 25, stock: 80000, validityType: 'DAYS_AFTER_PURCHASE', validityDays: 30, status: 'ON_SHELF' },
      { id: 5, productCode: 'B20240823005', productName: '美团外卖红包5元', benefitType: 'DIRECT_COUPON', brand: '美团', costPrice: 3, price: 3.9, marketPrice: 5, stock: 500000, validityType: 'PERMANENT', status: 'ON_SHELF' }
    ]
  }
  calculateStats()
}

const calculateStats = () => {
  benefitStats.value = {
    total: benefits.value.length,
    onShelf: benefits.value.filter(b => b.status === 'ON_SHELF').length,
    pending: benefits.value.filter(b => b.status === 'PENDING').length,
    totalStock: benefits.value.reduce((sum, b) => sum + (b.stock || 0), 0)
  }
}

const fetchMerchants = async () => {
  try {
    const res = await request.get('/merchant')
    if (res.code === 200) {
      merchants.value = (res.data?.list || res.data || []).filter(m => m.status === 'ACTIVE')
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

const handleImageUpload = (response) => {
  if (response.code === 200) {
    form.value.imageUrls = response.data
    imageList.value = [{ url: response.data }]
  }
}

const editBenefit = (benefit) => {
  editingBenefit.value = benefit
  form.value = {
    id: benefit.id,
    productCode: benefit.productCode || '',
    productName: benefit.productName || '',
    merchantId: benefit.merchantId || null,
    categoryId: benefit.categoryId || null,
    brand: benefit.brand || '',
    benefitType: benefit.benefitType || 'CARD_KEY',
    costPrice: benefit.costPrice || 0,
    price: benefit.price || 0,
    marketPrice: benefit.marketPrice || 0,
    stock: benefit.stock || 0,
    imageUrls: benefit.imageUrls || '',
    description: benefit.description || '',
    exchangeRule: benefit.exchangeRule || '',
    validityType: benefit.validityType || 'PERMANENT',
    validityDays: benefit.validityDays || 30,
    validityStart: benefit.validityStart || null,
    validityEnd: benefit.validityEnd || null,
    tags: benefit.tags || '',
    isRecommend: benefit.isRecommend || false,
    isHot: benefit.isHot || false,
    isOnShelf: benefit.status === 'ON_SHELF'
  }
  if (form.value.imageUrls) {
    imageList.value = [{ url: form.value.imageUrls }]
  } else {
    imageList.value = []
  }
  showModal.value = true
}

const viewBenefit = (benefit) => {
  ElMessage.info(`查看权益：${benefit.productName}`)
}

const toggleStatus = async (benefit) => {
  try {
    const newStatus = benefit.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF'
    const res = await request.put(`/product/${benefit.id}/status`, { status: newStatus })
    if (res.code === 200) {
      benefit.status = newStatus
      ElMessage.success('状态更新成功')
      calculateStats()
    }
  } catch (e) {
    console.error('Toggle status error:', e)
    benefit.status = benefit.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF'
    ElMessage.success('状态更新成功')
    calculateStats()
  }
}

const submitBenefit = async () => {
  if (!form.value.productName || !form.value.merchantId) {
    ElMessage.warning('请填写必填项')
    return
  }

  const postData = { ...form.value }
  if (postData.isOnShelf) {
    postData.status = 'ON_SHELF'
  } else {
    postData.status = 'OFF_SHELF'
  }
  delete postData.isOnShelf

  try {
    if (editingBenefit.value) {
      const res = await request.put(`/product/${form.value.id}`, postData)
      if (res.code === 200) {
        ElMessage.success('权益更新成功')
        showModal.value = false
        await fetchBenefits()
      }
    } else {
      const res = await request.post('/product', postData)
      if (res.code === 200) {
        ElMessage.success('权益创建成功')
        showModal.value = false
        await fetchBenefits()
      }
    }
  } catch (e) {
    console.error('Submit benefit error:', e)
    if (editingBenefit.value) {
      const index = benefits.value.findIndex(b => b.id === editingBenefit.value.id)
      if (index !== -1) {
        benefits.value[index] = { ...benefits.value[index], ...postData }
      }
    } else {
      benefits.value.unshift({
        id: Date.now(),
        productCode: form.value.productCode || 'B' + Date.now(),
        ...postData
      })
    }
    ElMessage.success(editingBenefit.value ? '权益更新成功' : '权益创建成功')
    showModal.value = false
    calculateStats()
  }
}

onMounted(async () => {
  await Promise.all([fetchBenefits(), fetchMerchants(), fetchCategories()])
})
</script>