<template>
  <div class="container">
    <h1 class="page-title">📝 合同签署</h1>
    <div class="card">
      <div class="card-header"><h3>待签署合同商户列表</h3></div>
      <el-table :data="merchants" style="width:100%" v-loading="loading">
        <el-table-column prop="merchantCode" label="商户编号" width="160"/>
        <el-table-column prop="merchantName" label="企业名称" min-width="200"/>
        <el-table-column prop="merchantType" label="类型" width="120">
          <template #default="{row}"><el-tag :type="getTypeTagType(row.merchantType)">{{getTypeText(row.merchantType)}}</el-tag></template>
        </el-table-column>
        <el-table-column label="审核节点" width="120"><template #default><el-tag type="info" size="small">合同签署</el-tag></template></el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{row}"><span :style="{color:isOverdue(row.auditNodeDeadline)?'red':''}">{{row.auditNodeDeadline?.substring(0,16)||'-'}}</span></template>
        </el-table-column>
        <el-table-column label="合同状态" width="110">
          <template #default="{row}"><el-tag :type="row.contractFile?'success':'info'" size="small">{{row.contractFile?'已上传':'未上传'}}</el-tag></template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120"/>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="success" size="small" @click="approveAudit(row)">签署完成</el-button>
            <el-button link type="danger" size="small" @click="rejectAudit(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" title="商户详情" width="850px">
      <el-tabs v-if="selected">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="商户编号" :span="2">{{selected.merchantCode||'-'}}</el-descriptions-item>
            <el-descriptions-item label="企业名称" :span="2">{{selected.merchantName||'-'}}</el-descriptions-item>
            <el-descriptions-item label="商户类型">{{getTypeText(selected.merchantType)}}</el-descriptions-item>
            <el-descriptions-item label="所属行业">{{selected.industry||'-'}}</el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{selected.creditCode||'-'}}</el-descriptions-item>
            <el-descriptions-item label="法人代表">{{selected.legalPerson||'-'}}</el-descriptions-item>
            <el-descriptions-item label="法人身份证号">{{selected.legalPersonId||'-'}}</el-descriptions-item>
            <el-descriptions-item label="注册资本">{{selected.registeredCapital||'-'}}</el-descriptions-item>
            <el-descriptions-item label="经营范围" :span="2">{{selected.businessScope||'-'}}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{selected.contactName||'-'}}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{selected.contactPhone||'-'}}</el-descriptions-item>
            <el-descriptions-item label="所在地区">{{[selected.province,selected.city,selected.district].filter(Boolean).join(' ')||'-'}}</el-descriptions-item>
            <el-descriptions-item label="详细地址">{{selected.address||'-'}}</el-descriptions-item>
            <el-descriptions-item label="开户银行">{{selected.bankName||'-'}}</el-descriptions-item>
            <el-descriptions-item label="银行账号">{{selected.bankAccount||'-'}}</el-descriptions-item>
            <el-descriptions-item label="税号">{{selected.taxNumber||'-'}}</el-descriptions-item>
            <el-descriptions-item label="商标注册号">{{selected.trademarkNo||'-'}}</el-descriptions-item>
            <el-descriptions-item label="授权链路">{{selected.authChain||'-'}}</el-descriptions-item>
            <el-descriptions-item label="品类匹配" :span="2">{{selected.categoryMatch||'-'}}</el-descriptions-item>
          </el-descriptions>
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
      <template #footer><el-button type="success" @click="approveAudit(selected)">签署完成</el-button><el-button type="danger" @click="rejectAudit(selected)">驳回</el-button></template>
    </el-dialog>

    <!-- 合同签署确认 -->
    <el-dialog v-model="showApprove" title="合同签署确认" width="650px">
      <template v-if="approving">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商户名称" :span="2">{{approving.merchantName}}</el-descriptions-item>
          <el-descriptions-item label="商户类型">{{getTypeText(approving.merchantType)}}</el-descriptions-item>
          <el-descriptions-item label="法人代表">{{approving.legalPerson||'-'}}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">上传合同文件</el-divider>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".pdf,.doc,.docx,.jpg,.png"
          v-model:file-list="contractFiles"
          drag
          style="margin-bottom:16px"
        >
          <el-icon class="el-icon--upload"><i class="upload-icon-text">📁</i></el-icon>
          <div class="el-upload__text">拖拽或<em>点击上传</em>签署合同</div>
          <template #tip><div class="el-upload__tip">支持 PDF/DOC/JPG/PNG，不超过 10MB</div></template>
        </el-upload>

        <!-- AI合同稽核按钮 -->
        <div style="margin-bottom:16px" v-if="contractFiles.length>0 || aiAuditResult">
          <el-button type="primary" @click="runAIAudit" :loading="aiAuditing" plain>🤖 AI合同稽核</el-button>
          <div v-if="aiAuditResult" style="margin-top:12px;padding:12px;background:#f5f7fa;border-radius:8px">
            <h4 style="margin:0 0 8px">AI稽核报告</h4>
            <el-table :data="aiAuditResult.items" size="small">
              <el-table-column prop="item" label="检查项"/>
              <el-table-column label="结果" width="100">
                <template #default="{row}"><el-tag :type="row.pass?'success':'danger'" size="small">{{row.pass?'通过':'异常'}}</el-tag></template>
              </el-table-column>
              <el-table-column prop="detail" label="详情"/>
            </el-table>
            <p style="margin:8px 0 0"><strong>总体评价：</strong>{{aiAuditResult.overall}}</p>
          </div>
        </div>

        <el-form label-position="top">
          <el-form-item label="签署备注"><el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="合同编号、签署日期等"/></el-form-item>
        </el-form>
        <el-alert type="warning" :closable="false" title="确认合同已签署完成（含AI质检），签署完成后将进入支付进件阶段。"/>
      </template>
      <template #footer><el-button type="success" @click="confirmApprove" :loading="submitting">确认完成</el-button></template>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showReject" :title="'驳回 '+rejecting?.merchantName" width="500px">
      <el-form label-position="top"><el-form-item label="驳回原因"><el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请填写驳回原因（必填）"/></el-form-item></el-form>
      <template #footer><el-button type="danger" @click="confirmReject" :loading="submitting">确认驳回</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading=ref(false),submitting=ref(false),showDetail=ref(false),showApprove=ref(false),showReject=ref(false)
const merchants=ref([]),selected=ref(null),approving=ref(null),rejecting=ref(null)
const approveForm=ref({comment:''}),rejectForm=ref({reason:''})
const contractFiles=ref([]),aiAuditing=ref(false),aiAuditResult=ref(null),auditLogs=ref([]),logLoading=ref(false)

const fetch=async()=>{loading.value=true;try{const r=await request.get('/merchant/node/CONTRACT');if(r.code===200)merchants.value=r.data||[]}finally{loading.value=false}}

const getTypeText=t=>({DIGITAL:'数字权益',PHYSICAL:'实物商品',LOCAL_LIFE:'本地生活'}[t]||t||'-')
const getTypeTagType=t=>({DIGITAL:'primary',PHYSICAL:'success',LOCAL_LIFE:'warning'}[t]||'info')
const isOverdue=d=>d?new Date(d)<new Date():false

const viewDetail=r=>{selected.value=r;fetchAuditLogs(r.id);showDetail.value=true}
const approveAudit=r=>{approving.value=r;approveForm.value.comment='';contractFiles.value=[];aiAuditResult.value=null;showApprove.value=true}

// AI合同稽核
const runAIAudit=async()=>{
  aiAuditing.value=true
  aiAuditResult.value=null
  await new Promise(r=>setTimeout(r,1200))
  aiAuditResult.value={
    overall:'合同稽核通过，未发现合规风险（Mock）',
    items:[
      {item:'甲乙方信息一致性',pass:true,detail:'签约主体与营业执照一致'},
      {item:'合同金额规范性',pass:true,detail:'金额与费率条款合规'},
      {item:'关键条款完整性',pass:true,detail:'包含保密、违约、争议解决条款'},
      {item:'签约日期有效期',pass:true,detail:'有效期在营业执照经营期限内'},
      {item:'盖章签字完整性',pass:false,detail:'乙方签章处模糊，建议核实'}
    ]
  }
  aiAuditing.value=false
}

const confirmApprove=async()=>{
  submitting.value=true
  try{
    await request.put(`/merchant/${approving.value.id}/audit`,{auditStatus:'APPROVED',auditNode:'CONTRACT',comment:approveForm.value.comment})
    ElMessage.success('合同签署完成！已进入支付进件');showApprove.value=false;showDetail.value=false;await fetch()
  }catch{ElMessage.error('操作失败，请稍后重试');showApprove.value=false;await fetch()}finally{submitting.value=false}
}

const rejectAudit=r=>{rejecting.value=r;rejectForm.value.reason='';showReject.value=true}
const confirmReject=async()=>{if(!rejectForm.value.reason.trim()){ElMessage.warning('请填写驳回原因');return}submitting.value=true;try{await request.put(`/merchant/${rejecting.value.id}/audit`,{auditStatus:'REJECTED',rejectReason:rejectForm.value.reason});ElMessage.success('驳回成功！');showReject.value=false;await fetch()}catch{ElMessage.error('操作失败，请稍后重试');showReject.value=false;await fetch()}finally{submitting.value=false}}

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
  const m = { QUALIFICATION:'资质初审', BUSINESS:'业务复审', COMPLIANCE:'合规终审', CONTRACT:'合同签署', PAYMENT:'支付进件', PRODUCT:'商品录入', COMPLETED:'已完成' }
  return m[n] || n || '-'
}

onMounted(()=>{fetch()})
</script>

<style scoped>
.container{padding:20px}.page-title{margin-bottom:24px;color:#333;font-size:24px;font-weight:600}.card{background:#fff;border-radius:8px;box-shadow:0 2px 12px 0 rgba(0,0,0,.1);padding:20px;margin-bottom:20px}.card-header{display:flex;justify-content:space-between;align-items:center;padding-bottom:16px;border-bottom:1px solid #f0f0f0;margin-bottom:16px}.card-header h3{margin:0;color:#333;font-size:16px}.upload-icon-text{font-size:28px}
</style>
