<template>
  <div>
    <h1 class="page-title">🤖 AI+应用功能 — 生态合作智能赋能</h1>
    

    <!-- AI能力矩阵 -->
    <h2 class="section-title">六大AI能力矩阵</h2>
    <div class="grid-3">
      <div v-for="ai in aiFeatures" :key="ai.id" class="card" style="cursor:pointer;transition:all 0.3s" @click="openDemo(ai.demoType)"
           @mouseenter="$event.target.style.boxShadow='0 4px 16px rgba(26,35,126,0.15)'"
           @mouseleave="$event.target.style.boxShadow='0 1px 3px rgba(0,0,0,0.08)'">
        <div style="font-size:36px;margin-bottom:8px">{{ ai.icon }}</div>
        <h3 style="font-size:15px;color:#1a237e">{{ ai.name }}</h3>
        <p style="color:#888;font-size:12px;margin-top:6px;line-height:1.6">{{ ai.desc }}</p>
        <div style="margin-top:12px">
          <button class="btn btn-primary btn-sm">点击演示 →</button>
        </div>
      </div>
    </div>

    <!-- OCR智能识别弹窗 -->
    <div v-if="modal === 'ocr'" class="modal-overlay" @click.self="modal = ''">
      <div class="modal">
        <div class="modal-header"><h3>🔍 商户入驻智能识别 — OCR资质识别</h3><button class="modal-close" @click="modal = ''">×</button></div>
        <div class="modal-body">
          <template v-if="processing">
            <div class="ai-processing">
              <div class="ai-spinner"></div>
              <div class="ai-status">正在识别营业执照和法人身份证...</div>
              <div style="margin-top:12px;font-size:12px;color:#aaa">OCR引擎 + NLP信息校验 + AI风险预判</div>
            </div>
          </template>
          <template v-else>
            <div class="alert alert-success">✅ 识别完成 · 处理耗时 2.3秒 · 识别准确率 98.8%</div>
            <h4 style="margin:16px 0 8px;color:#333">营业执照识别结果</h4>
            <table class="data-table">
              <thead><tr><th>字段</th><th>识别值</th><th>置信度</th></tr></thead>
              <tbody>
                <tr v-for="f in ocrData.license" :key="f.label">
                  <td>{{ f.label }}</td><td>{{ f.value }}</td>
                  <td><span :class="f.confidence > 98 ? 'tag tag-green' : 'tag tag-orange'">{{ f.confidence }}%</span></td>
                </tr>
              </tbody>
            </table>
            <h4 style="margin:16px 0 8px;color:#333">AI风险预判</h4>
            <div class="grid-2">
              <div v-for="r in ocrData.risk" :key="r.item" class="list-item">
                <div class="list-item-dot" :class="r.status === 'pass' ? 'green' : 'orange'"></div>
                <div class="list-item-content">
                  <div class="list-item-title">{{ r.item }}</div>
                  <div class="list-item-desc">{{ r.desc }}</div>
                </div>
                <span :class="r.status === 'pass' ? 'tag tag-green' : 'tag tag-orange'">{{ r.status === 'pass' ? '通过' : '需复核' }}</span>
              </div>
            </div>
            <div class="alert alert-warning" style="margin-top:12px">
              ⚠️ 风险等级：低风险 · 建议进入标准审核流程（快速通道）
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button v-if="!processing" class="btn btn-primary" @click="replay('ocr')">重新识别</button>
        </div>
      </div>
    </div>

    <!-- 合同智能质检弹窗 -->
    <div v-if="modal === 'contract'" class="modal-overlay" @click.self="modal = ''">
      <div class="modal">
        <div class="modal-header"><h3>📋 合同智能质检 — 大模型+NLP条款审查</h3><button class="modal-close" @click="modal = ''">×</button></div>
        <div class="modal-body">
          <template v-if="processing">
            <div class="ai-processing">
              <div class="ai-spinner"></div>
              <div class="ai-status">大模型解析合同条款中...</div>
            </div>
          </template>
          <template v-else>
            <div class="alert alert-warning">⚠️ 质检完成 · 处理耗时 3.2秒 · 风险评级：中风险</div>
            <div class="grid-3" style="margin-bottom:16px">
              <div class="stat-card green"><div class="stat-label">通过条款</div><div class="stat-value" style="font-size:22px">3</div></div>
              <div class="stat-card orange"><div class="stat-label">中风险</div><div class="stat-value" style="font-size:22px">1</div></div>
              <div class="stat-card red"><div class="stat-label">高风险</div><div class="stat-value" style="font-size:22px">2</div></div>
            </div>
            <table class="data-table">
              <thead><tr><th>#</th><th>条款内容</th><th>类型</th><th>风险</th><th>AI修改建议</th></tr></thead>
              <tbody>
                <tr v-for="c in contractClauses" :key="c.id">
                  <td>{{ c.id }}</td>
                  <td style="max-width:200px;font-size:12px">{{ c.content }}</td>
                  <td><span class="tag tag-gray">{{ c.type }}</span></td>
                  <td><span :class="c.risk === 'high' ? 'tag tag-red' : c.risk === 'medium' ? 'tag tag-orange' : 'tag tag-green'">{{ c.risk === 'high' ? '高风险' : c.risk === 'medium' ? '中风险' : c.risk === 'low' ? '低风险' : '无风险' }}</span></td>
                  <td style="max-width:250px;font-size:12px;color:#666">{{ c.suggestion || '—' }}</td>
                </tr>
              </tbody>
            </table>
            <div class="alert alert-info" style="margin-top:12px">
              💡 AI建议：合同存在2处高风险条款和1处中风险条款，建议修改后重新提交质检。重点关注终止条款和责任条款的公平性。
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button v-if="!processing" class="btn btn-primary" @click="replay('contract')">重新质检</button>
        </div>
      </div>
    </div>

    <!-- 价格智能摸排弹窗 -->
    <div v-if="modal === 'price'" class="modal-overlay" @click.self="modal = ''">
      <div class="modal">
        <div class="modal-header"><h3>💹 价格智能摸排 — AI多维度比价</h3><button class="modal-close" @click="modal = ''">×</button></div>
        <div class="modal-body">
          <template v-if="processing">
            <div class="ai-processing">
              <div class="ai-spinner"></div>
              <div class="ai-status">AI采集主流电商平台价格数据中...</div>
            </div>
          </template>
          <template v-else>
            <div class="alert alert-success">✅ 摸排完成 · 处理耗时 2.8秒 · 价格竞争力：优秀</div>
            <div class="grid-2">
              <div>
                <h4 style="margin-bottom:8px;color:#333">各平台价格对比</h4>
                <table class="data-table">
                  <thead><tr><th>平台</th><th>价格</th><th>与商城差价</th></tr></thead>
                  <tbody>
                    <tr v-for="p in priceData.platforms" :key="p.platform">
                      <td>{{ p.platform }}</td>
                      <td>¥{{ p.price.toFixed(2) }}</td>
                      <td><span class="tag tag-green">低{{ ((p.price - priceData.ourPrice) / p.price * 100).toFixed(1) }}%</span></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div>
                <h4 style="margin-bottom:8px;color:#333">价格分析</h4>
                <div class="card" style="margin-bottom:8px">
                  <div style="font-size:13px;color:#888">商城售价</div>
                  <div style="font-size:24px;font-weight:700;color:#1a237e">¥{{ priceData.ourPrice.toFixed(2) }}</div>
                </div>
                <div class="card" style="margin-bottom:8px">
                  <div style="font-size:13px;color:#888">市场均价</div>
                  <div style="font-size:20px;font-weight:600;color:#666">¥{{ priceData.marketAvg.toFixed(2) }}</div>
                </div>
                <div class="card">
                  <div style="font-size:13px;color:#888">价格优势</div>
                  <div style="font-size:20px;font-weight:700;color:#4caf50">低15.3%</div>
                </div>
              </div>
            </div>
            <div class="alert alert-success" style="margin-top:12px">
              💡 AI建议：当前定价低于市场均价15.3%，价格竞争力优秀。建议保持当前定价策略，可在营销活动中作为引流商品重点推广。
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button v-if="!processing" class="btn btn-primary" @click="replay('price')">重新摸排</button>
        </div>
      </div>
    </div>

    <!-- 商品卖点提炼弹窗 -->
    <div v-if="modal === 'selling'" class="modal-overlay" @click.self="modal = ''">
      <div class="modal">
        <div class="modal-header"><h3>✨ 商品卖点提炼 — 大模型文案生成</h3><button class="modal-close" @click="modal = ''">×</button></div>
        <div class="modal-body">
          <template v-if="processing">
            <div class="ai-processing">
              <div class="ai-spinner"></div>
              <div class="ai-status">大语言模型生成卖点文案中...</div>
            </div>
          </template>
          <template v-else>
            <div class="alert alert-success">✅ 生成完成 · 处理耗时 2.8秒 · 模型：大语言模型</div>
            <div style="background:#f5f7fa;padding:12px;border-radius:6px;margin-bottom:16px">
              <div style="font-size:12px;color:#999;margin-bottom:4px">输入信息</div>
              <div style="font-size:13px;color:#333">腾讯视频VIP会员月卡，可观看VIP专享内容，支持4K超清，无广告，多设备同时登录</div>
            </div>
            <div v-for="o in sellingOutputs" :key="o.type" class="card" style="margin-bottom:12px">
              <h4 style="font-size:14px;color:#1a237e;margin-bottom:8px">{{ o.type }}</h4>
              <pre style="white-space:pre-wrap;font-size:13px;color:#333;font-family:inherit;line-height:1.8">{{ o.content }}</pre>
            </div>
            <div style="margin-top:12px">
              <span style="font-size:13px;color:#888">核心亮点：</span>
              <span v-for="h in highlights" :key="h" class="tag tag-purple" style="margin:0 4px">{{ h }}</span>
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button v-if="!processing" class="btn btn-primary" @click="replay('selling')">重新生成</button>
        </div>
      </div>
    </div>

    <!-- 信息高效录入弹窗 -->
    <div v-if="modal === 'entry'" class="modal-overlay" @click.self="modal = ''">
      <div class="modal">
        <div class="modal-header"><h3>✍️ 信息高效录入 — AI辅助补全</h3><button class="modal-close" @click="modal = ''">×</button></div>
        <div class="modal-body">
          <template v-if="processing">
            <div class="ai-processing">
              <div class="ai-spinner"></div>
              <div class="ai-status">AI分析商品信息并智能补全中...</div>
            </div>
          </template>
          <template v-else>
            <div class="alert alert-success">✅ 补全完成 · 录入耗时从30分钟缩短至5分钟</div>
            <table class="data-table">
              <thead><tr><th>字段</th><th>用户输入</th><th>AI补全</th><th>来源</th></tr></thead>
              <tbody>
                <tr><td>商品名称</td><td>爱奇艺黄金会员</td><td>爱奇艺黄金会员月卡</td><td><span class="tag tag-blue">NLP补全</span></td></tr>
                <tr><td>商品分类</td><td>—</td><td>视频娱乐 → 视频会员</td><td><span class="tag tag-blue">分类推荐</span></td></tr>
                <tr><td>商品描述</td><td>—</td><td>爱奇艺黄金VIP会员月卡，享受专属内容...</td><td><span class="tag tag-purple">大模型生成</span></td></tr>
                <tr><td>建议售价</td><td>—</td><td>¥19.90（基于市场摸排）</td><td><span class="tag tag-green">价格摸排</span></td></tr>
                <tr><td>有效期</td><td>—</td><td>30天</td><td><span class="tag tag-blue">规则匹配</span></td></tr>
                <tr><td>使用说明</td><td>—</td><td>1.购买后自动发卡 2.登录爱奇艺兑换...</td><td><span class="tag tag-purple">模板匹配</span></td></tr>
              </tbody>
            </table>
          </template>
        </div>
        <div class="modal-footer">
          <button v-if="!processing" class="btn btn-primary" @click="replay('entry')">重新补全</button>
        </div>
      </div>
    </div>

    <!-- 文案智能校对弹窗 -->
    <div v-if="modal === 'proofread'" class="modal-overlay" @click.self="modal = ''">
      <div class="modal">
        <div class="modal-header"><h3>📝 文案智能校对 — 大模型规范校验</h3><button class="modal-close" @click="modal = ''">×</button></div>
        <div class="modal-body">
          <template v-if="processing">
            <div class="ai-processing">
              <div class="ai-spinner"></div>
              <div class="ai-status">大模型校对文案准确性与规范性中...</div>
            </div>
          </template>
          <template v-else>
            <div class="alert alert-warning">⚠️ 校对完成 · 发现3处问题</div>
            <div style="background:#fff3e0;padding:12px;border-radius:6px;margin-bottom:16px;border-left:4px solid #ff9800">
              <div style="font-size:12px;color:#999;margin-bottom:4px">原始文案</div>
              <div style="font-size:13px;color:#333;line-height:1.8">爱奇艺黄金VIP会员月卡，超清画质，无广告，最便宜的价格，让你追剧不停！支持所有设备登录！</div>
            </div>
            <h4 style="margin-bottom:8px;color:#333">校对结果</h4>
            <div class="list-item" style="flex-direction:column;align-items:flex-start">
              <div style="display:flex;align-items:center;width:100%">
                <div class="list-item-dot red"></div>
                <div class="list-item-content"><div class="list-item-title">违规用语："最便宜的价格"</div></div>
                <span class="tag tag-red">违反广告法</span>
              </div>
              <div style="font-size:12px;color:#666;margin-left:20px;margin-top:4px">→ 建议修改为："超值优惠价"</div>
            </div>
            <div class="list-item" style="flex-direction:column;align-items:flex-start">
              <div style="display:flex;align-items:center;width:100%">
                <div class="list-item-dot orange"></div>
                <div class="list-item-content"><div class="list-item-title">绝对化用语："支持所有设备"</div></div>
                <span class="tag tag-orange">表述不准确</span>
              </div>
              <div style="font-size:12px;color:#666;margin-left:20px;margin-top:4px">→ 建议修改为："支持多设备同时登录"</div>
            </div>
            <div class="list-item" style="flex-direction:column;align-items:flex-start">
              <div style="display:flex;align-items:center;width:100%">
                <div class="list-item-dot orange"></div>
                <div class="list-item-content"><div class="list-item-title">标点规范：感叹号过多</div></div>
                <span class="tag tag-orange">格式问题</span>
              </div>
              <div style="font-size:12px;color:#666;margin-left:20px;margin-top:4px">→ 建议保留一个感叹号</div>
            </div>
            <div class="alert alert-success" style="margin-top:12px">
              ✅ 修改后文案：爱奇艺黄金VIP会员月卡，超清画质，无广告，超值优惠价，让你追剧不停！支持多设备同时登录。
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button v-if="!processing" class="btn btn-primary" @click="replay('proofread')">重新校对</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const modal = ref('')
const processing = ref(false)

const aiFeatures = [
  { id: 'ocr', name: '商户入驻智能识别', icon: '🔍', desc: 'OCR自动识别营业执照、法人身份证等资质文件，NLP校验信息一致性', demoType: 'ocr' },
  { id: 'entry', name: '信息高效录入', icon: '✍️', desc: 'AI辅助商品信息补全、格式校验、批量导入，录入效率提升6倍', demoType: 'entry' },
  { id: 'proofread', name: '文案智能校对', icon: '📝', desc: '大模型智能校对商品描述、营销文案的准确性与规范性', demoType: 'proofread' },
  { id: 'contract', name: '合同智能质检', icon: '📋', desc: '大模型+NLP解析合同条款，自动识别合规风险并生成修改建议', demoType: 'contract' },
  { id: 'price', name: '价格智能摸排', icon: '💹', desc: 'AI采集主流平台价格，多维度比对分析，给出定价建议', demoType: 'price' },
  { id: 'selling', name: '商品卖点提炼', icon: '✨', desc: '大模型自动生成商品卖点描述和营销文案', demoType: 'selling' },
]

const ocrData = {
  license: [
    { label: '统一社会信用代码', value: '91110108MA01ABC23X', confidence: 99.2 },
    { label: '企业名称', value: '瑞幸咖啡（中国）有限公司', confidence: 98.8 },
    { label: '企业类型', value: '有限责任公司（台港澳法人独资）', confidence: 97.5 },
    { label: '法定代表人', value: '郭谨一', confidence: 99.5 },
    { label: '注册资本', value: '15000万美元', confidence: 98.3 },
    { label: '成立日期', value: '2018年03月02日', confidence: 99.0 },
    { label: '营业期限', value: '2018-03-02 至 2048-03-01', confidence: 98.6 },
    { label: '经营范围', value: '餐饮服务；食品经营；销售工艺品等', confidence: 96.2 },
  ],
  risk: [
    { item: '营业执照有效期', status: 'pass', desc: '有效期至2048年，状态正常' },
    { item: '注册资本验证', status: 'pass', desc: '注册资本充足' },
    { item: '经营范围匹配', status: 'pass', desc: '经营范围与入驻品类匹配' },
    { item: '企业信用查询', status: 'warning', desc: '存在2条经营异常记录，需人工复核' },
    { item: '法人身份核验', status: 'pass', desc: '法人身份信息与4A系统匹配' },
  ]
}

const contractClauses = [
  { id: 1, content: '甲方应于合同签订后30日内向乙方支付首期合作保证金人民币伍万元整。', type: '付款条款', risk: 'low', suggestion: '' },
  { id: 2, content: '乙方有权在任何情况下单方面终止本合同，且无需承担违约责任。', type: '终止条款', risk: 'high', suggestion: '建议修改为"乙方在甲方严重违约且经书面通知30日仍未纠正的情况下方可终止"' },
  { id: 3, content: '甲方应保证所提供商品符合国家相关质量标准及法律法规要求。', type: '质量条款', risk: 'none', suggestion: '' },
  { id: 4, content: '本合同项下商品销售价格由乙方单方面确定。', type: '价格条款', risk: 'medium', suggestion: '建议增加价格协商机制："商品销售价格应由双方协商确定"' },
  { id: 5, content: '甲方因商品质量问题造成的损失，乙方不承担任何连带责任。', type: '责任条款', risk: 'high', suggestion: '该条款存在法律风险，建议修改为"由甲方承担主要责任，乙方在过错范围内承担相应责任"' },
  { id: 6, content: '本合同争议提交北京仲裁委员会仲裁解决。', type: '争议解决', risk: 'none', suggestion: '' },
]

const priceData = {
  ourPrice: 19.90,
  marketAvg: 23.50,
  platforms: [
    { platform: '京东', price: 24.90 },
    { platform: '天猫', price: 23.00 },
    { platform: '拼多多', price: 21.90 },
    { platform: '淘宝', price: 22.50 },
    { platform: '抖音', price: 25.00 },
  ]
}

const sellingOutputs = [
  { type: '核心卖点', content: '🎬 VIP专享海量内容，院线大片抢先看\n📺 4K超清画质，沉浸式观影体验\n🚫 纯净无广告，追剧零打扰\n📱 多设备同时在线，全家共享好时光' },
  { type: '营销文案', content: '🔥 腾讯视频VIP月卡，低至5折！\n追剧不等待，大片随心看！4K超清+无广告+多设备同登，一站式满足全家观影需求。\n⏰ 限时特惠，抢完即止！' },
  { type: '短标题', content: '腾讯视频VIP月卡｜4K超清·无广告·多设备同登' },
  { type: '社交分享文案', content: '终于等到腾讯视频VIP打折了！月卡只要一杯奶茶钱，4K大片随便看，还能多设备同时用，赶紧冲！ #腾讯视频VIP #限时优惠' },
]

const highlights = ['4K超清画质', 'VIP专享内容', '无广告纯净体验', '多设备同时登录', '院线大片抢先看']

function openDemo(type) {
  modal.value = type
  processing.value = true
  setTimeout(() => { processing.value = false }, 2000)
}

function replay(type) {
  processing.value = true
  setTimeout(() => { processing.value = false }, 2000)
}
</script>
