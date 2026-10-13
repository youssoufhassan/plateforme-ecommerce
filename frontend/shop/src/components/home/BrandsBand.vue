<script setup lang="ts">
import { RouterLink } from "vue-router";

defineProps<{
  brands: string[];
}>();
</script>

<template>
  <section v-if="brands.length" class="brands" aria-label="Nos maisons">
    <div class="container">
      <h2 class="brands__title">Nos maisons</h2>

      <div class="brands__row">
        <RouterLink
          v-for="brand in brands"
          :key="brand"
          :to="{ path: '/produits', query: { brand } }"
          class="brands__item"
        >
          {{ brand }}
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.brands {
  padding-block: var(--space-12);
  background: var(--color-page);
}

.brands__title {
  margin: 0 0 var(--space-6);
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--weight-regular);
  letter-spacing: var(--tracking-tight);
}

.brands__row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.brands__item {
  padding: var(--space-3) var(--space-5);
  border: 1px solid var(--color-line);
  background: var(--color-paper);
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: var(--text-lg);
  text-decoration: none;
  transition:
    border-color var(--transition-fast),
    background var(--transition-fast),
    color var(--transition-fast);
}

.brands__item:hover {
  border-color: var(--color-ink);
  background: var(--color-ink);
  color: var(--color-text-inverse);
}

@media (max-width: 767px) {
  .brands {
    padding-block: var(--space-8);
  }

  .brands__item {
    padding: var(--space-2) var(--space-4);
    font-size: var(--text-base);
  }
}
</style>
