<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";

import ProductCard from "@/components/product/ProductCard.vue";
import CatalogFiltersPanel from "@/components/catalog/CatalogFiltersPanel.vue";

import {
  apiMessage,
  fetchBreadcrumb,
  fetchCatalogFilters,
  fetchCategoryTree,
  searchProducts,
} from "@/services/productService";

import type {
  CatalogFilters,
  CategoryNode,
  SearchParams,
} from "@/types/catalog";
import type { Product } from "@/types/product";

const route = useRoute();
const router = useRouter();

const PAGE_SIZE = 24;

const products = ref<Product[]>([]);
const filters = ref<CatalogFilters | null>(null);
const totalElements = ref(0);
const hasNext = ref(false);
const currentPage = ref(0);

const tree = ref<CategoryNode[]>([]);
const breadcrumb = ref<CategoryNode[]>([]);

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

/** L'URL conserve l'état du catalogue : la page reste partageable. */
const activeParams = computed<SearchParams>(() => {
  const query = route.query;

  return {
    q: typeof query.q === "string" ? query.q : undefined,
    category: typeof query.category === "string" ? query.category : undefined,
    brand: typeof query.brand === "string" ? query.brand : undefined,
    minPrice:
      typeof query.minPrice === "string" ? Number(query.minPrice) : undefined,
    maxPrice:
      typeof query.maxPrice === "string" ? Number(query.maxPrice) : undefined,
    availableOnly: query.availableOnly === "true" ? true : undefined,
    sort: typeof query.sort === "string" ? query.sort : "relevance",
  };
});

const currentCategory = computed(
  () => breadcrumb.value[breadcrumb.value.length - 1] ?? null,
);

const categoryName = computed(() => currentCategory.value?.name ?? null);

/** Rayons proposés : ceux de la branche consultée, ou les univers. */
const subCategories = computed(() => {
  if (currentCategory.value) return currentCategory.value.children ?? [];
  if (!activeParams.value.category && !activeParams.value.q) return tree.value;
  return [];
});

const activeChips = computed(() => {
  const { category, brand, minPrice, maxPrice, availableOnly } =
    activeParams.value;

  const chips: Array<{ key: string; label: string }> = [];

  if (category) {
    chips.push({ key: "category", label: categoryName.value ?? category });
  }
  if (brand) {
    chips.push({ key: "brand", label: brand });
  }
  if (availableOnly) {
    chips.push({ key: "availableOnly", label: "En stock" });
  }

  if (minPrice != null && maxPrice != null) {
    chips.push({ key: "price", label: `${minPrice} € – ${maxPrice} €` });
  } else if (minPrice != null) {
    chips.push({ key: "price", label: `À partir de ${minPrice} €` });
  } else if (maxPrice != null) {
    chips.push({ key: "price", label: `Jusqu'à ${maxPrice} €` });
  }

  return chips;
});

const heading = computed(() => {
  const { q, brand } = activeParams.value;

  if (q) return q;
  if (brand) return brand;
  if (categoryName.value) return categoryName.value;

  return "Tout le catalogue";
});

/** Une recherche mérite un grand titre ; une catégorie, un titre sobre. */
const isSearch = computed(() =>
  Boolean(activeParams.value.q || activeParams.value.brand),
);

/** Une catégorie se passe d'accroche : son contenu parle pour elle. */
const subheading = computed(() => {
  if (activeParams.value.brand) return "La maison et ses créations";
  if (currentCategory.value) return null;

  return "Parfums, beauté, mode et art de vivre";
});

const loadedRatio = computed(() =>
  totalElements.value === 0
    ? 0
    : Math.min(1, products.value.length / totalElements.value),
);

async function loadProducts(): Promise<void> {
  loading.value = true;
  loadingMore.value = false;
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
    hasNext.value = false;
  } finally {
    loading.value = false;
  }
}

async function loadCategoryContext(): Promise<void> {
  const path = activeParams.value.category;

  if (!path) {
    breadcrumb.value = [];
    return;
  }

  try {
    breadcrumb.value = await fetchBreadcrumb(path);
  } catch {
    breadcrumb.value = [];
  }
}

/** Marques et bornes de prix se limitent à la branche consultée. */
async function loadFilters(): Promise<void> {
  try {
    filters.value = await fetchCatalogFilters(activeParams.value.category);
  } catch {
    filters.value = null;
  }
}

/** La page suivante s'ajoute : la position de lecture est conservée. */
async function loadMore(): Promise<void> {
  if (loadingMore.value || loading.value || !hasNext.value) return;

  loadingMore.value = true;
  error.value = null;

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
    error.value = apiMessage(e, "La suite n'a pas pu être chargée.");
  } finally {
    loadingMore.value = false;
  }
}

function updateQuery(changes: Record<string, string | undefined>): void {
  const query = { ...route.query } as Record<string, string | string[] | null>;

  for (const [key, value] of Object.entries(changes)) {
    if (value === undefined || value === "") {
      delete query[key];
    } else {
      query[key] = value;
    }
  }

  void router.push({ path: "/produits", query });
}

function removeChip(key: string): void {
  if (key === "price") {
    updateQuery({ minPrice: undefined, maxPrice: undefined });
    return;
  }

  // Changer de rayon invalide la marque : elle peut ne plus exister
  if (key === "category") {
    updateQuery({ category: undefined, brand: undefined });
    return;
  }

  updateQuery({ [key]: undefined });
}

function clearFilters(): void {
  const { q, sort } = route.query;

  void router.push({
    path: "/produits",
    query: {
      ...(typeof q === "string" && q ? { q } : {}),
      ...(typeof sort === "string" && sort ? { sort } : {}),
    },
  });
}

watch(
  () => route.query,
  () => {
    void loadProducts();
    void loadCategoryContext();
    void loadFilters();
  },
  { immediate: true },
);

fetchCategoryTree()
  .then((data) => {
    tree.value = data;
  })
  .catch(() => {
    tree.value = [];
  });
</script>

<template>
  <main class="catalog">
    <div v-if="!activeParams.q" class="container">
      <nav class="trail" aria-label="Fil d'Ariane">
        <RouterLink to="/">Accueil</RouterLink>

        <template v-if="breadcrumb.length">
          <span class="trail__sep" aria-hidden="true">/</span>
          <RouterLink to="/produits">Catalogue</RouterLink>

          <template v-for="(node, index) in breadcrumb" :key="node.id">
            <span class="trail__sep" aria-hidden="true">/</span>

            <span v-if="index === breadcrumb.length - 1" aria-current="page">
              {{ node.name }}
            </span>

            <RouterLink
              v-else
              :to="{ path: '/produits', query: { category: node.path } }"
            >
              {{ node.name }}
            </RouterLink>
          </template>
        </template>

        <template v-else>
          <span class="trail__sep" aria-hidden="true">/</span>
          <span aria-current="page">Catalogue</span>
        </template>
      </nav>

      <header class="intro" :class="{ 'intro--search': isSearch }">
        <h1 class="intro__title">{{ heading }}</h1>

        <p v-if="subheading" class="intro__text">{{ subheading }}</p>
      </header>

      <nav v-if="subCategories.length" class="rayons" aria-label="Rayons">
        <RouterLink
          v-for="node in subCategories"
          :key="node.id"
          :to="{ path: '/produits', query: { category: node.path } }"
          class="rayons__link"
        >
          {{ node.name }}
          <span class="rayons__count numeric">{{ node.productCount }}</span>
        </RouterLink>
      </nav>
    </div>

    <h1 v-else class="sr-only">Résultats pour « {{ heading }} »</h1>

    <div class="toolbar">
      <div class="container toolbar__inner">
        <button
          type="button"
          class="toolbar__filter"
          :aria-expanded="panelOpen"
          @click="panelOpen = true"
        >
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path
              d="M3 6h14M6 10h8M8.5 14h3"
              stroke="currentColor"
              stroke-width="1.2"
              stroke-linecap="round"
            />
          </svg>

          Filtrer

          <span v-if="activeChips.length" class="toolbar__badge numeric">
            {{ activeChips.length }}
          </span>
        </button>

        <p class="toolbar__count numeric" aria-live="polite">
          <template v-if="loading">Chargement</template>
          <template v-else-if="totalElements === 0">Aucun produit</template>
          <template v-else>
            {{ totalElements }} produit{{ totalElements > 1 ? "s" : "" }}
          </template>
        </p>

        <label class="toolbar__sort">
          <span class="sr-only">Trier</span>

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
      <div v-if="activeChips.length" class="chips">
        <button
          v-for="chip in activeChips"
          :key="chip.key"
          type="button"
          class="chip"
          @click="removeChip(chip.key)"
        >
          {{ chip.label }}

          <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path
              d="M4 4l8 8M12 4l-8 8"
              stroke="currentColor"
              stroke-width="1.2"
              stroke-linecap="round"
            />
          </svg>

          <span class="sr-only">Retirer ce filtre</span>
        </button>

        <button type="button" class="chips__clear" @click="clearFilters">
          Tout effacer
        </button>
      </div>

      <div class="layout">
        <CatalogFiltersPanel
          :filters="filters"
          :categories="tree"
          :active="activeParams"
          :open="panelOpen"
          @update="updateQuery"
          @clear="clearFilters"
          @close="panelOpen = false"
        />

        <div class="results">
          <div v-if="loading" class="grid" aria-busy="true">
            <div v-for="n in 12" :key="`skeleton-${n}`">
              <div class="skeleton__visual"></div>
              <div class="skeleton__line skeleton__line--short"></div>
              <div class="skeleton__line"></div>
              <div class="skeleton__line skeleton__line--price"></div>
            </div>
          </div>

          <div v-else-if="error" class="state" role="alert">
            <p class="state__title">{{ error }}</p>

            <button type="button" class="state__action" @click="loadProducts">
              Réessayer
            </button>
          </div>

          <div v-else-if="products.length === 0" class="state">
            <p class="state__title">
              Aucun produit ne correspond à cette sélection.
            </p>

            <p class="state__hint">
              Essayez d'élargir la fourchette de prix, ou parcourez une autre
              maison.
            </p>

            <button
              v-if="activeChips.length"
              type="button"
              class="state__action"
              @click="clearFilters"
            >
              Effacer les filtres
            </button>
          </div>

          <template v-else>
            <div class="grid">
              <ProductCard
                v-for="(product, index) in products"
                :key="product.id"
                :product="product"
                :eager="index < 8"
              />
            </div>

            <div v-if="hasNext || loadingMore" class="more">
              <div class="more__progress" aria-hidden="true">
                <span :style="{ transform: `scaleX(${loadedRatio})` }"></span>
              </div>

              <p class="more__label numeric">
                {{ products.length }} sur {{ totalElements }}
              </p>

              <button
                type="button"
                class="more__button"
                :disabled="loadingMore"
                @click="loadMore"
              >
                {{ loadingMore ? "Chargement" : "Afficher plus de produits" }}
              </button>
            </div>

            <p v-else class="more__end">Vous avez vu tout ce rayon.</p>
          </template>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.catalog {
  width: 100%;
  padding-bottom: var(--space-20);
}

/* =========================================================
   FIL D'ARIANE
   ========================================================= */

.trail {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  padding-top: var(--space-5);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.trail a {
  color: inherit;
  text-decoration: none;
  transition: color var(--transition-fast);
}

.trail a:hover {
  color: var(--color-text);
}

.trail__sep {
  color: var(--color-line);
}

/* =========================================================
   INTRODUCTION
   ========================================================= */

.intro {
  padding-block: var(--space-5) var(--space-6);
}

.intro__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(26px, 2.8vw, 38px);
  font-weight: var(--weight-regular);
  line-height: 1.05;
  letter-spacing: -0.025em;
  color: var(--color-text);
}

/* Une recherche mérite un titre plus ample, pas une catégorie. */
.intro--search .intro__title {
  font-size: clamp(32px, 3.6vw, 52px);
  letter-spacing: -0.035em;
}

.intro__text {
  max-width: 54ch;
  margin: var(--space-3) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-md);
  line-height: var(--leading-normal);
}

/* =========================================================
   RAYONS
   ========================================================= */

.rayons {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  padding-bottom: var(--space-6);
}

.rayons__link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-3);

  height: 46px;
  padding-inline: var(--space-6);

  border: 1px solid var(--color-line);
  border-radius: var(--radius-pill);

  color: var(--color-text);
  font-size: var(--text-md);
  text-decoration: none;
  white-space: nowrap;

  transition:
    border-color var(--transition-fast),
    background var(--transition-fast),
    color var(--transition-fast);
}

.rayons__link:hover {
  border-color: var(--color-ink);
  background: var(--color-ink);
  color: var(--color-text-inverse);
}

.rayons__count {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  transition: color var(--transition-fast);
}

.rayons__link:hover .rayons__count {
  color: var(--color-text-inverse);
  opacity: 0.7;
}

/* =========================================================
   BARRE D'OUTILS
   ========================================================= */

.toolbar {
  position: sticky;
  top: var(--header-height);
  z-index: var(--z-sticky);

  border-bottom: 1px solid var(--color-line);
  background: var(--color-page);
}

.toolbar__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  min-height: 48px;
}

.toolbar__filter {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);

  height: 34px;
  padding-inline: var(--space-4);

  border: 1px solid var(--color-ink);
  background: transparent;
  cursor: pointer;

  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-sm);
  letter-spacing: 0.02em;

  transition:
    background var(--transition-fast),
    color var(--transition-fast);
}

.toolbar__filter:hover {
  background: var(--color-ink);
  color: var(--color-text-inverse);
}

.toolbar__filter svg {
  width: 15px;
  height: 15px;
}

.toolbar__badge {
  display: inline-grid;
  place-items: center;
  min-width: 17px;
  height: 17px;
  padding-inline: 5px;
  border-radius: var(--radius-pill);
  background: var(--color-ink);
  color: var(--color-text-inverse);
  font-size: 10px;
}

.toolbar__filter:hover .toolbar__badge {
  background: var(--color-paper);
  color: var(--color-text);
}

.toolbar__count {
  margin: 0;
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
}

.toolbar__sort {
  display: inline-flex;
  align-items: center;
}

.toolbar__sort select {
  padding: var(--space-1) 0;
  border: none;
  border-bottom: 1px solid var(--color-line);
  border-radius: 0;
  background: transparent;
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-sm);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.toolbar__sort select:hover,
.toolbar__sort select:focus {
  border-bottom-color: var(--color-ink);
  outline: none;
}

/* =========================================================
   FILTRES APPLIQUÉS
   ========================================================= */

.chips {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  padding-top: var(--space-5);
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);

  height: 30px;
  padding-inline: var(--space-3);

  border: 1px solid var(--color-line);
  background: transparent;
  cursor: pointer;

  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-xs);
  letter-spacing: 0.02em;

  transition: border-color var(--transition-fast);
}

.chip:hover {
  border-color: var(--color-ink);
}

.chip svg {
  width: 11px;
  height: 11px;
  opacity: 0.55;
}

.chip:hover svg {
  opacity: 1;
}

.chips__clear {
  height: 30px;
  padding-inline: var(--space-2);
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: var(--text-xs);
  text-decoration: underline;
  text-underline-offset: 0.25em;
}

.chips__clear:hover {
  color: var(--color-text);
}

/* =========================================================
   MISE EN PAGE
   ========================================================= */

.layout {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  padding-top: var(--space-6);
}

.results {
  min-width: 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-items: stretch;
  column-gap: var(--grid-gap-x);
  row-gap: var(--grid-gap-y);
}

/* =========================================================
   ÉTATS
   ========================================================= */

.state {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: var(--space-3);
  min-height: 320px;
  text-align: center;
}

.state__title {
  margin: 0;
  font-size: var(--text-lg);
}

.state__hint {
  max-width: 40ch;
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
}

.state__action {
  height: 42px;
  margin-top: var(--space-2);
  padding-inline: var(--space-6);
  border: 1px solid var(--color-ink);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-sm);
  letter-spacing: 0.02em;
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
}

.state__action:hover {
  background: var(--color-ink);
  color: var(--color-text-inverse);
}

/* =========================================================
   SQUELETTE
   ========================================================= */

.skeleton__visual {
  aspect-ratio: 1 / 1.05;
  background: var(--color-sand);
}

.skeleton__line {
  height: 9px;
  margin-top: var(--space-2);
  background: var(--color-sand);
}

.skeleton__line--short {
  width: 38%;
  height: 7px;
  margin-top: var(--space-3);
}

.skeleton__line--price {
  width: 26%;
}

@media (prefers-reduced-motion: no-preference) {
  .skeleton__visual,
  .skeleton__line {
    animation: pulse 1.6s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

/* =========================================================
   CHARGEMENT PROGRESSIF
   ========================================================= */

.more {
  display: grid;
  justify-items: center;
  gap: var(--space-3);
  margin-top: var(--space-16);
}

.more__progress {
  width: 160px;
  height: 1px;
  overflow: hidden;
  background: var(--color-line);
}

.more__progress span {
  display: block;
  width: 100%;
  height: 100%;
  background: var(--color-ink);
  transform-origin: left;
  transition: transform var(--transition-base);
}

.more__label {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  letter-spacing: 0.04em;
}

.more__button {
  margin-top: var(--space-2);
  padding: var(--space-1) 0;
  border: none;
  border-bottom: 1px solid var(--color-ink);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-sm);
  letter-spacing: 0.02em;
  transition: opacity var(--transition-fast);
}

.more__button:hover:not(:disabled) {
  opacity: 0.6;
}

.more__button:disabled {
  cursor: wait;
  opacity: 0.35;
}

.more__end {
  margin: var(--space-12) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  letter-spacing: 0.04em;
  text-align: center;
}

/* =========================================================
   TABLETTE
   ========================================================= */

@media (min-width: 600px) {
  .grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

/* =========================================================
   DESKTOP
   ========================================================= */

@media (min-width: 1001px) {
  .layout {
    display: grid;
    grid-template-columns: 220px minmax(0, 1fr);
    gap: var(--space-12);
    align-items: start;
  }

  .toolbar__filter {
    display: none;
  }

  .grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .intro {
    padding-block: var(--space-4) var(--space-5);
  }

  .intro__text {
    font-size: var(--text-sm);
  }

  .rayons {
    flex-wrap: nowrap;
    gap: var(--space-2);
    overflow-x: auto;
    scrollbar-width: none;
    margin-right: calc(var(--container-padding) * -1);
    padding-right: var(--container-padding);
  }

  .rayons::-webkit-scrollbar {
    display: none;
  }

  .rayons__link {
    flex-shrink: 0;
    height: 40px;
    padding-inline: var(--space-5);
    font-size: var(--text-base);
  }

  .toolbar__inner {
    min-height: 46px;
    gap: var(--space-2);
  }

  .toolbar__count {
    font-size: var(--text-xs);
  }

  .more__button {
    width: 100%;
    text-align: center;
  }
}
</style>
