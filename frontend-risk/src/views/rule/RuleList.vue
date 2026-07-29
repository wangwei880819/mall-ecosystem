<template>
  <div class="page-container">
    <div class="page-header">
      <h2>🔧 规则管理</h2>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>稽核规则列表</h3>
        <el-button type="primary" @click="showAddRule = true">新增规则</el-button>
      </div>

      <div class="search-bar">
        <el-input v-model="search.name" placeholder="规则名称" style="width: 200px" clearable />
        <el-select v-model="search.type" placeholder="规则类型" style="width: 140px" clearable>
          <el-option label="条件规则" value="CONDITION" />
          <el-option label="脚本规则" value="SCRIPT" />
          <el-option label="频率规则" value="FREQUENCY" />
          <el-option label="关联规则" value="RELATION" />
        </el-select>
        <el-select v-model="search.status" placeholder="状态" style="width: 100px" clearable>
          <el-option label="启用" value="ACTIVE" />
          <el-option label="停用" value="INACTIVE" />
        </el-select>
        <el-button type="primary" @click="fetchRules">搜索</el-button>
      </div>

      <div class="table-container">
        <el-table :data="filteredRules" border stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="规则名称" min-width="180" />
          <el-table-column prop="type" label="规则类型" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ getTypeLabel(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scene" label="适用场景" width="120" />
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="{ row }">
              <el-tag :type="row.priority <= 3 ? 'danger' : row.priority <= 6 ? 'warning' : 'info'" size="small">P{{ row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="condition" label="触发条件" min-width="240" show-overflow-tooltip />
          <el-table-column prop="action" label="处置动作" width="120">
            <template #default="{ row }">
              <el-tag :type="getActionType(row.action)" size="small">{{ getActionLabel(row.action) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="hitCount" label="命中次数" width="100" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-switch v-model="row.active" @change="toggleRule(row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" min-width="150">
            <template #default="{ row }">
              <el-button size="small" @click="editRule(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteRule(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新增/编辑规则弹窗 -->
    <el-dialog v-model="showAddRule" :title="editingRule ? '编辑规则' : '新增规则'" width="650px">
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item label="规则名称" required>
          <el-input v-model="ruleForm.name" placeholder="如：订单金额异常检测" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规则类型">
              <el-select v-model="ruleForm.type" style="width:100%">
                <el-option label="条件规则" value="CONDITION" />
                <el-option label="脚本规则" value="SCRIPT" />
                <el-option label="频率规则" value="FREQUENCY" />
                <el-option label="关联规则" value="RELATION" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用场景">
              <el-select v-model="ruleForm.scene" style="width:100%">
                <el-option label="下单风控" value="下单风控" />
                <el-option label="支付风控" value="支付风控" />
                <el-option label="注册风控" value="注册风控" />
                <el-option label="登录风控" value="登录风控" />
                <el-option label="评价风控" value="评价风控" />
                <el-option label="退款风控" value="退款风控" />
                <el-option label="入驻风控" value="入驻风控" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-input-number v-model="ruleForm.priority" :min="1" :max="10" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处置动作">
              <el-select v-model="ruleForm.action" style="width:100%">
                <el-option label="自动拦截" value="BLOCK" />
                <el-option label="人工审核" value="MANUAL" />
                <el-option label="降权处理" value="DOWNGRADE" />
                <el-option label="仅告警" value="ALERT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="触发条件" required>
          <el-input v-model="ruleForm.condition" type="textarea" :rows="3" placeholder="如：order.amount > avgAmount * 5 AND user.accountAge < 7" />
        </el-form-item>
        <el-form-item label="规则描述">
          <el-input v-model="ruleForm.description" type="textarea" :rows="2" placeholder="规则说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddRule = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRules, updateRule, toggleRule as toggleRuleApi } from '@/api/risk'
import http from '@/utils/http'

const search = reactive({ name: '', type: '', status: '' })
const rules = ref([])
const showAddRule = ref(false)
const editingRule = ref(null)
const ruleForm = reactive({ name: '', type: 'CONDITION', scene: '下单风控', priority: 5, action: 'MANUAL', condition: '', description: '' })

const getTypeLabel = (t) => ({ CONDITION: '条件规则', SCRIPT: '脚本规则', FREQUENCY: '频率规则', RELATION: '关联规则' }[t] || t)
const getActionType = (a) => ({ BLOCK: 'danger', MANUAL: 'warning', DOWNGRADE: 'info', ALERT: '' }[a] || 'info')
const getActionLabel = (a) => ({ BLOCK: '自动拦截', MANUAL: '人工审核', DOWNGRADE: '降权处理', ALERT: '仅告警' }[a] || a)

const filteredRules = computed(() => {
  return rules.value.filter(r => {
    if (search.name && !r.name.includes(search.name)) return false
    if (search.type && r.type !== search.type) return false
    if (search.status === 'ACTIVE' && !r.active) return false
    if (search.status === 'INACTIVE' && r.active) return false
    return true
  })
})

const fetchRules = async () => {
  try {
    const res = await getRules()
    if (res && res.code === 200) {
      rules.value = (res.data || []).map(r => ({
        ...r,
        active: r.active === true || r.active === 1
      }))
    }
  } catch (e) {
    ElMessage.error('获取规则列表失败')
  }
}

const toggleRule = async (row) => {
  try {
    await toggleRuleApi(row.id)
    ElMessage.success(`规则"${row.name}"已${row.active ? '启用' : '停用'}`)
  } catch (e) {
    row.active = !row.active
    ElMessage.error('操作失败')
  }
}

const editRule = (row) => {
  editingRule.value = row
  Object.assign(ruleForm, {
    name: row.name, type: row.type, scene: row.scene, priority: row.priority,
    action: row.action, condition: row.condition || '', description: row.description || ''
  })
  showAddRule.value = true
}

const deleteRule = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除规则"${row.name}"吗？`, '提示', { type: 'warning' })
    await http.delete(`/rules/${row.id}`)
    rules.value = rules.value.filter(r => r.id !== row.id)
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const saveRule = async () => {
  try {
    if (editingRule.value) {
      await updateRule(editingRule.value.id, ruleForm)
      Object.assign(editingRule.value, ruleForm)
      ElMessage.success('规则已更新')
    } else {
      await http.post('/rules', ruleForm)
      ElMessage.success('规则已添加')
      await fetchRules()
    }
    showAddRule.value = false
    editingRule.value = null
    Object.assign(ruleForm, { name: '', type: 'CONDITION', scene: '下单风控', priority: 5, action: 'MANUAL', condition: '', description: '' })
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(fetchRules)
</script>
