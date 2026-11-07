import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', meta: { title: '风险看板' }, component: () => import('@/views/Dashboard.vue') },
      { path: 'enterprise', name: 'enterprise', meta: { title: '企业管理' }, component: () => import('@/views/Enterprise.vue') },
      { path: 'transaction', name: 'transaction', meta: { title: '交易明细' }, component: () => import('@/views/Transaction.vue') },
      { path: 'warning', name: 'warning', meta: { title: '预警中心' }, component: () => import('@/views/Warning.vue') },
      { path: 'rule', name: 'rule', meta: { title: '预警规则' }, component: () => import('@/views/Rule.vue') },
      { path: 'risk', name: 'risk', meta: { title: '风险评分' }, component: () => import('@/views/RiskScore.vue') },
      { path: 'hadoop', name: 'hadoop', meta: { title: 'Hadoop作业' }, component: () => import('@/views/Hadoop.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path !== '/login' && !localStorage.getItem('token')) {
    next('/login')
  } else {
    next()
  }
})

export default router
