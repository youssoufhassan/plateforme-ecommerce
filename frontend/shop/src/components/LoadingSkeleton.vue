<script setup lang="ts">
defineProps<{ count?: number }>();
</script>

<template>
  <div class="product-grid">
    <div class="skeleton-card" v-for="i in count || 4" :key="i">
      <div class="skeleton-image" />
      <div class="skeleton-line" style="width: 70%" />
      <div class="skeleton-line" style="width: 40%" />
    </div>
  </div>
</template>
