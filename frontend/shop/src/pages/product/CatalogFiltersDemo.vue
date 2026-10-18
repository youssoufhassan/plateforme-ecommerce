<script setup lang="ts">
import { computed, ref } from "vue";

import { catalogCategories, categoryFilters } from "@/types/catalogFilters";
import { useCatalogFilters } from "@/services/useCatalogFilters";

import CatalogFiltersPanel from "@/components/catalog/CatalogFiltersPanel.vue";

const panelOpen = ref(false);

const {
  category,
  state,
  specificFilters,
  setCategory,
  setAttribute,
  toggleBrand,
  clearAll,
} = useCatalogFilters("chaussures");

const brands = [
  "Nike",
  "Adidas",
  "Puma",
  "New Balance",
  "Zara",
  "Lattafa",
  "Rasasi",
];

const flatCategories = computed(() =>
  catalogCategories.flatMap((item) => [item, ...(item.children ?? [])]),
);

const categoryLabel = computed(
  () =>
    flatCategories.value.find((item) => item.slug === category.value)?.name ??
    "Tous les produits",
);

function updatePriceMin(value: number | null) {
  state.minPrice = value;
}

function updatePriceMax(value: number | null) {
  state.maxPrice = value;
}

function updateAvailability(value: boolean) {
  state.availableOnly = value;
}

function updateSort(value: string) {
  state.sort = value;
}

function updateAttribute(payload: {
  key: string;
  value: string | string[] | number | null;
}) {
  setAttribute(payload.key, payload.value);
}

function changeCategory(event: Event) {
  setCategory((event.target as HTMLSelectElement).value);
}

const selectedFilters = computed(() => ({
  category: category.value,
  ...state,
}));
</script>

<template>
  <main class="catalog-demo">
    <header class="catalog-demo__header">
      <div>
        <p class="catalog-demo__eyebrow">SIDRA / BOUTIQUE</p>
        <h1>Explorer le catalogue</h1>
        <p>Une sélection pensée pour chaque univers.</p>
      </div>

      <button
        type="button"
        class="catalog-demo__open"
        @click="panelOpen = true"
      >
        Filtrer les produits
      </button>
    </header>

    <div class="catalog-demo__toolbar">
      <label>
        Univers
        <select :value="category" @change="changeCategory">
          <option value="">Tous les produits</option>
          <option
            v-for="item in flatCategories"
            :key="item.id"
            :value="item.slug"
          >
            {{ item.name }}
          </option>
        </select>
      </label>

      <p>
        Filtres spécifiques :
        <strong>{{ specificFilters.length }}</strong>
      </p>
    </div>

    <section class="catalog-demo__preview">
      <h2>État des filtres</h2>
      <p>
        Change de catégorie puis ouvre le panneau pour voir les champs propres à
        chaque univers.
      </p>
      <pre>{{ JSON.stringify(selectedFilters, null, 2) }}</pre>
    </section>

    <CatalogFiltersPanel
      :open="panelOpen"
      :category="category"
      :category-label="categoryLabel"
      :state="state"
      :brands="brands"
      :specific-filters="specificFilters"
      @close="panelOpen = false"
      @clear="clearAll"
      @update:min-price="updatePriceMin"
      @update:max-price="updatePriceMax"
      @update:available-only="updateAvailability"
      @toggle-brand="toggleBrand"
      @update:sort="updateSort"
      @update:attribute="updateAttribute"
    />
  </main>
</template>

<style scoped>
.catalog-demo {
  min-height: 100vh;
  padding: 48px;
  background: #faf9f6;
  color: #202020;
}

.catalog-demo__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 32px;
  border-bottom: 1px solid #e8e5df;
}

.catalog-demo__eyebrow {
  font-size: 10px;
  letter-spacing: 0.2em;
  color: #777;
}

.catalog-demo__header h1 {
  margin: 12px 0;
  font-size: clamp(28px, 4vw, 44px);
  font-weight: 400;
}

.catalog-demo__header p {
  color: #777;
}

.catalog-demo__open {
  min-height: 44px;
  padding: 0 18px;
  border: 1px solid #202020;
  background: #202020;
  color: white;
  cursor: pointer;
}

.catalog-demo__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 0;
}

.catalog-demo__toolbar label {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.catalog-demo__toolbar select {
  min-height: 40px;
  padding: 0 12px;
  border: 1px solid #e5e2dd;
  background: white;
}

.catalog-demo__toolbar p {
  font-size: 12px;
  color: #666;
}

.catalog-demo__preview {
  margin-top: 20px;
  padding: 24px;
  border: 1px solid #e8e5df;
  background: white;
}

.catalog-demo__preview h2 {
  font-size: 17px;
  font-weight: 500;
}

.catalog-demo__preview p {
  color: #777;
  font-size: 13px;
}

.catalog-demo__preview pre {
  max-height: 400px;
  overflow: auto;
  padding: 16px;
  background: #f6f5f2;
  font-size: 12px;
}

@media (max-width: 600px) {
  .catalog-demo {
    padding: 24px 16px;
  }

  .catalog-demo__header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
