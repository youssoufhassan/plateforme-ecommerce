<script setup lang="ts">
import { RouterLink } from "vue-router";

import ProductCarousel from "@/components/product/ProductCarousel.vue";
import type { Product } from "@/types/product";

withDefaults(
  defineProps<{
    products: Product[];
    loading?: boolean;
    error?: string | null;
    title?: string;
    seeAllTo?: string;
  }>(),
  {
    loading: false,
    error: null,
    title: "Recommandé pour vous",
    seeAllTo: "/produits",
  },
);
</script>

<template>
  <section class="new-arrivals" aria-label="Sélection de produits">
    <div class="new-arrivals__container container">
      <header class="new-arrivals__header">
        <h2 class="new-arrivals__title">
          {{ title }}
        </h2>

        <RouterLink :to="seeAllTo" class="new-arrivals__link">
          Tout découvrir
        </RouterLink>
      </header>

      <div class="new-arrivals__products">
        <ProductCarousel
          :products="products"
          :loading="loading"
          :error="error"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.new-arrivals {
  width: 100%;
  padding-block: 30px 36px;
}

.new-arrivals__container {
  width: 100%;
  min-width: 0;
}

.new-arrivals__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 22px;
}

.new-arrivals__title {
  min-width: 0;
  margin: 0;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: clamp(1.5rem, 2.4vw, 2rem);
  font-weight: var(--weight-regular);
  line-height: 1.2;
  letter-spacing: -0.045em;
}

.new-arrivals__link {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  padding-bottom: 5px;
  border-bottom: 1px solid var(--color-line);
  color: var(--color-text);
  font-size: var(--text-sm);
  text-decoration: none;
  transition:
    border-color var(--transition-base),
    color var(--transition-base);
}

.new-arrivals__link:hover {
  border-color: var(--color-ink);
}

.new-arrivals__products {
  min-width: 0;
  width: 100%;
}

@media (max-width: 1000px) {
  .new-arrivals {
    padding-block: 28px 30px;
  }

  .new-arrivals__header {
    margin-bottom: 20px;
  }
}

@media (max-width: 767px) {
  .new-arrivals {
    padding-block: 24px 26px;
  }

  .new-arrivals__header {
    align-items: flex-end;
    gap: 12px;
    margin-bottom: 18px;
  }

  .new-arrivals__title {
    font-size: 1.4rem;
    letter-spacing: -0.035em;
  }

  .new-arrivals__link {
    margin-bottom: 2px;
    font-size: var(--text-xs);
  }
}

@media (max-width: 380px) {
  .new-arrivals__header {
    align-items: flex-start;
  }

  .new-arrivals__link {
    margin-top: 5px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .new-arrivals__link {
    transition: none;
  }
}
</style>
