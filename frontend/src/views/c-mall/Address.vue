<template>
  <div class="cmall-address">
    <div class="address-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <h1>收货地址</h1>
      <button class="add-btn" @click="showAddModal = true">+</button>
    </div>

    <div class="address-list" v-if="addresses.length > 0">
      <div class="address-item" v-for="addr in addresses" :key="addr.id">
        <div class="address-info">
          <div class="address-header-row">
            <span class="name">{{ addr.name }}</span>
            <span class="phone">{{ addr.phone }}</span>
            <span class="default-badge" v-if="addr.isDefault === 1">默认</span>
          </div>
          <p class="address-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</p>
        </div>
        <div class="address-actions">
          <button @click="editAddress(addr)">编辑</button>
          <button @click="deleteAddress(addr.id)">删除</button>
        </div>
      </div>
    </div>

    <div class="empty-address" v-else>
      <div class="empty-icon">📍</div>
      <p>暂无收货地址</p>
      <button class="add-address" @click="showAddModal = true">添加地址</button>
    </div>

    <div class="modal-overlay" v-if="showAddModal" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h2>{{ editingAddress ? '编辑地址' : '添加地址' }}</h2>
        
        <form @submit.prevent="saveAddress">
          <div class="form-group">
            <input v-model="form.name" type="text" placeholder="收货人姓名" class="form-input" />
          </div>
          
          <div class="form-group">
            <input v-model="form.phone" type="tel" placeholder="手机号码" class="form-input" />
          </div>
          
          <div class="form-group">
            <select v-model="form.provinceCode" class="form-input" @change="onAddressProvinceChange">
              <option value="">请选择省份</option>
              <option v-for="p in regionData" :key="p.value" :value="p.value">{{ p.label }}</option>
            </select>
          </div>
          
          <div class="form-group">
            <select v-model="form.cityCode" class="form-input" :disabled="!form.provinceCode" @change="form.city = addressCities.find(c => c.value === form.cityCode)?.label || ''">
              <option value="">请选择城市</option>
              <option v-for="c in addressCities" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
          </div>
          
          <div class="form-group">
            <input v-model="form.district" type="text" placeholder="区/县" class="form-input" />
          </div>
          
          <div class="form-group">
            <textarea v-model="form.detail" placeholder="详细地址" class="form-textarea"></textarea>
          </div>
          
          <div class="form-group checkbox-group">
            <input type="checkbox" v-model="form.isDefault" id="default" />
            <label for="default">设为默认地址</label>
          </div>
          
          <div class="form-actions">
            <button type="submit" class="save-btn">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { regionData, findCitiesByProvinceCode } from '../../data/regionData'

const addresses = ref([])
const showAddModal = ref(false)
const editingAddress = ref(null)
const addressCities = ref([])

const form = ref({
  name: '',
  phone: '',
  province: '',
  provinceCode: '',
  city: '',
  cityCode: '',
  district: '',
  detail: '',
  isDefault: false
})

onMounted(async () => {
  await loadAddresses()
})

const loadAddresses = async () => {
  const customerId = localStorage.getItem('customer_id')
  if (!customerId) return
  
  try {
    const result = await fetch(`/api/customer/address/list?customerId=${customerId}`)
      .then(res => res.json())
    if (result.code === 200) {
      addresses.value = result.data
    }
  } catch (error) {
    console.error('获取地址失败', error)
  }
}

const openModal = () => {
  showAddModal.value = true
}

const closeModal = () => {
  showAddModal.value = false
  editingAddress.value = null
  addressCities.value = []
  form.value = {
    name: '',
    phone: '',
    province: '',
    provinceCode: '',
    city: '',
    cityCode: '',
    district: '',
    detail: '',
    isDefault: false
  }
}

const onAddressProvinceChange = () => {
  const code = form.value.provinceCode
  const province = regionData.find(p => p.value === code)
  form.value.province = province ? province.label : ''
  if (code) {
    addressCities.value = findCitiesByProvinceCode(code)
    if (!addressCities.value.find(c => c.value === form.value.cityCode)) {
      form.value.city = ''
      form.value.cityCode = ''
    }
  } else {
    addressCities.value = []
    form.value.city = ''
    form.value.cityCode = ''
  }
}

const editAddress = (addr) => {
  editingAddress.value = addr
  form.value = {
    name: addr.name,
    phone: addr.phone,
    province: addr.province,
    provinceCode: addr.provinceCode || '',
    city: addr.city,
    cityCode: addr.cityCode || '',
    district: addr.district,
    detail: addr.detail,
    isDefault: addr.isDefault === 1
  }
  // 加载城市列表
  if (addr.provinceCode) {
    addressCities.value = findCitiesByProvinceCode(addr.provinceCode)
  }
  showAddModal.value = true
}

const deleteAddress = async (id) => {
  if (!confirm('确定删除该地址吗?')) return
  
  try {
    const result = await fetch(`/api/customer/address/${id}`, {
      method: 'DELETE'
    }).then(res => res.json())
    
    if (result.code === 200) {
      await loadAddresses()
    } else {
      alert(result.message || '删除失败')
    }
  } catch (error) {
    alert('网络错误')
  }
}

const saveAddress = async () => {
  if (!form.value.name || !form.value.phone || !form.value.detail) {
    alert('请填写完整信息')
    return
  }
  
  const customerId = localStorage.getItem('customer_id')
  const data = {
    ...form.value,
    customerId: customerId,
    isDefault: form.value.isDefault ? 1 : 0
  }
  
  try {
    const url = editingAddress.value ? `/api/customer/address/${editingAddress.value.id}` : '/api/customer/address'
    const method = editingAddress.value ? 'PUT' : 'POST'
    
    const result = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    }).then(res => res.json())
    
    if (result.code === 200) {
      closeModal()
      await loadAddresses()
    } else {
      alert(result.message || '保存失败')
    }
  } catch (error) {
    alert('网络错误')
  }
}
</script>

<style scoped>
.cmall-address {
  min-height: 100vh;
  background: #f5f5f5;
}

.address-header {
  background: white;
  padding: 15px 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.back-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
  color: #333;
}

.address-header h1 {
  flex: 1;
  margin: 0;
  font-size: 20px;
}

.add-btn {
  width: 36px;
  height: 36px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
}

.address-list {
  padding: 20px;
}

.address-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
}

.address-header-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.name {
  font-size: 18px;
  font-weight: bold;
}

.phone {
  color: #666;
}

.default-badge {
  background: #667eea;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.address-detail {
  margin: 0;
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f5f5f5;
}

.address-actions button {
  padding: 8px 20px;
  background: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
}

.empty-address {
  text-align: center;
  padding: 100px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-address p {
  color: #999;
  margin: 0 0 20px 0;
}

.add-address {
  padding: 12px 40px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  width: 90%;
  max-width: 450px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  min-height: 80px;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.cancel-btn {
  flex: 1;
  padding: 12px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.save-btn {
  flex: 1;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>