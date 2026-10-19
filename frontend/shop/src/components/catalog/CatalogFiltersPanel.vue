<script setup lang="ts">
import { computed } from "vue";
import type {
  CatalogFilterState,
  CategoryFilterDefinition,
} from "@/types/catalog";

import CatalogGlobalFilters from "./CatalogGlobalFilters.vue";
import CatalogCategoryFilters from "./CatalogCategoryFilters.vue";

const props = defineProps<{
  open: boolean;
  category: string;
  state: CatalogFilterState;
  brands: string[];
  specificFilters: CategoryFilterDefinition[];
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

const visibleFilters = computed(() =>
  props.specificFilters.filter((filter) => {
    const label = String(filter.label ?? "").toLowerCase();
    const key = String(filter.key ?? "").toLowerCase();

    return label !== "maison" && key !== "maison";
  }),
);

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
  <!--
    Le wrapper est TOUJOURS monté dans le DOM.
    - Mobile  : caché par défaut, affiché quand `open === true` (drawer).
    - Desktop : toujours visible en statique, sert de colonne latérale.
  -->
  <div
    class="catalog-filter-overlay"
    :class="{ 'is-open': open }"
    @click.self="emit('close')"
  >
    <aside
      id="catalog-filter-panel"
      class="catalog-filter-panel"
      role="dialog"
      aria-modal="true"
      aria-label="Filtres"
    >
      <header class="catalog-filter-panel__header">
        <h2>Filtres</h2>

        <button
          type="button"
          class="catalog-filter-panel__close"
          aria-label="Fermer les filtres"
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

        <div class="catalog-filter-panel__divider" aria-hidden="true" />

        <CatalogCategoryFilters
          :fields="visibleFilters"
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
/* ============================================================
   ÉTAT PAR DÉFAUT = MOBILE (drawer)
   ============================================================ */
.catalog-filter-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: none;
  justify-content: flex-end;
  background: rgb(0 0 0 / 38%);
}

.catalog-filter-overlay.is-open {
  display: flex;
}

.catalog-filter-panel {
  display: flex;
  flex-direction: column;
  width: min(420px, 100%);
  height: 100%;
  min-width: 0;
  background: #fff;
  color: #202020;
  box-shadow: -8px 0 32px rgb(0 0 0 / 8%);
}

.catalog-filter-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
  border-bottom: 1px solid #eeeae4;
}

.catalog-filter-panel__header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
  /* Couleur figée pour ne plus hériter du #000000 pur du thème global */
  color: #202020;
}

.catalog-filter-panel__close {
  display: grid;
  flex: 0 0 auto;
  place-items: center;
  width: 34px;
  height: 34px;
  border: 1px solid #e8e5df;
  border-radius: 50%;
  background: #fff;
  color: inherit;
  cursor: pointer;
}

.catalog-filter-panel__body {
  flex: 1;
  min-height: 0;
  padding: 24px;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.catalog-filter-panel__divider {
  height: 1px;
  margin: 24px 0;
  background: #eeeae4;
}

.catalog-filter-panel__footer {
  display: grid;
  flex: 0 0 auto;
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

/* ============================================================
   DESKTOP (>= 1001px) : panneau toujours visible, en colonne
   ============================================================ */
@media (min-width: 1001px) {
  .catalog-filter-overlay {
    position: static;
    inset: auto;
    z-index: auto;
    display: block !important;
    min-width: 0;
    background: transparent;
  }

  .catalog-filter-panel {
    position: static;
    width: 100%;
    height: auto;
    box-shadow: none;
    background: transparent;
    color: inherit;
  }

  .catalog-filter-panel__header {
    padding: 0 0 20px;
  }

  .catalog-filter-panel__body {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    padding: 20px 0;
    overflow: visible;
  }

  .catalog-filter-panel__divider {
    margin: 4px 0;
  }

  .catalog-filter-panel__footer {
    padding: 16px 0 0;
    background: transparent;
  }

  /* En desktop, on n'a pas besoin de bouton fermer ni du bouton
     "Voir les résultats" : le panneau est déjà visible. */
  .catalog-filter-panel__close {
    display: none;
  }
}

/* ============================================================
   MOBILE : petites retouches de padding
   ============================================================ */
@media (max-width: 600px) {
  .catalog-filter-panel {
    width: min(420px, 100%);
  }

  .catalog-filter-panel__header,
  .catalog-filter-panel__body {
    padding: 20px;
  }

  .catalog-filter-panel__footer {
    padding: 16px 20px;
  }
}
</style>
