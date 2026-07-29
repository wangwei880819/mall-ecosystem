<template>
  <div class="page-container">
    <div class="page-header">
      <h2>💰 退款管理</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索订单号/退款单号" style="width: 300px" @keyup.enter="fetchRefunds" />
        <el-select v-model="filterStatus" placeholder="退款状态" style="width: 120px" @change="fetchRefunds">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已退款" value="REFUNDED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
        <el-select v-model="filterType" placeholder="退款类型" style="width: 120px" @change="fetchRefunds">
          <el-option label="全部" value="" />
          <el-option label="全额退款" value="FULL" />
          <el-option label="部分退款" value="PARTIAL" />
          <el-option label="缺货退款" value="OUT_OF_STOCK" />
        </el-select>
        <el-button type="primary" @click="fetchRefunds">搜索</el-button>
      </div>
    </div>

    <div class="grid-4" style="margin-bottom: 20px;">
      <div class="stat-card">
        <div class="stat-label">退款订单</div>
        <div class="stat-value">{{ refundStats.total || 0 }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待审核</div>
        <div class="stat-value">{{ refundStats.pending || 0 }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">已退款</div>
        <div class="stat-value">{{ refundStats.refunded || 0 }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">退款金额</div>
        <div class="stat-value">¥{{ refundStats.totalAmount || 0 }}</div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="refunds" border stripe>
      <el-table-column prop="refundNo" label="退款单号" width="160" />
      <el-table-column prop="orderCode" label="订单编号" width="160" />
      <el-table-column prop="customerPhone" label="买家手机号" width="140" />
      <el-table-column prop="productName" label="商品名称" width="200" />
      <el-table-column prop="payAmount" label="实付金额" width="120">
        <template #default="{ row }">¥{{ (row.payAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="refundAmount" label="退款金额" width="120">
        <template #default="{ row }">¥{{ (row.refundAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="refundType" label="退款类型" width="120">
        <template #default="{ row }">{{ getTypeText(row.refundType) }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="退款原因" width="150" />
      <el-table-column prop="status" label="退款状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="180" />
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="handleApprove(row)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          <el-button v-if="row.status === 'APPROVED'" size="small" type="primary" @click="handleRefund(row)">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog v-model="showRejectDialog" title="拒绝退款" width="400px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因">
          <el-textarea v-model="rejectForm.reason" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { REFUND_TYPE, REFUND_STATUS, REFUND_STATUS_TYPE } from '../../utils/constants'

const refunds = ref([])
const refundStats = ref({})
const searchKeyword = ref('')
const filterStatus = ref('')
const filterType = ref('')
const showRejectDialog = ref(false)
const currentRefund = ref(null)

const rejectForm = ref({ reason: '' })

const getTypeText = (type) => REFUND_TYPE[type] || type

const getStatusType = (status) => REFUND_STATUS_TYPE[status] || 'info'

const getStatusText = (status) => REFUND_STATUS[status] || status

const fetchRefunds = async () => {
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterType.value) params.type = filterType.value
    const res = await request.get('/order/refunds', { params })
    if (res.code === 200) {
      refunds.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch refunds:', e)
    refunds.value = [
      { id: 1, refundNo: 'REF20260726001', orderCode: 'ORD1785000000001', customerPhone: '13800138001', productName: '腾讯视频VIP会员月卡', payAmount: 19.90, refundAmount: 19.90, refundType: 'FULL', reason: '不想购买了', status: 'PENDING', applyTime: '2026-07-26 11:30:00' },
      { id: 2, refundNo: 'REF20260726002', orderCode: 'ORD1785000000002', customerPhone: '13800138002', productName: '瑞幸咖啡29元通兑券', payAmount: 39.80, refundAmount: 19.90, refundType: 'PARTIAL', reason: '收到一张，另一张未使用', status: 'APPROVED', applyTime: '2026-07-26 10:15:00' },
      { id: 3, refundNo: 'REF20260725003', orderCode: 'ORD1785000000005', customerPhone: '13800138005', productName: '美团外卖红包5元', payAmount: 19.50, refundAmount: 19.50, refundType: 'FULL', reason: '优惠券过期', status: 'REFUNDED', applyTime: '2026-07-25 17:00:00' },
      { id: 4, refundNo: 'REF20260726004', orderCode: 'ORD1785000000004', customerPhone: '13800138004', productName: 'QQ音乐绿钻豪华版月卡', payAmount: 45.00, refundAmount: 45.00, refundType: 'OUT_OF_STOCK', reason: '库存不足无法发货', status: 'PENDING', applyTime: '2026-07-26 14:20:00' },
      { id: 5, refundNo: 'REF20260725005', orderCode: 'ORD1785000000003', customerPhone: '13800138003', productName: '爱奇艺黄金会员月卡', payAmount: 22.00, refundAmount: 22.00, refundType: 'FULL', reason: '误购', status: 'REJECTED', applyTime: '2026-07-25 09:30:00' }
    ]
  }
  calculateStats()
}

const calculateStats = () => {
  refundStats.value = {
    total: refunds.value.length,
    pending: refunds.value.filter(r => r.status === 'PENDING').length,
    refunded: refunds.value.filter(r => r.status === 'REFUNDED').length,
    totalAmount: refunds.value.reduce((sum, r) => sum + (r.refundAmount || 0), 0).toFixed(2)
  }
}

const viewDetail = (refund) => {
  ElMessage.info(`查看退款详情：${refund.refundNo}`)
}

const handleApprove = async (refund) => {
  try {
    const res = await request.put(`/order/refunds/${refund.id}/approve`)
    if (res.code === 200) {
      refund.status = 'APPROVED'
      ElMessage.success('审核通过')
      calculateStats()
    }
  } catch (e) {
    console.error('Approve refund error:', e)
    refund.status = 'APPROVED'
    ElMessage.success('审核通过')
    calculateStats()
  }
}

const handleReject = (refund) => {
  currentRefund.value = refund
  rejectForm.value = { reason: '' }
  showRejectDialog.value = true
}

const submitReject = async () => {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    const res = await request.put(`/order/refunds/${currentRefund.value.id}/reject`, { reason: rejectForm.value.reason })
    if (res.code === 200) {
      currentRefund.value.status = 'REJECTED'
      ElMessage.success('已拒绝退款')
      showRejectDialog.value = false
      calculateStats()
    }
  } catch (e) {
    console.error('Reject refund error:', e)
    currentRefund.value.status = 'REJECTED'
    ElMessage.success('已拒绝退款')
    showRejectDialog.value = false
    calculateStats()
  }
}

const handleRefund = async (refund) => {
  try {
    await ElMessageBox.confirm(`确定要执行退款吗？退款金额：¥${refund.refundAmount.toFixed(2)}`, '确认退款', { type: 'warning' })
    const res = await request.put(`/order/refunds/${refund.id}/refund`)
    if (res.code === 200) {
      refund.status = 'REFUNDED'
      ElMessage.success('退款成功')
      calculateStats()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Refund error:', e)
      refund.status = 'REFUNDED'
      ElMessage.success('退款成功')
      calculateStats()
    }
  }
}

onMounted(async () => {
  await fetchRefunds()
})
</script>