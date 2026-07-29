<template>
  <div class="page-container">
    <div class="page-header">
      <h2>菜单管理</h2>
      <button @click="openAddModal" class="btn btn-primary">+ 添加菜单</button>
    </div>

    <div class="page-content">
      <div class="menu-tree">
        <div v-for="menu in menuTree" :key="menu.id" class="menu-item">
          <div class="menu-header" :class="{ active: expandedMenus.includes(menu.id) }">
            <span class="menu-icon">{{ menu.icon }}</span>
            <span class="menu-name">{{ menu.menuName }}</span>
            <span class="menu-type">{{ getMenuTypeText(menu.menuType) }}</span>
            <div class="menu-actions">
              <button @click="openAddChildModal(menu)" class="btn btn-xs btn-success">+ 添加子菜单</button>
              <button @click="openEditModal(menu)" class="btn btn-xs btn-info">编辑</button>
              <button @click="deleteMenu(menu.id)" class="btn btn-xs btn-danger">删除</button>
            </div>
            <span v-if="menu.children && menu.children.length > 0" 
                  class="menu-arrow" 
                  @click="toggleExpand(menu.id)">
              {{ expandedMenus.includes(menu.id) ? '▼' : '▶' }}
            </span>
          </div>
          <div v-if="expandedMenus.includes(menu.id) && menu.children.length > 0" class="menu-children">
            <div v-for="child in menu.children" :key="child.id" class="menu-item child">
              <div class="menu-header">
                <span class="menu-icon">{{ child.icon }}</span>
                <span class="menu-name">{{ child.menuName }}</span>
                <span class="menu-type">{{ getMenuTypeText(child.menuType) }}</span>
                <div class="menu-actions">
                  <button @click="openEditModal(child)" class="btn btn-xs btn-info">编辑</button>
                  <button @click="deleteMenu(child.id)" class="btn btn-xs btn-danger">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑菜单' : (isAddChild ? '添加子菜单' : '添加菜单') }}</h3>
          <button @click="closeModal" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveMenu">
            <div class="form-group">
              <label>上级菜单</label>
              <select v-model="form.parentId">
                <option :value="0">无（顶级菜单）</option>
                <option v-for="menu in topMenus" :key="menu.id" :value="menu.id">
                  {{ menu.icon }} {{ menu.menuName }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>菜单名称</label>
              <input v-model="form.menuName" type="text" placeholder="请输入菜单名称" required />
            </div>
            <div class="form-group">
              <label>菜单类型</label>
              <select v-model="form.menuType" @change="handleTypeChange">
                <option value="DIRECTORY">目录</option>
                <option value="MENU">菜单</option>
                <option value="BUTTON">按钮</option>
              </select>
            </div>
            <div class="form-group" v-if="form.menuType !== 'BUTTON'">
              <label>路由路径</label>
              <input v-model="form.path" type="text" placeholder="如：/merchant/list" />
            </div>
            <div class="form-group" v-if="form.menuType === 'MENU'">
              <label>组件路径</label>
              <input v-model="form.component" type="text" placeholder="如：../views/merchant/List.vue" />
            </div>
            <div class="form-group">
              <label>权限标识</label>
              <input v-model="form.permission" type="text" placeholder="如：merchant:list" />
            </div>
            <div class="form-group">
              <label>菜单图标</label>
              <input v-model="form.icon" type="text" placeholder="如：🏠" />
            </div>
            <div class="form-group">
              <label>排序</label>
              <input v-model.number="form.sortOrder" type="number" placeholder="排序数字" />
            </div>
            <div class="form-group">
              <label>是否可见</label>
              <select v-model="form.visible">
                <option :value="1">是</option>
                <option :value="0">否</option>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'

const menuTree = ref([])
const expandedMenus = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const isAddChild = ref(false)
const form = ref({})

const topMenus = computed(() => {
  return menuTree.value.filter(m => m.parentId === 0 || m.parentId === null)
})

const loadMenus = async () => {
  const res = await request.get('/rbac/menus/tree')
  if (res.code === 200) {
    menuTree.value = res.data
    menuTree.value.forEach(menu => {
      if (menu.children && menu.children.length > 0) {
        expandedMenus.value.push(menu.id)
      }
    })
  }
}

const openAddModal = () => {
  isEdit.value = false
  isAddChild.value = false
  form.value = { parentId: 0, menuName: '', menuType: 'DIRECTORY', path: '', component: '', permission: '', icon: '', sortOrder: 0, visible: 1, status: 'ACTIVE' }
  showModal.value = true
}

const openAddChildModal = (parentMenu) => {
  isEdit.value = false
  isAddChild.value = true
  form.value = { parentId: parentMenu.id, menuName: '', menuType: 'MENU', path: '', component: '', permission: '', icon: '', sortOrder: 0, visible: 1, status: 'ACTIVE' }
  showModal.value = true
}

const openEditModal = (menu) => {
  isEdit.value = true
  isAddChild.value = false
  form.value = { ...menu }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  form.value = {}
}

const toggleExpand = (menuId) => {
  const index = expandedMenus.value.indexOf(menuId)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(menuId)
  }
}

const handleTypeChange = () => {
  if (form.value.menuType === 'BUTTON') {
    form.value.path = ''
    form.value.component = ''
  }
}

const saveMenu = async () => {
  if (isEdit.value) {
    await request.put(`/rbac/menus/${form.value.id}`, form.value)
  } else {
    await request.post('/rbac/menus', form.value)
  }
  closeModal()
  loadMenus()
}

const deleteMenu = async (id) => {
  if (confirm('确定删除该菜单？')) {
    await request.delete(`/rbac/menus/${id}`)
    loadMenus()
  }
}

const getMenuTypeText = (type) => {
  const map = { DIRECTORY: '目录', MENU: '菜单', BUTTON: '按钮' }
  return map[type] || type
}

onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; }

.menu-tree { max-width: 800px; }
.menu-item { border: 1px solid #eee; border-radius: 4px; margin-bottom: 8px; overflow: hidden; }
.menu-item.child { margin-left: 24px; border-left: 3px solid #1890ff; }
.menu-header { display: flex; align-items: center; padding: 12px; background: #f8f9fa; cursor: pointer; transition: all 0.3s; }
.menu-header:hover, .menu-header.active { background: #e6f7ff; }
.menu-icon { font-size: 16px; margin-right: 8px; }
.menu-name { flex: 1; font-weight: 500; }
.menu-type { padding: 2px 8px; border-radius: 4px; font-size: 12px; background: #f0f0f0; margin-right: 12px; }
.menu-actions { display: flex; gap: 8px; }
.menu-arrow { margin-left: 8px; font-size: 12px; }
.menu-children { border-top: 1px solid #eee; }

.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
.btn-primary { background: #1890ff; color: #fff; }
.btn-secondary { background: #f0f0f0; color: #666; }
.btn-success { background: #52c41a; color: #fff; }
.btn-info { background: #13c2c2; color: #fff; }
.btn-danger { background: #ff4d4f; color: #fff; }
.btn-xs { padding: 2px 6px; font-size: 12px; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: #fff; border-radius: 8px; width: 500px; max-height: 80vh; overflow-y: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; border-bottom: 1px solid #eee; }
.modal-header h3 { margin: 0; }
.modal-close { font-size: 24px; border: none; background: none; cursor: pointer; }
.modal-body { padding: 16px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 4px; font-weight: 500; }
.form-group input, .form-group select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
</style>