<script setup>
const props = defineProps({
  state: { type: Number, default: 0 },
  label: { type: String, default: '' },
})

const stateConfig = {
  0: { text: '정상', color: 'var(--color-normal)', bg: '#ecfdf5' },
  1: { text: '관심', color: 'var(--color-caution)', bg: '#eff6ff' },
  2: { text: '경고', color: 'var(--color-warning)', bg: '#fffbeb' },
  3: { text: '위험', color: 'var(--color-danger)', bg: '#fef2f2' },
}

const config = stateConfig[props.state] || stateConfig[0]
const displayText = props.label || config.text
</script>

<template>
  <span
    class="status-badge"
    :style="{ color: config.color, backgroundColor: config.bg }"
  >
    {{ displayText }}
  </span>
</template>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
</style>
