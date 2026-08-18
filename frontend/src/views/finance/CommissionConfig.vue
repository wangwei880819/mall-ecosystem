<template>
  <div class="container">
    <h1 class="page-title">⚙️ 佣金费率配置</h1>

    <div class="card">
      <div class="card-header">
        <h3>佣金费率列表</h3>
        <button class="btn btn-primary btn-sm" @click="showCreateModal = true">+ 新增配置</button>
      </div>
      <table class="data-table">
        <thead>
          <tr><th>商户</th><th>品类</th><th>费率类型</th><th>佣金费率</th><th>阶梯配置</th><th>生效日期</th><th>到期日期</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-if="loading" class="text-center"><td colspan="9">加载中...</td></tr>
          <tr v-else-if="configs.length === 0" class="text-center"><td colspan="9">暂无费率配置</td></tr>
          <tr v-for="c in configs" :key="c.id">
            <td>{{ c.merchantName }}</td>
            <td>{{ c.categoryId ? '品类' + c.categoryId : '全品类' }}</td>
            <td><span :class="getRateTypeClass(c.rateType)">{{ getRateTypeText(c.rateType) }}</span></td>
            <td style="font-weight:600;color:#1a237e">{{ formatRate(c.commissionRate) }}%</td>
            <td>{{ c.ladderConfig ? '已配置' : '-' }}</td>
            <td>{{ c.effectiveDate || '-' }}</td>
            <td>{{ c.expireDate || '-' }}</td>
            <td><span :class="c.status === 'ACTIVE' ? 'tag tag-green' : 'tag tag-gray'">{{ c.status === 'ACTIVE' ? '启用' : '停用' }}</span></td>
            <td>
              <button class="btn btn-sm btn-outline" @click="editConfig(c)">编辑</button>
              <button class="btn btn-sm" :class="c.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'" @click="toggleConfig(c)">{{ c.status === 'ACTIVE' ? '停用' : '启用' }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 配置弹窗 -->
    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal" style="max-width:600px">
        <div class="modal-header">
          <h3>{{ editingConfig ? '编辑费率配置' : '新增费率配置' }}</h3>
          <button class="modal-close" @click="showCreateModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="grid-2">
            <div class="form-group"><label>商户</label><select v-model="form.merchantId"><option v-for="m in merchants" :key="m.id" :value="m.id">{{ m.merchantName }}</option></select></div>
            <div class="form-group"><label>费率类型</label><select v-model="form.rateType"><option value="FIXED">固定费率</option><option value="LADDER">阶梯费率</option><option value="CATEGORY">品类差异化</option></select></div>
            <div class="form-group"><label>佣金费率</label><input type="number" step="0.001" v-model.number="form.commissionRate" placeholder="如 0.05 表示 5%" /></div>
            <div class="form-group"><label>品类（可选）</label><input type="number" v-model.number="form.categoryId" placeholder="留空表示全品类" /></div>
            <div class="form-group"><label>生效日期</label><input type="date" v-model="form.effectiveDate" /></div>
            <div class="form-group"><label>到期日期</label><input type="date" v-model="form.expireDate" /></div>
          </div>
          <div v-if="form.rateType === 'LADDER'" class="form-group">
            <label>阶梯配置（JSON）</label>
            <textarea v-model="form.ladderConfig" rows="4" placeholder='[{"minAmount":0,"maxAmount":10000,"rate":0.05},{"minAmount":10000,"maxAmount":50000,"rate":0.04}]'></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="saveConfig" :disabled="submitting">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const loading = ref(false), submitting = ref(false)
const configs = ref([]), merchants = ref([])
const showCreateModal = ref(false), editingConfig = ref(null)

const form = ref({
  merchantId: 1, rateType: 'FIXED', commissionRate: 0.05,
  categoryId: null, effectiveDate: '', expireDate: '', ladderConfig: ''
})

const formatRate = (val) => {
  if (val === null || val === undefined) return '0'
  const num = Number(val)
  if (num < 1 && num > 0) return (num * 100).toFixed(2)
  return num.toFixed(2)
}

const getRateTypeClass = (t) => ({ FIXED: 'tag tag-blue', LADDER: 'tag tag-orange', CATEGORY: 'tag tag-purple' }[t] || 'tag tag-gray')
const getRateTypeText = (t) => ({ FIXED: '固定费率', LADDER: '阶梯费率', CATEGORY: '品类差异化' }[t] || t)

const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await request.get('/commission/list')
    if (res.code === 200) configs.value = res.data || []
  } catch (e) { console.error('获取配置失败', e) }
  finally { loading.value = false }
}

const fetchMerchants = async () => {
  try {
    const res = await request.get('/merchant', { params: { page: 0, size: 1000 } })
    merchants.value = (res.data?.list || res.data || []).map(m => ({ id: m.id, merchantName: m.merchantName }))
  } catch (e) { console.error('获取商户失败', e) }
}

onMounted(async () => { await Promise.all([fetchConfigs(), fetchMerchants()]) })

const saveConfig = async () => {
  if (!form.value.commissionRate) { alert('请输入佣金费率'); return }
  submitting.value = true
  try {
    if (editingConfig.value) {
      const res = await request.put(`/commission/${editingConfig.value.id}`, form.value)
      if (res.code === 200) { alert('更新成功'); showCreateModal.value = false; editingConfig.value = null }
    } else {
      const res = await request.post('/commission/merchant/' + form.value.merchantId + '/replace', form.value)
      if (res.code === 200) { alert('创建成功'); showCreateModal.value = false }
    }
    await fetchConfigs()
  } catch (e) { alert('保存失败') }
  finally { submitting.value = false }
}

const editConfig = (c) => {
  editingConfig.value = c
  form.value = {
    merchantId: c.merchantId, rateType: c.rateType, commissionRate: c.commissionRate,
    categoryId: c.categoryId, effectiveDate: c.effectiveDate || '', expireDate: c.expireDate || '',
    ladderConfig: c.ladderConfig || ''
  }
  showCreateModal.value = true
}

const toggleConfig = async (c) => {
  const newStatus = c.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    const res = await request.put(`/commission/${c.id}/toggle`, { status: newStatus })
    if (res.code === 200) { c.status = newStatus }
  } catch (e) { alert('操作失败') }
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
.tag-purple { background: #f3e5f5; color: #7b1fa2; }
</style>