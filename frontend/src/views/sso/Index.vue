<template>
  <div class="container">
    <h1 class="page-title">🔗 接入平台管理</h1>

    <div class="card">
      <div class="card-header">
        <h3>接入平台列表</h3>
        <el-button type="primary" @click="showAddModal = true">
          <el-icon><Plus /></el-icon>
          添加平台
        </el-button>
      </div>

      <el-table :data="platforms" style="width:100%" v-loading="loading">
        <el-table-column label="图标" width="80">
          <template #default="{ row }">{{ row.icon }}</template>
        </el-table-column>
        <el-table-column prop="name" label="平台名称" min-width="200" />
        <el-table-column prop="systemCode" label="系统标识" width="180" />
        <el-table-column prop="authType" label="认证方式" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.authType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '已接入' : '待接入' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editPlatform(row)">编辑</el-button>
            <el-button
              link
              size="small"
              :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定要删除该平台吗？此操作不可恢复" @confirm="deletePlatform(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="grid-2" style="margin-top:24px;">
      <div class="card">
        <div class="card-header">
          <h3>API配置管理</h3>
        </div>
        <el-table :data="apiConfigs" style="width:100%">
          <el-table-column prop="apiName" label="接口名称" min-width="180" />
          <el-table-column prop="targetSystem" label="目标系统" width="180" />
          <el-table-column prop="rateLimit" label="限流" width="120">
            <template #default="{ row }">{{ row.rateLimit }}/s</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                {{ row.status === 'ACTIVE' ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="card">
        <div class="card-header">
          <h3>认证统计</h3>
        </div>
        <div class="grid-2">
          <div class="stat-card blue">
            <div class="stat-label">今日认证次数</div>
            <div class="stat-value">12,580</div>
            <div class="stat-trend up">↑ 8.5%</div>
          </div>
          <div class="stat-card green">
            <div class="stat-label">成功率</div>
            <div class="stat-value">99.95%</div>
            <div class="stat-trend up">↑ 0.1%</div>
          </div>
          <div class="stat-card orange">
            <div class="stat-label">活跃平台</div>
            <div class="stat-value">6</div>
          </div>
          <div class="stat-card purple">
            <div class="stat-label">接入用户</div>
            <div class="stat-value">58,234</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑平台弹窗 -->
    <el-dialog
      v-model="showAddModal"
      :title="editingPlatform ? '编辑平台' : '添加接入平台'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form ref="platformFormRef" :model="form" :rules="rules" label-width="110px" label-position="right">
        <el-form-item label="平台名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：积分商城后台" />
        </el-form-item>
        <el-form-item label="系统标识" prop="systemCode">
          <el-input v-model="form.systemCode" placeholder="例如：POINTS_MALL" />
        </el-form-item>
        <el-form-item label="认证方式" prop="authType">
          <el-select v-model="form.authType" style="width:100%">
            <el-option label="OAUTH" value="OAUTH" />
            <el-option label="JWT" value="JWT" />
            <el-option label="API_KEY" value="API_KEY" />
          </el-select>
        </el-form-item>
        <el-form-item label="访问地址" prop="url">
          <el-input v-model="form.url" placeholder="例如：http://ecs-ip/mall" />
        </el-form-item>
        <el-form-item label="平台图标">
          <div style="display:flex;gap:8px;flex-wrap:wrap">
            <div
              v-for="icon in availableIcons"
              :key="icon"
              :style="{
                width:'40px',height:'40px',
                display:'flex',justifyContent:'center',alignItems:'center',
                fontSize:'22px',border:'2px solid',
                borderColor: form.icon === icon ? '#409eff' : '#e0e0e0',
                borderRadius:'6px',cursor:'pointer'
              }"
              @click="form.icon = icon"
            >
              {{ icon }}
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" @click="savePlatform" :loading="saveLoading">
          {{ editingPlatform ? '保存' : '添加' }}
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
const platformFormRef = ref()
const platforms = ref([])
const apiConfigs = ref([])
const editingPlatform = ref(null)

const availableIcons = ['🏢', '🎁', '🎬', '🌐', '🎫', '⭐', '🔗', '📱', '💻', '⚙️', '🛡️']

const form = reactive({
  name: '',
  systemCode: '',
  authType: 'OAUTH',
  status: 'ACTIVE',
  icon: '🔗',
  url: ''
})

const rules = {
  name: [{ required: true, message: '请输入平台名称', trigger: 'blur' }],
  systemCode: [{ required: true, message: '请输入系统标识', trigger: 'blur' }],
  authType: [{ required: true, message: '请选择认证方式', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/auth/sso/platforms')
    if (res.code === 200 && res.data) {
      platforms.value = res.data
    }
    const configRes = await request.get('/auth/sso/configs')
    if (configRes.code === 200) {
      apiConfigs.value = configRes.data
    }
  } catch (e) {
    console.error('获取平台列表失败:', e)
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (platform) => {
  const newStatus = platform.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    const res = await request.put(`/auth/sso/platforms/${platform.id}/status?status=${newStatus}`)
    if (res.code === 200) {
      platform.status = newStatus
      ElMessage.success(`平台已${newStatus === 'ACTIVE' ? '启用' : '禁用'}`)
    }
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

const deletePlatform = async (platform) => {
  try {
    const res = await request.delete(`/auth/sso/platforms/${platform.id}`)
    if (res.code === 200) {
      const idx = platforms.value.findIndex(x => x.id === platform.id)
      if (idx !== -1) {
        platforms.value.splice(idx, 1)
      }
      ElMessage.success('平台已删除')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const editPlatform = (platform) => {
  editingPlatform.value = platform
  Object.assign(form, {
    name: platform.name,
    systemCode: platform.systemCode || platform.system || '',
    authType: platform.authType,
    status: platform.status,
    icon: platform.icon,
    url: platform.url || ''
  })
  showAddModal.value = true
}

const closeModal = () => {
  showAddModal.value = false
  editingPlatform.value = null
  Object.assign(form, { name: '', systemCode: '', authType: 'OAUTH', status: 'ACTIVE', icon: '🔗', url: '' })
}

const savePlatform = async () => {
  if (!platformFormRef.value) return
  try {
    await platformFormRef.value.validate()
  } catch (e) {
    return
  }

  saveLoading.value = true
  try {
    if (editingPlatform.value) {
      const res = await request.put(`/auth/sso/platforms/${editingPlatform.value.id}`, form)
      if (res.code === 200) {
        ElMessage.success('保存成功')
        closeModal()
        await fetchData()
      }
    } else {
      const res = await request.post('/auth/sso/platforms', form)
      if (res.code === 200) {
        ElMessage.success('添加成功')
        closeModal()
        await fetchData()
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saveLoading.value = false
  }
}

onMounted(async () => {
  await fetchData()
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

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.stat-card.blue {
  border-left: 4px solid #409eff;
}

.stat-card.green {
  border-left: 4px solid #67c23a;
}

.stat-card.orange {
  border-left: 4px solid #e6a23c;
}

.stat-card.purple {
  border-left: 4px solid #909399;
}

.stat-label {
  color: #999;
  font-size: 14px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 8px 0;
}

.stat-trend.up {
  color: #67c23a;
  font-size: 13px;
}
</style>
