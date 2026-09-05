<script setup lang="ts">
defineProps<{ label: string; value: string; unavailable?: boolean }>();
</script>

<template>
  <div class="stat-card" :class="{ unavailable }">
    <div class="value">{{ unavailable ? "—" : value }}</div>
    <div class="label">{{ label }}</div>
    <div v-if="unavailable" class="unavailable-note">
      Nécessite une mise à jour backend
    </div>
  </div>
</template>

<style scoped>
.stat-card.unavailable {
  opacity: 0.55;
}
.unavailable-note {
  font-size: 0.68rem;
  color: var(--ink-soft);
  margin-top: 0.3rem;
}
</style>
