# B端与C端数据打通API文档

## 1. 概述

本文档定义了B端（商家管理端）与C端（消费者端）数据打通的RESTful API接口规范。

## 2. 接口规范

### 2.1 请求格式

- Content-Type: application/json
- 认证方式: Bearer Token (JWT)
- 编码: UTF-8

### 2.2 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 2.3 状态码定义

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器错误 |

## 3. 数据同步接口

### 3.1 同步商品

**POST** /api/sync/product/{id}

请求示例:
```json
{}
```

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "productId": 1,
    "productCode": "PRD123456",
    "syncTime": "2026-07-27T10:00:00",
    "syncId": 1,
    "success": true,
    "message": "商品数据同步成功"
  }
}
```

### 3.2 批量同步商品

**POST** /api/sync/product/batch

请求示例:
```json
{
  "productIds": [1, 2, 3, 4, 5]
}
```

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "successCount": 5,
    "failCount": 0,
    "syncTime": "2026-07-27T10:00:00",
    "totalCount": 5
  }
}
```

### 3.3 同步订单

**POST** /api/sync/order/{id}

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 1,
    "orderCode": "ORD202607270001",
    "status": "PAID",
    "syncTime": "2026-07-27T10:00:00",
    "syncId": 2,
    "success": true,
    "message": "订单数据同步成功"
  }
}
```

### 3.4 同步订单状态

**POST** /api/sync/order/status/{id}

请求示例:
```json
{
  "status": "PAID"
}
```

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 1,
    "orderCode": "ORD202607270001",
    "oldStatus": "CREATED",
    "newStatus": "PAID",
    "syncTime": "2026-07-27T10:00:00",
    "syncId": 3,
    "success": true,
    "message": "订单状态同步成功"
  }
}
```

### 3.5 同步库存

**POST** /api/sync/stock/{productId}

请求示例:
```json
{
  "changeAmount": 100,
  "changeType": "IN",
  "reason": "采购入库"
}
```

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "productId": 1,
    "beforeStock": 50,
    "afterStock": 150,
    "changeAmount": 100,
    "changeType": "IN",
    "syncTime": "2026-07-27T10:00:00",
    "syncId": 4,
    "success": true,
    "message": "库存数据同步成功"
  }
}
```

## 4. 数据一致性接口

### 4.1 校验数据一致性

**GET** /api/sync/consistency/validate

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "validatedAt": "2026-07-27T10:00:00",
    "totalInconsistencies": 0,
    "inconsistencies": [],
    "isConsistent": true
  }
}
```

### 4.2 修复数据不一致

**POST** /api/sync/consistency/repair/{type}/{id}

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "message": "商品数据修复成功"
  }
}
```

## 5. 监控告警接口

### 5.1 获取监控统计

**GET** /api/sync/monitor/stats

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalSyncCount": 1000,
    "successSyncCount": 995,
    "failedSyncCount": 5,
    "successRate": 99.5,
    "lastSyncTime": "2026-07-27T10:00:00",
    "lastAlertTime": "2026-07-27T09:30:00",
    "activeAlertCount": 0,
    "monitorTime": "2026-07-27T10:00:00"
  }
}
```

### 5.2 获取告警列表

**GET** /api/sync/monitor/alerts

## 6. 商品接口

### 6.1 获取商品列表

**GET** /api/product?page=0&size=20

### 6.2 获取商品详情

**GET** /api/product/{id}

### 6.3 创建商品

**POST** /api/product

### 6.4 更新商品

**PUT** /api/product/{id}

### 6.5 删除商品

**DELETE** /api/product/{id}

## 7. 订单接口

### 7.1 获取订单列表

**GET** /api/order?page=0&size=20

### 7.2 获取订单详情

**GET** /api/order/{id}

### 7.3 创建订单

**POST** /api/order

### 7.4 支付订单

**PUT** /api/order/{id}/pay

### 7.5 发货

**PUT** /api/order/{id}/fulfill

### 7.6 取消订单

**PUT** /api/order/{id}/cancel

## 8. C端接口

### 8.1 获取商品列表

**GET** /api/c-mall/products?category=&page=0&size=20

### 8.2 获取商品详情

**GET** /api/c-mall/products/{id}

### 8.3 创建订单

**POST** /api/c-mall/orders

### 8.4 支付订单

**POST** /api/c-mall/orders/{id}/pay

### 8.5 获取用户订单

**GET** /api/c-mall/orders?customerPhone=13800138000

## 9. 数据模型

### 9.1 商品模型 (Product)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 商品ID |
| productCode | String | 商品编号 |
| productName | String | 商品名称 |
| categoryId | Long | 分类ID |
| category | String | 分类名称 |
| brand | String | 品牌 |
| merchantId | Long | 商户ID |
| price | BigDecimal | 售价 |
| marketPrice | BigDecimal | 市场价 |
| vipPrice | BigDecimal | VIP价格 |
| stock | Integer | 库存 |
| salesCount | Integer | 销量 |
| avgScore | BigDecimal | 平均评分 |
| description | String | 描述 |
| imageUrls | String | 图片URL |
| status | String | 状态 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

### 9.2 订单模型 (MallOrder)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单ID |
| orderCode | String | 订单编号 |
| customerId | Long | 用户ID |
| customerPhone | String | 用户手机号 |
| merchantId | Long | 商户ID |
| productId | Long | 商品ID |
| productName | String | 商品名称 |
| productImage | String | 商品图片 |
| price | BigDecimal | 单价 |
| quantity | Integer | 数量 |
| orderAmount | BigDecimal | 订单金额 |
| payAmount | BigDecimal | 实付金额 |
| status | String | 订单状态 |
| payMethod | String | 支付方式 |
| payTime | LocalDateTime | 支付时间 |
| logisticsNo | String | 物流单号 |
| logisticsCompany | String | 物流公司 |
| fulfillTime | LocalDateTime | 发货时间 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

### 9.3 订单状态流转

```
CREATED → PAID → FULFILLED → EVALUATED
    ↓           ↓
CANCELLED    REFUNDED
```

## 10. 认证接口

### 10.1 登录

**POST** /api/auth/login

请求示例:
```json
{
  "username": "admin",
  "password": "password"
}
```

响应示例:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "role": "SUPER_ADMIN",
      "platforms": [1, 2, 3, 4]
    }
  }
}
```

### 10.2 刷新Token

**POST** /api/auth/refresh

请求示例:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## 11. 安全机制

### 11.1 JWT认证

- Token有效期: 24小时
- Refresh Token有效期: 7天
- Token黑名单机制

### 11.2 API限流

- 默认限流: 100次/分钟/IP
- 批量接口限流: 10次/分钟/IP
- 触发限流后封禁IP 5分钟

### 11.3 HTTPS

所有接口强制使用HTTPS协议传输。

## 12. 数据同步策略

### 12.1 实时同步

- 商品创建/更新后立即同步
- 订单状态变更后立即同步
- 库存变更后立即同步

### 12.2 定时同步

- 每小时执行一次数据一致性校验
- 每天生成同步报告

### 12.3 增量同步

- 通过syncId标记已同步数据
- 支持断点续传

## 13. 数据一致性保障

### 13.1 一致性校验

- 价格非负校验
- 库存非负校验
- 库存计算一致性校验
- 订单金额校验

### 13.2 异常处理

- 同步失败自动重试（最多3次）
- 数据不一致自动修复
- 告警通知机制

### 13.3 监控告警

- 同步超时告警（超过5分钟）
- 数据不一致告警
- 同步成功率低于95%告警