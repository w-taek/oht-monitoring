import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
  },
  {
    path: '/dashboard/:eqId',
    name: 'EquipmentDetail',
    component: () => import('@/views/EquipmentDetailView.vue'),
    props: true,
  },
  {
    path: '/equipment',
    name: 'Equipment',
    component: () => import('@/views/EquipmentListView.vue'),
  },
  {
    path: '/alerts',
    name: 'Alerts',
    component: () => import('@/views/AlertListView.vue'),
  },
  {
    path: '/maintenance',
    name: 'Maintenance',
    component: () => import('@/views/MaintOrderListView.vue'),
  },
  {
    path: '/maintenance/:id',
    name: 'MaintOrderDetail',
    component: () => import('@/views/MaintOrderDetailView.vue'),
    props: true,
  },
  {
    path: '/admin/thresholds',
    name: 'Thresholds',
    component: () => import('@/views/ThresholdView.vue'),
  },
  {
    path: '/stats',
    name: 'Stats',
    component: () => import('@/views/StatsView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 라우트 가드: 토큰 없으면 /login으로 리다이렉트
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
