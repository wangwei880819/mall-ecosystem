import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', tabIndex: 0 }
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('@/views/Category.vue'),
    meta: { title: '全部分类' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
    meta: { title: '购物车', tabIndex: 1 }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '我的', tabIndex: 2 }
  },
  {
    path: '/detail/:id',
    name: 'Detail',
    component: () => import('@/views/Detail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { title: '确认订单' }
  },
  {
    path: '/pay',
    name: 'Pay',
    component: () => import('@/views/Pay.vue'),
    meta: { title: '支付' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/Orders.vue'),
    meta: { title: '我的订单' }
  },
  {
    path: '/address',
    name: 'Address',
    component: () => import('@/views/Address.vue'),
    meta: { title: '收货地址' }
  },
  {
    path: '/address/edit/:id?',
    name: 'AddressEdit',
    component: () => import('@/views/AddressEdit.vue'),
    meta: { title: '编辑地址' }
  },
  {
    path: '/review',
    name: 'Review',
    component: () => import('@/views/Review.vue'),
    meta: { title: '商品评价' }
  }
]

const router = createRouter({
  history: createWebHistory('/mall'),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

export default router
