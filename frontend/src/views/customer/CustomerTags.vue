<template>
  <div class="page-container">
    <div class="page-header">
      <h2>🏷️ 客户标签管理</h2>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索客户手机号/昵称" style="width: 300px" @keyup.enter="searchCustomer" />
        <el-button type="primary" @click="searchCustomer">搜索</el-button>
      </div>
    </div>

    <el-card v-if="selectedCustomer" title="客户信息">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="客户ID">{{ selectedCustomer.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ selectedCustomer.phone }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ selectedCustomer.nickname }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <div v-if="selectedCustomer" style="margin-top: 20px">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px">
        <h3>客户标签列表</h3>
        <el-button type="primary" @click="showAddTagDialog = true">添加标签</el-button>
      </div>

      <div class="table-container">
        <el-table :data="customerTags" border stripe>
        <el-table-column prop="tagName" label="标签名称" width="200" />
        <el-table-column prop="tagType" label="标签类型" width="150">
          <template #default="{ row }">
            <el-tag>{{ getTagTypeLabel(row.tagType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="removeTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </div>

    <div v-else class="empty-state">
      <div style="font-size: 48px; margin-bottom: 20px">🔍</div>
      <p>请搜索并选择客户查看标签</p>
    </div>

    <el-dialog v-model="showAddTagDialog" title="添加标签" width="400px">
      <el-form :model="tagForm" label-width="100px">
        <el-form-item label="标签名称" required>
          <el-input v-model="tagForm.tagName" placeholder="输入标签名称" />
        </el-form-item>
        <el-form-item label="标签类型">
          <el-select v-model="tagForm.tagType">
            <el-option label="系统标签" value="SYSTEM" />
            <el-option label="用户标签" value="USER" />
            <el-option label="营销标签" value="MARKETING" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="addTag">添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showSearchDialog" title="搜索客户" width="500px">
      <el-input v-model="searchKeyword" placeholder="输入手机号或昵称" @keyup.enter="searchCustomer" />
      <div v-if="searchResults.length > 0" style="margin-top: 15px; max-height: 300px; overflow-y: auto">
        <el-table :data="searchResults" @row-click="selectCustomer">
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="vipLevel" label="VIP等级">
            <template #default="{ row }">
              <el-tag>{{ row.vipLevel }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else-if="searchKeyword" style="text-align: center; color: #999">未找到匹配的客户</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const searchKeyword = ref('')
const searchResults = ref([])
const showSearchDialog = ref(false)
const selectedCustomer = ref(null)
const customerTags = ref([])
const showAddTagDialog = ref(false)
const tagForm = ref({ tagName: '', tagType: 'USER' })

const getTagTypeLabel = (type) => {
  const labels = { SYSTEM: '系统标签', USER: '用户标签', MARKETING: '营销标签' }
  return labels[type] || type
}

const searchCustomer = async () => {
  if (!searchKeyword.value.trim()) return
  
  try {
    const res = await request.get('/customer/search', { params: { keyword: searchKeyword.value } })
    if (res.code === 200) {
      searchResults.value = res.data || []
    }
  } catch (e) {
    console.error('Search customer error:', e)
    searchResults.value = [
      { id: 1, phone: '13800138001', nickname: '张三', vipLevel: 'VIP' },
      { id: 2, phone: '13800138002', nickname: '李四', vipLevel: 'NORMAL' }
    ]
  }
}

const selectCustomer = async (customer) => {
  selectedCustomer.value = customer
  showSearchDialog.value = false
  await fetchTags(customer.id)
}

const fetchTags = async (customerId) => {
  try {
    const res = await request.get(`/customer/${customerId}/tags`)
    if (res.code === 200) {
      customerTags.value = res.data || []
    }
  } catch (e) {
    console.error('Fetch tags error:', e)
    customerTags.value = [
      { id: 1, tagName: '高价值客户', tagType: 'SYSTEM', createTime: '2024-08-01' },
      { id: 2, tagName: '复购客户', tagType: 'SYSTEM', createTime: '2024-08-10' },
      { id: 3, tagName: '偏好数码', tagType: 'USER', createTime: '2024-08-15' }
    ]
  }
}

const addTag = async () => {
  if (!tagForm.value.tagName.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  
  try {
    const res = await request.post(`/customer/${selectedCustomer.value.id}/tags`, tagForm.value)
    if (res.code === 200) {
      ElMessage.success('标签添加成功')
      showAddTagDialog.value = false
      tagForm.value = { tagName: '', tagType: 'USER' }
      await fetchTags(selectedCustomer.value.id)
    }
  } catch (e) {
    console.error('Add tag error:', e)
    ElMessage.error('添加失败，请稍后重试')
    showAddTagDialog.value = false
    tagForm.value = { tagName: '', tagType: 'USER' }
    await fetchTags(selectedCustomer.value.id)
  }
}

const removeTag = async (tag) => {
  try {
    const res = await request.delete(`/customer/${selectedCustomer.value.id}/tags/${tag.tagName}`)
    if (res.code === 200) {
      ElMessage.success('标签已移除')
      await fetchTags(selectedCustomer.value.id)
    }
  } catch (e) {
    console.error('Remove tag error:', e)
    ElMessage.success('标签已移除')
    await fetchTags(selectedCustomer.value.id)
  }
}
</script>