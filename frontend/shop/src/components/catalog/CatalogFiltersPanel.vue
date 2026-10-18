<script setup lang="ts">
import { computed } from "vue";

import type { CatalogFilterState } from "@/types/catalog";

import CatalogGlobalFilters from "./CatalogGlobalFilters.vue";
import CatalogCategoryFilters from "./CatalogCategoryFilters.vue";

const props = defineProps<{
  open: boolean;
  category: string;
  state: CatalogFilterState;
  brands: string[];
  categoryLabel?: string;
  specificFilters: import("@/types/catalog").CategoryFilterDefinition[];
}>();

const emit = defineEmits<{
  close: [];
  clear: [];
  "update:minPrice": [value: number | null];
  "update:maxPrice": [value: number | null];
  "update:availableOnly": [value: boolean];
  "toggle-brand": [value: string];
  "update:sort": [value: string];
  "update:attribute": [
    payload: {
      key: string;
      value: string | string[] | number | null;
    },
  ];
}>();

const activeCount = computed(() => {
  let count = 0;

  if (props.state.minPrice !== null) count++;
  if (props.state.maxPrice !== null) count++;
  if (props.state.availableOnly) count++;
  count += props.state.brand.length;

  for (const value of Object.values(props.state.attributes)) {
    if (
      value !== null &&
      value !== "" &&
      (!Array.isArray(value) || value.length > 0)
    ) {
      count++;
    }
  }

  return count;
});
</script>

<template>
  <div v-if="open" class="catalog-filter-overlay" @click.self="emit('close')">
    <aside class="catalog-filter-panel" aria-label="Filtres du catalogue">
      <header class="catalog-filter-panel__header">
        <div>
          <p class="catalog-filter-panel__eyebrow">SIDRA</p>
          <h2>Filtres</h2>
          <p v-if="categoryLabel" class="catalog-filter-panel__category">
            {{ categoryLabel }}
          </p>
        </div>

        <button
          type="button"
          class="catalog-filter-panel__close"
          aria-label="Fermer"
          @click="emit('close')"
        >
          ✕
        </button>
      </header>

      <div class="catalog-filter-panel__body">
        <CatalogGlobalFilters
          :model-value="state"
          :brands="brands"
          @update:min-price="emit('update:minPrice', $event)"
          @update:max-price="emit('update:maxPrice', $event)"
          @update:available-only="emit('update:availableOnly', $event)"
          @toggle-brand="emit('toggle-brand', $event)"
          @update:sort="emit('update:sort', $event)"
        />

        <div class="catalog-filter-panel__divider" />

        <CatalogCategoryFilters
          :fields="specificFilters"
          :model-value="state.attributes"
          @update:attribute="emit('update:attribute', $event)"
        />
      </div>

      <footer class="catalog-filter-panel__footer">
        <button
          type="button"
          class="catalog-filter-panel__clear"
          @click="emit('clear')"
        >
          Tout effacer
          <span v-if="activeCount">({{ activeCount }})</span>
        </button>

        <button
          type="button"
          class="catalog-filter-panel__apply"
          @click="emit('close')"
        >
          Voir les résultats
        </button>
      </footer>
    </aside>
  </div>
</template>

<style scoped>
.catalog-filter-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
  background: rgb(0 0 0 / 38%);
}

.catalog-filter-panel {
  display: flex;
  flex-direction: column;
  width: min(420px, 100%);
  height: 100%;
  background: #fff;
  color: #202020;
  box-shadow: -8px 0 32px rgb(0 0 0 / 8%);
}

.catalog-filter-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px;
  border-bottom: 1px solid #eeeae4;
}

.catalog-filter-panel__eyebrow {
  margin: 0 0 7px;
  color: #777;
  font-size: 10px;
  letter-spacing: 0.2em;
}

.catalog-filter-panel__header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
}

.catalog-filter-panel__category {
  margin: 8px 0 0;
  color: #777;
  font-size: 12px;
}

.catalog-filter-panel__close {
  width: 34px;
  height: 34px;
  border: 1px solid #e8e5df;
  border-radius: 50%;
  background: #fff;
  cursor: pointer;
}

.catalog-filter-panel__body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.catalog-filter-panel__divider {
  height: 1px;
  margin: 24px 0;
  background: #eeeae4;
}

.catalog-filter-panel__footer {
  display: grid;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #eeeae4;
  background: #fff;
}

.catalog-filter-panel__clear {
  padding: 8px;
  border: 0;
  background: transparent;
  color: #555;
  font-size: 12px;
  text-decoration: underline;
  cursor: pointer;
}

.catalog-filter-panel__apply {
  min-height: 48px;
  border: 1px solid #202020;
  background: #202020;
  color: #fff;
  font-size: 12px;
  letter-spacing: 0.04em;
  cursor: pointer;
}

.catalog-filter-panel__apply:hover {
  background: #383838;
}

@media (max-width: 600px) {
  .catalog-filter-panel {
    width: 100%;
  }

  .catalog-filter-panel__header,
  .catalog-filter-panel__body {
    padding: 20px;
  }
}
</style>
