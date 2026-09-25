<script setup lang="ts">
import { computed, onMounted } from "vue";
import { RouterLink } from "vue-router";

import ProductCard from "@/components/product/ProductCard.vue";
import { useProductStore } from "@/stores/productStore";

const productStore = useProductStore();

onMounted(() => {
  if (productStore.products.length === 0) {
    productStore.fetchProducts();
  }
});

function normalize(value?: string): string {
  return (value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

const menProducts = computed(() => {
  return productStore.products
    .filter((product) => {
      const category = normalize(product.categoryName);

      return (
        category.includes("homme") ||
        category.includes("men") ||
        category.includes("male")
      );
    })
    .slice(0, 8);
});

const womenProducts = computed(() => {
  return productStore.products
    .filter((product) => {
      const category = normalize(product.categoryName);

      return (
        category.includes("femme") ||
        category.includes("women") ||
        category.includes("female")
      );
    })
    .slice(0, 8);
});
</script>

<template>
  <section class="gender-sections">
    <div class="container">
      <!-- HOMME -->
      <section v-if="menProducts.length" class="gender-section">
        <div class="section-header">
          <div class="section-content">
            <h2 class="section-title">Homme</h2>

            <p class="section-description">
              Découvrez notre sélection de parfums pour homme.
            </p>
          </div>

          <RouterLink to="/produits?category=homme" class="section-link">
            <span>Voir tout</span>
            <span class="section-link__arrow">→</span>
          </RouterLink>
        </div>

        <div class="product-row">
          <ProductCard
            v-for="product in menProducts"
            :key="product.id"
            :product="product"
          />
        </div>
      </section>

      <!-- FEMME -->
      <section v-if="womenProducts.length" class="gender-section">
        <div class="section-header">
          <div class="section-content">
            <h2 class="section-title">Femme</h2>

            <p class="section-description">
              Découvrez notre sélection de parfums pour femme.
            </p>
          </div>

          <RouterLink to="/produits?category=femme" class="section-link">
            <span>Voir tout</span>
            <span class="section-link__arrow">→</span>
          </RouterLink>
        </div>

        <div class="product-row">
          <ProductCard
            v-for="product in womenProducts"
            :key="product.id"
            :product="product"
          />
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.gender-sections {
  padding: 14px 0 28px;
}

.gender-section {
  margin-bottom: 44px;
}

.gender-section:last-child {
  margin-bottom: 0;
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

/* Produits sur une seule ligne */
.product-row {
  display: flex;
  gap: 18px;
  overflow-x: auto;
  padding-bottom: 6px;

  scroll-snap-type: x mandatory;

  scrollbar-width: none;
  -ms-overflow-style: none;
}

.product-row::-webkit-scrollbar {
  display: none;
}

.product-row :deep(.product-card) {
  flex: 0 0 calc((100% - 54px) / 4);
  min-width: 0;
  scroll-snap-align: start;
}

/* Tablette */
@media (max-width: 1100px) {
  .gender-sections {
    padding-block: 32px 36px;
  }

  .gender-section {
    margin-bottom: 40px;
  }

  .product-row :deep(.product-card) {
    flex-basis: calc((100% - 36px) / 3);
  }
}

/* Mobile */
@media (max-width: 767px) {
  .gender-sections {
    padding-block: 14px 26px;
  }
  .gender-section {
    margin-bottom: 36px;
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

  .product-row {
    gap: 12px;
    margin-right: calc(var(--container-padding) * -1);
    padding-right: var(--container-padding);
  }

  .product-row :deep(.product-card) {
    flex: 0 0 72vw;
  }
}

/* Très petits écrans */
@media (max-width: 420px) {
  .gender-sections {
    padding-block: 24px 28px;
  }

  .gender-section {
    margin-bottom: 32px;
  }

  .section-title {
    font-size: 28px;
  }

  .section-description {
    font-size: 11px;
  }
}
</style>
