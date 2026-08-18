<template>
  <div class="container">
    <h1 class="page-title">🎯 招商线索管理</h1>

    <div class="grid-6">
      <div class="stat-card blue"><div class="stat-label">线索总数</div><div class="stat-value">{{ funnel.totalCount || 0 }}</div></div>
      <div class="stat-card" style="background:#e3f2fd"><div class="stat-label">新线索</div><div class="stat-value">{{ funnel.newCount || 0 }}</div></div>
      <div class="stat-card" style="background:#fff3e0"><div class="stat-label">接触中</div><div class="stat-value">{{ funnel.contactingCount || 0 }}</div></div>
      <div class="stat-card" style="background:#f3e5f5"><div class="stat-label">洽谈中</div><div class="stat-value">{{ funnel.negotiatingCount || 0 }}</div></div>
      <div class="stat-card green"><div class="stat-label">已转化</div><div class="stat-value">{{ funnel.convertedCount || 0 }}</div></div>
      <div class="stat-card red"><div class="stat-label">已丢失</div><div class="stat-value">{{ funnel.lostCount || 0 }}</div></div>
    </div>

    <!-- 转化漏斗 -->
    <div class="card">
      <div class="card-header"><h3>转化漏斗</h3></div>
      <div style="display:flex;align-items:center;justify-content:center;gap:8px;padding:20px 0">
        <template v-for="(stage, i) in funnelStages" :key="stage.key">
          <div style="text-align:center">
            <div :style="{ width: Math.max(60, stage.width) + 'px', height: Math.max(30, stage.width * 0.4) + 'px', background: stage.color, margin: '0 auto', borderRadius: '4px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 'bold', fontSize: '14px' }">{{ stage.count }}</div>
            <div style="font-size: 12px; color: #666; margin-top: 4px">{{ stage.label }}</div>
            <div v-if="stage.rate" style="font-size: 11px; color: #999">{{ stage.rate }}</div>
          </div>
          <div v-if="i < funnelStages.length - 1" style="color:#ccc;font-size:20px">→</div>
        </template>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>线索列表</h3>
        <div style="display:flex;gap:8px">
          <select v-model="filterStatus" style="padding:6px 12px;border-radius:4px;border:1px solid #ddd">
            <option value="">全部状态</option>
            <option value="NEW">新线索</option>
            <option value="CONTACTING">接触中</option>
            <option value="NEGOTIATING">洽谈中</option>
            <option value="INTENT_CONFIRMED">意向确认</option>
            <option value="CONVERTED">已转化</option>
            <option value="LOST">已丢失</option>
          </select>
          <input type="text" v-model="searchKeyword" placeholder="搜索企业/品牌" style="padding:6px 12px;border-radius:4px;border:1px solid #ddd;width:180px" @keyup.enter="fetchLeads" />
          <button class="btn btn-primary btn-sm" @click="showCreateLead = true">+ 新建线索</button>
        </div>
      </div>
      <table class="data-table">
        <thead>
          <tr><th>线索编号</th><th>企业名称</th><th>品牌</th><th>行业</th><th>联系人</th><th>来源</th><th>意向等级</th><th>预估GMV</th><th>状态</th><th>负责人</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-if="loading" class="text-center"><td colspan="11">加载中...</td></tr>
          <tr v-else-if="leads.length === 0" class="text-center"><td colspan="11">暂无线索</td></tr>
          <tr v-for="l in leads" :key="l.id">
            <td>{{ l.leadCode }}</td>
            <td>{{ l.companyName }}</td>
            <td>{{ l.brandName || '-' }}</td>
            <td>{{ l.industry || '-' }}</td>
            <td>{{ l.contactName || '-' }}<br><span style="font-size:11px;color:#999">{{ l.contactPhone || '' }}</span></td>
            <td><span class="tag tag-blue">{{ getSourceText(l.source) }}</span></td>
            <td><span :class="getIntentionClass(l.intentionLevel)">{{ getIntentionText(l.intentionLevel) }}</span></td>
            <td>{{ l.estimatedGmv ? '¥' + formatAmount(l.estimatedGmv) : '-' }}</td>
            <td><span :class="getStatusClass(l.status)">{{ getStatusText(l.status) }}</span></td>
            <td>{{ l.assignedTo || '-' }}</td>
            <td>
              <button class="btn btn-sm btn-outline" @click="viewLead(l)">详情</button>
              <button class="btn btn-sm btn-primary" @click="openFollowUp(l)">跟进</button>
              <button v-if="l.status !== 'CONVERTED' && l.status !== 'LOST'" class="btn btn-sm btn-success" @click="convertLead(l)">转化</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新建线索弹窗 -->
    <div v-if="showCreateLead" class="modal-overlay" @click.self="showCreateLead = false">
      <div class="modal" style="max-width:600px">
        <div class="modal-header"><h3>新建招商线索</h3><button class="modal-close" @click="showCreateLead = false">×</button></div>
        <div class="modal-body">
          <div class="grid-2">
            <div class="form-group"><label>企业名称 *</label><input type="text" v-model="newLead.companyName" placeholder="请输入企业名称" /></div>
            <div class="form-group"><label>品牌名称</label><input type="text" v-model="newLead.brandName" placeholder="品牌名称" /></div>
            <div class="form-group"><label>行业</label><input type="text" v-model="newLead.industry" placeholder="如：视频娱乐" /></div>
            <div class="form-group"><label>来源</label><select v-model="newLead.source"><option value="ACTIVE_MINING">主动挖掘</option><option value="REFERRAL">客户推荐</option><option value="EXHIBITION">展会活动</option><option value="ONLINE">线上渠道</option><option value="OTHER">其他</option></select></div>
            <div class="form-group"><label>联系人</label><input type="text" v-model="newLead.contactName" placeholder="联系人姓名" /></div>
            <div class="form-group"><label>联系电话</label><input type="text" v-model="newLead.contactPhone" placeholder="联系电话" /></div>
            <div class="form-group"><label>联系邮箱</label><input type="text" v-model="newLead.contactEmail" placeholder="联系邮箱" /></div>
            <div class="form-group"><label>意向等级</label><select v-model="newLead.intentionLevel"><option value="HIGH">高</option><option value="MEDIUM">中</option><option value="LOW">低</option></select></div>
            <div class="form-group"><label>预估GMV</label><input type="number" v-model.number="newLead.estimatedGmv" placeholder="预估年GMV" /></div>
            <div class="form-group"><label>负责人</label><input type="text" v-model="newLead.assignedTo" placeholder="指派负责人" /></div>
          </div>
          <div class="form-group"><label>备注</label><textarea v-model="newLead.remark" rows="2" placeholder="备注信息"></textarea></div>
        </div>
        <div class="modal-footer"><button class="btn btn-primary" @click="createLead" :disabled="submitting">创建线索</button></div>
      </div>
    </div>

    <!-- 线索详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal" style="max-width:700px">
        <div class="modal-header"><h3>线索详情 - {{ currentLead?.companyName }}</h3><button class="modal-close" @click="showDetail = false">×</button></div>
        <div class="modal-body">
          <div class="grid-2" v-if="currentLead">
            <div class="form-group"><label>线索编号</label><input type="text" :value="currentLead.leadCode" readonly /></div>
            <div class="form-group"><label>状态</label><input type="text" :value="getStatusText(currentLead.status)" readonly /></div>
            <div class="form-group"><label>企业名称</label><input type="text" :value="currentLead.companyName" readonly /></div>
            <div class="form-group"><label>品牌名称</label><input type="text" :value="currentLead.brandName || '-'" readonly /></div>
            <div class="form-group"><label>行业</label><input type="text" :value="currentLead.industry || '-'" readonly /></div>
            <div class="form-group"><label>来源</label><input type="text" :value="getSourceText(currentLead.source)" readonly /></div>
            <div class="form-group"><label>联系人</label><input type="text" :value="currentLead.contactName || '-'" readonly /></div>
            <div class="form-group"><label>联系电话</label><input type="text" :value="currentLead.contactPhone || '-'" readonly /></div>
            <div class="form-group"><label>意向等级</label><input type="text" :value="getIntentionText(currentLead.intentionLevel)" readonly /></div>
            <div class="form-group"><label>负责人</label><input type="text" :value="currentLead.assignedTo || '-'" readonly /></div>
          </div>
          <div style="margin-top:16px">
            <h4 style="font-size:14px;color:#333;margin-bottom:8px">跟进记录</h4>
            <div v-if="followUps.length === 0" style="color:#999;font-size:13px;text-align:center;padding:20px">暂无跟进记录</div>
            <div v-for="f in followUps" :key="f.id" style="padding:10px;background:#f5f7fa;border-radius:6px;margin-bottom:8px">
              <div style="font-size:12px;color:#999;margin-bottom:4px">{{ formatTime(f.createTime) }} | {{ getFollowTypeText(f.followType) }} | {{ f.followBy }}</div>
              <div style="font-size:13px;color:#333">{{ f.content }}</div>
              <div v-if="f.nextPlan" style="font-size:12px;color:#666;margin-top:4px">下一步：{{ f.nextPlan }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="openFollowUp(currentLead); showDetail = false">添加跟进</button>
        </div>
      </div>
    </div>

    <!-- 跟进弹窗 -->
    <div v-if="showFollowUp" class="modal-overlay" @click.self="showFollowUp = false">
      <div class="modal" style="max-width:500px">
        <div class="modal-header"><h3>添加跟进记录 - {{ followLead?.companyName }}</h3><button class="modal-close" @click="showFollowUp = false">×</button></div>
        <div class="modal-body">
          <div class="form-group"><label>跟进方式</label><select v-model="followForm.followType"><option value="PHONE">电话</option><option value="MEETING">会议</option><option value="EMAIL">邮件</option><option value="WECHAT">微信</option><option value="VISIT">拜访</option></select></div>
          <div class="form-group"><label>跟进内容</label><textarea v-model="followForm.content" rows="3" placeholder="详细描述跟进内容"></textarea></div>
          <div class="form-group"><label>下一步计划</label><textarea v-model="followForm.nextPlan" rows="2" placeholder="下一步行动计划"></textarea></div>
          <div class="form-group"><label>下次跟进时间</label><input type="datetime-local" v-model="followForm.nextFollowTime" /></div>
          <div class="form-group"><label>跟进人</label><input type="text" v-model="followForm.followBy" placeholder="跟进人" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-primary" @click="submitFollowUp" :disabled="submitting">保存</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'

const loading = ref(false), submitting = ref(false)
const leads = ref([]), funnel = ref({})
const filterStatus = ref(''), searchKeyword = ref('')
const showCreateLead = ref(false), showDetail = ref(false), showFollowUp = ref(false)
const currentLead = ref(null), followLead = ref(null), followUps = ref([])

const newLead = ref({
  companyName: '', brandName: '', industry: '', source: 'ACTIVE_MINING',
  contactName: '', contactPhone: '', contactEmail: '',
  intentionLevel: 'MEDIUM', estimatedGmv: 0, assignedTo: '', remark: ''
})

const followForm = ref({
  followType: 'PHONE', content: '', nextPlan: '',
  nextFollowTime: '', followBy: 'admin'
})

const funnelStages = computed(() => {
  const total = funnel.value.totalCount || 1
  const maxWidth = 180
  return [
    { key: 'new', label: '新线索', count: funnel.value.newCount || 0, color: '#2196f3', width: Math.max(40, (funnel.value.newCount || 0) / total * maxWidth), rate: '100%' },
    { key: 'contacting', label: '接触中', count: funnel.value.contactingCount || 0, color: '#ff9800', width: Math.max(40, (funnel.value.contactingCount || 0) / total * maxWidth), rate: calcRate(funnel.value.contactingCount, funnel.value.newCount) },
    { key: 'negotiating', label: '洽谈中', count: funnel.value.negotiatingCount || 0, color: '#9c27b0', width: Math.max(40, (funnel.value.negotiatingCount || 0) / total * maxWidth), rate: calcRate(funnel.value.negotiatingCount, funnel.value.contactingCount) },
    { key: 'intent', label: '意向确认', count: funnel.value.intentConfirmedCount || 0, color: '#00bcd4', width: Math.max(40, (funnel.value.intentConfirmedCount || 0) / total * maxWidth), rate: calcRate(funnel.value.intentConfirmedCount, funnel.value.negotiatingCount) },
    { key: 'converted', label: '已转化', count: funnel.value.convertedCount || 0, color: '#4caf50', width: Math.max(40, (funnel.value.convertedCount || 0) / total * maxWidth), rate: calcRate(funnel.value.convertedCount, funnel.value.intentConfirmedCount) },
  ]
})

const calcRate = (current, prev) => {
  if (!prev || prev === 0) return '-'
  return ((current / prev) * 100).toFixed(1) + '%'
}

const formatAmount = (v) => v ? Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0'
const formatTime = (t) => t ? t.substring(0, 16) : '-'

const getSourceText = (s) => ({ ACTIVE_MINING: '主动挖掘', REFERRAL: '客户推荐', EXHIBITION: '展会活动', ONLINE: '线上渠道', OTHER: '其他' }[s] || s)
const getIntentionClass = (l) => ({ HIGH: 'tag tag-red', MEDIUM: 'tag tag-orange', LOW: 'tag tag-blue' }[l] || 'tag tag-gray')
const getIntentionText = (l) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[l] || l)
const getStatusClass = (s) => ({ NEW: 'tag tag-blue', CONTACTING: 'tag tag-orange', NEGOTIATING: 'tag tag-purple', INTENT_CONFIRMED: 'tag tag-cyan', CONVERTED: 'tag tag-green', LOST: 'tag tag-gray' }[s] || 'tag tag-gray')
const getStatusText = (s) => ({ NEW: '新线索', CONTACTING: '接触中', NEGOTIATING: '洽谈中', INTENT_CONFIRMED: '意向确认', CONVERTED: '已转化', LOST: '已丢失' }[s] || s)
const getFollowTypeText = (t) => ({ PHONE: '电话', MEETING: '会议', EMAIL: '邮件', WECHAT: '微信', VISIT: '拜访' }[t] || t)

const fetchLeads = async () => {
  loading.value = true
  try {
    const params = { page: 0, size: 100 }
    if (filterStatus.value) params.status = filterStatus.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await request.get('/crm/leads', { params })
    if (res.code === 200) leads.value = res.data.list || []
  } catch (e) { console.error('获取线索失败', e) }
  finally { loading.value = false }
}

const fetchFunnel = async () => {
  try {
    const res = await request.get('/crm/funnel')
    if (res.code === 200) funnel.value = res.data
  } catch (e) { console.error('获取漏斗失败', e) }
}

onMounted(async () => { await Promise.all([fetchLeads(), fetchFunnel()]) })

const createLead = async () => {
  if (!newLead.value.companyName) { alert('请输入企业名称'); return }
  submitting.value = true
  try {
    const res = await request.post('/crm/leads', newLead.value)
    if (res.code === 200) { alert('创建成功'); showCreateLead.value = false; await fetchLeads(); await fetchFunnel() }
  } catch (e) { alert('创建失败') }
  finally { submitting.value = false }
}

const viewLead = async (l) => {
  currentLead.value = l
  try {
    const res = await request.get(`/crm/leads/${l.id}/follow-ups`)
    followUps.value = res.code === 200 ? (res.data || []) : []
  } catch (e) { followUps.value = [] }
  showDetail.value = true
}

const openFollowUp = (l) => {
  followLead.value = l
  followForm.value = { followType: 'PHONE', content: '', nextPlan: '', nextFollowTime: '', followBy: 'admin' }
  showFollowUp.value = true
}

const submitFollowUp = async () => {
  if (!followForm.value.content) { alert('请输入跟进内容'); return }
  submitting.value = true
  try {
    const res = await request.post(`/crm/leads/${followLead.value.id}/follow-ups`, followForm.value)
    if (res.code === 200) { alert('保存成功'); showFollowUp.value = false; await fetchLeads(); await fetchFunnel() }
  } catch (e) { alert('保存失败') }
  finally { submitting.value = false }
}

const convertLead = async (l) => {
  if (!confirm(`确认将「${l.companyName}」转化为已入驻商户？`)) return
  try {
    const res = await request.put(`/crm/leads/${l.id}/status`, { status: 'CONVERTED' })
    if (res.code === 200) { l.status = 'CONVERTED'; await fetchFunnel() }
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
.grid-6 { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin-bottom: 20px; }
.tag-purple { background: #f3e5f5; color: #7b1fa2; }
.tag-cyan { background: #e0f7fa; color: #00838f; }
</style>