<template>
  <div class="container">
    <h1 class="page-title">🏠 首页</h1>

    <div class="grid-4">
      <div class="stat-card blue">
        <div class="stat-label">入驻商户</div>
        <div class="stat-value">16,832</div>
        <div class="stat-trend up">↑ 12.3% 较上月</div>
        <div class="stat-sub">活跃 14,256 · 待审核 86</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">在售商品</div>
        <div class="stat-value">3,286</div>
        <div class="stat-trend up">↑ 8.6% 较上月</div>
        <div class="stat-sub">在售 2,891 · 待审 42</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">今日订单</div>
        <div class="stat-value">15,632</div>
        <div class="stat-trend up">↑ 15.2% 较昨日</div>
        <div class="stat-sub">订单金额 ¥486,200</div>
      </div>
      <div class="stat-card purple">
        <div class="stat-label">月度结算</div>
        <div class="stat-value">¥1,286万</div>
        <div class="stat-trend up">↑ 6.8% 较上月</div>
        <div class="stat-sub">待审批 23笔</div>
      </div>
    </div>

    <div class="grid-2" style="margin-top:16px">
      <div class="card">
        <div class="card-header"><h3>📌 待办事项</h3><span class="tag tag-red">5项待处理</span></div>
        <div v-for="item in todoList" :key="item.title" class="list-item">
          <div class="list-item-dot" :class="item.priority === 'urgent' ? 'red' : item.priority === 'important' ? 'orange' : 'blue'"></div>
          <div class="list-item-content">
            <div class="list-item-title">{{ item.title }}</div>
            <div class="list-item-desc">{{ item.desc }}</div>
          </div>
          <div class="list-item-time">{{ item.time }}</div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><h3>⚠️ 预警信息</h3><span class="tag tag-orange">4条预警</span></div>
        <div v-for="item in alertList" :key="item.title" class="list-item">
          <div class="list-item-dot" :class="item.level === 'error' ? 'red' : item.level === 'warning' ? 'orange' : 'blue'"></div>
          <div class="list-item-content">
            <div class="list-item-title">{{ item.title }}</div>
            <div class="list-item-desc">{{ item.desc }}</div>
          </div>
          <div class="list-item-time">{{ item.time }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const todoList = [
  { title: '瑞幸咖啡入驻申请待审核', desc: '资质初审环节 · 超时预警', time: '10分钟前', priority: 'urgent' },
  { title: '8月佣金结算待审批', desc: '涉及127家商户 · 金额¥86.5万', time: '1小时前', priority: 'important' },
  { title: '异常交易预警待核查', desc: '商户"优选好物"疑似刷单', time: '2小时前', priority: 'urgent' },
  { title: '腾讯视频会员月卡上架审核', desc: '选品委员会终审环节', time: '3小时前', priority: 'important' },
  { title: '差评工单待处理', desc: '3条差评需跟进处理', time: '4小时前', priority: 'normal' },
]

const alertList = [
  { title: '商户风险预警', desc: '商户"快消优选"连续3日异常交易量激增', time: '5分钟前', level: 'error' },
  { title: '结算异常预警', desc: '订单#2024082300156结算金额异常', time: '30分钟前', level: 'warning' },
  { title: '价格偏离预警', desc: '爱奇艺会员月卡价格低于市场均价18%', time: '1小时前', level: 'warning' },
  { title: '入驻审核超时', desc: '3家商户入驻审核即将超时', time: '2小时前', level: 'info' },
]
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

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.grid-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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

.stat-sub {
  color: #999;
  font-size: 13px;
  margin-top: 8px;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.list-item:last-child {
  border-bottom: none;
}

.list-item-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 16px;
}

.list-item-dot.red {
  background: #f56c6c;
}

.list-item-dot.orange {
  background: #e6a23c;
}

.list-item-dot.blue {
  background: #409eff;
}

.list-item-content {
  flex: 1;
}

.list-item-title {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.list-item-desc {
  color: #999;
  font-size: 13px;
  margin-top: 4px;
}

.list-item-time {
  color: #999;
  font-size: 13px;
}
</style>
