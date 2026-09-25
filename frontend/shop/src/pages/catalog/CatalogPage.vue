<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";

import ProductCard from "@/components/product/ProductCard.vue";
import CatalogFiltersPanel from "@/components/catalog/CatalogFiltersPanel.vue";
import {
  apiMessage,
  fetchCatalogFilters,
  searchProducts,
} from "@/services/productService";
import type { CatalogFilters, SearchParams } from "@/types/catalog";
import type { Product } from "@/types/product";

const route = useRoute();
const router = useRouter();

const PAGE_SIZE = 24;

const products = ref<Product[]>([]);
const filters = ref<CatalogFilters | null>(null);
const totalElements = ref(0);
const hasNext = ref(false);
const currentPage = ref(0);

const loading = ref(false);
const loadingMore = ref(false);
const error = ref<string | null>(null);
const panelOpen = ref(false);

const SORT_LABELS: Record<string, string> = {
  relevance: "Nos suggestions",
  newest: "Nouveautés",
  price_asc: "Prix croissant",
  price_desc: "Prix décroissant",
  name: "Ordre alphabétique",
};

/** L'URL porte l'état complet : la page reste partageable et restaurable. */
const activeParams = computed<SearchParams>(() => {
  const query = route.query;

  return {
    q: (query.q as string) || undefined,
    category: (query.category as string) || undefined,
    brand: (query.brand as string) || undefined,
    minPrice: query.minPrice ? Number(query.minPrice) : undefined,
    maxPrice: query.maxPrice ? Number(query.maxPrice) : undefined,
    availableOnly: query.availableOnly === "true" ? true : undefined,
    sort: (query.sort as string) || "relevance",
  };
});

/** Pastilles des filtres appliqués, chacune retirable d'un clic. */
const activeChips = computed(() => {
  const { category, brand, minPrice, maxPrice, availableOnly } =
    activeParams.value;
  const chips: Array<{ key: string; label: string }> = [];

  if (category) chips.push({ key: "category", label: category });
  if (brand) chips.push({ key: "brand", label: brand });
  if (availableOnly) chips.push({ key: "availableOnly", label: "En stock" });

  if (minPrice && maxPrice) {
    chips.push({ key: "price", label: `${minPrice} € – ${maxPrice} €` });
  } else if (minPrice) {
    chips.push({ key: "price", label: `À partir de ${minPrice} €` });
  } else if (maxPrice) {
    chips.push({ key: "price", label: `Jusqu'à ${maxPrice} €` });
  }

  return chips;
});

/** Le fil d'Ariane dit où l'on est, pas ce qu'on a tapé. */
const breadcrumbLabel = computed(() => {
  const { brand, category } = activeParams.value;
  if (brand) return brand;
  if (category) return category;
  return "Catalogue";
});

const heading = computed(() => {
  const { q, category, brand } = activeParams.value;
  if (q) return q;
  if (brand) return brand;
  if (category) return category;
  return "Tous les parfums";
});

const subheading = computed(() => {
  const { brand } = activeParams.value;
  if (brand) return "La maison et ses créations";
  return "Notre sélection de fragrances et de produits venus d'ailleurs";
});

/** Part du catalogue déjà chargée, pour la barre de progression. */
const loadedRatio = computed(() =>
  totalElements.value === 0
    ? 0
    : Math.min(1, products.value.length / totalElements.value),
);

async function loadProducts(): Promise<void> {
  loading.value = true;
  error.value = null;
  currentPage.value = 0;

  try {
    const result = await searchProducts({
      ...activeParams.value,
      page: 0,
      size: PAGE_SIZE,
    });

    products.value = result.content;
    totalElements.value = result.totalElements;
    hasNext.value = result.hasNext;
  } catch (e: unknown) {
    error.value = apiMessage(e, "Le catalogue n'a pas pu être chargé.");
    products.value = [];
    totalElements.value = 0;
  } finally {
    loading.value = false;
  }
}

/** Page suivante ajoutée à la suite, la position de lecture est conservée. */
async function loadMore(): Promise<void> {
  if (loadingMore.value || !hasNext.value) return;

  loadingMore.value = true;

  try {
    const result = await searchProducts({
      ...activeParams.value,
      page: currentPage.value + 1,
      size: PAGE_SIZE,
    });

    products.value = [...products.value, ...result.content];
    currentPage.value = result.page;
    hasNext.value = result.hasNext;
  } catch (e: unknown) {
    error.value = apiMessage(
      e,
      "La suite du catalogue n'a pas pu être chargée.",
    );
  } finally {
    loadingMore.value = false;
  }
}

function updateQuery(changes: Record<string, string | undefined>): void {
  const query = { ...route.query, ...changes };

  Object.keys(query).forEach((key) => {
    if (query[key] === undefined || query[key] === "") delete query[key];
  });

  router.push({ path: "/produits", query });
}

function removeChip(key: string): void {
  if (key === "price") {
    updateQuery({ minPrice: undefined, maxPrice: undefined });
    return;
  }
  updateQuery({ [key]: undefined });
}

function clearFilters(): void {
  const { q, sort } = route.query;
  router.push({
    path: "/produits",
    query: { ...(q ? { q } : {}), ...(sort ? { sort } : {}) },
  });
}

watch(() => route.query, loadProducts, { immediate: true, deep: true });

fetchCatalogFilters()
  .then((data) => (filters.value = data))
  .catch(() => {
    // Sans les filtres, la liste reste utilisable : inutile d'alarmer
  });
</script>

<template>
  <main class="catalog">
    <!-- Recherche : on va droit aux résultats, sans titre ni fil d'Ariane -->
    <div v-if="!activeParams.q" class="container">
      <nav class="catalog__breadcrumb" aria-label="Fil d'Ariane">
        <RouterLink to="/">Accueil</RouterLink>
        <span aria-hidden="true">/</span>
        <span>{{ breadcrumbLabel }}</span>
      </nav>

      <header class="catalog__intro">
        <h1 class="catalog__title">{{ heading }}</h1>
        <p class="catalog__subtitle">{{ subheading }}</p>
      </header>
    </div>

    <!-- Titre lu par les lecteurs d'écran, invisible à l'écran -->
    <h1 v-else class="sr-only">Résultats pour « {{ heading }} »</h1>

    <!-- Reste accessible au défilement -->
    <div class="catalog__toolbar">
      <div class="container catalog__toolbar-inner">
        <button
          type="button"
          class="catalog__filter-button"
          @click="panelOpen = true"
        >
          Filtrer
          <span v-if="activeChips.length" class="catalog__filter-badge">
            {{ activeChips.length }}
          </span>
        </button>

        <p class="catalog__count" aria-live="polite">
          <template v-if="loading">Chargement</template>
          <template v-else-if="totalElements === 0">Aucun produit</template>
          <template v-else>
            {{ totalElements }} produit{{ totalElements > 1 ? "s" : "" }}
          </template>
        </p>

        <label class="catalog__sort">
          <span class="catalog__sort-label">Trier</span>
          <select
            :value="activeParams.sort"
            @change="
              updateQuery({ sort: ($event.target as HTMLSelectElement).value })
            "
          >
            <option
              v-for="(label, value) in SORT_LABELS"
              :key="value"
              :value="value"
            >
              {{ label }}
            </option>
          </select>
        </label>
      </div>
    </div>

    <div class="container">
      <!-- Filtres appliqués -->
      <div v-if="activeChips.length" class="catalog__chips">
        <button
          v-for="chip in activeChips"
          :key="chip.key"
          type="button"
          class="catalog__chip"
          @click="removeChip(chip.key)"
        >
          {{ chip.label }}
          <span class="catalog__chip-remove" aria-hidden="true">✕</span>
          <span class="sr-only">Retirer ce filtre</span>
        </button>

        <button type="button" class="catalog__chip-clear" @click="clearFilters">
          Tout effacer
        </button>
      </div>

      <div class="catalog__layout">
        <CatalogFiltersPanel
          :filters="filters"
          :active="activeParams"
          :open="panelOpen"
          @update="updateQuery"
          @clear="clearFilters"
          @close="panelOpen = false"
        />

        <div class="catalog__results">
          <div v-if="loading" class="catalog__grid" aria-busy="true">
            <div
              v-for="n in 12"
              :key="`skeleton-${n}`"
              class="catalog-skeleton"
            >
              <div class="catalog-skeleton__visual"></div>
              <div
                class="catalog-skeleton__line catalog-skeleton__line--short"
              ></div>
              <div class="catalog-skeleton__line"></div>
              <div
                class="catalog-skeleton__line catalog-skeleton__line--price"
              ></div>
            </div>
          </div>

          <div v-else-if="error" class="catalog__state" role="alert">
            <p class="catalog__state-title">{{ error }}</p>
            <button
              type="button"
              class="catalog__state-action"
              @click="loadProducts"
            >
              Réessayer
            </button>
          </div>

          <div v-else-if="products.length === 0" class="catalog__state">
            <p class="catalog__state-title">
              Rien ne correspond à cette combinaison.
            </p>
            <p class="catalog__state-hint">
              Élargissez la fourchette de prix ou retirez un filtre.
            </p>
            <button
              v-if="activeChips.length"
              type="button"
              class="catalog__state-action"
              @click="clearFilters"
            >
              Effacer les filtres
            </button>
          </div>

          <template v-else>
            <div class="catalog__grid">
              <ProductCard
                v-for="(product, index) in products"
                :key="product.id"
                :product="product"
                :eager="index < 8"
              />
            </div>

            <div v-if="hasNext || loadingMore" class="catalog__more">
              <div class="catalog__progress" aria-hidden="true">
                <span :style="{ transform: `scaleX(${loadedRatio})` }"></span>
              </div>

              <p class="catalog__progress-label">
                {{ products.length }} sur {{ totalElements }}
              </p>

              <button
                type="button"
                class="catalog__more-button"
                :disabled="loadingMore"
                @click="loadMore"
              >
                {{ loadingMore ? "Chargement" : "Afficher plus de produits" }}
              </button>
            </div>

            <p v-else class="catalog__end">Vous avez vu tout le catalogue.</p>
          </template>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.catalog {
  width: 100%;
  padding-bottom: 72px;
}

/* =========================================================
   INTRODUCTION
   ========================================================= */

.catalog__breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 22px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.catalog__breadcrumb a {
  color: inherit;
  text-decoration: none;
}

.catalog__breadcrumb a:hover {
  color: var(--color-text);
}

.catalog__intro {
  padding-block: 18px 26px;
}

.catalog__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(30px, 3.6vw, 46px);
  font-weight: 400;
  line-height: 1.02;
  letter-spacing: -0.035em;
  color: var(--color-text);
}

.catalog__subtitle {
  max-width: 56ch;
  margin: 10px 0 0;
  color: var(--color-text-muted);
  font-size: 14px;
  line-height: 1.5;
}

/* =========================================================
   BARRE D'OUTILS
   ========================================================= */

.catalog__toolbar {
  position: sticky;
  /* Se cale juste sous l'en-tête : tri et compteur restent à portée */
  top: var(--header-height);
  z-index: 5;
  /* Respiration quand la barre est le premier élément de la page */
  margin-top: 20px;
  background: var(--color-background);
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
}

.catalog__toolbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 56px;
}

.catalog__count {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.catalog__filter-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  border: 1px solid var(--color-text);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
}

.catalog__filter-button:hover {
  background: var(--color-text);
  color: var(--color-white);
}

.catalog__filter-badge {
  display: inline-grid;
  place-items: center;
  min-width: 17px;
  height: 17px;
  padding-inline: 5px;
  border-radius: 9px;
  background: var(--color-text);
  color: var(--color-white);
  font-size: 10px;
  font-variant-numeric: tabular-nums;
}

.catalog__filter-button:hover .catalog__filter-badge {
  background: var(--color-white);
  color: var(--color-text);
}

.catalog__sort {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.catalog__sort-label {
  color: var(--color-text-muted);
  font-size: 12px;
}

.catalog__sort select {
  padding: 7px 8px;
  border: none;
  border-bottom: 1px solid var(--color-text);
  border-radius: 0;
  background: transparent;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
}

/* =========================================================
   FILTRES APPLIQUÉS
   ========================================================= */

.catalog__chips {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding-top: 20px;
}

.catalog__chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  border: none;
  background: var(--color-text);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 11.5px;
  transition: opacity var(--transition-fast);
}

.catalog__chip:hover {
  opacity: 0.78;
}

.catalog__chip-remove {
  font-size: 9px;
  opacity: 0.7;
}

.catalog__chip-clear {
  padding: 7px 4px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: 11.5px;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.catalog__chip-clear:hover {
  color: var(--color-text);
}

/* =========================================================
   MISE EN PAGE
   ========================================================= */

.catalog__layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 40px;
  padding-top: 26px;
}

.catalog__results {
  min-width: 0;
}

.catalog__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 34px 18px;
}

/* =========================================================
   ÉTATS
   ========================================================= */

.catalog__state {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: 10px;
  min-height: 300px;
  text-align: center;
}

.catalog__state-title {
  margin: 0;
  color: var(--color-text);
  font-size: 15px;
}

.catalog__state-hint {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.catalog__state-action {
  margin-top: 6px;
  padding: 11px 22px;
  border: 1px solid var(--color-text);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
}

.catalog__state-action:hover {
  background: var(--color-text);
  color: var(--color-white);
}

/* =========================================================
   SQUELETTE
   ========================================================= */

.catalog-skeleton__visual {
  aspect-ratio: 1 / 1.05;
  background: #efedea;
}

.catalog-skeleton__line {
  height: 9px;
  margin-top: 9px;
  background: #efedea;
}

.catalog-skeleton__line--short {
  width: 38%;
  height: 7px;
  margin-top: 13px;
}

.catalog-skeleton__line--price {
  width: 26%;
}

@media (prefers-reduced-motion: no-preference) {
  .catalog-skeleton__visual,
  .catalog-skeleton__line {
    animation: catalog-pulse 1.5s ease-in-out infinite;
  }
}

@keyframes catalog-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.55;
  }
}

/* =========================================================
   CHARGEMENT PROGRESSIF
   ========================================================= */

.catalog__more {
  display: grid;
  justify-items: center;
  gap: 12px;
  margin-top: 54px;
}

.catalog__progress {
  width: 180px;
  height: 2px;
  background: var(--color-border);
  overflow: hidden;
}

.catalog__progress span {
  display: block;
  width: 100%;
  height: 100%;
  background: var(--color-text);
  transform-origin: left;
  transition: transform var(--transition-base);
}

.catalog__progress-label {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 11.5px;
  font-variant-numeric: tabular-nums;
}

.catalog__more-button {
  min-width: 260px;
  padding: 14px 32px;
  border: 1px solid var(--color-text);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
}

.catalog__more-button:hover:not(:disabled) {
  background: var(--color-text);
  color: var(--color-white);
}

.catalog__more-button:disabled {
  cursor: wait;
  opacity: 0.45;
}

.catalog__end {
  margin: 48px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
  text-align: center;
}

/* =========================================================
   TABLETTE
   ========================================================= */

@media (min-width: 768px) {
  .catalog__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 38px 20px;
  }
}

/* =========================================================
   DESKTOP
   ========================================================= */

@media (min-width: 1001px) {
  .catalog__layout {
    grid-template-columns: 236px minmax(0, 1fr);
    gap: 48px;
  }

  .catalog__grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  /* Les filtres sont visibles en permanence : le bouton disparaît */
  .catalog__filter-button {
    display: none;
  }
}

@media (min-width: 1440px) {
  .catalog__grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .catalog__intro {
    padding-block: 14px 20px;
  }

  .catalog__subtitle {
    font-size: 13px;
  }

  .catalog__toolbar-inner {
    height: 50px;
  }

  .catalog__sort-label {
    display: none;
  }

  .catalog__grid {
    gap: 28px 14px;
  }

  .catalog__more-button {
    min-width: 100%;
  }
}
</style>
