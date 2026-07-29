<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📈 数据分析</h2>
    </div>

    <div class="stat-cards">
      <div class="stat-card red">
        <div class="stat-label">总稽核次数</div>
        <div class="stat-value">{{ analysis.totalAudits }}</div>
        <div class="stat-sub">本月 {{ analysis.monthAudits }} 次</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">拦截成功率</div>
        <div class="stat-value">{{ analysis.blockRate }}%</div>
        <div class="stat-sub">误拦截率 {{ analysis.falsePositiveRate }}%</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">平均响应时间</div>
        <div class="stat-value">{{ analysis.avgResponse }}ms</div>
        <div class="stat-sub">P99: {{ analysis.p99Response }}ms</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">规则覆盖场景</div>
        <div class="stat-value">{{ analysis.sceneCount }}</div>
        <div class="stat-sub">累计规则 {{ analysis.ruleCount }} 条</div>
      </div>
    </div>

    <div class="grid-2">
      <!-- 风控趋势 -->
      <div class="card">
        <div class="card-header"><h3>近30天风控拦截趋势</h3></div>
        <div class="chart-container" ref="trendChartRef"></div>
      </div>

      <!-- 事件类型分布 -->
      <div class="card">
        <div class="card-header"><h3>风控事件类型分布</h3></div>
        <div class="chart-container" ref="eventTypeChartRef"></div>
      </div>
    </div>

    <!-- 稽核记录 -->
    <div class="card" style="margin-top: 16px">
      <div class="card-header">
        <h3>稽核记录</h3>
        <div class="search-bar">
          <el-date-picker v-model="search.date" type="date" placeholder="选择日期" style="width: 160px" />
          <el-select v-model="search.result" placeholder="稽核结果" style="width: 120px" clearable>
            <el-option label="拦截" value="BLOCK" />
            <el-option label="放行" value="PASS" />
            <el-option label="人工审核" value="MANUAL" />
          </el-select>
          <el-button type="primary" @click="fetchAuditLogs">查询</el-button>
        </div>
      </div>
      <div class="table-container">
        <el-table :data="filteredLogs" border stripe>
          <el-table-column prop="id" label="编号" width="160" />
          <el-table-column prop="eventType" label="事件类型" width="120" />
          <el-table-column prop="target" label="稽核对象" min-width="160" />
          <el-table-column prop="ruleName" label="命中规则" width="160" />
          <el-table-column prop="riskScore" label="风险评分" width="100">
            <template #default="{ row }">
              <span :style="{ color: row.riskScore >= 80 ? '#f44336' : row.riskScore >= 50 ? '#ff9800' : '#4caf50', fontWeight: 'bold' }">{{ row.riskScore }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="result" label="稽核结果" width="100">
            <template #default="{ row }">
              <el-tag :type="getResultTag(row.result)" size="small">{{ getResultLabel(row.result) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditor" label="稽核人" width="100" />
          <el-table-column prop="auditTime" label="稽核时间" width="180" />
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        </el-table>
      </div>
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="totalLogs"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { getAnalysis, getAuditLogs, getEvents } from '@/api/risk'

const trendChartRef = ref(null)
const eventTypeChartRef = ref(null)
const search = reactive({ date: '', result: '' })
const page = ref(1)

const analysis = reactive({
  totalAudits: 0, monthAudits: 0, blockRate: 0, falsePositiveRate: 0,
  avgResponse: 0, p99Response: 0, sceneCount: 0, ruleCount: 0
})

const auditLogs = ref([])
const totalLogs = ref(0)

const filteredLogs = computed(() => {
  let list = auditLogs.value
  if (search.result) {
    list = list.filter(l => l.result === search.result)
  }
  const start = (page.value - 1) * 10
  return list.slice(start, start + 10)
})

const getResultTag = (r) => ({ BLOCK: 'danger', PASS: 'success', MANUAL: 'warning', BLOCKED: 'danger', PASSED: 'success' }[r] || 'info')
const getResultLabel = (r) => ({ BLOCK: '拦截', PASS: '放行', MANUAL: '人工审核', BLOCKED: '拦截', PASSED: '放行' }[r] || r)

const fetchAnalysis = async () => {
  try {
    const res = await getAnalysis()
    if (res && res.code === 200) {
      const d = res.data || {}
      analysis.totalAudits = d.totalAudits || 0
      analysis.monthAudits = d.monthAudits || 0
      analysis.blockRate = d.blockRate || 0
      analysis.falsePositiveRate = d.falsePositiveRate || 0
      analysis.avgResponse = d.avgResponse || 0
      analysis.p99Response = d.p99Response || 0
      analysis.sceneCount = d.sceneCount || 0
      analysis.ruleCount = d.ruleCount || 0
    }
  } catch (e) {
    console.error('获取分析数据失败', e)
  }
}

const fetchAuditLogs = async () => {
  try {
    const res = await getAuditLogs()
    if (res && res.code === 200) {
      auditLogs.value = res.data || []
      totalLogs.value = auditLogs.value.length
    }
  } catch (e) {
    console.error('获取稽核日志失败', e)
  }
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  const days = Array.from({ length: 30 }, (_, i) => {
    const d = new Date('2026-07-27')
    d.setDate(d.getDate() - (29 - i))
    return `${d.getMonth() + 1}/${d.getDate()}`
  })
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['拦截数', '放行数', '人工审核数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: days, axisLabel: { rotate: 45, fontSize: 10 } },
    yAxis: { type: 'value' },
    series: [
      { name: '拦截数', type: 'line', data: [35,42,38,45,50,48,55,52,58,60,62,55,58,65,60,63,58,62,68,70,65,72,68,75,70,78,72,80,75,82], smooth: true, lineStyle: { color: '#f44336' }, itemStyle: { color: '#f44336' } },
      { name: '放行数', type: 'line', data: [120,115,125,130,128,135,140,138,142,145,140,148,145,150,155,152,158,155,160,165,162,168,170,165,172,175,170,178,175,180], smooth: true, lineStyle: { color: '#4caf50' }, itemStyle: { color: '#4caf50' } },
      { name: '人工审核数', type: 'line', data: [15,18,12,16,14,20,22,18,24,20,22,20,18,22,25,20,24,22,28,25,22,26,24,28,30,25,28,24,25,28], smooth: true, lineStyle: { color: '#ff9800' }, itemStyle: { color: '#ff9800' } }
    ]
  })
}

const initEventTypeChart = async () => {
  if (!eventTypeChartRef.value) return
  try {
    const res = await getEvents()
    let events = []
    if (res && res.code === 200) {
      events = res.data || []
    }
    // 统计事件类型分布
    const typeMap = {}
    events.forEach(e => {
      const t = e.eventType || '未知'
      typeMap[t] = (typeMap[t] || 0) + 1
    })
    const pieData = Object.entries(typeMap).map(([name, value]) => ({ name, value }))

    const chart = echarts.init(eventTypeChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['45%', '70%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%' },
        data: pieData.length > 0 ? pieData : [
          { value: 3250, name: '下单风控' }, { value: 2890, name: '注册风控' },
          { value: 2150, name: '支付风控' }, { value: 1820, name: '退款风控' },
          { value: 1560, name: '入驻风控' }, { value: 1280, name: '评价风控' },
          { value: 980, name: '登录风控' }
        ],
        color: ['#f44336', '#e91e63', '#9c27b0', '#673ab7', '#3f51b5', '#2196f3', '#00bcd4']
      }]
    })
  } catch (e) {
    console.error('加载事件类型图表失败', e)
  }
}

onMounted(async () => {
  await fetchAnalysis()
  await fetchAuditLogs()
  nextTick(() => {
    initTrendChart()
  })
  nextTick(async () => {
    await initEventTypeChart()
  })
})
</script>
