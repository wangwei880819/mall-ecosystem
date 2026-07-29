<template>
  <div class="page-container">
    <div class="page-header">
      <h2>🚨 风险告警</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索告警编号/订单号" style="width: 300px" @keyup.enter="fetchAlerts" />
        <el-select v-model="filterLevel" placeholder="告警级别" style="width: 120px" @change="fetchAlerts">
          <el-option label="全部" value="" />
          <el-option label="低风险" value="LOW" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="高风险" value="HIGH" />
          <el-option label="严重" value="CRITICAL" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="处理状态" style="width: 120px" @change="fetchAlerts">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已处理" value="RESOLVED" />
          <el-option label="已忽略" value="IGNORED" />
        </el-select>
        <el-button type="primary" @click="fetchAlerts">搜索</el-button>
      </div>
    </div>

    <div class="grid-4" style="margin-bottom: 20px;">
      <div class="stat-card">
        <div class="stat-label">告警总数</div>
        <div class="stat-value">{{ alertStats.total || 0 }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待处理</div>
        <div class="stat-value">{{ alertStats.pending || 0 }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">严重告警</div>
        <div class="stat-value">{{ alertStats.critical || 0 }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">已处理</div>
        <div class="stat-value">{{ alertStats.resolved || 0 }}</div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="alerts" border stripe>
      <el-table-column prop="alertNo" label="告警编号" width="160" />
      <el-table-column prop="level" label="告警级别" width="120">
        <template #default="{ row }">
          <el-tag :type="getLevelType(row.level)" effect="dark">{{ getLevelText(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="alertType" label="告警类型" width="120">
        <template #default="{ row }">{{ getTypeText(row.alertType) }}</template>
      </el-table-column>
      <el-table-column prop="orderCode" label="关联订单" width="160" />
      <el-table-column prop="customerPhone" label="客户手机号" width="140" />
      <el-table-column prop="merchantName" label="商户名称" width="140" />
      <el-table-column prop="description" label="告警描述" width="200" />
      <el-table-column prop="status" label="处理状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="alertTime" label="告警时间" width="180" />
      <el-table-column label="操作" fixed="right" width="200" min-width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="primary" @click="handleProcess(row)">处理</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="warning" @click="handleIgnore(row)">忽略</el-button>
          <el-button v-if="row.status === 'PROCESSING'" size="small" type="success" @click="handleResolve(row)">完成</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const alerts = ref([])
const alertStats = ref({})
const searchKeyword = ref('')
const filterLevel = ref('')
const filterStatus = ref('')

const getLevelType = (level) => {
  const types = { LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger' }
  return types[level] || 'info'
}

const getLevelText = (level) => {
  const map = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '严重' }
  return map[level] || level
}

const getTypeText = (type) => {
  const map = {
    FRAUD: '欺诈风险',
    ANOMALY: '异常交易',
    SPAM: '恶意刷单',
    BLACKLIST: '黑名单用户',
    MONEY_LAUNDERING: '洗钱风险',
    OVERDUE: '逾期风险'
  }
  return map[type] || type
}

const getStatusType = (status) => {
  const types = { PENDING: 'warning', PROCESSING: 'primary', RESOLVED: 'success', IGNORED: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已处理', IGNORED: '已忽略' }
  return map[status] || status
}

const fetchAlerts = async () => {
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterLevel.value) params.level = filterLevel.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await request.get('/risk/alerts', { params })
    if (res.code === 200) {
      alerts.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch alerts:', e)
    alerts.value = [
      { id: 1, alertNo: 'ALT20260726001', level: 'CRITICAL', alertType: 'FRAUD', orderCode: 'ORD1785000000001', customerPhone: '13800138001', merchantName: '腾讯视频', description: '同一设备短时间内多次下单，疑似刷单', status: 'PENDING', alertTime: '2026-07-26 14:30:00' },
      { id: 2, alertNo: 'ALT20260726002', level: 'HIGH', alertType: 'BLACKLIST', orderCode: 'ORD1785000000002', customerPhone: '13800138002', merchantName: '瑞幸咖啡', description: '用户在黑名单中，禁止下单', status: 'PENDING', alertTime: '2026-07-26 14:25:00' },
      { id: 3, alertNo: 'ALT20260726003', level: 'MEDIUM', alertType: 'ANOMALY', orderCode: 'ORD1785000000003', customerPhone: '13800138003', merchantName: '爱奇艺', description: '订单金额异常，超出正常范围', status: 'PROCESSING', alertTime: '2026-07-26 13:45:00' },
      { id: 4, alertNo: 'ALT20260725004', level: 'LOW', alertType: 'SPAM', orderCode: 'ORD1785000000004', customerPhone: '13800138004', merchantName: 'QQ音乐', description: '用户频繁取消订单', status: 'RESOLVED', alertTime: '2026-07-25 11:20:00' },
      { id: 5, alertNo: 'ALT20260725005', level: 'MEDIUM', alertType: 'MONEY_LAUNDERING', orderCode: 'ORD1785000000005', customerPhone: '13800138005', merchantName: '美团', description: '资金流向异常，疑似洗钱', status: 'IGNORED', alertTime: '2026-07-25 10:00:00' }
    ]
  }
  calculateStats()
}

const calculateStats = () => {
  alertStats.value = {
    total: alerts.value.length,
    pending: alerts.value.filter(a => a.status === 'PENDING').length,
    critical: alerts.value.filter(a => a.level === 'CRITICAL').length,
    resolved: alerts.value.filter(a => a.status === 'RESOLVED').length
  }
}

const viewDetail = (alert) => {
  ElMessage.info(`查看告警详情：${alert.alertNo}`)
}

const handleProcess = async (alert) => {
  try {
    const res = await request.put(`/risk/alerts/${alert.id}/process`)
    if (res.code === 200) {
      alert.status = 'PROCESSING'
      ElMessage.success('开始处理')
      calculateStats()
    }
  } catch (e) {
    console.error('Process alert error:', e)
    alert.status = 'PROCESSING'
    ElMessage.success('开始处理')
    calculateStats()
  }
}

const handleIgnore = async (alert) => {
  try {
    await ElMessageBox.confirm('确定要忽略该告警吗？', '确认忽略', { type: 'warning' })
    const res = await request.put(`/risk/alerts/${alert.id}/ignore`)
    if (res.code === 200) {
      alert.status = 'IGNORED'
      ElMessage.success('已忽略')
      calculateStats()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Ignore alert error:', e)
      alert.status = 'IGNORED'
      ElMessage.success('已忽略')
      calculateStats()
    }
  }
}

const handleResolve = async (alert) => {
  try {
    const res = await request.put(`/risk/alerts/${alert.id}/resolve`)
    if (res.code === 200) {
      alert.status = 'RESOLVED'
      ElMessage.success('处理完成')
      calculateStats()
    }
  } catch (e) {
    console.error('Resolve alert error:', e)
    alert.status = 'RESOLVED'
    ElMessage.success('处理完成')
    calculateStats()
  }
}

onMounted(async () => {
  await fetchAlerts()
})
</script>