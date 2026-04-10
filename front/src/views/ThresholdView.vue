<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/api/axios'

const thresholds = ref([])
const loading = ref(true)
const activeTab = ref('OHT')
const editingId = ref(null)
const editForm = ref({ cautionValue: 0, warningValue: 0, dangerValue: 0 })
const saving = ref(false)

const filteredThresholds = computed(() =>
  thresholds.value.filter((t) => t.eqType === activeTab.value)
)

const sensorLabels = {
  pm10: 'PM10 (먼지)',
  pm25: 'PM2.5 (미세먼지)',
  ntc_temp: 'NTC 온도',
  ct1: '전류 CT1',
  ct2: '전류 CT2',
  ct3: '전류 CT3',
  ct4: '전류 CT4',
  ir_temp_max: '열화상 최고온도',
}

const sensorUnits = {
  pm10: 'ug/m3',
  pm25: 'ug/m3',
  ntc_temp: 'C',
  ct1: 'A',
  ct2: 'A',
  ct3: 'A',
  ct4: 'A',
  ir_temp_max: 'C',
}

async function fetchThresholds() {
  loading.value = true
  try {
    thresholds.value = await api.get('/api/v1/threshold')
  } catch (e) {
    console.error('임계값 로드 실패', e)
  } finally {
    loading.value = false
  }
}

function startEdit(row) {
  editingId.value = row.id
  editForm.value = {
    cautionValue: row.cautionValue,
    warningValue: row.warningValue,
    dangerValue: row.dangerValue,
  }
}

function cancelEdit() {
  editingId.value = null
}

async function saveEdit(id) {
  if (editForm.value.cautionValue >= editForm.value.warningValue ||
      editForm.value.warningValue >= editForm.value.dangerValue) {
    alert('임계값은 관심 < 경고 < 위험 순서여야 합니다')
    return
  }

  saving.value = true
  try {
    await api.put(`/api/v1/threshold/${id}`, editForm.value)
    editingId.value = null
    await fetchThresholds()
  } catch (e) {
    alert(e.message || '수정 실패')
  } finally {
    saving.value = false
  }
}

onMounted(fetchThresholds)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>임계값 설정</h1>
      <p>센서별 이상 감지 임계값 관리</p>
    </div>

    <!-- 탭 -->
    <div class="tabs">
      <button
        v-for="tab in ['OHT', 'AGV']"
        :key="tab"
        class="tab-btn"
        :class="{ active: activeTab === tab }"
        @click="activeTab = tab"
      >
        {{ tab }}
      </button>
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <div v-else class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>센서</th>
            <th>단위</th>
            <th class="th-value">관심 (CAUTION)</th>
            <th class="th-value">경고 (WARNING)</th>
            <th class="th-value">위험 (DANGER)</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in filteredThresholds" :key="row.id">
            <td>
              <strong>{{ sensorLabels[row.sensorName] || row.sensorName }}</strong>
            </td>
            <td class="unit">{{ sensorUnits[row.sensorName] || '-' }}</td>

            <template v-if="editingId === row.id">
              <td>
                <input
                  v-model.number="editForm.cautionValue"
                  type="number"
                  step="0.1"
                  class="inline-input caution"
                />
              </td>
              <td>
                <input
                  v-model.number="editForm.warningValue"
                  type="number"
                  step="0.1"
                  class="inline-input warning"
                />
              </td>
              <td>
                <input
                  v-model.number="editForm.dangerValue"
                  type="number"
                  step="0.1"
                  class="inline-input danger"
                />
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn btn-sm btn-primary" :disabled="saving" @click="saveEdit(row.id)">
                    저장
                  </button>
                  <button class="btn btn-sm btn-outline" @click="cancelEdit">취소</button>
                </div>
              </td>
            </template>

            <template v-else>
              <td>
                <span class="value-chip caution">{{ row.cautionValue }}</span>
              </td>
              <td>
                <span class="value-chip warning">{{ row.warningValue }}</span>
              </td>
              <td>
                <span class="value-chip danger">{{ row.dangerValue }}</span>
              </td>
              <td>
                <button class="btn btn-sm btn-outline" @click="startEdit(row)">편집</button>
              </td>
            </template>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
}

.tab-btn {
  padding: 8px 20px;
  border-radius: 8px 8px 0 0;
  font-size: 14px;
  font-weight: 600;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-bottom: none;
  transition: all 0.15s;
}

.tab-btn.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.th-value {
  text-align: center;
}

.unit {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.value-chip {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
}

.value-chip.caution {
  background: #eff6ff;
  color: var(--color-caution);
}

.value-chip.warning {
  background: #fffbeb;
  color: var(--color-warning);
}

.value-chip.danger {
  background: #fef2f2;
  color: var(--color-danger);
}

.inline-input {
  width: 90px;
  padding: 6px 10px;
  border: 2px solid var(--color-border);
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  outline: none;
}

.inline-input:focus {
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
}

.inline-input.caution:focus {
  border-color: var(--color-caution);
}

.inline-input.warning:focus {
  border-color: var(--color-warning);
}

.inline-input.danger:focus {
  border-color: var(--color-danger);
}

.action-btns {
  display: flex;
  gap: 4px;
}
</style>
