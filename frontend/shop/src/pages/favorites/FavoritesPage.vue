<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { RouterLink } from "vue-router";

import ProductCard from "@/components/product/ProductCard.vue";
import { useFavorites } from "@/composables/useFavorites";
import { fetchProduct } from "@/services/productService";
import type { Product } from "@/types/product";

const { favorites, clearFavorites } = useFavorites();

const products = ref<Product[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

async function loadFavoriteProducts(): Promise<void> {
  const ids = favorites.value;
  if (!ids.length) {
    products.value = [];
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const fetched = await Promise.allSettled(ids.map((id) => fetchProduct(id)));
    products.value = fetched
      .filter((res): res is PromiseFulfilledResult<Product> => res.status === "fulfilled")
      .map((res) => res.value);
  } catch {
    error.value = "Impossible de récupérer vos articles favoris.";
  } finally {
    loading.value = false;
  }
}

watch(
  () => favorites.value.length,
  () => {
    // Synchronize local list if an item was removed from card
    products.value = products.value.filter((p) => favorites.value.includes(String(p.id)));
    if (products.value.length === 0 && favorites.value.length > 0) {
      void loadFavoriteProducts();
    }
  },
);

onMounted(() => {
  void loadFavoriteProducts();
});
</script>

<template>
  <main class="favorites-page">
    <div class="container">
      <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <RouterLink to="/">Accueil</RouterLink>
        <span aria-hidden="true">/</span>
        <span>Favoris</span>
      </nav>

      <header class="favorites-header">
        <div>
          <span class="eyebrow">VOTRE SÉLECTION</span>
          <h1 class="page-title">Mes favoris</h1>
          <p class="page-subtitle">
            Retrouvez tous les articles que vous avez sauvegardés pour vos prochains achats.
          </p>
        </div>

        <button
          v-if="products.length > 0"
          type="button"
          class="btn-clear"
          @click="clearFavorites"
        >
          Tout effacer
        </button>
      </header>

      <!-- Chargement -->
      <div v-if="loading" class="favorites-loading">
        <div v-for="i in 4" :key="i" class="skeleton-card skeleton"></div>
      </div>

      <!-- Erreur -->
      <div v-else-if="error" class="notice notice-error" role="alert">
        {{ error }}
      </div>

      <!-- Vide -->
      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">♡</div>
        <h2 class="empty-state__title">Aucun favori enregistré</h2>
        <p class="empty-state__text">
          Vous n'avez pas encore ajouté de produit à votre liste d'envies.
          Parcourez nos collections et cliquez sur le cœur pour mémoriser vos coups de cœur.
        </p>
        <RouterLink to="/produits" class="btn btn-primary">
          Découvrir les collections
        </RouterLink>
      </div>

      <!-- Grille des favoris -->
      <div v-else class="favorites-grid">
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
        />
      </div>
    </div>
  </main>
</template>

<style scoped>
.favorites-page {
  padding-block: clamp(24px, 4vw, 48px) clamp(48px, 6vw, 80px);
  min-height: 70vh;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--text-xs, 0.75rem);
  color: var(--color-text-muted, #767672);
  margin-bottom: 24px;
}

.breadcrumb a {
  color: inherit;
  text-decoration: none;
}

.breadcrumb a:hover {
  color: var(--color-ink, #000);
}

.favorites-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: clamp(28px, 4vw, 44px);
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-line, #e0dfdb);
}

.eyebrow {
  display: block;
  font-size: 0.68rem;
  font-weight: 600;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--color-text-muted, #767672);
  margin-bottom: 8px;
}

.page-title {
  margin: 0;
  font-family: var(--font-display, serif);
  font-size: clamp(1.8rem, 3vw, 2.6rem);
  font-weight: 400;
  letter-spacing: -0.03em;
  line-height: 1.15;
}

.page-subtitle {
  margin: 10px 0 0;
  font-size: var(--text-sm, 0.88rem);
  color: var(--color-text-muted, #767672);
  max-width: 50ch;
}

.btn-clear {
  background: transparent;
  border: 1px solid var(--color-line, #e0dfdb);
  padding: 8px 16px;
  font-size: var(--text-xs, 0.75rem);
  cursor: pointer;
  color: var(--color-text-secondary, #3d3d3b);
  border-radius: var(--radius-none, 0);
  transition: all 0.15s ease;
}

.btn-clear:hover {
  border-color: var(--color-ink, #000);
  color: var(--color-ink, #000);
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--grid-gap-y, 2.5rem) var(--grid-gap-x, 1.25rem);
}

.favorites-loading {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--grid-gap-y, 2.5rem) var(--grid-gap-x, 1.25rem);
}

.skeleton-card {
  aspect-ratio: 3 / 4;
  border-radius: 2px;
}

.empty-icon {
  font-size: 2.8rem;
  line-height: 1;
  color: var(--color-text-muted, #767672);
  margin-bottom: 8px;
}

@media (max-width: 1024px) {
  .favorites-grid,
  .favorites-loading {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .favorites-grid,
  .favorites-loading {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1.5rem 0.875rem;
  }

  .favorites-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
