<script setup lang="ts">
import { computed, onMounted } from "vue";
import { RouterLink } from "vue-router";

import BrandCard from "@/components/home/BrandCard.vue";
import { useProductStore } from "@/stores/productStore";

const productStore = useProductStore();

onMounted(() => {
  if (productStore.products.length === 0) {
    productStore.fetchProducts();
  }
});

interface BrandItem {
  name: string;
  product: (typeof productStore.products)[number];
}

const brands = computed<BrandItem[]>(() => {
  const map = new Map<string, BrandItem>();

  for (const product of productStore.products) {
    const brand = product.brand?.trim();

    if (!brand) continue;

    const normalizedBrand = brand.toLowerCase();

    if (!map.has(normalizedBrand)) {
      map.set(normalizedBrand, {
        name: brand,
        product,
      });
    }
  }

  return Array.from(map.values()).slice(0, 8);
});
</script>

<template>
  <section v-if="brands.length" class="brands-section">
    <div class="container">
      <div class="section-header">
        <div class="section-content">
          <h2 class="section-title">Marques</h2>

          <p class="section-description">
            Découvrez les maisons disponibles chez SIDRA.
          </p>
        </div>

        <RouterLink to="/produits" class="section-link">
          <span>Voir toutes les marques</span>
          <span class="section-link__arrow">→</span>
        </RouterLink>
      </div>

      <div class="brands-row">
        <RouterLink
          v-for="brand in brands"
          :key="brand.name"
          :to="{
            path: '/produits',
            query: { brand: brand.name },
          }"
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

/* Marques sur une seule ligne */
.brands-row {
  display: flex;
  gap: 18px;

  overflow-x: auto;

  padding-bottom: 6px;

  scroll-snap-type: x mandatory;

  scrollbar-width: none;
  -ms-overflow-style: none;
}

.brands-row::-webkit-scrollbar {
  display: none;
}

.brand-link {
  display: block;

  flex: 0 0 calc((100% - 54px) / 4);

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
    flex-basis: calc((100% - 36px) / 3);
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
    flex: 0 0 72vw;
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
