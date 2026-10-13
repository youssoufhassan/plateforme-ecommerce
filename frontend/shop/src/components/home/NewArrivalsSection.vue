<script setup lang="ts">
import { RouterLink } from "vue-router";

import ProductCarousel from "@/components/product/ProductCarousel.vue";
import type { Product } from "@/types/product";

withDefaults(
  defineProps<{
    products: Product[];
    loading?: boolean;
    error?: string | null;
    /** Paramétrable : la section sert aussi aux meilleures ventes. */
    title?: string;
    subtitle?: string;
    seeAllTo?: string;
  }>(),
  {
    loading: false,
    error: null,
    title: "Nouveautés",
    subtitle: "Les dernières pièces ajoutées à notre sélection.",
    seeAllTo: "/produits?sort=newest",
  },
);
</script>

<template>
  <section class="new-arrivals">
    <div class="new-arrivals__container container">
      <div class="new-arrivals__header">
        <div class="new-arrivals__content">
          <h2 class="new-arrivals__title">{{ title }}</h2>

          <p v-if="subtitle" class="new-arrivals__description">
            {{ subtitle }}
          </p>
        </div>

        <RouterLink :to="seeAllTo" class="new-arrivals__link">
          Voir tout
        </RouterLink>
      </div>

      <ProductCarousel :products="products" :loading="loading" :error="error" />
    </div>
  </section>
</template>

<style scoped>
.new-arrivals {
  width: 100%;
  padding-block: var(--space-12) var(--space-6);
}

.new-arrivals__container {
  width: 100%;
}

.new-arrivals__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-8);
  margin-bottom: var(--space-6);
}

.new-arrivals__content {
  min-width: 0;
}

.new-arrivals__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--weight-regular);
  line-height: var(--leading-tight);
  letter-spacing: var(--tracking-tight);
  color: var(--color-text);
}

.new-arrivals__description {
  max-width: 52ch;
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
}

/* Le soulignement se renforce au survol, sans déplacer le texte */
.new-arrivals__link {
  flex-shrink: 0;
  color: var(--color-text);
  font-size: var(--text-sm);
  text-decoration: underline;
  text-underline-offset: 0.25em;
  text-decoration-color: var(--color-line);
  transition: text-decoration-color var(--transition-fast);
}

.new-arrivals__link:hover {
  text-decoration-color: currentColor;
}

/* =========================================================
   TABLETTE
   ========================================================= */

@media (max-width: 1000px) {
  .new-arrivals {
    padding-block: var(--space-10) var(--space-6);
  }

  .new-arrivals__header {
    margin-bottom: var(--space-5);
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .new-arrivals {
    padding-block: var(--space-8) var(--space-4);
  }

  .new-arrivals__header {
    align-items: flex-start;
    gap: var(--space-4);
    margin-bottom: var(--space-4);
  }

  .new-arrivals__description {
    max-width: 100%;
    font-size: var(--text-xs);
  }

  .new-arrivals__link {
    margin-top: 2px;
    font-size: var(--text-xs);
  }
}
</style>
