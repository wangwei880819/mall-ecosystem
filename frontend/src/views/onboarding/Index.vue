<template>
  <div>
    <h1 class="page-title">📦 生态入驻与商品引入 — 全流程展示</h1>

    <div class="alert alert-info">
      📋 提供商户企业入驻、商品引入及权益引入等在内的全流程展示
    </div>

    <!-- Tab切换 -->
    <div style="display:flex;gap:8px;margin-bottom:16px">
      <button :class="tab === 'merchant' ? 'btn btn-primary' : 'btn btn-outline'" @click="tab = 'merchant'">商户入驻流程</button>
      <button :class="tab === 'product' ? 'btn btn-primary' : 'btn btn-outline'" @click="tab = 'product'">商品引入流程</button>
      <button :class="tab === 'benefit' ? 'btn btn-primary' : 'btn btn-outline'" @click="tab = 'benefit'">权益引入流程</button>
    </div>

    <!-- 商户入驻 -->
    <template v-if="tab === 'merchant'">
      <div class="card">
        <div class="card-header">
          <h3>商户入驻八节点标准化流程</h3>
          <button class="btn btn-primary btn-sm" @click="showMerchantModal = true">+ 新增商户入驻</button>
        </div>
        <div class="steps">
          <template v-for="(step, i) in merchantSteps" :key="step.num">
            <div class="step-item" @click="selectMerchantStep(step)">
              <div class="step-circle" :class="step.status">{{ step.num }}</div>
              <div class="step-label">{{ step.name }}</div>
            </div>
            <div v-if="i < merchantSteps.length - 1" class="step-line" :class="step.status === 'done' ? 'done' : ''"></div>
          </template>
        </div>
      </div>

      <h2 class="section-title">入驻商户列表</h2>
      <div class="card">
        <table class="data-table">
          <thead>
            <tr>
              <th>商户编号</th><th>企业名称</th><th>类型</th><th>当前环节</th><th>进度</th><th>状态</th><th>申请日期</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in merchants" :key="m.id">
              <td>{{ m.merchantCode || m.id }}</td>
              <td>{{ m.merchantName || m.name }}</td>
              <td><span class="tag tag-blue">{{ m.merchantType || m.type }}</span></td>
              <td>{{ merchantSteps[(m.onboardingStep || m.step) - 1]?.name || '-' }}</td>
              <td>
                <div class="progress-bar" style="width:100px">
                  <div class="progress-fill" :class="m.progress === 100 ? 'green' : 'blue'" :style="{ width: m.progress + '%' }"></div>
                </div>
                <span style="font-size:12px;color:#999">{{ m.progress }}%</span>
              </td>
              <td>
                <span :class="getStatusClass(m.onboardingStatus || m.status)">{{ getStatusText(m.onboardingStatus || m.status) }}</span>
              </td>
              <td>{{ m.createTime || m.applyDate }}</td>
              <td>
                <button class="btn btn-sm btn-outline" @click="viewMerchant(m)">查看</button>
                <button v-if="m.onboardingStatus !== 'APPROVED' && m.status !== '已入驻'" class="btn btn-sm btn-primary" @click="approveMerchant(m)">推进</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <h2 class="section-title">AI赋能入驻</h2>
      <div class="grid-3">
        <div class="card">
          <h3 style="font-size:14px;color:#1a237e">🔍 OCR资质智能识别</h3>
          <p style="color:#666;font-size:13px;margin-top:8px">自动识别营业执照、法人身份证、商标注册证，识别准确率≥98%，单户初审耗时从2小时→10分钟</p>
        </div>
        <div class="card">
          <h3 style="font-size:14px;color:#1a237e">🤖 AI风险预判</h3>
          <p style="color:#666;font-size:13px;margin-top:8px">三级风险预判（高/中/低），高风险商户自动进入重点审核流程，低风险商户快速通道</p>
        </div>
        <div class="card">
          <h3 style="font-size:14px;color:#1a237e">📋 合同AI质检</h3>
          <p style="color:#666;font-size:13px;margin-top:8px">大模型+NLP自动审查合同条款合规性，自动评级风险等级，生成质检报告和修改建议</p>
        </div>
      </div>
    </template>

    <!-- 商品引入 -->
    <template v-if="tab === 'product'">
      <div class="card">
        <div class="card-header">
          <h3>数字权益商品引入九节点流程</h3>
          <button class="btn btn-primary btn-sm" @click="showProductModal = true">+ 引入商品</button>
        </div>
        <div class="steps">
          <template v-for="(step, i) in productSteps" :key="step.num">
            <div class="step-item" @click="selectProductStep(step)">
              <div class="step-circle" :class="step.status">{{ step.num }}</div>
              <div class="step-label">{{ step.name }}</div>
            </div>
            <div v-if="i < productSteps.length - 1" class="step-line" :class="step.status === 'done' ? 'done' : ''"></div>
          </template>
        </div>
      </div>

      <h2 class="section-title">商品列表</h2>
      <div class="card">
        <table class="data-table">
          <thead>
            <tr>
              <th>商品编号</th><th>商品名称</th><th>品类</th><th>品牌</th><th>售价</th><th>市场价</th><th>销量</th><th>评分</th><th>AI状态</th><th>状态</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.id">
              <td>{{ p.productCode || p.id }}</td>
              <td>{{ p.productName || p.name }}</td>
              <td><span class="tag tag-purple">{{ p.category }}</span></td>
              <td>{{ p.brand }}</td>
              <td>¥{{ (p.price || 0).toFixed(2) }}</td>
              <td style="text-decoration:line-through;color:#999">¥{{ (p.marketPrice || 0).toFixed(2) }}</td>
              <td>{{ (p.salesCount || p.sales || 0).toLocaleString() }}</td>
              <td>{{ p.avgScore > 0 || p.score > 0 ? '⭐' + (p.avgScore || p.score) : '-' }}</td>
              <td><span class="tag tag-blue">{{ p.aiTag || '-' }}</span></td>
              <td><span :class="getProductStatusClass(p.status)">{{ getProductStatusText(p.status) }}</span></td>
              <td>
                <button class="btn btn-sm btn-outline" @click="viewProduct(p)">查看</button>
                <button v-if="p.status !== 'ON_SHELF' && p.status !== '在售'" class="btn btn-sm btn-primary" @click="approveProduct(p)">上架</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <h2 class="section-title">两级选品委员会</h2>
      <div class="grid-2">
        <div class="card">
          <div class="card-header"><h3>省级选品初审</h3><span class="tag tag-blue">品控+合规+市场</span></div>
          <p style="color:#666;font-size:13px;line-height:1.8">
            审核维度：品牌资质（营业执照/商标注册/行业认证）、商品合规性（资质文件/质检报告/授权链路）、市场契合度（用户需求匹配/品类缺口分析）。初审评分按配置权重计算综合得分。
          </p>
        </div>
        <div class="card">
          <div class="card-header"><h3>总部选品终审</h3><span class="tag tag-purple">战略+资源+风险</span></div>
          <p style="color:#666;font-size:13px;line-height:1.8">
            审核维度：整体战略契合度（品类规划方向）、资源分配合理性（投入产出评估）、风险综合评估（合规/运营/品牌风险综合评级）。合规风险维度设置一票否决权。
          </p>
        </div>
      </div>
    </template>

    <!-- 权益引入 -->
    <template v-if="tab === 'benefit'">
      <div class="card">
        <div class="card-header">
          <h3>数字权益引入六节点流程</h3>
          <button class="btn btn-primary btn-sm" @click="showBenefitModal = true">+ 权益引入</button>
        </div>
        <div class="steps">
          <template v-for="(step, i) in benefitSteps" :key="step.num">
            <div class="step-item">
              <div class="step-circle" :class="step.status">{{ step.num }}</div>
              <div class="step-label">{{ step.name }}</div>
            </div>
            <div v-if="i < benefitSteps.length - 1" class="step-line" :class="step.status === 'done' ? 'done' : ''"></div>
          </template>
        </div>
      </div>

      <h2 class="section-title">权益商品列表</h2>
      <div class="card">
        <table class="data-table">
          <thead>
            <tr>
              <th>权益编号</th><th>权益名称</th><th>类型</th><th>售价</th><th>面值</th><th>结算价</th><th>状态</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in benefits" :key="b.id">
              <td>{{ b.benefitCode }}</td>
              <td>{{ b.benefitName }}</td>
              <td><span class="tag tag-green">{{ benefitTypeMap[b.benefitType] || b.benefitType }}</span></td>
              <td>¥{{ (b.price || 0).toFixed(2) }}</td>
              <td>¥{{ (b.faceValue || 0).toFixed(2) }}</td>
              <td>¥{{ (b.settlePrice || 0).toFixed(2) }}</td>
              <td><span :class="getBenefitStatusClass(b.status)">{{ getBenefitStatusText(b.status) }}</span></td>
              <td>
                <button class="btn btn-sm btn-outline" @click="viewBenefit(b)">查看</button>
                <button v-if="b.status === 'PENDING'" class="btn btn-sm btn-primary" @click="auditBenefit(b, 'APPROVED')">审核通过</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- 权益引入弹窗 -->
    <div v-if="showBenefitModal" class="modal-overlay" @click.self="showBenefitModal = false">
      <div class="modal" style="max-width: 650px;">
        <div class="modal-header">
          <h3>权益引入</h3>
          <button class="modal-close" @click="showBenefitModal = false">×</button>
        </div>
        <div style="max-height:60vh;overflow-y:auto;padding:8px 0">
          <div class="form-group">
            <label>权益名称 *</label>
            <input type="text" v-model="newBenefit.benefitName" placeholder="如：腾讯视频VIP月卡" />
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
            <div class="form-group">
              <label>权益类型 *</label>
              <select v-model="newBenefit.benefitType">
                <option value="MEMBERSHIP">会员权益</option>
                <option value="COUPON">优惠券/代金券</option>
                <option value="GAME_POINTS">游戏点卡</option>
                <option value="DIGITAL_CONTENT">数字内容</option>
                <option value="SERVICE">在线服务</option>
                <option value="INSURANCE">保险/延保</option>
              </select>
            </div>
            <div class="form-group">
              <label>兑换方式</label>
              <select v-model="newBenefit.exchangeMethod">
                <option value="AUTO_BIND">自动绑定账户</option>
                <option value="CODE">兑换码</option>
                <option value="QR_CODE">二维码核销</option>
                <option value="MANUAL">人工发放</option>
              </select>
            </div>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:16px">
            <div class="form-group">
              <label>面值/原价</label>
              <input type="number" v-model.number="newBenefit.faceValue" placeholder="面值" />
            </div>
            <div class="form-group">
              <label>售价 *</label>
              <input type="number" v-model.number="newBenefit.price" placeholder="销售价格" />
            </div>
            <div class="form-group">
              <label>结算价</label>
              <input type="number" v-model.number="newBenefit.settlePrice" placeholder="结算价格" />
            </div>
          </div>
          <div class="form-group">
            <label>有效期类型</label>
            <select v-model="newBenefit.validityType">
              <option value="FIXED_DATE">固定日期</option>
              <option value="DAYS_AFTER_RECEIVE">领取后N天有效</option>
              <option value="DURATION">长期有效</option>
            </select>
          </div>
          <div v-if="newBenefit.validityType === 'DAYS_AFTER_RECEIVE'" class="form-group">
            <label>有效天数</label>
            <input type="number" v-model.number="newBenefit.validityDays" placeholder="30" />
          </div>
          <div v-if="newBenefit.validityType === 'FIXED_DATE'" style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
            <div class="form-group">
              <label>有效期开始</label>
              <input type="datetime-local" v-model="newBenefit.validityStart" />
            </div>
            <div class="form-group">
              <label>有效期结束</label>
              <input type="datetime-local" v-model="newBenefit.validityEnd" />
            </div>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:16px">
            <div class="form-group">
              <label>总库存</label>
              <input type="number" v-model.number="newBenefit.stockTotal" placeholder="0表示无限" />
            </div>
            <div class="form-group">
              <label>每日限兑</label>
              <input type="number" v-model.number="newBenefit.stockDailyLimit" placeholder="不限" />
            </div>
            <div class="form-group">
              <label>每人限兑</label>
              <input type="number" v-model.number="newBenefit.stockPerUser" placeholder="不限" />
            </div>
          </div>
          <div class="form-group">
            <label>使用规则</label>
            <textarea v-model="newBenefit.usageRules" rows="2" placeholder="如：不可与其他优惠叠加、仅限指定平台使用"></textarea>
          </div>
          <div class="form-group">
            <label>适用范围</label>
            <input type="text" v-model="newBenefit.applicableScope" placeholder="如：全平台通用" />
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
            <div class="form-group">
              <label>供应商</label>
              <input type="text" v-model="newBenefit.supplierName" placeholder="供应商名称" />
            </div>
            <div class="form-group">
              <label>退款政策</label>
              <select v-model="newBenefit.refundPolicy">
                <option value="NO_REFUND">不可退款</option>
                <option value="CONDITIONAL">有条件退款</option>
                <option value="FULL_REFUND">支持退款</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitBenefit">提交权益引入</button>
        </div>
      </div>
    </div>

    <!-- 新增商户弹窗 -->
    <div v-if="showMerchantModal" class="modal-overlay" @click.self="showMerchantModal = false">
      <div class="modal" style="max-width: 600px;">
        <div class="modal-header">
          <h3>新增商户入驻</h3>
          <button class="modal-close" @click="showMerchantModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="grid-2">
            <div class="form-group">
              <label>企业名称</label>
              <input type="text" v-model="newMerchant.merchantName" placeholder="请输入企业名称" />
            </div>
            <div class="form-group">
              <label>商户类型</label>
              <select v-model="newMerchant.merchantType">
                <option>DIGITAL</option>
                <option>PHYSICAL</option>
                <option>LOCAL_LIFE</option>
              </select>
            </div>
            <div class="form-group">
              <label>统一社会信用代码</label>
              <input type="text" v-model="newMerchant.creditCode" placeholder="请输入信用代码" />
            </div>
            <div class="form-group">
              <label>法人代表</label>
              <input type="text" v-model="newMerchant.legalPerson" placeholder="请输入法人代表" />
            </div>
            <div class="form-group">
              <label>联系人</label>
              <input type="text" v-model="newMerchant.contactName" placeholder="请输入联系人" />
            </div>
            <div class="form-group">
              <label>联系电话</label>
              <input type="text" v-model="newMerchant.contactPhone" placeholder="请输入联系电话" />
            </div>
          </div>
          <div class="form-group" style="background:#f0f9ff;padding:12px;border-radius:6px;border:1px solid #bae6fd;margin-top:12px">
            <label style="color:#0369a1">🤖 AI智能识别 - 上传营业执照自动填充</label>
            <div style="display:flex;gap:8px;margin-top:6px">
              <input type="file" ref="ocrFileInput" accept="image/*" style="flex:1" />
              <button type="button" class="btn btn-primary btn-sm" @click="doOcr" style="white-space:nowrap">开始识别</button>
            </div>
            <div v-if="ocrResult" style="margin-top:8px;font-size:12px;color:#0369a1">
              识别置信度：{{ ocrResult.score }}%
              <button type="button" class="btn btn-sm btn-success" style="margin-left:8px" @click="applyOcrResult">自动填充</button>
            </div>
            <div v-if="ocrError" style="color:#f56c6c;font-size:12px;margin-top:4px">{{ ocrError }}</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitMerchant">提交申请</button>
        </div>
      </div>
    </div>

    <!-- 新增商品弹窗 -->
    <div v-if="showProductModal" class="modal-overlay" @click.self="showProductModal = false">
      <div class="modal" style="max-width: 600px;">
        <div class="modal-header">
          <h3>引入新商品</h3>
          <button class="modal-close" @click="showProductModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>所属商户</label>
            <select v-model="newProduct.merchantId">
              <option v-for="m in merchants.filter(m => m.onboardingStatus === 'APPROVED' || m.status === '已入驻')" :key="m.id" :value="m.id">
                {{ m.merchantName || m.name }}
              </option>
            </select>
          </div>
          <div class="grid-2">
            <div class="form-group">
              <label>商品名称</label>
              <input type="text" v-model="newProduct.productName" placeholder="请输入商品名称" />
            </div>
            <div class="form-group">
              <label>品牌</label>
              <input type="text" v-model="newProduct.brand" placeholder="请输入品牌" />
            </div>
            <div class="form-group">
              <label>品类</label>
              <select v-model="newProduct.category">
                <option>视频娱乐</option>
                <option>音乐音频</option>
                <option>本地生活</option>
                <option>电商会员</option>
              </select>
            </div>
            <div class="form-group">
              <label>售价</label>
              <input type="number" v-model.number="newProduct.price" placeholder="请输入售价" />
            </div>
            <div class="form-group">
              <label>市场价</label>
              <input type="number" v-model.number="newProduct.marketPrice" placeholder="请输入市场价" />
            </div>
            <div class="form-group">
              <label>库存</label>
              <input type="number" v-model.number="newProduct.stock" placeholder="请输入库存" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="submitProduct">提交引入</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const tab = ref('merchant')
const showMerchantModal = ref(false)
const showProductModal = ref(false)
const showBenefitModal = ref(false)
const ocrFileInput = ref(null)
const ocrResult = ref(null)
const ocrError = ref('')

const merchantSteps = [
  { num: 1, name: '商户申请', status: 'done' },
  { num: 2, name: '资质初审', status: 'done' },
  { num: 3, name: '业务复审', status: 'active' },
  { num: 4, name: '合同签署', status: 'pending' },
  { num: 5, name: '支付进件', status: 'pending' },
  { num: 6, name: '选品审批', status: 'pending' },
  { num: 7, name: '商品录入', status: 'pending' },
  { num: 8, name: '上架发布', status: 'pending' },
]

const productSteps = [
  { num: 1, name: '选品策划', status: 'done' },
  { num: 2, name: '品牌对接', status: 'done' },
  { num: 3, name: '信息录入', status: 'done' },
  { num: 4, name: '合同签署', status: 'done' },
  { num: 5, name: '价格摸排', status: 'active' },
  { num: 6, name: '价格判定', status: 'pending' },
  { num: 7, name: '卖点提炼', status: 'pending' },
  { num: 8, name: '上架审核', status: 'pending' },
  { num: 9, name: '发布上线', status: 'pending' },
]

const benefitSteps = [
  { num: 1, name: '权益对接', status: 'done' },
  { num: 2, name: '接口调试', status: 'done' },
  { num: 3, name: '库存配置', status: 'active' },
  { num: 4, name: '价格备案', status: 'pending' },
  { num: 5, name: '测试验证', status: 'pending' },
  { num: 6, name: '正式上线', status: 'pending' },
]

const merchants = ref([])
const products = ref([])
const benefits = ref([])

const newMerchant = ref({
  merchantName: '', merchantType: 'DIGITAL', creditCode: '', legalPerson: '', contactName: '', contactPhone: ''
})

const newProduct = ref({
  merchantId: 3, productName: '', brand: '', category: '视频娱乐', price: 0, marketPrice: 0, stock: 0
})

const newBenefit = ref({
  benefitName: '', benefitType: 'MEMBERSHIP', faceValue: 0, price: 0, settlePrice: 0,
  validityType: 'DAYS_AFTER_RECEIVE', validityStart: '', validityEnd: '', validityDays: 30,
  exchangeMethod: 'AUTO_BIND', stockTotal: 0, stockDailyLimit: 0, stockPerUser: 0,
  usageRules: '', applicableScope: '', supplierName: '', refundPolicy: 'NO_REFUND'
})

const benefitTypeMap = {
  'MEMBERSHIP': '会员权益', 'COUPON': '优惠券', 'GAME_POINTS': '游戏点卡',
  'DIGITAL_CONTENT': '数字内容', 'SERVICE': '在线服务', 'INSURANCE': '保险/延保'
}

onMounted(async () => {
  try {
    const res = await fetch('/api/admin/merchants?page=0&size=20')
    const data = await res.json()
    if (data.code === 200) {
      merchants.value = (data.data || []).map(m => ({
        ...m,
        progress: ((m.onboardingStep || 1) / 8) * 100
      }))
    }
    const productRes = await fetch('/api/c-mall/products')
    const productData = await productRes.json()
    if (productData.code === 200) {
      products.value = productData.data || []
    }
    // 加载权益列表
    const benefitRes = await fetch('/api/benefit')
    const benefitData = await benefitRes.json()
    if (benefitData.code === 200) {
      benefits.value = benefitData.data || []
    }
  } catch (e) {
    console.error('获取入驻数据失败', e)
  }
})

const getStatusClass = (status) => {
  if (status === 'APPROVED' || status === '已入驻') return 'tag tag-green'
  if (status === 'REVIEWING' || status === '审核中') return 'tag tag-orange'
  if (status === 'PENDING' || status === '待审核') return 'tag tag-blue'
  return 'tag tag-red'
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待审核', 'REVIEWING': '审核中', 'APPROVED': '已入驻', 'REJECTED': '驳回' }
  return map[status] || status
}

const getProductStatusClass = (status) => {
  if (status === 'ON_SHELF' || status === '在售') return 'tag tag-green'
  if (status === 'PENDING' || status === '待审核') return 'tag tag-orange'
  return 'tag tag-gray'
}

const getProductStatusText = (status) => {
  const map = { 'PENDING': '待审核', 'ON_SHELF': '在售', 'OFF_SHELF': '已下架', 'REJECTED': '已驳回' }
  return map[status] || status
}

const selectMerchantStep = (step) => {
  merchantSteps.forEach(s => s.status = s.num < step.num ? 'done' : s.num === step.num ? 'active' : 'pending')
}

const selectProductStep = (step) => {
  productSteps.forEach(s => s.status = s.num < step.num ? 'done' : s.num === step.num ? 'active' : 'pending')
}

const viewMerchant = (m) => {
  alert(`查看商户：${m.merchantName || m.name}`)
}

const approveMerchant = (m) => {
  if (m.onboardingStep < 8) {
    m.onboardingStep++
    m.progress = (m.onboardingStep / 8) * 100
    if (m.onboardingStep === 8) {
      m.onboardingStatus = 'APPROVED'
    }
  }
}

const viewProduct = (p) => {
  alert(`查看商品：${p.productName || p.name}`)
}

const approveProduct = (p) => {
  p.status = 'ON_SHELF'
}

const submitMerchant = async () => {
  try {
    const res = await fetch('/api/admin/merchants', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newMerchant.value)
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('申请提交成功！')
      showMerchantModal.value = false
    }
  } catch (e) {
    merchants.value.push({
      id: Date.now(),
      merchantCode: 'M' + Date.now(),
      ...newMerchant.value,
      onboardingStep: 1,
      onboardingStatus: 'PENDING',
      createTime: new Date().toISOString().split('T')[0],
      progress: 12
    })
    alert('申请提交成功！')
    showMerchantModal.value = false
  }
}

const submitProduct = async () => {
  try {
    const res = await fetch('/api/admin/products', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newProduct.value)
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('引入提交成功！')
      showProductModal.value = false
    }
  } catch (e) {
    products.value.push({
      id: Date.now(),
      productCode: 'P' + Date.now(),
      ...newProduct.value,
      status: 'PENDING',
      salesCount: 0,
      avgScore: 0,
      aiTag: '待AI处理'
    })
    alert('引入提交成功！')
    showProductModal.value = false
  }
}

const getBenefitStatusClass = (status) => {
  if (status === 'ON_SHELF') return 'tag tag-green'
  if (status === 'PENDING') return 'tag tag-orange'
  return 'tag tag-gray'
}

const getBenefitStatusText = (status) => {
  const map = { 'PENDING': '待审核', 'ON_SHELF': '已上线', 'OFF_SHELF': '已下架', 'REJECTED': '已驳回' }
  return map[status] || status
}

const viewBenefit = (b) => {
  alert(`权益详情：${b.benefitName}\n类型：${benefitTypeMap[b.benefitType]}\n售价：¥${b.price}\n库存：${b.stockTotal}`)
}

const auditBenefit = async (b, action) => {
  try {
    const res = await fetch(`/api/benefit/${b.id}/audit?action=${action}`, {
      method: 'PUT'
    })
    const data = await res.json()
    if (data.code === 200) {
      b.status = action === 'APPROVED' ? 'ON_SHELF' : 'REJECTED'
      alert('审核完成')
    }
  } catch (e) {
    alert('审核失败')
  }
}

const submitBenefit = async () => {
  if (!newBenefit.value.benefitName) { alert('请输入权益名称'); return }
  try {
    const res = await fetch('/api/benefit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...newBenefit.value, merchantId: 2 })
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('权益引入提交成功！')
      showBenefitModal.value = false
      // 刷新列表
      const benefitRes = await fetch('/api/benefit')
      const benefitData = await benefitRes.json()
      if (benefitData.code === 200) benefits.value = benefitData.data || []
    }
  } catch (e) {
    benefits.value.push({
      id: Date.now(),
      benefitCode: 'BFT' + Date.now(),
      ...newBenefit.value,
      status: 'PENDING'
    })
    alert('权益引入提交成功！')
    showBenefitModal.value = false
  }
}

// OCR 识别
const doOcr = async () => {
  ocrError.value = ''
  ocrResult.value = null
  try {
    const res = await fetch('/api/ai/ocr/quick', { method: 'POST' })
    const data = await res.json()
    if (data.code === 200) {
      ocrResult.value = data.data
    }
  } catch (e) {
    ocrError.value = '识别失败，请重试'
  }
}

const applyOcrResult = () => {
  if (!ocrResult.value) return
  newMerchant.value.merchantName = ocrResult.value.companyName || newMerchant.value.merchantName
  newMerchant.value.creditCode = ocrResult.value.creditCode || newMerchant.value.creditCode
  newMerchant.value.legalPerson = ocrResult.value.legalPerson || newMerchant.value.legalPerson
  ocrResult.value = null
  alert('已自动填充企业信息')
}
</script>

<style scoped>
.btn-sm {
  padding: 4px 12px;
  font-size: 12px;
}
</style>
