<template>
  <div class="page-container">
    <div class="page-header">
      <h2>💳 支付管理</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索订单号/支付流水号" style="width: 300px" @keyup.enter="fetchPayments" />
        <el-select v-model="filterStatus" placeholder="支付状态" style="width: 120px" @change="fetchPayments">
          <el-option label="全部" value="" />
          <el-option label="待支付" value="PENDING" />
          <el-option label="支付成功" value="SUCCESS" />
          <el-option label="支付失败" value="FAILED" />
          <el-option label="已退款" value="REFUNDED" />
        </el-select>
        <el-select v-model="filterMethod" placeholder="支付方式" style="width: 120px" @change="fetchPayments">
          <el-option label="全部" value="" />
          <el-option label="微信支付" value="WECHAT" />
          <el-option label="支付宝" value="ALIPAY" />
          <el-option label="银行卡" value="BANK" />
        </el-select>
        <el-button type="primary" @click="fetchPayments">搜索</el-button>
      </div>
    </div>

    <div class="grid-4" style="margin-bottom: 20px;">
      <div class="stat-card">
        <div class="stat-label">支付订单</div>
        <div class="stat-value">{{ payStats.total || 0 }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">支付成功</div>
        <div class="stat-value">{{ payStats.success || 0 }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待支付</div>
        <div class="stat-value">{{ payStats.pending || 0 }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">支付失败</div>
        <div class="stat-value">{{ payStats.failed || 0 }}</div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="payments" border stripe>
      <el-table-column prop="orderCode" label="订单编号" width="160" />
      <el-table-column prop="payNo" label="支付流水号" width="160" />
      <el-table-column prop="customerPhone" label="买家手机号" width="140" />
      <el-table-column prop="payAmount" label="支付金额" width="120">
        <template #default="{ row }">¥{{ (row.payAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="payMethod" label="支付方式" width="120">
        <template #default="{ row }">{{ getMethodText(row.payMethod) }}</template>
      </el-table-column>
      <el-table-column prop="payChannel" label="支付渠道" width="120" />
      <el-table-column prop="status" label="支付状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" width="180" />
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="primary" @click="handleRetry(row)">重试支付</el-button>
          <el-button v-if="row.status === 'SUCCESS'" size="small" type="warning" @click="handleRefund(row)">申请退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { PAY_METHOD, PAY_STATUS, PAY_STATUS_TYPE } from '../../utils/constants'

const payments = ref([])
const payStats = ref({})
const searchKeyword = ref('')
const filterStatus = ref('')
const filterMethod = ref('')

const getMethodText = (method) => PAY_METHOD[method] || method

const getStatusType = (status) => PAY_STATUS_TYPE[status] || 'info'

const getStatusText = (status) => PAY_STATUS[status] || status

const fetchPayments = async () => {
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterMethod.value) params.method = filterMethod.value
    const res = await request.get('/order/payments', { params })
    if (res.code === 200) {
      payments.value = res.data || []
    }
  } catch (e) {
    console.error('获取支付数据失败', e)
  }
  calculateStats()
}

const calculateStats = () => {
  payStats.value = {
    total: payments.value.length,
    success: payments.value.filter(p => p.status === 'SUCCESS').length,
    pending: payments.value.filter(p => p.status === 'PENDING').length,
    failed: payments.value.filter(p => p.status === 'FAILED').length
  }
}

const viewDetail = (payment) => {
  ElMessage.info(`查看支付详情：${payment.orderCode}`)
}

const handleRetry = (payment) => {
  ElMessage.info(`重试支付：${payment.orderCode}`)
}

const handleRefund = (payment) => {
  ElMessage.info(`申请退款：${payment.orderCode}`)
}

onMounted(async () => {
  await fetchPayments()
})
</script>