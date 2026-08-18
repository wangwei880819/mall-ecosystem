<template>
  <div class="container">
    <h1 class="page-title">📝 合同管理</h1>

    <div class="grid-4">
      <div class="stat-card blue"><div class="stat-label">合同总数</div><div class="stat-value">{{ overview.totalCount || 0 }}</div></div>
      <div class="stat-card orange"><div class="stat-label">待签署</div><div class="stat-value">{{ overview.pendingSignCount || 0 }}</div></div>
      <div class="stat-card green"><div class="stat-label">已签署</div><div class="stat-value">{{ overview.signedCount || 0 }}</div></div>
      <div class="stat-card purple"><div class="stat-label">模板数</div><div class="stat-value">{{ overview.templateCount || 0 }}</div></div>
    </div>

    <el-tabs v-model="tab">
      <el-tab-pane label="合同列表" name="contracts" />
      <el-tab-pane label="合同模板" name="templates" />
    </el-tabs>

    <!-- 合同列表 -->
    <template v-if="tab === 'contracts'">
      <div class="card">
        <div class="card-header">
          <h3>合同列表</h3>
          <div style="display:flex;gap:8px">
            <select v-model="filterContractStatus" style="padding:6px 12px;border-radius:4px;border:1px solid #ddd">
              <option value="">全部状态</option>
              <option value="DRAFT">草稿</option>
              <option value="PENDING_SIGN">待签署</option>
              <option value="SIGNED">已签署</option>
              <option value="TERMINATED">已终止</option>
            </select>
            <button class="btn btn-primary btn-sm" @click="showCreateContract = true">+ 新建合同</button>
          </div>
        </div>
        <table class="data-table">
          <thead>
            <tr><th>合同编号</th><th>商户</th><th>合同标题</th><th>类型</th><th>佣金费率</th><th>保证金</th><th>平台签署</th><th>商户签署</th><th>状态</th><th>有效期</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-if="contractLoading" class="text-center"><td colspan="11">加载中...</td></tr>
            <tr v-else-if="filteredContracts.length === 0" class="text-center"><td colspan="11">暂无合同</td></tr>
            <tr v-for="c in filteredContracts" :key="c.id">
              <td>{{ c.contractCode }}</td>
              <td>{{ c.merchantName }}</td>
              <td>{{ c.contractTitle }}</td>
              <td><span class="tag tag-blue">{{ getContractTypeText(c.contractType) }}</span></td>
              <td>{{ c.commissionRate ? (c.commissionRate * 100).toFixed(2) + '%' : '-' }}</td>
              <td>{{ c.depositAmount ? '¥' + formatAmount(c.depositAmount) : '-' }}</td>
              <td><span :class="c.platformSigned ? 'tag tag-green' : 'tag tag-gray'">{{ c.platformSigned ? '已签署' : '未签署' }}</span></td>
              <td><span :class="c.merchantSigned ? 'tag tag-green' : 'tag tag-gray'">{{ c.merchantSigned ? '已签署' : '未签署' }}</span></td>
              <td><span :class="getContractStatusClass(c.status)">{{ getContractStatusText(c.status) }}</span></td>
              <td>{{ c.effectiveDate || '-' }} ~ {{ c.expireDate || '-' }}</td>
              <td>
                <button class="btn btn-sm btn-outline" @click="viewContract(c)">详情</button>
                <button v-if="c.status === 'DRAFT'" class="btn btn-sm btn-primary" @click="submitContract(c)">提交签署</button>
                <button v-if="c.status === 'PENDING_SIGN' && !c.platformSigned" class="btn btn-sm btn-success" @click="platformSign(c)">平台签署</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 新建合同弹窗 -->
      <div v-if="showCreateContract" class="modal-overlay" @click.self="showCreateContract = false">
        <div class="modal" style="max-width:700px">
          <div class="modal-header"><h3>新建合同</h3><button class="modal-close" @click="showCreateContract = false">×</button></div>
          <div class="modal-body">
            <div class="grid-2">
              <div class="form-group"><label>商户</label><select v-model="newContract.merchantId"><option v-for="m in merchants" :key="m.id" :value="m.id">{{ m.merchantName }}</option></select></div>
              <div class="form-group"><label>合同类型</label><select v-model="newContract.contractType"><option value="SETTLEMENT">入驻合同</option><option value="COOPERATION">合作协议</option><option value="SUPPLEMENT">补充协议</option></select></div>
              <div class="form-group"><label>合同标题</label><input type="text" v-model="newContract.contractTitle" placeholder="商户入驻合作协议" /></div>
              <div class="form-group"><label>使用模板</label><select v-model="newContract.templateId"><option :value="null">不使用模板</option><option v-for="t in templates" :key="t.id" :value="t.id">{{ t.templateName }}</option></select></div>
              <div class="form-group"><label>佣金费率</label><input type="number" step="0.01" v-model.number="newContract.commissionRate" placeholder="如 0.05" /></div>
              <div class="form-group"><label>保证金金额</label><input type="number" v-model.number="newContract.depositAmount" placeholder="保证金金额" /></div>
              <div class="form-group"><label>生效日期</label><input type="date" v-model="newContract.effectiveDate" /></div>
              <div class="form-group"><label>到期日期</label><input type="date" v-model="newContract.expireDate" /></div>
            </div>
            <div class="form-group"><label>合同内容</label><textarea v-model="newContract.contractContent" rows="6" placeholder="合同正文内容..."></textarea></div>
            <div class="form-group"><label>备注</label><input type="text" v-model="newContract.remark" placeholder="备注信息" /></div>
          </div>
          <div class="modal-footer"><button class="btn btn-primary" @click="createContract" :disabled="submitting">创建合同</button></div>
        </div>
      </div>
    </template>

    <!-- 合同模板 -->
    <template v-if="tab === 'templates'">
      <div class="card">
        <div class="card-header">
          <h3>合同模板</h3>
          <button class="btn btn-primary btn-sm" @click="showCreateTemplate = true">+ 新建模板</button>
        </div>
        <table class="data-table">
          <thead><tr><th>模板编号</th><th>模板名称</th><th>类型</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="templateLoading" class="text-center"><td colspan="6">加载中...</td></tr>
            <tr v-else-if="templates.length === 0" class="text-center"><td colspan="6">暂无模板</td></tr>
            <tr v-for="t in templates" :key="t.id">
              <td>{{ t.templateCode }}</td>
              <td>{{ t.templateName }}</td>
              <td><span class="tag tag-blue">{{ getTemplateTypeText(t.templateType) }}</span></td>
              <td><span :class="t.status === 'ACTIVE' ? 'tag tag-green' : 'tag tag-gray'">{{ t.status === 'ACTIVE' ? '启用' : '停用' }}</span></td>
              <td>{{ formatTime(t.createTime) }}</td>
              <td>
                <button class="btn btn-sm btn-outline" @click="editTemplate(t)">编辑</button>
                <button class="btn btn-sm" :class="t.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'" @click="toggleTemplate(t)">{{ t.status === 'ACTIVE' ? '停用' : '启用' }}</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 模板弹窗 -->
      <div v-if="showCreateTemplate" class="modal-overlay" @click.self="showCreateTemplate = false">
        <div class="modal" style="max-width:700px">
          <div class="modal-header"><h3>{{ editingTemplate ? '编辑模板' : '新建模板' }}</h3><button class="modal-close" @click="showCreateTemplate = false">×</button></div>
          <div class="modal-body">
            <div class="grid-2">
              <div class="form-group"><label>模板名称</label><input type="text" v-model="templateForm.templateName" placeholder="标准入驻合同模板" /></div>
              <div class="form-group"><label>模板类型</label><select v-model="templateForm.templateType"><option value="SETTLEMENT">入驻合同</option><option value="COOPERATION">合作协议</option><option value="SUPPLEMENT">补充协议</option></select></div>
            </div>
            <div class="form-group"><label>模板内容</label><textarea v-model="templateForm.content" rows="10" placeholder="合同模板内容，支持变量 {{变量名}}"></textarea></div>
            <div class="form-group"><label>变量定义（JSON）</label><input type="text" v-model="templateForm.variables" placeholder='{"companyName":"企业名称","commissionRate":"佣金费率"}' /></div>
          </div>
          <div class="modal-footer"><button class="btn btn-primary" @click="saveTemplate" :disabled="submitting">保存</button></div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'

const tab = ref('contracts')
const contractLoading = ref(false), templateLoading = ref(false), submitting = ref(false)
const contracts = ref([]), templates = ref([]), merchants = ref([])
const filterContractStatus = ref('')
const showCreateContract = ref(false), showCreateTemplate = ref(false)
const editingTemplate = ref(null)

const overview = ref({ totalCount: 0, pendingSignCount: 0, signedCount: 0, templateCount: 0 })

const newContract = ref({
  merchantId: 1, contractType: 'SETTLEMENT', contractTitle: '商户入驻合作协议',
  templateId: null, commissionRate: 0.05, depositAmount: 0,
  contractContent: '', effectiveDate: '', expireDate: '', remark: ''
})

const templateForm = ref({ templateName: '', templateType: 'SETTLEMENT', content: '', variables: '' })

const filteredContracts = computed(() => {
  if (!filterContractStatus.value) return contracts.value
  return contracts.value.filter(c => c.status === filterContractStatus.value)
})

const formatAmount = (v) => v ? Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0'
const formatTime = (t) => t ? t.substring(0, 16) : '-'

const getContractTypeText = (t) => ({ SETTLEMENT: '入驻合同', COOPERATION: '合作协议', SUPPLEMENT: '补充协议' }[t] || t)
const getTemplateTypeText = (t) => ({ SETTLEMENT: '入驻合同', COOPERATION: '合作协议', SUPPLEMENT: '补充协议' }[t] || t)
const getContractStatusClass = (s) => ({ DRAFT: 'tag tag-gray', PENDING_SIGN: 'tag tag-orange', SIGNED: 'tag tag-green', TERMINATED: 'tag tag-red', EXPIRED: 'tag tag-gray' }[s] || 'tag tag-gray')
const getContractStatusText = (s) => ({ DRAFT: '草稿', PENDING_SIGN: '待签署', SIGNED: '已签署', TERMINATED: '已终止', EXPIRED: '已过期' }[s] || s)

const fetchContracts = async () => {
  contractLoading.value = true
  try {
    const res = await request.get('/contract/list', { params: { page: 0, size: 100 } })
    if (res.code === 200) contracts.value = res.data.list || []
  } catch (e) { console.error('获取合同失败', e) }
  finally { contractLoading.value = false }
}

const fetchTemplates = async () => {
  templateLoading.value = true
  try {
    const res = await request.get('/contract/templates')
    if (res.code === 200) templates.value = res.data || []
  } catch (e) { console.error('获取模板失败', e) }
  finally { templateLoading.value = false }
}

const fetchOverview = async () => {
  try {
    const res = await request.get('/contract/overview')
    if (res.code === 200) overview.value = res.data
  } catch (e) { console.error('获取概览失败', e) }
}

const fetchMerchants = async () => {
  try {
    const res = await request.get('/merchant', { params: { page: 0, size: 1000 } })
    merchants.value = (res.data?.list || res.data || []).map(m => ({ id: m.id, merchantName: m.merchantName }))
  } catch (e) { console.error('获取商户失败', e) }
}

onMounted(async () => { await Promise.all([fetchContracts(), fetchTemplates(), fetchOverview(), fetchMerchants()]) })

const createContract = async () => {
  submitting.value = true
  try {
    const res = await request.post('/contract', newContract.value)
    if (res.code === 200) { alert('创建成功'); showCreateContract.value = false; await fetchContracts(); await fetchOverview() }
  } catch (e) { alert('创建失败') }
  finally { submitting.value = false }
}

const submitContract = async (c) => {
  try {
    const res = await request.put(`/contract/${c.id}/submit`)
    if (res.code === 200) { c.status = 'PENDING_SIGN'; alert('已提交签署') }
  } catch (e) { alert('操作失败') }
}

const platformSign = async (c) => {
  try {
    const res = await request.put(`/contract/${c.id}/platform-sign`, { signer: 'admin' })
    if (res.code === 200) { await fetchContracts() }
  } catch (e) { alert('签署失败') }
}

const viewContract = (c) => { alert(`合同详情：${c.contractTitle}\n编号：${c.contractCode}`) }

const saveTemplate = async () => {
  submitting.value = true
  try {
    if (editingTemplate.value) {
      const res = await request.put(`/contract/templates/${editingTemplate.value.id}`, templateForm.value)
      if (res.code === 200) { alert('更新成功'); showCreateTemplate.value = false; editingTemplate.value = null }
    } else {
      const res = await request.post('/contract/templates', templateForm.value)
      if (res.code === 200) { alert('创建成功'); showCreateTemplate.value = false }
    }
    await fetchTemplates(); await fetchOverview()
  } catch (e) { alert('保存失败') }
  finally { submitting.value = false }
}

const editTemplate = (t) => {
  editingTemplate.value = t
  templateForm.value = { templateName: t.templateName, templateType: t.templateType, content: t.content, variables: t.variables }
  showCreateTemplate.value = true
}

const toggleTemplate = async (t) => {
  const newStatus = t.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    const res = await request.put(`/contract/templates/${t.id}/toggle`, { status: newStatus })
    if (res.code === 200) { t.status = newStatus }
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
</style>