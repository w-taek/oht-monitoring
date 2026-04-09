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
        @click="removeToast(toast.id)"
      >
        <div class="toast-header">
          <span
            class="toast-level"
            :style="{ color: getColors(toast.alertLevel).text }"
          >
            {{ toast.alertLevel }}
          </span>
          <span class="toast-eq">{{ toast.eqId }}</span>
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
.toast-item {
  min-width: 280px;
  padding: 12px 16px;
  border-radius: 8px;
  border-left: 4px solid;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  cursor: pointer;
}

.toast-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.toast-level {
  font-weight: 700;
  font-size: 13px;
}

.toast-eq {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-weight: 600;
}

.toast-body {
  font-size: 13px;
  color: var(--color-text);
}

/* Transition */
.toast-enter-active {
  transition: all 0.3s ease;
}
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(100px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(100px);
}
</style>
