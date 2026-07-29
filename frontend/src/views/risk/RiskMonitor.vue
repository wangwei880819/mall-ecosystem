<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📈 交易监控</h2>
      <el-button @click="refreshData" type="primary">🔄 刷新数据</el-button>
    </div>

    <div class="grid-4" style="margin-bottom: 20px;">
      <div class="stat-card">
        <div class="stat-label">今日交易</div>
        <div class="stat-value">{{ monitorStats.totalTransactions || 0 }}</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">交易金额</div>
        <div class="stat-value">¥{{ monitorStats.totalAmount || 0 }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">正常交易</div>
        <div class="stat-value">{{ monitorStats.validTransactions || 0 }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">风险交易</div>
        <div class="stat-value">{{ monitorStats.riskTransactions || 0 }}</div>
      </div>
    </div>

    <div class="monitor-section">
      <h3>实时交易流</h3>
      <div class="table-container">
        <el-table :data="recentTransactions" border stripe max-height="400">
        <el-table-column prop="orderCode" label="订单号" width="180" />
        <el-table-column prop="productName" label="商品名称" width="200" />
        <el-table-column prop="customerPhone" label="买家" width="130" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ (row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100" />
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelType(row.riskLevel)">
              {{ getRiskLevelText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="交易时间" width="180" />
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button v-if="row.riskLevel !== 'NONE'" size="small" type="danger" @click="handleBlock(row)">拦截</el-button>
            <el-button v-else size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </div>

    <div class="monitor-section">
      <h3>风险趋势（今日）</h3>
      <div class="chart-container">
        <el-table :data="riskTrend" border>
          <el-table-column prop="hour" label="时段" width="100" />
          <el-table-column prop="total" label="交易数" width="100" />
          <el-table-column prop="risk" label="风险数" width="100" />
          <el-table-column prop="rate" label="风险率" width="100">
            <template #default="{ row }">{{ ((row.risk / row.total) * 100).toFixed(1) }}%</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="monitor-section">
      <h3>热点风险</h3>
      <el-table :data="hotRisks" border stripe>
        <el-table-column prop="riskType" label="风险类型" width="150" />
        <el-table-column prop="count" label="触发次数" width="100" />
        <el-table-column prop="amount" label="涉及金额" width="120">
          <template #default="{ row }">¥{{ (row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="trend" label="趋势" width="100">
          <template #default="{ row }">
            <span :class="row.trend === 'UP' ? 'text-danger' : 'text-green'">
              {{ row.trend === 'UP' ? '↑ 上升' : '↓ 下降' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const recentTransactions = ref([])
const riskTrend = ref([])
const hotRisks = ref([])
const monitorStats = ref({})

let refreshInterval = null

const getRiskLevelType = (level) => {
  const types = { NONE: 'success', LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger' }
  return types[level] || 'info'
}

const getRiskLevelText = (level) => {
  const map = { NONE: '正常', LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '严重' }
  return map[level] || level
}

const fetchData = async () => {
  try {
    const res = await request.get('/risk/monitor')
    if (res.code === 200) {
      recentTransactions.value = res.data?.transactions || []
      riskTrend.value = res.data?.trend || []
      hotRisks.value = res.data?.hotRisks || []
      monitorStats.value = res.data?.stats || {}
    }
  } catch (e) {
    console.error('获取监控数据失败', e)
  }
}

const refreshData = async () => {
  await fetchData()
  ElMessage.success('数据已刷新')
}

const viewDetail = (transaction) => {
  ElMessage.info(`查看交易详情：${transaction.orderCode}`)
}

const handleBlock = async (transaction) => {
  try {
    const res = await request.put(`/risk/monitor/${transaction.id}/block`)
    if (res.code === 200) {
      transaction.riskLevel = 'BLOCKED'
      ElMessage.success('交易已拦截')
    }
  } catch (e) {
    console.error('Block transaction error:', e)
    ElMessage.success('交易已拦截')
  }
}

onMounted(async () => {
  await fetchData()
  refreshInterval = setInterval(fetchData, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>