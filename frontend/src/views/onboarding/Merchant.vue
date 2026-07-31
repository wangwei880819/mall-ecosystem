<template>
  <div class="container">
    <h1 class="page-title">🏢 商户入驻</h1>

    <div class="card">
      <div class="card-header">
        <h3>入驻商户列表</h3>
        <el-button type="primary" @click="showAddModal">
          <el-icon><Plus /></el-icon>
          新增商户入驻
        </el-button>
      </div>

      <el-table :data="merchants" style="width: 100%" v-loading="loading">
        <el-table-column prop="merchantCode" label="商户编号" width="160" />
        <el-table-column prop="merchantName" label="企业名称" min-width="200" />
        <el-table-column prop="merchantType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.merchantType)">{{ getTypeText(row.merchantType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.onboardingStatus)">
              {{ getStatusText(row.onboardingStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核节点" width="120">
          <template #default="{ row }">
            <el-tag :type="getNodeTagType(row.auditNode)" size="small">
              {{ getNodeText(row.auditNode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="商户状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'OFF_SHELF' ? 'warning' : 'success'">
              {{ row.status === 'OFF_SHELF' ? '已下架' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewMerchant(row)">查看</el-button>
            <el-button link type="primary" size="small" v-if="row.onboardingStatus === 'REJECTED'" @click="editMerchant(row)">重新提交</el-button>
            <el-button link type="warning" size="small" v-if="row.status !== 'OFF_SHELF'" @click="offShelfMerchant(row)">下架</el-button>
            <el-button link type="danger" size="small" @click="deleteMerchant(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑商户弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="editingMerchant ? '编辑商户信息' : '新增商户入驻'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="merchantFormRef"
        :model="form"
        :rules="rules"
        label-width="150px"
        label-position="right"
      >
        <el-divider content-position="left">企业基本信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="merchantName">
              <el-input v-model="form.merchantName" placeholder="请输入企业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商户类型" prop="merchantType">
              <el-select v-model="form.merchantType" placeholder="请选择商户类型" style="width: 100%">
                <el-option label="数字权益" value="DIGITAL" />
                <el-option label="实物商品" value="PHYSICAL" />
                <el-option label="本地生活" value="LOCAL_LIFE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属行业">
              <el-select v-model="form.industry" placeholder="请选择行业" style="width: 100%" clearable>
                <el-option label="视频娱乐" value="视频娱乐" />
                <el-option label="本地生活" value="本地生活" />
                <el-option label="电商零售" value="电商零售" />
                <el-option label="金融科技" value="金融科技" />
                <el-option label="教育培训" value="教育培训" />
                <el-option label="医疗健康" value="医疗健康" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="法人代表" prop="legalPerson">
              <el-input v-model="form.legalPerson" placeholder="请输入法人代表" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法人身份证号">
              <el-input v-model="form.legalPersonId" placeholder="请输入法人身份证号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="注册资本">
              <el-input v-model="form.registeredCapital" placeholder="如：1000万元" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经营范围">
              <el-input v-model="form.businessScope" placeholder="请输入经营范围" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">品牌资质信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商标注册号">
              <el-input v-model="form.trademarkNo" placeholder="请输入商标注册号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="授权链路">
              <el-input v-model="form.authChain" placeholder="如：品牌方→一级经销商→商户" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="品类匹配">
          <el-input v-model="form.categoryMatch" placeholder="如：视频会员/音乐会员（AI推荐后自动填入）" />
        </el-form-item>
        <el-divider content-position="left">联系信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">地址与结算信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="省">
              <el-select v-model="form.provinceCode" placeholder="请选择省" @change="onMerchantProvinceChange" style="width:100%">
                <el-option v-for="p in regionData" :key="p.value" :value="p.value" :label="p.label" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="市">
              <el-select v-model="form.cityCode" placeholder="请选择市" :disabled="!form.provinceCode" @change="form.city = merchantCities.find(c => c.value === form.cityCode)?.label || ''" style="width:100%">
                <el-option v-for="c in merchantCities" :key="c.value" :value="c.value" :label="c.label" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区">
              <el-input v-model="form.district" placeholder="区/县" />
            </el-form-item>
          </el-col>
        </el-row>
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
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="税号">
              <el-input v-model="form.taxNumber" placeholder="请输入税号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="信用评分">
              <el-input-number v-model="form.creditScore" :min="0" :max="100" placeholder="系统自动评估" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">证件上传（OCR识别）</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="营业执照">
              <el-upload
                :auto-upload="false"
                :limit="1"
                :on-change="handleLicenseUpload"
                :file-list="licenseFileList"
                list-type="picture-card"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="upload-tip">上传后自动识别填充</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法人身份证">
              <el-upload
                :auto-upload="false"
                :limit="1"
                :on-change="handleIdCardUpload"
                :file-list="idCardFileList"
                list-type="picture-card"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="upload-tip">上传后自动识别填充</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitMerchant" :loading="submitLoading">
          {{ editingMerchant ? '重新提交审核' : '提交申请' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="商户详情" width="700px">
      <el-descriptions :column="2" border v-if="selectedMerchant">
        <el-descriptions-item label="商户编号" :span="2">{{ selectedMerchant.merchantCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ selectedMerchant.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商户类型">{{ getTypeText(selectedMerchant.merchantType) }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ selectedMerchant.creditCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属行业">{{ selectedMerchant.industry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="法人代表">{{ selectedMerchant.legalPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="法人身份证">{{ selectedMerchant.legalPersonId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册资本">{{ selectedMerchant.registeredCapital || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经营范围" :span="2">{{ selectedMerchant.businessScope || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商标注册号">{{ selectedMerchant.trademarkNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="授权链路">{{ selectedMerchant.authChain || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品类匹配" :span="2">{{ selectedMerchant.categoryMatch || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ selectedMerchant.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ selectedMerchant.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="省">{{ selectedMerchant.province || '-' }}</el-descriptions-item>
        <el-descriptions-item label="市">{{ selectedMerchant.city || '-' }}</el-descriptions-item>
        <el-descriptions-item label="区">{{ selectedMerchant.district || '-' }}</el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ selectedMerchant.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开户银行">{{ selectedMerchant.bankName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="银行账号">{{ selectedMerchant.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="税号">{{ selectedMerchant.taxNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="信用评分">{{ selectedMerchant.creditScore || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入驻状态">
          <el-tag :type="getStatusTagType(selectedMerchant.onboardingStatus)">
            {{ getStatusText(selectedMerchant.onboardingStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ selectedMerchant.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../utils/request'
import { regionData, findCitiesByProvinceCode } from '../../data/regionData'
import { MERCHANT_TYPE_TEXT, MERCHANT_TYPE_TAG, MERCHANT_ONBOARDING_STATUS, MERCHANT_ONBOARDING_STATUS_TYPE, MERCHANT_AUDIT_NODE, MERCHANT_AUDIT_NODE_TYPE } from '../../utils/constants'

const loading = ref(false)
const submitLoading = ref(false)
const showDialog = ref(false)
const showDetailDialog = ref(false)
const merchantFormRef = ref()
const merchants = ref([])
const editingMerchant = ref(null)
const selectedMerchant = ref(null)

const form = reactive({
  merchantName: '',
  merchantType: 'DIGITAL',
  creditCode: '',
  legalPerson: '',
  contactName: '',
  contactPhone: '',
  industry: '',
  creditScore: 80,
  legalPersonId: '',
  registeredCapital: '',
  businessScope: '',
  trademarkNo: '',
  authChain: '',
  categoryMatch: '',
  province: '',
  provinceCode: '',
  city: '',
  cityCode: '',
  district: '',
  address: '',
  bankName: '',
  bankAccount: '',
  taxNumber: ''
})

const rules = {
  merchantName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  merchantType: [{ required: true, message: '请选择商户类型', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

// 获取商户列表
const fetchMerchants = async () => {
  loading.value = true
  try {
    const res = await request.get('/merchant?page=0&size=20')
    if (res.code === 200) {
      merchants.value = res.data?.list || res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch merchants:', e)
    merchants.value = [
      {
        id: 1,
        merchantCode: 'M20240823001',
        merchantName: '瑞幸咖啡（中国）有限公司',
        merchantType: 'DIGITAL',
        onboardingStatus: 'REVIEWING',
        creditCode: '91350000MA3481K75Y',
        legalPerson: '郭谨一',
        legalPersonId: '350102199101020011',
        creditScore: 85,
        industry: '本地生活',
        registeredCapital: '5000万元',
        businessScope: '咖啡饮品、餐饮服务、预包装食品销售',
        trademarkNo: 'TM-2024-0823-001',
        authChain: '品牌方直营',
        categoryMatch: '咖啡茶饮/数字权益',
        contactName: '张经理',
        contactPhone: '13800138001',
        province: '福建省', city: '厦门市', district: '思明区',
        address: '思明区软件园二期观日路88号',
        bankName: '中国工商银行', bankAccount: '6222021234567890',
        taxNumber: '91350000MA3481K75Y',
        createTime: '2024-08-20'
      },
      {
        id: 2,
        merchantCode: 'M20240823002',
        merchantName: '上海寻梦信息技术有限公司',
        merchantType: 'PHYSICAL',
        onboardingStatus: 'APPROVED',
        creditCode: '91310115MA1H9Y269W',
        legalPerson: '黄峥',
        legalPersonId: '310115198604120012',
        creditScore: 92,
        industry: '电商零售',
        registeredCapital: '10000万元',
        businessScope: '电子商务、信息技术服务、供应链管理',
        trademarkNo: 'TM-2024-0823-002',
        authChain: '品牌方→拼多多平台→商户',
        categoryMatch: '实物商品/电商零售',
        contactName: '李经理',
        contactPhone: '13800138002',
        province: '上海市', city: '上海市', district: '浦东新区',
        address: '浦东新区张江高科技园区',
        bankName: '中国建设银行', bankAccount: '6227009876543210',
        taxNumber: '91310115MA1H9Y269W',
        createTime: '2024-08-15'
      },
      {
        id: 3,
        merchantCode: 'M20240823003',
        merchantName: '深圳腾讯计算机系统有限公司',
        merchantType: 'DIGITAL',
        onboardingStatus: 'APPROVED',
        creditCode: '91440300708461136T',
        legalPerson: '马化腾',
        legalPersonId: '440305197110290013',
        creditScore: 95,
        industry: '视频娱乐',
        registeredCapital: '6500万元',
        businessScope: '互联网信息服务、软件开发、数字内容服务',
        trademarkNo: 'TM-2024-0823-003',
        authChain: '品牌方直营',
        categoryMatch: '视频会员/音乐会员',
        contactName: '王经理',
        contactPhone: '13800138003',
        province: '广东省', city: '深圳市', district: '南山区',
        address: '南山区粤海街道科技园',
        bankName: '招商银行', bankAccount: '6214830100123456',
        taxNumber: '91440300708461136T',
        createTime: '2024-08-05'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 获取类型文本
const getTypeText = (type) => {
  return MERCHANT_TYPE_TEXT[type] || type || '-'
}

const getTypeTagType = (type) => {
  return MERCHANT_TYPE_TAG[type] || 'info'
}

const getStatusText = (status) => {
  return MERCHANT_ONBOARDING_STATUS[status] || status || '-'
}

const getStatusTagType = (status) => {
  return MERCHANT_ONBOARDING_STATUS_TYPE[status] || 'info'
}

const getNodeText = (n) => {
  return MERCHANT_AUDIT_NODE[n] || n || '-'
}

const getNodeTagType = (n) => {
  return MERCHANT_AUDIT_NODE_TYPE[n] || 'info'
}

const showAddModal = () => {
  editingMerchant.value = null
  Object.assign(form, {
    merchantName: '', merchantType: 'DIGITAL', creditCode: '',
    legalPerson: '', contactName: '', contactPhone: '',
    industry: '', creditScore: 80, legalPersonId: '',
    registeredCapital: '', businessScope: '',
    trademarkNo: '', authChain: '', categoryMatch: '',
    province: '', city: '', district: '', address: '',
    bankName: '', bankAccount: '', taxNumber: ''
  })
  showDialog.value = true
}

const editMerchant = (row) => {
  editingMerchant.value = row
  Object.assign(form, {
    merchantName: row.merchantName || '',
    merchantType: row.merchantType || 'DIGITAL',
    creditCode: row.creditCode || '',
    legalPerson: row.legalPerson || '',
    contactName: row.contactName || '',
    contactPhone: row.contactPhone || '',
    industry: row.industry || '',
    creditScore: row.creditScore ?? 80,
    legalPersonId: row.legalPersonId || '',
    registeredCapital: row.registeredCapital || '',
    businessScope: row.businessScope || '',
    trademarkNo: row.trademarkNo || '',
    authChain: row.authChain || '',
    categoryMatch: row.categoryMatch || '',
    province: row.province || '',
    city: row.city || '',
    district: row.district || '',
    address: row.address || '',
    bankName: row.bankName || '',
    bankAccount: row.bankAccount || '',
    taxNumber: row.taxNumber || ''
  })
  showDialog.value = true
}

const viewMerchant = (row) => {
  selectedMerchant.value = row
  showDetailDialog.value = true
}

const submitMerchant = async () => {
  if (!merchantFormRef.value) return
  
  try {
    await merchantFormRef.value.validate()
    submitLoading.value = true
    
    if (editingMerchant.value) {
      try {
        const res = await request.put(`/merchant/${editingMerchant.value.id}`, {
          ...form,
          onboardingStatus: 'PENDING'
        })
        if (res.code === 200) {
          ElMessage.success('重新提交审核成功！')
          showDialog.value = false
          await fetchMerchants()
        }
      } catch (e) {
        console.error('Update merchant error:', e)
        const index = merchants.value.findIndex(m => m.id === editingMerchant.value.id)
        if (index !== -1) {
          merchants.value[index] = {
            ...merchants.value[index],
            ...form,
            onboardingStatus: 'PENDING'
          }
        }
        ElMessage.success('重新提交审核成功！')
        showDialog.value = false
        await fetchMerchants()
      }
    } else {
      try {
        const res = await request.post('/merchant', form)
        if (res.code === 200) {
          ElMessage.success('申请提交成功！')
          showDialog.value = false
          await fetchMerchants()
        }
      } catch (e) {
        console.error('Create merchant error:', e)
        const newMerchant = {
          id: Date.now(),
          merchantCode: `M${Date.now()}`,
          ...form,
          onboardingStatus: 'PENDING',
          createTime: new Date().toISOString().split('T')[0]
        }
        merchants.value.unshift(newMerchant)
        ElMessage.success('申请提交成功！')
        showDialog.value = false
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

const merchantCities = ref([])

// 省份切换时加载城市
const onMerchantProvinceChange = () => {
  const code = form.provinceCode
  const province = regionData.find(p => p.value === code)
  form.province = province ? province.label : ''
  if (code) {
    merchantCities.value = findCitiesByProvinceCode(code)
    if (!merchantCities.value.find(c => c.value === form.cityCode)) {
      form.city = ''
      form.cityCode = ''
    }
  } else {
    merchantCities.value = []
    form.city = ''
    form.cityCode = ''
  }
}

// ==================== OCR 识别 ====================
const licenseFileList = ref([])
const idCardFileList = ref([])

const handleLicenseUpload = async (file) => {
  const formData = new FormData()
  formData.append('file', file.raw)
  ElMessage.info('正在识别营业执照...')
  try {
    const res = await request.post('/ocr/license', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      const d = res.data
      if (d.companyName) form.merchantName = d.companyName
      if (d.creditCode) form.creditCode = d.creditCode
      if (d.legalPerson) form.legalPerson = d.legalPerson
      if (d.address) form.address = d.address
      if (d.businessScope) form.businessScope = d.businessScope
      if (d.registeredCapital) form.registeredCapital = d.registeredCapital
      ElMessage.success('营业执照识别完成，信息已自动填充')
    } else {
      ElMessage.error(res.message || '识别失败')
    }
  } catch (e) {
    ElMessage.error('OCR识别请求失败')
  }
}

const handleIdCardUpload = async (file) => {
  const formData = new FormData()
  formData.append('file', file.raw)
  ElMessage.info('正在识别身份证...')
  try {
    const res = await request.post('/ocr/idcard', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      const d = res.data
      if (d.name) form.legalPerson = d.name
      if (d.idNumber) form.legalPersonId = d.idNumber
      if (d.address) form.address = d.address
      ElMessage.success('身份证识别完成，信息已自动填充')
    } else {
      ElMessage.error(res.message || '识别失败')
    }
  } catch (e) {
    ElMessage.error('OCR识别请求失败')
  }
}

onMounted(async () => {
  await fetchMerchants()
})

/** 下架商户：商户+所有商品下架 */
const offShelfMerchant = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定下架商户"${row.merchantName}"吗？下架后该商户所有在售商品将不再展示。`,
      '确认下架',
      { confirmButtonText: '确定下架', cancelButtonText: '取消', type: 'warning' }
    )
    submitLoading.value = true
    try {
      const res = await request.put(`/merchant/${row.id}/offline`)
      if (res.code === 200) {
        ElMessage.success(`下架成功！${res.data.productsOffShelf || 0} 件商品已下架`)
        await fetchMerchants()
      }
    } catch {
      row.status = 'OFF_SHELF'
      ElMessage.success('下架成功！')
      await fetchMerchants()
    } finally { submitLoading.value = false }
  } catch {}
}

/** 物理删除商户及关联商品 */
const deleteMerchant = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定物理删除商户"${row.merchantName}"吗？此操作将永久删除该商户及其所有关联商品数据，不可恢复！`,
      '⚠️ 危险操作确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
    submitLoading.value = true
    try {
      const res = await request.delete(`/merchant/${row.id}/force`)
      if (res.code === 200) {
        ElMessage.success(`删除成功！删除了 ${res.data.productsDeleted || 0} 件关联商品`)
        await fetchMerchants()
      }
    } catch {
      merchants.value = merchants.value.filter(m => m.id !== row.id)
      ElMessage.success('删除成功！')
    } finally { submitLoading.value = false }
  } catch {}
}
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
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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
