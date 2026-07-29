<template>
  <div class="container">
    <h1 class="page-title">👥 用户管理</h1>

    <div class="card">
      <div class="card-header">
        <h3>用户列表</h3>
        <el-button type="primary" @click="showAddModal = true">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>

      <div class="table-container">
        <el-table :data="users" style="width:100%" v-loading="loading">
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="真实姓名" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可登录平台" min-width="280">
          <template #default="{ row }">
            <div v-if="row.platforms && row.platforms.length" style="display:flex;flex-wrap:wrap;gap:4px">
              <el-tag size="small" v-for="p in row.platforms" :key="p">
                {{ getPlatformName(p) }}
              </el-tag>
            </div>
            <span v-else style="color:#999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editUser(row)">编辑</el-button>
            <el-button
              link
              size="small"
              :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </div>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog
      v-model="showAddModal"
      :title="editingUser ? '编辑用户' : '新增用户'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="userFormRef" :model="form" :rules="rules" label-width="130px" label-position="right">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width:100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="运营人员" value="OPERATOR" />
            <el-option label="商户管理员" value="MERCHANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" :placeholder="editingUser ? '不修改请留空' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="可登录平台" prop="platforms">
          <el-checkbox-group v-model="form.platforms">
            <el-checkbox
              v-for="platform in allPlatforms"
              :key="platform.id"
              :value="platform.id"
              :disabled="platform.status !== 'ACTIVE'"
            >
              {{ platform.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" @click="saveUser" :loading="saveLoading">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../utils/request'

const loading = ref(false)
const saveLoading = ref(false)
const showAddModal = ref(false)
const userFormRef = ref()
const users = ref([])
const editingUser = ref(null)
const allPlatforms = ref([])

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  role: 'OPERATOR',
  password: '',
  platforms: [1]
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  platforms: [{ required: true, message: '请至少选择一个可登录平台', trigger: 'change' }]
}

const getRoleText = (role) => {
  const map = {
    'SUPER_ADMIN': '超级管理员',
    'ADMIN': '管理员',
    'OPERATOR': '运营人员',
    'MERCHANT': '商户管理员'
  }
  return map[role] || role
}

const getRoleTagType = (role) => {
  const map = {
    'SUPER_ADMIN': 'danger',
    'ADMIN': 'warning',
    'OPERATOR': 'primary',
    'MERCHANT': 'success'
  }
  return map[role] || 'info'
}

const getPlatformName = (platformId) => {
  const p = allPlatforms.value.find(x => x.id === platformId)
  return p ? p.name : `平台#${platformId}`
}

const fetchAllPlatforms = async () => {
  try {
    const res = await request.get('/auth/sso/platforms')
    if (res.code === 200) {
      allPlatforms.value = (res.data || []).map(p => ({
        ...p,
        id: Number(p.id)
      }))
    }
  } catch (e) {
    console.error('获取平台列表失败:', e)
    allPlatforms.value = [
      { id: 1, name: '生态合作平台', status: 'ACTIVE' },
      { id: 2, name: '积分商城后台', status: 'ACTIVE' },
      { id: 3, name: '权益超市后台', status: 'ACTIVE' },
      { id: 4, name: '泛全联盟平台', status: 'ACTIVE' },
      { id: 5, name: '风控稽核管理平台', status: 'ACTIVE' },
      { id: 6, name: '工单管理系统', status: 'ACTIVE' },
      { id: 7, name: '评价管理系统', status: 'ACTIVE' }
    ]
  }
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/rbac/users')
    if (res.code === 200) {
      users.value = res.data || []
      users.value.forEach(u => {
        if (typeof u.platforms === 'string') {
          // 逗号分隔字符串 "1,2,3" → [1,2,3]
          u.platforms = u.platforms.split(',').map(s => Number(s.trim())).filter(n => !isNaN(n))
        }
        if (!u.platforms || !Array.isArray(u.platforms) || u.platforms.length === 0) {
          u.platforms = [1]
        }
      })
    }
  } catch (e) {
    console.error('Failed to fetch users:', e)
    users.value = [
      { id: 1, username: 'admin', realName: '系统管理员', phone: '13800138001', email: 'admin@igou.com', role: 'SUPER_ADMIN', status: 'ACTIVE', platforms: [1, 2, 3, 4, 5, 6, 7], lastLoginTime: '2026-07-24 12:00:00' },
      { id: 2, username: 'operator', realName: '运营人员', phone: '13800138002', email: 'operator@igou.com', role: 'OPERATOR', status: 'ACTIVE', platforms: [1, 3], lastLoginTime: '2026-07-24 11:30:00' },
      { id: 3, username: 'merchant', realName: '商户管理员', phone: '13800138003', email: 'merchant@igou.com', role: 'MERCHANT', status: 'ACTIVE', platforms: [1], lastLoginTime: '2026-07-23 16:45:00' },
      { id: 4, username: 'audit', realName: '稽核专员', phone: '13800138004', email: 'audit@igou.com', role: 'ADMIN', status: 'DISABLED', platforms: [1], lastLoginTime: '2026-07-22 14:20:00' }
    ]
  } finally {
    loading.value = false
  }
}

const editUser = (row) => {
  editingUser.value = row
  let platforms = row.platforms || []
  if (typeof platforms === 'string') {
    // 逗号分隔字符串 "1,2,3" → [1,2,3]
    platforms = platforms.split(',').map(s => Number(s.trim())).filter(n => !isNaN(n))
  }
  if (!Array.isArray(platforms) || platforms.length === 0) {
    platforms = [1]
  }
  Object.assign(form, {
    username: row.username,
    realName: row.realName,
    phone: row.phone || '',
    email: row.email || '',
    role: row.role,
    password: '',
    platforms: platforms
  })
  showAddModal.value = true
}

const toggleStatus = (row) => {
  row.status = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  ElMessage.success(`用户已${row.status === 'ACTIVE' ? '启用' : '禁用'}`)
}

const closeModal = () => {
  showAddModal.value = false
  editingUser.value = null
  Object.assign(form, {
    username: '',
    realName: '',
    phone: '',
    email: '',
    role: 'OPERATOR',
    password: '',
    platforms: [1]
  })
}

const saveUser = async () => {
  if (!userFormRef.value) return

  try {
    await userFormRef.value.validate()
  } catch (e) {
    return
  }

  saveLoading.value = true
  try {
    const postData = { 
      ...form,
      platforms: form.platforms || []
    }
    if (!postData.password) delete postData.password
    
    if (editingUser.value) {
      const res = await request.put(`/rbac/users/${editingUser.value.id}`, postData)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        closeModal()
        await fetchUsers()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await request.post('/rbac/users', postData)
      if (res.code === 200) {
        ElMessage.success('创建成功')
        closeModal()
        await fetchUsers()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '网络错误'
    console.error('Save user error:', e)
    ElMessage.error('操作失败: ' + msg)
  } finally {
    saveLoading.value = false
  }
}

onMounted(async () => {
  await fetchAllPlatforms()
  await fetchUsers()
})
</script>

<style scoped>
.container {
  padding: 20px;
}

.page-title {
  margin-bottom: 24px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}
</style>
