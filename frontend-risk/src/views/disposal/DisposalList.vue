<template>
  <div class="page-container">
    <div class="page-header">
      <h2>⚡ 处置管理</h2>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>处置方案列表</h3>
        <el-button type="primary" @click="showAddDialog = true">新增处置方案</el-button>
      </div>

      <div class="table-container">
        <el-table :data="disposals" border stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="方案名称" min-width="180" />
          <el-table-column prop="type" label="处置方式" width="130">
            <template #default="{ row }">
              <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="triggerRule" label="触发规则" width="160" />
          <el-table-column prop="riskLevel" label="适用风险等级" width="130">
            <template #default="{ row }">
              <el-tag :type="getRiskTag(row.riskLevel)" size="small">{{ getRiskLabel(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="持续时间" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="execCount" label="执行次数" width="100" />
          <el-table-column prop="updateTime" label="更新时间" width="180" />
          <el-table-column label="操作" fixed="right" width="180">
            <template #default="{ row }">
              <el-button size="small" @click="editDisposal(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 'ACTIVE' ? 'warning' : 'success'" @click="toggleStatus(row)">
                {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 处置日志 -->
    <div class="card">
      <div class="card-header">
        <h3>处置执行日志</h3>
      </div>
      <div class="table-container">
        <el-table :data="execLogs" border stripe size="small">
          <el-table-column prop="id" label="日志ID" width="180" />
          <el-table-column prop="disposalName" label="处置方案" width="160" />
          <el-table-column prop="target" label="处置对象" min-width="180" />
          <el-table-column prop="result" label="执行结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small">
                {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="执行人" width="100" />
          <el-table-column prop="execTime" label="执行时间" width="180" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button size="small" link type="primary" @click="viewLog(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="showAddDialog" :title="editing ? '编辑处置方案' : '新增处置方案'" width="550px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="方案名称" required>
          <el-input v-model="form.name" placeholder="如：高风险订单自动拦截" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="处置方式">
              <el-select v-model="form.type" style="width:100%">
                <el-option label="自动拦截" value="BLOCK" />
                <el-option label="人工审核" value="MANUAL" />
                <el-option label="Webhook通知" value="WEBHOOK" />
                <el-option label="降权处理" value="DOWNGRADE" />
                <el-option label="限制登录" value="LOGIN_LIMIT" />
                <el-option label="冻结账户" value="FREEZE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级">
              <el-select v-model="form.riskLevel" style="width:100%">
                <el-option label="高风险" value="HIGH" />
                <el-option label="中风险" value="MEDIUM" />
                <el-option label="低风险" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联规则">
          <el-select v-model="form.triggerRule" style="width:100%" filterable placeholder="选择触发规则">
            <el-option v-for="r in availableRules" :key="r.id" :label="r.name" :value="r.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="持续时间">
          <el-select v-model="form.duration" style="width:100%">
            <el-option label="1小时" value="1小时" />
            <el-option label="24小时" value="24小时" />
            <el-option label="7天" value="7天" />
            <el-option label="30天" value="30天" />
            <el-option label="永久" value="永久" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置说明">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDisposal">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDisposals } from '@/api/risk'
import http from '@/utils/http'

const disposals = ref([])
const execLogs = ref([
  { id: 'DISP20260727001', disposalName: '高风险订单自动拦截', target: '订单ORD20260727001 / 商户:数码旗舰店', result: 'SUCCESS', operator: '风控系统', execTime: '2026-07-27 10:32:16' },
  { id: 'DISP20260727002', disposalName: '异常设备登录拦截', target: '设备指纹DEV_UNKNOWN_001', result: 'SUCCESS', operator: '风控系统', execTime: '2026-07-27 09:31:00' },
  { id: 'DISP20260727003', disposalName: '退款异常人工审核', target: '用户137****8003', result: 'SUCCESS', operator: '风控系统', execTime: '2026-07-27 09:45:12' },
  { id: 'DISP20260727004', disposalName: '资质造假冻结账户', target: '商户:XX科技有限公司', result: 'SUCCESS', operator: '风控系统', execTime: '2026-07-27 08:50:15' },
  { id: 'DISP20260727005', disposalName: '敏感评价Webhook通知', target: '商品SPU0001 评价', result: 'FAILED', operator: '风控系统', execTime: '2026-07-27 09:58:05' }
])
const showAddDialog = ref(false)
const editing = ref(false)
const availableRules = ref([])
const editingId = ref(null)
const form = reactive({ name: '', type: 'BLOCK', riskLevel: 'HIGH', triggerRule: '', duration: '24小时', description: '' })

const getTypeTag = (t) => ({ BLOCK: 'danger', MANUAL: 'warning', WEBHOOK: 'info', DOWNGRADE: '', LOGIN_LIMIT: 'danger', FREEZE: 'danger' }[t] || 'info')
const getTypeLabel = (t) => ({ BLOCK: '自动拦截', MANUAL: '人工审核', WEBHOOK: 'Webhook通知', DOWNGRADE: '降权处理', LOGIN_LIMIT: '限制登录', FREEZE: '冻结账户' }[t] || t)
const getRiskTag = (l) => ({ HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }[l] || 'info')
const getRiskLabel = (l) => ({ HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }[l] || l)

const fetchData = async () => {
  try {
    const res = await getDisposals()
    if (res && res.code === 200) {
      disposals.value = res.data || []
      availableRules.value = (res.data || []).map(d => ({ id: d.id, name: d.triggerRule })).filter(r => r.name)
    }
  } catch (e) {
    ElMessage.error('获取处置方案失败')
  }
}

const editDisposal = (row) => {
  editing.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, type: row.type, riskLevel: row.riskLevel,
    triggerRule: row.triggerRule, duration: row.duration, description: row.description || ''
  })
  showAddDialog.value = true
}

const toggleStatus = async (row) => {
  try {
    await http.put(`/disposals/${row.id}/toggle`)
    row.status = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    ElMessage.success(`方案"${row.name}"已${row.status === 'ACTIVE' ? '启用' : '停用'}`)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const saveDisposal = async () => {
  if (!form.name) { ElMessage.warning('请输入方案名称'); return }
  try {
    if (editing.value) {
      await http.put(`/disposals/${editingId.value}`, form)
      ElMessage.success('方案已更新')
    } else {
      await http.post('/disposals', form)
      ElMessage.success('方案已添加')
    }
    showAddDialog.value = false
    editing.value = false
    editingId.value = null
    Object.assign(form, { name: '', type: 'BLOCK', riskLevel: 'HIGH', triggerRule: '', duration: '24小时', description: '' })
    await fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const viewLog = (row) => { ElMessage.info('日志详情：' + row.id) }

onMounted(fetchData)
</script>
