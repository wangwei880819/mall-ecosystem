<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import httpInstance from '@/utils/http'
import { toast } from '@/utils/toast'
import { regionData, findCitiesByProvinceCode } from '@/data/regionData'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const editId = route.params.id

const cities = ref([])
const cityLoading = ref(false)

const form = ref({
  name: '',
  phone: '',
  province: '',
  provinceCode: '',
  city: '',
  cityCode: '',
  district: '',
  address: '',
  isDefault: 0,
  customerId: userStore.userInfo?.id || null
})

const saving = ref(false)
const loading = ref(false)

const isEdit = computed(() => !!editId)
const pageTitle = computed(() => isEdit.value ? '编辑地址' : '新增地址')

// 省份切换时加载城市
const onProvinceChange = () => {
  const code = form.value.provinceCode
  // 同步省份名称
  const province = regionData.find(p => p.value === code)
  form.value.province = province ? province.label : ''
  if (code) {
    cityLoading.value = true
    const cityList = findCitiesByProvinceCode(code)
    cities.value = cityList
    // 如果当前选中的城市不在新省份中，清空
    if (!cityList.find(c => c.value === form.value.cityCode)) {
      form.value.city = ''
      form.value.cityCode = ''
    }
    cityLoading.value = false
  } else {
    cities.value = []
    form.value.city = ''
    form.value.cityCode = ''
  }
}

onMounted(async () => {
  if (!userStore.userInfo?.token && !editId) {
    toast('请先登录')
    router.push('/login')
    return
  }
  if (editId) {
    loading.value = true
    try {
      const res = await httpInstance.get(`/address/${editId}`)
      const data = res.result
      if (data) {
        form.value.name = data.name || ''
        form.value.phone = data.phone || ''
        form.value.province = data.province || ''
        form.value.provinceCode = data.provinceCode || ''
        form.value.city = data.city || ''
        form.value.cityCode = data.cityCode || ''
        form.value.district = data.district || ''
        form.value.address = data.address || ''
        form.value.isDefault = data.isDefault ?? 0
        // 加载对应的城市列表
        if (data.provinceCode) {
          cities.value = findCitiesByProvinceCode(data.provinceCode)
        }
      }
    } catch (e) {
      toast('获取地址信息失败')
      router.back()
    } finally {
      loading.value = false
    }
  }
})

function toggleDefault() {
  form.value.isDefault = form.value.isDefault === 1 ? 0 : 1
}

async function handleSave() {
  if (!userStore.userInfo?.token) {
    toast('请先登录')
    router.push('/login')
    return
  }
  if (!form.value.name.trim()) {
    toast('请输入收货人姓名')
    return
  }
  if (!form.value.phone || !/^1\d{10}$/.test(form.value.phone)) {
    toast('请输入正确的手机号')
    return
  }
  if (!form.value.province) {
    toast('请选择省份')
    return
  }
  if (!form.value.city) {
    toast('请选择城市')
    return
  }
  if (!form.value.address.trim()) {
    toast('请输入详细地址')
    return
  }

  saving.value = true
  try {
    const url = editId ? `/address/${editId}` : '/address'
    const method = editId ? 'put' : 'post'
    await httpInstance[method](url, form.value)
    toast('保存成功')
    router.back()
  } catch (e) {
    // error handled by http interceptor
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="address-edit-page">
    <!-- Header -->
    <header class="page-header">
      <button class="back-btn" @click="router.back()">←</button>
      <span class="header-title">{{ pageTitle }}</span>
      <span class="header-right"></span>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <!-- Form -->
    <template v-else>
      <div class="form-card card">
        <!-- 收货人 -->
        <div class="form-group">
          <label class="form-label">收货人</label>
          <input
            v-model="form.name"
            type="text"
            class="form-input"
            placeholder="请输入收货人姓名"
            maxlength="20"
          />
        </div>

        <!-- 手机号 -->
        <div class="form-group">
          <label class="form-label">手机号</label>
          <input
            v-model="form.phone"
            type="tel"
            class="form-input"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </div>

        <!-- 所在地区 -->
        <div class="form-group">
          <label class="form-label">所在地区</label>
          <div class="region-row">
            <select
              v-model="form.provinceCode"
              class="region-select"
              @change="onProvinceChange"
            >
              <option value="">请选择省</option>
              <option v-for="p in regionData" :key="p.value" :value="p.value">{{ p.label }}</option>
            </select>
            <select
              v-model="form.cityCode"
              class="region-select"
              :disabled="!form.provinceCode"
              @change="form.city = cities.find(c => c.value === form.cityCode)?.label || ''"
            >
              <option value="">请选择市</option>
              <option v-for="c in cities" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
            <input
              v-model="form.district"
              type="text"
              class="region-input"
              placeholder="区/县"
            />
          </div>
        </div>

        <!-- 详细地址 -->
        <div class="form-group">
          <label class="form-label">详细地址</label>
          <textarea
            v-model="form.address"
            class="form-textarea"
            placeholder="街道、门牌号等"
            rows="3"
            maxlength="100"
          ></textarea>
        </div>

        <!-- 设为默认 -->
        <div class="form-group default-group">
          <span class="form-label-inline">设为默认收货地址</span>
          <div
            class="toggle-switch"
            :class="{ active: form.isDefault === 1 }"
            @click="toggleDefault"
          >
            <span class="toggle-thumb"></span>
          </div>
        </div>
      </div>

      <!-- Save Button -->
      <div class="save-section">
        <button
          class="save-btn"
          :class="{ disabled: saving }"
          :disabled="saving"
          @click="handleSave"
        >
          {{ saving ? '保存中...' : '保存地址' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.address-edit-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 20px;
}

/* ========== Header ========== */
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  min-height: 48px;
}
.back-btn {
  display: flex;
  align-items: center;
  padding: 6px;
  border: none;
  background: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
}
.header-title {
  flex: 1;
  text-align: center;
}
.header-right {
  width: 36px;
}

/* ========== Loading ========== */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

/* ========== Card ========== */
.card {
  background: #fff;
  border-radius: 12px;
  margin: 12px 16px;
  overflow: hidden;
}
.form-card {
  padding: 20px 16px;
}

/* ========== Form ========== */
.form-group {
  margin-bottom: 20px;
}
.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}
.form-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  font-size: 15px;
  background: #f8f9fb;
  transition: border-color 0.2s;
  outline: none;
  color: #333;
}
.form-input:focus {
  border-color: #667eea;
  background: #fff;
}
.form-input::placeholder {
  color: #ccc;
}

.form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  font-size: 15px;
  background: #f8f9fb;
  transition: border-color 0.2s;
  outline: none;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
  color: #333;
}
.form-textarea:focus {
  border-color: #667eea;
  background: #fff;
}
.form-textarea::placeholder {
  color: #ccc;
}

/* ========== Region Row ========== */
.region-row {
  display: flex;
  gap: 10px;
}
.region-input {
  flex: 1;
  padding: 12px 14px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  font-size: 15px;
  background: #f8f9fb;
  transition: border-color 0.2s;
  outline: none;
  color: #333;
  min-width: 0;
}
.region-select {
  flex: 1;
  padding: 12px 10px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  font-size: 14px;
  background: #f8f9fb;
  transition: border-color 0.2s;
  outline: none;
  color: #333;
  min-width: 0;
  appearance: auto;
}
.region-select:focus {
  border-color: #667eea;
  background: #fff;
}
.region-select:disabled {
  background: #f0f0f0;
  color: #999;
}
.region-input:focus {
  border-color: #667eea;
  background: #fff;
}
.region-input::placeholder {
  color: #ccc;
}

/* ========== Default Toggle ========== */
.default-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 4px;
  margin-bottom: 0;
}
.form-label-inline {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.toggle-switch {
  width: 46px;
  height: 26px;
  background: #ddd;
  border-radius: 13px;
  cursor: pointer;
  position: relative;
  transition: background 0.3s;
  flex-shrink: 0;
}
.toggle-switch.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  background: #fff;
  border-radius: 50%;
  transition: transform 0.3s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}
.toggle-switch.active .toggle-thumb {
  transform: translateX(20px);
}

/* ========== Save Button ========== */
.save-section {
  padding: 8px 16px;
}
.save-btn {
  display: block;
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}
.save-btn:active {
  opacity: 0.85;
}
.save-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
