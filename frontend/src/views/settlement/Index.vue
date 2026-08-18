<template>
  <div>
    <h1 class="page-title">💰 结算能力 — 合作伙伴结算支撑管理</h1>

    <!-- 结算概览 -->
    <div class="grid-4">
      <div class="stat-card blue">
        <div class="stat-label">月度结算总额</div>
        <div class="stat-value">¥{{ formatAmount(overview.totalAmount) }}</div>
        <div class="stat-sub">共 {{ overview.totalCount || 0 }} 笔结算</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">已完成</div>
        <div class="stat-value">{{ overview.completedCount || 0 }}笔</div>
        <div class="stat-sub">涉及 {{ overview.merchantCount || 0 }} 个商户</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待审批</div>
        <div class="stat-value">{{ overview.pendingCount || 0 }}笔</div>
        <div class="stat-sub">需及时处理</div>
      </div>
      <div class="stat-card purple">
        <div class="stat-label">结算类型</div>
        <div class="stat-value">3类并行</div>
        <div class="stat-sub">AI豆/佣金/商拓费</div>
      </div>
    </div>

    <!-- 统计区与Tab之间的分割线 -->
    <div class="section-divider"></div>

    <!-- 功能Tab（标签式设计） -->
    <el-tabs v-model="tab" class="settle-tabs">
      <el-tab-pane label="结算记录" name="records" />
      <el-tab-pane label="结算规则" name="rules" />
      <el-tab-pane label="结算类型" name="types" />
    </el-tabs>

    <!-- 结算记录 -->
    <template v-if="tab === 'records'">
      <!-- 筛选区域 -->
      <div class="card filter-card">
        <div class="filter-row">
          <div class="filter-item">
            <label>结算类型</label>
            <select v-model="filterParams.settleType" style="padding:6px 12px;border-radius:4px;border:1px solid #ddd;width:150px">
              <option value="">全部</option>
              <option value="AI_DOU">AI豆结算</option>
              <option value="COMMISSION">佣金结算</option>
              <option value="EXPANSION">商拓费结算</option>
            </select>
          </div>
          <div class="filter-item">
            <label>商户</label>
            <select v-model="filterParams.merchantId" style="padding:6px 12px;border-radius:4px;border:1px solid #ddd;width:180px">
              <option value="">全部商户</option>
              <option v-for="m in merchantOptions" :key="m.id" :value="m.id">{{ m.name }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>开始时间</label>
            <input type="date" v-model="filterParams.startTime" style="padding:6px 12px;border-radius:4px;border:1px solid #ddd" />
          </div>
          <div class="filter-item">
            <label>结束时间</label>
            <input type="date" v-model="filterParams.endTime" style="padding:6px 12px;border-radius:4px;border:1px solid #ddd" />
          </div>
          <div class="filter-item filter-actions">
            <button class="btn btn-primary btn-sm" @click="applyFilter">查询</button>
            <button class="btn btn-outline btn-sm" @click="resetFilter">重置</button>
            <button class="btn btn-success btn-sm" @click="exportExcel" :disabled="exportLoading">
              {{ exportLoading ? '导出中...' : '📥 导出Excel' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 合计金额展示 -->
      <div class="total-amount-bar" v-if="filteredTotalAmount !== null">
        <span class="total-label">当前筛选结果结算金额合计：</span>
        <span class="total-value">¥{{ formatAmount(filteredTotalAmount) }}</span>
        <span class="total-count">（共 {{ filteredRecords.length }} 笔）</span>
      </div>

      <div class="card">
        <div class="card-header">
          <h3>结算记录</h3>
          <div>
            <button class="btn btn-primary btn-sm" @click="showGenerateModal = true">生成结算单</button>
          </div>
        </div>
        <table class="data-table">
          <thead>
            <tr><th>结算编号</th><th>所属商户</th><th>结算类型</th><th>结算周期</th><th>金额</th><th>笔数</th><th>状态</th><th>创建时间</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-if="recordsLoading" class="text-center"><td colspan="9">加载中...</td></tr>
            <tr v-else-if="filteredRecords.length === 0" class="text-center"><td colspan="9">暂无结算记录</td></tr>
            <tr v-for="r in filteredRecords" :key="r.id">
              <td>{{ r.settleCode }}</td>
              <td>{{ r.merchant }}</td>
              <td><span :class="getTypeClass(r.settleType)">{{ getTypeText(r.settleType) }}</span></td>
              <td>{{ r.settlePeriod }}</td>
              <td style="font-weight:600">¥{{ formatAmount(r.totalAmount) }}</td>
              <td>{{ (r.itemCount || 0).toLocaleString() }}</td>
              <td><span :class="getStatusClass(r.status)">{{ getStatusText(r.status) }}</span></td>
              <td>{{ r.createTime ? r.createTime.substring(0, 10) : '-' }}</td>
              <td>
                <button class="btn btn-sm btn-outline" @click="viewDetail(r)">详情</button>
                <button v-if="r.status === 'PENDING'" class="btn btn-sm btn-primary" @click="approve(r)">审批</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- AI豆收支趋势 -->
      <h2 class="section-title">AI豆收支趋势</h2>
      <div class="card">
        <div style="display:flex;gap:4px;align-items:flex-end;height:160px;padding:0 20px">
          <div v-for="d in aiDouTrend" :key="d.date" style="flex:1;display:flex;flex-direction:column;align-items:center">
            <div style="font-size:11px;color:#4caf50;margin-bottom:2px">{{ (d.earn / 1000).toFixed(1) }}k</div>
            <div style="width:100%;max-width:40px;height:80px;background:#4caf50;border-radius:4px 4px 0 0;opacity:0.8" :style="{ height: (d.earn / 20000 * 100) + 'px' }"></div>
            <div style="font-size:11px;color:#2196f3;margin-top:2px">{{ (d.consume / 1000).toFixed(1) }}k</div>
            <div style="width:100%;max-width:40px;height:60px;background:#2196f3;border-radius:0 0 4px 4px;opacity:0.8" :style="{ height: (d.consume / 20000 * 100) + 'px' }"></div>
            <div style="font-size:10px;color:#999;margin-top:4px">{{ d.date }}</div>
          </div>
        </div>
        <div style="display:flex;justify-content:center;gap:24px;margin-top:12px">
          <span style="font-size:12px"><span style="display:inline-block;width:12px;height:12px;background:#4caf50;border-radius:2px;margin-right:4px"></span>获取量</span>
          <span style="font-size:12px"><span style="display:inline-block;width:12px;height:12px;background:#2196f3;border-radius:2px;margin-right:4px"></span>消费量</span>
        </div>
      </div>
    </template>

    <!-- 结算规则 -->
    <template v-if="tab === 'rules'">
      <div class="card">
        <div class="card-header">
          <h3>结算规则配置</h3>
          <button class="btn btn-primary btn-sm" @click="openRuleModal()">+ 配置规则</button>
        </div>
        <table class="data-table">
          <thead>
            <tr><th>商户</th><th>结算类型</th><th>佣金比例</th><th>结算周期</th><th>最低结算额</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-if="rulesLoading" class="text-center"><td colspan="6">加载中...</td></tr>
            <tr v-else-if="settleRules.length === 0" class="text-center"><td colspan="6">暂无规则配置</td></tr>
            <tr v-for="r in settleRules" :key="r.id || (r.merchantId + '_' + r.settleType)">
              <td>{{ r.merchantName || '商户' + r.merchantId }}</td>
              <td><span :class="getTypeClass(r.settleType)">{{ getTypeText(r.settleType) }}</span></td>
              <td><span style="font-weight:600;color:#1a237e">{{ formatRate(r.commissionRate) }}%</span></td>
              <td>{{ r.settlePeriod === 'MONTHLY' ? '月结' : r.settlePeriod === 'WEEKLY' ? '周结' : '实时结算' }}</td>
              <td>¥{{ formatAmount(r.minSettleAmount) }}</td>
              <td>
                <button class="btn btn-sm btn-outline" @click="editRule(r)">编辑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- 三类结算类型 -->
    <template v-if="tab === 'types'">
      <div class="grid-3">
        <div class="card">
          <div class="card-header"><h3>🤖 AI豆结算</h3><span class="tag tag-blue">获取+消费+对账</span></div>
          <div style="text-align:center;padding:12px 0">
            <div style="font-size:28px;font-weight:700;color:#2196f3">¥326.5万</div>
            <div style="font-size:12px;color:#999">月度结算额</div>
          </div>
          <div style="font-size:13px;color:#666;line-height:1.8">
            • 多渠道核算（通信充值/会员赠送/营销活动/网龄回馈）<br>
            • 消费抵扣精确到0.01元<br>
            • 总量/渠道/时间三维度对账<br>
            • 报账开票自动化
          </div>
        </div>
        <div class="card">
          <div class="card-header"><h3>💸 平台佣金结算</h3><span class="tag tag-green">计算+分账+周期</span></div>
          <div style="text-align:center;padding:12px 0">
            <div style="font-size:28px;font-weight:700;color:#4caf50">¥789.2万</div>
            <div style="font-size:12px;color:#999">月度结算额</div>
          </div>
          <div style="font-size:13px;color:#666;line-height:1.8">
            • 3种计算模式（固定/阶梯/品类差异化）<br>
            • Seata分布式事务保障分账一致性<br>
            • 月结/季结/实时结算三种模式<br>
            • 商户分账+平台分账=订单金额
          </div>
        </div>
        <div class="card">
          <div class="card-header"><h3>📊 商拓服务费结算</h3><span class="tag tag-purple">提成+费用+奖金</span></div>
          <div style="text-align:center;padding:12px 0">
            <div style="font-size:28px;font-weight:700;color:#9c27b0">¥170.8万</div>
            <div style="font-size:12px;color:#999">月度结算额</div>
          </div>
          <div style="font-size:13px;color:#666;line-height:1.8">
            • 业绩提成（固定/阶梯/目标达成）<br>
            • 推广费用申请→核算→结算<br>
            • 激励奖金自动核算<br>
            • 多级审批流程保障合规
          </div>
        </div>
      </div>

      <!-- 结算全流程 -->
      <h2 class="section-title">结算全流程（8节点）</h2>
      <div class="card">
        <div class="steps">
          <template v-for="(step, i) in settleSteps" :key="step.num">
            <div class="step-item">
              <div class="step-circle" :class="step.status">{{ step.num }}</div>
              <div class="step-label">{{ step.name }}</div>
            </div>
            <div v-if="i < settleSteps.length - 1" class="step-line" :class="step.status === 'done' ? 'done' : ''"></div>
          </template>
        </div>
        <div class="alert alert-success">
          ✅ 技术保障：Seata分布式事务保障结算一致性 | RocketMQ消息可靠性投递保障资金划拨不丢消息 | ClickHouse实时OLAP支撑稽核查询
        </div>
      </div>
    </template>

    <!-- 结算详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal">
        <div class="modal-header">
          <h3>结算详情</h3>
          <button class="modal-close" @click="showDetail = false">×</button>
        </div>
        <div class="modal-body" v-if="currentItem">
          <div class="grid-2">
            <div class="form-group">
              <label>结算编号</label>
              <input type="text" :value="currentItem.settleCode" readonly />
            </div>
            <div class="form-group">
              <label>所属商户</label>
              <input type="text" :value="currentItem.merchant" readonly />
            </div>
            <div class="form-group">
              <label>结算类型</label>
              <input type="text" :value="getTypeText(currentItem.settleType)" readonly />
            </div>
            <div class="form-group">
              <label>结算周期</label>
              <input type="text" :value="currentItem.settlePeriod" readonly />
            </div>
            <div class="form-group">
              <label>结算金额</label>
              <input type="text" :value="'¥' + formatAmount(currentItem.totalAmount)" readonly />
            </div>
            <div class="form-group">
              <label>结算笔数</label>
              <input type="text" :value="(currentItem.itemCount || 0).toLocaleString()" readonly />
            </div>
          </div>
          <div v-if="currentItem.approver" class="form-group">
            <label>审批人</label>
            <input type="text" :value="currentItem.approver" readonly />
          </div>
          <div v-if="currentItem.approveTime" class="form-group">
            <label>审批时间</label>
            <input type="text" :value="currentItem.approveTime" readonly />
          </div>
        </div>
        <div class="modal-footer">
        </div>
      </div>
    </div>

    <!-- 生成结算单弹窗 -->
    <div v-if="showGenerateModal" class="modal-overlay" @click.self="showGenerateModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>生成结算单</h3>
          <button class="modal-close" @click="showGenerateModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>选择商户</label>
            <select v-model="newSettle.merchantId">
              <option :value="m.id" v-for="m in merchantOptions" :key="m.id">{{ m.name }}</option>
            </select>
          </div>
          <div class="grid-2">
            <div class="form-group">
              <label>结算类型</label>
              <select v-model="newSettle.settleType">
                <option value="AI_DOU">AI豆结算</option>
                <option value="COMMISSION">佣金结算</option>
                <option value="EXPANSION">商拓费结算</option>
              </select>
            </div>
            <div class="form-group">
              <label>结算周期</label>
              <input type="text" v-model="newSettle.settlePeriod" placeholder="如 2024年8月" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="generateSettle" :disabled="generateLoading">
            {{ generateLoading ? '生成中...' : '生成' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 规则配置弹窗 -->
    <div v-if="showRuleModal" class="modal-overlay" @click.self="showRuleModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingRule ? '编辑规则' : '配置规则' }}</h3>
          <button class="modal-close" @click="showRuleModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>商户</label>
            <select v-model="newRule.merchantId">
              <option :value="m.id" v-for="m in merchantOptions" :key="m.id">{{ m.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>结算类型 <span style="color:red">*</span></label>
            <select v-model="newRule.settleType">
              <option value="">请选择结算类型</option>
              <option value="AI_DOU">AI豆结算</option>
              <option value="COMMISSION">佣金结算</option>
              <option value="EXPANSION">商拓费结算</option>
            </select>
          </div>
          <div class="grid-2">
            <div class="form-group">
              <label>佣金比例</label>
              <input type="number" step="0.01" v-model.number="newRule.commissionRate" placeholder="如 5.00" />
            </div>
            <div class="form-group">
              <label>结算周期</label>
              <select v-model="newRule.settlePeriod">
                <option value="MONTHLY">月结</option>
                <option value="WEEKLY">周结</option>
                <option value="DAILY">实时结算</option>
              </select>
            </div>
            <div class="form-group">
              <label>最低结算额</label>
              <input type="number" v-model.number="newRule.minSettleAmount" placeholder="如 100" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="saveRule" :disabled="ruleSaveLoading">
            {{ ruleSaveLoading ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { SETTLE_STATUS, SETTLE_STATUS_CLASS, SETTLE_TYPE_TEXT, SETTLE_TYPE_CLASS } from '../../utils/constants'

const tab = ref('records')
const showDetail = ref(false)
const showGenerateModal = ref(false)
const showRuleModal = ref(false)
const currentItem = ref(null)
const editingRule = ref(null)

const recordsLoading = ref(false)
const rulesLoading = ref(false)
const generateLoading = ref(false)
const ruleSaveLoading = ref(false)
const exportLoading = ref(false)

const filterParams = ref({
  settleType: '',
  merchantId: '',
  startTime: '',
  endTime: ''
})

const filteredTotalAmount = ref(null)

const overview = ref({
  totalAmount: 0,
  completedCount: 0,
  pendingCount: 0,
  totalCount: 0,
  merchantCount: 0
})

const settleSteps = [
  { num: 1, name: '订单完成', status: 'done' },
  { num: 2, name: '结算触发', status: 'done' },
  { num: 3, name: '类型判定', status: 'done' },
  { num: 4, name: 'AI豆结算', status: 'done' },
  { num: 5, name: '佣金结算', status: 'active' },
  { num: 6, name: '商拓费结算', status: 'pending' },
  { num: 7, name: '对账确认', status: 'pending' },
  { num: 8, name: '资金划拨', status: 'pending' },
]

const records = ref([])
const settleRules = ref([])

const merchantOptions = ref([])

const newSettle = ref({
  merchantId: 1,
  settleType: 'COMMISSION',
  settlePeriod: '2024年8月'
})

const newRule = ref({
  merchantId: 1,
  settleType: '',
  commissionRate: 5,
  settlePeriod: 'MONTHLY',
  minSettleAmount: 100
})

const aiDouTrend = [
  { date: '08-01', earn: 12500, consume: 8200, balance: 156000 },
  { date: '08-05', earn: 15800, consume: 9500, balance: 162300 },
  { date: '08-10', earn: 13200, consume: 11000, balance: 164500 },
  { date: '08-15', earn: 18600, consume: 7800, balance: 175300 },
  { date: '08-20', earn: 14300, consume: 12500, balance: 177100 },
  { date: '08-23', earn: 9800, consume: 6200, balance: 180700 },
]

const filteredRecords = computed(() => {
  return records.value
})

const formatAmount = (val) => {
  if (val === null || val === undefined) return '0'
  const num = typeof val === 'number' ? val : Number(val)
  if (isNaN(num)) return '0'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatRate = (val) => {
  if (val === null || val === undefined) return '0'
  const num = typeof val === 'number' ? val : Number(val)
  if (isNaN(num)) return '0'
  if (num < 1 && num > 0) return (num * 100).toFixed(2)
  return num.toFixed(2)
}

const fetchOverview = async () => {
  try {
    const res = await request.get('/admin/settlement/overview')
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (e) {
    console.error('获取结算概览失败:', e)
  }
}

const fetchRecords = async () => {
  recordsLoading.value = true
  try {
    const params = {}
    if (filterParams.value.settleType) params.settleType = filterParams.value.settleType
    if (filterParams.value.merchantId) params.merchantId = filterParams.value.merchantId
    if (filterParams.value.startTime) params.startTime = filterParams.value.startTime + ' 00:00:00'
    if (filterParams.value.endTime) params.endTime = filterParams.value.endTime + ' 23:59:59'
    const res = await request.get('/admin/settlement/records', { params })
    if (res.code === 200) {
      records.value = res.data || []
    }
  } catch (e) {
    console.error('获取结算记录失败:', e)
  } finally {
    recordsLoading.value = false
  }
}

const fetchTotalAmount = async () => {
  try {
    const params = {}
    if (filterParams.value.settleType) params.settleType = filterParams.value.settleType
    if (filterParams.value.merchantId) params.merchantId = filterParams.value.merchantId
    if (filterParams.value.startTime) params.startTime = filterParams.value.startTime + ' 00:00:00'
    if (filterParams.value.endTime) params.endTime = filterParams.value.endTime + ' 23:59:59'
    const res = await request.get('/admin/settlement/records/total', { params })
    if (res.code === 200) {
      filteredTotalAmount.value = res.data?.totalAmount || 0
    }
  } catch (e) {
    console.error('获取结算合计失败:', e)
    filteredTotalAmount.value = null
  }
}

const applyFilter = async () => {
  await fetchRecords()
  await fetchTotalAmount()
}

const resetFilter = () => {
  filterParams.value = { settleType: '', merchantId: '', startTime: '', endTime: '' }
  fetchRecords()
  fetchTotalAmount()
}

const exportExcel = async () => {
  exportLoading.value = true
  try {
    const params = new URLSearchParams()
    if (filterParams.value.settleType) params.append('settleType', filterParams.value.settleType)
    if (filterParams.value.merchantId) params.append('merchantId', filterParams.value.merchantId)
    if (filterParams.value.startTime) params.append('startTime', filterParams.value.startTime + ' 00:00:00')
    if (filterParams.value.endTime) params.append('endTime', filterParams.value.endTime + ' 23:59:59')

    const response = await fetch(`/api/admin/settlement/records/export?${params.toString()}`, {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    if (!response.ok) {
      ElMessage.error('导出失败')
      return
    }
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `结算记录_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败:', e)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const fetchRules = async () => {
  rulesLoading.value = true
  try {
    const res = await request.get('/admin/settlement/rules')
    if (res.code === 200) {
      settleRules.value = res.data || []
    }
  } catch (e) {
    console.error('获取结算规则失败:', e)
  } finally {
    rulesLoading.value = false
  }
}

const fetchMerchants = async () => {
  try {
    const res = await request.get('/merchant', { params: { page: 0, size: 1000 } })
    if (res.code === 200) {
      const list = res.data?.list || res.data || []
      merchantOptions.value = list.map(m => ({ id: m.id, name: m.merchantName }))
    }
  } catch (e) {
    console.error('获取商户列表失败:', e)
  }
}

onMounted(async () => {
  await Promise.all([fetchOverview(), fetchRecords(), fetchTotalAmount(), fetchRules(), fetchMerchants()])
})

const getTypeClass = (type) => {
  return SETTLE_TYPE_CLASS[type] || 'tag tag-gray'
}

const getTypeText = (type) => {
  return SETTLE_TYPE_TEXT[type] || type || '未知'
}

const getStatusClass = (status) => {
  return SETTLE_STATUS_CLASS[status] || 'tag tag-gray'
}

const getStatusText = (status) => {
  return SETTLE_STATUS[status] || status || '未知'
}

const viewDetail = (item) => {
  currentItem.value = item
  showDetail.value = true
}

const approve = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确认审核通过该结算单？\n结算编号：${item.settleCode}\n结算金额：¥${formatAmount(item.totalAmount)}`,
      '审核确认',
      {
        confirmButtonText: '确认通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
  } catch {
    return
  }
  try {
    const res = await request.put(`/finance/settlements/${item.id}/approve`, {
      approver: 'admin'
    })
    if (res.code === 200) {
      ElMessage.success('审批通过')
      await Promise.all([fetchRecords(), fetchOverview(), fetchTotalAmount()])
    } else {
      ElMessage.error(res.message || '审批失败')
    }
  } catch (e) {
    console.error('审批失败:', e)
    ElMessage.error('审批操作失败')
  }
}

const generateSettle = async () => {
  if (!newSettle.value.merchantId) {
    ElMessage.warning('请选择商户')
    return
  }
  if (!newSettle.value.settlePeriod) {
    ElMessage.warning('请输入结算周期')
    return
  }
  generateLoading.value = true
  try {
    const res = await request.post('/finance/settlements', {
      merchantId: newSettle.value.merchantId,
      settleType: newSettle.value.settleType,
      settlePeriod: newSettle.value.settlePeriod
    })
    if (res.code === 200) {
      ElMessage.success('结算单生成成功')
      showGenerateModal.value = false
      await Promise.all([fetchRecords(), fetchOverview(), fetchTotalAmount()])
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (e) {
    console.error('生成结算单失败:', e)
    ElMessage.error('生成结算单失败')
  } finally {
    generateLoading.value = false
  }
}

const openRuleModal = () => {
  editingRule.value = null
  newRule.value = {
    merchantId: merchantOptions.value[0]?.id || 1,
    settleType: '',
    commissionRate: 5,
    settlePeriod: 'MONTHLY',
    minSettleAmount: 100
  }
  showRuleModal.value = true
}

const editRule = (rule) => {
  editingRule.value = rule
  newRule.value = {
    merchantId: rule.merchantId,
    settleType: rule.settleType || '',
    commissionRate: rule.commissionRate,
    settlePeriod: rule.settlePeriod || 'MONTHLY',
    minSettleAmount: rule.minSettleAmount || 100
  }
  showRuleModal.value = true
}

const saveRule = async () => {
  if (!newRule.value.settleType) {
    ElMessage.warning('请选择结算类型')
    return
  }
  if (!newRule.value.commissionRate) {
    ElMessage.warning('请输入佣金比例')
    return
  }
  ruleSaveLoading.value = true
  try {
    const res = await request.post('/admin/settlement/rules', {
      merchantId: newRule.value.merchantId,
      settleType: newRule.value.settleType,
      commissionRate: newRule.value.commissionRate,
      settlePeriod: newRule.value.settlePeriod,
      minSettleAmount: newRule.value.minSettleAmount
    })
    if (res.code === 200) {
      ElMessage.success('规则保存成功')
      showRuleModal.value = false
      editingRule.value = null
      await fetchRules()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error('保存规则失败:', e)
    ElMessage.error('保存规则失败')
  } finally {
    ruleSaveLoading.value = false
  }
}
</script>

<style scoped>
.btn-sm {
  padding: 4px 12px;
  font-size: 12px;
}

.btn-success {
  background: #67c23a;
  color: #fff;
  border: 1px solid #67c23a;
}
.btn-success:hover {
  background: #85ce61;
}

/* 统计区与Tab之间分割线 */
.section-divider {
  height: 1px;
  background: #e4e7ed;
  margin: 20px 0 4px 0;
}

/* Tab标签式设计 - 缩小与下方内容间距 */
.settle-tabs {
  margin-bottom: 8px;
}

.settle-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.settle-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

/* 筛选区域 */
.filter-card {
  padding: 16px 20px;
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-item label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.filter-actions {
  flex-direction: row;
  align-items: flex-end;
  gap: 8px;
  padding-bottom: 2px;
}

/* 合计金额展示 */
.total-amount-bar {
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  color: #fff;
  padding: 14px 24px;
  border-radius: 8px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
}

.total-label {
  opacity: 0.9;
}

.total-value {
  font-size: 24px;
  font-weight: 700;
  color: #ffd54f;
}

.total-count {
  opacity: 0.7;
  font-size: 13px;
}

.text-center {
  text-align: center;
}
</style>
