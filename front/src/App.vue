<script setup>
import AppSidebar from './components/AppSidebar.vue'
import AlertToast from './components/AlertToast.vue'
</script>

<template>
  <div class="app-layout">
    <AppSidebar />
    <main class="main-content">
      <RouterView />
    </main>
    <AlertToast />
  </div>
</template>
