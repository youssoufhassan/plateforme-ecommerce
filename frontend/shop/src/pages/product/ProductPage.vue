<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";

import SimilarProducts from "@/components/product/SimilarProducts.vue";
import VariantPicker from "@/components/product/VariantPicker.vue";
import {
  apiMessage,
  fetchProduct,
  fullImageUrl,
} from "@/services/productService";
import { useFavorites } from "@/composables/useFavorites";
import { useCartStore } from "@/stores/cartStore";
import HeartIcon from "@/components/icons/HeartIcon.vue";
import type { Product, ProductVariant } from "@/types/product";

const route = useRoute();
const { isFavorite, toggleFavorite } = useFavorites();
const cartStore = useCartStore();

const product = ref<Product | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

/** Valeur retenue pour chaque axe, indexée par identifiant d'axe. */
const selection = ref<Record<string, string>>({});

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

const options = computed(() => product.value?.options ?? []);
const allVariants = computed(() => product.value?.variants ?? []);

/** Le sélecteur n'a de sens qu'avec de vraies déclinaisons. */
const showVariantPicker = computed(() => {
  if (options.value.length === 0) return false;
  if (options.value.length > 1) return true;
  // Un axe à valeur unique ne mérite pas de sélecteur
  return options.value[0].values.length > 1;
});

/** La variante correspondant à la combinaison choisie, si elle existe. */
const selectedVariant = computed<ProductVariant | null>(() => {
  const chosen = Object.values(selection.value);

  if (chosen.length === 0) {
    return (
      allVariants.value.find((v) => v.available) ?? allVariants.value[0] ?? null
    );
  }

  return (
    allVariants.value.find((variant) =>
      chosen.every((valueId) => variant.optionValueIds?.includes(valueId)),
    ) ?? null
  );
});

/** Tous les axes doivent être renseignés avant l'ajout au panier. */
const selectionComplete = computed(
  () =>
    options.value.length === 0 ||
    Object.keys(selection.value).length === options.value.length,
);

const displayPrice = computed(
  () => selectedVariant.value?.price ?? product.value?.price ?? 0,
);

const formattedPrice = computed(() =>
  new Intl.NumberFormat("fr-FR", { style: "currency", currency: "EUR" }).format(
    displayPrice.value,
  ),
);

const canAddToCart = computed(
  () =>
    selectionComplete.value &&
    !!selectedVariant.value?.available &&
    !adding.value,
);

const favorite = computed(() =>
  product.value ? isFavorite(product.value.id) : false,
);

/**
 * Change la valeur d'un axe.
 * Si la combinaison obtenue n'existe pas, les autres axes sont réinitialisés :
 * mieux vaut redemander un choix que laisser le client bloqué.
 */
function handleSelect(payload: { optionId: string; valueId: string }): void {
  const next = { ...selection.value, [payload.optionId]: payload.valueId };

  const exists = allVariants.value.some((variant) =>
    Object.values(next).every((id) => variant.optionValueIds?.includes(id)),
  );

  selection.value = exists ? next : { [payload.optionId]: payload.valueId };
}

/** Présélectionne la première combinaison disponible. */
function preselect(data: Product): void {
  const firstAvailable =
    data.variants?.find((v) => v.available) ?? data.variants?.[0] ?? null;

  if (!firstAvailable?.optionValueIds || !data.options) {
    selection.value = {};
    return;
  }

  const initial: Record<string, string> = {};

  for (const option of data.options) {
    const match = option.values.find((v) =>
      firstAvailable.optionValueIds?.includes(v.id),
    );
    if (match) initial[option.id] = match.id;
  }

  selection.value = initial;
}

async function loadProduct(id: string): Promise<void> {
  loading.value = true;
  error.value = null;
  imgError.value = false;
  activeImageIndex.value = 0;
  quantity.value = 1;
  addError.value = null;

  try {
    const data = await fetchProduct(id);
    product.value = data;

    preselect(data);
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
    await cartStore.add({
      productId: product.value.id,
      variantId: selectedVariant.value.id,
      quantity: quantity.value,
      productName: product.value.name,
      variantLabel: selectedVariant.value.label,
    });

    added.value = true;
    setTimeout(() => (added.value = false), 2000);
  } catch (e: unknown) {
    addError.value = apiMessage(e, "L'ajout au panier a échoué.");
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

            <!-- Déclinaisons : une rangée par axe -->
            <VariantPicker
              v-if="showVariantPicker"
              :product="product"
              :selection="selection"
              class="purchase__picker"
              @select="handleSelect"
            />

            <p
              v-if="
                showVariantPicker &&
                selectionComplete &&
                !selectedVariant?.available
              "
              class="purchase__notice"
            >
              Cette combinaison est momentanément épuisée.
            </p>

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
                <span v-else-if="!selectionComplete"
                  >Choisissez vos options</span
                >
                <span v-else-if="!selectedVariant?.available">
                  Combinaison épuisée
                </span>
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
                @click="toggleFavorite(product.id, product.name)"
              >
                <HeartIcon />
              </button>
            </div>

            <p v-if="addError" class="purchase__message" role="alert">
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
                <dd>Offerte dès 40 € d'achat (expédition suivie)</dd>
              </div>

              <div>
                <dt>Retours</dt>
                <dd>Délai de rétractation de 14 jours</dd>
              </div>

              <div>
                <dt>Paiement</dt>
                <dd>Paiement 100% sécurisé via Stripe</dd>
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
   DÉCLINAISONS
   ========================================================= */

.purchase__picker {
  margin-top: 28px;
}

.purchase__notice {
  margin: 12px 0 0;
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
  color: var(--color-error);
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
