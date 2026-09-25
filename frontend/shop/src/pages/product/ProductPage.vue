<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";

import SimilarProducts from "@/components/product/SimilarProducts.vue";
import {
  apiMessage,
  fetchProduct,
  fullImageUrl,
} from "@/services/productService";
import { useFavorites } from "@/composables/useFavorites";
import HeartIcon from "@/components/icons/HeartIcon.vue";
import type { Product, ProductVariant } from "@/types/product";

const route = useRoute();
const { isFavorite, toggleFavorite } = useFavorites();

const product = ref<Product | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const selectedVariantId = ref<string | null>(null);
const activeImageIndex = ref(0);
const imgError = ref(false);

const quantity = ref(1);
const adding = ref(false);
const added = ref(false);
const addError = ref<string | null>(null);

/** Toutes les images disponibles, sans doublon ni valeur vide. */
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

const availableVariants = computed(
  () => product.value?.variants?.filter((variant) => variant.available) ?? [],
);

const allVariants = computed(() => product.value?.variants ?? []);

/** Plusieurs contenances : le sélecteur n'a de sens que dans ce cas. */
const showVariantPicker = computed(
  () =>
    allVariants.value.length > 1 || allVariants.value[0]?.label !== "Standard",
);

const selectedVariant = computed<ProductVariant | null>(() => {
  if (!selectedVariantId.value) return null;
  return (
    allVariants.value.find((v) => v.id === selectedVariantId.value) ?? null
  );
});

const displayPrice = computed(
  () => selectedVariant.value?.price ?? product.value?.price ?? 0,
);

const formattedPrice = computed(() =>
  new Intl.NumberFormat("fr-FR", { style: "currency", currency: "EUR" }).format(
    displayPrice.value,
  ),
);

const canAddToCart = computed(
  () => !!selectedVariant.value?.available && !adding.value,
);

const favorite = computed(() =>
  product.value ? isFavorite(product.value.id) : false,
);

async function loadProduct(id: string): Promise<void> {
  loading.value = true;
  error.value = null;
  imgError.value = false;
  activeImageIndex.value = 0;
  quantity.value = 1;

  try {
    const data = await fetchProduct(id);
    product.value = data;

    // Première variante disponible pré-sélectionnée : l'achat est possible d'emblée
    const firstAvailable = data.variants?.find((variant) => variant.available);
    selectedVariantId.value =
      firstAvailable?.id ?? data.variants?.[0]?.id ?? null;
  } catch (e: unknown) {
    error.value = apiMessage(e, "Ce produit est introuvable.");
    product.value = null;
  } finally {
    loading.value = false;
  }
}

async function handleAddToCart(): Promise<void> {
  if (!product.value || !selectedVariant.value) return;

  adding.value = true;
  addError.value = null;

  try {
    // TODO : brancher le panier au ticket suivant
    addError.value = "Le panier arrive très bientôt.";
  } finally {
    adding.value = false;
  }
}

watch(
  () => route.params.id,
  (id) => {
    if (typeof id === "string") loadProduct(id);
  },
  { immediate: true },
);
</script>

<template>
  <main class="product-page">
    <!-- Chargement -->
    <div v-if="loading" class="container product-page__skeleton">
      <div class="product-skeleton__gallery"></div>
      <div class="product-skeleton__info">
        <div class="product-skeleton__line product-skeleton__line--short"></div>
        <div class="product-skeleton__line product-skeleton__line--title"></div>
        <div class="product-skeleton__line product-skeleton__line--price"></div>
        <div class="product-skeleton__line"></div>
        <div class="product-skeleton__line"></div>
      </div>
    </div>

    <!-- Erreur -->
    <div v-else-if="error || !product" class="container product-page__error">
      <p class="product-page__error-title">{{ error }}</p>
      <RouterLink to="/produits" class="product-page__error-action">
        Retour au catalogue
      </RouterLink>
    </div>

    <template v-else>
      <div class="container">
        <nav class="product-page__breadcrumb" aria-label="Fil d'Ariane">
          <RouterLink to="/">Accueil</RouterLink>
          <span aria-hidden="true">/</span>
          <RouterLink to="/produits">Catalogue</RouterLink>
          <span aria-hidden="true">/</span>
          <span>{{ product.name }}</span>
        </nav>

        <div class="product-page__main">
          <!-- Galerie -->
          <section class="gallery" aria-label="Visuels du produit">
            <div class="gallery__stage">
              <img
                v-if="!imgError && activeImage"
                :src="activeImage"
                :alt="product.name"
                class="gallery__image"
                @error="imgError = true"
              />

              <div v-else class="gallery__fallback">
                {{ product.name.slice(0, 2).toUpperCase() }}
              </div>

              <span v-if="!product.available" class="gallery__badge">
                Épuisé
              </span>
            </div>

            <div v-if="images.length > 1" class="gallery__thumbs">
              <button
                v-for="(image, index) in images"
                :key="image"
                type="button"
                class="gallery__thumb"
                :class="{
                  'gallery__thumb--active': index === activeImageIndex,
                }"
                :aria-label="`Visuel ${index + 1}`"
                :aria-current="index === activeImageIndex"
                @click="
                  activeImageIndex = index;
                  imgError = false;
                "
              >
                <img :src="image" :alt="''" loading="lazy" />
              </button>
            </div>
          </section>

          <!-- Informations et achat -->
          <section class="purchase">
            <RouterLink
              v-if="product.brand"
              :to="{ path: '/produits', query: { brand: product.brand } }"
              class="purchase__brand"
            >
              {{ product.brand }}
            </RouterLink>

            <h1 class="purchase__name">{{ product.name }}</h1>

            <p class="purchase__price">
              {{ formattedPrice }}
              <span
                v-if="
                  selectedVariant?.label && selectedVariant.label !== 'Standard'
                "
                class="purchase__price-unit"
              >
                / {{ selectedVariant.label }}
              </span>
            </p>

            <!-- Contenances -->
            <fieldset v-if="showVariantPicker" class="variants">
              <legend class="variants__legend">Contenance</legend>

              <div class="variants__options">
                <button
                  v-for="variant in allVariants"
                  :key="variant.id"
                  type="button"
                  class="variants__option"
                  :class="{
                    'variants__option--active':
                      variant.id === selectedVariantId,
                    'variants__option--disabled': !variant.available,
                  }"
                  :disabled="!variant.available"
                  :aria-pressed="variant.id === selectedVariantId"
                  @click="selectedVariantId = variant.id"
                >
                  <span>{{ variant.label }}</span>
                  <span class="variants__option-price">
                    {{
                      new Intl.NumberFormat("fr-FR", {
                        style: "currency",
                        currency: "EUR",
                      }).format(variant.price)
                    }}
                  </span>
                </button>
              </div>

              <p
                v-if="selectedVariant && !selectedVariant.available"
                class="variants__notice"
              >
                Cette contenance est momentanément épuisée.
              </p>
            </fieldset>

            <!-- Quantité et achat -->
            <div class="purchase__actions">
              <div class="quantity">
                <button
                  type="button"
                  class="quantity__button"
                  aria-label="Diminuer la quantité"
                  :disabled="quantity <= 1"
                  @click="quantity = Math.max(1, quantity - 1)"
                >
                  −
                </button>

                <span class="quantity__value" aria-live="polite">{{
                  quantity
                }}</span>

                <button
                  type="button"
                  class="quantity__button"
                  aria-label="Augmenter la quantité"
                  :disabled="quantity >= 20"
                  @click="quantity = Math.min(20, quantity + 1)"
                >
                  +
                </button>
              </div>

              <button
                type="button"
                class="purchase__add"
                :disabled="!canAddToCart"
                @click="handleAddToCart"
              >
                <span v-if="added">Ajouté au panier</span>
                <span v-else-if="adding">Ajout en cours</span>
                <span v-else-if="!product.available">Produit épuisé</span>
                <span v-else>Ajouter au panier</span>
              </button>

              <button
                type="button"
                class="purchase__favorite"
                :class="{ 'purchase__favorite--active': favorite }"
                :aria-label="
                  favorite ? 'Retirer des favoris' : 'Ajouter aux favoris'
                "
                :aria-pressed="favorite"
                @click="toggleFavorite(product.id)"
              >
                <HeartIcon />
              </button>
            </div>

            <p v-if="addError" class="purchase__message" role="status">
              {{ addError }}
            </p>

            <!-- Description -->
            <div v-if="product.description" class="purchase__description">
              <h2 class="purchase__section-title">Description</h2>
              <p>{{ product.description }}</p>
            </div>

            <dl class="purchase__meta">
              <div v-if="product.categoryName">
                <dt>Catégorie</dt>
                <dd>
                  <RouterLink
                    :to="{
                      path: '/produits',
                      query: { category: product.categoryName },
                    }"
                  >
                    {{ product.categoryName }}
                  </RouterLink>
                </dd>
              </div>

              <div>
                <dt>Disponibilité</dt>
                <dd>{{ product.available ? "En stock" : "Épuisé" }}</dd>
              </div>

              <div>
                <dt>Livraison</dt>
                <dd>Offerte dès 40 € d'achat en France</dd>
              </div>
            </dl>
          </section>
        </div>
      </div>

      <SimilarProducts :product-id="product.id" />
    </template>
  </main>
</template>

<style scoped>
.product-page {
  width: 100%;
  padding-bottom: 72px;
}

/* =========================================================
   FIL D'ARIANE
   ========================================================= */

.product-page__breadcrumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding-top: 22px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.product-page__breadcrumb a {
  color: inherit;
  text-decoration: none;
}

.product-page__breadcrumb a:hover {
  color: var(--color-text);
}

/* =========================================================
   STRUCTURE
   ========================================================= */

.product-page__main {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32px;
  padding-top: 26px;
}

/* =========================================================
   GALERIE
   ========================================================= */

.gallery__stage {
  position: relative;
  aspect-ratio: 1 / 1.08;
  background: var(--color-white);
  overflow: hidden;
}

.gallery__image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 28px;
}

.gallery__fallback {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  background: #f4f3f1;
  color: var(--color-text-muted);
  font-family: var(--font-display);
  font-size: 56px;
  letter-spacing: 0.1em;
}

.gallery__badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 6px 10px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  font-size: 10px;
  letter-spacing: 0.06em;
}

.gallery__thumbs {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  overflow-x: auto;
  scrollbar-width: none;
}

.gallery__thumbs::-webkit-scrollbar {
  display: none;
}

.gallery__thumb {
  flex: 0 0 72px;
  height: 84px;
  padding: 6px;
  border: 1px solid var(--color-border);
  background: var(--color-white);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.gallery__thumb:hover {
  border-color: var(--color-border-strong);
}

.gallery__thumb--active {
  border-color: var(--color-text);
}

.gallery__thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* =========================================================
   ACHAT
   ========================================================= */

.purchase__brand {
  display: inline-block;
  color: var(--color-text-muted);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  text-decoration: none;
}

.purchase__brand:hover {
  color: var(--color-text);
}

.purchase__name {
  margin: 8px 0 0;
  font-family: var(--font-display);
  font-size: clamp(26px, 3vw, 38px);
  font-weight: 400;
  line-height: 1.08;
  letter-spacing: -0.03em;
  color: var(--color-text);
}

.purchase__price {
  margin: 14px 0 0;
  font-size: 22px;
  font-weight: 500;
  color: var(--color-text);
}

.purchase__price-unit {
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 400;
}

/* =========================================================
   CONTENANCES
   ========================================================= */

.variants {
  margin: 28px 0 0;
  padding: 0;
  border: none;
}

.variants__legend {
  padding: 0;
  margin-bottom: 10px;
  color: var(--color-text-muted);
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.variants__options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.variants__option {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 96px;
  padding: 11px 14px;
  border: 1px solid var(--color-border-strong);
  background: var(--color-white);
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 13px;
  text-align: left;
  transition:
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.variants__option:hover:not(:disabled) {
  border-color: var(--color-text);
}

.variants__option--active {
  border-color: var(--color-text);
  background: var(--color-text);
  color: var(--color-white);
}

.variants__option--disabled {
  cursor: not-allowed;
  opacity: 0.4;
  text-decoration: line-through;
}

.variants__option-price {
  font-size: 11px;
  opacity: 0.75;
}

.variants__notice {
  margin: 10px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
}

/* =========================================================
   QUANTITÉ ET ACTIONS
   ========================================================= */

.purchase__actions {
  display: flex;
  align-items: stretch;
  gap: 10px;
  margin-top: 26px;
}

.quantity {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-border-strong);
}

.quantity__button {
  width: 40px;
  height: 100%;
  min-height: 48px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-size: 16px;
  line-height: 1;
}

.quantity__button:disabled {
  cursor: not-allowed;
  opacity: 0.3;
}

.quantity__value {
  min-width: 28px;
  text-align: center;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

.purchase__add {
  flex: 1;
  min-height: 48px;
  padding-inline: 24px;
  border: none;
  background: var(--color-black);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.06em;
  transition: opacity var(--transition-fast);
}

.purchase__add:hover:not(:disabled) {
  opacity: 0.85;
}

.purchase__add:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.purchase__favorite {
  display: grid;
  place-items: center;
  width: 48px;
  min-height: 48px;
  border: 1px solid var(--color-border-strong);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  transition:
    border-color var(--transition-fast),
    color var(--transition-fast);
}

.purchase__favorite:hover {
  border-color: var(--color-text);
}

.purchase__favorite--active {
  color: #c0392b;
  border-color: #c0392b;
}

.purchase__favorite--active :deep(svg) {
  fill: currentColor;
}

.purchase__message {
  margin: 12px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
}

/* =========================================================
   DESCRIPTION ET DÉTAILS
   ========================================================= */

.purchase__description {
  margin-top: 36px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
}

.purchase__section-title {
  margin: 0 0 10px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.purchase__description p {
  margin: 0;
  max-width: 62ch;
  color: var(--color-text);
  font-size: 14px;
  line-height: 1.65;
}

.purchase__meta {
  display: grid;
  gap: 10px;
  margin: 26px 0 0;
  padding-top: 22px;
  border-top: 1px solid var(--color-border);
}

.purchase__meta > div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 13px;
}

.purchase__meta dt {
  color: var(--color-text-muted);
}

.purchase__meta dd {
  margin: 0;
  color: var(--color-text);
  text-align: right;
}

.purchase__meta a {
  color: inherit;
}

/* =========================================================
   ÉTATS
   ========================================================= */

.product-page__skeleton {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32px;
  padding-top: 40px;
}

.product-skeleton__gallery {
  aspect-ratio: 1 / 1.08;
  background: #efedea;
}

.product-skeleton__line {
  height: 13px;
  margin-top: 14px;
  background: #efedea;
}

.product-skeleton__line--short {
  width: 25%;
  height: 9px;
}

.product-skeleton__line--title {
  width: 65%;
  height: 30px;
}

.product-skeleton__line--price {
  width: 35%;
  height: 20px;
}

@media (prefers-reduced-motion: no-preference) {
  .product-skeleton__gallery,
  .product-skeleton__line {
    animation: product-pulse 1.5s ease-in-out infinite;
  }
}

@keyframes product-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.55;
  }
}

.product-page__error {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: 16px;
  min-height: 380px;
  text-align: center;
}

.product-page__error-title {
  margin: 0;
  font-size: 15px;
  color: var(--color-text);
}

.product-page__error-action {
  padding: 11px 22px;
  border: 1px solid var(--color-text);
  color: var(--color-text);
  font-size: 12px;
  text-decoration: none;
}

.product-page__error-action:hover {
  background: var(--color-text);
  color: var(--color-white);
}

/* =========================================================
   DESKTOP
   ========================================================= */

@media (min-width: 1001px) {
  .product-page__main,
  .product-page__skeleton {
    grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr);
    gap: 56px;
  }

  .purchase {
    /* Le bloc d'achat reste visible pendant la lecture de la description */
    position: sticky;
    top: calc(var(--header-height) + 28px);
    align-self: start;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .gallery__image {
    padding: 16px;
  }

  .purchase__actions {
    flex-wrap: wrap;
  }

  .purchase__add {
    order: 3;
    flex-basis: 100%;
  }

  .purchase__meta > div {
    font-size: 12.5px;
  }
}
</style>
