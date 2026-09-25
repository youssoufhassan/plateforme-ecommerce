```vue
<script setup lang="ts">
import { computed, ref } from "vue";
import { RouterLink } from "vue-router";

import { fullImageUrl } from "@/services/productService";
import HeartIcon from "@/components/icons/HeartIcon.vue";
import type { Product } from "@/types/product";

const props = defineProps<{
  product: Product;
}>();

const emit = defineEmits<{
  add: [id: string];
}>();

const adding = ref(false);
const imgError = ref(false);

const displayImage = computed(() => {
  const path =
    props.product.imageUrls?.find((image) => image?.trim()) ||
    props.product.imageUrl ||
    "";

  return fullImageUrl(path);
});

const isOutOfStock = computed(() => !props.product.available);

const formattedPrice = computed(() => {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(props.product.price);
});

async function handleAdd(event: MouseEvent): Promise<void> {
  event.preventDefault();
  event.stopPropagation();

  if (isOutOfStock.value || adding.value) {
    return;
  }

  adding.value = true;

  try {
    emit("add", props.product.id);

    await new Promise((resolve) => setTimeout(resolve, 600));
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
        :to="`/products/${product.id}`"
        class="product-card__image-link"
        :aria-label="`Voir ${product.name}`"
      >
        <img
          v-if="!imgError && displayImage"
          :src="displayImage"
          :alt="product.name"
          class="product-card__image"
          loading="lazy"
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
        :aria-label="`Ajouter ${product.name} aux favoris`"
      >
        <HeartIcon />
      </button>
    </div>

    <!-- Informations -->
    <div class="product-card__content">
      <p v-if="product.brand" class="product-card__brand">
        {{ product.brand }}
      </p>

      <RouterLink :to="`/products/${product.id}`" class="product-card__name">
        {{ product.name }}
      </RouterLink>

      <p v-if="product.description" class="product-card__description">
        {{ product.description }}
      </p>

      <div class="product-card__footer">
        <span class="product-card__price">
          {{ formattedPrice }}
        </span>

        <button
          type="button"
          class="product-card__add"
          :class="{ 'product-card__add--adding': adding }"
          :disabled="isOutOfStock || adding"
          :aria-label="
            isOutOfStock
              ? `${product.name} est épuisé`
              : `Ajouter ${product.name} au panier`
          "
          @click="handleAdd"
        >
          <span v-if="adding">Ajouté ✓</span>
          <span v-else-if="isOutOfStock">Épuisé</span>
          <span v-else>Ajouter</span>
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.product-card {
  position: relative;
  min-width: 0;
}

.product-card__visual {
  position: relative;
  aspect-ratio: 1 / 1.18;
  overflow: hidden;
  background: #f1f0ed;
}

.product-card__image-link {
  display: block;
  width: 100%;
  height: 100%;
}

.product-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 400ms ease;
}

.product-card__image-link:hover .product-card__image {
  transform: scale(1.025);
}

.product-card__fallback {
  width: 100%;
  height: 100%;

  display: grid;
  place-items: center;

  background: #f1f0ed;

  color: var(--color-text-muted);

  font-size: 28px;
  font-weight: 500;
  letter-spacing: 0.12em;
}

.product-card__favorite {
  position: absolute;
  top: 14px;
  right: 14px;

  width: 38px;
  height: 38px;

  display: grid;
  place-items: center;

  background: rgba(255, 255, 255, 0.92);

  border-radius: 50%;

  color: var(--color-text);

  transition:
    background var(--transition-fast),
    transform var(--transition-fast);
}

.product-card__favorite:hover {
  background: var(--color-white);
  transform: scale(1.05);
}

.product-card__favorite:active {
  transform: scale(0.94);
}

.product-card__unavailable {
  position: absolute;
  left: 14px;
  bottom: 14px;

  padding: 6px 9px;

  background: rgba(255, 255, 255, 0.94);

  font-size: 9px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.product-card__content {
  padding-top: 14px;
}

.product-card__brand {
  margin-bottom: 3px;

  color: var(--color-text-muted);

  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.product-card__name {
  display: block;

  color: var(--color-text);

  font-size: 14px;
  line-height: 1.35;

  transition: opacity var(--transition-fast);
}

.product-card__name:hover {
  opacity: 0.6;
}

.product-card__description {
  margin-top: 5px;

  color: var(--color-text-muted);

  font-size: 11px;
  line-height: 1.4;

  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;

  gap: 12px;

  margin-top: 10px;
}

.product-card__price {
  font-size: 13px;
  font-weight: 500;
}

.product-card__add {
  flex-shrink: 0;

  padding: 8px 12px;

  background: var(--color-black);
  color: var(--color-white);

  font-size: 9px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;

  transition:
    opacity var(--transition-fast),
    transform var(--transition-fast);
}

.product-card__add:hover:not(:disabled) {
  opacity: 0.8;
}

.product-card__add:active:not(:disabled) {
  transform: scale(0.96);
}

.product-card__add:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.product-card__add--adding {
  opacity: 0.7;
}

@media (max-width: 767px) {
  .product-card__visual {
    aspect-ratio: 1 / 1.22;
  }

  .product-card__favorite {
    top: 10px;
    right: 10px;

    width: 34px;
    height: 34px;
  }

  .product-card__content {
    padding-top: 11px;
  }

  .product-card__brand {
    font-size: 9px;
  }

  .product-card__name {
    font-size: 13px;
  }

  .product-card__description {
    font-size: 10px;
  }

  .product-card__price {
    font-size: 12px;
  }

  .product-card__add {
    padding: 7px 10px;
    font-size: 8px;
  }
}
</style>
