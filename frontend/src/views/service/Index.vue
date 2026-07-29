<template>
  <div>
    <h1 class="page-title">⭐ 服务能力 — 多维度订单评价支撑体系</h1>
    

    <!-- 评价概览 -->
    <div class="grid-4">
      <div class="stat-card blue">
        <div class="stat-label">综合评分</div>
        <div class="stat-value">4.62</div>
        <div class="stat-trend up">↑ 2.1% 较上月</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">评价总数</div>
        <div class="stat-value">89,230</div>
        <div class="stat-sub">覆盖率高</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">好评率</div>
        <div class="stat-value">94.3%</div>
        <div class="stat-trend up">↑ 1.5%</div>
      </div>
      <div class="stat-card purple">
        <div class="stat-label">商户回复率</div>
        <div class="stat-value">96.8%</div>
        <div class="stat-sub">平均回复 2.3小时</div>
      </div>
    </div>

    <!-- 五维度评价体系 -->
    <h2 class="section-title">五维度评价体系</h2>
    <div class="card">
      <div class="grid-5" style="display:grid;grid-template-columns:repeat(5,1fr);gap:16px">
        <div v-for="d in dimensions" :key="d.name" style="text-align:center">
          <div style="font-size:13px;color:#888;margin-bottom:8px">{{ d.name }}</div>
          <div style="font-size:32px;font-weight:700;color:#1a237e">{{ d.score }}</div>
          <div style="font-size:11px;color:#999;margin-top:4px">{{ d.total.toLocaleString() }}条</div>
          <div class="progress-bar" style="margin-top:8px">
            <div class="progress-fill" :class="d.score >= 4.5 ? 'green' : 'orange'" :style="{ width: (d.score / 5 * 100) + '%' }"></div>
          </div>
          <!-- 星级分布 -->
          <div style="margin-top:8px;font-size:11px;color:#aaa">
            <span v-for="(pct, i) in d.distribution" :key="i" style="display:inline-block;margin:0 2px">
              {{ 5 - i }}★{{ pct }}%
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新评价 -->
    <div class="grid-2">
      <div class="card">
        <div class="card-header"><h3>最新评价动态</h3></div>
        <div v-for="r in reviews" :key="r.id" class="list-item" style="flex-direction:column;align-items:flex-start">
          <div style="display:flex;justify-content:space-between;width:100%">
            <div>
              <span style="font-weight:600">{{ '⭐'.repeat(r.score) }}</span>
              <span style="margin-left:8px;font-size:13px;color:#333">{{ r.product }}</span>
            </div>
            <span style="font-size:12px;color:#ccc">{{ r.date }}</span>
          </div>
          <div style="font-size:13px;color:#666;margin-top:4px">{{ r.content }}</div>
          <div style="margin-top:4px">
            <span v-for="t in r.tags" :key="t" class="tag tag-blue" style="margin-right:4px">{{ t }}</span>
            <span :class="r.sentiment === 'positive' ? 'tag tag-green' : r.sentiment === 'negative' ? 'tag tag-red' : 'tag tag-gray'">
              {{ r.sentiment === 'positive' ? '正面' : r.sentiment === 'negative' ? '负面' : '中性' }}
            </span>
          </div>
        </div>
      </div>

      <!-- 商户评价考核 -->
      <div class="card">
        <div class="card-header"><h3>商户评价考核排名</h3></div>
        <table class="data-table">
          <thead>
            <tr><th>排名</th><th>商户</th><th>均分</th><th>评价数</th><th>回复率</th><th>趋势</th><th>等级</th></tr>
          </thead>
          <tbody>
            <tr v-for="(m, i) in merchantScores" :key="m.merchant">
              <td>{{ i + 1 }}</td>
              <td>{{ m.merchant }}</td>
              <td style="font-weight:600;color:#1a237e">{{ m.avgScore }}</td>
              <td>{{ m.totalReviews.toLocaleString() }}</td>
              <td>{{ m.responseRate }}</td>
              <td><span :class="m.trend.startsWith('+') ? 'tag tag-green' : 'tag tag-red'">{{ m.trend }}</span></td>
              <td><span :class="m.avgScore >= 4.8 ? 'tag tag-green' : m.avgScore >= 4.5 ? 'tag tag-blue' : m.avgScore >= 4.3 ? 'tag tag-orange' : 'tag tag-red'">{{ m.avgScore >= 4.8 ? 'A级' : m.avgScore >= 4.5 ? 'B级' : m.avgScore >= 4.3 ? 'C级' : 'D级' }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 评价闭环 -->
    <h2 class="section-title">评价考核闭环体系</h2>
    <div class="card">
      <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px">
        <div v-for="(s, i) in cycleSteps" :key="i" style="text-align:center;flex:1;min-width:120px">
          <div style="width:48px;height:48px;border-radius:50%;background:#e8eaf6;display:flex;align-items:center;justify-content:center;margin:0 auto 8px;font-size:24px">{{ s.icon }}</div>
          <div style="font-size:13px;font-weight:600;color:#333">{{ s.name }}</div>
          <div style="font-size:11px;color:#999;margin-top:2px">{{ s.desc }}</div>
        </div>
      </div>
    </div>

    <!-- AI内容审核 -->
    <h2 class="section-title">AI+人工双轨内容审核</h2>
    <div class="grid-3">
      <div class="card">
        <h3 style="font-size:14px;color:#4caf50">✅ AI可信通过</h3>
        <p style="color:#666;font-size:13px;margin-top:8px">AI模型自动检测，可信内容自动放行展示。敏感词库+上下文语义分析+刷评检测三重过滤。</p>
        <div style="margin-top:8px"><span class="tag tag-green">自动放行率 87.3%</span></div>
      </div>
      <div class="card">
        <h3 style="font-size:14px;color:#ff9800">⚠️ AI可疑待审</h3>
        <p style="color:#666;font-size:13px;margin-top:8px">可疑内容推送人工复核，确保审核准确性。覆盖AI可疑标记和用户申诉内容。</p>
        <div style="margin-top:8px"><span class="tag tag-orange">人工复核率 10.2%</span></div>
      </div>
      <div class="card">
        <h3 style="font-size:14px;color:#f44336">🚫 AI明确违规</h3>
        <p style="color:#666;font-size:13px;margin-top:8px">违规内容自动屏蔽，商户警告通知+累积处罚机制（首次警告→二次降级→三次冻结）。</p>
        <div style="margin-top:8px"><span class="tag tag-red">自动屏蔽率 2.5%</span></div>
      </div>
    </div>
  </div>
</template>

<script setup>
const dimensions = [
  { name: '商品质量', score: 4.65, total: 89230, distribution: [2, 5, 8, 25, 60] },
  { name: '配送速度', score: 4.52, total: 85120, distribution: [3, 6, 10, 28, 53] },
  { name: '客服服务', score: 4.71, total: 78930, distribution: [1, 3, 6, 22, 68] },
  { name: '售后体验', score: 4.43, total: 65230, distribution: [4, 7, 12, 30, 47] },
  { name: '性价比', score: 4.58, total: 88950, distribution: [2, 4, 9, 27, 58] },
]

const reviews = [
  { id: 'R001', product: '腾讯视频VIP月卡', score: 5, content: '兑换方便，秒到账，价格比官方便宜！', date: '14:32', tags: ['性价比高', '到账快'], sentiment: 'positive' },
  { id: 'R002', product: '美团外卖20元代金券', score: 4, content: '整体不错，就是有时段限制，希望能放宽使用时间。', date: '13:15', tags: ['有时间限制'], sentiment: 'neutral' },
  { id: 'R003', product: '网易云音乐黑胶VIP', score: 5, content: '音质提升明显，广告也少了，推荐购买！', date: '12:48', tags: ['音质好', '推荐'], sentiment: 'positive' },
  { id: 'R004', product: '京东PLUS会员季卡', score: 2, content: '收到卡密无法使用，联系客服处理中，体验不好。', date: '11:22', tags: ['卡密问题'], sentiment: 'negative' },
  { id: 'R005', product: '哔哩哔哩大会员月卡', score: 5, content: '番剧随便看，画质清晰，值得购买！', date: '10:05', tags: ['画质清晰', '内容丰富'], sentiment: 'positive' },
]

const merchantScores = [
  { merchant: '腾讯', avgScore: 4.82, totalReviews: 15632, responseRate: '98.5%', trend: '+0.3%' },
  { merchant: '美团', avgScore: 4.71, totalReviews: 23456, responseRate: '97.2%', trend: '+0.1%' },
  { merchant: '网易云音乐', avgScore: 4.65, totalReviews: 8923, responseRate: '96.8%', trend: '-0.2%' },
  { merchant: '哔哩哔哩', avgScore: 4.89, totalReviews: 11234, responseRate: '99.1%', trend: '+0.5%' },
  { merchant: '爱奇艺', avgScore: 4.23, totalReviews: 6789, responseRate: '92.3%', trend: '-0.8%' },
]

const cycleSteps = [
  { icon: '📝', name: '评价采集', desc: '多触点推送邀请' },
  { icon: '💬', name: '评价互动', desc: '回复+追问+晒单' },
  { icon: '🔍', name: '内容审核', desc: 'AI+人工双轨' },
  { icon: '📊', name: '考核互通', desc: '指标映射商户分级' },
  { icon: '🎯', name: '运营调整', desc: '佣金/权重联动' },
  { icon: '📈', name: '服务提升', desc: '正向驱动闭环' },
]
</script>
