<template>
  <div class="settlement-page">
    <h2>💰 结算管理</h2>

    <div class="stats-row">
      <div class="stat-card"><div class="stat-label">本月结算总额</div><div class="stat-value">¥{{ formatAmount(stats.totalAmount) }}</div></div>
      <div class="stat-card green"><div class="stat-label">已结算</div><div class="stat-value">{{ stats.completedCount || 0 }}笔</div></div>
      <div class="stat-card orange"><div class="stat-label">待结算</div><div class="stat-value">{{ stats.pendingCount || 0 }}笔</div></div>
      <div class="stat-card blue"><div class="stat-label">佣金费率</div><div class="stat-value">{{ formatRate(stats.commissionRate) }}%</div></div>
    </div>

    <div class="card">
      <div class="card-header"><h3>结算记录</h3></div>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="settleCode" label="结算编号" width="160" />
        <el-table-column prop="settlePeriod" label="结算周期" width="130" />
        <el-table-column label="结算类型" width="120">
          <template #default="{row}"><el-tag :type="getTypeTag(row.settleType)">{{ getTypeText(row.settleType) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="结算金额" width="140">
          <template #default="{row}">¥{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="itemCount" label="笔数" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{row}"><el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="approver" label="审批人" width="100" />
        <el-table-column prop="approveTime" label="审批时间" width="170">
          <template #default="{row}">{{ row.approveTime ? row.approveTime.substring(0, 16) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{row}">{{ row.createTime ? row.createTime.substring(0, 16) : '-' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '@/utils/http'

const loading = ref(false)
const records = ref([])
const stats = ref({ totalAmount: 0, completedCount: 0, pendingCount: 0, commissionRate: 0.05 })

const formatAmount = (v) => v ? Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00'
const formatRate = (v) => v ? (Number(v) * 100).toFixed(2) : '0.00'

const getTypeTag = (t) => ({ AI_DOU: 'primary', COMMISSION: 'success', EXPANSION: 'warning' }[t] || 'info')
const getTypeText = (t) => ({ AI_DOU: 'AI豆结算', COMMISSION: '佣金结算', EXPANSION: '商拓费' }[t] || t)
const getStatusTag = (s) => ({ PENDING: 'warning', COMPLETED: 'success', PAID: 'primary', REJECTED: 'danger' }[s] || 'info')
const getStatusText = (s) => ({ PENDING: '待审批', COMPLETED: '已完成', PAID: '已打款', REJECTED: '已驳回' }[s] || s)

const fetchData = async () => {
  loading.value = true
  try {
    const merchantId = localStorage.getItem('merchantId') || '1'
    const res = await http.get('/settlement/list', { params: { merchantId, page: 0, size: 100 } })
    records.value = res.list || res || []
    const statsRes = await http.get('/settlement/statistics', { params: { merchantId } })
    stats.value = { ...stats.value, ...(statsRes || {}) }
    const rateRes = await http.get(`/commission/merchant/${merchantId}/rate`)
    if (rateRes) {
      stats.value.commissionRate = rateRes.commissionRate
    }
  } catch (e) {
    console.error('获取结算数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.settlement-page { padding: 20px; }
.settlement-page h2 { margin-bottom: 24px; color: #333; font-size: 24px; font-weight: 600; }
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); text-align: center; }
.stat-card.green { border-left: 3px solid #67c23a; }
.stat-card.orange { border-left: 3px solid #e6a23c; }
.stat-card.blue { border-left: 3px solid #409eff; }
.stat-label { font-size: 13px; color: #999; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: 700; color: #333; }
.card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.card-header { margin-bottom: 16px; }
.card-header h3 { margin: 0; font-size: 16px; color: #333; }
</style>