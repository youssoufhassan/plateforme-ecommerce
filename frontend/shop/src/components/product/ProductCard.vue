<script setup lang="ts">
import { computed, ref } from "vue";
import { RouterLink } from "vue-router";

import { fullImageUrl } from "@/services/productService";
import HeartIcon from "@/components/icons/HeartIcon.vue";
import { useFavorites } from "@/composables/useFavorites";
import type { Product } from "@/types/product";

const props = defineProps<{
  product: Product;
  /** Les premières cartes visibles chargent leur image immédiatement. */
  eager?: boolean;
  /** Ajout au panier. Renvoie une promesse : permet d'attendre la réponse du serveur. */
  onAdd?: (payload: { productId: string; variantId: string }) => Promise<void>;
}>();

const { isFavorite, toggleFavorite } = useFavorites();

const adding = ref(false);
const added = ref(false);
const error = ref<string | null>(null);
const imgError = ref(false);
/** Nombre de contenances proposées, information utile en parfumerie. */
const variantCount = computed(() => availableVariants.value.length);
const productUrl = computed(() => `/produits/${props.product.id}`);

const displayImage = computed(() => {
  const path =
    props.product.imageUrls?.find((image) => image?.trim()) ||
    props.product.imageUrl ||
    "";

  return fullImageUrl(path);
});

const availableVariants = computed(
  () => props.product.variants?.filter((variant) => variant.available) ?? [],
);

const isOutOfStock = computed(() => !props.product.available);

/** Plusieurs tailles : le choix se fait sur la fiche produit. */
const needsVariantChoice = computed(() => availableVariants.value.length > 1);

const canQuickAdd = computed(
  () => !!props.onAdd && !needsVariantChoice.value && !isOutOfStock.value,
);

/** Prix différents entre variantes : afficher « dès ». */
const hasPriceRange = computed(() => {
  const prices = new Set(
    availableVariants.value.map((variant) => variant.price),
  );
  return prices.size > 1;
});

const formattedPrice = computed(() =>
  new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(props.product.price),
);

const favorite = computed(() => isFavorite(props.product.id));

function handleFavorite(event: MouseEvent): void {
  event.preventDefault();
  event.stopPropagation();
  toggleFavorite(props.product.id);
}

async function handleAdd(event: MouseEvent): Promise<void> {
  event.preventDefault();
  event.stopPropagation();

  const variant = availableVariants.value[0];
  if (!props.onAdd || !variant || adding.value) {
    return;
  }

  adding.value = true;
  error.value = null;

  try {
    await props.onAdd({ productId: props.product.id, variantId: variant.id });

    added.value = true;
    setTimeout(() => (added.value = false), 1800);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : "Ajout impossible";
    setTimeout(() => (error.value = null), 3000);
  } finally {
    adding.value = false;
  }
}
</script>

<template>
  <article class="product-card">
    <!-- Image -->
    <div class="product-card__visual">
      <RouterLink
        :to="productUrl"
        class="product-card__image-link"
        :aria-label="`Voir ${product.name}`"
      >
        <img
          v-if="!imgError && displayImage"
          :src="displayImage"
          :alt="product.name"
          class="product-card__image"
          :loading="eager ? 'eager' : 'lazy'"
          decoding="async"
          @error="imgError = true"
        />

        <div v-else class="product-card__fallback">
          {{ product.name.slice(0, 2).toUpperCase() }}
        </div>

        <span v-if="isOutOfStock" class="product-card__unavailable">
          Épuisé
        </span>
      </RouterLink>

      <button
        type="button"
        class="product-card__favorite"
        :class="{ 'product-card__favorite--active': favorite }"
        :aria-label="
          favorite
            ? `Retirer ${product.name} des favoris`
            : `Ajouter ${product.name} aux favoris`
        "
        :aria-pressed="favorite"
        @click="handleFavorite"
      >
        <HeartIcon />
      </button>
    </div>

    <!-- Informations -->
    <div class="product-card__content">
      <!-- Ligne toujours présente : garde l'alignement entre les cartes -->
      <p class="product-card__brand">
        {{ product.brand || "\u00A0" }}
      </p>

      <RouterLink :to="productUrl" class="product-card__name">
        {{ product.name }}
      </RouterLink>

      <p v-if="variantCount > 1" class="product-card__variants">
        {{ variantCount }} contenances
      </p>

      <div class="product-card__footer">
        <span class="product-card__price">
          <span v-if="hasPriceRange" class="product-card__price-prefix"
            >dès</span
          >
          {{ formattedPrice }}
        </span>

        <button
          v-if="canQuickAdd"
          type="button"
          class="product-card__add"
          :class="{ 'product-card__add--adding': adding }"
          :disabled="adding"
          :aria-label="`Ajouter ${product.name} au panier`"
          @click="handleAdd"
        >
          <span v-if="added">✓</span>
          <span v-else-if="adding">…</span>
          <span v-else>Ajouter</span>
        </button>

        <span
          v-else-if="isOutOfStock"
          class="product-card__add product-card__add--disabled"
        >
          Épuisé
        </span>

        <RouterLink v-else :to="productUrl" class="product-card__add">
          {{ needsVariantChoice ? "Choisir" : "Voir" }}
        </RouterLink>
      </div>

      <p v-if="error" class="product-card__error" role="alert">
        {{ error }}
      </p>
    </div>
  </article>
</template>

<style scoped>
.product-card {
  position: relative;
  min-width: 0;
  /* La carte occupe toute la hauteur de sa colonne */
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* =========================================================
   VISUEL
   ========================================================= */

.product-card__visual {
  position: relative;
  flex-shrink: 0;
  aspect-ratio: 1 / 1.05;
  overflow: hidden;
  background: #ffffff;
}

.product-card__image-link {
  display: block;
  width: 100%;
  height: 100%;
}

.product-card__image {
  width: 100%;
  height: 100%;
  /* contain : le flacon entier reste visible, jamais rogné */
  object-fit: contain;
  padding: 12px;
  transition: transform 400ms ease;
}

.product-card__image-link:hover .product-card__image {
  transform: scale(1.03);
}

.product-card__fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: #f4f3f1;
  color: var(--color-text-muted);
  font-size: 22px;
  font-weight: 500;
  letter-spacing: 0.12em;
}

.product-card__favorite {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.92);
  border: none;
  border-radius: 50%;
  color: var(--color-text);
  cursor: pointer;
  transition:
    background var(--transition-fast),
    transform var(--transition-fast),
    color var(--transition-fast);
}

.product-card__favorite:hover {
  background: var(--color-white);
  transform: scale(1.08);
}

.product-card__favorite:active {
  transform: scale(0.92);
}

.product-card__favorite--active {
  color: #c0392b;
}

.product-card__favorite--active :deep(svg) {
  fill: currentColor;
}

.product-card__unavailable {
  position: absolute;
  left: 8px;
  bottom: 8px;
  padding: 4px 7px;
  background: rgba(255, 255, 255, 0.94);
  font-size: 8px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

/* =========================================================
   CONTENU
   ========================================================= */

.product-card__content {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding-top: 10px;
}

.product-card__brand {
  margin-bottom: 2px;
  /* Hauteur réservée même sans marque : les noms restent alignés */
  min-height: 1.2em;
  color: var(--color-text-muted);
  font-size: 9px;
  font-weight: 500;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-card__name {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5em;
  color: var(--color-text);
  font-size: 12.5px;
  line-height: 1.25;
  text-decoration: none;
  transition: opacity var(--transition-fast);
}

.product-card__name:hover {
  opacity: 0.6;
}

.product-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  /* Pousse le prix en bas : tous les prix s'alignent entre cartes */
  margin-top: auto;
  padding-top: 8px;
}

.product-card__price {
  font-size: 12.5px;
  font-weight: 600;
  white-space: nowrap;
}

.product-card__price-prefix {
  font-size: 9px;
  font-weight: 400;
  color: var(--color-text-muted);
}

.product-card__add {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  padding: 6px 9px;
  background: var(--color-black);
  color: var(--color-white);
  border: none;
  cursor: pointer;
  font-size: 8.5px;
  font-weight: 500;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  text-decoration: none;
  transition:
    opacity var(--transition-fast),
    transform var(--transition-fast);
}

.product-card__add:hover:not(:disabled):not(.product-card__add--disabled) {
  opacity: 0.8;
}

.product-card__add:active:not(:disabled):not(.product-card__add--disabled) {
  transform: scale(0.96);
}

.product-card__add:disabled,
.product-card__add--disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.product-card__add--adding {
  opacity: 0.7;
}

.product-card__error {
  margin-top: 5px;
  font-size: 9.5px;
  line-height: 1.3;
  color: #b00020;
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .product-card__visual {
    aspect-ratio: 1 / 1.1;
  }

  .product-card__image {
    padding: 8px;
  }

  .product-card__favorite {
    top: 6px;
    right: 6px;
    width: 27px;
    height: 27px;
  }

  .product-card__content {
    padding-top: 8px;
  }

  .product-card__brand {
    font-size: 8.5px;
  }

  .product-card__name {
    font-size: 12px;
  }

  .product-card__price {
    font-size: 12px;
  }

  .product-card__add {
    padding: 5px 8px;
    font-size: 8px;
  }
}
.product-card__variants {
  margin: 4px 0 0;
  color: var(--color-text-muted);
  font-size: 10.5px;
}

/* Le bouton d'ajout s'efface tant que la carte n'est pas survolée (souris seulement) */
@media (hover: hover) and (min-width: 1001px) {
  .product-card__add {
    opacity: 0;
    transition:
      opacity var(--transition-fast),
      background var(--transition-fast);
  }

  .product-card:hover .product-card__add,
  .product-card:focus-within .product-card__add {
    opacity: 1;
  }
}
</style>
