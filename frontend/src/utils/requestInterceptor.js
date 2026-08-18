/**
 * 前端请求拦截器 — 纯前端实现，不依赖后端
 * 拦截 XMLHttpRequest 和 fetch 请求，记录请求/响应信息
 */

const listeners = []
let enabled = false
let requestIdCounter = 0

// 存储拦截到的日志
const interceptedLogs = []

function notify(log) {
  interceptedLogs.push(log)
  if (interceptedLogs.length > 200) interceptedLogs.shift()
  listeners.forEach(fn => {
    try { fn(log) } catch (e) { /* ignore */ }
  })
}

function sanitizeBody(body) {
  if (!body) return null
  if (typeof body === 'string') {
    try { return JSON.parse(body) } catch { return body.substring(0, 2000) }
  }
  try { return JSON.parse(JSON.stringify(body)) } catch { return String(body).substring(0, 2000) }
}

function formatTime() {
  const now = new Date()
  return now.getFullYear() + '-' + 
    String(now.getMonth() + 1).padStart(2, '0') + '-' +
    String(now.getDate()).padStart(2, '0') + ' ' +
    String(now.getHours()).padStart(2, '0') + ':' +
    String(now.getMinutes()).padStart(2, '0') + ':' +
    String(now.getSeconds()).padStart(2, '0')
}

// ---- 拦截 XMLHttpRequest ----
const OrigXHR = window.XMLHttpRequest
const origOpen = OrigXHR.prototype.open
const origSend = OrigXHR.prototype.send

OrigXHR.prototype.open = function (method, url, ...rest) {
  this._reqId = ++requestIdCounter
  this._reqMethod = method
  this._reqUrl = url
  this._reqStartTime = Date.now()
  return origOpen.apply(this, [method, url, ...rest])
}

OrigXHR.prototype.send = function (body) {
  const xhr = this
  const log = {
    id: xhr._reqId,
    requestMethod: xhr._reqMethod,
    requestUri: cleanUrl(xhr._reqUrl),
    fullUrl: xhr._reqUrl,
    requestParams: sanitizeBody(body),
    createTime: formatTime(),
    requestHeaders: null,
    status: 0,
    responseBody: null,
    costTime: 0,
    result: 'PENDING',
    channel: 'XHR'
  }

  // 记录请求头
  const origSetRequestHeader = xhr.setRequestHeader
  const headers = {}
  xhr.setRequestHeader = function (name, value) {
    headers[name] = value
    return origSetRequestHeader.apply(xhr, [name, value])
  }

  xhr.addEventListener('loadend', function () {
    log.status = xhr.status
    log.costTime = Date.now() - xhr._reqStartTime
    log.result = (xhr.status >= 200 && xhr.status < 400) ? 'SUCCESS' : 'FAILURE'
    log.requestHeaders = headers
    log.errorMessage = xhr.status >= 400 ? ('HTTP ' + xhr.status) : null
    try {
      log.responseBody = sanitizeBody(xhr.responseText)
    } catch (e) {
      log.responseBody = '[解析失败]'
    }
    if (enabled) notify(log)
  })

  return origSend.apply(xhr, [body])
}

// ---- 拦截 fetch ----
const origFetch = window.fetch
window.fetch = function (input, init = {}) {
  const id = ++requestIdCounter
  const method = (init.method || 'GET').toUpperCase()
  const url = typeof input === 'string' ? input : (input.url || '')
  const startTime = Date.now()

  const log = {
    id,
    requestMethod: method,
    requestUri: cleanUrl(url),
    fullUrl: url,
    requestParams: sanitizeBody(init.body),
    createTime: formatTime(),
    requestHeaders: init.headers || null,
    status: 0,
    responseBody: null,
    costTime: 0,
    result: 'PENDING',
    channel: 'FETCH'
  }

  return origFetch.apply(window, [input, init]).then(response => {
    log.status = response.status
    log.costTime = Date.now() - startTime
    log.result = response.ok ? 'SUCCESS' : 'FAILURE'
    log.errorMessage = response.ok ? null : ('HTTP ' + response.status)
    // 克隆响应以便读取 body
    const cloned = response.clone()
    cloned.text().then(text => {
      log.responseBody = sanitizeBody(text)
    }).catch(() => {
      log.responseBody = '[无法读取响应体]'
    }).finally(() => {
      if (enabled) notify(log)
    })
    return response
  }).catch(err => {
    log.status = 0
    log.costTime = Date.now() - startTime
    log.result = 'FAILURE'
    log.errorMessage = err.message
    if (enabled) notify(log)
    throw err
  })
}

function cleanUrl(url) {
  if (!url) return ''
  // 去掉 origin 部分，只保留路径
  try {
    const u = new URL(url, window.location.origin)
    return u.pathname + u.search
  } catch {
    return url
  }
}

export default {
  /** 启用拦截 */
  enable() {
    enabled = true
  },

  /** 禁用拦截 */
  disable() {
    enabled = false
  },

  /** 是否已启用 */
  isEnabled() {
    return enabled
  },

  /** 获取所有已拦截日志 */
  getLogs() {
    return interceptedLogs.slice()
  },

  /** 清空日志 */
  clear() {
    interceptedLogs.length = 0
  },

  /** 添加监听器，每次有新日志时回调 */
  onLog(fn) {
    listeners.push(fn)
    return () => {
      const idx = listeners.indexOf(fn)
      if (idx > -1) listeners.splice(idx, 1)
    }
  }
}