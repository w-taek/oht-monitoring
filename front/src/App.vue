<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from './components/AppSidebar.vue'
import AlertToast from './components/AlertToast.vue'

const route = useRoute()
const isLoginPage = computed(() => route.path === '/login')
</script>

<template>
  <div :class="isLoginPage ? '' : 'app-layout'">
    <AppSidebar v-if="!isLoginPage" />
    <main :class="isLoginPage ? '' : 'main-content'">
      <RouterView />
    </main>
    <AlertToast v-if="!isLoginPage" />
  </div>
</template>
