<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/utils/http'
import { regionData } from '@/data/regionData'
import { ElMessage } from 'element-plus'

const router = useRouter()

const form = ref({
  merchantName: '',
  merchantType: '',
  creditCode: '',
  legalPerson: '',
  registeredCapital: '',
  businessScope: '',
  contactName: '',
  contactPhone: '',
  password: '',
  province: '',
  city: '',
  district: '',
  address: '',
  bankName: '',
  bankAccount: '',
  taxNumber: ''
})

const loading = ref(false)

const merchantTypeOptions = [
  { value: 'LOCAL_LIFE', label: '本地生活' },
  { value: 'ECOMMERCE', label: '电子商务' },
  { value: 'DIGITAL_CONTENT', label: '数字内容' },
  { value: 'SERVICE', label: '服务类' }
]

const provinceOptions = regionData.map(p => ({ value: p.label, label: p.label }))

const selectedProvince = computed(() => {
  return regionData.find(p => p.label === form.value.province)
})

const cityOptions = computed(() => {
  if (!selectedProvince.value) return []
  return selectedProvince.value.cities.map(c => ({ value: c.label, label: c.label }))
})

function onProvinceChange() {
  form.value.city = ''
}

async function handleSubmit() {
  if (!form.value.merchantName) { ElMessage.warning('请输入企业名称'); return }
  if (!form.value.merchantType) { ElMessage.warning('请选择商户类型'); return }
  if (!form.value.contactName) { ElMessage.warning('请输入联系人'); return }
  if (!form.value.contactPhone) { ElMessage.warning('请输入联系电话'); return }
  if (!form.value.password) { ElMessage.warning('请输入登录密码'); return }
  if (form.value.password.length < 6) { ElMessage.warning('密码长度不能少于6位'); return }

  loading.value = true
  try {
    await http.post('/merchant-portal/register', form.value)
    ElMessage.success('入驻申请已提交，请等待审核')
    setTimeout(() => router.push('/login'), 1000)
  } catch (error) {
    const msg = error?.response?.data?.message || error?.message || '提交失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-bg">
      <div class="register-card">
        <div class="back-row">
          <span class="back-btn" @click="router.push('/login')">← 返回登录</span>
        </div>
        <h2 class="register-title">商户入驻申请</h2>
        <p class="register-subtitle">填写以下信息，完成商户入驻申请</p>

        <el-form :model="form" label-position="top" class="register-form" @submit.prevent="handleSubmit">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="企业名称 *">
                <el-input v-model="form.merchantName" placeholder="请输入企业名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商户类型 *">
                <el-select v-model="form.merchantType" placeholder="请选择商户类型" style="width:100%">
                  <el-option v-for="opt in merchantTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="统一社会信用代码">
                <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="法人代表">
                <el-input v-model="form.legalPerson" placeholder="请输入法人代表" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="注册资本">
                <el-input v-model="form.registeredCapital" placeholder="请输入注册资本" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系人 *">
                <el-input v-model="form.contactName" placeholder="请输入联系人" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="经营范围">
            <el-input v-model="form.businessScope" type="textarea" :rows="2" placeholder="请输入经营范围" />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="联系电话 *">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="登录密码 *">
                <el-input v-model="form.password" type="password" placeholder="请输入登录密码（至少6位）" show-password />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="所在地区">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-select v-model="form.province" placeholder="省份" style="width:100%" @change="onProvinceChange">
                  <el-option v-for="opt in provinceOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-select v-model="form.city" placeholder="城市" style="width:100%" :disabled="!form.province">
                  <el-option v-for="opt in cityOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-input v-model="form.district" placeholder="区县" />
              </el-col>
            </el-row>
          </el-form-item>

          <el-form-item label="详细地址">
            <el-input v-model="form.address" placeholder="请输入详细地址" />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="开户银行">
                <el-input v-model="form.bankName" placeholder="请输入开户银行" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="银行账号">
                <el-input v-model="form.bankAccount" placeholder="请输入银行账号" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="纳税人识别号">
            <el-input v-model="form.taxNumber" placeholder="请输入纳税人识别号" />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="submit-btn"
            @click="handleSubmit"
          >
            提交入驻申请
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
}

.register-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #6c5ce7, #a29bfe);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40px 20px;
}

.register-card {
  width: 700px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 36px;
  box-shadow: 0 20px 60px rgba(108, 92, 231, 0.3);
}

.back-row {
  margin-bottom: 8px;
}

.back-btn {
  font-size: 14px;
  color: #6c5ce7;
  cursor: pointer;
}

.back-btn:hover {
  text-decoration: underline;
}

.register-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.register-subtitle {
  font-size: 13px;
  color: #999;
  margin-bottom: 24px;
}

.register-form {
  width: 100%;
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  margin-top: 8px;
  background: linear-gradient(135deg, #6c5ce7, #a29bfe);
  border: none;
  border-radius: 8px;
}

.submit-btn:hover {
  opacity: 0.9;
}
</style>
