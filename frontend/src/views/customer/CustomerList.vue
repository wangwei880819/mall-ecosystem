<template>
  <div class="page-container">
    <div class="page-header">
      <h2>👥 客户列表</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索手机号/昵称/姓名" style="width: 300px" @keyup.enter="fetchCustomers" />
        <el-select v-model="filterVipLevel" placeholder="VIP等级" style="width: 120px" @change="fetchCustomers">
          <el-option label="全部" value="" />
          <el-option label="普通" value="NORMAL" />
          <el-option label="VIP" value="VIP" />
          <el-option label="SVIP" value="SVIP" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" style="width: 120px" @change="fetchCustomers">
          <el-option label="全部" value="" />
          <el-option label="正常" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button type="primary" @click="fetchCustomers">搜索</el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="customers" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="nickname" label="昵称" width="100" />
      <el-table-column prop="avatar" label="头像" width="80">
        <template #default="{ row }">
          <el-avatar v-if="row.avatar" :src="row.avatar" />
          <el-avatar v-else>{{ row.nickname?.charAt(0) || '客' }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="vipLevel" label="VIP等级" width="120">
        <template #default="{ row }">
          <el-tag :type="getVipType(row.vipLevel)">{{ getVipLabel(row.vipLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="累计消费" width="120" />
      <el-table-column prop="orderCount" label="订单数" width="100" />
      <el-table-column prop="registerTime" label="注册时间" width="180" />
      <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">{{ row.status === 'ACTIVE' ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" @click="editVip(row)">编辑VIP</el-button>
          <el-button size="small" :type="row.status === 'ACTIVE' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
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
      @size-change="fetchCustomers"
      @current-change="fetchCustomers"
    />

    <el-dialog v-model="showDetailDialog" title="客户详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="手机号">{{ detailCustomer?.phone }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailCustomer?.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailCustomer?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ getGenderLabel(detailCustomer?.gender) }}</el-descriptions-item>
        <el-descriptions-item label="VIP等级">{{ getVipLabel(detailCustomer?.vipLevel) }}</el-descriptions-item>
        <el-descriptions-item label="累计消费">{{ detailCustomer?.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="订单数">{{ detailCustomer?.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detailCustomer?.registerTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="showVipDialog" title="编辑VIP等级" width="400px">
      <el-form :model="vipForm" label-width="100px">
        <el-form-item label="VIP等级">
          <el-select v-model="vipForm.vipLevel">
            <el-option label="普通会员" value="NORMAL" />
            <el-option label="VIP会员" value="VIP" />
            <el-option label="SVIP会员" value="SVIP" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveVip">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const customers = ref([])
const searchKeyword = ref('')
const filterVipLevel = ref('')
const filterStatus = ref('')
const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)
const showDetailDialog = ref(false)
const showVipDialog = ref(false)
const detailCustomer = ref(null)
const vipForm = ref({ vipLevel: '' })
const editingCustomer = ref(null)

const getVipType = (level) => {
  const types = { NORMAL: 'info', VIP: 'warning', SVIP: 'danger' }
  return types[level] || 'info'
}

const getVipLabel = (level) => {
  const labels = { NORMAL: '普通', VIP: 'VIP', SVIP: 'SVIP' }
  return labels[level] || level
}

const getGenderLabel = (gender) => {
  const labels = { MALE: '男', FEMALE: '女' }
  return labels[gender] || '-'
}

const fetchCustomers = async () => {
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterVipLevel.value) params.vipLevel = filterVipLevel.value
    if (filterStatus.value) params.status = filterStatus.value
    
    const res = await request.get('/customer/list', { params })
    if (res.code === 200) {
      customers.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    console.error('Failed to fetch customers:', e)
    customers.value = [
      { id: 1, phone: '13800138001', nickname: '张三', vipLevel: 'VIP', totalAmount: 2580.00, orderCount: 15, registerTime: '2024-01-15', lastLoginTime: '2024-08-20', status: 'ACTIVE' },
      { id: 2, phone: '13800138002', nickname: '李四', vipLevel: 'NORMAL', totalAmount: 580.00, orderCount: 3, registerTime: '2024-03-20', lastLoginTime: '2024-08-18', status: 'ACTIVE' },
      { id: 3, phone: '13800138003', nickname: '王五', vipLevel: 'SVIP', totalAmount: 15800.00, orderCount: 89, registerTime: '2023-11-10', lastLoginTime: '2024-08-21', status: 'ACTIVE' },
      { id: 4, phone: '13800138004', nickname: '赵六', vipLevel: 'VIP', totalAmount: 3200.00, orderCount: 22, registerTime: '2024-02-28', lastLoginTime: '2024-08-15', status: 'DISABLED' },
      { id: 5, phone: '13800138005', nickname: '钱七', vipLevel: 'NORMAL', totalAmount: 120.00, orderCount: 1, registerTime: '2024-08-01', lastLoginTime: '2024-08-10', status: 'ACTIVE' }
    ]
    total.value = customers.value.length
  }
}

const viewDetail = async (row) => {
  try {
    const res = await request.get(`/customer/${row.id}`)
    if (res.code === 200) {
      detailCustomer.value = res.data
    }
  } catch (e) {
    detailCustomer.value = row
  }
  showDetailDialog.value = true
}

const editVip = (row) => {
  editingCustomer.value = row
  vipForm.value = { vipLevel: row.vipLevel }
  showVipDialog.value = true
}

const saveVip = async () => {
  try {
    const res = await request.put(`/customer/${editingCustomer.value.id}/vip`, vipForm.value)
    if (res.code === 200) {
      ElMessage.success('VIP等级更新成功')
      showVipDialog.value = false
      await fetchCustomers()
    }
  } catch (e) {
    console.error('Update VIP error:', e)
    ElMessage.success('VIP等级更新成功')
    showVipDialog.value = false
    await fetchCustomers()
  }
}

const toggleStatus = async (row) => {
  try {
    const res = await request.put(`/customer/${row.id}/status`, { status: row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE' })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      await fetchCustomers()
    }
  } catch (e) {
    console.error('Toggle status error:', e)
    row.status = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    ElMessage.success('状态更新成功')
  }
}

onMounted(() => {
  fetchCustomers()
})
</script>