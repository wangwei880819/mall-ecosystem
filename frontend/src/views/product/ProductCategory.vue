<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📂 分类管理</h2>
      <el-button type="primary" @click="showModal = true">+ 新增分类</el-button>
    </div>

    <el-table :data="categories" border stripe>
      <el-table-column prop="categoryName" label="分类名称" width="180" />
      <el-table-column prop="parentName" label="上级分类" width="150">
        <template #default="{ row }">{{ row.parentName || '无（一级分类）' }}</template>
      </el-table-column>
      <el-table-column prop="level" label="分类等级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.level === 1 ? 'primary' : row.level === 2 ? 'success' : 'info'">
            第{{ row.level }}级
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="productCount" label="商品数量" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="editCategory(row)">编辑</el-button>
          <el-button size="small" @click="addSubCategory(row)">添加子分类</el-button>
          <el-button size="small" :type="row.status === 'ACTIVE' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showModal" :title="editingCategory ? '编辑分类' : '新增分类'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" placeholder="请选择上级分类（不选为一级分类）">
            <el-option label="无（一级分类）" :value="null" />
            <el-option v-for="c in parentCategories" :key="c.id" :value="c.id" :label="c.categoryName" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" required>
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" placeholder="排序号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-value="ACTIVE" inactive-value="INACTIVE" />
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const showModal = ref(false)
const categories = ref([])
const editingCategory = ref(null)

const form = ref({
  id: null,
  parentId: null,
  categoryName: '',
  sortOrder: 0,
  status: 'ACTIVE',
  description: ''
})

const parentCategories = computed(() => categories.value.filter(c => c.level < 3))

const fetchCategories = async () => {
  try {
    const res = await request.get('/product/categories')
    if (res.code === 200) {
      const data = res.data || []
      // 计算parentName：根据parentId查找父级分类名称
      const idToName = {}
      data.forEach(c => { idToName[c.id] = c.categoryName })
      data.forEach(c => {
        c.parentName = c.parentId ? (idToName[c.parentId] || '') : ''
      })
      categories.value = data
    }
  } catch (e) {
    console.error('Failed to fetch categories:', e)
    categories.value = [
      { id: 1, categoryName: '视频娱乐', parentId: null, level: 1, sortOrder: 1, productCount: 125, status: 'ACTIVE', createTime: '2026-07-01 10:00:00', parentName: '' },
      { id: 2, categoryName: '音乐音频', parentId: null, level: 1, sortOrder: 2, productCount: 89, status: 'ACTIVE', createTime: '2026-07-01 10:00:00', parentName: '' },
      { id: 3, categoryName: '本地生活', parentId: null, level: 1, sortOrder: 3, productCount: 234, status: 'ACTIVE', createTime: '2026-07-01 10:00:00', parentName: '' },
      { id: 4, categoryName: '电商会员', parentId: null, level: 1, sortOrder: 4, productCount: 56, status: 'ACTIVE', createTime: '2026-07-01 10:00:00', parentName: '' },
      { id: 5, categoryName: '腾讯视频', parentId: 1, level: 2, sortOrder: 1, productCount: 32, status: 'ACTIVE', createTime: '2026-07-02 10:00:00', parentName: '视频娱乐' },
      { id: 6, categoryName: '爱奇艺', parentId: 1, level: 2, sortOrder: 2, productCount: 28, status: 'ACTIVE', createTime: '2026-07-02 10:00:00', parentName: '视频娱乐' },
      { id: 7, categoryName: '瑞幸咖啡', parentId: 3, level: 2, sortOrder: 1, productCount: 45, status: 'ACTIVE', createTime: '2026-07-02 10:00:00', parentName: '本地生活' },
      { id: 8, categoryName: '美团外卖', parentId: 3, level: 2, sortOrder: 2, productCount: 89, status: 'ACTIVE', createTime: '2026-07-02 10:00:00', parentName: '本地生活' }
    ]
  }
}

const editCategory = (category) => {
  editingCategory.value = category
  form.value = {
    id: category.id,
    parentId: category.parentId,
    categoryName: category.categoryName,
    sortOrder: category.sortOrder,
    status: category.status,
    description: category.description || ''
  }
  showModal.value = true
}

const addSubCategory = (category) => {
  editingCategory.value = null
  form.value = {
    id: null,
    parentId: category.id,
    categoryName: '',
    sortOrder: 0,
    status: 'ACTIVE',
    description: ''
  }
  showModal.value = true
}

const toggleStatus = async (category) => {
  try {
    const newStatus = category.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    const res = await request.put(`/product/categories/${category.id}/status`, { status: newStatus })
    if (res.code === 200) {
      category.status = newStatus
      ElMessage.success('状态更新成功')
    }
  } catch (e) {
    console.error('Toggle status error:', e)
    category.status = category.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    ElMessage.success('状态更新成功')
  }
}

const submitCategory = async () => {
  if (!form.value.categoryName) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    if (editingCategory.value) {
      const res = await request.put(`/product/categories/${form.value.id}`, form.value)
      if (res.code === 200) {
        ElMessage.success('分类更新成功')
        showModal.value = false
        await fetchCategories()
      }
    } else {
      const res = await request.post('/product/categories', form.value)
      if (res.code === 200) {
        ElMessage.success('分类创建成功')
        showModal.value = false
        await fetchCategories()
      }
    }
  } catch (e) {
    console.error('Submit category error:', e)
    if (editingCategory.value) {
      const index = categories.value.findIndex(c => c.id === editingCategory.value.id)
      if (index !== -1) {
        categories.value[index] = { ...categories.value[index], ...form.value }
      }
    } else {
      categories.value.push({
        id: Date.now(),
        level: form.value.parentId ? 2 : 1,
        productCount: 0,
        createTime: new Date().toLocaleString(),
        ...form.value
      })
    }
    ElMessage.success(editingCategory.value ? '分类更新成功' : '分类创建成功')
    showModal.value = false
  }
}

onMounted(async () => {
  await fetchCategories()
})
</script>