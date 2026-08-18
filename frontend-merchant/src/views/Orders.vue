<script setup>
import { ref, computed, onMounted } from 'vue'
import http from '@/utils/http'
import { useMerchantStore } from '@/stores/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'

const merchantStore = useMerchantStore()
const merchantId = computed(() => merchantStore.merchantInfo?.id || merchantStore.merchantInfo?.merchant?.id || '')
const orders = ref([])
const loading = ref(false)
const statusFilter = ref('')

const orderStatusMap = {
  'CREATED': '待支付',
  'PAID': '已支付',
  'FULFILLED': '已发货',
  'SHIPPED': '已发货',
  'EVALUATED': '已评价',
  'REFUNDED': '已退款',
  'CANCELLED': '已取消'
}

async function loadOrders() {
  loading.value = true
  try {
    const params = { merchantId: merchantId.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await http.get('/merchant-portal/orders', { params })
    orders.value = res || []
  } catch (error) {
    // 静默处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})

async function handleShip(row) {
  try {
    await ElMessageBox.confirm(`确认对订单 ${row.orderCode} 进行发货？`, '发货确认', { type: 'info', showCancelButton: false })
    await http.put(`/merchant-portal/orders/${row.id}/ship`)
    ElMessage.success('发货成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '发货失败')
    }
  }
}

function handleDetail(row) {
  ElMessageBox.alert(
    `订单号：${row.orderCode}\n商品：${row.productName}\n金额：¥${row.payAmount}\n状态：${orderStatusMap[row.status] || row.status}\n时间：${row.createTime || '-'}`,
    '订单详情',
    { confirmButtonText: '确定' }
  )
}

function formatTime(time) {
  if (!time) return '-'
  return time
}
</script>

<template>
  <div class="page-container">
    <div class="page-header-actions">
      <h2>我的订单</h2>
    </div>

    <div class="card">
      <div class="search-bar">
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:140px" @change="loadOrders">
          <el-option label="全部" value="" />
          <el-option label="待支付" value="CREATED" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已发货" value="FULFILLED" />
          <el-option label="已评价" value="EVALUATED" />
          <el-option label="已退款" value="REFUNDED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>

      <el-table :data="orders" v-loading="loading" size="small">
        <el-table-column prop="orderCode" label="订单编号" width="160" />
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="payAmount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'PAID' ? '' : row.status === 'FULFILLED' || row.status === 'SHIPPED' ? 'success' : row.status === 'CANCELLED' || row.status === 'REFUNDED' ? 'danger' : 'info'">
              {{ orderStatusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PAID' || row.status === 'SHIPPED'"
              type="primary"
              size="small"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button
              size="small"
              @click="handleDetail(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
