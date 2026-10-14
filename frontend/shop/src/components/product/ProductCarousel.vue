<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";

import ProductCard from "@/components/product/ProductCard.vue";
import type { Product } from "@/types/product";

const props = withDefaults(
  defineProps<{
    products: Product[];
    loading?: boolean;
    error?: string | null;
    skeletonCount?: number;
    onAdd?: (payload: {
      productId: string;
      variantId: string;
    }) => Promise<void>;
  }>(),
  {
    loading: false,
    error: null,
    skeletonCount: 5,
  },
);

const track = ref<HTMLDivElement | null>(null);
const hasOverflow = ref(false);
const canGoLeft = ref(false);
const canGoRight = ref(false);

let resizeObserver: ResizeObserver | null = null;

function updateControls(): void {
  const element = track.value;

  if (!element) {
    hasOverflow.value = false;
    canGoLeft.value = false;
    canGoRight.value = false;
    return;
  }

  const maxScroll = element.scrollWidth - element.clientWidth;

  hasOverflow.value = maxScroll > 2;
  canGoLeft.value = element.scrollLeft > 2;
  canGoRight.value = element.scrollLeft < maxScroll - 2;
}

function observeTrack(): void {
  resizeObserver?.disconnect();

  const element = track.value;

  if (element && typeof ResizeObserver !== "undefined") {
    resizeObserver = new ResizeObserver(updateControls);
    resizeObserver.observe(element);
  }

  updateControls();
}

function getScrollStep(): number {
  const element = track.value;

  if (!element) return 280;

  const firstCard = element.firstElementChild as HTMLElement | null;

  if (!firstCard) return element.clientWidth * 0.8;

  const cardWidth = firstCard.getBoundingClientRect().width;
  const styles = window.getComputedStyle(element);
  const gap = Number.parseFloat(styles.columnGap || styles.gap) || 16;

  return cardWidth + gap;
}

/** Avance ou recule d'une carte entière, sans changer de ligne. */
function moveCarousel(direction: -1 | 1): void {
  const element = track.value;

  if (!element) return;

  element.scrollBy({
    left: getScrollStep() * direction,
    behavior: "smooth",
  });
}

watch(
  () => [props.products.length, props.loading, Boolean(props.error)] as const,
  async () => {
    await nextTick();
    observeTrack();
  },
  { flush: "post" },
);

onMounted(async () => {
  await nextTick();
  observeTrack();
});

onBeforeUnmount(() => {
  resizeObserver?.disconnect();
  resizeObserver = null;
});
</script>

<template>
  <section class="product-carousel" aria-label="Carrousel de produits">
    <div
      v-if="!loading && !error && hasOverflow"
      class="product-carousel__toolbar"
      aria-label="Navigation des produits"
    >
      <button
        type="button"
        class="product-carousel__arrow"
        :disabled="!canGoLeft"
        aria-label="Afficher le produit précédent"
        @click="moveCarousel(-1)"
      >
        <svg
          viewBox="0 0 24 24"
          width="19"
          height="19"
          fill="none"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <path d="M15 18l-6-6 6-6" />
        </svg>
      </button>

      <button
        type="button"
        class="product-carousel__arrow"
        :disabled="!canGoRight"
        aria-label="Afficher le produit suivant"
        @click="moveCarousel(1)"
      >
        <svg
          viewBox="0 0 24 24"
          width="19"
          height="19"
          fill="none"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <path d="M9 18l6-6-6-6" />
        </svg>
      </button>
    </div>

    <div
      v-if="error"
      class="product-carousel__state product-carousel__state--error"
      role="alert"
    >
      {{ error }}
    </div>

    <div
      v-else-if="products.length === 0 && !loading"
      class="product-carousel__state"
    >
      Aucun produit disponible pour le moment.
    </div>

    <div
      v-else
      ref="track"
      class="product-carousel__track"
      :aria-busy="loading"
      @scroll.passive="updateControls"
    >
      <template v-if="loading">
        <div
          v-for="n in skeletonCount"
          :key="`skeleton-${n}`"
          class="product-skeleton"
        >
          <div class="product-skeleton__visual"></div>
          <div
            class="product-skeleton__line product-skeleton__line--short"
          ></div>
          <div class="product-skeleton__line"></div>
          <div
            class="product-skeleton__line product-skeleton__line--price"
          ></div>
        </div>
      </template>

      <template v-else>
        <ProductCard
          v-for="(product, index) in products"
          :key="product.id"
          :product="product"
          :eager="index < 5"
          :on-add="props.onAdd"
        />
      </template>
    </div>
  </section>
</template>

<style scoped>
.product-carousel {
  position: relative;
  width: 100%;
  min-width: 0;
}

/* Flèches discrètes, alignées à droite au-dessus des cartes. */
.product-carousel__toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 12px;
}

.product-carousel__arrow {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  padding: 0;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-pill);
  background: var(--color-plate);
  color: var(--color-text);
  cursor: pointer;
  transition:
    color var(--transition-base),
    background var(--transition-base),
    border-color var(--transition-base),
    opacity var(--transition-base);
}

.product-carousel__arrow:hover:not(:disabled) {
  border-color: var(--color-ink);
  background: var(--color-ink);
  color: var(--color-text-inverse);
}

.product-carousel__arrow:disabled {
  cursor: default;
  opacity: 0.3;
}

.product-carousel__arrow:focus-visible {
  outline: 2px solid var(--color-ink);
  outline-offset: 3px;
}

/* Une seule rangée : les cartes défilent horizontalement. */
.product-carousel__track {
  display: flex;
  align-items: stretch;
  gap: 16px;
  width: 100%;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 2px 1px 10px;
  scrollbar-width: none;
  scroll-snap-type: x mandatory;
  scroll-behavior: smooth;
  overscroll-behavior-x: contain;
  -webkit-overflow-scrolling: touch;
}

.product-carousel__track::-webkit-scrollbar {
  display: none;
}

.product-carousel__track > * {
  flex: 0 0 calc((100% - 64px) / 5);
  min-width: 0;
  scroll-snap-align: start;
}

/* États */
.product-carousel__state {
  display: grid;
  place-items: center;
  min-height: 240px;
  padding: 24px;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  text-align: center;
}

.product-carousel__state--error {
  color: #8a2525;
}

/* Squelette de chargement */
.product-skeleton {
  min-width: 0;
}

.product-skeleton__visual {
  aspect-ratio: 4 / 5;
  background: var(--color-plate);
}

.product-skeleton__line {
  height: 10px;
  margin-top: 9px;
  background: var(--color-plate);
}

.product-skeleton__line--short {
  width: 38%;
  height: 8px;
  margin-top: 13px;
}

.product-skeleton__line--price {
  width: 28%;
}

/* Six cartes sur les grands écrans. */
@media (min-width: 1440px) {
  .product-carousel__track > * {
    flex-basis: calc((100% - 80px) / 6);
  }
}

/* Tablette : environ trois cartes et une partie de la suivante. */
@media (max-width: 1000px) {
  .product-carousel__track {
    gap: 14px;
  }

  .product-carousel__track > * {
    flex-basis: calc((100% - 2 * 14px) / 3.25);
  }
}

/* Mobile : deux cartes environ, avec défilement tactile. */
@media (max-width: 767px) {
  .product-carousel__toolbar {
    margin-bottom: 10px;
  }

  .product-carousel__arrow {
    width: 34px;
    height: 34px;
  }

  .product-carousel__track {
    gap: 12px;
    margin-right: calc(var(--container-padding) * -1);
    padding-right: var(--container-padding);
  }

  .product-carousel__track > * {
    flex-basis: 44%;
  }

  .product-carousel__state {
    min-height: 200px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .product-carousel__track {
    scroll-behavior: auto;
  }

  .product-carousel__arrow {
    transition: none;
  }

  .product-carousel__track > * {
    animation: none;
  }
}
</style>
