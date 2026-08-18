<template>
  <div class="onboarding-page">
    <h2>📋 入驻进度</h2>

    <div class="steps-container">
      <div class="step" v-for="(step, i) in steps" :key="step.key" :class="{ active: currentStep === i + 1, completed: currentStep > i + 1 }">
        <div class="step-circle">
          <span v-if="currentStep > i + 1">✓</span>
          <span v-else>{{ i + 1 }}</span>
        </div>
        <div class="step-label">{{ step.title }}</div>
        <div class="step-desc">{{ step.desc }}</div>
        <div v-if="i < steps.length - 1" class="step-line" :class="{ active: currentStep > i + 1 }"></div>
      </div>
    </div>

    <div class="card" v-if="currentStep">
      <div class="card-header">
        <h3>当前节点：{{ steps[currentStep - 1]?.title }}</h3>
        <el-tag :type="statusTag">{{ statusText }}</el-tag>
      </div>

      <div class="step-detail">
        <!-- 步骤1：申请入驻 -->
        <template v-if="currentStep === 1">
          <p class="info-text">请先完成商户入驻申请，填写基本信息和资质材料。</p>
          <el-button type="primary" @click="$router.push('/register')">去申请</el-button>
        </template>

        <!-- 步骤2-7：审核中 -->
        <template v-else-if="currentStep >= 2 && currentStep <= 7">
          <div class="audit-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="入驻状态">{{ onboardingStatus }}</el-descriptions-item>
              <el-descriptions-item label="当前节点">{{ steps[currentStep - 1]?.title }}</el-descriptions-item>
              <el-descriptions-item label="风险等级">{{ riskLevel }}</el-descriptions-item>
              <el-descriptions-item label="信用分">{{ creditScore }}分</el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-if="rejectReason" class="reject-box">
            <el-alert :title="'驳回原因：' + rejectReason" type="error" show-icon :closable="false" />
          </div>
        </template>

        <!-- 步骤8：已通过 -->
        <template v-else-if="currentStep === 8">
          <el-result icon="success" title="入驻审核已通过" sub-title="您现在可以开始上架商品了">
            <template #extra>
              <el-button type="primary" @click="$router.push('/products')">去上架商品</el-button>
            </template>
          </el-result>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '@/utils/http'

const steps = [
  { key: 'apply', title: '申请入驻', desc: '填写基本信息和资质材料' },
  { key: 'qualification', title: '资质初审', desc: '审核营业执照和行业资质' },
  { key: 'business', title: '业务复审', desc: '评估业务能力和品类匹配' },
  { key: 'compliance', title: '合规终审', desc: '风控合规部门最终审核' },
  { key: 'contract', title: '合同签署', desc: '签署合作协议和佣金约定' },
  { key: 'payment', title: '支付进件', desc: '开通支付渠道和结算账户' },
  { key: 'product', title: '商品上架', desc: '选品审核和商品录入' },
  { key: 'online', title: '正式上线', desc: '商品发布上线开始营业' }
]

const currentStep = ref(1)
const onboardingStatus = ref('')
const statusTag = ref('info')
const statusText = ref('')
const riskLevel = ref('')
const creditScore = ref(0)
const rejectReason = ref('')

const fetchStatus = async () => {
  try {
    const merchantId = localStorage.getItem('merchantId') || '1'
    const res = await http.get(`/merchant/${merchantId}`)
    if (res) {
      const m = res
      currentStep.value = m.onboardingStep || 1
      onboardingStatus.value = m.onboardingStatus || ''
      riskLevel.value = m.riskLevel || '-'
      creditScore.value = m.creditScore || 0
      rejectReason.value = m.rejectReason || ''

      if (m.onboardingStatus === 'APPROVED') {
        statusTag.value = 'success'
        statusText.value = '已通过'
      } else if (m.onboardingStatus === 'REJECTED') {
        statusTag.value = 'danger'
        statusText.value = '已驳回'
      } else if (m.onboardingStatus === 'REVIEWING') {
        statusTag.value = 'warning'
        statusText.value = '审核中'
      } else {
        statusTag.value = 'info'
        statusText.value = m.onboardingStatus || '待申请'
      }
    }
  } catch (e) {
    console.error('获取入驻状态失败', e)
  }
}

onMounted(fetchStatus)
</script>

<style scoped>
.onboarding-page { padding: 20px; max-width: 900px; }
.onboarding-page h2 { margin-bottom: 24px; color: #333; font-size: 24px; font-weight: 600; }
.steps-container { display: flex; align-items: flex-start; padding: 30px 0; position: relative; }
.step { flex: 1; text-align: center; position: relative; z-index: 1; }
.step-circle { width: 40px; height: 40px; border-radius: 50%; background: #e0e0e0; color: #999; display: flex; align-items: center; justify-content: center; margin: 0 auto 8px; font-weight: bold; font-size: 14px; transition: all 0.3s; }
.step.active .step-circle { background: #409eff; color: #fff; }
.step.completed .step-circle { background: #67c23a; color: #fff; }
.step-label { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 4px; }
.step-desc { font-size: 11px; color: #999; }
.step-line { position: absolute; top: 20px; right: -50%; width: 100%; height: 3px; background: #e0e0e0; z-index: -1; }
.step-line.active { background: #67c23a; }
.card { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); margin-top: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-header h3 { margin: 0; font-size: 16px; color: #333; }
.step-detail { padding: 10px 0; }
.info-text { color: #666; margin-bottom: 16px; }
.audit-info { margin-bottom: 16px; }
.reject-box { margin-top: 16px; }
</style>