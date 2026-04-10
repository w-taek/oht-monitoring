<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { path: '/dashboard', label: '대시보드', icon: '📊' },
  { path: '/equipment', label: '장비 목록', icon: '🏭' },
  { path: '/alerts', label: '알림 이력', icon: '🔔' },
  { path: '/maintenance', label: '정비 오더', icon: '🔧' },
]

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
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
</style>
