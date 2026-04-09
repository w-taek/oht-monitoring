<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import StatusBadge from '@/components/StatusBadge.vue'

const router = useRouter()
const equipmentList = ref([])
const loading = ref(true)

const filterType = ref('')
const filterStatus = ref('')

const statusMap = { RUNNING: 0, STOPPED: 1, MAINTENANCE: 2 }

async function fetchEquipment() {
  loading.value = true
  try {
    const params = {}
    if (filterType.value) params.type = filterType.value
    if (filterStatus.value) params.status = filterStatus.value
    equipmentList.value = await api.get('/api/v1/equipment', { params })
  } catch (e) {
    console.error('장비 목록 로드 실패', e)
  } finally {
    loading.value = false
  }
}

watch([filterType, filterStatus], fetchEquipment)
onMounted(fetchEquipment)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>장비 목록</h1>
      <p>전체 OHT/AGV 장비 현황</p>
    </div>

    <div class="filter-bar">
      <select v-model="filterType">
        <option value="">전체 유형</option>
        <option value="OHT">OHT</option>
        <option value="AGV">AGV</option>
      </select>
      <select v-model="filterStatus">
        <option value="">전체 상태</option>
        <option value="RUNNING">RUNNING</option>
        <option value="STOPPED">STOPPED</option>
        <option value="MAINTENANCE">MAINTENANCE</option>
      </select>
      <span class="filter-count">총 {{ equipmentList.length }}대</span>
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <div v-else class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>장비 ID</th>
            <th>이름</th>
            <th>유형</th>
            <th>제조사</th>
            <th>위치</th>
            <th>상태</th>
            <th>설치일</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="eq in equipmentList"
            :key="eq.eqId"
            class="clickable"
            @click="router.push(`/dashboard/${eq.eqId}`)"
          >
            <td><strong>{{ eq.eqId }}</strong></td>
            <td>{{ eq.eqName }}</td>
            <td>{{ eq.eqType }}</td>
            <td>{{ eq.manufacturer }}</td>
            <td>{{ eq.location }}</td>
            <td>
              <StatusBadge :state="statusMap[eq.status] || 0" :label="eq.status" />
            </td>
            <td>{{ eq.installDate }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.filter-count {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-left: auto;
}
</style>
