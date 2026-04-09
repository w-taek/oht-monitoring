import { reactive } from 'vue'

const toasts = reactive([])
let idCounter = 0

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

    // 3초 후 자동 제거
    setTimeout(() => {
      removeToast(id)
    }, 3000)
  }

  function removeToast(id) {
    const idx = toasts.findIndex((t) => t.id === id)
    if (idx !== -1) toasts.splice(idx, 1)
  }

  return { toasts, addToast, removeToast }
}
