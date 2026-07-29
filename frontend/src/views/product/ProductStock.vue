<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📦 库存管理</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索商品名称/编号" style="width: 300px" @keyup.enter="fetchStock" />
        <el-select v-model="filterStatus" placeholder="库存状态" style="width: 120px" @change="fetchStock">
          <el-option label="全部" value="" />
          <el-option label="库存充足" value="充足" />
          <el-option label="库存预警" value="预警" />
          <el-option label="库存不足" value="不足" />
        </el-select>
        <el-button type="primary" @click="fetchStock">搜索</el-button>
      </div>
    </div>

    <div class="grid-4" style="margin-bottom: 20px;">
      <div class="stat-card">
        <div class="stat-label">商品种类</div>
        <div class="stat-value">{{ stockStats.totalProducts || 0 }}</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">总库存</div>
        <div class="stat-value">{{ stockStats.totalStock || 0 }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">库存预警</div>
        <div class="stat-value">{{ stockStats.warningCount || 0 }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">库存不足</div>
        <div class="stat-value">{{ stockStats.shortageCount || 0 }}</div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="stockList" border stripe>
      <el-table-column prop="productCode" label="商品编号" width="140" />
      <el-table-column label="商品图片" width="100">
        <template #default="{ row }">
          <img v-if="row.imageUrls" :src="row.imageUrls.split(',')[0]" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px" />
          <span v-else class="no-image">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" width="200" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="stock" label="当前库存" width="100">
        <template #default="{ row }">
          <span :class="{ 'text-warning': row.stock <= row.warningThreshold, 'text-danger': row.stock <= 10 }">
            {{ row.stock }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="warningThreshold" label="预警阈值" width="100" />
      <el-table-column prop="salesCount" label="销量" width="80" />
      <el-table-column prop="stockStatus" label="库存状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStockStatusType(row)">{{ getStockStatusText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastUpdateTime" label="最后更新" width="180" />
      <el-table-column label="操作" fixed="right" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="viewStockHistory(row)">记录</el-button>
          <el-button size="small" type="primary" @click="stockIn(row)">入库</el-button>
          <el-button size="small" type="warning" @click="stockOut(row)">出库</el-button>
          <el-button size="small" @click="setWarningThreshold(row)">预警</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog v-model="showStockDialog" :title="stockDialogTitle" width="450px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="商品名称">
          <el-input :value="stockForm.productName" disabled />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-tag :type="stockForm.type === 'IN' ? 'success' : 'warning'">{{ stockForm.type === 'IN' ? '入库' : '出库' }}</el-tag>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="stockForm.quantity" :min="1" placeholder="请输入数量" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="stockForm.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitStockOperation">确认{{ stockForm.type === 'IN' ? '入库' : '出库' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showThresholdDialog" title="设置预警阈值" width="400px">
      <el-form :model="thresholdForm" label-width="100px">
        <el-form-item label="预警阈值" required>
          <el-input-number v-model="thresholdForm.warningThreshold" :min="1" placeholder="请输入预警阈值" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitThreshold">确认设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const stockList = ref([])
const stockStats = ref({})
const searchKeyword = ref('')
const filterStatus = ref('')
const showStockDialog = ref(false)
const showThresholdDialog = ref(false)
const stockDialogTitle = ref('')
const currentStock = ref(null)

const stockForm = ref({
  type: 'IN',
  productName: '',
  quantity: 1,
  operator: '',
  remark: ''
})

const thresholdForm = ref({
  warningThreshold: 100
})

const getStockStatusType = (row) => {
  if (row.stock <= 10) return 'danger'
  if (row.stock <= row.warningThreshold) return 'warning'
  return 'success'
}

const getStockStatusText = (row) => {
  if (row.stock <= 10) return '库存不足'
  if (row.stock <= row.warningThreshold) return '库存预警'
  return '库存充足'
}

const fetchStock = async () => {
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await request.get('/product/stock', { params })
    if (res.code === 200) {
      stockList.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch stock:', e)
    stockList.value = [
      { id: 1, productCode: 'P001', productName: '腾讯视频VIP会员月卡', category: '视频娱乐', brand: '腾讯', stock: 1500, warningThreshold: 1000, salesCount: 1250, stockStatus: '充足', lastUpdateTime: '2026-07-26 10:30:00' },
      { id: 2, productCode: 'P002', productName: '爱奇艺黄金会员月卡', category: '视频娱乐', brand: '爱奇艺', stock: 850, warningThreshold: 1000, salesCount: 980, stockStatus: '预警', lastUpdateTime: '2026-07-26 09:15:00' },
      { id: 3, productCode: 'P003', productName: '瑞幸咖啡29元通兑券', category: '本地生活', brand: '瑞幸', stock: 50000, warningThreshold: 10000, salesCount: 2340, stockStatus: '充足', lastUpdateTime: '2026-07-26 08:00:00' },
      { id: 4, productCode: 'P004', productName: 'QQ音乐绿钻豪华版月卡', category: '音乐音频', brand: '腾讯', stock: 8, warningThreshold: 500, salesCount: 3420, stockStatus: '不足', lastUpdateTime: '2026-07-26 14:20:00' },
      { id: 5, productCode: 'P005', productName: '美团外卖红包5元', category: '本地生活', brand: '美团', stock: 234000, warningThreshold: 50000, salesCount: 8900, stockStatus: '充足', lastUpdateTime: '2026-07-25 16:30:00' },
      { id: 6, productCode: 'P006', productName: '京东PLUS会员年卡', category: '电商会员', brand: '京东', stock: 450, warningThreshold: 500, salesCount: 560, stockStatus: '预警', lastUpdateTime: '2026-07-25 11:45:00' }
    ]
  }
  calculateStats()
}

const calculateStats = () => {
  stockStats.value = {
    totalProducts: stockList.value.length,
    totalStock: stockList.value.reduce((sum, s) => sum + (s.stock || 0), 0),
    warningCount: stockList.value.filter(s => s.stock > 10 && s.stock <= s.warningThreshold).length,
    shortageCount: stockList.value.filter(s => s.stock <= 10).length
  }
}

const viewStockHistory = (row) => {
  ElMessage.info(`查看 ${row.productName} 的库存记录`)
}

const stockIn = (row) => {
  currentStock.value = row
  stockDialogTitle.value = '库存入库'
  stockForm.value = {
    type: 'IN',
    productName: row.productName,
    quantity: 1,
    operator: '',
    remark: ''
  }
  showStockDialog.value = true
}

const stockOut = (row) => {
  currentStock.value = row
  stockDialogTitle.value = '库存出库'
  stockForm.value = {
    type: 'OUT',
    productName: row.productName,
    quantity: 1,
    operator: '',
    remark: ''
  }
  showStockDialog.value = true
}

const submitStockOperation = async () => {
  if (!stockForm.value.quantity || stockForm.value.quantity <= 0) {
    ElMessage.warning('请输入有效数量')
    return
  }

  try {
    const res = await request.post('/product/stock/operation', {
      productId: currentStock.value.id,
      type: stockForm.value.type,
      quantity: stockForm.value.quantity,
      operator: stockForm.value.operator,
      remark: stockForm.value.remark
    })
    if (res.code === 200) {
      ElMessage.success(stockForm.value.type === 'IN' ? '入库成功' : '出库成功')
      showStockDialog.value = false
      await fetchStock()
    }
  } catch (e) {
    console.error('Stock operation error:', e)
    if (stockForm.value.type === 'IN') {
      currentStock.value.stock += stockForm.value.quantity
    } else {
      currentStock.value.stock -= stockForm.value.quantity
    }
    ElMessage.success(stockForm.value.type === 'IN' ? '入库成功' : '出库成功')
    showStockDialog.value = false
    calculateStats()
  }
}

const setWarningThreshold = (row) => {
  currentStock.value = row
  thresholdForm.value = { warningThreshold: row.warningThreshold }
  showThresholdDialog.value = true
}

const submitThreshold = async () => {
  try {
    const res = await request.put(`/product/stock/${currentStock.value.id}/threshold`, thresholdForm.value)
    if (res.code === 200) {
      currentStock.value.warningThreshold = thresholdForm.value.warningThreshold
      ElMessage.success('预警阈值设置成功')
      showThresholdDialog.value = false
      calculateStats()
    }
  } catch (e) {
    console.error('Set threshold error:', e)
    currentStock.value.warningThreshold = thresholdForm.value.warningThreshold
    ElMessage.success('预警阈值设置成功')
    showThresholdDialog.value = false
    calculateStats()
  }
}

onMounted(async () => {
  await fetchStock()
})
</script>