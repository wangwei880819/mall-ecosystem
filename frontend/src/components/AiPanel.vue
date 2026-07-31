<template>
  <div class="ai-panel" v-if="visible">
    <div class="ai-panel-header">
      <span class="ai-icon">🤖</span>
      <span class="ai-title">AI 能力中心</span>
      <el-button link type="danger" @click="$emit('close')">×</el-button>
    </div>

    <div class="ai-panel-body">
      <!-- 文案校对 -->
      <div class="ai-card">
        <div class="ai-card-title">文案智能校对</div>
        <el-input v-model="proofText" type="textarea" :rows="2" placeholder="输入需要校对的内容..." />
        <el-button type="primary" size="small" style="margin-top:8px" :loading="proofLoading" @click="doProof">
          开始校对
        </el-button>
        <div v-if="proofResult" class="ai-result">
          <div class="ai-score">评分：{{ proofResult.score }} 分</div>
          <div v-for="(item, i) in proofResult.items" :key="i" class="ai-item"
               :class="'severity-' + item.severity.toLowerCase()">
            <span class="ai-item-type">[{{ item.type }}]</span>
            <span>{{ item.detail }}</span>
            <div class="ai-suggestion">{{ item.suggestion }}</div>
          </div>
          <div v-if="proofResult.correctedText !== proofText" class="ai-corrected">
            建议修改为：{{ proofResult.correctedText }}
          </div>
        </div>
      </div>

      <!-- 卖点提炼 -->
      <div class="ai-card">
        <div class="ai-card-title">商品卖点提炼</div>
        <div class="ai-info-text">基于当前商品信息自动生成卖点</div>
        <el-button type="primary" size="small" :loading="spLoading" @click="doSellingPoint">
          生成卖点
        </el-button>
        <div v-if="spResult" class="ai-result">
          <div class="ai-score">质量评分：{{ spResult.score }} 分</div>
          <div class="ai-selling-point">核心卖点：{{ spResult.sellingPoint }}</div>
          <div class="ai-tags">
            <el-tag v-for="(t, i) in spResult.tags" :key="i" size="small" style="margin:2px">{{ t }}</el-tag>
          </div>
          <div class="ai-marketing">营销文案：{{ spResult.marketingCopy }}</div>
          <el-button type="success" size="small" style="margin-top:8px" @click="$emit('apply-selling-point', spResult)">
            应用到商品
          </el-button>
        </div>
      </div>

      <!-- 价格摸排 -->
      <div class="ai-card">
        <div class="ai-card-title">价格智能摸排</div>
        <el-button type="primary" size="small" :loading="prLoading" @click="doPriceResearch">
          价格分析
        </el-button>
        <div v-if="prResult" class="ai-result">
          <div class="ai-score">
            <el-tag :type="prResult.overall === 'REASONABLE' ? 'success' : prResult.overall === 'HIGH' ? 'danger' : 'warning'" size="small">
              {{ prResult.overall === 'REASONABLE' ? '价格合理' : prResult.overall === 'HIGH' ? '价格偏高' : '价格偏低' }}
            </el-tag>
            评分：{{ prResult.score }} 分
          </div>
          <div class="ai-price-info">
            建议售价：¥{{ prResult.suggestedPrice }}（区间 ¥{{ prResult.priceLower }} ~ ¥{{ prResult.priceUpper }}）
          </div>
          <div class="ai-competitors">
            <div v-for="c in prResult.competitors" :key="c.platform" class="ai-comp-item">
              {{ c.platform }}：¥{{ (c.price || 0).toFixed(2) }}
            </div>
          </div>
          <div class="ai-summary">{{ prResult.summary }}</div>
          <el-button type="success" size="small" style="margin-top:8px" @click="$emit('apply-price', prResult)">
            应用建议价
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const props = defineProps({
  visible: Boolean,
  productName: { type: String, default: '' },
  productPrice: { type: Number, default: 0 },
  productCategory: { type: String, default: '' }
})

const emit = defineEmits(['close', 'apply-selling-point', 'apply-price'])

const proofText = ref('')
const proofLoading = ref(false)
const proofResult = ref(null)

const spLoading = ref(false)
const spResult = ref(null)

const prLoading = ref(false)
const prResult = ref(null)

// 文案校对
async function doProof() {
  if (!proofText.value) { ElMessage.warning('请输入需要校对的内容'); return }
  proofLoading.value = true
  try {
    const res = await request.post('/ai/proof', { text: proofText.value })
    proofResult.value = res
  } catch (e) {
    ElMessage.error('校对失败')
  } finally {
    proofLoading.value = false
  }
}

// 卖点提炼
async function doSellingPoint() {
  spLoading.value = true
  try {
    const res = await request.post('/ai/selling-point', {
      productName: props.productName,
      description: props.productName
    })
    spResult.value = res
  } catch (e) {
    ElMessage.error('卖点提炼失败')
  } finally {
    spLoading.value = false
  }
}

// 价格摸排
async function doPriceResearch() {
  prLoading.value = true
  try {
    const res = await request.post('/ai/price-research', {
      price: props.productPrice,
      productName: props.productName,
      category: props.productCategory
    })
    prResult.value = res
  } catch (e) {
    ElMessage.error('价格分析失败')
  } finally {
    prLoading.value = false
  }
}

watch(() => props.visible, (v) => { if (!v) { proofResult.value = null; spResult.value = null; prResult.value = null } })
</script>

<style scoped>
.ai-panel { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; margin-bottom: 16px }
.ai-panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px }
.ai-icon { font-size: 20px }
.ai-title { font-size: 15px; font-weight: 600; color: #333 }
.ai-card { background: #fff; border: 1px solid #e8e8e8; border-radius: 6px; padding: 12px; margin-bottom: 10px }
.ai-card-title { font-size: 13px; font-weight: 600; color: #555; margin-bottom: 8px }
.ai-result { margin-top: 10px; font-size: 12px; line-height: 1.8 }
.ai-score { color: #409eff; font-weight: 600 }
.ai-item { padding: 4px 8px; border-radius: 4px; margin: 4px 0; font-size: 12px }
.severity-high { background: #fef0f0; border-left: 3px solid #f56c6c }
.severity-medium { background: #fdf6ec; border-left: 3px solid #e6a23c }
.severity-low { background: #f0f9eb; border-left: 3px solid #67c23a }
.ai-suggestion { color: #e6a23c; padding-left: 12px }
.ai-corrected { margin-top: 8px; padding: 8px; background: #f0f9eb; border-radius: 4px; color: #67c23a }
.ai-selling-point { font-weight: 600; color: #333; margin: 4px 0 }
.ai-marketing { color: #666; font-size: 11px; margin: 4px 0; padding: 6px; background: #f5f5f5; border-radius: 4px }
.ai-competitors { margin: 4px 0 }
.ai-comp-item { color: #666; padding: 2px 0 }
.ai-summary { color: #666; margin: 4px 0 }
.ai-price-info { color: #409eff; font-weight: 600; margin: 4px 0 }
.ai-info-text { font-size: 12px; color: #999; margin-bottom: 8px }
</style>
