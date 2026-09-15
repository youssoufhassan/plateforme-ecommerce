<script setup lang="ts">
defineProps<{
  name: string;
  imageUrl?: string;
}>();
</script>

<template>
  <router-link to="/produits" class="ccard">
    <div class="ccard-image">
      <img v-if="imageUrl" :src="imageUrl" :alt="name" loading="lazy" />

      <div v-else class="ccard-fallback">
        {{ name }}
      </div>

      <div class="ccard-overlay">
        <span>Découvrir</span>
      </div>
    </div>

    <div class="ccard-name">
      {{ name }}
    </div>
  </router-link>
</template>
