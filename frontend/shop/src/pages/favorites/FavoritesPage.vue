<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { RouterLink } from "vue-router";

import ProductCard from "@/components/product/ProductCard.vue";
import { useFavorites } from "@/composables/useFavorites";
import { fetchProduct } from "@/services/productService";
import type { Product } from "@/types/product";

const { favorites, clearFavorites } = useFavorites();

const products = ref<Product[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const clearing = ref(false);

// ID de requête pour ignorer les réponses obsolètes
let requestId = 0;
let cancelled = false;

/**
 * Compare un ID produit avec un favori, en tolérant les types string/number.
 * `useFavorites` peut stocker selon la source (localStorage = string,
 * state Pinia = string ou number). On normalise pour éviter les faux négatifs.
 */
function sameId(a: string, b: string): boolean {
  return String(a) === String(b);
}

function isFavorite(productId: string): boolean {
  return favorites.value.some((id) => sameId(id, productId));
}

async function loadFavoriteProducts(): Promise<void> {
  const ids = favorites.value;

  if (!ids.length) {
    products.value = [];
    error.value = null;
    loading.value = false;
    return;
  }

  const currentRequest = ++requestId;
  loading.value = true;
  error.value = null;

  const fetched = await Promise.allSettled(ids.map((id) => fetchProduct(id)));

  // Ignore la réponse si une requête plus récente a démarré
  // ou si le composant a été démonté entre-temps.
  if (currentRequest !== requestId || cancelled) return;

  const resolved: Product[] = [];
  let failedCount = 0;

  for (const result of fetched) {
    if (result.status === "fulfilled") {
      resolved.push(result.value);
    } else {
      failedCount++;
    }
  }

  // Conserve l'ordre de la liste de favoris
  const ordered = ids
    .map((id) => resolved.find((p) => sameId(p.id, id)))
    .filter((p): p is Product => Boolean(p));

  products.value = ordered;

  if (failedCount > 0) {
    error.value =
      failedCount === ids.length
        ? "Impossible de récupérer vos articles favoris."
        : `${failedCount} favori${failedCount > 1 ? "s" : ""} n'est plus disponible${failedCount > 1 ? "s" : ""}.`;
  }

  loading.value = false;
}

/**
 * Watch profond sur le contenu du tableau de favoris : détecte
 * aussi les remplacements à longueur constante (retrait + ajout).
 */
watch(
  favorites,
  (newIds) => {
    if (cancelled) return;

    // Synchronise localement sans refetch si tous les favoris actuels
    // sont déjà présents dans `products`
    const missing = newIds.filter(
      (id) => !products.value.some((p) => sameId(p.id, id)),
    );

    if (missing.length === 0) {
      // Retire les produits qui ne sont plus favoris
      products.value = products.value.filter((p) => isFavorite(p.id));
      return;
    }

    // De nouveaux favoris sont apparus : refetch complet
    void loadFavoriteProducts();
  },
  { deep: true },
);

const favoriteCount = computed(() => products.value.length);

const favoriteCountLabel = computed(
  () => `${favoriteCount.value} favori${favoriteCount.value > 1 ? "s" : ""}`,
);

async function handleClearFavorites(): Promise<void> {
  if (clearing.value) return;

  if (
    !window.confirm("Retirer tous les favoris ? Cette action est définitive.")
  ) {
    return;
  }

  clearing.value = true;

  try {
    clearFavorites();
    products.value = [];
  } finally {
    clearing.value = false;
  }
}

onMounted(() => {
  void loadFavoriteProducts();
});

onBeforeUnmount(() => {
  cancelled = true;
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
        <div class="favorites-header__title-block">
          <h1 class="page-title">Mes favoris</h1>

          <p v-if="favoriteCount > 0" class="page-meta" aria-live="polite">
            {{ favoriteCountLabel }}
          </p>
        </div>

        <button
          v-if="favoriteCount > 0"
          type="button"
          class="btn-clear"
          :disabled="clearing"
          @click="handleClearFavorites"
        >
          {{ clearing ? "Suppression…" : "Tout effacer" }}
        </button>
      </header>

      <!-- Chargement -->
      <div
        v-if="loading"
        class="favorites-loading"
        aria-label="Chargement des favoris"
      >
        <div v-for="i in 4" :key="i" class="skeleton-card skeleton"></div>
      </div>

      <!-- Erreur -->
      <div v-else-if="error" class="notice notice-error" role="alert">
        {{ error }}
      </div>

      <!-- Vide -->
      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon" aria-hidden="true">♡</div>
        <h2 class="empty-state__title">Aucun favori</h2>
        <p class="empty-state__text">
          Ajoutez des articles à vos favoris pour les retrouver ici.
        </p>
        <RouterLink to="/produits" class="btn btn-primary">
          Parcourir la boutique
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
  padding-block: var(--space-6, 1.5rem) var(--space-20, 5rem);
  min-height: 70vh;
  background: var(--color-page, #f2f2f1);
  color: var(--color-text, #000);
}

/* Fil d'Ariane */

.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  font-size: var(--text-xs, 0.75rem);
  color: var(--color-text-muted, #767672);
  margin-bottom: var(--space-6, 1.5rem);
}

.breadcrumb a {
  color: inherit;
  text-decoration: none;
  transition: color var(--transition-fast, 140ms ease);
}

.breadcrumb a:hover {
  color: var(--color-ink, #000);
}

/* En-tête */

.favorites-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-6, 1.5rem);
  margin-bottom: var(--space-8, 2rem);
  padding-bottom: var(--space-5, 1.25rem);
  border-bottom: 1px solid var(--color-line, #e0dfdb);
}

.favorites-header__title-block {
  min-width: 0;
}

.page-title {
  margin: 0;
  font-family: var(--font-display, Georgia, serif);
  font-size: clamp(var(--text-3xl, 2.375rem), 3vw, var(--text-4xl, 3rem));
  font-weight: var(--weight-regular, 400);
  letter-spacing: var(--tracking-tight, -0.03em);
  line-height: var(--leading-tight, 1.05);
  color: var(--color-text, #000);
}

.page-meta {
  margin: var(--space-2, 0.5rem) 0 0;
  font-size: var(--text-sm, 0.8125rem);
  color: var(--color-text-muted, #767672);
}

.btn-clear {
  background: transparent;
  border: 1px solid var(--color-line, #e0dfdb);
  padding: var(--space-2, 0.5rem) var(--space-4, 1rem);
  font-family: var(--font-body, sans-serif);
  font-size: var(--text-xs, 0.75rem);
  color: var(--color-text-secondary, #3d3d3b);
  border-radius: var(--radius-none, 0);
  cursor: pointer;
  transition:
    border-color var(--transition-fast, 140ms ease),
    color var(--transition-fast, 140ms ease);
}

.btn-clear:hover:not(:disabled) {
  border-color: var(--color-ink, #000);
  color: var(--color-ink, #000);
}

.btn-clear:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-clear:focus-visible {
  outline: 2px solid var(--color-ink, #000);
  outline-offset: 3px;
}

/* Grille */

.favorites-grid,
.favorites-loading {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--grid-gap-y, 2.5rem) var(--grid-gap-x, 1.25rem);
}

.skeleton-card {
  aspect-ratio: 3 / 4;
  background: var(--color-surface, #fff);
  animation: skeleton-pulse 1.6s ease-in-out infinite;
}

@keyframes skeleton-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}

/* État vide */

.empty-state {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: var(--space-3, 0.75rem);
  min-height: 320px;
  padding-block: var(--space-16, 4rem);
  text-align: center;
}

.empty-icon {
  font-size: var(--text-4xl, 3rem);
  line-height: 1;
  color: var(--color-text-muted, #767672);
}

.empty-state__title {
  margin: 0;
  font-family: var(--font-body, sans-serif);
  font-size: var(--text-lg, 1.125rem);
  font-weight: var(--weight-regular, 400);
  color: var(--color-text, #000);
}

.empty-state__text {
  max-width: 40ch;
  margin: 0;
  color: var(--color-text-muted, #767672);
  font-size: var(--text-sm, 0.8125rem);
  line-height: var(--leading-normal, 1.5);
}

.empty-state .btn {
  margin-top: var(--space-2, 0.5rem);
}

/* Tablette */

@media (max-width: 1024px) {
  .favorites-grid,
  .favorites-loading {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

/* Mobile */

@media (max-width: 768px) {
  .favorites-page {
    padding-block: var(--space-5, 1.25rem) var(--space-12, 3rem);
  }

  .favorites-grid,
  .favorites-loading {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: var(--space-6, 1.5rem) var(--space-3, 0.75rem);
  }

  .favorites-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4, 1rem);
  }
}

/* Accessibilité */

@media (prefers-reduced-motion: reduce) {
  .skeleton-card {
    animation: none;
  }

  .btn-clear,
  .breadcrumb a {
    transition: none;
  }
}
</style>
