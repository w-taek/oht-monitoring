<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const navItems = [
  { path: '/dashboard', label: '대시보드', icon: '📊' },
  { path: '/equipment', label: '장비 목록', icon: '🏭' },
  { path: '/alerts', label: '알림 이력', icon: '🔔' },
  { path: '/maintenance', label: '정비 오더', icon: '🔧' },
  { path: '/stats', label: '통계', icon: '📈' },
  { path: '/admin/thresholds', label: '임계값 설정', icon: '⚙️' },
]

const userName = computed(() => localStorage.getItem('userName') || '')
const userRole = computed(() => localStorage.getItem('userRole') || '')

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userName')
  localStorage.removeItem('userRole')
  router.push('/login')
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h2 class="logo">OHT Monitor</h2>
      <span class="logo-sub">예지보전 시스템</span>
    </div>
    <nav class="sidebar-nav">
      <RouterLink
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-label">{{ item.label }}</span>
      </RouterLink>
    </nav>
    <div class="sidebar-footer" v-if="userName">
      <div class="user-info">
        <div class="user-name">{{ userName }}</div>
        <div class="user-role">{{ userRole }}</div>
      </div>
      <button class="logout-btn" @click="logout">로그아웃</button>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--sidebar-width);
  background: #1e293b;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.sidebar-header {
  padding: 24px 20px 20px;
  border-bottom: 1px solid #334155;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.logo-sub {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
  display: block;
}

.sidebar-nav {
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 14px;
  color: #cbd5e1;
  transition: all 0.15s;
}

.nav-item:hover {
  background: #334155;
  color: #fff;
}

.nav-item.active {
  background: var(--color-primary);
  color: #fff;
}

.nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #334155;
}

.user-info {
  margin-bottom: 10px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.user-role {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}

.logout-btn {
  width: 100%;
  padding: 8px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #cbd5e1;
  background: #334155;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.logout-btn:hover {
  background: #475569;
  color: #fff;
}
</style>
