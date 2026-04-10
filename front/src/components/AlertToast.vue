<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useAlertToast } from '@/composables/useAlertToast'
import { useWebSocket } from '@/composables/useWebSocket'

const { toasts, addToast, removeToast } = useAlertToast()
const { connect, subscribe, disconnect } = useWebSocket()

const levelColors = {
  CAUTION: { bg: '#eff6ff', border: 'var(--color-caution)', text: 'var(--color-caution)' },
  WARNING: { bg: '#fffbeb', border: 'var(--color-warning)', text: 'var(--color-warning)' },
  DANGER: { bg: '#fef2f2', border: 'var(--color-danger)', text: 'var(--color-danger)' },
}

function getColors(level) {
  return levelColors[level] || levelColors.CAUTION
}

onMounted(() => {
  connect()
  subscribe('/topic/alert', (alert) => {
    addToast(alert)
  })
})

onUnmounted(() => {
  disconnect()
})
</script>

<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast-item"
        :style="{
          background: getColors(toast.alertLevel).bg,
          borderLeftColor: getColors(toast.alertLevel).border,
        }"
      >
        <div class="toast-header">
          <span
            class="toast-level"
            :style="{ color: getColors(toast.alertLevel).text }"
          >
            {{ toast.alertLevel }}
          </span>
          <div class="toast-header-right">
            <span class="toast-eq">{{ toast.eqId }}</span>
            <button class="toast-close" @click="removeToast(toast.id)">&times;</button>
          </div>
        </div>
        <div class="toast-body">
          {{ toast.sensorName }} = {{ toast.sensorValue?.toFixed(1) }}
          (임계값: {{ toast.thresholdValue }})
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column-reverse;
  gap: 8px;
  pointer-events: none;
}

.toast-container > * {
  pointer-events: auto;
}

.toast-item {
  min-width: 300px;
  max-width: 380px;
  padding: 14px 16px;
  border-radius: 10px;
  border-left: 4px solid;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.toast-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.toast-level {
  font-weight: 700;
  font-size: 13px;
}

.toast-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toast-eq {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-weight: 600;
}

.toast-close {
  background: none;
  border: none;
  font-size: 18px;
  line-height: 1;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 0 2px;
  opacity: 0.6;
  transition: opacity 0.15s;
}

.toast-close:hover {
  opacity: 1;
  color: var(--color-text);
}

.toast-body {
  font-size: 13px;
  color: var(--color-text);
}

/* Transition */
.toast-enter-active {
  transition: all 0.35s ease;
}
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(80px) scale(0.95);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(80px) scale(0.95);
}
</style>
