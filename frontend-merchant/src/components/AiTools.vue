<template>
  <div class="ai-toolbar">
    <el-button size="small" type="warning" :loading="spLoading" @click="doSellingPoint">
      🤖 AI卖点提炼
    </el-button>
    <el-button size="small" type="info" :loading="prLoading" @click="doPriceResearch">
      🔍 AI价格摸排
    </el-button>
  </div>

  <!-- 卖点提炼结果 -->
  <div v-if="spResult" class="ai-result-bar">
    <div class="ai-result-header">
      <span>AI卖点提炼（{{ spResult.score }}分）</span>
      <el-button link size="small" type="primary" @click="applySellingPoint">应用</el-button>
    </div>
    <div class="ai-tags-row">
      <el-tag v-for="(t, i) in spResult.tags" :key="i" size="small">{{ t }}</el-tag>
    </div>
    <div class="ai-text">{{ spResult.sellingPoint }}</div>
  </div>

  <!-- 价格摸排结果 -->
  <div v-if="prResult" class="ai-result-bar">
    <div class="ai-result-header">
      <span>
        AI价格分析
        <el-tag :type="prResult.overall === 'REASONABLE' ? 'success' : prResult.overall === 'HIGH' ? 'danger' : 'warning'" size="small" style="margin-left:4px">
          {{ prResult.overall === 'REASONABLE' ? '合理' : prResult.overall === 'HIGH' ? '偏高' : '偏低' }}
        </el-tag>
      </span>
      <el-button link size="small" type="primary" @click="applyPrice">应用建议价</el-button>
    </div>
    <div class="ai-price-row">
      <div v-for="c in prResult.competitors" :key="c.platform" class="ai-comp">{{ c.platform }}: ¥{{ (c.price||0).toFixed(2) }}</div>
    </div>
    <div class="ai-text">建议售价：¥{{ prResult.suggestedPrice }}（区间 ¥{{ prResult.priceLower }} ~ ¥{{ prResult.priceUpper }}）</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/utils/http'

const props = defineProps({
  productName: { type: String, default: '' },
  productPrice: { type: Number, default: 0 },
  productCategory: { type: String, default: '' },
  description: { type: String, default: '' }
})

const emit = defineEmits(['apply-selling-point', 'apply-price'])

const spLoading = ref(false)
const spResult = ref(null)
const prLoading = ref(false)
const prResult = ref(null)

async function doSellingPoint() {
  if (!props.productName) { ElMessage.warning('请先输入商品名称'); return }
  spLoading.value = true
  try {
    const res = await http.post('/ai/selling-point', {
      productName: props.productName,
      description: props.description
    })
    spResult.value = res
  } catch (e) {
    ElMessage.error('卖点提炼失败')
  } finally {
    spLoading.value = false
  }
}

async function doPriceResearch() {
  if (!props.productPrice) { ElMessage.warning('请先输入售价'); return }
  prLoading.value = true
  try {
    const res = await http.post('/ai/price-research', {
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

function applySellingPoint() {
  emit('apply-selling-point', spResult.value)
  spResult.value = null
}

function applyPrice() {
  emit('apply-price', prResult.value)
  prResult.value = null
}
</script>

<style scoped>
.ai-toolbar { display: flex; gap: 8px; margin-bottom: 12px }
.ai-result-bar { background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 6px; padding: 10px; margin-bottom: 12px; font-size: 12px }
.ai-result-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; color: #0369a1 }
.ai-tags-row { margin: 6px 0; display: flex; gap: 4px; flex-wrap: wrap }
.ai-text { color: #555; margin-top: 4px; line-height: 1.6 }
.ai-price-row { display: flex; gap: 16px; margin: 4px 0; color: #666 }
.ai-comp { font-size: 11px }
</style>
