<template>
  <div class="container">
    <h1 class="page-title">📋 业务复审</h1>

    <div class="card">
      <div class="card-header">
        <h3>待复审商户列表</h3>
      </div>

      <el-table :data="merchants" style="width: 100%" v-loading="loading">
        <el-table-column prop="merchantCode" label="商户编号" width="160" />
        <el-table-column prop="merchantName" label="企业名称" min-width="200" />
        <el-table-column prop="merchantType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.merchantType)">{{ getTypeText(row.merchantType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前审核节点" width="140">
          <template #default>
            <el-tag type="warning" size="small">业务复审</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{ row }">
            <span v-if="row.auditNodeDeadline" :style="{ color: isOverdue(row.auditNodeDeadline) ? 'red' : '' }">
              {{ row.auditNodeDeadline?.substring(0, 16) || '-' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" size="small">{{ row.riskLevel || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信用评分" width="90">
          <template #default="{ row }">
            <span :style="{ color: row.creditScore >= 80 ? 'green' : row.creditScore >= 60 ? 'orange' : 'red' }">
              {{ row.creditScore || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="success" size="small" @click="approveAudit(row)">通过</el-button>
            <el-button link type="danger" size="small" @click="rejectAudit(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="商户详情" width="850px">
      <el-tabs v-if="selectedMerchant" v-model="detailTab">
        <el-tab-pane label="基本信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="商户编号" :span="2">{{ selectedMerchant.merchantCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="企业名称" :span="2">{{ selectedMerchant.merchantName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商户类型">{{ getTypeText(selectedMerchant.merchantType) }}</el-descriptions-item>
            <el-descriptions-item label="所属行业">{{ selectedMerchant.industry || '-' }}</el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{ selectedMerchant.creditCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="法人代表">{{ selectedMerchant.legalPerson || '-' }}</el-descriptions-item>
            <el-descriptions-item label="法人身份证">{{ selectedMerchant.legalPersonId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册资本">{{ selectedMerchant.registeredCapital || '-' }}</el-descriptions-item>
            <el-descriptions-item label="经营范围" :span="2">{{ selectedMerchant.businessScope || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商标注册号">{{ selectedMerchant.trademarkNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="授权链路">{{ selectedMerchant.authChain || '-' }}</el-descriptions-item>
            <el-descriptions-item label="品类匹配" :span="2">{{ selectedMerchant.categoryMatch || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ selectedMerchant.contactName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ selectedMerchant.contactPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所在地区">{{ [selectedMerchant.province,selectedMerchant.city,selectedMerchant.district].filter(Boolean).join(' ') || '-' }}</el-descriptions-item>
            <el-descriptions-item label="详细地址">{{ selectedMerchant.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="开户银行">{{ selectedMerchant.bankName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="银行账号">{{ selectedMerchant.bankAccount || '-' }}</el-descriptions-item>
            <el-descriptions-item label="税号">{{ selectedMerchant.taxNumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag :type="getRiskTagType(selectedMerchant.riskLevel)">{{ selectedMerchant.riskLevel || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="信用评分">{{ selectedMerchant.creditScore || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审核节点">
              <el-tag type="warning" size="small">业务复审</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="截止时间">{{ selectedMerchant.auditNodeDeadline?.substring(0, 16) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="入驻状态">
              <el-tag type="warning">{{ selectedMerchant.onboardingStatus || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请日期">{{ selectedMerchant.createTime || '-' }}</el-descriptions-item>
          </el-descriptions>
          <!-- 审核轨迹（横向节点布局） -->
          <div class="audit-trail">
            <div class="trail-title">📋 审核轨迹</div>
            <div class="trail-track">
              <div v-for="(node, idx) in auditTrailNodes" :key="node.key" class="trail-node-wrapper">
                <div class="trail-node" :class="node.status">
                  <div class="trail-dot" :class="node.status">
                    <span v-if="node.status==='done'">✓</span>
                    <span v-else-if="node.status==='active'">●</span>
                    <span v-else>○</span>
                  </div>
                  <div class="trail-label">{{ node.label }}</div>
                  <div class="trail-time">{{ node.time || '-' }}</div>
                  <div v-if="node.reason" class="trail-reason" :title="node.reason">{{ node.reason }}</div>
                </div>
                <div v-if="idx < auditTrailNodes.length - 1" class="trail-line" :class="node.status"></div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="审核记录" name="auditLog">
          <div v-loading="logLoading">
            <el-timeline v-if="auditLogs.length>0">
              <el-timeline-item v-for="log in auditLogs" :key="log.id" :timestamp="formatTime(log.createTime)" :color="log.action==='APPROVED'?'#67c23a':log.action==='RESUBMIT'?'#409eff':'#f56c6c'" placement="top">
                <el-card shadow="hover">
                  <p><strong>{{log.action==='RESUBMIT'?'重新提交':getAuditNodeText(log.auditNode)}}</strong> - <el-tag :type="log.action==='APPROVED'?'success':log.action==='RESUBMIT'?'primary':'danger'" size="small">{{log.action==='APPROVED'?'通过':log.action==='RESUBMIT'?'重新提交':'驳回'}}</el-tag></p>
                  <p v-if="log.comment">说明：{{log.comment}}</p>
                  <p v-if="log.rejectReason">驳回原因：{{log.rejectReason}}</p>
                  <p>操作人：{{log.operator||'-'}}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无审核记录"/>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button type="success" @click="approveAudit(selectedMerchant)">复审通过</el-button>
        <el-button type="danger" @click="rejectAudit(selectedMerchant)">驳回</el-button>
      </template>
    </el-dialog>

    <!-- 复审通过确认 -->
    <el-dialog v-model="showApproveDialog" title="业务复审确认" width="500px">
      <el-descriptions :column="1" border v-if="approvingMerchant">
        <el-descriptions-item label="商户名称">{{ approvingMerchant.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商户类型">{{ getTypeText(approvingMerchant.merchantType) }}</el-descriptions-item>
        <el-descriptions-item label="所属行业">{{ approvingMerchant.industry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品类匹配">{{ approvingMerchant.categoryMatch || '-' }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getRiskTagType(approvingMerchant.riskLevel)">{{ approvingMerchant.riskLevel || '-' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-form :model="approveForm" label-position="top" style="margin-top:16px">
        <el-form-item label="复审意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入复审意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="success" @click="confirmApprove" :loading="submitLoading">确认通过</el-button>
      </template>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showRejectDialog" :title="`驳回 ${rejectingMerchant?.merchantName}`" width="500px">
      <el-form :model="rejectForm" label-position="top">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入驳回原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmReject" :loading="submitLoading">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const showDetailDialog = ref(false)
const showApproveDialog = ref(false)
const showRejectDialog = ref(false)
const merchants = ref([])
const selectedMerchant = ref(null)
const approvingMerchant = ref(null)
const rejectingMerchant = ref(null)
const approveForm = ref({ comment: '' })
const rejectForm = ref({ reason: '' })
const auditLogs = ref([])
const detailTab = ref('basic')
const logLoading = ref(false)

// 审核轨迹节点定义
const auditNodes = [
  { key: 'SUBMIT', label: '商品入驻提交', order: 0 },
  { key: 'QUALIFICATION', label: '资质初审', order: 1 },
  { key: 'BUSINESS', label: '业务复审', order: 2 },
  { key: 'COMPLIANCE', label: '合规终审', order: 3 },
  { key: 'CONTRACT', label: '合同签署', order: 4 },
  { key: 'PAYMENT', label: '支付进件', order: 5 },
  { key: 'COMPLETED', label: '已完成', order: 6 },
]

const auditTrailNodes=computed(()=>{if(!selectedMerchant.value)return auditNodes.map(n=>({...n,status:'pending',time:'',reason:''}));const m=selectedMerchant.value;const cn=m.auditNode||'BUSINESS';const lt={};(auditLogs.value||[]).forEach(l=>{if(l.auditNode)lt[l.auditNode]=formatTime(l.createTime)});const ci=auditNodes.findIndex(n=>n.key===cn);return auditNodes.map((n,i)=>{let s='pending',t='';if(n.key==='SUBMIT'){s='done';t=m.createTime?formatTime(m.createTime):''}else if(n.key==='COMPLETED'){if(m.onboardingStatus==='APPROVED')s='done';t=lt[n.key]||''}else{if(i<ci){s='done';t=lt[n.key]||''}else if(i===ci){s='active';t=''}}return{...n,status:s,time:t,reason:''}})})

const fetchMerchants = async () => {
  loading.value = true
  try {
    const res = await request.get('/merchant/business-audit')
    if (res.code === 200) {
      merchants.value = res.data || []
    }
  } finally { loading.value = false }
}

const getTypeText = (type) => {
  const map = { DIGITAL: '数字权益', PHYSICAL: '实物商品', LOCAL_LIFE: '本地生活' }
  return map[type] || type || '-'
}
const getTypeTagType = (type) => {
  const map = { DIGITAL: 'primary', PHYSICAL: 'success', LOCAL_LIFE: 'warning' }
  return map[type] || 'info'
}
const getRiskTagType = (level) => {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }
  return map[level] || 'info'
}
const isOverdue = (deadline) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

const viewDetail = (row) => {
  selectedMerchant.value = row
  detailTab.value = 'basic'
  fetchAuditLogs(row.id)
  showDetailDialog.value = true
}

const approveAudit = (row) => {
  approvingMerchant.value = row
  approveForm.value.comment = ''
  showApproveDialog.value = true
}

const confirmApprove = async () => {
  submitLoading.value = true
  try {
    const res = await request.put(`/merchant/${approvingMerchant.value.id}/audit`, {
      auditStatus: 'APPROVED',
      auditNode: 'BUSINESS'
    })
    if (res.code === 200) {
      ElMessage.success('业务复审通过！已推进至合规终审')
      showApproveDialog.value = false
      showDetailDialog.value = false
      await fetchMerchants()
    }
  } catch {
    ElMessage.error('操作失败，请稍后重试')
    showApproveDialog.value = false
    showDetailDialog.value = false
    await fetchMerchants()
  } finally { submitLoading.value = false }
}

const rejectAudit = (row) => {
  rejectingMerchant.value = row
  rejectForm.value.reason = ''
  showRejectDialog.value = true
}

const confirmReject = async () => {
  if (!rejectForm.value.reason.trim()) { ElMessage.warning('请输入驳回原因'); return }
  submitLoading.value = true
  try {
    const res = await request.put(`/merchant/${rejectingMerchant.value.id}/audit`, {
      auditStatus: 'REJECTED',
      rejectReason: rejectForm.value.reason
    })
    if (res.code === 200) {
      ElMessage.success('驳回成功！')
      showRejectDialog.value = false
      await fetchMerchants()
    }
  } catch {
    ElMessage.error('操作失败，请稍后重试')
    showRejectDialog.value = false
    await fetchMerchants()
  } finally { submitLoading.value = false }
}

onMounted(() => { fetchMerchants() })

const fetchAuditLogs = async (merchantId) => {
  logLoading.value = true
  try {
    const r = await request.get(`/merchant/${merchantId}/audit-logs`)
    auditLogs.value = r.code === 200 ? (r.data || []) : []
  } catch { auditLogs.value = [] }
  finally { logLoading.value = false }
}

const formatTime = (t) => t ? t.substring(0, 16) : '-'

const getAuditNodeText = n => {
  const m = { SUBMIT:'商品入驻提交', QUALIFICATION:'资质初审', BUSINESS:'业务复审', COMPLIANCE:'合规终审', CONTRACT:'合同签署', PAYMENT:'支付进件', COMPLETED:'已完成' }
  return m[n] || n || '-'
}
</script>

<style scoped>
.container { padding: 20px; }
.page-title { margin-bottom: 24px; color: #333; font-size: 24px; font-weight: 600; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1); padding: 20px; margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-bottom: 16px; }
.card-header h3 { margin: 0; color: #333; font-size: 16px; }

/* 审核轨迹样式 */
.audit-trail{margin-top:20px;padding:16px;background:#fafafa;border-radius:8px;border:1px solid #ebeef5}
.trail-title{font-size:14px;font-weight:600;color:#333;margin-bottom:16px}
.trail-track{display:flex;align-items:flex-start;justify-content:space-between;position:relative}
.trail-node-wrapper{flex:1;display:flex;align-items:flex-start;position:relative}
.trail-node{display:flex;flex-direction:column;align-items:center;gap:6px;flex:1}
.trail-dot{width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:700;border:2px solid #ddd;background:#fff;color:#999}
.trail-dot.done{background:#67c23a;border-color:#67c23a;color:#fff}
.trail-dot.active{background:#409eff;border-color:#409eff;color:#fff;animation:pulse 1.5s infinite}
.trail-dot.pending{background:#f5f5f5;border-color:#ddd;color:#ccc}
.trail-label{font-size:12px;color:#333;font-weight:500;text-align:center;white-space:nowrap}
.trail-time{font-size:10px;color:#999;text-align:center}
.trail-line{flex:1;height:3px;background:#e0e0e0;margin-top:16px;min-width:20px}
.trail-line.done{background:#67c23a}
.trail-line.active{background:linear-gradient(90deg,#409eff,#e0e0e0)}
@keyframes pulse{0%,100%{box-shadow:0 0 0 0 rgba(64,158,255,.4)}50%{box-shadow:0 0 0 6px rgba(64,158,255,0)}}
.trail-dot.rejected{border-color:#f56c6c;background:#f56c6c;color:#fff}
.trail-reason{font-size:10px;color:#f56c6c;margin-top:2px;max-width:90px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.trail-node.rejected .trail-label{color:#f56c6c}
.trail-line.rejected{background:#f56c6c}
/* 重新提交节点样式 */
.trail-node.resubmit .trail-dot{border-color:#409eff;background:#409eff;color:#fff}
.trail-node.resubmit .trail-label{color:#409eff}
.trail-line.resubmit{background:#409eff}
</style>
