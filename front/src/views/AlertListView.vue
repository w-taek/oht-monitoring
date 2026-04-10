<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import StatusBadge from '@/components/StatusBadge.vue'
import { useWebSocket } from '@/composables/useWebSocket'

const router = useRouter()

const alerts = ref([])
const loading = ref(true)
const page = ref(0)
const size = 20
const totalPages = ref(0)
const totalElements = ref(0)

const filterLevel = ref('')
const filterEqId = ref('')

const levelMap = { CAUTION: 1, WARNING: 2, DANGER: 3 }

const { connect, subscribe, disconnect } = useWebSocket()

function formatTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

async function fetchAlerts() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (filterLevel.value) params.level = filterLevel.value
    if (filterEqId.value) params.eqId = filterEqId.value
    const data = await api.get('/api/v1/alerts', { params })
    alerts.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e) {
    console.error('알림 목록 로드 실패', e)
  } finally {
    loading.value = false
  }
}

async function acknowledge(id) {
  try {
    await api.patch(`/api/v1/alerts/${id}/acknowledge`)
    // 해당 알림의 acknowledged 상태 갱신
    const alert = alerts.value.find((a) => a.id === id)
    if (alert) {
      alert.acknowledged = true
      alert.acknowledgedAt = new Date().toISOString()
    }
  } catch (e) {
    console.error('알림 확인 실패', e)
  }
}

function createOrderFromAlert(alert) {
  router.push({
    path: '/maintenance',
    query: {
      alertEventId: alert.id,
      eqId: alert.eqId,
      alertLevel: alert.alertLevel,
      sensorName: alert.sensorName,
    },
  })
}

function prevPage() {
  if (page.value > 0) {
    page.value--
    fetchAlerts()
  }
}

function nextPage() {
  if (page.value < totalPages.value - 1) {
    page.value++
    fetchAlerts()
  }
}

watch([filterLevel, filterEqId], () => {
  page.value = 0
  fetchAlerts()
})

onMounted(() => {
  fetchAlerts()
  connect()
  subscribe('/topic/alert', (newAlert) => {
    // 새 알림을 목록 상단에 추가
    alerts.value.unshift({
      id: newAlert.alertId,
      eqId: newAlert.eqId,
      alertLevel: newAlert.alertLevel,
      sensorName: newAlert.sensorName,
      sensorValue: newAlert.sensorValue,
      thresholdValue: newAlert.thresholdValue,
      acknowledged: false,
      acknowledgedAt: null,
      createdAt: newAlert.createdAt,
    })
    totalElements.value++
    // 페이지 사이즈 초과 시 마지막 항목 제거
    if (alerts.value.length > size) {
      alerts.value.pop()
    }
  })
})

onUnmounted(() => {
  disconnect()
})
</script>

<template>
  <div>
    <div class="page-header">
      <h1>알림 이력</h1>
      <p>총 {{ totalElements }}건의 알림</p>
    </div>

    <div class="filter-bar">
      <select v-model="filterLevel">
        <option value="">전체 등급</option>
        <option value="CAUTION">CAUTION</option>
        <option value="WARNING">WARNING</option>
        <option value="DANGER">DANGER</option>
      </select>
      <input
        v-model="filterEqId"
        type="text"
        placeholder="장비 ID (예: OHT-01)"
        class="filter-input"
      />
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <div v-else class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>시간</th>
            <th>장비</th>
            <th>등급</th>
            <th>센서</th>
            <th>값</th>
            <th>임계값</th>
            <th>확인</th>
            <th>조치</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="alert in alerts" :key="alert.id">
            <td>{{ formatTime(alert.createdAt) }}</td>
            <td><strong>{{ alert.eqId }}</strong></td>
            <td>
              <StatusBadge
                :state="levelMap[alert.alertLevel] || 0"
                :label="alert.alertLevel"
              />
            </td>
            <td>{{ alert.sensorName }}</td>
            <td>{{ alert.sensorValue?.toFixed(1) }}</td>
            <td>{{ alert.thresholdValue }}</td>
            <td>
              <button
                v-if="!alert.acknowledged"
                class="btn btn-sm btn-primary"
                @click="acknowledge(alert.id)"
              >
                확인
              </button>
              <span v-else class="ack-done">확인됨</span>
            </td>
            <td>
              <button
                class="btn btn-sm btn-outline create-order-btn"
                @click="createOrderFromAlert(alert)"
              >
                🔧 정비 오더
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page === 0" @click="prevPage">이전</button>
        <span>{{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="nextPage">다음</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-input {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  width: 180px;
}

.filter-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
}

.ack-done {
  font-size: 12px;
  color: var(--color-normal);
  font-weight: 600;
}

.create-order-btn {
  white-space: nowrap;
  font-size: 11px;
}
</style>
