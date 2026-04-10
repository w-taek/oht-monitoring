<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

const order = ref(null)
const loading = ref(true)
const updating = ref(false)

// 전이 입력 필드
const assigneeInput = ref('')
const actionTakenInput = ref('')

const statusColors = {
  OPEN: { bg: '#fef2f2', color: '#dc2626' },
  ASSIGNED: { bg: '#fffbeb', color: '#d97706' },
  IN_PROGRESS: { bg: '#eff6ff', color: '#2563eb' },
  COMPLETED: { bg: '#ecfdf5', color: '#059669' },
}

const priorityColors = {
  LOW: '#6b7280',
  MEDIUM: '#2563eb',
  HIGH: '#d97706',
  CRITICAL: '#dc2626',
}

const nextStatusMap = {
  OPEN: 'ASSIGNED',
  ASSIGNED: 'IN_PROGRESS',
  IN_PROGRESS: 'COMPLETED',
}

const nextStatusLabel = {
  ASSIGNED: '담당자 배정',
  IN_PROGRESS: '작업 시작',
  COMPLETED: '작업 완료',
}

const nextStatus = computed(() => order.value ? nextStatusMap[order.value.status] : null)

const statusSteps = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED']

function getStepState(step) {
  if (!order.value) return 'pending'
  const currentIdx = statusSteps.indexOf(order.value.status)
  const stepIdx = statusSteps.indexOf(step)
  if (stepIdx < currentIdx) return 'done'
  if (stepIdx === currentIdx) return 'current'
  return 'pending'
}

function formatTime(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR')
}

async function fetchOrder() {
  loading.value = true
  try {
    order.value = await api.get(`/api/v1/maintenance/${orderId}`)
  } catch (e) {
    console.error('정비 오더 로드 실패', e)
  } finally {
    loading.value = false
  }
}

async function transitionStatus() {
  if (!nextStatus.value) return

  const body = { status: nextStatus.value }

  if (nextStatus.value === 'ASSIGNED') {
    if (!assigneeInput.value.trim()) {
      alert('담당자를 입력해주세요')
      return
    }
    body.assignee = assigneeInput.value.trim()
  }

  if (nextStatus.value === 'COMPLETED') {
    if (!actionTakenInput.value.trim()) {
      alert('조치 내용을 입력해주세요')
      return
    }
    body.actionTaken = actionTakenInput.value.trim()
  }

  updating.value = true
  try {
    await api.patch(`/api/v1/maintenance/${orderId}/status`, body)
    assigneeInput.value = ''
    actionTakenInput.value = ''
    await fetchOrder()
  } catch (e) {
    alert(e.message || '상태 변경 실패')
  } finally {
    updating.value = false
  }
}

onMounted(fetchOrder)
</script>

<template>
  <div>
    <div class="page-header">
      <button class="btn btn-outline" @click="router.push('/maintenance')">← 목록으로</button>
    </div>

    <div v-if="loading" class="loading">데이터를 불러오는 중...</div>

    <template v-else-if="order">
      <!-- 상태 스텝 -->
      <div class="status-stepper card">
        <div
          v-for="step in statusSteps"
          :key="step"
          class="step"
          :class="getStepState(step)"
        >
          <div class="step-dot"></div>
          <span class="step-label">{{ step }}</span>
        </div>
      </div>

      <!-- 오더 정보 -->
      <div class="detail-grid">
        <div class="card info-card">
          <h2 class="card-title">오더 정보</h2>
          <div class="info-rows">
            <div class="info-row">
              <span class="info-label">ID</span>
              <span>#{{ order.id }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">제목</span>
              <span>{{ order.title }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">장비</span>
              <span>{{ order.eqId }} ({{ order.eqName }})</span>
            </div>
            <div class="info-row">
              <span class="info-label">유형</span>
              <span>{{ order.orderType }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">우선순위</span>
              <span class="priority-badge" :style="{ color: priorityColors[order.priority] }">
                {{ order.priority }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">상태</span>
              <span
                class="status-chip"
                :style="{ background: statusColors[order.status]?.bg, color: statusColors[order.status]?.color }"
              >
                {{ order.status }}
              </span>
            </div>
            <div class="info-row" v-if="order.alertLevel">
              <span class="info-label">연관 알림</span>
              <span>{{ order.alertLevel }} - {{ order.alertSensorName }}</span>
            </div>
            <div class="info-row" v-if="order.description">
              <span class="info-label">설명</span>
              <span>{{ order.description }}</span>
            </div>
            <div class="info-row" v-if="order.assignee">
              <span class="info-label">담당자</span>
              <span>{{ order.assignee }}</span>
            </div>
            <div class="info-row" v-if="order.actionTaken">
              <span class="info-label">조치 내용</span>
              <span>{{ order.actionTaken }}</span>
            </div>
          </div>
        </div>

        <div class="card info-card">
          <h2 class="card-title">타임라인</h2>
          <div class="info-rows">
            <div class="info-row">
              <span class="info-label">생성일</span>
              <span>{{ formatTime(order.createdAt) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">작업 시작</span>
              <span>{{ formatTime(order.startedAt) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">완료</span>
              <span>{{ formatTime(order.completedAt) }}</span>
            </div>
          </div>

          <!-- 상태 전이 액션 -->
          <div v-if="nextStatus" class="transition-section">
            <h3 class="transition-title">다음 단계: {{ nextStatusLabel[nextStatus] }}</h3>

            <div v-if="nextStatus === 'ASSIGNED'" class="transition-field">
              <label>담당자 *</label>
              <input v-model="assigneeInput" placeholder="담당자 이름" />
            </div>

            <div v-if="nextStatus === 'COMPLETED'" class="transition-field">
              <label>조치 내용 *</label>
              <textarea v-model="actionTakenInput" rows="3" placeholder="수행한 조치 내용"></textarea>
            </div>

            <button
              class="btn btn-primary transition-btn"
              :disabled="updating"
              @click="transitionStatus"
            >
              {{ updating ? '처리 중...' : nextStatusLabel[nextStatus] }}
            </button>
          </div>

          <div v-else-if="order.status === 'COMPLETED'" class="completed-msg">
            작업이 완료되었습니다
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* Status Stepper */
.status-stepper {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 20px 24px;
  margin-bottom: 20px;
}

.step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  position: relative;
}

.step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 8px;
  left: 55%;
  right: -45%;
  height: 2px;
  background: var(--color-border);
}

.step.done:not(:last-child)::after {
  background: var(--color-normal);
}

.step-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--color-border);
  position: relative;
  z-index: 1;
}

.step.done .step-dot {
  background: var(--color-normal);
}

.step.current .step-dot {
  background: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.2);
}

.step-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.step.current .step-label {
  color: var(--color-primary);
}

.step.done .step-label {
  color: var(--color-normal);
}

/* Detail Grid */
.detail-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border);
}

.info-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  gap: 12px;
}

.info-label {
  min-width: 80px;
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.info-row > span:last-child {
  font-size: 14px;
}

.priority-badge {
  font-weight: 600;
  font-size: 13px;
}

.status-chip {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

/* Transition */
.transition-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.transition-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.transition-field {
  margin-bottom: 12px;
}

.transition-field label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.transition-field input,
.transition-field textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
}

.transition-field input:focus,
.transition-field textarea:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
}

.transition-btn {
  width: 100%;
  margin-top: 4px;
}

.completed-msg {
  margin-top: 24px;
  padding: 16px;
  background: #ecfdf5;
  border-radius: 8px;
  color: var(--color-normal);
  font-weight: 600;
  text-align: center;
  font-size: 14px;
}
</style>
