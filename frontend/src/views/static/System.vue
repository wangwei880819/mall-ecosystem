<template>
  <div>
    <h1 class="page-title">⚙️ 系统管理</h1>
    

    <div class="grid-3">
      <div class="card">
        <div class="card-header"><h3>👥 用户管理</h3></div>
        <p style="color:#666;font-size:13px;line-height:1.8">
          • 系统用户总数：256人<br>
          • 角色体系：5级（超级管理员→系统管理员→业务操作员→审计员→普通用户）<br>
          • 在线用户：43人<br>
          • 待激活账号：8个
        </p>
        <div style="margin-top:8px"><button class="btn btn-outline btn-sm">查看详情（静态）</button></div>
      </div>
      <div class="card">
        <div class="card-header"><h3>🔐 权限管理</h3></div>
        <p style="color:#666;font-size:13px;line-height:1.8">
          • RBAC模型：角色-权限映射<br>
          • 金库管控：特权操作临时授权<br>
          • 权限审计日志：全链路追溯<br>
          • 会话安全：超时注销+多终端互斥
        </p>
        <div style="margin-top:8px"><button class="btn btn-outline btn-sm">查看详情（静态）</button></div>
      </div>
      <div class="card">
        <div class="card-header"><h3>📊 统计报表</h3></div>
        <p style="color:#666;font-size:13px;line-height:1.8">
          • 运营月报：自动生成<br>
          • 结算报表：按月/季/年<br>
          • 稽核报告：实时+定期<br>
          • 商户考核报告：月度/季度/年度
        </p>
        <div style="margin-top:8px"><button class="btn btn-outline btn-sm">查看详情（静态）</button></div>
      </div>
    </div>

    <div class="grid-3">
      <div class="card">
        <div class="card-header"><h3>📜 历史记录</h3></div>
        <p style="color:#666;font-size:13px;line-height:1.8">
          • 操作日志：全部操作留痕<br>
          • 审计日志：全链路可追溯<br>
          • 登录日志：异常检测<br>
          • 系统日志：运行状态监控
        </p>
      </div>
      <div class="card">
        <div class="card-header"><h3>🔌 接口管理</h3></div>
        <p style="color:#666;font-size:13px;line-height:1.8">
          • API网关：统一路由/鉴权/限流<br>
          • 接口配置中心：可视化配置<br>
          • 对接监控看板：实时监控<br>
          • 接口版本管理：平滑切换
        </p>
      </div>
      <div class="card">
        <div class="card-header"><h3>⚙️ 系统配置</h3></div>
        <p style="color:#666;font-size:13px;line-height:1.8">
          • 参数配置：业务规则可配置<br>
          • 模板管理：合同/报账/通知模板<br>
          • 定时任务：自动化调度<br>
          • 系统监控：健康度看板
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>
