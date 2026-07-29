<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📊 风控看板</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card red">
        <div class="stat-label">今日拦截风险</div>
        <div class="stat-value">{{ dashboard.todayBlocks }}</div>
        <div class="stat-sub">↑ {{ dashboard.blockTrend }}% vs 昨日</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">待处理告警</div>
        <div class="stat-value">{{ dashboard.pendingAlerts }}</div>
        <div class="stat-sub">高风险 {{ dashboard.highRiskCount }} 条</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">规则命中率</div>
        <div class="stat-value">{{ dashboard.ruleHitRate }}%</div>
        <div class="stat-sub">累计命中 {{ dashboard.totalHits }} 次</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">稽核准确率</div>
        <div class="stat-value">{{ dashboard.auditAccuracy }}%</div>
        <div class="stat-sub">近30天统计</div>
      </div>
    </div>

    <div class="grid-2">
      <!-- 风险等级分布 -->
      <div class="card">
        <div class="card-header"><h3>风险等级分布</h3></div>
        <div class="chart-container" ref="riskChartRef"></div>
      </div>

      <!-- 最近告警 -->
      <div class="card">
        <div class="card-header">
          <h3>最近告警</h3>
          <el-button link type="primary" @click="$router.push('/events')">查看全部</el-button>
        </div>
        <div class="table-container">
          <el-table :data="dashboard.recentAlerts" size="small" stripe>
            <el-table-column prop="eventType" label="事件类型" width="120" />
            <el-table-column prop="target" label="目标对象" min-width="140" />
            <el-table-column prop="riskLevel" label="风险等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getRiskTagType(row.riskLevel)" size="small">{{ row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ruleName" label="命中规则" width="140" />
            <el-table-column prop="time" label="时间" width="160" />
          </el-table>
        </div>
      </div>
    </div>

    <!-- 规则命中TOP10 -->
    <div class="card" style="margin-top: 16px">
      <div class="card-header"><h3>规则命中 TOP10</h3></div>
      <div class="chart-container" ref="ruleChartRef"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboard, getRules } from '@/api/risk'

const riskChartRef = ref(null)
const ruleChartRef = ref(null)

const getRiskTagType = (level) => {
  const map = { '高风险': 'danger', '中风险': 'warning', '低风险': 'info' }
  return map[level] || 'info'
}

const dashboard = reactive({
  todayBlocks: 0,
  blockTrend: 0,
  pendingAlerts: 0,
  highRiskCount: 0,
  ruleHitRate: 0,
  totalHits: 0,
  auditAccuracy: 0,
  recentAlerts: []
})

const fetchDashboard = async () => {
  try {
    const res = await getDashboard()
    if (res && res.code === 200) {
      const d = res.data || {}
      dashboard.todayBlocks = d.todayBlocks || 0
      dashboard.blockTrend = d.blockTrend || 0
      dashboard.pendingAlerts = d.pendingAlerts || 0
      dashboard.highRiskCount = d.highRiskCount || 0
      dashboard.ruleHitRate = d.ruleHitRate || 0
      dashboard.totalHits = d.totalHits || 0
      dashboard.auditAccuracy = d.auditAccuracy || 0
      dashboard.recentAlerts = d.recentAlerts || []
    }
  } catch (e) {
    console.error('获取看板数据失败', e)
  }
}

const initRiskChart = () => {
  if (!riskChartRef.value) return
  const chart = echarts.init(riskChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: [
        { value: dashboard.highRiskCount || 15, name: '高风险' },
        { value: 38, name: '中风险' },
        { value: 74, name: '低风险' }
      ],
      color: ['#f44336', '#ff9800', '#2196f3']
    }]
  })
}

const initRuleChart = async () => {
  if (!ruleChartRef.value) return
  try {
    const res = await getRules()
    let ruleData = []
    if (res && res.code === 200) {
      ruleData = (res.data || []).sort((a, b) => (b.hitCount || 0) - (a.hitCount || 0)).slice(0, 10)
    }
    const names = ruleData.map(r => r.name)
    const values = ruleData.map(r => r.hitCount || 0)

    const chart = echarts.init(ruleChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: {
        type: 'category',
        data: names
      },
      series: [{
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#c62828' },
            { offset: 1, color: '#ef5350' }
          ])
        }
      }]
    })
  } catch (e) {
    console.error('加载规则图表失败', e)
  }
}

onMounted(async () => {
  await fetchDashboard()
  nextTick(() => {
    initRiskChart()
  })
  nextTick(async () => {
    await initRuleChart()
  })
})
</script>
