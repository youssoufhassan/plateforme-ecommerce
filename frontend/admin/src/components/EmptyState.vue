<script setup lang="ts">
defineProps<{ title: string; message?: string }>();
</script>

<template>
  <div class="admin-empty">
    <h3>{{ title }}</h3>
    <p v-if="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.admin-empty {
  text-align: center;
  padding: 3.5rem 1rem;
  color: var(--ink-soft);
}
.admin-empty h3 {
  color: var(--ink);
  margin-bottom: 0.4rem;
  font-size: 1rem;
}
.admin-empty p {
  font-size: 0.85rem;
  margin: 0;
}
</style>
