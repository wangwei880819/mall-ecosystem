<template>
  <div>
    <h1 class="page-title">🛡️ 业务数据稽核 — 风控稽核管理平台</h1>

    <!-- 稽核概览 -->
    <div class="grid-4">
      <div class="stat-card blue">
        <div class="stat-label">稽核总量</div>
        <div class="stat-value">{{ stats.totalChecked.toLocaleString() }}</div>
        <div class="stat-sub">覆盖率 100%</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">通过率</div>
        <div class="stat-value">{{ stats.passRate }}</div>
        <div class="stat-trend up">↑ 0.02%</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">异常数</div>
        <div class="stat-value">{{ stats.anomalyCount }}</div>
        <div class="stat-sub">待核查 {{ stats.pendingCount }}项</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">追回金额</div>
        <div class="stat-value">¥{{ stats.recoveredAmount.toLocaleString() }}</div>
        <div class="stat-sub">本月已追回</div>
      </div>
    </div>

    <!-- 功能Tab -->
    <div style="display:flex;gap:8px;margin-bottom:16px">
      <button :class="tab === 'list' ? 'btn btn-primary' : 'btn btn-outline'" @click="tab = 'list'">稽核记录</button>
      <button :class="tab === 'rules' ? 'btn btn-primary' : 'btn btn-outline'" @click="tab = 'rules'">规则配置</button>
      <button :class="tab === 'stats' ? 'btn btn-primary' : 'btn btn-outline'" @click="tab = 'stats'">风险统计</button>
    </div>

    <!-- 稽核记录 -->
    <template v-if="tab === 'list'">
      <div class="grid-2">
        <!-- 订单稽核 -->
        <div class="card">
          <div class="card-header">
            <h3>📦 订单稽核</h3>
            <span class="tag tag-blue">全链路核查</span>
          </div>
          <table class="data-table">
            <thead>
              <tr><th>稽核编号</th><th>商户</th><th>风险类型</th><th>风险等级</th><th>金额</th><th>状态</th><th style="width:100px;white-space:nowrap">操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="a in orderAudit" :key="a.id">
                <td>{{ a.auditCode || a.id }}</td>
                <td>{{ a.merchant }}</td>
                <td><span class="tag tag-orange">{{ a.riskType }}</span></td>
                <td><span :class="getRiskClass(a.riskLevel)">{{ a.riskLevel }}</span></td>
                <td>¥{{ a.amount.toLocaleString() }}</td>
                <td><span :class="getStatusClass(a.status)">{{ a.status }}</span></td>
                <td style="white-space:nowrap">
                  <button class="btn btn-sm btn-outline" @click="viewDetail(a)">详情</button>
                  <button v-if="a.status !== 'RESOLVED'" class="btn btn-sm btn-primary" @click="resolve(a)">处理</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 资金稽核 -->
        <div class="card">
          <div class="card-header">
            <h3>💰 资金稽核</h3>
            <span class="tag tag-purple">三类资金核查</span>
          </div>
          <table class="data-table">
            <thead>
              <tr><th>稽核编号</th><th>商户</th><th>风险类型</th><th>风险等级</th><th>金额</th><th>状态</th><th style="width:100px;white-space:nowrap">操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="a in fundAudit" :key="a.id">
                <td>{{ a.auditCode || a.id }}</td>
                <td>{{ a.merchant }}</td>
                <td><span class="tag tag-orange">{{ a.riskType }}</span></td>
                <td><span :class="getRiskClass(a.riskLevel)">{{ a.riskLevel }}</span></td>
                <td>¥{{ a.amount.toLocaleString() }}</td>
                <td><span :class="getStatusClass(a.status)">{{ a.status }}</span></td>
                <td style="white-space:nowrap">
                  <button class="btn btn-sm btn-outline" @click="viewDetail(a)">详情</button>
                  <button v-if="a.status !== 'RESOLVED'" class="btn btn-sm btn-primary" @click="resolve(a)">处理</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 三维度对账 -->
      <h2 class="section-title">三维度资金对账</h2>
      <div class="grid-3">
        <div class="card">
          <div class="card-header"><h3>AI豆对账</h3><span class="tag tag-green">✅ 一致</span></div>
          <p style="font-size:13px;color:#666;line-height:1.8">
            发放量 = 消费量 + 过期量 + 余额量<br>
            发放: <b>326.5万</b> | 消费: <b>285.2万</b> | 过期: <b>12.3万</b> | 余额: <b>29.0万</b>
          </p>
        </div>
        <div class="card">
          <div class="card-header"><h3>佣金对账</h3><span class="tag tag-green">✅ 一致</span></div>
          <p style="font-size:13px;color:#666;line-height:1.8">
            计算总额 = 分账总额 = 结算总额<br>
            计算: <b>¥789.2万</b> | 分账: <b>¥789.2万</b> | 结算: <b>¥789.2万</b>
          </p>
        </div>
        <div class="card">
          <div class="card-header"><h3>商拓费对账</h3><span class="tag tag-orange">⚠️ 差异2笔</span></div>
          <p style="font-size:13px;color:#666;line-height:1.8">
            核算总额 = 审批总额 = 结算总额<br>
            核算: <b>¥170.8万</b> | 审批: <b>¥170.5万</b> | 结算: <b>¥170.5万</b><br>
            <span style="color:#f44336">差异: ¥3,000（2笔待追溯）</span>
          </p>
        </div>
      </div>
    </template>

    <!-- 规则配置 -->
    <template v-if="tab === 'rules'">
      <div class="card">
        <div class="card-header">
          <h3>AI风控规则引擎</h3>
          <button class="btn btn-primary btn-sm" @click="showAddRule = true">+ 添加规则</button>
        </div>
        <table class="data-table">
          <thead>
            <tr><th>规则编号</th><th>规则名称</th><th>规则描述</th><th>阈值</th><th>触发次数</th><th>状态</th><th style="width:80px;white-space:nowrap">操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="r in rules" :key="r.id">
              <td>{{ r.id }}</td>
              <td style="font-weight:600">{{ r.name }}</td>
              <td>{{ r.desc }}</td>
              <td>{{ r.threshold || '-' }}</td>
              <td><span class="tag tag-red">{{ r.triggered }}次</span></td>
              <td><span :class="r.enabled ? 'tag tag-green' : 'tag tag-gray'" @click="toggleRule(r)" style="cursor:pointer">{{ r.enabled ? '已启用' : '已禁用' }}</span></td>
              <td style="white-space:nowrap">
                <button class="btn btn-sm btn-outline" @click="editRule(r)">编辑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- 风险统计 -->
    <template v-if="tab === 'stats'">
      <div class="grid-2">
        <div class="card">
          <div class="card-header"><h3>风险等级分布</h3></div>
          <div style="display:flex;justify-content:space-around;padding:20px">
            <div style="text-align:center">
              <div style="font-size:32px;color:#f44336;font-weight:700">12</div>
              <div style="font-size:12px;color:#666">高风险</div>
            </div>
            <div style="text-align:center">
              <div style="font-size:32px;color:#ff9800;font-weight:700">28</div>
              <div style="font-size:12px;color:#666">中风险</div>
            </div>
            <div style="text-align:center">
              <div style="font-size:32px;color:#4caf50;font-weight:700">45</div>
              <div style="font-size:12px;color:#666">低风险</div>
            </div>
          </div>
        </div>
        <div class="card">
          <div class="card-header"><h3>风险类型TOP5</h3></div>
          <div style="padding:10px 0">
            <div v-for="(item, i) in riskTypes" :key="i" style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid #f0f0f0">
              <span style="color:#666">{{ item.name }}</span>
              <div style="flex:1;margin:0 16px;background:#f0f0f0;height:8px;border-radius:4px">
                <div style="height:100%;width:{{ item.percent }}%;background:linear-gradient(90deg,#f44336,#ff9800);border-radius:4px"></div>
              </div>
              <span style="color:#666;font-weight:600">{{ item.count }}次</span>
            </div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3>趋势分析（近7天）</h3></div>
        <div style="display:flex;justify-content:space-around;padding:20px 0;min-height:200px;align-items:flex-end">
          <div v-for="item in trendData" :key="item.date" style="text-align:center">
            <div style="width:40px;background:linear-gradient(180deg,#667eea,#764ba2);height:{{ item.count * 5 }}px;border-radius:4px 4px 0 0"></div>
            <div style="font-size:12px;color:#666;margin-top:8px">{{ item.date }}</div>
            <div style="font-size:12px;color:#333;font-weight:600">{{ item.count }}</div>
          </div>
        </div>
      </div>
    </template>

    <!-- 详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal">
        <div class="modal-header">
          <h3>风险详情</h3>
          <button class="modal-close" @click="showDetail = false">×</button>
        </div>
        <div class="modal-body" v-if="currentItem">
          <div class="grid-2">
            <div class="form-group">
              <label>稽核编号</label>
              <input type="text" :value="currentItem.auditCode || currentItem.id" readonly />
            </div>
            <div class="form-group">
              <label>风险类型</label>
              <input type="text" :value="currentItem.riskType" readonly />
            </div>
            <div class="form-group">
              <label>风险等级</label>
              <input type="text" :value="currentItem.riskLevel" readonly />
            </div>
            <div class="form-group">
              <label>涉及金额</label>
              <input type="text" :value="'¥' + currentItem.amount.toLocaleString()" readonly />
            </div>
          </div>
          <div class="form-group">
            <label>风险描述</label>
            <textarea rows="3" readonly>{{ currentItem.description || currentItem.desc }}</textarea>
          </div>
        </div>
        <div class="modal-footer">
        </div>
      </div>
    </div>

    <!-- 添加规则弹窗 -->
    <div v-if="showAddRule" class="modal-overlay" @click.self="showAddRule = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingRule ? '编辑规则' : '添加规则' }}</h3>
          <button class="modal-close" @click="showAddRule = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>规则名称</label>
            <input type="text" v-model="newRule.name" placeholder="请输入规则名称" />
          </div>
          <div class="form-group">
            <label>规则描述</label>
            <textarea rows="3" v-model="newRule.desc" placeholder="请输入规则描述"></textarea>
          </div>
          <div class="grid-2">
            <div class="form-group">
              <label>阈值</label>
              <input type="number" v-model.number="newRule.threshold" placeholder="请输入阈值" />
            </div>
            <div class="form-group">
              <label>风险等级</label>
              <select v-model="newRule.riskLevel">
                <option>HIGH</option>
                <option>MEDIUM</option>
                <option>LOW</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="saveRule">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const tab = ref('list')
const showDetail = ref(false)
const showAddRule = ref(false)
const currentItem = ref(null)
const editingRule = ref(null)

const stats = ref({
  totalChecked: 156320,
  passRate: '99.98%',
  anomalyCount: 34,
  pendingCount: 8,
  recoveredAmount: 86500
})

const orderAudit = ref([])
const fundAudit = ref([])
const rules = ref([])

const riskTypes = ref([
  { name: '疑似刷单', count: 23, percent: 30 },
  { name: '虚假交易', count: 18, percent: 24 },
  { name: '异常退款', count: 12, percent: 16 },
  { name: '结算异常', count: 10, percent: 13 },
  { name: '重复结算', count: 5, percent: 7 }
])

const trendData = ref([
  { date: '08-20', count: 12 },
  { date: '08-21', count: 8 },
  { date: '08-22', count: 15 },
  { date: '08-23', count: 10 },
  { date: '08-24', count: 6 },
  { date: '08-25', count: 18 },
  { date: '08-26', count: 9 }
])

const newRule = ref({
  name: '',
  desc: '',
  threshold: 10,
  riskLevel: 'MEDIUM'
})

onMounted(async () => {
  try {
    const rulesRes = await fetch('http://localhost:8081/api/admin/audit/rules')
    const rulesData = await rulesRes.json()
    if (rulesData.code === 200) {
      rules.value = rulesData.data
    }
    const recordsRes = await fetch('http://localhost:8081/api/admin/audit/records')
    const recordsData = await recordsRes.json()
    if (recordsData.code === 200) {
      orderAudit.value = recordsData.data.filter(r => r.auditType === 'ORDER')
      fundAudit.value = recordsData.data.filter(r => r.auditType === 'FUND')
    }
  } catch (e) {
    console.error('获取稽核数据失败', e)
  }
})

const getRiskClass = (level) => {
  if (level === 'HIGH' || level === '高') return 'tag tag-red'
  if (level === 'MEDIUM' || level === '中') return 'tag tag-orange'
  return 'tag tag-green'
}

const getStatusClass = (status) => {
  if (status === 'PENDING' || status === '待核查') return 'tag tag-red'
  if (status === 'CHECKING' || status === '核查中') return 'tag tag-orange'
  return 'tag tag-green'
}

const viewDetail = (item) => {
  currentItem.value = item
  showDetail.value = true
}

const resolve = (item) => {
  if (confirm('确认已处理该风险？')) {
    item.status = 'RESOLVED'
  }
}

const toggleRule = (rule) => {
  rule.enabled = !rule.enabled
}

const editRule = (rule) => {
  editingRule.value = rule
  newRule.value = { ...rule }
  showAddRule.value = true
}

const saveRule = () => {
  if (!newRule.value.name) {
    alert('请输入规则名称')
    return
  }
  if (editingRule.value) {
    Object.assign(editingRule.value, newRule.value)
  } else {
    rules.value.push({
      id: 'R' + Date.now(),
      ...newRule.value,
      triggered: 0,
      enabled: true
    })
  }
  showAddRule.value = false
  editingRule.value = null
  newRule.value = { name: '', desc: '', threshold: 10, riskLevel: 'MEDIUM' }
}
</script>

<style scoped>
.btn-sm {
  padding: 2px 10px;
  font-size: 12px;
}
</style>
