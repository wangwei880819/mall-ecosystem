<template>
  <div class="container">
    <h1 class="page-title">💳 支付进件</h1>
    <div class="card">
      <div class="card-header"><h3>待进件商户列表</h3></div>
      <el-table :data="merchants" style="width:100%" v-loading="loading">
        <el-table-column prop="merchantCode" label="商户编号" width="160"/>
        <el-table-column prop="merchantName" label="企业名称" min-width="200"/>
        <el-table-column prop="merchantType" label="类型" width="120"><template #default="{row}"><el-tag :type="getTypeTagType(row.merchantType)">{{getTypeText(row.merchantType)}}</el-tag></template></el-table-column>
        <el-table-column label="审核节点" width="120"><template #default><el-tag type="info" size="small">支付进件</el-tag></template></el-table-column>
        <el-table-column label="截止时间" width="170"><template #default="{row}"><span :style="{color:isOverdue(row.auditNodeDeadline)?'red':''}">{{row.auditNodeDeadline?.substring(0,16)||'-'}}</span></template></el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120"/>
        <el-table-column prop="contactPhone" label="电话" width="140"/>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}"><el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button><el-button link type="success" size="small" @click="approveAudit(row)">通过</el-button><el-button link type="danger" size="small" @click="rejectAudit(row)">驳回</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDetail" title="商户详情" width="850px">
      <el-tabs v-if="selected">
        <el-tab-pane label="基本信息">
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
      <template #footer><el-button type="success" @click="approveAudit(selected)">进件通过</el-button><el-button type="danger" @click="rejectAudit(selected)">驳回</el-button></template>
    </el-dialog>

    <el-dialog v-model="showApprove" title="支付进件确认" width="500px">
      <el-alert type="success" :closable="false" title="确认该商户结算账户已通过银行验证？通过后商户状态变为「已入驻」。" style="margin-bottom:16px"/>
      <el-descriptions :column="1" border v-if="approving"><el-descriptions-item label="商户">{{approving.merchantName}}</el-descriptions-item><el-descriptions-item label="开户银行">{{approving.bankName||'-'}}</el-descriptions-item><el-descriptions-item label="银行账号">{{approving.bankAccount||'-'}}</el-descriptions-item></el-descriptions>
      <el-form label-position="top" style="margin-top:12px"><el-form-item label="进件备注"><el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="支付渠道开通情况等"/></el-form-item></el-form>
      <template #footer><el-button type="success" @click="confirmApprove" :loading="submitting">确认通过</el-button></template>
    </el-dialog>

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
const approveForm=ref({comment:''}),rejectForm=ref({reason:''}),auditLogs=ref([]),logLoading=ref(false)

const fetch=async()=>{loading.value=true;try{const r=await request.get('/merchant/node/PAYMENT');if(r.code===200)merchants.value=r.data||[]}finally{loading.value=false}}

const getTypeText=t=>({DIGITAL:'数字权益',PHYSICAL:'实物商品',LOCAL_LIFE:'本地生活'}[t]||t||'-')
const getTypeTagType=t=>({DIGITAL:'primary',PHYSICAL:'success',LOCAL_LIFE:'warning'}[t]||'info')
const isOverdue=d=>d?new Date(d)<new Date():false

const viewDetail=r=>{selected.value=r;fetchAuditLogs(r.id);showDetail.value=true}
const approveAudit=r=>{approving.value=r;approveForm.value.comment='';showApprove.value=true}
const confirmApprove=async()=>{
  submitting.value=true
  try{
    // 支付进件通过后 → 已入驻状态
    await request.put(`/merchant/${approving.value.id}/audit`,{
      auditStatus:'APPROVED',
      auditNode:'PAYMENT',
      onboardingStatus:'APPROVED'
    })
    ElMessage.success('支付进件通过！商户状态已变更为「已入驻」');showApprove.value=false;showDetail.value=false;await fetch()
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
.container{padding:20px}.page-title{margin-bottom:24px;color:#333;font-size:24px;font-weight:600}.card{background:#fff;border-radius:8px;box-shadow:0 2px 12px 0 rgba(0,0,0,.1);padding:20px;margin-bottom:20px}.card-header{display:flex;justify-content:space-between;align-items:center;padding-bottom:16px;border-bottom:1px solid #f0f0f0;margin-bottom:16px}.card-header h3{margin:0;color:#333;font-size:16px}
</style>
