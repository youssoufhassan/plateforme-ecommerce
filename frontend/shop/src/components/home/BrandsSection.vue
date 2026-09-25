<script setup lang="ts">
import { computed } from "vue";
import { RouterLink } from "vue-router";

import BrandCard from "@/components/home/BrandCard.vue";
import type { Product } from "@/types/product";

const props = withDefaults(
  defineProps<{
    /** Produits servant à extraire les marques (fournis par la page d'accueil). */
    products: Product[];
    limit?: number;
  }>(),
  { limit: 8 },
);

interface BrandItem {
  name: string;
  product: Product;
}

/** Une marque par produit, sans doublon, avec son premier visuel. */
const brands = computed<BrandItem[]>(() => {
  const map = new Map<string, BrandItem>();

  for (const product of props.products) {
    const brand = product.brand?.trim();
    if (!brand) continue;

    const key = brand.toLowerCase();
    if (!map.has(key)) {
      map.set(key, { name: brand, product });
    }
  }

  return Array.from(map.values()).slice(0, props.limit);
});
</script>

<template>
  <section v-if="brands.length" class="brands-section">
    <div class="container">
      <div class="section-header">
        <div class="section-content">
          <h2 class="section-title">Marques</h2>

          <p class="section-description">
            Découvrez les maisons disponibles chez SHAHIN.
          </p>
        </div>

        <RouterLink to="/produits" class="section-link">
          <span>Voir toutes les marques</span>
          <span class="section-link__arrow" aria-hidden="true">→</span>
        </RouterLink>
      </div>

      <div class="brands-row">
        <RouterLink
          v-for="brand in brands"
          :key="brand.name"
          :to="{ path: '/produits', query: { brand: brand.name } }"
          class="brand-link"
        >
          <BrandCard :brand="brand.name" :product="brand.product" />
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.brands-section {
  width: 100%;
  padding: 18px 0 32px;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 30px;
  margin-bottom: 22px;
}

.section-content {
  min-width: 0;
}

.section-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(30px, 3vw, 42px);
  font-weight: 400;
  line-height: 1.05;
  letter-spacing: -0.03em;
  color: var(--color-text);
}

.section-description {
  margin: 7px 0 0;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.45;
}

.section-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  padding-bottom: 3px;
  border-bottom: 1px solid var(--color-text);
  color: var(--color-text);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-decoration: none;
  text-transform: uppercase;
  transition:
    opacity 0.2s ease,
    gap 0.2s ease;
}

.section-link:hover {
  opacity: 0.65;
  gap: 11px;
}

.section-link__arrow {
  font-size: 14px;
  line-height: 1;
}

/* =========================================================
   RANGÉE — même rythme que le carrousel produits
   ========================================================= */

.brands-row {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 6px;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
  -ms-overflow-style: none;
  overscroll-behavior-x: contain;
}

.brands-row::-webkit-scrollbar {
  display: none;
}

.brand-link {
  display: block;
  /* Quatre marques visibles : plus larges que les produits, elles respirent mieux */
  flex: 0 0 calc((100% - 3 * 16px) / 4);
  min-width: 0;
  color: inherit;
  text-decoration: none;
  scroll-snap-align: start;
}

/* Tablette */
@media (max-width: 1100px) {
  .brands-section {
    padding-block: 16px 30px;
  }

  .brand-link {
    flex-basis: calc((100% - 2 * 14px) / 2.6);
  }

  .brands-row {
    gap: 14px;
  }
}

/* Mobile */
@media (max-width: 767px) {
  .brands-section {
    padding-block: 14px 26px;
  }

  .section-header {
    align-items: flex-start;
    gap: 14px;
    margin-bottom: 18px;
  }

  .section-title {
    font-size: 30px;
  }

  .section-description {
    margin-top: 6px;
    font-size: 12px;
  }

  .section-link {
    font-size: 10px;
  }

  .brands-row {
    gap: 12px;
    margin-right: calc(var(--container-padding) * -1);
    padding-right: var(--container-padding);
  }

  .brand-link {
    flex: 0 0 62%;
  }
}

/* Très petits écrans */
@media (max-width: 420px) {
  .brands-section {
    padding-block: 12px 24px;
  }

  .section-title {
    font-size: 28px;
  }

  .section-description {
    font-size: 11px;
  }
}
</style>
