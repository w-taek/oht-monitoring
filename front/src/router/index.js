import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
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
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
