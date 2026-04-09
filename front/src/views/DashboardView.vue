<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import StatusBadge from '@/components/StatusBadge.vue'

const router = useRouter()
const summary = ref({ normal: 0, caution: 0, warning: 0, danger: 0 })
const equipmentList = ref([])
const recentAlerts = ref([])
const loading = ref(true)

const statusCards = computed(() => [
  { label: '정상', count: summary.value.normal, state: 0, color: 'var(--color-normal)', bg: '#ecfdf5' },
  { label: '관심', count: summary.value.caution, state: 1, color: 'var(--color-caution)', bg: '#eff6ff' },
  { label: '경고', count: summary.value.warning, state: 2, color: 'var(--color-warning)', bg: '#fffbeb' },
  { label: '위험', count: summary.value.danger, state: 3, color: 'var(--color-danger)', bg: '#fef2f2' },
])

const statusMap = {
  RUNNING: 0,
  STOPPED: 1,
  MAINTENANCE: 2,
}

function getEquipmentState(status) {
  return statusMap[status] ?? 0
}

const levelMap = {
  CAUTION: 1,
  WARNING: 2,
  DANGER: 3,
}

function formatTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

async function fetchData() {
  loading.value = true
  try {
    const [sum, eqList, alerts] = await Promise.all([
      api.get('/api/v1/sensor/summary'),
      api.get('/api/v1/equipment'),
      api.get('/api/v1/alerts?size=10'),
    ])
    summary.value = sum
    equipmentList.value = eqList
    recentAlerts.value = alerts.content || []
  } catch (e) {
    console.error('대시보드 데이터 로드 실패', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>대시보드</h1>
      <p>전체 장비 상태 및 최근 알림 현황</p>
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <template v-else>
      <!-- 상태 카운트 카드 -->
      <div class="status-cards">
        <div
          v-for="card in statusCards"
          :key="card.label"
          class="status-card"
          :style="{ borderTopColor: card.color }"
        >
          <div class="status-card-count" :style="{ color: card.color }">
            {{ card.count }}
          </div>
          <div class="status-card-label">{{ card.label }}</div>
        </div>
      </div>

      <!-- 장비 그리드 -->
      <section class="section">
        <h2 class="section-title">장비 현황</h2>
        <div class="equipment-grid">
          <div
            v-for="eq in equipmentList"
            :key="eq.eqId"
            class="eq-card card"
            @click="router.push(`/dashboard/${eq.eqId}`)"
          >
            <div class="eq-card-header">
              <span class="eq-id">{{ eq.eqId }}</span>
              <StatusBadge :state="getEquipmentState(eq.status)" :label="eq.status" />
            </div>
            <div class="eq-card-name">{{ eq.eqName }}</div>
            <div class="eq-card-meta">
              <span>{{ eq.eqType }}</span>
              <span>{{ eq.location }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 최근 알림 -->
      <section class="section">
        <h2 class="section-title">최근 알림</h2>
        <div class="card">
          <table class="data-table" v-if="recentAlerts.length">
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
              <tr v-for="alert in recentAlerts" :key="alert.id">
                <td>{{ formatTime(alert.createdAt) }}</td>
                <td>{{ alert.eqId }}</td>
                <td>
                  <StatusBadge :state="levelMap[alert.alertLevel] || 0" :label="alert.alertLevel" />
                </td>
                <td>{{ alert.sensorName }}</td>
                <td>{{ alert.sensorValue?.toFixed(1) }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="loading">알림이 없습니다</div>
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

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
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
