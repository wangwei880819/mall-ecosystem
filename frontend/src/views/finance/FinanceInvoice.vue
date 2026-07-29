<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📄 发票管理</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索发票号/订单号" style="width: 300px" @keyup.enter="fetchInvoices" />
        <el-select v-model="filterStatus" placeholder="发票状态" style="width: 120px" @change="fetchInvoices">
          <el-option label="全部" value="" />
          <el-option label="待开票" value="PENDING" />
          <el-option label="已开票" value="ISSUED" />
          <el-option label="已推送" value="PUSHED" />
          <el-option label="已作废" value="CANCELLED" />
        </el-select>
        <el-select v-model="filterType" placeholder="发票类型" style="width: 120px" @change="fetchInvoices">
          <el-option label="全部" value="" />
          <el-option label="增值税普通发票" value="NORMAL" />
          <el-option label="增值税专用发票" value="SPECIAL" />
          <el-option label="电子发票" value="ELECTRONIC" />
        </el-select>
        <el-button type="primary" @click="fetchInvoices">搜索</el-button>
      </div>
    </div>

    <div class="grid-4" style="margin-bottom: 20px;">
      <div class="stat-card">
        <div class="stat-label">发票总数</div>
        <div class="stat-value">{{ invoiceStats.total || 0 }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待开票</div>
        <div class="stat-value">{{ invoiceStats.pending || 0 }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">已开票</div>
        <div class="stat-value">{{ invoiceStats.issued || 0 }}</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">开票金额</div>
        <div class="stat-value">¥{{ invoiceStats.totalAmount || 0 }}</div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="invoices" border stripe>
      <el-table-column prop="invoiceNo" label="发票号码" width="160" />
      <el-table-column prop="orderCode" label="订单编号" width="160" />
      <el-table-column prop="customerName" label="客户名称" width="140" />
      <el-table-column prop="customerPhone" label="客户手机号" width="140" />
      <el-table-column prop="invoiceType" label="发票类型" width="120">
        <template #default="{ row }">{{ getTypeText(row.invoiceType) }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="发票金额" width="120">
        <template #default="{ row }">¥{{ (row.amount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="taxAmount" label="税额" width="100">
        <template #default="{ row }">¥{{ (row.taxAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="发票状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="issueTime" label="开票时间" width="180" />
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="primary" @click="handleIssue(row)">开票</el-button>
          <el-button v-if="row.status === 'ISSUED'" size="small" type="success" @click="handlePush(row)">推送</el-button>
          <el-button v-if="row.status === 'ISSUED'" size="small" type="danger" @click="handleCancel(row)">作废</el-button>
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

const invoices = ref([])
const invoiceStats = ref({})
const searchKeyword = ref('')
const filterStatus = ref('')
const filterType = ref('')

const getTypeText = (type) => {
  const map = { NORMAL: '增值税普通发票', SPECIAL: '增值税专用发票', ELECTRONIC: '电子发票' }
  return map[type] || type
}

const getStatusType = (status) => {
  const types = { PENDING: 'warning', ISSUED: 'primary', PUSHED: 'success', CANCELLED: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待开票', ISSUED: '已开票', PUSHED: '已推送', CANCELLED: '已作废' }
  return map[status] || status
}

const fetchInvoices = async () => {
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterType.value) params.type = filterType.value
    const res = await request.get('/finance/invoices', { params })
    if (res.code === 200) {
      invoices.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch invoices:', e)
    invoices.value = [
      { id: 1, invoiceNo: 'FP20260726001', orderCode: 'ORD1785000000001', customerName: '张三', customerPhone: '13800138001', invoiceType: 'ELECTRONIC', amount: 19.90, taxAmount: 1.15, status: 'ISSUED', issueTime: '2026-07-26 11:00:00' },
      { id: 2, invoiceNo: '', orderCode: 'ORD1785000000002', customerName: '李四', customerPhone: '13800138002', invoiceType: 'NORMAL', amount: 39.80, taxAmount: 2.30, status: 'PENDING', issueTime: '' },
      { id: 3, invoiceNo: 'FP20260725003', orderCode: 'ORD1785000000005', customerName: '王五', customerPhone: '13800138005', invoiceType: 'ELECTRONIC', amount: 19.50, taxAmount: 1.13, status: 'PUSHED', issueTime: '2026-07-25 17:30:00' },
      { id: 4, invoiceNo: '', orderCode: 'ORD1785000000004', customerName: '赵六', customerPhone: '13800138004', invoiceType: 'SPECIAL', amount: 45.00, taxAmount: 5.20, status: 'PENDING', issueTime: '' },
      { id: 5, invoiceNo: 'FP20260724005', orderCode: 'ORD1785000000006', customerName: '孙七', customerPhone: '13800138006', invoiceType: 'NORMAL', amount: 59.90, taxAmount: 3.47, status: 'CANCELLED', issueTime: '2026-07-24 10:00:00' }
    ]
  }
  calculateStats()
}

const calculateStats = () => {
  invoiceStats.value = {
    total: invoices.value.length,
    pending: invoices.value.filter(i => i.status === 'PENDING').length,
    issued: invoices.value.filter(i => i.status === 'ISSUED').length,
    totalAmount: invoices.value.reduce((sum, i) => sum + (i.amount || 0), 0).toFixed(2)
  }
}

const viewDetail = (invoice) => {
  ElMessage.info(`查看发票详情：${invoice.invoiceNo || invoice.orderCode}`)
}

const handleIssue = async (invoice) => {
  try {
    const res = await request.put(`/finance/invoices/${invoice.id}/issue`)
    if (res.code === 200) {
      invoice.invoiceNo = 'FP' + Date.now()
      invoice.status = 'ISSUED'
      invoice.issueTime = new Date().toLocaleString()
      ElMessage.success('发票已开具')
      calculateStats()
    }
  } catch (e) {
    console.error('Issue invoice error:', e)
    invoice.invoiceNo = 'FP' + Date.now()
    invoice.status = 'ISSUED'
    invoice.issueTime = new Date().toLocaleString()
    ElMessage.error('开具失败，请稍后重试')
    calculateStats()
  }
}

const handlePush = async (invoice) => {
  try {
    const res = await request.put(`/finance/invoices/${invoice.id}/push`)
    if (res.code === 200) {
      invoice.status = 'PUSHED'
      ElMessage.success('发票已推送')
      calculateStats()
    }
  } catch (e) {
    console.error('Push invoice error:', e)
    invoice.status = 'PUSHED'
    ElMessage.error('推送失败，请稍后重试')
    calculateStats()
  }
}

const handleCancel = async (invoice) => {
  try {
    await ElMessageBox.confirm('确定要作废该发票吗？', '确认作废', { type: 'warning' })
    const res = await request.put(`/finance/invoices/${invoice.id}/cancel`)
    if (res.code === 200) {
      invoice.status = 'CANCELLED'
      ElMessage.success('发票已作废')
      calculateStats()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Cancel invoice error:', e)
      invoice.status = 'CANCELLED'
      ElMessage.error('作废失败，请稍后重试')
      calculateStats()
    }
  }
}

onMounted(async () => {
  await fetchInvoices()
})
</script>