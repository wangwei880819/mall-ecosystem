<template>
  <div class="page-container">
    <div class="page-header">
      <h2>角色管理</h2>
      <button @click="openAddModal" class="btn btn-primary">+ 添加角色</button>
    </div>

    <div class="page-content">
      <table class="data-table">
        <thead>
          <tr>
            <th>角色编码</th>
            <th>角色名称</th>
            <th>描述</th>
            <th>数据范围</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="role in roles" :key="role.id">
            <td>{{ role.roleCode }}</td>
            <td>{{ role.roleName }}</td>
            <td>{{ role.roleDesc || '-' }}</td>
            <td>{{ getDataScopeText(role.dataScope) }}</td>
            <td>
              <span :class="['status-tag', role.status === 'ACTIVE' ? 'status-active' : 'status-disabled']">
                {{ role.status === 'ACTIVE' ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ formatTime(role.createTime) }}</td>
            <td>
              <button @click="openEditModal(role)" class="btn btn-sm btn-info">编辑</button>
              <button @click="openPermissionModal(role)" class="btn btn-sm btn-warning">权限配置</button>
              <button @click="deleteRole(role.id)" class="btn btn-sm btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑角色' : '添加角色' }}</h3>
          <button @click="closeModal" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveRole">
            <div class="form-group">
              <label>角色编码</label>
              <input v-model="form.roleCode" type="text" placeholder="如：SUPER_ADMIN" required />
            </div>
            <div class="form-group">
              <label>角色名称</label>
              <input v-model="form.roleName" type="text" placeholder="如：超级管理员" required />
            </div>
            <div class="form-group">
              <label>角色描述</label>
              <textarea v-model="form.roleDesc" placeholder="请输入角色描述"></textarea>
            </div>
            <div class="form-group">
              <label>数据范围</label>
              <select v-model="form.dataScope">
                <option value="ALL">全部数据</option>
                <option value="DEPT">本部门</option>
                <option value="SELF">仅自己</option>
              </select>
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="form.status">
                <option value="ACTIVE">启用</option>
                <option value="DISABLED">禁用</option>
              </select>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div v-if="showPermissionModal" class="modal-overlay" @click.self="closePermissionModal">
      <div class="modal-content" style="width: 700px;">
        <div class="modal-header">
          <h3>权限配置 - {{ permissionRole?.roleName }}</h3>
          <button @click="closePermissionModal" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="permission-tree">
            <div v-for="menu in menuTree" :key="menu.id" class="permission-item">
              <label>
                <input type="checkbox" 
                       :checked="selectedMenuIds.includes(menu.id)" 
                       @change="toggleMenu(menu)" />
                <span>{{ menu.icon }} {{ menu.menuName }}</span>
              </label>
              <div v-if="menu.children && menu.children.length > 0" class="permission-children">
                <div v-for="child in menu.children" :key="child.id" class="permission-item">
                  <label>
                    <input type="checkbox" 
                           :checked="selectedMenuIds.includes(child.id)" 
                           @change="toggleMenu(child)" />
                    <span>{{ child.icon }} {{ child.menuName }}</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" @click="savePermission" class="btn btn-primary">保存权限</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const roles = ref([])
const menuTree = ref([])
const showModal = ref(false)
const showPermissionModal = ref(false)
const isEdit = ref(false)
const form = ref({})
const permissionRole = ref(null)
const selectedMenuIds = ref([])

const loadRoles = async () => {
  const res = await request.get('/rbac/roles')
  if (res.code === 200) roles.value = res.data
}

const loadMenuTree = async () => {
  const res = await request.get('/rbac/menus/tree')
  if (res.code === 200) menuTree.value = res.data
}

const openAddModal = () => {
  isEdit.value = false
  form.value = { roleCode: '', roleName: '', roleDesc: '', dataScope: 'ALL', status: 'ACTIVE' }
  showModal.value = true
}

const openEditModal = (role) => {
  isEdit.value = true
  form.value = { ...role }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  form.value = {}
}

const saveRole = async () => {
  if (isEdit.value) {
    await request.put(`/rbac/roles/${form.value.id}`, form.value)
  } else {
    await request.post('/rbac/roles', form.value)
  }
  closeModal()
  loadRoles()
}

const deleteRole = async (id) => {
  if (confirm('确定删除该角色？')) {
    await request.delete(`/rbac/roles/${id}`)
    loadRoles()
  }
}

const openPermissionModal = async (role) => {
  permissionRole.value = role
  selectedMenuIds.value = []
  await loadMenuTree()
  
  const res = await request.get(`/rbac/roles/${role.id}`)
  if (res.code === 200) {
    res.data.menus.forEach(menu => {
      selectedMenuIds.value.push(menu.id)
    })
  }
  showPermissionModal.value = true
}

const closePermissionModal = () => {
  showPermissionModal.value = false
  permissionRole.value = null
  selectedMenuIds.value = []
}

const toggleMenu = (menu) => {
  const index = selectedMenuIds.value.indexOf(menu.id)
  if (index > -1) {
    selectedMenuIds.value.splice(index, 1)
    if (menu.children) {
      menu.children.forEach(child => {
        const idx = selectedMenuIds.value.indexOf(child.id)
        if (idx > -1) selectedMenuIds.value.splice(idx, 1)
      })
    }
  } else {
    selectedMenuIds.value.push(menu.id)
    if (menu.children) {
      menu.children.forEach(child => {
        if (!selectedMenuIds.value.includes(child.id)) {
          selectedMenuIds.value.push(child.id)
        }
      })
    }
  }
}

const savePermission = async () => {
  await request.put(`/rbac/roles/${permissionRole.value.id}`, {
    menuIds: selectedMenuIds.value
  })
  closePermissionModal()
}

const getDataScopeText = (scope) => {
  const map = { ALL: '全部数据', DEPT: '本部门', SELF: '仅自己' }
  return map[scope] || scope
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : '-'
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.data-table th { background: #f8f9fa; }

.status-tag { padding: 4px 12px; border-radius: 12px; font-size: 12px; }
.status-active { background: #e8f5e9; color: #2e7d32; }
.status-disabled { background: #fce4ec; color: #c62828; }

.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
.btn-primary { background: #1890ff; color: #fff; }
.btn-secondary { background: #f0f0f0; color: #666; }
.btn-info { background: #13c2c2; color: #fff; }
.btn-warning { background: #faad14; color: #fff; }
.btn-danger { background: #ff4d4f; color: #fff; }
.btn-sm { padding: 4px 8px; font-size: 12px; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: #fff; border-radius: 8px; width: 500px; max-height: 80vh; overflow-y: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; border-bottom: 1px solid #eee; }
.modal-header h3 { margin: 0; }
.modal-close { font-size: 24px; border: none; background: none; cursor: pointer; }
.modal-body { padding: 16px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 4px; font-weight: 500; }
.form-group input, .form-group textarea, .form-group select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
.form-group textarea { height: 80px; resize: vertical; }

.permission-tree { max-height: 400px; overflow-y: auto; }
.permission-item { padding: 8px 0; }
.permission-children { padding-left: 24px; }
</style>