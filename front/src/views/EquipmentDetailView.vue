<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'
import StatusBadge from '@/components/StatusBadge.vue'
import { useWebSocket } from '@/composables/useWebSocket'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])

const route = useRoute()
const router = useRouter()
const eqId = route.params.eqId

const equipment = ref(null)
const loading = ref(true)
const { connect, subscribe, disconnect } = useWebSocket()

const MAX_POINTS = 30

// 차트 데이터 (시간라벨 + 센서값 배열)
const timeLabels = ref([])
const pm10Data = ref([])
const pm25Data = ref([])
const ntcTempData = ref([])
const ct1Data = ref([])
const ct2Data = ref([])
const ct3Data = ref([])
const ct4Data = ref([])
const irTempMaxData = ref([])

const statusMap = { RUNNING: 0, STOPPED: 1, MAINTENANCE: 2 }

function pushData(sensorData) {
  const now = new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  timeLabels.value.push(now)
  pm10Data.value.push(sensorData.pm10)
  pm25Data.value.push(sensorData.pm25)
  ntcTempData.value.push(sensorData.ntcTemp)
  ct1Data.value.push(sensorData.ct1)
  ct2Data.value.push(sensorData.ct2)
  ct3Data.value.push(sensorData.ct3)
  ct4Data.value.push(sensorData.ct4)
  irTempMaxData.value.push(sensorData.irTempMax)

  // 최대 포인트 유지
  if (timeLabels.value.length > MAX_POINTS) {
    timeLabels.value.shift()
    pm10Data.value.shift()
    pm25Data.value.shift()
    ntcTempData.value.shift()
    ct1Data.value.shift()
    ct2Data.value.shift()
    ct3Data.value.shift()
    ct4Data.value.shift()
    irTempMaxData.value.shift()
  }
}

function makeOption(title, series) {
  return computed(() => ({
    title: { text: title, textStyle: { fontSize: 14, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    grid: { top: 40, right: 20, bottom: 36, left: 50 },
    xAxis: {
      type: 'category',
      data: timeLabels.value,
      axisLabel: { fontSize: 10, rotate: 30 },
    },
    yAxis: { type: 'value' },
    series: series.value,
    animation: false,
  }))
}

const dustOption = makeOption('먼지 (PM10 / PM2.5)', computed(() => [
  { name: 'PM10', type: 'line', data: pm10Data.value, smooth: true, symbol: 'none', lineStyle: { width: 2 } },
  { name: 'PM2.5', type: 'line', data: pm25Data.value, smooth: true, symbol: 'none', lineStyle: { width: 2 } },
]))

const tempOption = makeOption('온도 (NTC)', computed(() => [
  { name: 'NTC Temp', type: 'line', data: ntcTempData.value, smooth: true, symbol: 'none', lineStyle: { width: 2 }, itemStyle: { color: '#dc2626' } },
]))

const currentOption = makeOption('전류 (CT1~CT4)', computed(() => [
  { name: 'CT1', type: 'line', data: ct1Data.value, smooth: true, symbol: 'none', lineStyle: { width: 1.5 } },
  { name: 'CT2', type: 'line', data: ct2Data.value, smooth: true, symbol: 'none', lineStyle: { width: 1.5 } },
  { name: 'CT3', type: 'line', data: ct3Data.value, smooth: true, symbol: 'none', lineStyle: { width: 1.5 } },
  { name: 'CT4', type: 'line', data: ct4Data.value, smooth: true, symbol: 'none', lineStyle: { width: 1.5 } },
]))

const irOption = makeOption('열화상 (IR Max)', computed(() => [
  { name: 'IR Max', type: 'line', data: irTempMaxData.value, smooth: true, symbol: 'none', lineStyle: { width: 2 }, itemStyle: { color: '#d97706' } },
]))

async function fetchEquipment() {
  loading.value = true
  try {
    equipment.value = await api.get(`/api/v1/equipment/${eqId}`)
  } catch (e) {
    console.error('장비 정보 로드 실패', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchEquipment()
  connect()
  subscribe(`/topic/sensor/${eqId}`, (data) => {
    pushData(data)
  })
})

onUnmounted(() => {
  disconnect()
})
</script>

<template>
  <div>
    <div class="page-header">
      <button class="btn btn-outline" @click="router.push('/dashboard')">← 대시보드</button>
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <template v-else-if="equipment">
      <!-- 장비 정보 헤더 -->
      <div class="eq-header card">
        <div class="eq-header-main">
          <h1 class="eq-title">{{ equipment.eqId }} - {{ equipment.eqName }}</h1>
          <StatusBadge :state="statusMap[equipment.status] || 0" :label="equipment.status" />
        </div>
        <div class="eq-meta">
          <span>유형: <strong>{{ equipment.eqType }}</strong></span>
          <span>제조사: <strong>{{ equipment.manufacturer }}</strong></span>
          <span>위치: <strong>{{ equipment.location }}</strong></span>
          <span>설치일: <strong>{{ equipment.installDate }}</strong></span>
        </div>
      </div>

      <!-- 실시간 차트 4개 -->
      <div class="chart-grid">
        <div class="card chart-card">
          <VChart :option="dustOption" autoresize style="height: 280px" />
        </div>
        <div class="card chart-card">
          <VChart :option="tempOption" autoresize style="height: 280px" />
        </div>
        <div class="card chart-card">
          <VChart :option="currentOption" autoresize style="height: 280px" />
        </div>
        <div class="card chart-card">
          <VChart :option="irOption" autoresize style="height: 280px" />
        </div>
      </div>

      <div v-if="timeLabels.length === 0" class="loading mt-16">
        WebSocket 연결 중... 실시간 데이터를 기다리고 있습니다.
      </div>
    </template>
  </div>
</template>

<style scoped>
.eq-header {
  margin-bottom: 20px;
}

.eq-header-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.eq-title {
  font-size: 22px;
  font-weight: 700;
}

.eq-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.eq-meta strong {
  color: var(--color-text);
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
  padding: 16px;
}
</style>
