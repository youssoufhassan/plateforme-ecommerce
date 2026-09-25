<script setup lang="ts">
import { computed, onMounted } from "vue";

import { useProductStore } from "@/stores/productStore";

const productStore = useProductStore();

const products = computed(() => {
  return productStore.products.slice(0, 4);
});

function getProductImage(imageUrl?: string): string {
  if (!imageUrl) {
    return "/placeholder-product.jpg";
  }

  if (imageUrl.startsWith("http")) {
    return imageUrl;
  }

  return `${import.meta.env.VITE_API_URL || "http://localhost:8080"}${imageUrl}`;
}

function formatPrice(price: number): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(price);
}

onMounted(() => {
  productStore.fetchProducts();
});
</script>

<template>
  <section class="featured-products">
    <div class="featured-products__container container">
      <div class="featured-products__header">
        <div>
          <p class="featured-products__eyebrow">LA SÉLECTION SIDRA</p>

          <h2 class="featured-products__title">Les essentiels</h2>
        </div>

        <RouterLink to="/products" class="featured-products__link">
          Voir toute la collection
          <span aria-hidden="true">→</span>
        </RouterLink>
      </div>

      <!-- Chargement -->
      <div v-if="productStore.loading" class="featured-products__status">
        Chargement des produits...
      </div>

      <!-- Erreur -->
      <div v-else-if="productStore.error" class="featured-products__status">
        {{ productStore.error }}
      </div>

      <!-- Aucun produit -->
      <div v-else-if="products.length === 0" class="featured-products__status">
        Aucun produit disponible.
      </div>

      <!-- Produits -->
      <div v-else class="featured-products__grid">
        <article
          v-for="product in products"
          :key="product.id"
          class="product-card"
        >
          <RouterLink
            :to="`/products/${product.id}`"
            class="product-card__image"
          >
            <div class="product-card__image-container">
              <img
                :src="getProductImage(product.imageUrl)"
                :alt="product.name"
                loading="lazy"
              />
            </div>

            <button
              type="button"
              class="product-card__favorite"
              :aria-label="`Ajouter ${product.name} aux favoris`"
              @click.prevent
            >
              ♡
            </button>
          </RouterLink>

          <div class="product-card__content">
            <p class="product-card__brand">
              {{ product.brand || "SIDRA" }}
            </p>

            <h3 class="product-card__name">
              {{ product.name }}
            </h3>

            <p class="product-card__price">
              {{ formatPrice(product.price) }}
            </p>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.featured-products {
  padding: 110px 0 120px;
  background: var(--color-white);
}

.featured-products__container {
  width: min(calc(100% - (var(--container-padding) * 2)), var(--container-max));
}

.featured-products__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
  margin-bottom: 48px;
}

.featured-products__eyebrow {
  margin-bottom: 14px;
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.featured-products__title {
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 3.5rem);
  font-weight: 400;
  line-height: 1;
  letter-spacing: -0.04em;
}

.featured-products__link {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 5px;
  color: var(--color-text);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--color-text);
  transition:
    opacity var(--transition-fast),
    gap var(--transition-fast);
}

.featured-products__link:hover {
  gap: 15px;
  opacity: 0.6;
}

.featured-products__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.featured-products__status {
  padding: 80px 20px;
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.product-card {
  min-width: 0;
}

.product-card__image {
  position: relative;
  display: block;
  overflow: hidden;
  background: #f3f1ed;
}

.product-card__image-container {
  aspect-ratio: 0.82;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12%;
  overflow: hidden;
}

.product-card__image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform var(--transition-slow);
}

.product-card__image:hover img {
  transform: scale(1.04);
}

.product-card__favorite {
  position: absolute;
  top: 16px;
  right: 16px;

  width: 38px;
  height: 38px;

  display: grid;
  place-items: center;

  background: rgba(255, 255, 255, 0.92);
  border-radius: 50%;

  font-size: 22px;
  font-weight: 300;

  transition:
    transform var(--transition-fast),
    background var(--transition-fast);
}

.product-card__favorite:hover {
  transform: scale(1.08);
  background: var(--color-white);
}

.product-card__content {
  padding-top: 16px;
}

.product-card__brand {
  margin-bottom: 4px;
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.14em;
}

.product-card__name {
  font-size: 14px;
  font-weight: 400;
  line-height: 1.4;
}

.product-card__price {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 500;
}

@media (max-width: 1000px) {
  .featured-products__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 28px 14px;
  }
}

@media (max-width: 767px) {
  .featured-products {
    padding: 72px 0 80px;
  }

  .featured-products__header {
    align-items: flex-start;
    flex-direction: column;
    margin-bottom: 32px;
    gap: 20px;
  }

  .featured-products__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 28px 10px;
  }

  .product-card__image-container {
    padding: 10%;
  }

  .product-card__favorite {
    top: 10px;
    right: 10px;
    width: 32px;
    height: 32px;
    font-size: 18px;
  }

  .product-card__content {
    padding-top: 12px;
  }

  .product-card__brand {
    font-size: 9px;
  }

  .product-card__name {
    font-size: 13px;
  }

  .product-card__price {
    font-size: 12px;
  }
}
</style>
