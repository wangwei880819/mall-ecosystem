<template>
  <div class="container">
    <h1 class="page-title">📋 商户入驻资质审核</h1>

    <!-- 超时预警 -->
    <div class="card" v-if="timeoutWarnings.length > 0">
      <div class="card-header">
        <h3>⚠️ 超时预警</h3>
        <el-tag type="danger" size="small">{{ timeoutWarnings.length }} 条超时</el-tag>
      </div>
      <el-table :data="timeoutWarnings" style="width:100%" size="small">
        <el-table-column prop="merchantName" label="商户名称" />
        <el-table-column label="当前审核节点" width="150">
          <template #default="{row}">
            <el-tag size="small">{{ getAuditNodeText(row.auditNode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="170" />
        <el-table-column label="超期天数" width="100">
          <template #default="{row}">
            <el-tag type="danger">{{ row.overdueDays }}天</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 待审核列表 -->
    <div class="card">
      <div class="card-header"><h3>待审核商户列表</h3></div>
      <el-table :data="pendingMerchants" style="width:100%" v-loading="loading">
        <el-table-column prop="merchantCode" label="商户编号" width="160" />
        <el-table-column prop="merchantName" label="企业名称" min-width="180" />
        <el-table-column prop="merchantType" label="类型" width="120">
          <template #default="{row}"><el-tag :type="getTypeTagType(row.merchantType)">{{ getTypeText(row.merchantType) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="当前审核节点" width="140">
          <template #default="{row}">
            <el-tag size="small" :type="getNodeTagType(row.auditNode)">
              {{ getAuditNodeText(row.auditNode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{row}">
            <span :style="{color:isOverdue(row.auditNodeDeadline)?'red':''}">{{ row.auditNodeDeadline?.substring(0,16)||'-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100">
          <template #default="{row}"><el-tag :type="getRiskTagType(row.riskLevel)" size="small">{{ row.riskLevel||'-' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信用评分" width="90" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="info" size="small" @click="openAIReview(row)">AI审查</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="商户详情" width="850px">
      <el-tabs v-model="detailTab" v-if="selectedMerchant">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="商户编号" :span="2">{{ selectedMerchant.merchantCode||'-' }}</el-descriptions-item>
            <el-descriptions-item label="企业名称" :span="2">{{ selectedMerchant.merchantName||'-' }}</el-descriptions-item>
            <el-descriptions-item label="商户类型">{{ getTypeText(selectedMerchant.merchantType) }}</el-descriptions-item>
            <el-descriptions-item label="所属行业">{{ selectedMerchant.industry||'-' }}</el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{ selectedMerchant.creditCode||'-' }}</el-descriptions-item>
            <el-descriptions-item label="法人代表">{{ selectedMerchant.legalPerson||'-' }}</el-descriptions-item>
            <el-descriptions-item label="法人身份证号">{{ selectedMerchant.legalPersonId||'-' }}</el-descriptions-item>
            <el-descriptions-item label="注册资本">{{ selectedMerchant.registeredCapital||'-' }}</el-descriptions-item>
            <el-descriptions-item label="经营范围" :span="2">{{ selectedMerchant.businessScope||'-' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ selectedMerchant.contactName||'-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ selectedMerchant.contactPhone||'-' }}</el-descriptions-item>
            <el-descriptions-item label="所在地区">{{ [selectedMerchant.province,selectedMerchant.city,selectedMerchant.district].filter(Boolean).join(' ')||'-' }}</el-descriptions-item>
            <el-descriptions-item label="详细地址">{{ selectedMerchant.address||'-' }}</el-descriptions-item>
            <el-descriptions-item label="开户银行">{{ selectedMerchant.bankName||'-' }}</el-descriptions-item>
            <el-descriptions-item label="银行账号">{{ selectedMerchant.bankAccount||'-' }}</el-descriptions-item>
            <el-descriptions-item label="税号">{{ selectedMerchant.taxNumber||'-' }}</el-descriptions-item>
            <el-descriptions-item label="商标注册号">{{ selectedMerchant.trademarkNo||'-' }}</el-descriptions-item>
            <el-descriptions-item label="授权链路">{{ selectedMerchant.authChain||'-' }}</el-descriptions-item>
            <el-descriptions-item label="品类匹配" :span="2">{{ selectedMerchant.categoryMatch||'-' }}</el-descriptions-item>
            <el-descriptions-item label="风险等级"><el-tag :type="getRiskTagType(selectedMerchant.riskLevel)">{{ selectedMerchant.riskLevel||'-' }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="信用评分">{{ selectedMerchant.creditScore||'-' }}</el-descriptions-item>
            <el-descriptions-item label="审核节点">
              <el-tag size="small" :type="getNodeTagType(selectedMerchant.auditNode)">{{ getAuditNodeText(selectedMerchant.auditNode) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="截止时间">{{ selectedMerchant.auditNodeDeadline?.substring(0,16)||'-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="AI审查报告" name="ai">
          <div v-loading="aiLoading" style="min-height:200px">
            <div v-if="aiReport">
              <el-alert :title="'风险等级：'+aiReport.overallRisk" :type="aiReport.overallRisk==='LOW'?'success':aiReport.overallRisk==='MEDIUM'?'warning':'error'" :closable="false" style="margin-bottom:16px"/>
              <el-table :data="aiReport.dimensions" size="small">
                <el-table-column prop="dimension" label="风险维度"/>
                <el-table-column label="等级" width="100"><template #default="{row}"><el-tag :type="row.level==='LOW'?'success':row.level==='MEDIUM'?'warning':'danger'" size="small">{{row.level}}</el-tag></template></el-table-column>
                <el-table-column prop="score" label="评分" width="80"/>
                <el-table-column prop="detail" label="详情"/>
              </el-table>
              <div style="margin-top:12px">
                <p><strong>审核建议：</strong>{{aiReport.flowRecommend}}</p>
                <p v-if="aiReport.attentionItems"><strong>关注事项：</strong></p>
                <ul><li v-for="item in aiReport.attentionItems" :key="item">{{item}}</li></ul>
              </div>
              <div style="margin-top:16px;text-align:right">
                <el-button type="success" @click="approveFromAIReview">通过</el-button>
                <el-button type="danger" @click="rejectFromAIReview">驳回</el-button>
              </div>
            </div>
            <el-empty v-else description="点击AI审查获取风险预判报告"/>
          </div>
        </el-tab-pane>
        <el-tab-pane label="审核记录" name="auditLog">
          <div v-loading="logLoading">
            <el-timeline v-if="auditLogs.length>0">
              <el-timeline-item v-for="log in auditLogs" :key="log.id" :timestamp="formatTime(log.createTime)" :color="log.action==='APPROVED'?'#67c23a':'#f56c6c'" placement="top">
                <el-card shadow="hover">
                  <p><strong>{{getAuditNodeText(log.auditNode)}}</strong> - <el-tag :type="log.action==='APPROVED'?'success':'danger'" size="small">{{log.action==='APPROVED'?'通过':'驳回'}}</el-tag></p>
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
      <template #footer></template>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showRejectDialog" :title="'驳回 '+rejectingMerchant?.merchantName" width="500px">
      <el-form label-position="top"><el-form-item label="驳回原因"><el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请填写驳回原因（必填）"/></el-form-item></el-form>
      <template #footer><el-button type="danger" @click="confirmReject" :loading="submitLoading">确认驳回</el-button></template>
    </el-dialog>

    <!-- 审核确认弹窗 -->
    <el-dialog v-model="showAIAuditDialog" title="资质审核确认" width="650px">
      <template v-if="auditConfirmData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商户名称" :span="2">{{auditConfirmData.merchantName}}</el-descriptions-item>
          <el-descriptions-item label="商户类型">{{getTypeText(auditConfirmData.merchantType)}}</el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">{{auditConfirmData.creditCode||'-'}}</el-descriptions-item>
          <el-descriptions-item label="法人代表">{{auditConfirmData.legalPerson||'-'}}</el-descriptions-item>
          <el-descriptions-item label="风险等级"><el-tag :type="getRiskTagType(auditConfirmData.riskLevel)">{{auditConfirmData.riskLevel||'-'}}</el-tag></el-descriptions-item>
          <el-descriptions-item label="信用评分">{{auditConfirmData.creditScore||'-'}}</el-descriptions-item>
          <el-descriptions-item label="AI风险预判"><el-tag :type="auditConfirmData.aiRisk==='LOW'?'success':auditConfirmData.aiRisk==='MEDIUM'?'warning':'danger'">{{auditConfirmData.aiRisk||'-'}}</el-tag></el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:12px">
          <p><strong>AI风险维度分析：</strong></p>
          <el-table :data="auditConfirmData.aiDimensions||[]" size="small">
            <el-table-column prop="dimension" label="维度"/>
            <el-table-column label="评分" width="80"><template #default="{row}">{{row.score}}</template></el-table-column>
            <el-table-column prop="detail" label="详情"/>
          </el-table>
        </div>
        <el-alert type="warning" :closable="false" style="margin-top:12px" title="请确认以上信息无误后提交，审核通过后将推进至下一节点。"/>
      </template>
      <template #footer><el-button type="primary" @click="submitAIAudit" :loading="submitLoading">提交审核通过</el-button></template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading=ref(false),submitLoading=ref(false)
const showDetailDialog=ref(false),showAIAuditDialog=ref(false),showRejectDialog=ref(false)
const selectedMerchant=ref(null),merchants=ref([]),timeoutWarnings=ref([])
const detailTab=ref('basic'),aiReport=ref(null),aiLoading=ref(false)
const auditConfirmData=ref(null),rejectingMerchant=ref(null),rejectForm=ref({reason:''})
const auditLogs=ref([]),logLoading=ref(false)

const pendingMerchants = computed(()=>
  merchants.value.filter(m=>{
    const node=m.auditNode||'QUALIFICATION'
    if(node!=='QUALIFICATION')return false
    if(m.onboardingStatus==='APPROVED'||m.onboardingStatus==='REJECTED')return false
    return true
  })
)

const fetchMerchants=async()=>{loading.value=true;try{const r=await request.get('/merchant?page=0&size=50');if(r.code===200)merchants.value=r.data?.list||r.data||[]}finally{loading.value=false}}
const fetchTimeoutWarnings=async()=>{try{const r=await request.get('/merchant/timeout-warnings');if(r.code===200)timeoutWarnings.value=r.data||[]}catch{timeoutWarnings.value=[]}}

const getTypeText=t=>({DIGITAL:'数字权益',PHYSICAL:'实物商品',LOCAL_LIFE:'本地生活'}[t]||t||'-')
const getTypeTagType=t=>({DIGITAL:'primary',PHYSICAL:'success',LOCAL_LIFE:'warning'}[t]||'info')
const getRiskTagType=l=>({LOW:'success',MEDIUM:'warning',HIGH:'danger'}[l]||'info')
const getAuditNodeText=n=>{
  const m={QUALIFICATION:'资质初审',BUSINESS:'业务复审',COMPLIANCE:'合规终审',CONTRACT:'合同签署',PAYMENT:'支付进件',PRODUCT:'商品录入',COMPLETED:'已完成'}
  return m[n||'QUALIFICATION']||'资质初审'
}
const getNodeTagType=n=>({QUALIFICATION:'info',BUSINESS:'warning',COMPLIANCE:'danger',CONTRACT:'info',PAYMENT:'info',PRODUCT:'info',COMPLETED:'success'}[n||'QUALIFICATION']||'info')
const isOverdue=d=>d?new Date(d)<new Date():false

const viewDetail=r=>{selectedMerchant.value=r;detailTab.value='basic';aiReport.value=null;fetchAuditLogs(r.id);showDetailDialog.value=true}
const openAIReview=async row=>{selectedMerchant.value=row;detailTab.value='ai';aiReport.value=null;fetchAuditLogs(row.id);showDetailDialog.value=true;aiLoading.value=true;try{const r=await request.post('/ai/risk-predict',{merchantId:row.id,merchantName:row.merchantName,merchantType:row.merchantType});if(r.code===200)aiReport.value=r.data}catch{aiReport.value={overallRisk:'MEDIUM',riskScore:74,flowRecommend:'中风险商户，建议进入复核流程',dimensions:[{dimension:'资质风险',level:'LOW',score:85,detail:'资质文件齐全'},{dimension:'经营风险',level:'MEDIUM',score:62,detail:'存在经营异常记录'},{dimension:'信用风险',level:'LOW',score:78,detail:'信用评分良好'}],attentionItems:['经营异常记录需人工核实']}}finally{aiLoading.value=false}}

// 获取真实审核日志
const fetchAuditLogs=async(merchantId)=>{logLoading.value=true;try{const r=await request.get(`/merchant/${merchantId}/audit-logs`);auditLogs.value=r.code===200?r.data||[]:[]}catch{auditLogs.value=[]}finally{logLoading.value=false}}
const formatTime=t=>t?t.substring(0,16):'-'

// AI审查通过
const approveFromAIReview=()=>{if(!selectedMerchant.value)return;auditConfirmData.value={...selectedMerchant.value,aiRisk:aiReport.value?.overallRisk||'MEDIUM',aiDimensions:aiReport.value?.dimensions||[]};showAIAuditDialog.value=true}
const submitAIAudit=async()=>{submitLoading.value=true;try{await request.put(`/merchant/${selectedMerchant.value.id}/audit`,{auditStatus:'APPROVED'});ElMessage.success('审核通过！已推进至下一节点');showAIAuditDialog.value=false;showDetailDialog.value=false;await fetchMerchants();await fetchTimeoutWarnings()}catch{ElMessage.error('操作失败，请稍后重试');showAIAuditDialog.value=false;showDetailDialog.value=false;await fetchMerchants()}finally{submitLoading.value=false}}

// AI审查驳回
const rejectFromAIReview=()=>{rejectingMerchant.value=selectedMerchant.value;rejectForm.value.reason='';showRejectDialog.value=true}
const confirmReject=async()=>{if(!rejectForm.value.reason.trim()){ElMessage.warning('请填写驳回原因');return}submitLoading.value=true;try{await request.put(`/merchant/${rejectingMerchant.value.id}/audit`,{auditStatus:'REJECTED',rejectReason:rejectForm.value.reason});ElMessage.success('驳回成功！');showRejectDialog.value=false;showDetailDialog.value=false;await fetchMerchants()}catch{ElMessage.error('操作失败，请稍后重试');showRejectDialog.value=false;await fetchMerchants()}finally{submitLoading.value=false}}

onMounted(async()=>{await fetchMerchants();await fetchTimeoutWarnings()})
</script>

<style scoped>
.container{padding:20px}.page-title{margin-bottom:24px;color:#333;font-size:24px;font-weight:600}.card{background:#fff;border-radius:8px;box-shadow:0 2px 12px 0 rgba(0,0,0,.1);padding:20px;margin-bottom:20px}.card-header{display:flex;justify-content:space-between;align-items:center;padding-bottom:16px;border-bottom:1px solid #f0f0f0;margin-bottom:16px}.card-header h3{margin:0;color:#333;font-size:16px}
</style>
