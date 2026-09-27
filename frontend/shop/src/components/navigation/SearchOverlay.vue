<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import { RouterLink, useRouter } from "vue-router";

import CloseIcon from "@/components/icons/CloseIcon.vue";
import SearchIcon from "@/components/icons/SearchIcon.vue";
import {
  fetchCatalogFilters,
  fullImageUrl,
  searchProducts,
} from "@/services/productService";
import type { CatalogFilters } from "@/types/catalog";
import type { Product } from "@/types/product";

const props = defineProps<{ open: boolean }>();
const emit = defineEmits<{ close: [] }>();

const router = useRouter();

const RECENT_KEY = "sidra:recent-searches";
const MIN_CHARS = 2;
const DEBOUNCE = 250;

const query = ref("");
const products = ref<Product[]>([]);
const filters = ref<CatalogFilters | null>(null);
const searching = ref(false);
const searched = ref(false);

const inputRef = ref<HTMLInputElement | null>(null);
let timer: ReturnType<typeof setTimeout> | null = null;

const recent = ref<string[]>(readRecent());

function readRecent(): string[] {
  try {
    const raw = localStorage.getItem(RECENT_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function rememberSearch(value: string): void {
  const next = [value, ...recent.value.filter((item) => item !== value)].slice(
    0,
    5,
  );
  recent.value = next;

  try {
    localStorage.setItem(RECENT_KEY, JSON.stringify(next));
  } catch {
    // Stockage indisponible : l'historique ne sera pas conservé
  }
}

function clearRecent(): void {
  recent.value = [];
  try {
    localStorage.removeItem(RECENT_KEY);
  } catch {
    // sans effet
  }
}

/** Marques dont le nom contient la recherche, pour proposer un raccourci. */
const matchingBrands = computed(() => {
  const value = query.value.trim().toLowerCase();
  if (value.length < MIN_CHARS || !filters.value) return [];

  return filters.value.brands
    .filter((brand) => brand.toLowerCase().includes(value))
    .slice(0, 4);
});

const hasResults = computed(
  () => products.value.length > 0 || matchingBrands.value.length > 0,
);

function money(value: number): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(value);
}

async function runSearch(): Promise<void> {
  const value = query.value.trim();

  if (value.length < MIN_CHARS) {
    products.value = [];
    searched.value = false;
    return;
  }

  searching.value = true;

  try {
    const result = await searchProducts({ q: value, size: 6 });
    products.value = result.content;
    searched.value = true;
  } catch {
    products.value = [];
  } finally {
    searching.value = false;
  }
}

/** La frappe est temporisée : une requête par pause, pas par caractère. */
watch(query, () => {
  if (timer) clearTimeout(timer);
  timer = setTimeout(runSearch, DEBOUNCE);
});

function submit(): void {
  const value = query.value.trim();
  if (!value) return;

  rememberSearch(value);
  close();
  router.push({ path: "/produits", query: { q: value } });
}

function searchTerm(value: string): void {
  query.value = value;
  submit();
}

function goToProduct(id: string): void {
  close();
  router.push(`/produits/${id}`);
}

function close(): void {
  emit("close");
}

function handleKeydown(event: KeyboardEvent): void {
  if (event.key === "Escape") close();
}

watch(
  () => props.open,
  async (open) => {
    if (open) {
      document.addEventListener("keydown", handleKeydown);
      document.body.style.overflow = "hidden";

      await nextTick();
      inputRef.value?.focus();

      if (!filters.value) {
        fetchCatalogFilters()
          .then((data) => (filters.value = data))
          .catch(() => {
            // Les raccourcis marque ne seront pas proposés
          });
      }
    } else {
      document.removeEventListener("keydown", handleKeydown);
      document.body.style.overflow = "";
      query.value = "";
      products.value = [];
      searched.value = false;
    }
  },
);
</script>

<template>
  <Teleport to="body">
    <Transition name="search">
      <div v-if="open" class="search" role="dialog" aria-label="Rechercher">
        <div class="search__backdrop" @click="close"></div>

        <div class="search__panel">
          <div class="container search__bar">
            <SearchIcon class="search__icon" />

            <input
              ref="inputRef"
              v-model="query"
              type="search"
              class="search__input"
              placeholder="Rechercher un parfum, une marque"
              aria-label="Rechercher"
              autocomplete="off"
              @keydown.enter="submit"
            />

            <button
              type="button"
              class="btn-icon search__close"
              aria-label="Fermer la recherche"
              @click="close"
            >
              <CloseIcon />
            </button>
          </div>

          <div class="container search__body">
            <!-- Avant la frappe : historique et raccourcis -->
            <template v-if="query.trim().length < MIN_CHARS">
              <section v-if="recent.length" class="search__block">
                <div class="search__block-head">
                  <h2 class="search__block-title">Recherches récentes</h2>
                  <button
                    type="button"
                    class="search__clear"
                    @click="clearRecent"
                  >
                    Effacer
                  </button>
                </div>

                <div class="search__tags">
                  <button
                    v-for="term in recent"
                    :key="term"
                    type="button"
                    class="search__tag"
                    @click="searchTerm(term)"
                  >
                    {{ term }}
                  </button>
                </div>
              </section>

              <section v-if="filters?.brands?.length" class="search__block">
                <h2 class="search__block-title">Maisons</h2>

                <div class="search__tags">
                  <RouterLink
                    v-for="brand in filters.brands.slice(0, 8)"
                    :key="brand"
                    :to="{ path: '/produits', query: { brand } }"
                    class="search__tag"
                    @click="close"
                  >
                    {{ brand }}
                  </RouterLink>
                </div>
              </section>
            </template>

            <!-- Pendant et après la frappe -->
            <template v-else>
              <p v-if="searching && !hasResults" class="search__state">
                Recherche
              </p>

              <p v-else-if="searched && !hasResults" class="search__state">
                Aucun résultat pour « {{ query.trim() }} ». Essayez un nom de
                parfum ou de maison.
              </p>

              <template v-else>
                <section v-if="matchingBrands.length" class="search__block">
                  <h2 class="search__block-title">Maisons</h2>

                  <div class="search__tags">
                    <RouterLink
                      v-for="brand in matchingBrands"
                      :key="brand"
                      :to="{ path: '/produits', query: { brand } }"
                      class="search__tag"
                      @click="close"
                    >
                      {{ brand }}
                    </RouterLink>
                  </div>
                </section>

                <section v-if="products.length" class="search__block">
                  <h2 class="search__block-title">Produits</h2>

                  <ul class="suggestions">
                    <li v-for="product in products" :key="product.id">
                      <button
                        type="button"
                        class="suggestion"
                        @click="goToProduct(product.id)"
                      >
                        <span class="suggestion__plate plate">
                          <img
                            v-if="product.imageUrl"
                            :src="fullImageUrl(product.imageUrl)"
                            :alt="''"
                            loading="lazy"
                          />
                        </span>

                        <span class="suggestion__text">
                          <span v-if="product.brand" class="suggestion__brand">
                            {{ product.brand }}
                          </span>
                          <span class="suggestion__name">{{
                            product.name
                          }}</span>
                        </span>

                        <span class="suggestion__price numeric">
                          {{ money(product.price) }}
                        </span>
                      </button>
                    </li>
                  </ul>

                  <button type="button" class="search__all" @click="submit">
                    Voir tous les résultats
                  </button>
                </section>
              </template>
            </template>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.search {
  position: fixed;
  inset: 0;
  z-index: var(--z-overlay);
}

.search__backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
}

.search__panel {
  position: relative;
  max-height: 86vh;
  overflow-y: auto;
  overscroll-behavior: contain;
  background: var(--color-paper);
}

/* =========================================================
   BARRE DE SAISIE
   ========================================================= */

.search__bar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  height: var(--header-height);
  border-bottom: 1px solid var(--color-line);
}

.search__icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  color: var(--color-text-muted);
}

.search__input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  background: transparent;
  color: var(--color-text);
  font-family: var(--font-body);
  font-size: var(--text-lg);
}

.search__input::placeholder {
  color: var(--color-text-muted);
}

.search__input:focus {
  outline: none;
}

.search__close {
  flex-shrink: 0;
}

/* =========================================================
   CONTENU
   ========================================================= */

.search__body {
  padding-block: var(--space-8) var(--space-10);
}

.search__block + .search__block {
  margin-top: var(--space-8);
}

.search__block-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-4);
}

.search__block-title {
  margin: 0 0 var(--space-4);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--color-text-muted);
}

.search__clear {
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

.search__clear:hover {
  color: var(--color-text);
}

.search__tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.search__tag {
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--color-line);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-sm);
  text-decoration: none;
  transition:
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.search__tag:hover {
  border-color: var(--color-ink);
  background: var(--color-sand);
}

.search__state {
  color: var(--color-text-muted);
  font-size: var(--text-base);
}

/* =========================================================
   SUGGESTIONS PRODUIT
   ========================================================= */

.suggestions {
  display: grid;
  gap: var(--space-1);
  margin: 0;
  padding: 0;
  list-style: none;
}

.suggestion {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  width: 100%;
  padding: var(--space-2);
  border: none;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: background var(--transition-fast);
}

.suggestion:hover {
  background: var(--color-page);
}

.suggestion__plate {
  flex-shrink: 0;
  width: 54px;
  height: 58px;
}

.suggestion__text {
  display: grid;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.suggestion__brand {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.suggestion__name {
  color: var(--color-text);
  font-size: var(--text-base);
}

.suggestion__price {
  flex-shrink: 0;
  font-size: var(--text-base);
}

.search__all {
  margin-top: var(--space-5);
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-sm);
  text-decoration: underline;
  text-underline-offset: 0.25em;
}

/* Le panneau glisse depuis le haut : il répond à l'ouverture */
.search-enter-active .search__panel,
.search-leave-active .search__panel {
  transition: transform var(--transition-base);
}

.search-enter-from .search__panel,
.search-leave-to .search__panel {
  transform: translateY(-100%);
}

.search-enter-active .search__backdrop,
.search-leave-active .search__backdrop {
  transition: opacity var(--transition-base);
}

.search-enter-from .search__backdrop,
.search-leave-to .search__backdrop {
  opacity: 0;
}

@media (max-width: 767px) {
  .search__panel {
    max-height: 100dvh;
    height: 100dvh;
  }

  .search__input {
    font-size: var(--text-md);
  }
}
</style>
