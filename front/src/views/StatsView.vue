<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/api/axios'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart, GaugeChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, GridComponent, LegendComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, PieChart, BarChart, LineChart, GaugeChart,
     TitleComponent, TooltipComponent, GridComponent, LegendComponent])

const stateDistribution = ref([])
const dangerRanking = ref([])
const healthScores = ref([])
const sensorTrend = ref([])
const loading = ref(true)

// 장비 선택
const equipmentList = ref([])
const selectedEqForHealth = ref('')
const selectedEqForTrend = ref('OHT-01')
const selectedSensor = ref('pm10')

const sensorOptions = [
  { value: 'pm10', label: 'PM10' },
  { value: 'pm25', label: 'PM2.5' },
  { value: 'ntc_temp', label: 'NTC 온도' },
  { value: 'ct1', label: '전류 CT1' },
  { value: 'ct2', label: '전류 CT2' },
  { value: 'ct3', label: '전류 CT3' },
  { value: 'ct4', label: '전류 CT4' },
  { value: 'ir_temp_max', label: '열화상 최고온도' },
]

const stateLabels = { 0: '정상', 1: '관심', 2: '경고', 3: '위험' }
const stateColors = ['#059669', '#2563eb', '#d97706', '#dc2626']

// -- 차트 옵션 (ref로 관리하여 명시적 갱신) --

const pieOption = ref({})
const barOption = ref({})
const gaugeOption = ref({})
const trendOption = ref({})

function buildPieOption() {
  const data = stateDistribution.value
  if (!data || data.length === 0) return
  pieOption.value = {
    title: { text: '상태 분포', textStyle: { fontSize: 15, fontWeight: 600 } },
    tooltip: { trigger: 'item', formatter: '{b}: {c}건 ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: data.map(d => ({
        name: stateLabels[d.state] ?? `state ${d.state}`,
        value: Number(d.count),
        itemStyle: { color: stateColors[d.state] || '#999' },
      })),
    }],
  }
}

function buildBarOption() {
  const data = dangerRanking.value
  if (!data || data.length === 0) return
  const reversed = [...data].reverse()
  barOption.value = {
    title: { text: '위험 비율 TOP 10', textStyle: { fontSize: 15, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 80, right: 30, bottom: 30, top: 40 },
    xAxis: { type: 'value', name: '%', axisLabel: { formatter: '{value}%' } },
    yAxis: {
      type: 'category',
      data: reversed.map(d => d.eqId),
      axisLabel: { fontSize: 12 },
    },
    series: [{
      type: 'bar',
      data: reversed.map(d => ({
        value: Number(d.dangerRatio),
        itemStyle: {
          color: d.dangerRatio >= 5 ? '#dc2626' : d.dangerRatio >= 3 ? '#d97706' : '#2563eb',
        },
      })),
      barWidth: 18,
      label: { show: true, position: 'right', formatter: '{c}%', fontSize: 11 },
    }],
  }
}

const currentHealthScore = computed(() => {
  const scores = healthScores.value
  if (!scores || scores.length === 0) return null
  if (selectedEqForHealth.value) {
    return scores.find(h => h.eqId === selectedEqForHealth.value) || scores[0]
  }
  return scores[0]
})

function buildGaugeOption() {
  const hs = currentHealthScore.value
  const score = hs ? Number(hs.score) : 0
  gaugeOption.value = {
    series: [{
      type: 'gauge',
      startAngle: 200,
      endAngle: -20,
      min: 0,
      max: 100,
      progress: { show: true, width: 16 },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 16, color: [[0.4, '#dc2626'], [0.7, '#d97706'], [1, '#059669']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: {
        valueAnimation: true,
        formatter: '{value}점',
        fontSize: 28,
        fontWeight: 700,
        offsetCenter: [0, '30%'],
      },
      title: {
        show: true,
        offsetCenter: [0, '55%'],
        fontSize: 13,
        color: '#6b7280',
      },
      data: [{ value: score, name: hs?.eqId || '' }],
    }],
  }
}

function buildTrendOption() {
  const data = sensorTrend.value
  if (!data || data.length === 0) return
  trendOption.value = {
    title: { text: `센서 추이 (${selectedEqForTrend.value})`, textStyle: { fontSize: 15, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    grid: { top: 40, right: 20, bottom: 36, left: 50 },
    xAxis: {
      type: 'category',
      data: data.map(d => `#${d.hour}`),
      axisLabel: { fontSize: 11 },
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '평균', type: 'line', data: data.map(d => d.avgValue),
        smooth: true, symbol: 'circle', symbolSize: 5, lineStyle: { width: 2 },
        itemStyle: { color: '#2563eb' },
      },
      {
        name: '최대', type: 'line', data: data.map(d => d.maxValue),
        smooth: true, symbol: 'circle', symbolSize: 5, lineStyle: { width: 2, type: 'dashed' },
        itemStyle: { color: '#dc2626' },
      },
    ],
    legend: { bottom: 0 },
  }
}

// -- 데이터 로딩 --

async function fetchCoreStats() {
  loading.value = true
  try {
    const [dist, ranking, health, eqList] = await Promise.all([
      api.get('/api/v1/stats/state-distribution'),
      api.get('/api/v1/stats/danger-ranking?limit=10'),
      api.get('/api/v1/stats/health-score'),
      api.get('/api/v1/equipment'),
    ])
    stateDistribution.value = dist || []
    dangerRanking.value = ranking || []
    healthScores.value = health || []
    equipmentList.value = eqList || []

    // 첫 번째 장비를 건강 점수 기본 선택으로
    if (eqList && eqList.length > 0 && !selectedEqForHealth.value) {
      selectedEqForHealth.value = eqList[0].eqId
    }

    buildPieOption()
    buildBarOption()
    buildGaugeOption()
  } catch (e) {
    console.error('통계 데이터 로드 실패', e)
  } finally {
    loading.value = false
  }
}

async function fetchTrend() {
  try {
    const data = await api.get('/api/v1/stats/sensor-trend', {
      params: { eqId: selectedEqForTrend.value, sensor: selectedSensor.value },
    })
    sensorTrend.value = data || []
    buildTrendOption()
  } catch (e) {
    console.error('센서 추이 로드 실패', e)
  }
}

async function downloadExcel() {
  try {
    const token = localStorage.getItem('token')
    const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const res = await fetch(`${base}/api/v1/stats/export`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `OHT_Report_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 다운로드 실패', e)
  }
}

watch(selectedEqForHealth, () => buildGaugeOption())
watch([selectedEqForTrend, selectedSensor], fetchTrend)

onMounted(async () => {
  await fetchCoreStats()
  await fetchTrend()
})
</script>

<template>
  <div>
    <div class="page-header">
      <div style="display:flex; justify-content:space-between; align-items:center">
        <div>
          <h1>통계 & 리포트</h1>
          <p>장비 센서 데이터 분석 및 건강 점수</p>
        </div>
        <button class="btn btn-primary" @click="downloadExcel">📥 엑셀 다운로드</button>
      </div>
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <template v-else>
      <!-- 1행: 도넛 + 바 차트 -->
      <div class="chart-row">
        <div class="card chart-card">
          <VChart v-if="pieOption.series" :option="pieOption" autoresize style="height: 320px" />
          <div v-else class="loading">상태 분포 데이터 없음</div>
        </div>
        <div class="card chart-card">
          <VChart v-if="barOption.series" :option="barOption" autoresize style="height: 320px" />
          <div v-else class="loading">위험 랭킹 데이터 없음</div>
        </div>
      </div>

      <!-- 2행: 건강 점수 게이지 + 센서 추이 -->
      <div class="chart-row">
        <div class="card chart-card">
          <div class="chart-header">
            <h3>건강 점수</h3>
            <select v-model="selectedEqForHealth" class="chart-select">
              <option v-for="eq in equipmentList" :key="eq.eqId" :value="eq.eqId">
                {{ eq.eqId }}
              </option>
            </select>
          </div>
          <VChart v-if="gaugeOption.series" :option="gaugeOption" autoresize style="height: 260px" />
          <div v-if="currentHealthScore" class="deviation-grid">
            <div class="deviation-item">
              <span class="dev-label">PM10 이탈</span>
              <span class="dev-value">{{ currentHealthScore.pm10Deviation }}%</span>
            </div>
            <div class="deviation-item">
              <span class="dev-label">NTC 이탈</span>
              <span class="dev-value">{{ currentHealthScore.ntcDeviation }}%</span>
            </div>
            <div class="deviation-item">
              <span class="dev-label">CT 이탈</span>
              <span class="dev-value">{{ currentHealthScore.ctDeviation }}%</span>
            </div>
            <div class="deviation-item">
              <span class="dev-label">IR 이탈</span>
              <span class="dev-value">{{ currentHealthScore.irDeviation }}%</span>
            </div>
          </div>
        </div>

        <div class="card chart-card">
          <div class="chart-header">
            <h3>센서 추이</h3>
            <div class="chart-filters">
              <select v-model="selectedEqForTrend" class="chart-select">
                <option v-for="eq in equipmentList" :key="eq.eqId" :value="eq.eqId">
                  {{ eq.eqId }}
                </option>
              </select>
              <select v-model="selectedSensor" class="chart-select">
                <option v-for="opt in sensorOptions" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </option>
              </select>
            </div>
          </div>
          <VChart v-if="trendOption.series" :option="trendOption" autoresize style="height: 320px" />
          <div v-else class="loading">센서 추이 데이터 없음</div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  padding: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chart-header h3 {
  font-size: 15px;
  font-weight: 600;
}

.chart-filters {
  display: flex;
  gap: 8px;
}

.chart-select {
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  background: var(--color-surface);
}

.chart-select:focus {
  border-color: var(--color-primary);
}

.deviation-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.deviation-item {
  text-align: center;
}

.dev-label {
  display: block;
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-bottom: 2px;
}

.dev-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
}
</style>
