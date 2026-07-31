import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/platform-select', name: 'PlatformSelect', component: () => import('../views/portal/PlatformSelect.vue'), meta: { title: '选择平台' } },
  { path: '/', redirect: '/login' },
  
  { path: '/portal', name: 'Portal', component: () => import('../views/portal/Dashboard.vue'), meta: { title: '首页', icon: '🏠' } },
  
  { path: '/merchant', name: 'Merchant', component: () => import('../views/onboarding/Merchant.vue'), meta: { title: '商户管理', icon: '🏢' } },
  { path: '/merchant/list', name: 'MerchantList', component: () => import('../views/onboarding/Merchant.vue'), meta: { title: '商户列表', icon: '📋', parent: 'Merchant' } },
  { path: '/merchant/audit', name: 'MerchantAudit', component: () => import('../views/onboarding/MerchantAudit.vue'), meta: { title: '资质审核', icon: '✅', parent: 'Merchant' } },
  { path: '/merchant/business-audit', name: 'BusinessAudit', component: () => import('../views/onboarding/BusinessAudit.vue'), meta: { title: '业务复审', icon: '📋', parent: 'Merchant' } },
  { path: '/merchant/compliance-audit', name: 'ComplianceAudit', component: () => import('../views/onboarding/ComplianceAudit.vue'), meta: { title: '合规终审', icon: '🔍', parent: 'Merchant' } },
  { path: '/merchant/contract-audit', name: 'ContractAudit', component: () => import('../views/onboarding/ContractAudit.vue'), meta: { title: '合同签署', icon: '📝', parent: 'Merchant' } },
  { path: '/merchant/payment-audit', name: 'PaymentAudit', component: () => import('../views/onboarding/PaymentAudit.vue'), meta: { title: '支付进件', icon: '💳', parent: 'Merchant' } },
  { path: '/merchant/detail/:id', name: 'MerchantDetail', component: () => import('../views/onboarding/Merchant.vue'), meta: { title: '商户详情', icon: '🔍', parent: 'Merchant' } },
  
  { path: '/customer', name: 'Customer', component: () => import('../views/customer/CustomerList.vue'), meta: { title: '客户管理', icon: '👥' } },
  { path: '/customer/list', name: 'CustomerList', component: () => import('../views/customer/CustomerList.vue'), meta: { title: '客户列表', icon: '📋', parent: 'Customer' } },
  { path: '/customer/tags', name: 'CustomerTags', component: () => import('../views/customer/CustomerTags.vue'), meta: { title: '客户标签', icon: '🏷️', parent: 'Customer' } },
  { path: '/customer/stats', name: 'CustomerStats', component: () => import('../views/customer/CustomerList.vue'), meta: { title: '客户统计', icon: '📊', parent: 'Customer' } },
  
  { path: '/product', name: 'Product', component: () => import('../views/onboarding/Product.vue'), meta: { title: '商品管理', icon: '📦' } },
  { path: '/product/list', name: 'ProductList', component: () => import('../views/onboarding/Product.vue'), meta: { title: '商品列表', icon: '📋', parent: 'Product' } },
  { path: '/product/audit', name: 'ProductAudit', component: () => import('../views/onboarding/ProductAudit.vue'), meta: { title: '商品审核', icon: '✅', parent: 'Product' } },
  { path: '/product/category', name: 'ProductCategory', component: () => import('../views/product/ProductCategory.vue'), meta: { title: '分类管理', icon: '📂', parent: 'Product' } },
  { path: '/product/stock', name: 'ProductStock', component: () => import('../views/product/ProductStock.vue'), meta: { title: '库存管理', icon: '📦', parent: 'Product' } },
  { path: '/product/benefit', name: 'Benefit', component: () => import('../views/onboarding/Benefit.vue'), meta: { title: '权益引入', icon: '🎁', parent: 'Product' } },
  
  { path: '/order', name: 'Order', component: () => import('../views/order/OrderList.vue'), meta: { title: '订单管理', icon: '📋' } },
  { path: '/order/list', name: 'OrderList', component: () => import('../views/order/OrderList.vue'), meta: { title: '订单列表', icon: '📋', parent: 'Order' } },
  { path: '/order/pay', name: 'OrderPay', component: () => import('../views/order/OrderPay.vue'), meta: { title: '支付管理', icon: '💳', parent: 'Order' } },
  { path: '/order/refund', name: 'OrderRefund', component: () => import('../views/order/OrderRefund.vue'), meta: { title: '退款管理', icon: '💰', parent: 'Order' } },
  
  { path: '/finance', name: 'Finance', component: () => import('../views/settlement/Index.vue'), meta: { title: '财务管理', icon: '💰' } },
  { path: '/finance/settlement', name: 'FinanceSettlement', component: () => import('../views/settlement/Index.vue'), meta: { title: '结算管理', icon: '📊', parent: 'Finance' } },
  { path: '/finance/invoice', name: 'FinanceInvoice', component: () => import('../views/finance/FinanceInvoice.vue'), meta: { title: '发票管理', icon: '📄', parent: 'Finance' } },
  { path: '/finance/reconciliation', name: 'FinanceReconciliation', component: () => import('../views/finance/FinanceReconciliation.vue'), meta: { title: '对账管理', icon: '🔍', parent: 'Finance' } },
  
  { path: '/risk', name: 'Risk', component: () => import('../views/audit/Index.vue'), meta: { title: '风险管理', icon: '🛡️' } },
  { path: '/risk/rules', name: 'RiskRules', component: () => import('../views/audit/Index.vue'), meta: { title: '规则管理', icon: '📋', parent: 'Risk' } },
  { path: '/risk/alerts', name: 'RiskAlerts', component: () => import('../views/risk/RiskAlert.vue'), meta: { title: '风险告警', icon: '🚨', parent: 'Risk' } },
  { path: '/risk/monitor', name: 'RiskMonitor', component: () => import('../views/risk/RiskMonitor.vue'), meta: { title: '交易监控', icon: '📈', parent: 'Risk' } },
  
  { path: '/order/evaluation', name: 'Evaluation', component: () => import('../views/service/Index.vue'), meta: { title: '订单评价', icon: '⭐', parent: 'Order' } },
  { path: '/ai', name: 'AI', component: () => import('../views/ai/Index.vue'), meta: { title: 'AI+应用', icon: '🤖' } },
  { path: '/ai/config', name: 'AIConfig', component: () => import('../views/ai/AIConfig.vue'), meta: { title: '模型配置', icon: '⚙️', parent: 'AI' } },
  { path: '/system', name: 'System', component: () => import('../views/static/System.vue'), meta: { title: '系统管理', icon: '⚙️' } },
  { path: '/system/users', name: 'UserManage', component: () => import('../views/system/UserManage.vue'), meta: { title: '用户管理', icon: '👥', parent: 'System' } },
  { path: '/system/roles', name: 'RoleManage', component: () => import('../views/system/RoleManage.vue'), meta: { title: '角色管理', icon: '🎭', parent: 'System' } },
  { path: '/system/menus', name: 'MenuManage', component: () => import('../views/system/MenuManage.vue'), meta: { title: '菜单管理', icon: '📑', parent: 'System' } },
  { path: '/system/platforms', name: 'PlatformManage', component: () => import('../views/sso/Index.vue'), meta: { title: '接入平台管理', icon: '🔗', parent: 'System' } },
  
  { path: '/cconfig', name: 'CConfig', component: () => import('../views/cconfig/HomeConfig.vue'), meta: { title: 'C端配置', icon: '⚙️' } },
  { path: '/cconfig/banners', name: 'BannerManage', component: () => import('../views/cconfig/BannerManage.vue'), meta: { title: '轮播图管理', icon: '🎠', parent: 'CConfig' } },
  { path: '/cconfig/home', name: 'HomeConfig', component: () => import('../views/cconfig/HomeConfig.vue'), meta: { title: '首页配置', icon: '🏠', parent: 'CConfig' } },
  { path: '/mall', name: 'CMall', component: () => import('../views/c-mall/Index.vue'), meta: { title: 'C端商城', icon: '🛒' } },
  { path: '/mall/login', name: 'CMallLogin', component: () => import('../views/c-mall/Login.vue'), meta: { title: '商城登录', icon: '🔐' } },
  { path: '/mall/register', name: 'CMallRegister', component: () => import('../views/c-mall/Register.vue'), meta: { title: '商城注册', icon: '📝' } },
  { path: '/mall/product/:id', name: 'ProductDetail', component: () => import('../views/c-mall/ProductDetail.vue'), meta: { title: '商品详情', icon: '📦' } },
  { path: '/mall/cart', name: 'ShoppingCart', component: () => import('../views/c-mall/ShoppingCart.vue'), meta: { title: '购物车', icon: '🛒' } },
  { path: '/mall/orders', name: 'MyOrders', component: () => import('../views/c-mall/MyOrders.vue'), meta: { title: '我的订单', icon: '📋' } },
  { path: '/mall/profile', name: 'Profile', component: () => import('../views/c-mall/Profile.vue'), meta: { title: '个人中心', icon: '👤' } },
  { path: '/mall/address', name: 'Address', component: () => import('../views/c-mall/Address.vue'), meta: { title: '收货地址', icon: '📍' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.public) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router