<script setup lang="ts">
import { computed, ref } from "vue";
import { RouterLink } from "vue-router";

import { apiMessage, fullImageUrl } from "@/services/productService";
import HeartIcon from "@/components/icons/HeartIcon.vue";
import { useFavorites } from "@/composables/useFavorites";
import { useCartStore } from "@/stores/cartStore";
import type { Product } from "@/types/product";

const props = defineProps<{
  product: Product;
  /** Les premières cartes visibles chargent leur image immédiatement. */
  eager?: boolean;
}>();

const { isFavorite, toggleFavorite } = useFavorites();
const cartStore = useCartStore();

const adding = ref(false);
const added = ref(false);
const error = ref<string | null>(null);
const imgError = ref(false);

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

const options = computed(() => props.product.options ?? []);

const isOutOfStock = computed(() => !props.product.available);

/**
 * Plusieurs axes, plusieurs valeurs ou plusieurs variantes :
 * le choix se fait sur la fiche produit.
 */
const needsVariantChoice = computed(() => {
  if (options.value.length > 1) return true;
  if (options.value.length === 1) return options.value[0].values.length > 1;
  return availableVariants.value.length > 1;
});

const canQuickAdd = computed(
  () => !needsVariantChoice.value && !isOutOfStock.value,
);

/**
 * Résumé des déclinaisons, adapté au produit :
 * « 3 contenances » pour un parfum, « taille et couleur » pour un vêtement.
 */
const variantSummary = computed(() => {
  if (options.value.length === 1) {
    const option = options.value[0];
    if (option.values.length < 2) return null;

    const label = option.name.toLowerCase();
    return `${option.values.length} ${label}${label.endsWith("s") ? "" : "s"}`;
  }

  if (options.value.length > 1) {
    return options.value
      .map((option) => option.name.toLowerCase())
      .join(" et ");
  }

  if (availableVariants.value.length > 1) {
    return `${availableVariants.value.length} déclinaisons`;
  }

  return null;
});

/** Prix différents entre variantes : afficher « dès ». */
const hasPriceRange = computed(() => {
  const prices = new Set(availableVariants.value.map((v) => v.price));
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
  toggleFavorite(props.product.id, props.product.name);
}

async function handleAdd(event: MouseEvent): Promise<void> {
  event.preventDefault();
  event.stopPropagation();

  const variant = availableVariants.value[0];
  if (!variant || adding.value) return;

  adding.value = true;
  error.value = null;

  try {
    await cartStore.add({
      productId: props.product.id,
      variantId: variant.id,
      quantity: 1,
      productName: props.product.name,
      variantLabel: variant.label,
    });

    added.value = true;
    setTimeout(() => (added.value = false), 1800);
  } catch (e: unknown) {
    error.value = apiMessage(e, "Ajout impossible");
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

      <p v-if="variantSummary" class="product-card__variants">
        {{ variantSummary }}
      </p>

      <div class="product-card__footer">
        <span class="product-card__price">
          <span v-if="hasPriceRange" class="product-card__price-prefix">
            dès
          </span>
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
  background: var(--color-paper);
}

.product-card__image-link {
  display: block;
  width: 100%;
  height: 100%;
}

.product-card__image {
  width: 100%;
  height: 100%;
  /* contain : le produit entier reste visible, jamais rogné */
  object-fit: contain;
  padding: var(--space-3);
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
  background: var(--color-plate);
  color: var(--color-text-muted);
  font-family: var(--font-display);
  font-size: 24px;
  letter-spacing: 0.1em;
}

.product-card__favorite {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);

  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;

  border: none;
  border-radius: var(--radius-pill);
  background: rgb(255 255 255 / 92%);
  cursor: pointer;
  color: var(--color-text);

  transition:
    background var(--transition-fast),
    transform var(--transition-fast),
    color var(--transition-fast);
}

.product-card__favorite:hover {
  background: var(--color-paper);
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
  left: var(--space-2);
  bottom: var(--space-2);
  padding: var(--space-1) var(--space-2);
  background: rgb(255 255 255 / 94%);
  font-size: var(--text-2xs);
}

/* =========================================================
   CONTENU
   ========================================================= */

.product-card__content {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding-top: var(--space-3);
}

.product-card__brand {
  margin-bottom: 2px;
  /* Hauteur réservée même sans marque : les noms restent alignés */
  min-height: 1.2em;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
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
  min-height: 2.6em;

  color: var(--color-text);
  font-size: var(--text-base);
  line-height: var(--leading-snug);
  text-decoration: none;

  transition: opacity var(--transition-fast);
}

.product-card__name:hover {
  opacity: 0.6;
}

.product-card__variants {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  text-transform: lowercase;
}

/* Les noms d'axes viennent de l'admin : première lettre en majuscule */
.product-card__variants::first-letter {
  text-transform: uppercase;
}

.product-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  /* Pousse le prix en bas : tous les prix s'alignent entre cartes */
  margin-top: auto;
  padding-top: var(--space-3);
}

.product-card__price {
  font-size: var(--text-base);
  font-weight: var(--weight-medium);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.product-card__price-prefix {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: var(--weight-regular);
}

.product-card__add {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;

  height: 30px;
  padding-inline: var(--space-3);

  border: 1px solid var(--color-ink);
  background: transparent;
  cursor: pointer;

  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-xs);

  transition:
    background var(--transition-fast),
    color var(--transition-fast),
    opacity var(--transition-fast);
}

.product-card__add:hover:not(:disabled):not(.product-card__add--disabled) {
  background: var(--color-ink);
  color: var(--color-text-inverse);
}

.product-card__add:disabled,
.product-card__add--disabled {
  cursor: not-allowed;
  border-color: var(--color-line);
  color: var(--color-text-muted);
}

.product-card__add--adding {
  opacity: 0.6;
}

.product-card__error {
  margin-top: var(--space-1);
  color: var(--color-error);
  font-size: var(--text-xs);
  line-height: var(--leading-snug);
}

/* Le bouton se révèle au survol, souris uniquement */
@media (hover: hover) and (min-width: 1001px) {
  .product-card__add {
    opacity: 0;
  }

  .product-card:hover .product-card__add,
  .product-card:focus-within .product-card__add {
    opacity: 1;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .product-card__visual {
    aspect-ratio: 1 / 1.1;
  }

  .product-card__image {
    padding: var(--space-2);
  }

  .product-card__favorite {
    top: var(--space-1);
    right: var(--space-1);
    width: 28px;
    height: 28px;
  }

  .product-card__content {
    padding-top: var(--space-2);
  }

  .product-card__name {
    /* Une seule ligne : un nom trop long est coupé proprement */
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    color: var(--color-text);
    font-size: var(--text-base);
    line-height: var(--leading-snug);
    text-decoration: none;

    transition: opacity var(--transition-fast);
  }

  .product-card__price {
    font-size: var(--text-sm);
  }

  .product-card__add {
    height: 28px;
    padding-inline: var(--space-2);
    font-size: var(--text-2xs);
  }
}
</style>
