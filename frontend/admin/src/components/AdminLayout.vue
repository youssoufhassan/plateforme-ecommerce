<script setup lang="ts">
import Sidebar from "./Sidebar.vue";
</script>

<template>
  <div class="admin-shell">
    <Sidebar />
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>
