/**
 * 订单状态映射
 */
export const ORDER_STATUS = {
  CREATED: '待支付',
  PAID: '已支付',
  FULFILLED: '已发货',
  EVALUATED: '已完成',
  REFUNDED: '已退款',
  CANCELLED: '已取消'
}

export const ORDER_STATUS_TYPE = {
  CREATED: 'warning',
  PAID: 'primary',
  FULFILLED: 'success',
  EVALUATED: 'info',
  REFUNDED: 'danger',
  CANCELLED: 'info'
}

/**
 * 支付状态映射
 */
export const PAY_STATUS = {
  PENDING: '待支付',
  SUCCESS: '支付成功',
  FAILED: '支付失败',
  REFUNDED: '已退款'
}

export const PAY_STATUS_TYPE = {
  PENDING: 'warning',
  SUCCESS: 'success',
  FAILED: 'danger',
  REFUNDED: 'info'
}

/**
 * 支付方式映射
 */
export const PAY_METHOD = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  BANK: '银行卡',
  AI_DOUB: 'AI豆'
}

/**
 * 退款状态映射
 */
export const REFUND_STATUS = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REFUNDED: '已退款',
  REJECTED: '已拒绝'
}

export const REFUND_STATUS_TYPE = {
  PENDING: 'warning',
  APPROVED: 'primary',
  REFUNDED: 'success',
  REJECTED: 'danger'
}

/**
 * 退款类型映射
 */
export const REFUND_TYPE = {
  FULL: '全额退款',
  PARTIAL: '部分退款',
  OUT_OF_STOCK: '缺货退款'
}

/**
 * 结算状态映射
 */
export const SETTLE_STATUS = {
  COMPLETED: '已完成',
  PENDING: '待审批',
  PAID: '已支付'
}

export const SETTLE_STATUS_CLASS = {
  COMPLETED: 'tag tag-green',
  PENDING: 'tag tag-orange',
  PAID: 'tag tag-blue'
}

/**
 * 结算类型映射
 */
export const SETTLE_TYPE_TEXT = {
  COMMISSION: '佣金结算',
  AI_DOU: 'AI豆结算',
  EXPANSION: '商拓费结算'
}

export const SETTLE_TYPE_CLASS = {
  COMMISSION: 'tag tag-green',
  AI_DOU: 'tag tag-blue',
  EXPANSION: 'tag tag-purple'
}

/**
 * 商户入驻状态映射
 */
export const MERCHANT_ONBOARDING_STATUS = {
  PENDING: '待审核',
  REVIEWING: '审核中',
  APPROVED: '已入驻',
  REJECTED: '驳回'
}

export const MERCHANT_ONBOARDING_STATUS_TYPE = {
  PENDING: 'info',
  REVIEWING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger'
}

/**
 * 商户类型映射
 */
export const MERCHANT_TYPE_TEXT = {
  DIGITAL: '数字权益',
  PHYSICAL: '实物商品',
  LOCAL_LIFE: '本地生活'
}

export const MERCHANT_TYPE_TAG = {
  DIGITAL: 'primary',
  PHYSICAL: 'success',
  LOCAL_LIFE: 'warning'
}

/**
 * 商户审核节点映射
 */
export const MERCHANT_AUDIT_NODE = {
  QUALIFICATION: '资质初审',
  BUSINESS: '业务复审',
  COMPLIANCE: '合规终审',
  CONTRACT: '合同签署',
  PAYMENT: '支付进件',
  PRODUCT: '商品录入',
  COMPLETED: '已完成'
}

export const MERCHANT_AUDIT_NODE_TYPE = {
  QUALIFICATION: 'info',
  BUSINESS: 'warning',
  COMPLIANCE: 'danger',
  CONTRACT: 'info',
  PAYMENT: 'info',
  PRODUCT: 'info',
  COMPLETED: 'success'
}
