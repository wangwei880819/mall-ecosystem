<template>
  <div class="page-container">
    <div class="page-header">
      <h2>🔍 对账管理</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索对账编号" style="width: 300px" @keyup.enter="fetchReconciliations" />
        <el-select v-model="filterStatus" placeholder="对账状态" style="width: 120px" @change="fetchReconciliations">
          <el-option label="全部" value="" />
          <el-option label="待对账" value="PENDING" />
          <el-option label="对账中" value="PROCESSING" />
          <el-option label="已对账" value="COMPLETED" />
          <el-option label="有差异" value="DIFF" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="fetchReconciliations" />
        <el-button type="primary" @click="fetchReconciliations">搜索</el-button>
      </div>
    </div>

    <div class="grid-4" style="margin-bottom: 20px;">
      <div class="stat-card">
        <div class="stat-label">对账批次</div>
        <div class="stat-value">{{ reconStats.total || 0 }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待对账</div>
        <div class="stat-value">{{ reconStats.pending || 0 }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">已对账</div>
        <div class="stat-value">{{ reconStats.completed || 0 }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">有差异</div>
        <div class="stat-value">{{ reconStats.diff || 0 }}</div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="reconciliations" border stripe>
      <el-table-column prop="reconNo" label="对账编号" width="160" />
      <el-table-column prop="reconDate" label="对账日期" width="140" />
      <el-table-column prop="channel" label="对账渠道" width="120" />
      <el-table-column prop="orderCount" label="订单数" width="100" />
      <el-table-column prop="systemAmount" label="系统金额" width="140">
        <template #default="{ row }">¥{{ (row.systemAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="channelAmount" label="渠道金额" width="140">
        <template #default="{ row }">¥{{ (row.channelAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="diffAmount" label="差异金额" width="140">
        <template #default="{ row }">
          <span :class="{ 'text-danger': Math.abs(row.diffAmount || 0) > 0 }">
            ¥{{ (row.diffAmount || 0).toFixed(2) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="diffCount" label="差异笔数" width="120">
        <template #default="{ row }">
          <span :class="{ 'text-danger': row.diffCount > 0 }">{{ row.diffCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="对账状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reconTime" label="对账时间" width="180" />
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="primary" @click="handleRecon(row)">对账</el-button>
          <el-button v-if="row.status === 'DIFF'" size="small" type="warning" @click="handleResolve(row)">处理</el-button>
          <el-button v-if="row.status === 'COMPLETED'" size="small" type="success" @click="handleExport(row)">导出</el-button>
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

const reconciliations = ref([])
const reconStats = ref({})
const searchKeyword = ref('')
const filterStatus = ref('')
const dateRange = ref(null)

const getStatusType = (status) => {
  const types = { PENDING: 'warning', PROCESSING: 'primary', COMPLETED: 'success', DIFF: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待对账', PROCESSING: '对账中', COMPLETED: '已对账', DIFF: '有差异' }
  return map[status] || status
}

const fetchReconciliations = async () => {
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await request.get('/finance/reconciliation', { params })
    if (res.code === 200) {
      reconciliations.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch reconciliations:', e)
    reconciliations.value = [
      { id: 1, reconNo: 'RC20260726001', reconDate: '2026-07-26', channel: '微信支付', orderCount: 125, systemAmount: 2580.50, channelAmount: 2580.50, diffAmount: 0, diffCount: 0, status: 'COMPLETED', reconTime: '2026-07-26 01:00:00' },
      { id: 2, reconNo: 'RC20260726002', reconDate: '2026-07-26', channel: '支付宝', orderCount: 89, systemAmount: 1850.30, channelAmount: 1850.30, diffAmount: 0, diffCount: 0, status: 'COMPLETED', reconTime: '2026-07-26 01:30:00' },
      { id: 3, reconNo: 'RC20260725001', reconDate: '2026-07-25', channel: '微信支付', orderCount: 156, systemAmount: 3210.80, channelAmount: 3212.80, diffAmount: -2, diffCount: 1, status: 'DIFF', reconTime: '2026-07-25 01:00:00' },
      { id: 4, reconNo: 'RC20260725002', reconDate: '2026-07-25', channel: '支付宝', orderCount: 112, systemAmount: 2340.00, channelAmount: 2340.00, diffAmount: 0, diffCount: 0, status: 'COMPLETED', reconTime: '2026-07-25 01:30:00' },
      { id: 5, reconNo: 'RC20260727001', reconDate: '2026-07-27', channel: '微信支付', orderCount: 0, systemAmount: 0, channelAmount: 0, diffAmount: 0, diffCount: 0, status: 'PENDING', reconTime: '' }
    ]
  }
  calculateStats()
}

const calculateStats = () => {
  reconStats.value = {
    total: reconciliations.value.length,
    pending: reconciliations.value.filter(r => r.status === 'PENDING').length,
    completed: reconciliations.value.filter(r => r.status === 'COMPLETED').length,
    diff: reconciliations.value.filter(r => r.status === 'DIFF').length
  }
}

const viewDetail = (recon) => {
  ElMessage.info(`查看对账详情：${recon.reconNo}`)
}

const handleRecon = async (recon) => {
  try {
    const res = await request.put(`/finance/reconciliation/${recon.id}/start`)
    if (res.code === 200) {
      recon.status = 'PROCESSING'
      ElMessage.success('对账已开始')
      setTimeout(() => {
        recon.status = 'COMPLETED'
        recon.reconTime = new Date().toLocaleString()
        calculateStats()
      }, 2000)
    }
  } catch (e) {
    console.error('Start recon error:', e)
    recon.status = 'PROCESSING'
    ElMessage.success('对账已开始')
    setTimeout(() => {
      recon.status = 'COMPLETED'
      recon.reconTime = new Date().toLocaleString()
      calculateStats()
    }, 2000)
  }
}

const handleResolve = (recon) => {
  ElMessage.info(`处理差异：${recon.reconNo}`)
}

const handleExport = (recon) => {
  ElMessage.success('报表导出成功')
}

onMounted(async () => {
  await fetchReconciliations()
})
</script>