<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMerchantStore } from '@/stores/merchant'
import http from '@/utils/http'
import { ElMessage } from 'element-plus'

const merchantStore = useMerchantStore()
const merchantId = computed(() => merchantStore.merchantInfo?.id || merchantStore.merchantInfo?.merchant?.id || '')

const stats = ref({
  productTotal: 0,
  pendingProducts: 0,
  orderTotal: 0,
  orderAmount: 0
})

const statusDistribution = ref([
  { label: '待审核', value: 0, color: '#fdcb6e' },
  { label: '已上架', value: 0, color: '#00b894' },
  { label: '已下架', value: 0, color: '#b2bec3' },
  { label: '已驳回', value: 0, color: '#e17055' }
])

const recentOrders = ref([])
const merchantName = ref('')
const merchantStatus = ref('')

const statusMap = {
  'PENDING': '待审核',
  'APPROVED': '已通过',
  'REJECTED': '已驳回',
  'REVIEWING': '审核中',
  'ACTIVE': '正常'
}

const orderStatusMap = {
  'CREATED': '待支付',
  'PAID': '已支付',
  'FULFILLED': '已发货',
  'SHIPPED': '已发货',
  'EVALUATED': '已评价',
  'REFUNDED': '已退款',
  'CANCELLED': '已取消'
}

onMounted(async () => {
  try {
    const info = merchantStore.merchantInfo
    if (info) {
      merchantName.value = info.merchantName || info.merchant?.merchantName || ''
      merchantStatus.value = info.onboardingStatus || info.merchant?.onboardingStatus || 'PENDING'
    }

    const [productRes, orderRes] = await Promise.all([
      http.get('/merchant-portal/dashboard', { params: { merchantId: merchantId.value } }),
      http.get('/merchant-portal/orders', { params: { merchantId: merchantId.value } }).catch(() => [])
    ])

    if (productRes) {
      const ps = productRes.productStats || {}
      const os = productRes.orderStats || {}
      stats.value.productTotal = ps.total || 0
      stats.value.pendingProducts = ps.pending || 0
      stats.value.orderTotal = os.total || 0
      stats.value.orderAmount = os.totalAmount || 0

      if (ps.total > 0) {
        statusDistribution.value = [
          { label: '待审核', value: ps.pending || 0, color: '#fdcb6e' },
          { label: '已上架', value: ps.approved || 0, color: '#00b894' },
          { label: '已下架', value: 0, color: '#b2bec3' },
          { label: '已驳回', value: ps.rejected || 0, color: '#e17055' }
        ]
      }
    }

    recentOrders.value = (Array.isArray(orderRes) ? orderRes : (orderRes?.orders || [])).slice(0, 5)
  } catch (error) {
    console.error('加载仪表盘数据失败', error)
  }
})
</script>

<template>
  <div class="page-container">
    <div class="merchant-info-bar">
      <h2>{{ merchantName || '商户仪表盘' }}</h2>
      <el-tag
        :type="merchantStatus === 'APPROVED' ? 'success' : merchantStatus === 'REJECTED' ? 'danger' : 'warning'"
        size="large"
      >
        {{ statusMap[merchantStatus] || merchantStatus || '未知' }}
      </el-tag>
    </div>

    <div class="stat-cards">
      <div class="stat-card purple">
        <div class="stat-label">商品总数</div>
        <div class="stat-value">{{ stats.productTotal }}</div>
        <div class="stat-sub">已入驻商品</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待审核商品</div>
        <div class="stat-value">{{ stats.pendingProducts }}</div>
        <div class="stat-sub">待处理</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">订单总数</div>
        <div class="stat-value">{{ stats.orderTotal }}</div>
        <div class="stat-sub">累计订单</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">订单总额</div>
        <div class="stat-value">¥{{ stats.orderAmount }}</div>
        <div class="stat-sub">累计金额</div>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <h3>商品状态分布</h3>
        </div>
        <div class="progress-grid">
          <div class="progress-item" v-for="item in statusDistribution" :key="item.label">
            <span class="progress-label">{{ item.label }}</span>
            <div class="progress-bar-wrap">
              <el-progress
                :percentage="stats.productTotal > 0 ? Math.round(item.value / stats.productTotal * 100) : 0"
                :color="item.color"
                :stroke-width="16"
              />
            </div>
            <span style="width:30px;text-align:right;font-size:13px;color:#666">{{ item.value }}</span>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3>最近订单</h3>
          <el-button text type="primary" @click="$router.push('/orders')">查看全部</el-button>
        </div>
        <el-table :data="recentOrders" size="small" style="width:100%">
          <el-table-column prop="orderCode" label="订单号" width="160" />
          <el-table-column prop="payAmount" label="金额" width="90">
            <template #default="{ row }">¥{{ row.payAmount }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'PAID' ? '' : row.status === 'FULFILLED' ? 'success' : 'info'">
                {{ orderStatusMap[row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" min-width="160" />
        </el-table>
        <div v-if="recentOrders.length === 0" style="text-align:center;padding:40px;color:#999">暂无订单数据</div>
      </div>
    </div>
  </div>
</template>
