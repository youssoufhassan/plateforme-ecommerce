<script setup lang="ts">
import { computed, onMounted } from "vue";

import ProductCard from "@/components/product/ProductCard.vue";
import { useProductStore } from "@/stores/productStore";

const productStore = useProductStore();

const products = computed(() => {
  return productStore.products.slice(0, 8);
});

onMounted(() => {
  if (productStore.products.length === 0) {
    productStore.fetchProducts();
  }
});
</script>

<template>
  <section class="product-carousel">
    <div v-if="productStore.loading" class="product-carousel__state">
      Chargement des produits...
    </div>

    <div
      v-else-if="productStore.error"
      class="product-carousel__state product-carousel__state--error"
    >
      {{ productStore.error }}
    </div>

    <div v-else-if="products.length === 0" class="product-carousel__state">
      Aucun produit disponible.
    </div>

    <div v-else class="product-carousel__track">
      <ProductCard
        v-for="product in products"
        :key="product.id"
        :product="product"
      />
    </div>
  </section>
</template>

<style scoped>
.product-carousel {
  width: 100%;
}

/* =========================================================
   CAROUSEL
   ========================================================= */

.product-carousel__track {
  display: grid;

  grid-auto-flow: column;

  grid-auto-columns: calc((100% - 72px) / 4);

  gap: 24px;

  overflow-x: auto;

  padding-bottom: 8px;

  scroll-snap-type: x mandatory;

  scrollbar-width: none;

  -webkit-overflow-scrolling: touch;
}

.product-carousel__track::-webkit-scrollbar {
  display: none;
}

.product-carousel__track > * {
  scroll-snap-align: start;
}

/* =========================================================
   ÉTATS
   ========================================================= */

.product-carousel__state {
  min-height: 220px;

  display: grid;
  place-items: center;

  color: var(--color-text-muted);

  font-size: 13px;
}

.product-carousel__state--error {
  color: #8a2525;
}

/* =========================================================
   TABLET
   ========================================================= */

@media (max-width: 1000px) {
  .product-carousel__track {
    grid-auto-columns: calc((100% - 48px) / 3);

    gap: 24px;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .product-carousel__track {
    grid-auto-columns: 72%;

    gap: 14px;

    margin-right: calc(var(--container-padding) * -1);

    padding-right: var(--container-padding);
  }
}
</style>
