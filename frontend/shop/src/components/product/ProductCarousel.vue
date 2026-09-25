<script setup lang="ts">
import ProductCard from "@/components/product/ProductCard.vue";
import type { Product } from "@/types/product";

withDefaults(
  defineProps<{
    products: Product[];
    loading?: boolean;
    error?: string | null;
    skeletonCount?: number;
    onAdd?: (payload: {
      productId: string;
      variantId: string;
    }) => Promise<void>;
  }>(),
  {
    loading: false,
    error: null,
    skeletonCount: 5,
  },
);
</script>

<template>
  <section class="product-carousel">
    <!-- Chargement : mêmes dimensions que les vraies cartes, donc aucun décalage -->
    <div v-if="loading" class="product-carousel__track" aria-busy="true">
      <div
        v-for="n in skeletonCount"
        :key="`skeleton-${n}`"
        class="product-skeleton"
      >
        <div class="product-skeleton__visual"></div>
        <div class="product-skeleton__line product-skeleton__line--short"></div>
        <div class="product-skeleton__line"></div>
        <div class="product-skeleton__line product-skeleton__line--price"></div>
      </div>
    </div>

    <div
      v-else-if="error"
      class="product-carousel__state product-carousel__state--error"
      role="alert"
    >
      {{ error }}
    </div>

    <div v-else-if="products.length === 0" class="product-carousel__state">
      Aucun produit disponible pour le moment.
    </div>

    <div v-else class="product-carousel__track">
      <ProductCard
        v-for="(product, index) in products"
        :key="product.id"
        :product="product"
        :eager="index < 5"
        :on-add="onAdd"
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
  /* Cinq cartes visibles en desktop */
  grid-auto-columns: calc((100% - 4 * 16px) / 5);
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
  /* Évite que le défilement horizontal ne bloque celui de la page */
  overscroll-behavior-x: contain;
}

.product-carousel__track::-webkit-scrollbar {
  display: none;
}

.product-carousel__track > * {
  scroll-snap-align: start;
  /* Toutes les colonnes ont la même hauteur : les prix s'alignent */
  align-self: stretch;
}

/* =========================================================
   ÉTATS
   ========================================================= */

.product-carousel__state {
  /* Hauteur proche d'une carte : évite un saut entre les états */
  min-height: 300px;
  display: grid;
  place-items: center;
  color: var(--color-text-muted);
  font-size: 13px;
  text-align: center;
}

.product-carousel__state--error {
  color: #8a2525;
}

/* =========================================================
   SQUELETTE DE CHARGEMENT
   ========================================================= */

.product-skeleton {
  min-width: 0;
}

.product-skeleton__visual {
  aspect-ratio: 1 / 1.05;
  background: #f4f3f1;
}

.product-skeleton__line {
  height: 10px;
  margin-top: 8px;
  background: #f4f3f1;
}

.product-skeleton__line--short {
  width: 35%;
  height: 8px;
  margin-top: 12px;
}

.product-skeleton__line--price {
  width: 25%;
}

@media (prefers-reduced-motion: no-preference) {
  .product-skeleton__visual,
  .product-skeleton__line {
    animation: skeleton-pulse 1.4s ease-in-out infinite;
  }
}

@keyframes skeleton-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.55;
  }
}

/* =========================================================
   GRAND DESKTOP
   ========================================================= */

@media (min-width: 1440px) {
  .product-carousel__track {
    grid-auto-columns: calc((100% - 5 * 16px) / 6);
  }
}

/* =========================================================
   TABLET
   ========================================================= */

@media (max-width: 1000px) {
  .product-carousel__track {
    /* 3,4 colonnes : la carte coupée invite à faire défiler */
    grid-auto-columns: calc((100% - 3 * 14px) / 3.4);
    gap: 14px;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .product-carousel__track {
    grid-auto-columns: 44%;
    gap: 12px;
    margin-right: calc(var(--container-padding) * -1);
    padding-right: var(--container-padding);
  }

  .product-carousel__state {
    min-height: 260px;
  }

  .product-skeleton__visual {
    aspect-ratio: 1 / 1.1;
  }
}
</style>
