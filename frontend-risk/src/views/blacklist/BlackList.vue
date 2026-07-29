<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📋 名单库管理</h2>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>黑白名单</h3>
        <el-button type="primary" @click="showAddDialog = true">添加名单</el-button>
      </div>

      <div class="search-bar">
        <el-input v-model="search.keyword" placeholder="搜索值" style="width: 220px" clearable />
        <el-select v-model="search.type" placeholder="名单类型" style="width: 120px" clearable>
          <el-option label="黑名单" value="BLACK" />
          <el-option label="白名单" value="WHITE" />
          <el-option label="灰名单" value="GRAY" />
        </el-select>
        <el-select v-model="search.listType" placeholder="数据类型" style="width: 140px" clearable>
          <el-option label="手机号" value="PHONE" />
          <el-option label="IP地址" value="IP" />
          <el-option label="设备指纹" value="DEVICE" />
          <el-option label="商户编号" value="MERCHANT" />
          <el-option label="统一社会信用代码" value="CREDIT_CODE" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <div class="table-container">
        <el-table :data="filteredData" border stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="value" label="名单值" min-width="200" />
          <el-table-column prop="type" label="数据类型" width="160">
            <template #default="{ row }">
              <el-tag size="small">{{ getListTypeLabel(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="listType" label="名单类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getListTagType(row.listType)" size="small">{{ getListLabel(row.listType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="加入原因" min-width="200" show-overflow-tooltip />
          <el-table-column prop="source" label="来源" width="120" />
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="createTime" label="加入时间" width="180" />
          <el-table-column prop="expireTime" label="过期时间" width="180">
            <template #default="{ row }">
              {{ row.expireTime || '永久' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="100">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="removeItem(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="showAddDialog" title="添加名单" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名单值" required>
          <el-input v-model="form.value" placeholder="手机号/IP/设备指纹等" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="数据类型">
              <el-select v-model="form.type" style="width:100%">
                <el-option label="手机号" value="PHONE" />
                <el-option label="IP地址" value="IP" />
                <el-option label="设备指纹" value="DEVICE" />
                <el-option label="商户编号" value="MERCHANT" />
                <el-option label="统一社会信用代码" value="CREDIT_CODE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名单类型">
              <el-select v-model="form.listType" style="width:100%">
                <el-option label="黑名单" value="BLACK" />
                <el-option label="白名单" value="WHITE" />
                <el-option label="灰名单" value="GRAY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="加入原因">
          <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="说明加入原因" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="form.expireTime" type="datetime" placeholder="不填则永久有效" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addItem">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBlackList, addBlackItem, removeBlackItem } from '@/api/risk'

const search = reactive({ keyword: '', type: '', listType: '' })
const listData = ref([])
const showAddDialog = ref(false)
const form = reactive({ value: '', type: 'PHONE', listType: 'BLACK', reason: '', expireTime: null })

const getListTypeLabel = (t) => ({ PHONE: '手机号', IP: 'IP地址', DEVICE: '设备指纹', MERCHANT: '商户编号', CREDIT_CODE: '统一社会信用代码' }[t] || t)
const getListTagType = (t) => ({ BLACK: 'danger', WHITE: 'success', GRAY: 'warning' }[t] || 'info')
const getListLabel = (t) => ({ BLACK: '黑名单', WHITE: '白名单', GRAY: '灰名单' }[t] || t)

const filteredData = computed(() => {
  return listData.value.filter(item => {
    if (search.keyword && !item.value.includes(search.keyword)) return false
    if (search.type && item.listType !== search.type) return false
    if (search.listType && item.type !== search.listType) return false
    return true
  })
})

const fetchList = async () => {
  try {
    const res = await getBlackList({
      keyword: search.keyword || undefined,
      type: search.type || undefined,
      listType: search.listType || undefined
    })
    if (res && res.code === 200) {
      listData.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('获取名单列表失败')
  }
}

const addItem = async () => {
  if (!form.value) { ElMessage.warning('请输入名单值'); return }
  try {
    await addBlackItem({ ...form })
    ElMessage.success('添加成功')
    showAddDialog.value = false
    Object.assign(form, { value: '', type: 'PHONE', listType: 'BLACK', reason: '', expireTime: null })
    await fetchList()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const removeItem = async (row) => {
  try {
    await ElMessageBox.confirm(`确定将"${row.value}"从名单中移除吗？`, '提示', { type: 'warning' })
    await removeBlackItem(row.id)
    listData.value = listData.value.filter(r => r.id !== row.id)
    ElMessage.success('已移除')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('移除失败')
  }
}

onMounted(fetchList)
</script>
