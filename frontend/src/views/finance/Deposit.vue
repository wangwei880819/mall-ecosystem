<template>
  <div class="container">
    <h1 class="page-title">💰 保证金管理</h1>

    <div class="grid-4">
      <div class="stat-card blue">
        <div class="stat-label">保证金余额</div>
        <div class="stat-value">¥{{ formatAmount(overview.currentBalance) }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">已缴纳</div>
        <div class="stat-value">¥{{ formatAmount(overview.totalPaid) }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">已退还</div>
        <div class="stat-value">¥{{ formatAmount(overview.totalRefunded) }}</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">已扣除</div>
        <div class="stat-value">¥{{ formatAmount(overview.totalDeducted) }}</div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>保证金流水</h3>
        <div style="display:flex;gap:8px">
          <select v-model="filterType" style="padding:6px 12px;border-radius:4px;border:1px solid #ddd">
            <option value="">全部类型</option>
            <option value="PAY">缴纳</option>
            <option value="REFUND">退还</option>
            <option value="DEDUCT">扣除</option>
          </select>
          <button class="btn btn-primary btn-sm" @click="showPayModal = true">+ 缴纳保证金</button>
          <button class="btn btn-success btn-sm" @click="showRefundModal = true">退还保证金</button>
          <button class="btn btn-danger btn-sm" @click="showDeductModal = true">扣除保证金</button>
        </div>
      </div>
      <table class="data-table">
        <thead>
          <tr><th>流水编号</th><th>商户</th><th>类型</th><th>金额</th><th>余额</th><th>支付方式</th><th>状态</th><th>原因</th><th>时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-if="loading" class="text-center"><td colspan="10">加载中...</td></tr>
          <tr v-else-if="filteredRecords.length === 0" class="text-center"><td colspan="10">暂无保证金记录</td></tr>
          <tr v-for="r in filteredRecords" :key="r.id">
            <td>{{ r.depositCode }}</td>
            <td>{{ r.merchantName }}</td>
            <td><span :class="getTypeClass(r.depositType)">{{ getTypeText(r.depositType) }}</span></td>
            <td style="font-weight:600">¥{{ formatAmount(r.amount) }}</td>
            <td>¥{{ formatAmount(r.balance) }}</td>
            <td>{{ r.payMethod || '-' }}</td>
            <td><span :class="getStatusClass(r.status)">{{ getStatusText(r.status) }}</span></td>
            <td>{{ r.reason || '-' }}</td>
            <td>{{ formatTime(r.createTime) }}</td>
            <td>
              <button v-if="r.status === 'PENDING'" class="btn btn-sm btn-primary" @click="approve(r)">通过</button>
              <button v-if="r.status === 'PENDING'" class="btn btn-sm btn-danger" @click="reject(r)">驳回</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 缴纳弹窗 -->
    <div v-if="showPayModal" class="modal-overlay" @click.self="showPayModal = false">
      <div class="modal" style="max-width:500px">
        <div class="modal-header"><h3>缴纳保证金</h3><button class="modal-close" @click="showPayModal = false">×</button></div>
        <div class="modal-body">
          <div class="form-group"><label>商户</label><select v-model="payForm.merchantId"><option v-for="m in merchants" :key="m.id" :value="m.id">{{ m.merchantName }}</option></select></div>
          <div class="form-group"><label>金额</label><input type="number" v-model.number="payForm.amount" placeholder="请输入金额" /></div>
          <div class="form-group"><label>支付方式</label><select v-model="payForm.payMethod"><option>BANK_TRANSFER</option><option>ALIPAY</option><option>WECHAT</option></select></div>
          <div class="form-group"><label>原因</label><input type="text" v-model="payForm.reason" placeholder="商户入驻保证金" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-primary" @click="submitPay" :disabled="submitting">提交</button></div>
      </div>
    </div>

    <!-- 退还弹窗 -->
    <div v-if="showRefundModal" class="modal-overlay" @click.self="showRefundModal = false">
      <div class="modal" style="max-width:500px">
        <div class="modal-header"><h3>退还保证金</h3><button class="modal-close" @click="showRefundModal = false">×</button></div>
        <div class="modal-body">
          <div class="form-group"><label>商户</label><select v-model="refundForm.merchantId"><option v-for="m in merchants" :key="m.id" :value="m.id">{{ m.merchantName }}</option></select></div>
          <div class="form-group"><label>退还金额</label><input type="number" v-model.number="refundForm.amount" placeholder="请输入金额" /></div>
          <div class="form-group"><label>原因</label><input type="text" v-model="refundForm.reason" placeholder="商户退出退还" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-primary" @click="submitRefund" :disabled="submitting">提交</button></div>
      </div>
    </div>

    <!-- 扣除弹窗 -->
    <div v-if="showDeductModal" class="modal-overlay" @click.self="showDeductModal = false">
      <div class="modal" style="max-width:500px">
        <div class="modal-header"><h3>扣除保证金</h3><button class="modal-close" @click="showDeductModal = false">×</button></div>
        <div class="modal-body">
          <div class="form-group"><label>商户</label><select v-model="deductForm.merchantId"><option v-for="m in merchants" :key="m.id" :value="m.id">{{ m.merchantName }}</option></select></div>
          <div class="form-group"><label>扣除金额</label><input type="number" v-model.number="deductForm.amount" placeholder="请输入金额" /></div>
          <div class="form-group"><label>原因</label><input type="text" v-model="deductForm.reason" placeholder="违规处罚扣除" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-primary" @click="submitDeduct" :disabled="submitting">提交</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false), submitting = ref(false)
const records = ref([]), merchants = ref([])
const filterType = ref('')
const showPayModal = ref(false), showRefundModal = ref(false), showDeductModal = ref(false)

const overview = ref({ currentBalance: 0, totalPaid: 0, totalRefunded: 0, totalDeducted: 0 })

const payForm = ref({ merchantId: 1, amount: 0, payMethod: 'BANK_TRANSFER', reason: '商户入驻保证金' })
const refundForm = ref({ merchantId: 1, amount: 0, reason: '商户退出退还' })
const deductForm = ref({ merchantId: 1, amount: 0, reason: '违规处罚扣除' })

const filteredRecords = computed(() => {
  if (!filterType.value) return records.value
  return records.value.filter(r => r.depositType === filterType.value)
})

const formatAmount = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
const formatTime = (t) => t ? t.substring(0, 16) : '-'

const getTypeClass = (t) => ({ PAY: 'tag tag-green', REFUND: 'tag tag-blue', DEDUCT: 'tag tag-red' }[t] || 'tag tag-gray')
const getTypeText = (t) => ({ PAY: '缴纳', REFUND: '退还', DEDUCT: '扣除' }[t] || t)
const getStatusClass = (s) => ({ PENDING: 'tag tag-orange', COMPLETED: 'tag tag-green', REJECTED: 'tag tag-red' }[s] || 'tag tag-gray')
const getStatusText = (s) => ({ PENDING: '待审批', COMPLETED: '已完成', REJECTED: '已驳回' }[s] || s)

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await request.get('/deposit/list', { params: { page: 0, size: 100 } })
    if (res.code === 200) records.value = res.data.list || []
  } catch (e) { console.error('获取保证金记录失败', e) }
  finally { loading.value = false }
}

const fetchMerchants = async () => {
  try {
    const res = await request.get('/merchant', { params: { page: 0, size: 1000 } })
    merchants.value = (res.data?.list || res.data || []).map(m => ({ id: m.id, merchantName: m.merchantName }))
  } catch (e) { console.error('获取商户失败', e) }
}

onMounted(async () => { await Promise.all([fetchRecords(), fetchMerchants()]) })

const submitPay = async () => {
  if (!payForm.value.amount || payForm.value.amount <= 0) { ElMessage.warning('请输入有效金额'); return }
  if (!payForm.value.merchantId) { ElMessage.warning('请选择商户'); return }
  submitting.value = true
  try {
    const res = await request.post('/deposit/pay', payForm.value)
    if (res.code === 200) { ElMessage.success('提交成功'); showPayModal.value = false; await fetchRecords() }
    else { ElMessage.error(res.message || '提交失败') }
  } catch (e) { ElMessage.error('提交失败，请稍后重试') }
  finally { submitting.value = false }
}

const submitRefund = async () => {
  if (!refundForm.value.amount || refundForm.value.amount <= 0) { ElMessage.warning('请输入有效金额'); return }
  if (!refundForm.value.merchantId) { ElMessage.warning('请选择商户'); return }
  submitting.value = true
  try {
    const res = await request.post('/deposit/refund', refundForm.value)
    if (res.code === 200) { ElMessage.success('提交成功'); showRefundModal.value = false; await fetchRecords() }
    else { ElMessage.error(res.message || '操作失败') }
  } catch (e) { ElMessage.error('操作失败，请稍后重试') }
  finally { submitting.value = false }
}

const submitDeduct = async () => {
  if (!deductForm.value.amount || deductForm.value.amount <= 0) { ElMessage.warning('请输入有效金额'); return }
  if (!deductForm.value.merchantId) { ElMessage.warning('请选择商户'); return }
  submitting.value = true
  try {
    const res = await request.post('/deposit/deduct', deductForm.value)
    if (res.code === 200) { ElMessage.success('提交成功'); showDeductModal.value = false; await fetchRecords() }
    else { ElMessage.error(res.message || '操作失败') }
  } catch (e) { ElMessage.error('操作失败，请稍后重试') }
  finally { submitting.value = false }
}

const approve = async (r) => {
  try {
    const res = await request.put(`/deposit/${r.id}/approve`, { approver: 'admin' })
    if (res.code === 200) { r.status = 'COMPLETED'; ElMessage.success('审批通过') }
    else { ElMessage.error(res.message || '操作失败') }
  } catch (e) { ElMessage.error('操作失败') }
}

const reject = async (r) => {
  try {
    await ElMessageBox.prompt('请输入驳回原因', '驳回确认', { confirmButtonText: '确认驳回', cancelButtonText: '取消' })
  } catch { return }
  // 使用 prompt 的输入值
  const reason = document.querySelector('.el-message-box__input input')?.value || ''
  if (!reason) { ElMessage.warning('请输入驳回原因'); return }
  try {
    const res = await request.put(`/deposit/${r.id}/reject`, { reason })
    if (res.code === 200) { r.status = 'REJECTED'; ElMessage.success('已驳回') }
    else { ElMessage.error(res.message || '操作失败') }
  } catch (e) { ElMessage.error('操作失败') }
}
</script>

<style scoped>
.container { padding: 20px; }
.page-title { margin-bottom: 24px; color: #333; font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,.1); padding: 20px; margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-bottom: 16px; }
.card-header h3 { margin: 0; color: #333; font-size: 16px; }
.text-center { text-align: center; }
.btn-sm { padding: 4px 12px; font-size: 12px; }
.btn-success { background: #67c23a; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-danger { background: #f56c6c; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>