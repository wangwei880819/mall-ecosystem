<template>
  <div class="log-float" :class="{ collapsed: !expanded, expanded: expanded }" :style="panelStyle">
    <div class="log-float-header" @dblclick="toggleExpand">
      <span class="log-float-title" @click="toggleExpand" style="cursor:pointer">
        📋 请求监控 {{ expanded ? '' : `(${logs.length})` }}
        <span v-if="!enabled" class="log-disabled-badge">已暂停</span>
      </span>
      <div class="log-float-actions">
        <button class="log-float-btn" @click="toggleEnabled" :title="enabled ? '暂停拦截' : '启用拦截'">
          {{ enabled ? '⏸' : '▶' }}
        </button>
        <button class="log-float-btn" @click="clearLogs" title="清空">🗑</button>
        <button class="log-float-btn" @click="toggleExpand" :title="expanded ? '收起' : '展开'">
          {{ expanded ? '▼' : '▲' }}
        </button>
      </div>
    </div>

    <div v-show="expanded" class="log-float-body">
      <div class="log-float-toolbar">
        <select v-model="filterType" style="font-size:11px;padding:2px 4px;border-radius:3px;border:1px solid #ccc">
          <option value="">全部</option>
          <option value="SUCCESS">成功</option>
          <option value="FAILURE">失败</option>
        </select>
        <div style="display:flex;align-items:center;gap:8px">
          <label style="font-size:11px;display:flex;align-items:center;gap:4px;cursor:pointer">
            <input type="checkbox" v-model="autoScroll" /> 自动滚动
          </label>
          <span style="font-size:10px;color:#999">共 {{ logs.length }} 条</span>
        </div>
      </div>
      <div class="log-float-list" ref="logListRef">
        <div v-for="log in filteredLogs" :key="log.id" class="log-float-item"
          :class="{ 'log-error': log.result === 'FAILURE', 'log-pending': log.result === 'PENDING' }" @click="showLogDetail(log)">
          <div class="log-item-header">
            <span class="log-item-method" :class="'method-' + (log.requestMethod || 'GET')">{{ log.requestMethod || 'GET' }}</span>
            <span class="log-item-uri">{{ log.requestUri }}</span>
            <span v-if="log.costTime > 0" class="log-item-time">{{ log.costTime }}ms</span>
            <span v-else class="log-item-time">...</span>
          </div>
          <div class="log-item-meta">
            <span :class="log.result === 'SUCCESS' ? 'log-success' : log.result === 'FAILURE' ? 'log-fail' : 'log-pending-text'">
              {{ log.result === 'SUCCESS' ? '✓' : log.result === 'FAILURE' ? '✗' : '⏳' }}
            </span>
            <span>{{ log.status || '-' }}</span>
            <span>{{ log.channel || '-' }}</span>
            <span>{{ formatTime(log.createTime) }}</span>
          </div>
        </div>
        <div v-if="filteredLogs.length === 0" class="log-empty">
          {{ enabled ? '等待请求...' : '已暂停拦截' }}
        </div>
      </div>
    </div>

    <el-dialog v-model="showDetail" title="请求详情" width="700px" append-to-body>
      <el-descriptions v-if="currentLog" :column="2" border size="small">
        <el-descriptions-item label="请求URI">{{ currentLog.fullUrl || currentLog.requestUri }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="状态码">{{ currentLog.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.costTime > 0 ? currentLog.costTime + 'ms' : '...' }}</el-descriptions-item>
        <el-descriptions-item label="通道">{{ currentLog.channel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求时间">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentLog.requestHeaders" label="请求头" :span="2">
          <pre style="max-height:100px;overflow:auto;background:#f5f7fa;padding:8px;font-size:12px;white-space:pre-wrap">{{ formatJson(currentLog.requestHeaders) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre style="max-height:150px;overflow:auto;background:#f5f7fa;padding:8px;font-size:12px;white-space:pre-wrap">{{ formatJson(currentLog.requestParams) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应内容" :span="2">
          <pre style="max-height:150px;overflow:auto;background:#f5f7fa;padding:8px;font-size:12px;white-space:pre-wrap">{{ formatJson(currentLog.responseBody) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.errorMessage" label="错误信息" :span="2">
          <span style="color:#f56c6c">{{ currentLog.errorMessage }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <div v-show="expanded" class="log-float-resize" @mousedown="startResize"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import requestInterceptor from '../utils/requestInterceptor'

const expanded = ref(false)
const logs = ref([])
const filterType = ref('')
const autoScroll = ref(true)
const enabled = ref(true)
const showDetail = ref(false)
const currentLog = ref(null)
const logListRef = ref(null)
const panelWidth = ref(380)
const panelHeight = ref(400)
let unsubscribe = null

const filteredLogs = computed(() => {
  if (!filterType.value) return logs.value
  return logs.value.filter(l => l.result === filterType.value)
})

const panelStyle = computed(() => ({
  width: expanded.value ? panelWidth.value + 'px' : 'auto',
  height: expanded.value ? panelHeight.value + 'px' : 'auto'
}))

const toggleExpand = () => {
  expanded.value = !expanded.value
  if (expanded.value) {
    nextTick(() => scrollToBottom())
  }
}

const toggleEnabled = () => {
  if (enabled.value) {
    requestInterceptor.disable()
    enabled.value = false
  } else {
    requestInterceptor.enable()
    enabled.value = true
  }
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.substring(11, 19)
}

const formatJson = (obj) => {
  if (!obj) return '-'
  try {
    return JSON.stringify(obj, null, 2)
  } catch {
    return String(obj)
  }
}

const clearLogs = () => {
  requestInterceptor.clear()
  logs.value = []
  // 清除后重新加载历史
  const existing = requestInterceptor.getLogs()
  if (existing.length > 0) {
    logs.value = existing
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (logListRef.value && autoScroll.value) {
      logListRef.value.scrollTop = logListRef.value.scrollHeight
    }
  })
}

const showLogDetail = (log) => {
  currentLog.value = log
  showDetail.value = true
}

let isResizing = false
let startX = 0
let startY = 0
let startWidth = 0
let startHeight = 0

const startResize = (e) => {
  isResizing = true
  startX = e.clientX
  startY = e.clientY
  startWidth = panelWidth.value
  startHeight = panelHeight.value
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

const onResize = (e) => {
  if (!isResizing) return
  panelWidth.value = Math.max(300, Math.min(800, startWidth - (e.clientX - startX)))
  panelHeight.value = Math.max(200, Math.min(800, startHeight + (e.clientY - startY)))
}

const stopResize = () => {
  isResizing = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

onMounted(() => {
  requestInterceptor.enable()
  // 加载已有日志
  logs.value = requestInterceptor.getLogs()
  // 监听新日志
  unsubscribe = requestInterceptor.onLog((log) => {
    logs.value = [...logs.value, log].slice(-200)
    scrollToBottom()
  })
})

onUnmounted(() => {
  requestInterceptor.disable()
  if (unsubscribe) {
    unsubscribe()
    unsubscribe = null
  }
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})
</script>

<style scoped>
.log-float {
  position: fixed;
  bottom: 16px;
  right: 16px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.2s, height 0.2s;
  font-family: 'Microsoft YaHei', sans-serif;
}

.log-float.collapsed {
  border-radius: 8px;
}

.log-float-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #2c3e50;
  color: #fff;
  cursor: default;
  user-select: none;
  flex-shrink: 0;
}

.log-float-title {
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.log-disabled-badge {
  font-size: 10px;
  background: #e6a23c;
  color: #fff;
  padding: 1px 6px;
  border-radius: 10px;
}

.log-float-actions {
  display: flex;
  gap: 2px;
}

.log-float-btn {
  background: rgba(255,255,255,0.15);
  border: none;
  color: #fff;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.log-float-btn:hover {
  background: rgba(255,255,255,0.3);
}

.log-float-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.log-float-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.log-float-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.log-float-item {
  padding: 6px 10px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.15s;
}

.log-float-item:hover {
  background: #f5f9ff;
}

.log-float-item.log-error {
  background: #fff5f5;
}

.log-float-item.log-error:hover {
  background: #ffebeb;
}

.log-float-item.log-pending {
  background: #fefbe6;
}

.log-item-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.log-item-method {
  font-weight: 600;
  font-size: 11px;
  padding: 1px 4px;
  border-radius: 3px;
  min-width: 36px;
  text-align: center;
}

.method-GET { background: #e6f7e6; color: #52c41a; }
.method-POST { background: #e6f0ff; color: #1890ff; }
.method-PUT { background: #fff7e6; color: #fa8c16; }
.method-DELETE { background: #ffe6e6; color: #f5222d; }

.log-item-uri {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}

.log-item-time {
  color: #999;
  font-size: 11px;
  white-space: nowrap;
}

.log-item-meta {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.log-success { color: #52c41a; }
.log-fail { color: #f5222d; }
.log-pending-text { color: #e6a23c; }

.log-empty {
  text-align: center;
  padding: 20px;
  color: #ccc;
  font-size: 13px;
}

.log-float-resize {
  position: absolute;
  top: 0;
  left: 0;
  width: 6px;
  height: 100%;
  cursor: w-resize;
}

.log-float-resize:hover {
  background: rgba(0,0,0,0.05);
}
</style>