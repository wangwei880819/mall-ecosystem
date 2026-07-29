<template>
  <div class="page-container">
    <div class="page-header">
      <h2>⚠️ 风控事件管理</h2>
    </div>

    <div class="card">
      <div class="search-bar">
        <el-input v-model="search.eventType" placeholder="事件类型" style="width: 160px" clearable @change="fetchData" />
        <el-select v-model="search.riskLevel" placeholder="风险等级" style="width: 120px" clearable @change="fetchData">
          <el-option label="高风险" value="HIGH" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="低风险" value="LOW" />
        </el-select>
        <el-select v-model="search.status" placeholder="状态" style="width: 120px" clearable @change="fetchData">
          <el-option label="待处理" value="PENDING" />
          <el-option label="已拦截" value="BLOCKED" />
          <el-option label="已放行" value="PASSED" />
          <el-option label="人工审核" value="MANUAL" />
        </el-select>
        <el-date-picker v-model="search.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" @change="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>

      <div class="table-container">
        <el-table :data="events" border stripe>
          <el-table-column prop="id" label="事件ID" width="140" />
          <el-table-column prop="eventType" label="事件类型" width="120" />
          <el-table-column prop="target" label="目标对象" min-width="160" />
          <el-table-column prop="riskLevel" label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getRiskType(row.riskLevel)" size="small">{{ getRiskLabel(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="风险评分" width="100">
            <template #default="{ row }">
              <span :style="{ color: row.score >= 80 ? '#f44336' : row.score >= 50 ? '#ff9800' : '#4caf50', fontWeight: 'bold' }">{{ row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="hitRule" label="命中规则" width="160" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="source" label="来源" width="120" />
          <el-table-column prop="createTime" label="触发时间" width="180" />
          <el-table-column label="操作" fixed="right" min-width="260">
            <template #default="{ row }">
              <el-button size="small" @click="viewDetail(row)">详情</el-button>
              <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="blockEvent(row)">拦截</el-button>
              <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="passEvent(row)">放行</el-button>
              <el-button v-if="row.status === 'PENDING'" size="small" type="warning" @click="manualReview(row)">审核</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="showDetail" title="事件详情" width="700px">
      <el-descriptions v-if="currentEvent" :column="2" border>
        <el-descriptions-item label="事件ID">{{ currentEvent.id }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">{{ currentEvent.eventType }}</el-descriptions-item>
        <el-descriptions-item label="目标对象">{{ currentEvent.target }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">{{ getRiskLabel(currentEvent.riskLevel) }}</el-descriptions-item>
        <el-descriptions-item label="风险评分">{{ currentEvent.score }}</el-descriptions-item>
        <el-descriptions-item label="命中规则">{{ currentEvent.hitRule }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ currentEvent.source }}</el-descriptions-item>
        <el-descriptions-item label="触发时间">{{ currentEvent.createTime }}</el-descriptions-item>
        <el-descriptions-item label="事件详情" :span="2">{{ currentEvent.detail || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEvents } from '@/api/risk'
import http from '@/utils/http'

const search = reactive({ eventType: '', riskLevel: '', status: '', dateRange: [] })
const events = ref([])
const showDetail = ref(false)
const currentEvent = ref(null)

const getRiskType = (level) => ({ HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }[level] || 'info')
const getRiskLabel = (level) => ({ HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }[level] || level)
const getStatusType = (s) => ({ PENDING: 'warning', BLOCKED: 'danger', PASSED: 'success', MANUAL: 'info' }[s] || 'info')
const getStatusLabel = (s) => ({ PENDING: '待处理', BLOCKED: '已拦截', PASSED: '已放行', MANUAL: '人工审核' }[s] || s)

const fetchData = async () => {
  try {
    const res = await getEvents({
      eventType: search.eventType || undefined,
      riskLevel: search.riskLevel || undefined,
      status: search.status || undefined
    })
    if (res && res.code === 200) {
      events.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('获取事件列表失败')
  }
}

const viewDetail = (row) => { currentEvent.value = row; showDetail.value = true }

const updateEventStatus = async (row, status) => {
  try {
    const idNum = parseInt(String(row.id).replace('EVT', '')) || row.id
    await http.put(`/events/${idNum}/status`, { status })
    row.status = status
    ElMessage.success(status === 'BLOCKED' ? '已拦截' : status === 'PASSED' ? '已放行' : '已转人工审核')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const blockEvent = (row) => updateEventStatus(row, 'BLOCKED')
const passEvent = (row) => updateEventStatus(row, 'PASSED')
const manualReview = (row) => updateEventStatus(row, 'MANUAL')

onMounted(fetchData)
</script>
