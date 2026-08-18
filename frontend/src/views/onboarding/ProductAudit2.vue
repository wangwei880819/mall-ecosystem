<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📋 二级选品审核</h2>
    </div>

    <div class="table-container">
      <el-table :data="pagedProducts" border stripe v-loading="loading">
        <el-table-column prop="productCode" label="商品编号" width="160" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <img v-if="row.imageUrls || row.productImage" :src="getFirstImage(row)" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px" />
            <span v-else class="no-image">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column prop="productType" label="商品类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getProductTypeTag(row.productType)">{{ getProductTypeText(row.productType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="所属分类" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.categoryName || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="一级审核时间" width="170">
          <template #default="{ row }">
            {{ row.level1AuditTime ? row.level1AuditTime.substring(0, 16) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:center;margin-top:16px">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="products.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </div>

    <!-- 审核对话框 -->
    <el-dialog v-model="showAuditDialog" title="二级审核" width="900px" :close-on-click-modal="false" @closed="resetAudit">
      <div class="audit-layout" v-if="selectedProduct">
        <!-- 左侧：商品详情 -->
        <div class="audit-left">
          <div class="product-image-large">
            <img v-if="selectedProduct.imageUrls || selectedProduct.productImage" :src="getFirstImage(selectedProduct)" style="width: 100%; max-height: 300px; object-fit: cover; border-radius: 8px" />
            <el-empty v-else description="暂无图片" />
          </div>
          <el-descriptions :column="2" border size="small" style="margin-top: 16px">
            <el-descriptions-item label="商品名称" :span="2">{{ selectedProduct.productName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商品编号">{{ selectedProduct.productCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="售价">¥{{ (selectedProduct.price || 0).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedProduct.productType !== 'BENEFIT'" label="市场价">¥{{ (selectedProduct.marketPrice || 0).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedProduct.productType !== 'BENEFIT'" label="会员价">¥{{ (selectedProduct.vipPrice || 0).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="所属商户">{{ selectedProduct.merchantName || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedProduct.productType !== 'BENEFIT'" label="品牌">{{ selectedProduct.brand || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedProduct.productType !== 'BENEFIT'" label="所属分类">{{ selectedProduct.categoryName || selectedProduct.category || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商品类型">
              <el-tag size="small">{{ getProductTypeText(selectedProduct.productType) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedProduct.productType !== 'BENEFIT'" label="库存">{{ selectedProduct.stock || 0 }}</el-descriptions-item>
            <el-descriptions-item label="一级审核人">{{ selectedProduct.level1Auditor || '-' }}</el-descriptions-item>
            <el-descriptions-item label="一级审核时间">{{ selectedProduct.level1AuditTime ? selectedProduct.level1AuditTime.substring(0, 16) : '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedProduct.productType !== 'BENEFIT'" label="商品介绍" :span="2">{{ selectedProduct.description || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedProduct.productType !== 'BENEFIT'" label="商品详情" :span="2">
              <div v-if="selectedProduct.detail" v-html="selectedProduct.detail" style="max-height: 200px; overflow-y: auto"></div>
              <span v-else>-</span>
            </el-descriptions-item>

            <!-- 权益商品特有属性 -->
            <template v-if="selectedProduct.productType === 'BENEFIT' && selectedProduct._benefit">
              <el-descriptions-item label="权益类型">{{ getBenefitTypeText(selectedProduct._benefit.benefitType) }}</el-descriptions-item>
              <el-descriptions-item label="兑换方式">{{ getExchangeMethodText(selectedProduct._benefit.exchangeMethod) }}</el-descriptions-item>
              <el-descriptions-item label="面值">¥{{ (selectedProduct._benefit.faceValue || 0).toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="结算价">¥{{ (selectedProduct._benefit.settlePrice || 0).toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="有效期类型">{{ getValidityTypeText(selectedProduct._benefit.validityType) }}</el-descriptions-item>
              <el-descriptions-item label="有效天数">{{ selectedProduct._benefit.validityType === 'DAYS_AFTER_RECEIVE' ? selectedProduct._benefit.validityDays + '天' : '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="selectedProduct._benefit.validityType === 'FIXED_DATE'" label="有效期范围">{{ selectedProduct._benefit.validityStart || '-' }} ~ {{ selectedProduct._benefit.validityEnd || '-' }}</el-descriptions-item>
              <el-descriptions-item label="总库存">{{ selectedProduct._benefit.stockTotal || '-' }}</el-descriptions-item>
              <el-descriptions-item label="每日限兑">{{ selectedProduct._benefit.stockDailyLimit || '-' }}</el-descriptions-item>
              <el-descriptions-item label="每人限兑">{{ selectedProduct._benefit.stockPerUser || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系方式">{{ selectedProduct._benefit.supplierContact || '-' }}</el-descriptions-item>
              <el-descriptions-item label="退款政策">{{ getRefundPolicyText(selectedProduct._benefit.refundPolicy) }}</el-descriptions-item>
              <el-descriptions-item label="使用规则" :span="2">{{ selectedProduct._benefit.usageRules || '-' }}</el-descriptions-item>
              <el-descriptions-item label="适用范围" :span="2">{{ selectedProduct._benefit.applicableScope || '-' }}</el-descriptions-item>
              <el-descriptions-item label="详细说明" :span="2">{{ selectedProduct._benefit.detailDesc || '-' }}</el-descriptions-item>
              <el-descriptions-item label="权益描述" :span="2">{{ selectedProduct._benefit.benefitDescription || '-' }}</el-descriptions-item>
            </template>
          </el-descriptions>
        </div>

        <!-- 右侧：AI审核模块 -->
        <div class="audit-right">
          <div class="ai-title">🤖 AI智能审核</div>
          <div v-if="!aiResult && !aiLoading">
            <el-button type="primary" @click="startAIAudit" :loading="aiLoading">开始AI审核</el-button>
          </div>
          <div v-loading="aiLoading" style="min-height: 200px; margin-top: 16px">
            <div v-if="aiResult">
              <div class="ai-score-section">
                <div class="ai-score-circle" :class="aiResult.overall === 'PASS' ? 'passed' : 'failed'">
                  <span class="score-number">{{ aiResult.score || 0 }}</span>
                  <span class="score-label">分</span>
                </div>
                <el-tag :type="aiResult.overall === 'PASS' ? 'success' : 'danger'" size="large" style="margin-top: 8px">
                  {{ aiResult.overall === 'PASS' ? '审核通过' : '审核不通过' }}
                </el-tag>
              </div>
              <div class="ai-dimensions" v-if="aiResult.items && aiResult.items.length > 0">
                <div class="ai-dimension-item" v-for="dim in aiResult.items" :key="dim.dimension" :class="{ 'dim-failed': !dim.passed }">
                  <div class="dim-header">
                    <span class="dim-name">{{ dim.dimension }}</span>
                    <span class="dim-status">{{ dim.passed ? '✅' : '❌' }}</span>
                  </div>
                  <div class="dim-detail" v-if="dim.detail">
                    <span :style="{ color: dim.passed ? '#67c23a' : '#f56c6c' }">{{ dim.passed ? '✓' : '✗' }} {{ dim.detail }}</span>
                  </div>
                  <div class="dim-suggestion" v-if="dim.suggestion">💡 {{ dim.suggestion }}</div>
                </div>
              </div>
              <div class="ai-summary" v-if="aiResult.summary">
                <div class="summary-title">📝 审核总结</div>
                <div class="summary-content">{{ aiResult.summary }}</div>
              </div>
            </div>
            <el-empty v-else-if="!aiLoading" description="点击按钮开始AI智能审核" />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="audit-footer">
          <el-button type="success" @click="approveProduct" :loading="submitLoading" :disabled="!selectedProduct">审核通过</el-button>
          <el-button type="danger" @click="showRejectInput" :disabled="!selectedProduct">驳回</el-button>
        </div>
      </template>

      <!-- 审核轨迹 -->
      <div class="audit-trail">
        <div class="trail-title">📋 审核轨迹</div>
        <div class="trail-track">
          <div class="trail-node-wrapper">
            <div class="trail-node done">
              <div class="trail-dot done"><span>✓</span></div>
              <div class="trail-label">商品入驻</div>
              <div class="trail-time">{{ selectedProduct?.createTime?.substring(0, 16) || '-' }}</div>
            </div>
            <div class="trail-line done"></div>
          </div>
          <div class="trail-node-wrapper">
            <div class="trail-node done">
              <div class="trail-dot done"><span>✓</span></div>
              <div class="trail-label">一级审核</div>
              <div class="trail-time">{{ selectedProduct?.level1AuditTime || '-' }}</div>
            </div>
            <div class="trail-line done"></div>
          </div>
          <div class="trail-node-wrapper">
            <div class="trail-node active">
              <div class="trail-dot active"><span>●</span></div>
              <div class="trail-label">二级审核</div>
              <div class="trail-time">-</div>
            </div>
            <div class="trail-line active"></div>
          </div>
          <div class="trail-node-wrapper">
            <div class="trail-node pending">
              <div class="trail-dot pending"><span>○</span></div>
              <div class="trail-label">已上架</div>
              <div class="trail-time">-</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 驳回原因对话框 -->
    <el-dialog v-model="showRejectDialog" title="驳回原因" width="500px" append-to-body>
      <el-form label-position="top">
        <el-form-item label="请输入驳回原因（必填）">
          <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请填写驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmReject" :loading="submitLoading">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- AI不通过时审核通过原因对话框 -->
    <el-dialog v-model="showApproveReasonDialog" title="审核通过原因" width="500px" append-to-body>
      <el-alert type="warning" title="AI审核未通过" :closable="false" style="margin-bottom: 16px">
        <template #default>
          该商品AI智能审核评分为 <strong>{{ aiResult?.score }}分</strong>，未达到通过标准。如确认人工审核通过，请填写原因说明。
        </template>
      </el-alert>
      <el-form label-position="top">
        <el-form-item label="审核通过原因（必填）">
          <el-input v-model="approveReason" type="textarea" :rows="4" placeholder="请填写人工审核通过的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmApproveWithReason" :loading="submitLoading">确认通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const aiLoading = ref(false)
const submitLoading = ref(false)
const showAuditDialog = ref(false)
const showRejectDialog = ref(false)
const showApproveReasonDialog = ref(false)
const products = ref([])
const selectedProduct = ref(null)
const aiResult = ref(null)
const rejectReason = ref('')
const approveReason = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return products.value.slice(start, start + pageSize.value)
})

const getStatusType = (status) => {
  const types = { ON_SHELF: 'success', ONE_PASSED: 'warning', PENDING: 'warning', OFF_SHELF: 'info', REJECTED: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const map = { ON_SHELF: '在售', ONE_PASSED: '一级通过', PENDING: '待二级审核', OFF_SHELF: '已下架', REJECTED: '已驳回' }
  return map[status] || status
}

const getProductTypeText = (type) => {
  const map = { PHYSICAL: '实物商品', VIRTUAL: '虚拟商品', BENEFIT: '权益商品', DIGITAL: '权益商品' }
  return map[type] || type || '实物商品'
}

const getProductTypeTag = (type) => {
  if (!type || type === 'PHYSICAL') return ''
  if (type === 'VIRTUAL') return 'warning'
  return 'success'
}

const benefitTypeMap = { MEMBERSHIP: '会员权益', COUPON: '优惠券', GAME_POINTS: '游戏点卡', DIGITAL_CONTENT: '数字内容', SERVICE: '在线服务', INSURANCE: '保险/延保' }
const getBenefitTypeText = (t) => t ? (benefitTypeMap[t] || t) : '-'
const exchangeMethodMap = { AUTO_BIND: '自动绑定', CODE: '兑换码', QR_CODE: '二维码核销', MANUAL: '人工发放' }
const getExchangeMethodText = (m) => m ? (exchangeMethodMap[m] || m) : '-'
const validityTypeMap = { FIXED_DATE: '固定日期', DAYS_AFTER_RECEIVE: '领取后N天有效', DURATION: '长期有效' }
const getValidityTypeText = (v) => v ? (validityTypeMap[v] || v) : '-'
const refundPolicyMap = { NO_REFUND: '不可退款', CONDITIONAL: '有条件退款', FULL_REFUND: '支持退款' }
const getRefundPolicyText = (p) => p ? (refundPolicyMap[p] || p) : '-'

const getFirstImage = (row) => {
  const urls = row?.productImage || row?.imageUrls
  if (!urls) return ''
  return urls.split(',')[0]
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/product/audit-list-2')
    if (res.code === 200) {
      products.value = res.data?.list || res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch audit products:', e)
    products.value = []
  } finally {
    loading.value = false
  }
}

const openAudit = async (product) => {
  selectedProduct.value = product
  if (product.productType === 'BENEFIT') {
    try {
      const res = await request.get(`/benefit/by-name/${encodeURIComponent(product.productName)}`)
      if (res.code === 200 && res.data) {
        selectedProduct.value = { ...product, _benefit: res.data }
      }
    } catch (e) {
      console.error('Failed to fetch benefit data:', e)
    }
  }
  aiResult.value = null
  showAuditDialog.value = true
}

const resetAudit = () => {
  selectedProduct.value = null
  aiResult.value = null
  aiLoading.value = false
}

const startAIAudit = async () => {
  if (!selectedProduct.value) return
  aiLoading.value = true
  aiResult.value = null
  try {
    const res = await request.post(`/product/${selectedProduct.value.id}/ai-audit`)
    if (res.code === 200) {
      aiResult.value = res.data
    } else {
      ElMessage.error(res.message || 'AI审核失败')
    }
  } catch (e) {
    console.error('AI audit error:', e)
    ElMessage.error('AI审核请求失败，请稍后重试')
  } finally {
    aiLoading.value = false
  }
}

const approveProduct = async () => {
  if (aiResult.value && aiResult.value.overall === 'FAIL') {
    approveReason.value = ''
    showApproveReasonDialog.value = true
    return
  }

  try {
    await ElMessageBox.confirm('确认二级审核通过该商品？通过后商品将正式上架。', '审核确认', {
      confirmButtonText: '确认通过',
      cancelButtonText: '取消',
      type: 'success'
    })
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const res = await request.put(`/product/${selectedProduct.value.id}/audit`, {
      auditStatus: 'ON_SHELF',
      auditor: '审核员',
      reviewLevel: 2
    })
    if (res.code === 200) {
      ElMessage.success('审核通过，商品已正式上架')
      showAuditDialog.value = false
      await fetchProducts()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('Approve error:', e)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

const confirmApproveWithReason = async () => {
  if (!approveReason.value.trim()) {
    ElMessage.warning('请填写审核通过原因')
    return
  }

  submitLoading.value = true
  try {
    const res = await request.put(`/product/${selectedProduct.value.id}/audit`, {
      auditStatus: 'ON_SHELF',
      auditor: '审核员',
      approveReason: approveReason.value,
      reviewLevel: 2
    })
    if (res.code === 200) {
      ElMessage.success('审核通过，商品已正式上架')
      showApproveReasonDialog.value = false
      showAuditDialog.value = false
      approveReason.value = ''
      await fetchProducts()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('Approve with reason error:', e)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

const showRejectInput = () => {
  rejectReason.value = ''
  showRejectDialog.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }

  submitLoading.value = true
  try {
    const res = await request.put(`/product/${selectedProduct.value.id}/audit`, {
      auditStatus: 'REJECTED',
      rejectReason: rejectReason.value,
      auditor: '审核员',
      reviewLevel: 2
    })
    if (res.code === 200) {
      ElMessage.success('商品已驳回')
      showRejectDialog.value = false
      showAuditDialog.value = false
      await fetchProducts()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('Reject error:', e)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h2 { margin: 0; color: #333; font-size: 24px; font-weight: 600; }
.table-container { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1); padding: 20px; overflow-x: auto; }
.no-image { color: #c0c4cc; font-size: 12px; }
.audit-layout { display: flex; gap: 20px; }
.audit-left { flex: 1; min-width: 0; }
.audit-right { width: 380px; flex-shrink: 0; background: #f5f7fa; border-radius: 8px; padding: 20px; max-height: 600px; overflow-y: auto; }
.ai-title { font-size: 18px; font-weight: 600; color: #333; margin-bottom: 16px; text-align: center; }
.ai-score-section { display: flex; flex-direction: column; align-items: center; padding: 16px 0; }
.ai-score-circle { width: 100px; height: 100px; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; border: 4px solid #e0e0e0; }
.ai-score-circle.passed { border-color: #67c23a; background: #f0f9eb; }
.ai-score-circle.failed { border-color: #f56c6c; background: #fef0f0; }
.score-number { font-size: 32px; font-weight: 700; line-height: 1; }
.score-label { font-size: 14px; color: #909399; }
.ai-dimensions { margin-top: 16px; }
.ai-dimension-item { background: #fff; border-radius: 6px; padding: 12px; margin-bottom: 8px; }
.dim-header { display: flex; justify-content: space-between; align-items: center; }
.dim-name { font-weight: 600; font-size: 14px; color: #333; }
.dim-status { font-size: 16px; }
.dim-detail { color: #666; font-size: 13px; margin-top: 6px; }
.dim-suggestion { font-size: 13px; margin-top: 4px; }
.ai-summary { margin-top: 16px; background: #fff; border-radius: 6px; padding: 12px; }
.summary-title { font-weight: 600; font-size: 14px; color: #333; margin-bottom: 8px; }
.summary-content { font-size: 13px; color: #555; line-height: 1.6; white-space: pre-wrap; }
.audit-footer { display: flex; justify-content: flex-end; gap: 8px; }

/* 审核轨迹样式 */
.audit-trail{margin-top:16px;padding:16px;background:#fafafa;border-radius:8px;border:1px solid #ebeef5}
.trail-title{font-size:14px;font-weight:600;color:#333;margin-bottom:16px}
.trail-track{display:flex;align-items:flex-start;justify-content:center;gap:0}
.trail-node-wrapper{display:flex;align-items:flex-start;flex:1;min-width:0;max-width:140px}
.trail-node{display:flex;flex-direction:column;align-items:center;text-align:center;flex-shrink:0}
.trail-dot{width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:700;border:2px solid #dcdfe6;background:#fff;color:#c0c4cc}
.trail-dot.done{border-color:#67c23a;background:#67c23a;color:#fff}
.trail-dot.active{border-color:#409eff;background:#409eff;color:#fff;box-shadow:0 0 0 4px rgba(64,158,255,.2)}
.trail-label{font-size:12px;color:#333;margin-top:8px;white-space:nowrap;font-weight:500}
.trail-time{font-size:10px;color:#999;margin-top:4px;white-space:nowrap}
.trail-node.pending .trail-label{color:#c0c4cc}
.trail-node.pending .trail-time{color:#c0c4cc}
.trail-line{flex:1;height:2px;margin-top:15px;background:#dcdfe6;min-width:12px}
.trail-line.done{background:#67c23a}
.trail-line.active{background:linear-gradient(to right,#67c23a,#dcdfe6)}
</style>