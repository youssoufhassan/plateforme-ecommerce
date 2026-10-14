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
    subtitle?: string;
    seeAllTo?: string;
  }>(),
  {
    loading: false,
    error: null,
    title: "Recommandé pour vous",
    subtitle: "Une sélection à découvrir dans nos différents univers.",
    seeAllTo: "/produits",
  },
);
</script>

<template>
  <section class="new-arrivals" aria-label="Sélection de produits">
    <div class="new-arrivals__container container">
      <header class="new-arrivals__header">
        <div class="new-arrivals__content">
          <h2 class="new-arrivals__title">
            {{ title }}
          </h2>

          <p v-if="subtitle" class="new-arrivals__description">
            {{ subtitle }}
          </p>
        </div>

        <RouterLink :to="seeAllTo" class="new-arrivals__link">
          <span>Tout découvrir</span>
          <span class="new-arrivals__link-arrow" aria-hidden="true">↗</span>
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

.new-arrivals__content {
  min-width: 0;
}

.new-arrivals__title {
  margin: 0;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: clamp(1.5rem, 2.4vw, 2rem);
  font-weight: var(--weight-regular);
  line-height: 1.2;
  letter-spacing: -0.045em;
}

.new-arrivals__description {
  max-width: 52ch;
  margin: 9px 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: 1.6;
}

.new-arrivals__link {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  gap: 10px;
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

.new-arrivals__link-arrow {
  display: inline-block;
  transition: transform var(--transition-base);
}

.new-arrivals__link:hover .new-arrivals__link-arrow {
  transform: translate(2px, -2px);
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

  .new-arrivals__description {
    max-width: 34ch;
    margin-top: 7px;
    font-size: var(--text-xs);
  }

  .new-arrivals__link {
    gap: 6px;
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

  .new-arrivals__description {
    max-width: 25ch;
  }
}

@media (prefers-reduced-motion: reduce) {
  .new-arrivals__link,
  .new-arrivals__link-arrow {
    transition: none;
  }
}
</style>
