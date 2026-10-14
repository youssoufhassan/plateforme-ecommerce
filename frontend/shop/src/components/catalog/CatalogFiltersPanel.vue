<script setup lang="ts">
import { computed, ref, watch } from "vue";

import type {
  CatalogFilters,
  CategoryNode,
  SearchParams,
} from "@/types/catalog";

const props = defineProps<{
  filters: CatalogFilters | null;
  categories: CategoryNode[];
  active: SearchParams;
  open: boolean;
}>();

const emit = defineEmits<{
  update: [changes: Record<string, string | undefined>];
  clear: [];
  close: [];
}>();

const BRAND_PREVIEW = 10;

/* Tranches de prix : plus rapides qu'un formulaire, plus élégantes. */
const PRICE_BANDS: Array<{
  key: string;
  label: string;
  min?: number;
  max?: number;
}> = [
  { key: "0-50", label: "Moins de 50 €", max: 50 },
  { key: "50-100", label: "50 € – 100 €", min: 50, max: 100 },
  { key: "100-200", label: "100 € – 200 €", min: 100, max: 200 },
  { key: "200-500", label: "200 € – 500 €", min: 200, max: 500 },
  { key: "500+", label: "Plus de 500 €", min: 500 },
];

const brandQuery = ref("");
const showAllBrands = ref(false);

const openGroups = ref<Record<string, boolean>>({
  brand: true,
  price: true,
});

function toggleGroup(key: string): void {
  openGroups.value[key] = !openGroups.value[key];
}

/* Marques : recherche locale uniquement si la liste est longue. */
const filteredBrands = computed(() => {
  const all = props.filters?.brands ?? [];
  const query = brandQuery.value.trim().toLowerCase();

  return query ? all.filter((b) => b.toLowerCase().includes(query)) : all;
});

const visibleBrands = computed(() =>
  showAllBrands.value || brandQuery.value
    ? filteredBrands.value
    : filteredBrands.value.slice(0, BRAND_PREVIEW),
);

const hiddenBrandCount = computed(() =>
  Math.max(0, filteredBrands.value.length - BRAND_PREVIEW),
);

/* Tranche active : on la déduit des bornes en cours. */
const activePriceBand = computed(() => {
  const { minPrice, maxPrice } = props.active;
  if (minPrice == null && maxPrice == null) return null;

  return (
    PRICE_BANDS.find((band) => band.min === minPrice && band.max === maxPrice)
      ?.key ?? null
  );
});

const hasActiveFilters = computed(() =>
  Boolean(
    props.active.brand ||
    props.active.minPrice != null ||
    props.active.maxPrice != null ||
    props.active.availableOnly,
  ),
);

function toggleBrand(value: string): void {
  emit("update", { brand: props.active.brand === value ? undefined : value });
}

function selectPriceBand(band: (typeof PRICE_BANDS)[number]): void {
  const isActive = activePriceBand.value === band.key;

  emit("update", {
    minPrice: isActive ? undefined : band.min?.toString(),
    maxPrice: isActive ? undefined : band.max?.toString(),
  });
}

function toggleAvailable(event: Event): void {
  const checked = (event.target as HTMLInputElement).checked;
  emit("update", { availableOnly: checked ? "true" : undefined });
}

/* Changer de rayon renouvelle la liste : la recherche repart à zéro. */
watch(
  () => props.filters,
  () => {
    brandQuery.value = "";
    showAllBrands.value = false;
  },
);
</script>

<template>
  <div v-if="open" class="backdrop" @click="emit('close')" />

  <aside
    class="filters"
    :class="{ 'filters--open': open }"
    aria-label="Filtres"
  >
    <header class="filters__head">
      <h2 class="filters__title">Filtrer</h2>

      <button
        type="button"
        class="filters__close"
        aria-label="Fermer les filtres"
        @click="emit('close')"
      >
        <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path
            d="M5 5l10 10M15 5L5 15"
            stroke="currentColor"
            stroke-width="1.2"
            stroke-linecap="round"
          />
        </svg>
      </button>
    </header>

    <div class="filters__body">
      <!-- Disponibilité : une ligne discrète en tête -->
      <section class="group group--plain">
        <label class="check">
          <input
            type="checkbox"
            :checked="!!active.availableOnly"
            @change="toggleAvailable"
          />
          <span>En stock uniquement</span>
        </label>
      </section>

      <!-- Prix : tranches cliquables -->
      <section class="group">
        <button
          type="button"
          class="group__head"
          :aria-expanded="openGroups.price"
          @click="toggleGroup('price')"
        >
          <span class="group__label">Prix</span>

          <svg
            class="group__chevron"
            :class="{ 'group__chevron--open': openGroups.price }"
            viewBox="0 0 20 20"
            fill="none"
            aria-hidden="true"
          >
            <path
              d="M5 8l5 5 5-5"
              stroke="currentColor"
              stroke-width="1.2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>

        <ul v-show="openGroups.price" class="bands">
          <li v-for="band in PRICE_BANDS" :key="band.key">
            <button
              type="button"
              class="band"
              :class="{ 'band--active': activePriceBand === band.key }"
              @click="selectPriceBand(band)"
            >
              {{ band.label }}
            </button>
          </li>
        </ul>
      </section>

      <!-- Maisons : liste typographique dense -->
      <section v-if="filters?.brands?.length" class="group">
        <button
          type="button"
          class="group__head"
          :aria-expanded="openGroups.brand"
          @click="toggleGroup('brand')"
        >
          <span class="group__label">
            Maisons
            <span class="group__count numeric">
              {{ filters.brands.length }}
            </span>
          </span>

          <svg
            class="group__chevron"
            :class="{ 'group__chevron--open': openGroups.brand }"
            viewBox="0 0 20 20"
            fill="none"
            aria-hidden="true"
          >
            <path
              d="M5 8l5 5 5-5"
              stroke="currentColor"
              stroke-width="1.2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>

        <div v-show="openGroups.brand" class="group__body">
          <input
            v-if="filters.brands.length > BRAND_PREVIEW"
            v-model="brandQuery"
            type="search"
            class="search"
            placeholder="Rechercher une maison"
            aria-label="Rechercher une maison"
          />

          <p v-if="filteredBrands.length === 0" class="empty">
            Aucune maison ne correspond.
          </p>

          <template v-else>
            <ul class="brands">
              <li v-for="brand in visibleBrands" :key="brand">
                <button
                  type="button"
                  class="brand"
                  :class="{ 'brand--active': active.brand === brand }"
                  @click="toggleBrand(brand)"
                >
                  {{ brand }}
                </button>
              </li>
            </ul>

            <button
              v-if="!brandQuery && hiddenBrandCount > 0"
              type="button"
              class="more-link"
              @click="showAllBrands = !showAllBrands"
            >
              {{
                showAllBrands
                  ? "Voir moins"
                  : `Voir les ${hiddenBrandCount} autres`
              }}
            </button>
          </template>
        </div>
      </section>

      <button
        v-if="hasActiveFilters"
        type="button"
        class="clear"
        @click="emit('clear')"
      >
        Tout effacer
      </button>
    </div>

    <footer class="filters__foot">
      <button type="button" class="filters__done" @click="emit('close')">
        Voir les résultats
      </button>
    </footer>
  </aside>
</template>

<style scoped>
/* =========================================================
   MOBILE — panneau latéral
   ========================================================= */

.backdrop {
  position: fixed;
  inset: 0;
  z-index: var(--z-overlay);
  background: rgb(0 0 0 / 38%);
}

.filters {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: var(--z-menu);

  display: flex;
  flex-direction: column;

  width: min(360px, 90vw);
  height: 100dvh;

  background: var(--color-paper);

  transform: translateX(-105%);
  transition: transform var(--transition-base);
}

.filters--open {
  transform: translateX(0);
}

.filters__head {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
  padding-inline: var(--space-5);
  border-bottom: 1px solid var(--color-line);
}

.filters__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--weight-regular);
  letter-spacing: -0.01em;
}

.filters__close {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
}

.filters__close svg {
  width: 16px;
  height: 16px;
}

.filters__body {
  flex: 1;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: var(--space-5) var(--space-5) var(--space-10);
}

.filters__foot {
  flex-shrink: 0;
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--color-line);
}

.filters__done {
  width: 100%;
  height: 46px;
  border: none;
  background: var(--color-ink);
  cursor: pointer;
  color: var(--color-text-inverse);
  font-family: inherit;
  font-size: var(--text-base);
  letter-spacing: 0.02em;
}

/* =========================================================
   GROUPES — un filet, jamais deux
   ========================================================= */

.group {
  padding-bottom: var(--space-5);
  margin-bottom: var(--space-5);
  border-bottom: 1px solid var(--color-line);
}

.group--plain {
  padding-bottom: var(--space-4);
  margin-bottom: var(--space-4);
}

.group:last-of-type {
  border-bottom: none;
}

.group__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);

  width: 100%;
  padding: 0 0 var(--space-3);

  border: none;
  background: transparent;
  cursor: pointer;

  font-family: inherit;
  text-align: left;
}

.group__label {
  display: inline-flex;
  align-items: baseline;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.group__count {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: var(--weight-regular);
  letter-spacing: normal;
  text-transform: none;
}

.group__chevron {
  flex-shrink: 0;
  width: 14px;
  height: 14px;
  color: var(--color-text-muted);
  transition: transform var(--transition-fast);
}

.group__chevron--open {
  transform: rotate(180deg);
}

.group__body {
  display: flex;
  flex-direction: column;
  max-height: 340px;
  overflow-y: auto;
  overscroll-behavior: contain;
}

/* =========================================================
   DISPONIBILITÉ
   ========================================================= */

.check {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
  color: var(--color-text);
  font-size: var(--text-sm);
}

.check input {
  width: 15px;
  height: 15px;
  accent-color: var(--color-ink);
  cursor: pointer;
}

/* =========================================================
   TRANCHES DE PRIX
   ========================================================= */

.bands {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.band {
  width: 100%;
  padding: var(--space-1) 0;
  border: none;
  background: transparent;
  cursor: pointer;

  color: var(--color-text-muted);
  font-family: inherit;
  font-size: var(--text-sm);
  text-align: left;

  transition: color var(--transition-fast);
}

.band:hover {
  color: var(--color-text);
}

.band--active {
  color: var(--color-text);
  font-weight: var(--weight-medium);
}

/* =========================================================
   MAISONS — liste typographique
   ========================================================= */

.search {
  width: 100%;
  height: 34px;
  margin-bottom: var(--space-3);
  padding: 0;

  border: none;
  border-bottom: 1px solid var(--color-line);
  background: transparent;

  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-sm);
}

.search:focus {
  border-bottom-color: var(--color-ink);
  outline: none;
}

.brands {
  display: flex;
  flex-direction: column;
  gap: 1px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.brand {
  width: 100%;
  padding: var(--space-1) 0;
  border: none;
  background: transparent;
  cursor: pointer;

  color: var(--color-text-secondary);
  font-family: inherit;
  font-size: var(--text-sm);
  text-align: left;

  transition: color var(--transition-fast);
}

.brand:hover {
  color: var(--color-text);
}

.brand--active {
  color: var(--color-text);
  font-weight: var(--weight-medium);
}

.empty {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.more-link {
  align-self: flex-start;
  margin-top: var(--space-3);
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: var(--text-xs);
  text-decoration: underline;
  text-underline-offset: 0.25em;
}

.more-link:hover {
  color: var(--color-text);
}

.clear {
  align-self: flex-start;
  margin-top: var(--space-4);
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: var(--text-xs);
  text-decoration: underline;
  text-underline-offset: 0.25em;
}

.clear:hover {
  color: var(--color-text);
}

/* =========================================================
   DESKTOP — colonne silencieuse
   ========================================================= */

@media (min-width: 1001px) {
  .backdrop,
  .filters__head,
  .filters__foot {
    display: none;
  }

  .filters {
    position: sticky;
    inset: auto;
    top: calc(var(--header-height) + var(--space-12));

    display: block;
    width: 100%;
    height: auto;
    max-height: calc(100vh - var(--header-height) - var(--space-16));
    overflow-y: auto;

    background: transparent;

    transform: none;
    transition: none;
  }

  .filters__body {
    padding: 0;
    overflow: visible;
  }
}

@media (prefers-reduced-motion: reduce) {
  .filters,
  .group__chevron,
  .band,
  .brand {
    transition: none;
  }
}
</style>
