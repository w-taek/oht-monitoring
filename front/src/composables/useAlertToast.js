import { reactive } from 'vue'

const toasts = reactive([])
let idCounter = 0

const MAX_TOASTS = 3
const AUTO_CLOSE_MS = 5000

export function useAlertToast() {
  function addToast(alert) {
    const id = ++idCounter
    toasts.push({
      id,
      eqId: alert.eqId,
      alertLevel: alert.alertLevel,
      sensorName: alert.sensorName,
      sensorValue: alert.sensorValue,
      thresholdValue: alert.thresholdValue,
    })

    // 최대 동시 3개 유지 — 초과 시 가장 오래된 것 제거
    while (toasts.length > MAX_TOASTS) {
      toasts.shift()
    }

    // 5초 후 자동 제거
    setTimeout(() => {
      removeToast(id)
    }, AUTO_CLOSE_MS)
  }

  function removeToast(id) {
    const idx = toasts.findIndex((t) => t.id === id)
    if (idx !== -1) toasts.splice(idx, 1)
  }

  return { toasts, addToast, removeToast }
}
