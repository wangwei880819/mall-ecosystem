import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '风控看板' }
  },
  {
    path: '/events',
    name: 'Events',
    component: () => import('../views/event/EventList.vue'),
    meta: { title: '风控事件' }
  },
  {
    path: '/rules',
    name: 'Rules',
    component: () => import('../views/rule/RuleList.vue'),
    meta: { title: '规则管理' }
  },
  {
    path: '/blacklist',
    name: 'Blacklist',
    component: () => import('../views/blacklist/BlackList.vue'),
    meta: { title: '名单库' }
  },
  {
    path: '/disposals',
    name: 'Disposals',
    component: () => import('../views/disposal/DisposalList.vue'),
    meta: { title: '处置管理' }
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('../views/analysis/Analysis.vue'),
    meta: { title: '数据分析' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
