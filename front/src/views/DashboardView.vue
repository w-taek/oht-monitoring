<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import StatusBadge from '@/components/StatusBadge.vue'
import { useWebSocket } from '@/composables/useWebSocket'

const router = useRouter()
const loading = ref(true)

// 장비별 실시간 센서 상태: { eqId → state(0~3) }
const liveStateMap = ref({})
// 장비 목록 (REST로 1회 로드)
const equipmentList = ref([])
// 최근 알림
const recentAlerts = ref([])

// 필터: null=전체, 0=정상, 1=관심, 2=경고, 3=위험
const activeFilter = ref(null)

const { connect, subscribe, disconnect } = useWebSocket()

const stateLabels = ['정상', '관심', '경고', '위험']
const stateColors = ['var(--color-normal)', 'var(--color-caution)', 'var(--color-warning)', 'var(--color-danger)']

// 장비별 현재 상태를 liveStateMap에서 가져오기 (없으면 0)
function getEqState(eqId) {
  return liveStateMap.value[eqId] ?? 0
}

// 상태별 카운트 (liveStateMap 기반 → 실시간 반영)
const statusCards = computed(() => {
  const counts = [0, 0, 0, 0]
  for (const eq of equipmentList.value) {
    const state = getEqState(eq.eqId)
    counts[state] = (counts[state] || 0) + 1
  }
  return [
    { label: '정상', count: counts[0], state: 0, color: stateColors[0], bg: '#ecfdf5' },
    { label: '관심', count: counts[1], state: 1, color: stateColors[1], bg: '#eff6ff' },
    { label: '경고', count: counts[2], state: 2, color: stateColors[2], bg: '#fffbeb' },
    { label: '위험', count: counts[3], state: 3, color: stateColors[3], bg: '#fef2f2' },
  ]
})

// 필터된 장비
const filteredEquipment = computed(() => {
  if (activeFilter.value === null) return equipmentList.value
  return equipmentList.value.filter((eq) => getEqState(eq.eqId) === activeFilter.value)
})

// 필터된 알림 (필터 연동)
const levelToState = { CAUTION: 1, WARNING: 2, DANGER: 3 }
const stateToLevels = { 1: ['CAUTION'], 2: ['WARNING'], 3: ['DANGER'] }

const filteredAlerts = computed(() => {
  if (activeFilter.value === null) return recentAlerts.value
  const levels = stateToLevels[activeFilter.value]
  if (!levels) {
    // 정상(0) → 알림 없음
    return []
  }
  return recentAlerts.value.filter((a) => levels.includes(a.alertLevel))
})

function toggleFilter(state) {
  activeFilter.value = activeFilter.value === state ? null : state
}

function formatTime(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleTimeString('ko-KR', {
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  })
}

// 초기 데이터 로드
async function fetchData() {
  loading.value = true
  try {
    const [eqList, alerts] = await Promise.all([
      api.get('/api/v1/equipment'),
      api.get('/api/v1/alerts?size=10'),
    ])
    equipmentList.value = eqList || []
    recentAlerts.value = (alerts.content || [])

    // 초기 상태: 모두 정상(0)으로 설정
    const stateMap = {}
    for (const eq of equipmentList.value) {
      stateMap[eq.eqId] = 0
    }
    liveStateMap.value = stateMap
  } catch (e) {
    console.error('대시보드 데이터 로드 실패', e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchData()
  connect()

  // 모든 장비의 센서 데이터 구독 → 상태 실시간 업데이트
  for (const eq of equipmentList.value) {
    subscribe(`/topic/sensor/${eq.eqId}`, (data) => {
      liveStateMap.value[eq.eqId] = data.state ?? 0
    })
  }

  // 알림 구독 → 최근 알림 실시간 추가
  subscribe('/topic/alert', (alert) => {
    recentAlerts.value.unshift({
      id: alert.alertId,
      eqId: alert.eqId,
      alertLevel: alert.alertLevel,
      sensorName: alert.sensorName,
      sensorValue: alert.sensorValue,
      thresholdValue: alert.thresholdValue,
      createdAt: alert.createdAt,
    })
    // 최대 20건 유지
    if (recentAlerts.value.length > 20) {
      recentAlerts.value.pop()
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
      <h1>대시보드</h1>
      <p>전체 장비 상태 및 최근 알림 현황</p>
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <template v-else>
      <!-- 상태 카운트 카드 (클릭으로 필터) -->
      <div class="status-cards">
        <div
          v-for="card in statusCards"
          :key="card.label"
          class="status-card"
          :class="{ active: activeFilter === card.state }"
          :style="{ borderTopColor: card.color }"
          @click="toggleFilter(card.state)"
        >
          <div class="status-card-count" :style="{ color: card.color }">
            {{ card.count }}
          </div>
          <div class="status-card-label">{{ card.label }}</div>
        </div>
      </div>

      <!-- 장비 그리드 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">
            장비 현황
            <span class="section-count">({{ filteredEquipment.length }}대)</span>
          </h2>
          <div class="filter-chips">
            <button
              class="filter-chip"
              :class="{ active: activeFilter === null }"
              @click="activeFilter = null"
            >전체</button>
            <button
              v-for="card in statusCards"
              :key="card.state"
              class="filter-chip"
              :class="{ active: activeFilter === card.state }"
              :style="activeFilter === card.state ? { background: card.color, color: '#fff', borderColor: card.color } : {}"
              @click="toggleFilter(card.state)"
            >{{ card.label }} ({{ card.count }})</button>
          </div>
        </div>
        <div class="equipment-grid">
          <div
            v-for="eq in filteredEquipment"
            :key="eq.eqId"
            class="eq-card card"
            @click="router.push(`/dashboard/${eq.eqId}`)"
          >
            <div class="eq-card-header">
              <span class="eq-id">{{ eq.eqId }}</span>
              <StatusBadge :state="getEqState(eq.eqId)" :label="stateLabels[getEqState(eq.eqId)]" />
            </div>
            <div class="eq-card-name">{{ eq.eqName }}</div>
            <div class="eq-card-meta">
              <span>{{ eq.eqType }}</span>
              <span>{{ eq.location }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 최근 알림 (필터 연동) -->
      <section class="section">
        <h2 class="section-title">최근 알림</h2>
        <div class="card">
          <table class="data-table" v-if="filteredAlerts.length">
            <thead>
              <tr>
                <th>시간</th>
                <th>장비</th>
                <th>등급</th>
                <th>센서</th>
                <th>값</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="alert in filteredAlerts" :key="alert.id">
                <td>{{ formatTime(alert.createdAt) }}</td>
                <td>{{ alert.eqId }}</td>
                <td>
                  <StatusBadge :state="levelToState[alert.alertLevel] || 0" :label="alert.alertLevel" />
                </td>
                <td>{{ alert.sensorName }}</td>
                <td>{{ alert.sensorValue?.toFixed(1) }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="loading">
            {{ activeFilter !== null ? '해당 등급의 알림이 없습니다' : '알림이 없습니다' }}
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.status-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}

.status-card {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 20px;
  border-top: 3px solid;
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}

.status-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.status-card.active {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
  outline: 2px solid currentColor;
  outline-offset: -2px;
}

.status-card-count {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.status-card-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: 6px;
  font-weight: 500;
}

.section {
  margin-bottom: 28px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
}

.section-count {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  margin-left: 4px;
}

.filter-chips {
  display: flex;
  gap: 6px;
}

.filter-chip {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.filter-chip:hover {
  border-color: var(--color-text-secondary);
}

.filter-chip.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.eq-card {
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
  padding: 16px;
}

.eq-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.eq-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.eq-id {
  font-weight: 700;
  font-size: 15px;
  color: var(--color-primary);
}

.eq-card-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
}

.eq-card-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
