<template>
  <div class="page-container">
    <div class="page-header">
      <h2>🎠 轮播图管理</h2>
      <el-button type="primary" @click="showAddModal = true">新增轮播图</el-button>
    </div>

    <el-table :data="bannerList" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" width="150" />
      <el-table-column label="图片" width="200">
        <template #default="{ row }">
          <img :src="row.imageUrl" style="width: 80px; height: 40px; object-fit: cover" />
        </template>
      </el-table-column>
      <el-table-column prop="link" label="跳转链接" width="200" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
            {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editBanner(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteBanner(row.id)">删除</el-button>
          <el-button size="small" :type="row.status === 'ENABLED' ? 'warning' : 'success'" 
                     @click="toggleStatus(row)">
            {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showAddModal" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.link" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveBanner">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const bannerList = ref([])
const showAddModal = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  title: '',
  imageUrl: '',
  link: '',
  sortOrder: 0,
  status: 'ENABLED'
})

const fetchBanners = async () => {
  try {
    const res = await fetch('/api/c-mall/banners')
    const data = await res.json()
    if (data.code === 200) {
      bannerList.value = data.data
    }
  } catch (error) {
    console.error('获取轮播图失败', error)
  }
}

const saveBanner = async () => {
  try {
    const url = isEdit.value ? `/api/c-mall/banners/${form.value.id}` : '/api/c-mall/banners'
    const method = isEdit.value ? 'PUT' : 'POST'
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value)
    })
    const data = await res.json()
    if (data.code === 200) {
      showAddModal.value = false
      fetchBanners()
      alert('保存成功')
    }
  } catch (error) {
    console.error('保存失败', error)
  }
}

const editBanner = (row) => {
  isEdit.value = true
  form.value = { ...row }
  showAddModal.value = true
}

const deleteBanner = async (id) => {
  if (!confirm('确定删除？')) return
  try {
    const res = await fetch(`/api/c-mall/banners/${id}`, { method: 'DELETE' })
    const data = await res.json()
    if (data.code === 200) {
      fetchBanners()
      alert('删除成功')
    }
  } catch (error) {
    console.error('删除失败', error)
  }
}

const toggleStatus = async (row) => {
  try {
    const res = await fetch(`/api/c-mall/banners/${row.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...row, status: row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED' })
    })
    const data = await res.json()
    if (data.code === 200) {
      fetchBanners()
    }
  } catch (error) {
    console.error('操作失败', error)
  }
}

onMounted(fetchBanners)
</script>
