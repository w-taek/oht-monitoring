<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()
const orders = ref([])
const loading = ref(true)
const page = ref(0)
const size = 20
const totalPages = ref(0)
const totalElements = ref(0)

const filterStatus = ref('')
const showCreateModal = ref(false)

// 생성 폼
const createForm = ref({
  eqId: '',
  orderType: 'CORRECTIVE',
  priority: 'MEDIUM',
  title: '',
  description: '',
})

const priorityColors = {
  LOW: '#6b7280',
  MEDIUM: '#2563eb',
  HIGH: '#d97706',
  CRITICAL: '#dc2626',
}

const statusColors = {
  OPEN: { bg: '#fef2f2', color: '#dc2626' },
  ASSIGNED: { bg: '#fffbeb', color: '#d97706' },
  IN_PROGRESS: { bg: '#eff6ff', color: '#2563eb' },
  COMPLETED: { bg: '#ecfdf5', color: '#059669' },
}

function formatTime(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  })
}

async function fetchOrders() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (filterStatus.value) params.status = filterStatus.value
    const data = await api.get('/api/v1/maintenance', { params })
    orders.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e) {
    console.error('정비 오더 목록 로드 실패', e)
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  try {
    await api.post('/api/v1/maintenance', createForm.value)
    showCreateModal.value = false
    createForm.value = { eqId: '', orderType: 'CORRECTIVE', priority: 'MEDIUM', title: '', description: '' }
    page.value = 0
    fetchOrders()
  } catch (e) {
    alert(e.message || '생성 실패')
  }
}

function prevPage() { if (page.value > 0) { page.value--; fetchOrders() } }
function nextPage() { if (page.value < totalPages.value - 1) { page.value++; fetchOrders() } }

watch(filterStatus, () => { page.value = 0; fetchOrders() })
onMounted(fetchOrders)
</script>

<template>
  <div>
    <div class="page-header">
      <div style="display:flex; justify-content:space-between; align-items:center">
        <div>
          <h1>정비 오더</h1>
          <p>총 {{ totalElements }}건</p>
        </div>
        <button class="btn btn-primary" @click="showCreateModal = true">+ 새 오더</button>
      </div>
    </div>

    <div class="filter-bar">
      <select v-model="filterStatus">
        <option value="">전체 상태</option>
        <option value="OPEN">OPEN</option>
        <option value="ASSIGNED">ASSIGNED</option>
        <option value="IN_PROGRESS">IN_PROGRESS</option>
        <option value="COMPLETED">COMPLETED</option>
      </select>
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <div v-else class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>장비</th>
            <th>유형</th>
            <th>우선순위</th>
            <th>제목</th>
            <th>상태</th>
            <th>담당자</th>
            <th>생성일</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="order in orders"
            :key="order.id"
            class="clickable"
            @click="router.push(`/maintenance/${order.id}`)"
          >
            <td>#{{ order.id }}</td>
            <td><strong>{{ order.eqId }}</strong></td>
            <td>{{ order.orderType }}</td>
            <td>
              <span class="priority-badge" :style="{ color: priorityColors[order.priority] }">
                {{ order.priority }}
              </span>
            </td>
            <td>{{ order.title }}</td>
            <td>
              <span
                class="status-chip"
                :style="{
                  background: statusColors[order.status]?.bg,
                  color: statusColors[order.status]?.color,
                }"
              >
                {{ order.status }}
              </span>
            </td>
            <td>{{ order.assignee || '-' }}</td>
            <td>{{ formatTime(order.createdAt) }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="orders.length === 0" class="loading">정비 오더가 없습니다</div>

      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page === 0" @click="prevPage">이전</button>
        <span>{{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="nextPage">다음</button>
      </div>
    </div>

    <!-- 생성 모달 -->
    <Teleport to="body">
      <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
        <div class="modal">
          <h2 class="modal-title">정비 오더 생성</h2>
          <form @submit.prevent="submitCreate" class="modal-form">
            <label>
              장비 ID *
              <input v-model="createForm.eqId" required placeholder="예: OHT-01" />
            </label>
            <label>
              오더 유형 *
              <select v-model="createForm.orderType" required>
                <option value="PREVENTIVE">PREVENTIVE</option>
                <option value="CORRECTIVE">CORRECTIVE</option>
                <option value="EMERGENCY">EMERGENCY</option>
              </select>
            </label>
            <label>
              우선순위
              <select v-model="createForm.priority">
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
                <option value="CRITICAL">CRITICAL</option>
              </select>
            </label>
            <label>
              제목 *
              <input v-model="createForm.title" required placeholder="정비 오더 제목" />
            </label>
            <label>
              설명
              <textarea v-model="createForm.description" rows="3" placeholder="상세 설명 (선택)"></textarea>
            </label>
            <div class="modal-actions">
              <button type="button" class="btn btn-outline" @click="showCreateModal = false">취소</button>
              <button type="submit" class="btn btn-primary">생성</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.priority-badge {
  font-weight: 600;
  font-size: 12px;
}

.status-chip {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: var(--color-surface);
  border-radius: 12px;
  padding: 28px;
  width: 480px;
  max-width: 90vw;
  box-shadow: var(--shadow-lg);
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 20px;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.modal-form label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.modal-form input,
.modal-form select,
.modal-form textarea {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
}

.modal-form input:focus,
.modal-form select:focus,
.modal-form textarea:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
