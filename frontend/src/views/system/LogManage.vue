<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📋 日志管理</h2>
    </div>

    <div class="filter-bar">
      <el-select v-model="filter.logType" placeholder="日志类型" clearable style="width:140px">
        <el-option v-for="t in logTypes" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="filter.module" placeholder="业务模块" clearable style="width:140px">
        <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
      </el-select>
      <el-input v-model="filter.operator" placeholder="操作人" clearable style="width:160px" />
      <el-select v-model="filter.result" placeholder="操作结果" clearable style="width:120px">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILURE" />
      </el-select>
      <el-date-picker v-model="filter.timeRange" type="datetimerange" range-separator="至"
        start-placeholder="开始时间" end-placeholder="结束时间" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"
        style="width:360px" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div class="table-container">
      <el-table :data="pagedLogs" border stripe v-loading="loading" max-height="calc(100vh - 260px)">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="logType" label="日志类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.logType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="业务模块" width="110" />
        <el-table-column prop="operation" label="操作类型" width="90" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="requestMethod" label="请求方式" width="80">
          <template #default="{ row }">
            <el-tag :type="methodTag(row.requestMethod)" size="small">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUri" label="请求URI" min-width="200" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="90" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column label="操作时间" width="170">
          <template #default="{ row }">{{ row.createTime ? row.createTime.substring(0, 19) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="80">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:center;margin-top:16px">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @change="fetchLogs" />
      </div>
    </div>

    <el-dialog v-model="showDetailDialog" title="日志详情" width="800px" :close-on-click-modal="false">
      <el-descriptions v-if="detailLog" :column="2" border size="small">
        <el-descriptions-item label="日志ID">{{ detailLog.id }}</el-descriptions-item>
        <el-descriptions-item label="日志类型">{{ detailLog.logType }}</el-descriptions-item>
        <el-descriptions-item label="业务模块">{{ detailLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detailLog.operation }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ detailLog.operatorId }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URI">{{ detailLog.requestUri }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="UserAgent" :span="2">{{ detailLog.userAgent }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="detailLog.result === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ detailLog.result === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailLog.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre style="max-height:200px;overflow:auto;background:#f5f7fa;padding:8px;border-radius:4px;font-size:12px;white-space:pre-wrap">{{ formatJson(detailLog.requestParams) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应内容" :span="2">
          <pre style="max-height:200px;overflow:auto;background:#f5f7fa;padding:8px;border-radius:4px;font-size:12px;white-space:pre-wrap">{{ formatJson(detailLog.responseBody) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailLog.errorMessage" label="错误信息" :span="2">
          <span style="color:#f56c6c">{{ detailLog.errorMessage }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const showDetailDialog = ref(false)
const detailLog = ref(null)

const filter = ref({
  logType: '',
  module: '',
  operator: '',
  result: '',
  timeRange: null
})

const logTypes = [
  { value: 'AUTH', label: '认证' },
  { value: 'MERCHANT', label: '商户' },
  { value: 'PRODUCT', label: '商品' },
  { value: 'ORDER', label: '订单' },
  { value: 'FINANCE', label: '财务' },
  { value: 'RISK', label: '风险' },
  { value: 'CUSTOMER', label: '客户' },
  { value: 'AI', label: 'AI服务' },
  { value: 'SYSTEM', label: '系统' },
  { value: 'OTHER', label: '其他' }
]

const modules = ['商户管理', '商品管理', '订单管理', '财务管理', '风险管理', '客户管理', '系统管理', 'AI服务', 'C端商城', '其他']

const pagedLogs = computed(() => logs.value)

const methodTag = (method) => {
  const map = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (filter.value.logType) params.logType = filter.value.logType
    if (filter.value.module) params.module = filter.value.module
    if (filter.value.operator) params.operator = filter.value.operator
    if (filter.value.result) params.result = filter.value.result
    if (filter.value.timeRange && filter.value.timeRange.length === 2) {
      params.startTime = filter.value.timeRange[0]
      params.endTime = filter.value.timeRange[1]
    }
    const res = await request.get('/log/list', { params })
    if (res.code === 200) {
      logs.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('获取日志失败', e)
  } finally {
    loading.value = false
  }
}

const search = () => {
  currentPage.value = 1
  fetchLogs()
}

const reset = () => {
  filter.value = { logType: '', module: '', operator: '', result: '', timeRange: null }
  currentPage.value = 1
  fetchLogs()
}

const showDetail = (row) => {
  detailLog.value = row
  showDetailDialog.value = true
}

const formatJson = (str) => {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; color: #333; font-size: 20px; }
.filter-bar { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 16px; align-items: center; }
.table-container { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,.1); padding: 20px; }
</style>