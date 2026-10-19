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
  CatalogFilterState,
  CategoryNode,
  CategoryFilterDefinition,
  CategoryFilterValue,
  SearchParams,
} from "@/types/catalog";

import { categoryFilters } from "@/types/catalogFilters";
import type { Product } from "@/types/product";

const PAGE_SIZE = 24;

const route = useRoute();
const router = useRouter();

const products = ref<Product[]>([]);
const filters = ref<CatalogFilters | null>(null);
const tree = ref<CategoryNode[]>([]);
const breadcrumb = ref<any[]>([]);

const loading = ref(false);
const loadingMore = ref(false);
const error = ref("");
const panelOpen = ref(false);

const totalElements = ref(0);
const hasNext = ref(false);
const currentPage = ref(0);

const filterState = ref<CatalogFilterState>({
  minPrice: null,
  maxPrice: null,
  availableOnly: false,
  brand: [],
  sort: "relevance",
  attributes: {},
});

const SORT_LABELS: Record<string, string> = {
  relevance: "Pertinence",
  newest: "Plus récents",
  price_asc: "Prix croissant",
  price_desc: "Prix décroissant",
  name: "Nom",
};

function queryString(value: unknown): string {
  if (Array.isArray(value)) return String(value[0] ?? "");
  if (value === null || value === undefined) return "";
  return String(value);
}

function queryNumber(value: unknown): number | undefined {
  const raw = queryString(value);
  if (!raw) return undefined;

  const number = Number(raw);
  return Number.isFinite(number) ? number : undefined;
}

function parseAttributes(): Record<string, CategoryFilterValue> {
  const attributes: Record<string, CategoryFilterValue> = {};

  for (const [key, rawValue] of Object.entries(route.query)) {
    if (!key.startsWith("attr_")) continue;

    const attributeKey = key.slice(5);
    const value = queryString(rawValue);

    if (attributeKey && value !== "") {
      attributes[attributeKey] = value;
    }
  }

  return attributes;
}

const activeParams = computed<SearchParams>(() => {
  const q = queryString(route.query.q);
  const category = queryString(route.query.category);
  const brand = queryString(route.query.brand);
  const minPrice = queryNumber(route.query.minPrice);
  const maxPrice = queryNumber(route.query.maxPrice);
  const availableOnly = queryString(route.query.availableOnly) === "true";
  const sort = queryString(route.query.sort) || "relevance";

  return {
    q: q || undefined,
    category: category || undefined,
    brand: brand || undefined,
    minPrice,
    maxPrice,
    availableOnly: availableOnly || undefined,
    sort,
    attributes: parseAttributes(),
  };
});

const currentCategory = computed(() => activeParams.value.category ?? "");

const categorySlug = computed(() => {
  const parts = currentCategory.value.split("/").filter(Boolean);
  return parts[parts.length - 1] ?? "";
});

const categoryName = computed(() => {
  const slug = categorySlug.value;
  if (!slug) return "";

  const nodes: CategoryNode[] = [];

  function collect(items: CategoryNode[]) {
    for (const item of items) {
      nodes.push(item);
      if (item.children?.length) collect(item.children);
    }
  }

  collect(tree.value);

  return (
    nodes.find(
      (item) => item.slug === slug || item.path === currentCategory.value,
    )?.name ?? slug.replace(/-/g, " ")
  );
});

const subCategories = computed(() => {
  function find(items: CategoryNode[]): CategoryNode | undefined {
    for (const item of items) {
      if (
        item.slug === categorySlug.value ||
        item.path === currentCategory.value
      ) {
        return item;
      }

      const child = find(item.children ?? []);
      if (child) return child;
    }

    return undefined;
  }

  return find(tree.value)?.children ?? [];
});

/**
 * Supprime le filtre Maison et garantit la présence du filtre Taille.
 * Les options sont des objets { label, value } pour correspondre au type
 * FilterOption[] utilisé par CategoryFilterDefinition.
 */
const specificFilters = computed<CategoryFilterDefinition[]>(() => {
  const definitions = categoryFilters[categorySlug.value] ?? [];

  const filtered = definitions.filter((filter) => {
    const label = String(filter.label ?? "")
      .trim()
      .toLowerCase();
    const key = String(filter.key ?? "")
      .trim()
      .toLowerCase();

    return label !== "maison" && key !== "maison";
  });

  const hasSizeFilter = filtered.some((filter) => {
    const label = String(filter.label ?? "")
      .trim()
      .toLowerCase();
    const key = String(filter.key ?? "")
      .trim()
      .toLowerCase();

    return label === "taille" || key === "taille" || key === "size";
  });

  if (!hasSizeFilter) {
    filtered.push({
      key: "taille",
      label: "Taille",
      type: "select",
      options: [
        { label: "XS", value: "XS" },
        { label: "S", value: "S" },
        { label: "M", value: "M" },
        { label: "L", value: "L" },
        { label: "XL", value: "XL" },
        { label: "XXL", value: "XXL" },
        ...Array.from({ length: 10 }, (_, index) => {
          const size = String(36 + index);
          return { label: size, value: size };
        }),
      ],
    });
  }

  return filtered;
});

const filterBrands = computed(() => filters.value?.brands ?? []);

const isSearch = computed(() => Boolean(activeParams.value.q));

const heading = computed(() => {
  if (isSearch.value) {
    return `Résultats pour « ${activeParams.value.q} »`;
  }

  return categoryName.value
    ? categoryName.value.charAt(0).toUpperCase() + categoryName.value.slice(1)
    : "Tous les produits";
});

const loadedRatio = computed(() => {
  if (totalElements.value === 0) return 0;

  return Math.min(100, (products.value.length / totalElements.value) * 100);
});

const activeChips = computed(() => {
  const chips: Array<{ key: string; label: string }> = [];
  const params = activeParams.value;

  if (params.minPrice !== undefined) {
    chips.push({ key: "minPrice", label: `Dès ${params.minPrice} €` });
  }

  if (params.maxPrice !== undefined) {
    chips.push({ key: "maxPrice", label: `Jusqu’à ${params.maxPrice} €` });
  }

  if (params.availableOnly) {
    chips.push({ key: "availableOnly", label: "En stock" });
  }

  if (params.brand) {
    for (const brand of params.brand.split(",").filter(Boolean)) {
      chips.push({ key: `brand:${brand}`, label: brand });
    }
  }

  for (const [key, value] of Object.entries(params.attributes ?? {})) {
    if (value !== null && value !== "") {
      chips.push({
        key: `attr_${key}`,
        label: `${key.replace(/-/g, " ")} : ${String(value)}`,
      });
    }
  }

  return chips;
});

function syncFilterState() {
  const params = activeParams.value;

  filterState.value = {
    minPrice: params.minPrice ?? null,
    maxPrice: params.maxPrice ?? null,
    availableOnly: params.availableOnly ?? false,
    brand: (params.brand ?? "").split(",").filter(Boolean),
    sort: params.sort ?? "relevance",
    attributes: { ...(params.attributes ?? {}) },
  };
}

function updateQuery(changes: Record<string, string | undefined>) {
  const query: Record<string, any> = { ...route.query };

  for (const [key, value] of Object.entries(changes)) {
    if (value === undefined || value === "") {
      delete query[key];
    } else {
      query[key] = value;
    }
  }

  return router.push({
    path: "/produits",
    query,
  });
}

function setMinPrice(value: number | null) {
  void updateQuery({
    minPrice: value === null ? undefined : String(value),
  });
}

function setMaxPrice(value: number | null) {
  void updateQuery({
    maxPrice: value === null ? undefined : String(value),
  });
}

function setAvailableOnly(value: boolean) {
  void updateQuery({
    availableOnly: value ? "true" : undefined,
  });
}

function setSort(value: string) {
  void updateQuery({
    sort: value === "relevance" ? undefined : value,
  });
}

function toggleBrand(brand: string) {
  const selected = [...filterState.value.brand];
  const index = selected.indexOf(brand);

  if (index >= 0) {
    selected.splice(index, 1);
  } else {
    selected.push(brand);
  }

  void updateQuery({
    brand: selected.length ? selected.join(",") : undefined,
  });
}

function setAttribute(payload: {
  key: string;
  value: string | string[] | number | null;
}) {
  const { key, value } = payload;

  const empty =
    value === null ||
    value === "" ||
    (Array.isArray(value) && value.length === 0);

  const serialized = Array.isArray(value)
    ? value.join(",")
    : value === null
      ? undefined
      : String(value);

  void updateQuery({
    [`attr_${key}`]: empty ? undefined : serialized,
  });
}

function removeChip(key: string) {
  if (key.startsWith("brand:")) {
    const brand = key.slice("brand:".length);
    const remaining = filterState.value.brand.filter((item) => item !== brand);

    void updateQuery({
      brand: remaining.length ? remaining.join(",") : undefined,
    });
    return;
  }

  void updateQuery({ [key]: undefined });
}

function clearFilters() {
  const query: Record<string, any> = {};

  if (route.query.q) query.q = route.query.q;

  if (route.query.sort && route.query.sort !== "relevance") {
    query.sort = route.query.sort;
  }

  void router.push({ path: "/produits", query });
}

async function loadProducts(append = false) {
  if (append) {
    loadingMore.value = true;
  } else {
    loading.value = true;
    error.value = "";
    currentPage.value = 0;
  }

  try {
    const page = append ? currentPage.value + 1 : 0;

    const result = await searchProducts({
      ...activeParams.value,
      page,
      size: PAGE_SIZE,
    });

    if (append) {
      products.value = [...products.value, ...(result.content ?? [])];
    } else {
      products.value = result.content ?? [];
    }

    currentPage.value = result.page ?? page;
    totalElements.value = result.totalElements ?? products.value.length;
    hasNext.value =
      result.hasNext ?? (result.page ?? page) + 1 < (result.totalPages ?? 0);
  } catch (err) {
    error.value = apiMessage(err, "ajout au panier à échoué");

    if (!append) {
      products.value = [];
      totalElements.value = 0;
      hasNext.value = false;
    }
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
}

async function loadCategoryContext() {
  const category = activeParams.value.category;

  if (!category) {
    breadcrumb.value = [];
    return;
  }

  try {
    const result = await fetchBreadcrumb(category);
    breadcrumb.value = Array.isArray(result) ? result : [];
  } catch {
    breadcrumb.value = [];
  }
}

async function loadFilters() {
  try {
    filters.value = await fetchCatalogFilters(activeParams.value.category);
  } catch {
    filters.value = null;
  }
}

async function loadMore() {
  if (!hasNext.value || loadingMore.value || loading.value) return;
  await loadProducts(true);
}

watch(
  () => route.query,
  async () => {
    syncFilterState();

    await Promise.all([loadProducts(), loadCategoryContext(), loadFilters()]);
  },
  { immediate: true },
);

async function initializeCategories() {
  try {
    tree.value = await fetchCategoryTree();
  } catch {
    tree.value = [];
  }
}

void initializeCategories();
</script>

<template>
  <main class="catalog-page">
    <div class="catalog-container">
      <header class="catalog-header">
        <nav
          v-if="breadcrumb.length"
          class="breadcrumbs"
          aria-label="Fil d’Ariane"
        >
          <RouterLink to="/">Accueil</RouterLink>

          <template
            v-for="(item, index) in breadcrumb"
            :key="item.slug ?? index"
          >
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>

            <RouterLink
              v-if="item.path && index < breadcrumb.length - 1"
              :to="`/produits?category=${encodeURIComponent(item.path)}`"
            >
              {{ item.name }}
            </RouterLink>

            <span v-else>{{ item.name }}</span>
          </template>
        </nav>

        <h1 class="catalog-title">{{ heading }}</h1>
      </header>

      <nav
        v-if="subCategories.length"
        class="category-list"
        aria-label="Sous-catégories"
      >
        <RouterLink
          v-for="category in subCategories"
          :key="category.id"
          class="category-chip"
          :to="`/produits?category=${encodeURIComponent(category.path || category.slug)}`"
        >
          {{ category.name }}
          <span v-if="category.productCount">
            ({{ category.productCount }})
          </span>
        </RouterLink>
      </nav>

      <section class="catalog-toolbar" aria-label="Outils du catalogue">
        <div class="catalog-toolbar__left">
          <button
            type="button"
            class="filter-button"
            :aria-expanded="panelOpen"
            aria-controls="catalog-filters"
            @click="panelOpen = !panelOpen"
          >
            <span aria-hidden="true">☷</span>
            Filtres

            <span v-if="activeChips.length" class="filter-count">
              {{ activeChips.length }}
            </span>
          </button>

          <span class="result-count">
            {{ totalElements }} résultat{{ totalElements > 1 ? "s" : "" }}
          </span>
        </div>

        <label class="sort-control">
          <span>Trier par</span>

          <select
            :value="filterState.sort"
            @change="setSort(($event.target as HTMLSelectElement).value)"
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
      </section>

      <div v-if="activeChips.length" class="active-filters">
        <button
          v-for="chip in activeChips"
          :key="chip.key"
          type="button"
          class="active-filter-chip"
          @click="removeChip(chip.key)"
        >
          {{ chip.label }}
          <span aria-hidden="true">×</span>
        </button>

        <button type="button" class="clear-filters" @click="clearFilters">
          Tout effacer
        </button>
      </div>

      <section class="catalog-layout">
        <CatalogFiltersPanel
          :open="panelOpen"
          :category="currentCategory"
          :state="filterState"
          :brands="filterBrands"
          :specific-filters="specificFilters"
          @update:min-price="setMinPrice"
          @update:max-price="setMaxPrice"
          @update:available-only="setAvailableOnly"
          @toggle-brand="toggleBrand"
          @update:sort="setSort"
          @update:attribute="setAttribute"
          @clear="clearFilters"
          @close="panelOpen = false"
        />

        <div class="catalog-results">
          <div
            v-if="loading && products.length === 0"
            class="product-grid"
            aria-label="Chargement des produits"
          >
            <div v-for="n in 8" :key="n" class="product-skeleton">
              <div class="product-skeleton__image"></div>
              <div class="product-skeleton__line"></div>
              <div
                class="product-skeleton__line product-skeleton__line--short"
              ></div>
            </div>
          </div>

          <div
            v-else-if="error && products.length === 0"
            class="catalog-message"
            role="alert"
          >
            <h2>Impossible de charger les produits</h2>
            <p>{{ error }}</p>

            <button
              type="button"
              class="primary-button"
              @click="loadProducts()"
            >
              Réessayer
            </button>
          </div>

          <div v-else-if="products.length === 0" class="catalog-message">
            <div class="empty-icon" aria-hidden="true">⌕</div>
            <h2>Aucun produit trouvé</h2>
            <p>
              Essaie de modifier tes filtres ou de choisir une autre catégorie.
            </p>

            <button type="button" class="primary-button" @click="clearFilters">
              Réinitialiser les filtres
            </button>
          </div>

          <template v-else>
            <div class="product-grid">
              <ProductCard
                v-for="product in products"
                :key="product.id"
                :product="product"
              />
            </div>

            <p v-if="error" class="inline-error" role="alert">
              {{ error }}
            </p>

            <div v-if="hasNext" class="load-more">
              <p class="load-more__count">
                {{ products.length }} sur {{ totalElements }} produits
              </p>

              <div
                class="progress-track"
                role="progressbar"
                :aria-valuenow="Math.round(loadedRatio)"
                aria-valuemin="0"
                aria-valuemax="100"
                :aria-label="`${Math.round(loadedRatio)} % des produits chargés`"
              >
                <div
                  class="progress-track__fill"
                  :style="{ width: `${loadedRatio}%` }"
                ></div>
              </div>

              <button
                type="button"
                class="load-more__button"
                :disabled="loadingMore"
                @click="loadMore"
              >
                {{ loadingMore ? "Chargement…" : "Afficher plus de produits" }}
              </button>
            </div>
          </template>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.catalog-page {
  min-height: 70vh;
  padding: 36px 0 64px;
  background: var(--color-page, #fff);
  color: var(--color-text, #191919);
}

.catalog-container {
  width: min(1440px, 100%);
  margin: 0 auto;
  padding: 0 32px;
}

.catalog-header {
  margin-bottom: 28px;
}

.breadcrumbs {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 9px;
  margin-bottom: 24px;
  color: #777;
  font-size: 13px;
}

.breadcrumbs a {
  color: inherit;
  text-decoration: none;
}

.breadcrumbs a:hover {
  color: #111;
}

.breadcrumbs__separator {
  color: #bbb;
}

.catalog-title {
  margin: 0;
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.035em;
  /* Couleur adoucie : au lieu d'hériter du noir pur (#000000) du thème */
  color: #191919;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 0 0 28px;
}

.category-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 15px;
  border: 1px solid #e6e6e6;
  border-radius: 999px;
  background: #fff;
  color: #333;
  font-size: 13px;
  text-decoration: none;
  transition:
    border-color 0.2s,
    background 0.2s;
}

.category-chip:hover {
  border-color: #222;
  background: #fafafa;
}

.category-chip span {
  color: #888;
  font-size: 12px;
}

.catalog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0;
  border-top: 1px solid #eaeaea;
  border-bottom: 1px solid #eaeaea;
}

.catalog-toolbar__left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  min-height: 42px;
  padding: 0 16px;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  background: #fff;
  color: #222;
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.filter-button:hover {
  border-color: #222;
}

.filter-button:focus-visible,
.sort-control select:focus-visible,
.active-filter-chip:focus-visible,
.clear-filters:focus-visible,
.primary-button:focus-visible,
.load-more__button:focus-visible {
  outline: 2px solid #222;
  outline-offset: 3px;
}

.filter-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #222;
  color: #fff;
  font-size: 11px;
}

.result-count {
  color: #777;
  font-size: 13px;
}

.sort-control {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #777;
  font-size: 13px;
}

.sort-control select {
  max-width: 190px;
  min-height: 40px;
  padding: 0 28px 0 10px;
  border: 1px solid #e2e2e2;
  border-radius: 7px;
  background: #fff;
  color: #222;
  font: inherit;
  cursor: pointer;
}

.active-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 16px 0 4px;
}

.active-filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 11px;
  border: 1px solid #e5e5e5;
  border-radius: 5px;
  background: #f8f8f8;
  color: #333;
  font: inherit;
  font-size: 12px;
  cursor: pointer;
}

.active-filter-chip span {
  color: #777;
  font-size: 17px;
  line-height: 12px;
}

.clear-filters {
  padding: 8px 4px;
  border: 0;
  background: transparent;
  color: #555;
  font: inherit;
  font-size: 12px;
  text-decoration: underline;
  cursor: pointer;
}

/* Mise en page générale */
.catalog-layout {
  display: grid;
  grid-template-columns: minmax(220px, 260px) minmax(0, 1fr);
  align-items: start;
  gap: 28px;
  min-width: 0;
  margin-top: 24px;
}

.catalog-results {
  min-width: 0;
  width: 100%;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 28px 18px;
}

.product-skeleton {
  min-width: 0;
}

.product-skeleton__image {
  aspect-ratio: 3 / 4;
  border-radius: 8px;
  background: linear-gradient(100deg, #f1f1f1 20%, #f8f8f8 40%, #f1f1f1 60%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s infinite linear;
}

.product-skeleton__line {
  width: 80%;
  height: 12px;
  margin-top: 13px;
  border-radius: 4px;
  background: #eee;
}

.product-skeleton__line--short {
  width: 45%;
  margin-top: 8px;
}

@keyframes skeleton-shimmer {
  to {
    background-position-x: -200%;
  }
}

.catalog-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 40px 20px;
  text-align: center;
}

.catalog-message h2 {
  margin: 12px 0 8px;
  font-size: 22px;
}

.catalog-message p {
  max-width: 440px;
  margin: 0 0 20px;
  color: #777;
  font-size: 14px;
  line-height: 1.6;
}

.empty-icon {
  color: #999;
  font-size: 46px;
}

.primary-button {
  min-height: 44px;
  padding: 0 20px;
  border: 0;
  border-radius: 7px;
  background: #191919;
  color: #fff;
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.primary-button:hover {
  background: #383838;
}

.inline-error {
  margin: 16px 0;
  color: #b42318;
  font-size: 13px;
}

.load-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  max-width: 320px;
  margin: 44px auto 0;
}

.load-more__count {
  margin: 0;
  color: #777;
  font-size: 12px;
}

.progress-track {
  width: 100%;
  height: 3px;
  overflow: hidden;
  border-radius: 3px;
  background: #e8e8e8;
}

.progress-track__fill {
  height: 100%;
  background: #222;
  transition: width 0.25s ease;
}

.load-more__button {
  min-height: 44px;
  padding: 0 22px;
  border: 1px solid #222;
  border-radius: 7px;
  background: #fff;
  color: #222;
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.load-more__button:hover:not(:disabled) {
  background: #222;
  color: #fff;
}

.load-more__button:disabled {
  opacity: 0.55;
  cursor: wait;
}

@media (min-width: 1500px) {
  .product-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .catalog-container {
    padding: 0 24px;
  }

  .catalog-layout {
    grid-template-columns: minmax(200px, 230px) minmax(0, 1fr);
    gap: 20px;
  }

  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 24px 15px;
  }
}

@media (max-width: 700px) {
  .catalog-page {
    padding: 24px 0 48px;
  }

  .catalog-container {
    padding: 0 16px;
  }

  .catalog-header {
    margin-bottom: 22px;
  }

  .breadcrumbs {
    margin-bottom: 18px;
  }

  .catalog-title {
    font-size: 30px;
  }

  .category-list {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 5px;
    scrollbar-width: thin;
  }

  .category-chip {
    flex-shrink: 0;
  }

  .catalog-toolbar {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .catalog-toolbar__left {
    flex: 1;
    flex-wrap: wrap;
    gap: 10px;
  }

  .sort-control {
    width: 100%;
    justify-content: space-between;
  }

  .sort-control select {
    flex: 1;
    max-width: 240px;
  }

  /*
   * Sur mobile, le panneau est géré comme un tiroir par
   * CatalogFiltersPanel. La grille ne réserve pas de colonne aux filtres.
   */
  .catalog-layout {
    display: block;
    margin-top: 18px;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 22px 12px;
  }

  .catalog-message {
    min-height: 250px;
  }
}

@media (max-width: 360px) {
  .catalog-container {
    padding: 0 12px;
  }

  .catalog-title {
    font-size: 26px;
  }

  .product-grid {
    gap: 18px 9px;
  }

  .filter-button {
    padding: 0 11px;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
