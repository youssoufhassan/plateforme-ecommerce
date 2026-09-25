<script setup lang="ts">
import { computed, ref, watch } from "vue";

import type { CatalogFilters, SearchParams } from "@/types/catalog";

const props = defineProps<{
  filters: CatalogFilters | null;
  active: SearchParams;
  open: boolean;
}>();

const emit = defineEmits<{
  update: [changes: Record<string, string | undefined>];
  clear: [];
  close: [];
}>();

/** Nombre de marques affichées avant le bouton « Voir plus ». */
const BRAND_PREVIEW = 8;

/**
 * Filtres prévus mais pas encore exploitables : le backend ne renvoie
 * ni les contenances ni les familles olfactives. Affichés désactivés
 * pour valider la mise en page, à activer dès que les données existent.
 */
const UPCOMING_FILTERS = [
  {
    key: "size",
    label: "Contenance",
    options: ["30 ml", "50 ml", "100 ml", "Coffret"],
  },
  {
    key: "family",
    label: "Famille olfactive",
    options: ["Oriental", "Boisé", "Floral", "Frais", "Gourmand"],
  },
];

const brandQuery = ref("");
const showAllBrands = ref(false);

const minPrice = ref<string>("");
const maxPrice = ref<string>("");

// Sections repliables : les plus utilisées sont ouvertes par défaut
const openGroups = ref<Record<string, boolean>>({
  category: true,
  brand: true,
  price: true,
});

function toggleGroup(key: string): void {
  openGroups.value[key] = !openGroups.value[key];
}

watch(
  () => props.active,
  (value) => {
    minPrice.value = value.minPrice?.toString() ?? "";
    maxPrice.value = value.maxPrice?.toString() ?? "";
  },
  { immediate: true, deep: true },
);

/** Marques filtrées par la recherche, puis limitées tant que « Voir plus » n'est pas cliqué. */
const filteredBrands = computed(() => {
  const all = props.filters?.brands ?? [];
  const query = brandQuery.value.trim().toLowerCase();

  return query
    ? all.filter((brand) => brand.toLowerCase().includes(query))
    : all;
});

const visibleBrands = computed(() =>
  showAllBrands.value || brandQuery.value
    ? filteredBrands.value
    : filteredBrands.value.slice(0, BRAND_PREVIEW),
);

const hiddenBrandCount = computed(() =>
  Math.max(0, filteredBrands.value.length - BRAND_PREVIEW),
);

const hasActiveFilters = computed(() =>
  Boolean(
    props.active.category ||
    props.active.brand ||
    props.active.minPrice ||
    props.active.maxPrice ||
    props.active.availableOnly,
  ),
);

/** Cliquer sur un filtre déjà actif le retire. */
function toggle(key: "category" | "brand", value: string): void {
  emit("update", { [key]: props.active[key] === value ? undefined : value });
}

function applyPrice(): void {
  const min = minPrice.value.trim();
  const max = maxPrice.value.trim();

  if (min && max && Number(min) > Number(max)) {
    return; // fourchette incohérente : on n'applique rien
  }

  emit("update", { minPrice: min || undefined, maxPrice: max || undefined });
}

function toggleAvailable(event: Event): void {
  const checked = (event.target as HTMLInputElement).checked;
  emit("update", { availableOnly: checked ? "true" : undefined });
}
</script>

<template>
  <!-- Voile, uniquement en mobile -->
  <div v-if="open" class="filters__backdrop" @click="emit('close')"></div>

  <aside class="filters" :class="{ 'filters--open': open }">
    <div class="filters__header">
      <h2 class="filters__heading">Filtres</h2>

      <button
        type="button"
        class="filters__close"
        aria-label="Fermer les filtres"
        @click="emit('close')"
      >
        ✕
      </button>
    </div>

    <div class="filters__body">
      <!-- Disponibilité -->
      <section class="filters__group">
        <label class="filters__checkbox">
          <input
            type="checkbox"
            :checked="!!active.availableOnly"
            @change="toggleAvailable"
          />
          <span>Uniquement les produits disponibles</span>
        </label>
      </section>

      <!-- Catégories -->
      <section v-if="filters?.categories?.length" class="filters__group">
        <button
          type="button"
          class="filters__toggle"
          :aria-expanded="openGroups.category"
          @click="toggleGroup('category')"
        >
          <span class="filters__label">Catégorie</span>
          <span
            class="filters__chevron"
            :class="{ 'filters__chevron--open': openGroups.category }"
          >
            ⌄
          </span>
        </button>

        <ul v-show="openGroups.category" class="filters__list">
          <li v-for="category in filters.categories" :key="category">
            <button
              type="button"
              class="filters__option"
              :class="{
                'filters__option--active': active.category === category,
              }"
              @click="toggle('category', category)"
            >
              {{ category }}
            </button>
          </li>
        </ul>
      </section>

      <!-- Marques -->
      <section v-if="filters?.brands?.length" class="filters__group">
        <button
          type="button"
          class="filters__toggle"
          :aria-expanded="openGroups.brand"
          @click="toggleGroup('brand')"
        >
          <span class="filters__label">
            Marque
            <span class="filters__count">({{ filters.brands.length }})</span>
          </span>
          <span
            class="filters__chevron"
            :class="{ 'filters__chevron--open': openGroups.brand }"
          >
            ⌄
          </span>
        </button>

        <div v-show="openGroups.brand">
          <!-- Recherche : indispensable dès que les marques se multiplient -->
          <input
            v-model="brandQuery"
            type="search"
            class="filters__search"
            placeholder="Rechercher une marque"
            aria-label="Rechercher une marque"
          />

          <p v-if="filteredBrands.length === 0" class="filters__empty">
            Aucune marque ne correspond.
          </p>

          <ul v-else class="filters__list">
            <li v-for="brand in visibleBrands" :key="brand">
              <button
                type="button"
                class="filters__option"
                :class="{ 'filters__option--active': active.brand === brand }"
                @click="toggle('brand', brand)"
              >
                {{ brand }}
              </button>
            </li>
          </ul>

          <button
            v-if="!brandQuery && hiddenBrandCount > 0"
            type="button"
            class="filters__show-more"
            @click="showAllBrands = !showAllBrands"
          >
            {{
              showAllBrands ? "Voir moins" : `Voir plus (${hiddenBrandCount})`
            }}
          </button>
        </div>
      </section>

      <!-- Prix -->
      <section class="filters__group">
        <button
          type="button"
          class="filters__toggle"
          :aria-expanded="openGroups.price"
          @click="toggleGroup('price')"
        >
          <span class="filters__label">Prix</span>
          <span
            class="filters__chevron"
            :class="{ 'filters__chevron--open': openGroups.price }"
          >
            ⌄
          </span>
        </button>

        <div v-show="openGroups.price">
          <div class="filters__price">
            <input
              v-model="minPrice"
              type="number"
              min="0"
              step="1"
              :placeholder="
                filters?.minPrice != null
                  ? `${Math.floor(filters.minPrice)}`
                  : 'Min'
              "
              aria-label="Prix minimum"
              @keyup.enter="applyPrice"
            />

            <span class="filters__price-separator">—</span>

            <input
              v-model="maxPrice"
              type="number"
              min="0"
              step="1"
              :placeholder="
                filters?.maxPrice != null
                  ? `${Math.ceil(filters.maxPrice)}`
                  : 'Max'
              "
              aria-label="Prix maximum"
              @keyup.enter="applyPrice"
            />
          </div>

          <button type="button" class="filters__apply" @click="applyPrice">
            Appliquer
          </button>
        </div>
      </section>

      <!-- Filtres à venir : visibles, désactivés tant que le backend ne les fournit pas -->
      <section
        v-for="group in UPCOMING_FILTERS"
        :key="group.key"
        class="filters__group filters__group--upcoming"
      >
        <div class="filters__toggle filters__toggle--static">
          <span class="filters__label">{{ group.label }}</span>
          <span class="filters__soon">Bientôt</span>
        </div>

        <ul class="filters__list">
          <li v-for="option in group.options" :key="option">
            <button type="button" class="filters__option" disabled>
              {{ option }}
            </button>
          </li>
        </ul>
      </section>

      <button
        v-if="hasActiveFilters"
        type="button"
        class="filters__clear"
        @click="emit('clear')"
      >
        Effacer les filtres
      </button>
    </div>

    <!-- Validation, uniquement en mobile -->
    <div class="filters__footer">
      <button type="button" class="filters__done" @click="emit('close')">
        Voir les résultats
      </button>
    </div>
  </aside>
</template>

<style scoped>
/* =========================================================
   MOBILE : panneau latéral
   ========================================================= */

.filters__backdrop {
  position: fixed;
  inset: 0;
  z-index: var(--z-overlay);
  background: rgba(0, 0, 0, 0.42);
}

.filters {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: var(--z-menu);

  display: flex;
  flex-direction: column;

  width: min(360px, 88vw);
  height: 100vh;
  height: 100dvh;

  background: var(--color-white);
  box-shadow: 12px 0 40px rgba(0, 0, 0, 0.12);

  transform: translateX(-100%);
  transition: transform var(--transition-base);
}

.filters--open {
  transform: translateX(0);
}

.filters__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  height: var(--header-height);
  padding-inline: 20px;
  border-bottom: 1px solid var(--color-border);
}

.filters__heading {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.filters__close {
  width: 38px;
  height: 38px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-size: 16px;
}

.filters__body {
  flex: 1;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 24px 20px 32px;
}

.filters__footer {
  flex-shrink: 0;
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}

.filters__done {
  width: 100%;
  padding: 14px;
  border: none;
  background: var(--color-black);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

/* =========================================================
   GROUPES
   ========================================================= */

.filters__group {
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--color-border);
}

.filters__group:last-of-type {
  border-bottom: none;
}

.filters__toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 0 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
}

.filters__label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.filters__count {
  font-weight: 400;
  letter-spacing: 0;
}

.filters__chevron {
  color: var(--color-text-muted);
  font-size: 14px;
  line-height: 1;
  transition: transform var(--transition-fast);
}

.filters__chevron--open {
  transform: rotate(180deg);
}

/* =========================================================
   RECHERCHE DE MARQUE
   ========================================================= */

.filters__search {
  width: 100%;
  height: 34px;
  margin-bottom: 10px;
  padding-inline: 10px;
  border: 1px solid var(--color-border);
  background: #f7f6f3;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
}

.filters__search:focus {
  border-color: var(--color-border-strong);
  background: var(--color-white);
  outline: none;
}

.filters__empty {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
}

.filters__show-more {
  margin-top: 8px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 11px;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.filters__show-more:hover {
  opacity: 0.65;
}

/* =========================================================
   OPTIONS
   ========================================================= */

.filters__list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 0;
  padding: 0;
  /* Liste longue : on la limite pour garder la colonne lisible */
  max-height: 260px;
  overflow-y: auto;
  overscroll-behavior: contain;
  list-style: none;
}

.filters__option {
  width: 100%;
  padding: 7px 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 13px;
  text-align: left;
  transition:
    opacity var(--transition-fast),
    padding-left var(--transition-fast);
}

.filters__option:hover {
  padding-left: 5px;
  opacity: 0.65;
}

.filters__option--active {
  font-weight: 600;
  text-decoration: underline;
  text-underline-offset: 4px;
}

.filters__option:disabled {
  cursor: default;
}

.filters__checkbox {
  display: flex;
  align-items: center;
  gap: 9px;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text);
}

.filters__checkbox input {
  width: 15px;
  height: 15px;
  accent-color: var(--color-black);
  cursor: pointer;
}

/* =========================================================
   PRIX
   ========================================================= */

.filters__price {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filters__price input {
  width: 100%;
  min-width: 0;
  height: 36px;
  padding-inline: 10px;
  border: 1px solid var(--color-border);
  background: var(--color-white);
  color: var(--color-text);
  font-family: inherit;
  font-size: 13px;
}

.filters__price input:focus {
  border-color: var(--color-border-strong);
  outline: none;
}

.filters__price-separator {
  flex-shrink: 0;
  color: var(--color-text-muted);
}

.filters__apply {
  width: 100%;
  margin-top: 10px;
  padding: 9px;
  border: 1px solid var(--color-border-strong);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.filters__apply:hover {
  background: #f5f4f1;
}

.filters__clear {
  width: 100%;
  margin-top: 8px;
  padding: 11px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: 11px;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.filters__clear:hover {
  color: var(--color-text);
}

/* =========================================================
   FILTRES À VENIR
   ========================================================= */

.filters__group--upcoming {
  opacity: 0.45;
  /* Aucun clic possible : évite de laisser croire à un filtre actif */
  pointer-events: none;
  user-select: none;
}

.filters__toggle--static {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 0 12px;
}

.filters__soon {
  padding: 2px 6px;
  border: 1px solid var(--color-border-strong);
  color: var(--color-text-muted);
  font-size: 8px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

/* =========================================================
   DESKTOP : colonne fixe, plus de panneau
   ========================================================= */

@media (min-width: 1001px) {
  .filters__backdrop {
    display: none;
  }

  .filters {
    position: sticky;
    /* Reste visible au défilement, sous l'en-tête */
    top: calc(var(--header-height) + 20px);
    z-index: 1;

    width: 100%;
    height: auto;
    max-height: calc(100vh - var(--header-height) - 40px);

    background: transparent;
    box-shadow: none;

    transform: none;
  }

  .filters__header,
  .filters__footer {
    display: none;
  }

  .filters__body {
    padding: 0;
    overflow-y: auto;
  }
}
</style>
