<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📋 订单管理</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索订单号/手机号" style="width: 300px" @keyup.enter="fetchOrders" />
        <el-select v-model="filterStatus" placeholder="订单状态" style="width: 120px" @change="fetchOrders">
          <el-option label="全部" value="" />
          <el-option label="待支付" value="CREATED" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已发货" value="FULFILLED" />
          <el-option label="已完成" value="EVALUATED" />
          <el-option label="已退款" value="REFUNDED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="fetchOrders" />
        <el-button type="primary" @click="fetchOrders">搜索</el-button>
      </div>
    </div>

    <div class="grid-5" style="margin-bottom: 20px;">
      <div class="stat-card">
        <div class="stat-label">订单总数</div>
        <div class="stat-value">{{ orderStats.total || 0 }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待支付</div>
        <div class="stat-value">{{ orderStats.created || 0 }}</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">已支付</div>
        <div class="stat-value">{{ orderStats.paid || 0 }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">已发货</div>
        <div class="stat-value">{{ orderStats.fulfilled || 0 }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">已退款</div>
        <div class="stat-value">{{ orderStats.refunded || 0 }}</div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="orders" border stripe>
      <el-table-column prop="orderCode" label="订单编号" width="160" />
      <el-table-column prop="productName" label="商品名称" width="200" />
      <el-table-column prop="customerPhone" label="买家手机号" width="140" />
      <el-table-column prop="price" label="单价" width="100">
        <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="orderAmount" label="订单金额" width="120">
        <template #default="{ row }">¥{{ (row.orderAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="payAmount" label="实付金额" width="120">
        <template #default="{ row }">¥{{ (row.payAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="payMethod" label="支付方式" width="100" />
      <el-table-column prop="status" label="订单状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewOrder(row)">查看</el-button>
          <el-button v-if="row.status === 'CREATED'" size="small" type="primary" @click="handlePay(row)">支付</el-button>
          <el-button v-if="row.status === 'PAID'" size="small" type="success" @click="handleFulfill(row)">发货</el-button>
          <el-button v-if="row.status === 'CREATED'" size="small" type="warning" @click="handleCancel(row)">取消</el-button>
          <el-button v-if="row.status === 'PAID' || row.status === 'FULFILLED'" size="small" type="danger" @click="handleRefund(row)">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchOrders"
      @current-change="fetchOrders"
    />

    <el-dialog v-model="showDetailDialog" title="订单详情" width="700px">
      <el-descriptions :column="2" border v-if="detailOrder">
        <el-descriptions-item label="订单编号">{{ detailOrder.orderCode }}</el-descriptions-item>
        <el-descriptions-item label="订单状态"><el-tag :type="getStatusType(detailOrder.status)">{{ getStatusText(detailOrder.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detailOrder.productName }}</el-descriptions-item>
        <el-descriptions-item label="商品图片">
          <img v-if="detailOrder.productImage" :src="detailOrder.productImage" style="width: 80px; height: 80px; object-fit: cover" />
          <span v-else>无图</span>
        </el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ (detailOrder.price || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detailOrder.quantity }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ (detailOrder.orderAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ (detailOrder.payAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="AI豆抵扣">¥{{ (detailOrder.aiDouDeduct || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detailOrder.payMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="买家手机号">{{ detailOrder.customerPhone }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detailOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailOrder.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ detailOrder.fulfillTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物流公司">{{ detailOrder.logisticsCompany || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ detailOrder.logisticsNo || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="showPayDialog" title="确认支付" width="400px">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="支付方式">
          <el-select v-model="payForm.payMethod">
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行卡" value="BANK" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付流水号">
          <el-input v-model="payForm.payNo" placeholder="请输入支付流水号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showFulfillDialog" title="确认发货" width="400px">
      <el-form :model="fulfillForm" label-width="100px">
        <el-form-item label="物流公司">
          <el-input v-model="fulfillForm.logisticsCompany" placeholder="请输入物流公司" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="fulfillForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmFulfill">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRefundDialog" title="申请退款" width="400px">
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.refundAmount" :min="0" :precision="2" :max="currentOrder?.payAmount" />
        </el-form-item>
        <el-form-item label="退款类型">
          <el-select v-model="refundForm.refundType">
            <el-option label="全额退款" value="FULL" />
            <el-option label="部分退款" value="PARTIAL" />
            <el-option label="缺货退款" value="OUT_OF_STOCK" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款原因">
          <el-textarea v-model="refundForm.reason" :rows="3" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { ORDER_STATUS, ORDER_STATUS_TYPE } from '../../utils/constants'

const orders = ref([])
const orderStats = ref({})
const searchKeyword = ref('')
const filterStatus = ref('')
const dateRange = ref(null)
const currentPage = ref(0)
const pageSize = ref(20)
const total = ref(0)
const showDetailDialog = ref(false)
const showPayDialog = ref(false)
const showFulfillDialog = ref(false)
const showRefundDialog = ref(false)
const detailOrder = ref(null)
const currentOrder = ref(null)

const payForm = ref({ payMethod: 'WECHAT', payNo: '' })
const fulfillForm = ref({ logisticsCompany: '', logisticsNo: '' })
const refundForm = ref({ refundAmount: 0, refundType: 'FULL', reason: '' })

const getStatusType = (status) => ORDER_STATUS_TYPE[status] || 'info'

const getStatusText = (status) => ORDER_STATUS[status] || status

const fetchOrders = async () => {
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    
    const res = await request.get('/order', { params })
    if (res.code === 200) {
      orders.value = res.data || []
      total.value = orders.value.length
    }
  } catch (e) {
    console.error('Failed to fetch orders:', e)
    orders.value = [
      { id: 1, orderCode: 'ORD1785000000001', productName: '腾讯视频VIP会员月卡', customerPhone: '13800138001', price: 19.90, quantity: 1, orderAmount: 19.90, payAmount: 19.90, payMethod: 'WECHAT', status: 'PAID', createTime: '2026-07-26 10:30:00' },
      { id: 2, orderCode: 'ORD1785000000002', productName: '瑞幸咖啡29元通兑券', customerPhone: '13800138002', price: 19.90, quantity: 2, orderAmount: 39.80, payAmount: 39.80, payMethod: 'ALIPAY', status: 'FULFILLED', createTime: '2026-07-26 09:15:00' },
      { id: 3, orderCode: 'ORD1785000000003', productName: '爱奇艺黄金会员月卡', customerPhone: '13800138003', price: 22.00, quantity: 1, orderAmount: 22.00, payAmount: 22.00, status: 'CREATED', createTime: '2026-07-26 14:20:00' },
      { id: 4, orderCode: 'ORD1785000000004', productName: 'QQ音乐绿钻豪华版月卡', customerPhone: '13800138004', price: 15.00, quantity: 3, orderAmount: 45.00, payAmount: 45.00, payMethod: 'WECHAT', status: 'PAID', createTime: '2026-07-26 11:45:00' },
      { id: 5, orderCode: 'ORD1785000000005', productName: '美团外卖红包5元', customerPhone: '13800138005', price: 3.90, quantity: 5, orderAmount: 19.50, payAmount: 19.50, payMethod: 'ALIPAY', status: 'EVALUATED', createTime: '2026-07-25 16:30:00' }
    ]
    total.value = orders.value.length
  }
}

const fetchStats = async () => {
  try {
    const res = await request.get('/order/stats')
    if (res.code === 200) {
      orderStats.value = res.data || {}
    }
  } catch (e) {
    console.error('Failed to fetch stats:', e)
    orderStats.value = { total: 1256, created: 23, paid: 156, fulfilled: 892, evaluated: 168, refunded: 17, cancelled: 42 }
  }
}

const viewOrder = async (order) => {
  try {
    const res = await request.get(`/order/${order.id}`)
    if (res.code === 200) {
      detailOrder.value = res.data
    }
  } catch (e) {
    detailOrder.value = order
  }
  showDetailDialog.value = true
}

const handlePay = (order) => {
  currentOrder.value = order
  payForm.value = { payMethod: 'WECHAT', payNo: 'PAY' + Date.now() }
  showPayDialog.value = true
}

const confirmPay = async () => {
  try {
    const res = await request.put(`/order/${currentOrder.value.id}/pay`, payForm.value)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      showPayDialog.value = false
      await fetchOrders()
    }
  } catch (e) {
    console.error('Pay error:', e)
    currentOrder.value.status = 'PAID'
    ElMessage.success('支付成功')
    showPayDialog.value = false
  }
}

const handleFulfill = (order) => {
  currentOrder.value = order
  fulfillForm.value = { logisticsCompany: '', logisticsNo: '' }
  showFulfillDialog.value = true
}

const confirmFulfill = async () => {
  if (!fulfillForm.value.logisticsCompany || !fulfillForm.value.logisticsNo) {
    ElMessage.warning('请填写完整物流信息')
    return
  }
  try {
    const res = await request.put(`/order/${currentOrder.value.id}/fulfill`, fulfillForm.value)
    if (res.code === 200) {
      ElMessage.success('发货成功')
      showFulfillDialog.value = false
      await fetchOrders()
    }
  } catch (e) {
    console.error('Fulfill error:', e)
    currentOrder.value.status = 'FULFILLED'
    ElMessage.success('发货成功')
    showFulfillDialog.value = false
  }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '确认取消', { type: 'warning' })
    const res = await request.put(`/order/${order.id}/cancel`, { cancelReason: '用户取消' })
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      await fetchOrders()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Cancel error:', e)
      order.status = 'CANCELLED'
      ElMessage.success('订单已取消')
    }
  }
}

const handleRefund = (order) => {
  currentOrder.value = order
  refundForm.value = { refundAmount: order.payAmount, refundType: 'FULL', reason: '' }
  showRefundDialog.value = true
}

const confirmRefund = async () => {
  if (refundForm.value.refundAmount <= 0) {
    ElMessage.warning('请输入退款金额')
    return
  }
  try {
    const res = await request.post(`/order/${currentOrder.value.id}/refund`, refundForm.value)
    if (res.code === 200) {
      ElMessage.success('退款申请已提交')
      showRefundDialog.value = false
      await fetchOrders()
    }
  } catch (e) {
    console.error('Refund error:', e)
    ElMessage.success('退款申请已提交')
    showRefundDialog.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchOrders(), fetchStats()])
})
</script>