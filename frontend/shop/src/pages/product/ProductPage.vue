<script setup lang="ts">
import { computed, ref, watch, onBeforeUnmount } from "vue";
import { RouterLink, useRoute } from "vue-router";

import SimilarProducts from "@/components/product/SimilarProducts.vue";
import VariantPicker from "@/components/product/VariantPicker.vue";
import HeartIcon from "@/components/icons/HeartIcon.vue";

import {
  apiMessage,
  fetchProduct,
  fetchCategoryTree,
  fullImageUrl,
} from "@/services/productService";

import { useFavorites } from "@/composables/useFavorites";
import { useCartStore } from "@/stores/cartStore";

import type { Product, ProductVariant } from "@/types/product";
import type { CategoryNode } from "@/types/catalog";

const route = useRoute();
const { isFavorite, toggleFavorite } = useFavorites();
const cartStore = useCartStore();

const product = ref<Product | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const selection = ref<Record<string, string>>({});
const activeImageIndex = ref(0);
const imgError = ref(false);

const quantity = ref(1);
const adding = ref(false);
const added = ref(false);
const addError = ref<string | null>(null);

const categoryTree = ref<CategoryNode[]>([]);

let addedTimeout: ReturnType<typeof setTimeout> | undefined;

/* IMAGES */

const images = computed(() => {
  if (!product.value) return [];

  const all = [
    ...(product.value.imageUrls ?? []),
    ...(product.value.imageUrl ? [product.value.imageUrl] : []),
  ]
    .filter((image) => image?.trim())
    .map(fullImageUrl);

  return [...new Set(all)];
});

const activeImage = computed(() => images.value[activeImageIndex.value] ?? "");

/* VARIANTES */

const options = computed(() => product.value?.options ?? []);
const allVariants = computed(() => product.value?.variants ?? []);

const showVariantPicker = computed(() => {
  if (options.value.length === 0) return false;
  if (options.value.length > 1) return true;

  return options.value[0].values.length > 1;
});

/**
 * Trouve la variante correspondant EXACTEMENT à la sélection courante.
 * Retourne null si la sélection est incomplète ou si aucune variante
 * ne correspond (combinaison inexistante).
 */
function findVariantForSelection(
  selected: Record<string, string>,
): ProductVariant | null {
  const chosen = Object.values(selected);

  if (chosen.length === 0) return null;

  return (
    allVariants.value.find(
      (variant) =>
        chosen.length === (variant.optionValueIds?.length ?? 0) &&
        chosen.every((valueId) => variant.optionValueIds?.includes(valueId)),
    ) ?? null
  );
}

const selectedVariant = computed<ProductVariant | null>(() => {
  const chosen = Object.values(selection.value);

  // Aucune sélection : on retourne la première variante disponible
  if (chosen.length === 0) {
    return (
      allVariants.value.find((variant) => variant.available) ??
      allVariants.value[0] ??
      null
    );
  }

  // Sélection partielle ou complète : on cherche la correspondance exacte
  return findVariantForSelection(selection.value);
});

const selectionComplete = computed(
  () =>
    options.value.length === 0 ||
    options.value.every((option) => Boolean(selection.value[option.id])),
);

const displayPrice = computed(
  () => selectedVariant.value?.price ?? product.value?.price ?? 0,
);

const formattedPrice = computed(() =>
  new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(displayPrice.value),
);

const favorite = computed(() =>
  product.value ? isFavorite(product.value.id) : false,
);

/**
 * Le bouton est actif uniquement si :
 * - le produit est disponible
 * - la sélection est complète
 * - une variante EXACTE existe et est disponible
 * - aucun ajout n'est en cours
 */
const canAddToCart = computed(
  () =>
    !!product.value &&
    !!product.value.available &&
    selectionComplete.value &&
    !!selectedVariant.value &&
    selectedVariant.value.available &&
    !adding.value,
);

function handleSelect(payload: { optionId: string; valueId: string }): void {
  const next = {
    ...selection.value,
    [payload.optionId]: payload.valueId,
  };

  // Vérifie si la nouvelle combinaison correspond à une variante existante
  const compatible = allVariants.value.some((variant) =>
    Object.values(next).every((valueId) =>
      variant.optionValueIds?.includes(valueId),
    ),
  );

  if (compatible) {
    selection.value = next;
  } else {
    // La combinaison n'existe pas : on ne conserve que le nouveau choix,
    // ce qui force l'utilisateur à re-sélectionner les autres options
    // au lieu de garder silencieusement une combinaison invalide.
    selection.value = { [payload.optionId]: payload.valueId };
  }
}

function preselect(data: Product): void {
  const firstAvailable =
    data.variants?.find((variant) => variant.available) ??
    data.variants?.[0] ??
    null;

  if (!firstAvailable?.optionValueIds || !data.options) {
    selection.value = {};
    return;
  }

  const initial: Record<string, string> = {};

  for (const option of data.options) {
    const match = option.values.find((value) =>
      firstAvailable.optionValueIds?.includes(value.id),
    );

    if (match) initial[option.id] = match.id;
  }

  selection.value = initial;
}

/* CHARGEMENT DU PRODUIT */

async function loadProduct(id: string): Promise<void> {
  loading.value = true;
  error.value = null;
  product.value = null;

  imgError.value = false;
  activeImageIndex.value = 0;
  quantity.value = 1;
  added.value = false;
  addError.value = null;
  selection.value = {};

  if (addedTimeout) {
    clearTimeout(addedTimeout);
    addedTimeout = undefined;
  }

  try {
    const data = await fetchProduct(id);
    product.value = data;
    preselect(data);
  } catch (e: unknown) {
    error.value = apiMessage(e, "Ce produit est introuvable.");
  } finally {
    loading.value = false;
  }
}

async function handleAddToCart(): Promise<void> {
  if (!product.value || !selectedVariant.value || !canAddToCart.value) {
    return;
  }

  adding.value = true;
  addError.value = null;

  try {
    await cartStore.add({
      productId: product.value.id,
      variantId: selectedVariant.value.id,
      quantity: quantity.value,
      productName: product.value.name,
      variantLabel: selectedVariant.value.label,
    });

    added.value = true;

    if (addedTimeout) clearTimeout(addedTimeout);

    addedTimeout = setTimeout(() => {
      added.value = false;
    }, 2200);
  } catch (e: unknown) {
    addError.value = apiMessage(e, "L'ajout au panier a échoué.");
  } finally {
    adding.value = false;
  }
}

function selectImage(index: number): void {
  activeImageIndex.value = index;
  imgError.value = false;
}

/* AUTRES UNIVERS */

function normalizeCategoryName(name: string): string {
  return name
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim()
    .toLocaleLowerCase("fr");
}

function findCategoryTrail(
  nodes: CategoryNode[],
  name: string,
  trail: CategoryNode[] = [],
): CategoryNode[] | null {
  const target = normalizeCategoryName(name);

  for (const node of nodes) {
    const currentTrail = [...trail, node];

    if (normalizeCategoryName(node.name) === target) {
      return currentTrail;
    }

    const found = findCategoryTrail(node.children ?? [], name, currentTrail);

    if (found) return found;
  }

  return null;
}

const currentUniverse = computed<CategoryNode | null>(() => {
  const categoryName = product.value?.categoryName;

  if (!categoryName) return null;

  const trail = findCategoryTrail(categoryTree.value, categoryName);

  return trail?.[0] ?? null;
});

const relatedCategories = computed(() => {
  const universes = categoryTree.value;
  const current = currentUniverse.value;

  if (!universes.length) return [];

  if (!current) return universes;

  return universes.filter(
    (universe) =>
      normalizeCategoryName(universe.name) !==
      normalizeCategoryName(current.name),
  );
});

async function loadCategoryTree(): Promise<void> {
  try {
    categoryTree.value = await fetchCategoryTree();
  } catch {
    categoryTree.value = [];
  }
}

void loadCategoryTree();

/* ROUTE */

watch(
  () => route.params.id,
  (id) => {
    if (typeof id === "string") {
      void loadProduct(id);
    }
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  if (addedTimeout) clearTimeout(addedTimeout);
});
</script>

<template>
  <main class="product-page">
    <!-- CHARGEMENT -->
    <div v-if="loading" class="container product-page__skeleton">
      <div class="skeleton__gallery"></div>

      <div class="skeleton__content">
        <div class="skeleton__line skeleton__line--small"></div>
        <div class="skeleton__line skeleton__line--title"></div>
        <div class="skeleton__line skeleton__line--price"></div>
        <div class="skeleton__line"></div>
        <div class="skeleton__line"></div>
        <div class="skeleton__line skeleton__line--button"></div>
      </div>
    </div>

    <!-- ERREUR -->
    <div v-else-if="error || !product" class="container product-error">
      <h1>{{ error || "Produit indisponible" }}</h1>

      <RouterLink to="/produits" class="product-error__link">
        Retour à la boutique
      </RouterLink>
    </div>

    <template v-else>
      <div class="container">
        <!-- FIL D'ARIANE -->
        <nav class="product-breadcrumb" aria-label="Fil d'Ariane">
          <RouterLink to="/">Accueil</RouterLink>
          <span aria-hidden="true">/</span>
          <RouterLink to="/produits">Boutique</RouterLink>
          <span aria-hidden="true">/</span>
          <span class="product-breadcrumb__current">{{ product.name }}</span>
        </nav>

        <div class="product-layout">
          <!-- GALERIE -->
          <section class="product-gallery" aria-label="Photos du produit">
            <div class="product-gallery__stage">
              <img
                v-if="!imgError && activeImage"
                :key="activeImage"
                :src="activeImage"
                :alt="product.name"
                class="product-gallery__image"
                fetchpriority="high"
                @error="imgError = true"
              />

              <div v-else class="product-gallery__fallback">
                <span>{{ product.name.slice(0, 2).toUpperCase() }}</span>
              </div>

              <span v-if="!product.available" class="product-gallery__status">
                Indisponible
              </span>

              <button
                type="button"
                class="product-gallery__favorite"
                :class="{
                  'product-gallery__favorite--active': favorite,
                }"
                :aria-label="
                  favorite ? 'Retirer des favoris' : 'Ajouter aux favoris'
                "
                :aria-pressed="favorite"
                @click="toggleFavorite(product.id, product.name)"
              >
                <HeartIcon />
              </button>
            </div>

            <div
              v-if="images.length > 1"
              class="product-gallery__thumbnails"
              aria-label="Choisir une photo"
            >
              <button
                v-for="(image, index) in images"
                :key="image"
                type="button"
                class="product-gallery__thumbnail"
                :class="{
                  'product-gallery__thumbnail--active':
                    index === activeImageIndex,
                }"
                :aria-label="`Afficher la photo ${index + 1}`"
                :aria-pressed="index === activeImageIndex"
                @click="selectImage(index)"
              >
                <img :src="image" alt="" loading="lazy" />
              </button>
            </div>

            <div
              v-if="product.description"
              class="product-gallery__description"
            >
              <h2 class="product-gallery__description-title">Description</h2>
              <p class="product-gallery__description-text">
                {{ product.description }}
              </p>
            </div>
          </section>

          <!-- INFORMATIONS PRODUIT -->
          <section class="product-info">
            <RouterLink
              v-if="product.brand"
              :to="{
                path: '/produits',
                query: { brand: product.brand },
              }"
              class="product-info__brand"
            >
              {{ product.brand }}
            </RouterLink>

            <span v-else-if="product.categoryName" class="product-info__brand">
              {{ product.categoryName }}
            </span>

            <h1 class="product-info__title">{{ product.name }}</h1>

            <div class="product-info__price-row">
              <p class="product-info__price">{{ formattedPrice }}</p>

              <span
                v-if="
                  selectedVariant?.label && selectedVariant.label !== 'Standard'
                "
                class="product-info__price-detail"
              >
                {{ selectedVariant.label }}
              </span>
            </div>

            <div class="product-info__availability">
              <span
                class="product-info__availability-dot"
                :class="{
                  'product-info__availability-dot--out': !product.available,
                }"
              ></span>

              {{
                product.available ? "Disponible à la commande" : "Indisponible"
              }}
            </div>

            <div class="product-info__rule"></div>

            <!-- VARIANTES -->
            <div v-if="showVariantPicker" class="product-variants">
              <VariantPicker
                :product="product"
                :selection="selection"
                class="product-variants__picker"
                @select="handleSelect"
              />
            </div>

            <!-- ACHAT -->
            <div class="product-purchase">
              <div class="product-quantity">
                <span class="product-quantity__label">Quantité</span>

                <div class="product-quantity__control">
                  <button
                    type="button"
                    aria-label="Diminuer la quantité"
                    :disabled="quantity <= 1 || adding"
                    @click="quantity = Math.max(1, quantity - 1)"
                  >
                    −
                  </button>

                  <span aria-live="polite">{{ quantity }}</span>

                  <button
                    type="button"
                    aria-label="Augmenter la quantité"
                    :disabled="quantity >= 20 || adding"
                    @click="quantity = Math.min(20, quantity + 1)"
                  >
                    +
                  </button>
                </div>
              </div>

              <button
                type="button"
                class="product-add"
                :class="{ 'product-add--success': added }"
                :disabled="!canAddToCart"
                @click="handleAddToCart"
              >
                <span>
                  {{
                    added
                      ? "Ajouté au panier"
                      : adding
                        ? "Ajout en cours…"
                        : !product.available
                          ? "Produit indisponible"
                          : !selectionComplete
                            ? "Choisissez vos options"
                            : !selectedVariant?.available
                              ? "Combinaison épuisée"
                              : "Ajouter au panier"
                  }}
                </span>

                <svg
                  v-if="!added && !adding && canAddToCart"
                  viewBox="0 0 24 24"
                  fill="none"
                  aria-hidden="true"
                >
                  <path
                    d="M5 12h14M13 6l6 6-6 6"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>

                <svg
                  v-else-if="added"
                  viewBox="0 0 24 24"
                  fill="none"
                  aria-hidden="true"
                >
                  <path
                    d="m5 12 4 4L19 6"
                    stroke="currentColor"
                    stroke-width="1.7"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </button>
            </div>

            <p v-if="addError" class="product-info__error" role="alert">
              {{ addError }}
            </p>

            <!-- INFORMATIONS DÉPLIABLES -->
            <div class="product-details">
              <details
                v-if="product.description"
                class="product-details__item product-details__item--description-mobile"
              >
                <summary>
                  <span>Description</span>
                  <span class="product-details__toggle"></span>
                </summary>

                <div class="product-details__content">
                  <p>{{ product.description }}</p>
                </div>
              </details>

              <details class="product-details__item">
                <summary>
                  <span>Détails</span>
                  <span class="product-details__toggle"></span>
                </summary>

                <div class="product-details__content">
                  <dl class="product-meta">
                    <div v-if="product.brand">
                      <dt>Marque</dt>
                      <dd>{{ product.brand }}</dd>
                    </div>

                    <div v-if="product.categoryName">
                      <dt>Catégorie</dt>
                      <dd>{{ product.categoryName }}</dd>
                    </div>

                    <div>
                      <dt>Disponibilité</dt>
                      <dd>{{ product.available ? "En stock" : "Épuisé" }}</dd>
                    </div>
                  </dl>
                </div>
              </details>

              <details class="product-details__item">
                <summary>
                  <span>Livraison et retours</span>
                  <span class="product-details__toggle"></span>
                </summary>

                <div class="product-details__content">
                  <p>
                    Les conditions de livraison et de rétractation applicables
                    sont indiquées lors de la commande et dans les conditions
                    générales de vente.
                  </p>
                </div>
              </details>
            </div>
          </section>
        </div>

        <!-- GARANTIES -->
        <section class="product-promises" aria-label="Nos services">
          <div class="product-promises__item">
            <span class="product-promises__icon">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path
                  d="M3 7h11v10H3zM14 10h4l3 3v4h-7z"
                  stroke="currentColor"
                  stroke-width="1.4"
                  stroke-linejoin="round"
                />
                <circle
                  cx="7.5"
                  cy="18"
                  r="1.5"
                  stroke="currentColor"
                  stroke-width="1.4"
                />
                <circle
                  cx="17.5"
                  cy="18"
                  r="1.5"
                  stroke="currentColor"
                  stroke-width="1.4"
                />
              </svg>
            </span>

            <div>
              <h2>Livraison</h2>
              <p>Conditions à la commande</p>
            </div>
          </div>

          <div class="product-promises__item">
            <span class="product-promises__icon">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path
                  d="M4 12a8 8 0 1 0 2.4-5.7L4 8.5M4 4v4.5h4.5"
                  stroke="currentColor"
                  stroke-width="1.4"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M12 8v4l2.5 1.5"
                  stroke="currentColor"
                  stroke-width="1.4"
                  stroke-linecap="round"
                />
              </svg>
            </span>

            <div>
              <h2>Retours</h2>
              <p>Conditions générales de vente</p>
            </div>
          </div>

          <div class="product-promises__item">
            <span class="product-promises__icon">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <rect
                  x="4"
                  y="10"
                  width="16"
                  height="11"
                  rx="1.5"
                  stroke="currentColor"
                  stroke-width="1.4"
                />
                <path
                  d="M8 10V7a4 4 0 0 1 8 0v3"
                  stroke="currentColor"
                  stroke-width="1.4"
                  stroke-linecap="round"
                />
              </svg>
            </span>

            <div>
              <h2>Paiement</h2>
              <p>Sécurisé</p>
            </div>
          </div>
        </section>

        <!-- AUTRES UNIVERS -->
        <section
          v-if="relatedCategories.length"
          class="related-categories"
          aria-label="Découvrir les autres univers"
        >
          <div class="related-categories__heading">
            <h2>Autres univers</h2>
          </div>

          <nav class="related-categories__links">
            <RouterLink
              v-for="category in relatedCategories"
              :key="category.path"
              :to="{
                path: '/produits',
                query: { category: category.path },
              }"
              class="related-categories__link"
            >
              {{ category.name }}
            </RouterLink>
          </nav>
        </section>
      </div>

      <!-- PRODUITS SIMILAIRES -->
      <section class="product-recommendations">
        <SimilarProducts :product-id="product.id" />
      </section>
    </template>
  </main>
</template>

<style scoped>
.product-page {
  width: 100%;
  min-height: 60vh;
  padding-bottom: var(--space-16);
  color: var(--color-text);
  background: var(--color-page);
}

/* Fil d'Ariane */

.product-breadcrumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-5) 0 var(--space-6);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  letter-spacing: var(--tracking-normal);
}

.product-breadcrumb a {
  color: inherit;
  text-decoration: none;
  transition: color var(--transition-fast);
}

.product-breadcrumb a:hover {
  color: var(--color-text);
}

.product-breadcrumb > span:nth-child(2),
.product-breadcrumb > span:nth-child(4) {
  opacity: 0.4;
}

.product-breadcrumb__current {
  max-width: 45%;
  overflow: hidden;
  color: var(--color-text);
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Composition principale */

.product-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(340px, 0.9fr);
  align-items: start;
  gap: clamp(var(--space-8), 5vw, var(--space-16));
}

.product-gallery,
.product-info {
  min-width: 0;
}

/* Galerie */

.product-gallery {
  position: relative;
}

.product-gallery__stage {
  position: relative;
  display: grid;
  width: 100%;
  aspect-ratio: 0.91;
  place-items: center;
  overflow: hidden;
  background: var(--color-surface);
}

.product-gallery__image {
  display: block;
  width: 100%;
  height: 100%;
  padding: var(--space-4);
  object-fit: contain;
  object-position: center;
  mix-blend-mode: multiply;
  transition: transform var(--transition-base);
}

.product-gallery__stage:hover .product-gallery__image {
  transform: scale(1.03);
}

.product-gallery__fallback {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  font-family: var(--font-display);
}

.product-gallery__fallback span {
  font-size: var(--text-5xl);
  letter-spacing: 0.08em;
}

.product-gallery__status {
  position: absolute;
  top: var(--space-4);
  left: var(--space-4);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-line);
  background: var(--color-page);
  color: var(--color-text);
  font-size: var(--text-2xs);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.product-gallery__favorite {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-pill);
  background: var(--color-page);
  color: var(--color-text);
  cursor: pointer;
  transition:
    background var(--transition-fast),
    color var(--transition-fast),
    border-color var(--transition-fast);
}

.product-gallery__favorite:hover {
  border-color: var(--color-text);
  background: var(--color-surface);
}

.product-gallery__favorite :deep(svg) {
  width: 19px;
  height: 19px;
}

.product-gallery__favorite--active {
  color: var(--color-error);
}

.product-gallery__favorite--active :deep(svg) {
  fill: currentColor;
}

.product-gallery__thumbnails {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-4);
  overflow-x: auto;
  padding-bottom: var(--space-1);
  scrollbar-width: thin;
  scrollbar-color: var(--color-line) transparent;
}

.product-gallery__thumbnail {
  flex: 0 0 76px;
  width: 76px;
  height: 84px;
  padding: var(--space-1);
  overflow: hidden;
  border: 1px solid transparent;
  background: var(--color-surface);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.product-gallery__thumbnail:hover {
  border-color: var(--color-text-muted);
}

.product-gallery__thumbnail--active {
  border-color: var(--color-ink);
}

.product-gallery__thumbnail img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  mix-blend-mode: multiply;
}

/* Description sous galerie (desktop) */

.product-gallery__description {
  margin-top: var(--space-6);
  padding-top: var(--space-6);
  border-top: 1px solid var(--color-line);
}

.product-gallery__description-title {
  margin: 0 0 var(--space-3);
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: var(--weight-regular);
  letter-spacing: var(--tracking-tight);
}

.product-gallery__description-text {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
  white-space: pre-line;
}

/* Informations produit */

.product-info {
  padding-top: var(--space-1);
}

.product-info__brand {
  display: inline-block;
  color: var(--color-text-muted);
  font-size: var(--text-2xs);
  font-weight: var(--weight-medium);
  letter-spacing: 0.2em;
  text-decoration: none;
  text-transform: uppercase;
  transition: color var(--transition-fast);
}

a.product-info__brand:hover {
  color: var(--color-text);
}

.product-info__title {
  max-width: 570px;
  margin: var(--space-3) 0 0;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: clamp(var(--text-3xl), 4vw, var(--text-5xl));
  font-weight: var(--weight-regular);
  line-height: var(--leading-tight);
  letter-spacing: var(--tracking-tight);
  overflow-wrap: anywhere;
}

.product-info__price-row {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: var(--space-3);
  margin-top: var(--space-6);
}

.product-info__price {
  margin: 0;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--weight-regular);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
}

.product-info__price-detail {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.product-info__availability {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-3);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.product-info__availability-dot {
  width: 6px;
  height: 6px;
  flex: 0 0 6px;
  border-radius: 50%;
  background: var(--color-success);
}

.product-info__availability-dot--out {
  background: var(--color-error);
}

.product-info__rule {
  height: 1px;
  margin-top: var(--space-6);
  background: var(--color-line);
}

/* Variantes */

.product-variants {
  margin-top: var(--space-6);
}

.product-variants__picker {
  display: block;
}

/* Quantité et panier */

.product-purchase {
  margin-top: var(--space-6);
}

.product-quantity {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
  margin-bottom: var(--space-4);
}

.product-quantity__label {
  color: var(--color-text);
  font-size: var(--text-xs);
}

.product-quantity__control {
  display: flex;
  width: 130px;
  height: 46px;
  align-items: center;
  justify-content: space-between;
  border: 1px solid var(--color-line);
  background: var(--color-page);
  transition: border-color var(--transition-fast);
}

.product-quantity__control:hover {
  border-color: var(--color-text-muted);
}

.product-quantity__control button {
  display: grid;
  width: 40px;
  height: 100%;
  place-items: center;
  border: 0;
  background: transparent;
  color: var(--color-text);
  font: inherit;
  font-size: var(--text-lg);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.product-quantity__control button:hover:not(:disabled) {
  color: var(--color-text-secondary);
}

.product-quantity__control button:disabled {
  cursor: not-allowed;
  opacity: 0.3;
}

.product-quantity__control span {
  color: var(--color-text);
  font-size: var(--text-sm);
  font-variant-numeric: tabular-nums;
}

.product-add {
  display: flex;
  width: 100%;
  min-height: 56px;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: 0 var(--space-6);
  border: 1px solid var(--color-ink);
  background: var(--color-ink);
  color: var(--color-text-inverse);
  font-family: var(--font-body);
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
  letter-spacing: 0.1em;
  text-align: left;
  text-transform: uppercase;
  cursor: pointer;
  transition:
    background var(--transition-fast),
    opacity var(--transition-fast);
}

.product-add:hover:not(:disabled) {
  background: var(--color-text-secondary);
  border-color: var(--color-text-secondary);
}

.product-add:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.product-add svg {
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
}

.product-add--success {
  background: var(--color-success);
  border-color: var(--color-success);
}

.product-info__error {
  margin: var(--space-3) 0 0;
  color: var(--color-error);
  font-size: var(--text-xs);
  line-height: var(--leading-normal);
}

/* Accordéons */

.product-details {
  margin-top: var(--space-8);
}

.product-details__item {
  border-top: 1px solid var(--color-line);
}

.product-details__item:last-child {
  border-bottom: 1px solid var(--color-line);
}

.product-details__item summary {
  display: flex;
  min-height: 60px;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  list-style: none;
  color: var(--color-text);
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.product-details__item summary:hover {
  color: var(--color-text-secondary);
}

.product-details__item summary::-webkit-details-marker {
  display: none;
}

.product-details__toggle {
  position: relative;
  width: 16px;
  height: 16px;
  flex: 0 0 16px;
}

.product-details__toggle::before,
.product-details__toggle::after {
  position: absolute;
  top: 7.5px;
  left: 2px;
  width: 12px;
  height: 1px;
  background: currentColor;
  content: "";
}

.product-details__toggle::after {
  transform: rotate(90deg);
  transition: transform var(--transition-base);
}

.product-details__item[open] .product-details__toggle::after {
  transform: rotate(0);
}

.product-details__content {
  padding: 0 0 var(--space-4);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
}

.product-details__content p {
  margin: 0;
  white-space: pre-line;
}

.product-meta {
  display: grid;
  gap: var(--space-3);
  margin: 0;
}

.product-meta > div {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-4);
}

.product-meta dt {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.product-meta dd {
  margin: 0;
  color: var(--color-text);
  font-size: var(--text-sm);
  text-align: right;
  overflow-wrap: anywhere;
}

/* Description mobile masquée en desktop */

.product-details__item--description-mobile {
  display: none;
}

/* Services */

.product-promises {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-top: var(--space-12);
  border-top: 1px solid var(--color-line);
}

.product-promises__item {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6) var(--space-5);
}

.product-promises__item + .product-promises__item {
  border-left: 1px solid var(--color-line);
}

.product-promises__icon {
  display: grid;
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  place-items: center;
  background: var(--color-surface);
  color: var(--color-text);
}

.product-promises__icon svg {
  width: 22px;
  height: 22px;
}

.product-promises__item h2 {
  margin: 0;
  color: var(--color-text);
  font-family: var(--font-body);
  font-size: var(--text-2xs);
  font-weight: var(--weight-medium);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.product-promises__item p {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  line-height: var(--leading-normal);
}

/* Autres univers */

.related-categories {
  display: grid;
  grid-template-columns: minmax(180px, 0.7fr) minmax(0, 2fr);
  align-items: center;
  gap: var(--space-8);
  margin-top: var(--space-8);
  padding: var(--space-6) 0;
  border-top: 1px solid var(--color-line);
}

.related-categories__heading h2 {
  margin: 0;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--weight-regular);
  line-height: var(--leading-snug);
  letter-spacing: var(--tracking-tight);
}

.related-categories__links {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.related-categories__link {
  display: inline-flex;
  min-height: 38px;
  align-items: center;
  justify-content: center;
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--color-line);
  background: var(--color-page);
  color: var(--color-text);
  font-size: var(--text-xs);
  text-decoration: none;
  transition:
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.related-categories__link:hover {
  border-color: var(--color-text);
  background: var(--color-surface);
}

/* Produits similaires */

.product-recommendations {
  margin-top: var(--space-20);
}

/* Chargement */

.product-page__skeleton {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
  gap: clamp(var(--space-8), 5vw, var(--space-16));
  padding-top: var(--space-7);
}

.skeleton__gallery {
  aspect-ratio: 0.91;
  background: var(--color-surface);
  animation: skeleton-pulse 1.6s ease-in-out infinite;
}

.skeleton__content {
  padding-top: var(--space-5);
}

.skeleton__line {
  height: 13px;
  margin-top: var(--space-5);
  background: var(--color-surface);
  animation: skeleton-pulse 1.6s ease-in-out infinite;
}

.skeleton__line--small {
  width: 25%;
  margin-top: 0;
}

.skeleton__line--title {
  width: 80%;
  height: 36px;
}

.skeleton__line--price {
  width: 30%;
  height: 24px;
}

.skeleton__line--button {
  height: 56px;
  margin-top: var(--space-8);
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

/* Erreur */

.product-error {
  display: flex;
  min-height: 420px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-block: var(--space-16);
  text-align: center;
}

.product-error h1 {
  margin: 0;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: var(--weight-regular);
  letter-spacing: var(--tracking-tight);
}

.product-error__link {
  display: inline-flex;
  min-height: 46px;
  align-items: center;
  justify-content: center;
  margin-top: var(--space-6);
  padding: 0 var(--space-6);
  border: 1px solid var(--color-ink);
  color: var(--color-ink);
  font-size: var(--text-xs);
  letter-spacing: 0.05em;
  text-decoration: none;
  text-transform: uppercase;
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
}

.product-error__link:hover {
  background: var(--color-ink);
  color: var(--color-text-inverse);
}

/* Focus a11y */

.product-add:focus-visible,
.product-gallery__favorite:focus-visible,
.product-gallery__thumbnail:focus-visible,
.related-categories__link:focus-visible,
.product-error__link:focus-visible,
.product-details__item summary:focus-visible {
  outline: 2px solid var(--color-ink);
  outline-offset: 3px;
}

/* Tablette */

@media (max-width: 1000px) {
  .product-layout {
    grid-template-columns: minmax(0, 1fr) minmax(280px, 0.9fr);
    gap: var(--space-8);
  }

  .product-info__title {
    font-size: var(--text-3xl);
  }

  .product-promises {
    grid-template-columns: minmax(0, 1fr);
    margin-top: var(--space-10);
  }

  .product-promises__item {
    align-items: center;
    padding: var(--space-5) 0;
  }

  .product-promises__item + .product-promises__item {
    border-left: 0;
    border-top: 1px solid var(--color-line);
  }

  .related-categories {
    grid-template-columns: minmax(0, 1fr);
    gap: var(--space-4);
  }

  .product-gallery__description {
    display: none;
  }

  .product-details__item--description-mobile {
    display: block;
  }
}

/* Mobile */

@media (max-width: 767px) {
  .product-page {
    padding-bottom: var(--space-10);
  }

  .product-breadcrumb {
    gap: var(--space-1);
    padding: var(--space-4) 0;
    font-size: var(--text-2xs);
  }

  .product-layout {
    grid-template-columns: minmax(0, 1fr);
    gap: var(--space-6);
  }

  .product-gallery__stage {
    aspect-ratio: 0.96;
  }

  .product-gallery__image {
    padding: var(--space-2);
  }

  .product-gallery__favorite {
    width: 42px;
    height: 42px;
  }

  .product-gallery__thumbnail {
    flex-basis: 66px;
    width: 66px;
    height: 74px;
  }

  .product-info {
    padding-top: 0;
  }

  .product-info__title {
    margin-top: var(--space-2);
    font-size: clamp(var(--text-2xl), 8vw, var(--text-3xl));
  }

  .product-info__price {
    font-size: var(--text-xl);
  }

  .product-add {
    min-height: 54px;
  }

  .product-promises {
    margin-top: var(--space-8);
  }

  .product-promises__item {
    padding: var(--space-4) 0;
  }

  .product-promises__icon {
    width: 42px;
    height: 42px;
    flex-basis: 42px;
  }

  .related-categories {
    margin-top: var(--space-6);
    padding-top: var(--space-5);
  }

  .related-categories__link {
    min-height: 36px;
    padding: var(--space-2) var(--space-3);
    font-size: var(--text-2xs);
  }

  .product-recommendations {
    margin-top: var(--space-10);
  }

  .product-page__skeleton {
    grid-template-columns: minmax(0, 1fr);
    gap: var(--space-5);
    padding-top: var(--space-3);
  }

  .skeleton__gallery {
    aspect-ratio: 0.96;
  }

  .skeleton__content {
    padding-top: 0;
  }

  .product-error {
    min-height: 340px;
  }
}

/* Petits écrans */

@media (max-width: 380px) {
  .product-info__title {
    font-size: var(--text-2xl);
  }

  .product-promises__item {
    gap: var(--space-3);
  }

  .product-promises__item p {
    overflow-wrap: anywhere;
  }

  .related-categories__link {
    padding-inline: var(--space-3);
    font-size: var(--text-2xs);
  }
}

/* Accessibilité */

@media (prefers-reduced-motion: reduce) {
  .product-gallery__image,
  .skeleton__gallery,
  .skeleton__line {
    animation: none;
  }

  .product-add,
  .product-gallery__favorite,
  .product-gallery__thumbnail,
  .product-error__link,
  .product-details__toggle::after,
  .related-categories__link,
  .product-gallery__image,
  .product-promises__icon {
    transition: none;
  }
}
</style>
