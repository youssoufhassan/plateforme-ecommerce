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

const selectedVariant = computed<ProductVariant | null>(() => {
  const chosen = Object.values(selection.value);

  if (chosen.length === 0) {
    return (
      allVariants.value.find((variant) => variant.available) ??
      allVariants.value[0] ??
      null
    );
  }

  return (
    allVariants.value.find(
      (variant) =>
        chosen.length === (variant.optionValueIds?.length ?? 0) &&
        chosen.every((valueId) => variant.optionValueIds?.includes(valueId)),
    ) ?? null
  );
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

const canAddToCart = computed(
  () =>
    !!product.value &&
    !!product.value.available &&
    selectionComplete.value &&
    !!selectedVariant.value?.available &&
    !adding.value,
);

function handleSelect(payload: { optionId: string; valueId: string }): void {
  const next = {
    ...selection.value,
    [payload.optionId]: payload.valueId,
  };

  const compatible = allVariants.value.some((variant) =>
    Object.values(next).every((valueId) =>
      variant.optionValueIds?.includes(valueId),
    ),
  );

  selection.value = compatible ? next : { [payload.optionId]: payload.valueId };
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
      <h1>Ce produit est indisponible</h1>
      <p>{{ error || "Nous n'avons pas trouvé ce produit." }}</p>

      <RouterLink to="/produits" class="product-error__link">
        Découvrir la boutique
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
          <span class="product-breadcrumb__current">
            {{ product.name }}
          </span>
        </nav>

        <div class="product-layout">
          <!-- GALERIE -->
          <section class="product-gallery" aria-label="Photos du produit">
            <div class="product-gallery__stage">
              <div class="product-gallery__image-wash"></div>

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
            <div class="product-info__eyebrow">
              <span class="product-info__eyebrow-line"></span>

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

              <span v-else class="product-info__brand">
                {{ product.categoryName }}
              </span>
            </div>

            <h1 class="product-info__title">
              {{ product.name }}
            </h1>

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
              <p class="product-variants__heading">
                Choisissez votre déclinaison
              </p>

              <VariantPicker
                :product="product"
                :selection="selection"
                class="product-variants__picker"
                @select="handleSelect"
              />

              <p v-if="!selectionComplete" class="product-variants__hint">
                Sélectionnez une valeur pour chaque option.
              </p>

              <p
                v-else-if="!selectedVariant?.available"
                class="product-variants__hint"
                role="status"
              >
                Cette combinaison est momentanément épuisée.
              </p>
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
                  <span>Détails du produit</span>
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
                    Livraison offerte dès 40 € d'achat. Les conditions
                    applicables de livraison et de rétractation sont celles
                    indiquées lors de la commande et dans les conditions
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
              <p>Offerte dès 40 € d'achat</p>
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
              <p>14 jours de rétractation</p>
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
              <h2>Paiement sécurisé</h2>
              <p>Via Stripe</p>
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
            <h2>Et si vous découvriez un autre univers&nbsp;?</h2>
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
              <span>{{ category.name }}</span>
              <span class="related-categories__view">Découvrir</span>
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
  --ivory: var(--color-page, #fff);
  --paper: var(--color-surface, #f7f7f5);
  --sand: var(--color-line, #deded8);
  --champagne: var(--color-text-muted, #777);
  --ink: var(--color-ink, #262626);
  --muted: var(--color-text-muted, #777);
  --line: var(--color-line, #deded8);

  width: 100%;
  min-height: 60vh;
  padding-bottom: 56px;
  color: var(--ink);
}

/* Fil d'Ariane */

.product-breadcrumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 22px 0 26px;
  color: var(--muted);
  font-size: 12px;
}

.product-breadcrumb a {
  color: inherit;
  text-decoration: none;
  transition: color 180ms ease;
}

.product-breadcrumb a:hover {
  color: var(--ink);
}

.product-breadcrumb > span:nth-child(2),
.product-breadcrumb > span:nth-child(4) {
  opacity: 0.45;
}

.product-breadcrumb__current {
  max-width: 45%;
  overflow: hidden;
  color: var(--ink);
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Composition principale */

.product-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.12fr) minmax(340px, 0.88fr);
  align-items: start;
  gap: clamp(36px, 5.5vw, 78px);
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
  background: var(--color-surface, #f7f7f5);
  isolation: isolate;
}

.product-gallery__stage::before {
  position: absolute;
  z-index: -1;
  inset: 0;
  background: var(--color-page, #fff);
  content: "";
}

.product-gallery__image {
  display: block;
  width: 100%;
  height: 100%;
  padding: 10px;
  object-fit: contain;
  object-position: center;
  mix-blend-mode: multiply;
  transform: scale(1.12);
  transform-origin: center;
  animation: product-image-in 300ms ease both;
}

@keyframes product-image-in {
  from {
    opacity: 0.65;
    transform: scale(1.08);
  }

  to {
    opacity: 1;
    transform: scale(1.12);
  }
}

.product-gallery__fallback {
  display: flex;
  width: 100%;
  height: 100%;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--muted);
  font-family: var(--font-display, Georgia, serif);
}

.product-gallery__fallback span {
  font-size: 72px;
  letter-spacing: 0.08em;
}

.product-gallery__status {
  position: absolute;
  top: 18px;
  left: 18px;
  padding: 8px 11px;
  border: 1px solid var(--line);
  background: var(--color-page, #fff);
  color: var(--ink);
  font-size: 10px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.product-gallery__favorite {
  position: absolute;
  top: 16px;
  right: 16px;
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 50%;
  background: var(--color-page, #fff);
  color: var(--ink);
  cursor: pointer;
  transition:
    background 180ms ease,
    color 180ms ease,
    transform 180ms ease;
}

.product-gallery__favorite:hover {
  transform: scale(1.04);
  background: var(--color-surface, #f7f7f5);
}

.product-gallery__favorite :deep(svg) {
  width: 19px;
  height: 19px;
}

.product-gallery__favorite--active {
  color: #a84949;
}

.product-gallery__favorite--active :deep(svg) {
  fill: currentColor;
}

.product-gallery__thumbnails {
  display: flex;
  gap: 10px;
  margin-top: 13px;
  overflow-x: auto;
  padding-bottom: 3px;
  scrollbar-width: thin;
  scrollbar-color: var(--line) transparent;
}

.product-gallery__thumbnail {
  flex: 0 0 75px;
  width: 75px;
  height: 82px;
  padding: 4px;
  overflow: hidden;
  border: 1px solid transparent;
  background: var(--color-surface, #f7f7f5);
  cursor: pointer;
  transition: border-color 180ms ease;
}

.product-gallery__thumbnail:hover {
  border-color: var(--muted);
}

.product-gallery__thumbnail--active {
  border-color: var(--ink);
}

.product-gallery__thumbnail img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  mix-blend-mode: multiply;
}

/* Description sous la galerie (desktop uniquement) */

.product-gallery__description {
  margin-top: 22px;
  padding-top: 22px;
  border-top: 1px solid var(--line);
}

.product-gallery__description-title {
  margin: 0 0 12px;
  color: var(--ink);
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.04em;
}

.product-gallery__description-text {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.85;
  white-space: pre-line;
}

/* Informations produit */

.product-info {
  padding-top: 12px;
}

.product-info__eyebrow {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-info__eyebrow-line {
  width: 25px;
  height: 1px;
  background: var(--line);
}

.product-info__brand {
  color: var(--muted);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.19em;
  text-decoration: none;
  text-transform: uppercase;
}

a.product-info__brand:hover {
  color: var(--ink);
}

.product-info__title {
  max-width: 570px;
  margin: 20px 0 0;
  color: var(--ink);
  font-family: var(--font-display, Georgia, "Times New Roman", serif);
  font-size: clamp(40px, 4.2vw, 60px);
  font-weight: 400;
  line-height: 1.08;
  letter-spacing: -0.04em;
  overflow-wrap: anywhere;
}

.product-info__price-row {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 12px;
  margin-top: 25px;
}

.product-info__price {
  margin: 0;
  color: var(--ink);
  font-size: 27px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

.product-info__price-detail {
  color: var(--muted);
  font-size: 13px;
}

.product-info__availability {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  color: var(--muted);
  font-size: 12.5px;
}

.product-info__availability-dot {
  width: 6px;
  height: 6px;
  flex: 0 0 6px;
  border-radius: 50%;
  background: #64856b;
}

.product-info__availability-dot--out {
  background: #a15a52;
}

/*
  Un seul trait de séparation entre la zone prix/dispo et la zone
  variantes/achat : utile, pas redondant.
*/
.product-info__rule {
  height: 1px;
  margin-top: 25px;
  background: var(--line);
}

/* Variantes */

.product-variants {
  margin-top: 25px;
}

.product-variants__heading {
  margin: 0 0 14px;
  color: var(--ink);
  font-size: 12px;
  font-weight: 500;
}

.product-variants__picker {
  display: block;
}

.product-variants__hint {
  margin: 11px 0 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

/* Quantité et panier */

.product-purchase {
  margin-top: 28px;
}

.product-quantity {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
}

.product-quantity__label {
  color: var(--ink);
  font-size: 12.5px;
}

.product-quantity__control {
  display: flex;
  width: 132px;
  height: 48px;
  align-items: center;
  justify-content: space-between;
  border: 1px solid var(--line);
  background: var(--color-page, #fff);
}

.product-quantity__control button {
  display: grid;
  width: 40px;
  height: 100%;
  place-items: center;
  border: 0;
  background: transparent;
  color: var(--ink);
  font: inherit;
  font-size: 18px;
  cursor: pointer;
}

.product-quantity__control button:disabled {
  cursor: not-allowed;
  opacity: 0.3;
}

.product-quantity__control span {
  color: var(--ink);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.product-add {
  display: flex;
  width: 100%;
  min-height: 62px;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
  padding: 0 24px;
  border: 1px solid var(--color-ink, #262626);
  background: var(--color-ink, #262626);
  color: #fff;
  font-family: inherit;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.11em;
  text-align: left;
  text-transform: uppercase;
  cursor: pointer;
  transition:
    background 180ms ease,
    border-color 180ms ease,
    opacity 180ms ease;
}

.product-add svg {
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
}

.product-add:hover:not(:disabled) {
  opacity: 0.86;
}

.product-add:disabled {
  cursor: not-allowed;
  opacity: 0.43;
}

.product-add--success {
  border-color: #45694e;
  background: #45694e;
}

.product-info__error {
  margin: 12px 0 0;
  color: var(--color-error, #a33d36);
  font-size: 12.5px;
  line-height: 1.5;
}

/* Accordéons */

.product-details {
  margin-top: 32px;
  /*
    Pas de border-top ici : chaque item porte son propre border-bottom.
    On garde donc UN trait par accordéon au lieu de deux.
  */
}

.product-details__item {
  border-bottom: 1px solid var(--line);
}

.product-details__item summary {
  display: flex;
  min-height: 62px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  list-style: none;
  color: var(--ink);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.025em;
  cursor: pointer;
}

.product-details__item summary::-webkit-details-marker {
  display: none;
}

.product-details__toggle {
  position: relative;
  width: 15px;
  height: 15px;
  flex: 0 0 15px;
}

.product-details__toggle::before,
.product-details__toggle::after {
  position: absolute;
  top: 7px;
  left: 2px;
  width: 11px;
  height: 1px;
  background: var(--ink);
  content: "";
}

.product-details__toggle::after {
  transform: rotate(90deg);
  transition: transform 180ms ease;
}

.product-details__item[open] .product-details__toggle::after {
  transform: rotate(0);
}

.product-details__content {
  padding: 0 0 20px;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.85;
}

.product-details__content p {
  margin: 0;
  white-space: pre-line;
}

.product-meta {
  display: grid;
  gap: 13px;
  margin: 0;
}

.product-meta > div {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
}

.product-meta dt {
  color: var(--muted);
  font-size: 13px;
}

.product-meta dd {
  margin: 0;
  color: var(--ink);
  font-size: 13px;
  text-align: right;
  overflow-wrap: anywhere;
}

/*
  L'accordéon description est masqué en desktop (≥ 1001px)
  car la description est déjà visible sous la galerie.
*/
.product-details__item--description-mobile {
  display: none;
}

/* Services */

.product-promises {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-top: 44px;
  /*
    On garde UN SEUL trait (border-top) pour séparer la zone
    services du reste. Pas de border-bottom redondant.
  */
  border-top: 1px solid var(--line);
}

.product-promises__item {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 16px;
  padding: 28px clamp(15px, 2.5vw, 32px);
}

/* Plus de traits verticaux entre les 3 services : design épuré. */

.product-promises__icon {
  display: grid;
  width: 46px;
  height: 46px;
  flex: 0 0 46px;
  place-items: center;
  background: var(--color-surface, #f7f7f5);
  color: var(--ink);
}

.product-promises__icon svg {
  width: 26px;
  height: 26px;
}

.product-promises__item h2 {
  margin: 0;
  color: var(--ink);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.product-promises__item p {
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

/* Autres catégories */

.related-categories {
  display: grid;
  grid-template-columns: minmax(190px, 0.8fr) minmax(0, 2fr);
  align-items: center;
  gap: 25px;
  /*
    Plus de border-top ni border-bottom : la section services
    juste au-dessus fournit déjà le trait de séparation.
  */
  margin-top: 28px;
  padding: 24px 0;
}

.related-categories__heading {
  min-width: 0;
}

.related-categories__heading h2 {
  margin: 0;
  color: var(--ink);
  font-family: var(--font-display, Georgia, "Times New Roman", serif);
  font-size: clamp(19px, 2vw, 25px);
  font-weight: 400;
  line-height: 1.25;
  letter-spacing: -0.025em;
}

.related-categories__links {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.related-categories__link {
  display: inline-flex;
  min-height: 38px;
  align-items: center;
  justify-content: center;
  padding: 9px 14px;
  border: 1px solid var(--line);
  background: transparent;
  color: var(--ink);
  font-size: 11px;
  text-decoration: none;
  transition:
    border-color 180ms ease,
    background 180ms ease;
}

.related-categories__link:hover {
  border-color: var(--ink);
  background: var(--color-surface, #f7f7f5);
}

/* Produits similaires */

.product-recommendations {
  margin-top: 75px;
}

/* Chargement */

.product-page__skeleton {
  display: grid;
  grid-template-columns: minmax(0, 1.12fr) minmax(0, 0.88fr);
  gap: clamp(36px, 5.5vw, 78px);
  padding-top: 28px;
}

.skeleton__gallery {
  aspect-ratio: 0.91;
  background: var(--color-surface, #f7f7f5);
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton__content {
  padding-top: 20px;
}

.skeleton__line {
  height: 13px;
  margin-top: 20px;
  background: var(--color-surface, #f7f7f5);
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton__line--small {
  width: 25%;
  margin-top: 0;
}

.skeleton__line--title {
  width: 80%;
  height: 34px;
}

.skeleton__line--price {
  width: 30%;
  height: 23px;
}

.skeleton__line--button {
  height: 56px;
  margin-top: 32px;
}

@keyframes skeleton-pulse {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.48;
  }
}

/* Erreur */

.product-error {
  display: flex;
  min-height: 430px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-block: 60px;
  text-align: center;
}

.product-error h1 {
  margin: 0;
  color: var(--ink);
  font-family: var(--font-display, Georgia, "Times New Roman", serif);
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 400;
}

.product-error p {
  margin: 12px 0 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.7;
}

.product-error__link {
  display: inline-flex;
  min-height: 48px;
  align-items: center;
  justify-content: center;
  margin-top: 25px;
  padding: 0 24px;
  border: 1px solid var(--ink);
  color: var(--ink);
  font-size: 12px;
  text-decoration: none;
  transition:
    background 180ms ease,
    color 180ms ease;
}

.product-error__link:hover {
  background: var(--ink);
  color: var(--color-page, #fff);
}

/* Tablette */

@media (max-width: 1000px) {
  .product-layout {
    grid-template-columns: minmax(0, 1fr) minmax(280px, 0.9fr);
    gap: 30px;
  }

  .product-info__title {
    font-size: clamp(34px, 4vw, 46px);
  }

  .product-promises__item {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
    padding: 22px 17px;
  }

  .related-categories {
    grid-template-columns: minmax(0, 1fr);
    gap: 17px;
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
    padding-bottom: 45px;
  }

  .product-breadcrumb {
    gap: 7px;
    padding: 17px 0;
    font-size: 11px;
  }

  .product-layout {
    grid-template-columns: minmax(0, 1fr);
    gap: 28px;
  }

  .product-gallery__stage {
    aspect-ratio: 0.96;
  }

  .product-gallery__image {
    padding: 8px;
    transform: scale(1.08);
  }

  @keyframes product-image-in {
    from {
      opacity: 0.65;
      transform: scale(1.04);
    }

    to {
      opacity: 1;
      transform: scale(1.08);
    }
  }

  .product-gallery__favorite {
    width: 40px;
    height: 40px;
  }

  .product-gallery__thumbnail {
    flex-basis: 65px;
    width: 65px;
    height: 74px;
  }

  .product-info {
    padding-top: 0;
  }

  .product-info__title {
    margin-top: 15px;
    font-size: clamp(32px, 8.5vw, 42px);
    line-height: 1.12;
  }

  .product-info__price-row {
    margin-top: 19px;
  }

  .product-info__rule {
    margin-top: 22px;
  }

  .product-variants {
    margin-top: 22px;
  }

  .product-add {
    min-height: 58px;
  }

  .product-promises {
    grid-template-columns: minmax(0, 1fr);
    margin-top: 36px;
  }

  .product-promises__item {
    min-height: 84px;
    align-items: center;
    flex-direction: row;
    gap: 15px;
    padding: 18px 4px;
  }

  .product-promises__item + .product-promises__item {
    border-top: 1px solid var(--line);
  }

  .product-promises__icon {
    width: 44px;
    height: 44px;
    flex-basis: 44px;
  }

  .related-categories {
    gap: 16px;
    margin-top: 20px;
    padding: 20px 0;
  }

  .related-categories__heading h2 {
    max-width: 340px;
    font-size: 23px;
  }

  .related-categories__links {
    gap: 7px;
  }

  .related-categories__link {
    min-height: 36px;
    padding: 9px 12px;
    font-size: 11px;
  }

  .product-recommendations {
    margin-top: 50px;
  }

  .product-page__skeleton {
    grid-template-columns: minmax(0, 1fr);
    gap: 24px;
    padding-top: 12px;
  }

  .skeleton__gallery {
    aspect-ratio: 0.96;
  }

  .skeleton__content {
    padding-top: 0;
  }

  .product-error {
    min-height: 350px;
  }
}

/* Petits écrans */

@media (max-width: 380px) {
  .product-info__title {
    font-size: 30px;
  }

  .product-promises__item {
    gap: 12px;
  }

  .product-promises__item p {
    overflow-wrap: anywhere;
  }

  .related-categories {
    padding-inline: 0;
  }

  .related-categories__link {
    padding-inline: 10px;
    font-size: 10px;
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
  .related-categories__link {
    transition: none;
  }
}
</style>
