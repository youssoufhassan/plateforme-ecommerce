<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { RouterLink } from "vue-router";
import { fullImageUrl } from "@/services/productService";
import type { Product } from "@/types/product";

const props = defineProps<{
  products: Product[];
  loading?: boolean;
}>();

const DURATION = 5000;
const index = ref(0);
const paused = ref(false);
const elapsed = ref(0);

let timer: ReturnType<typeof setInterval> | undefined;
let lastTick = 0;
let motionQuery: MediaQueryList | undefined;

const stories = computed(() =>
  (Array.isArray(props.products) ? props.products : []).map((product) => {
    const paths = [
      ...(Array.isArray(product.imageUrls) ? product.imageUrls : []),
      ...(product.imageUrl ? [product.imageUrl] : []),
    ].filter(
      (image): image is string =>
        typeof image === "string" && image.trim().length > 0,
    );

    const image = [...new Set(paths)][0];

    return {
      product,
      image: image ? fullImageUrl(image) : "",
    };
  }),
);

const current = computed(() => stories.value[index.value] ?? null);

const price = computed(() => {
  const value = Number(current.value?.product.price ?? 0);

  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(Number.isFinite(value) ? value : 0);
});

const progress = computed(() =>
  stories.value.map((_, i) => {
    if (i < index.value) return 100;
    if (i > index.value) return 0;
    return Math.min(100, (elapsed.value / DURATION) * 100);
  }),
);

function stopTimer() {
  if (timer !== undefined) {
    clearInterval(timer);
    timer = undefined;
  }
}

function startTimer() {
  stopTimer();

  if (
    paused.value ||
    props.loading ||
    stories.value.length < 2 ||
    motionQuery?.matches ||
    document.hidden
  ) {
    return;
  }

  lastTick = Date.now();

  timer = setInterval(() => {
    const now = Date.now();
    elapsed.value += now - lastTick;
    lastTick = now;

    if (elapsed.value >= DURATION) next();
  }, 100);
}

function goTo(target: number) {
  const length = stories.value.length;
  if (!length) return;

  index.value = (target + length) % length;
  elapsed.value = 0;
  startTimer();
}

function next() {
  if (index.value < stories.value.length - 1) {
    goTo(index.value + 1);
  } else {
    stopTimer();
    elapsed.value = DURATION;
  }
}

function previous() {
  goTo(index.value - 1);
}

function togglePause() {
  paused.value = !paused.value;
  paused.value ? stopTimer() : startTimer();
}

function onVisibilityChange() {
  document.hidden ? stopTimer() : startTimer();
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === "ArrowRight") next();
  if (event.key === "ArrowLeft") previous();
}

watch(
  () => props.products,
  () => {
    index.value = 0;
    elapsed.value = 0;
    startTimer();
  },
  { deep: true },
);

watch(() => props.loading, startTimer);

watch(
  () => stories.value.length,
  (length) => {
    if (!length || index.value >= length) {
      index.value = 0;
      elapsed.value = 0;
    }
    startTimer();
  },
);

onMounted(() => {
  motionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
  motionQuery.addEventListener("change", startTimer);
  document.addEventListener("visibilitychange", onVisibilityChange);
  window.addEventListener("keydown", onKeydown);
  startTimer();
});

onBeforeUnmount(() => {
  stopTimer();
  motionQuery?.removeEventListener("change", startTimer);
  document.removeEventListener("visibilitychange", onVisibilityChange);
  window.removeEventListener("keydown", onKeydown);
});
</script>

<template>
  <section
    v-if="loading || current"
    class="product-stories"
    aria-label="Sélection de produits"
  >
    <div v-if="loading && !current" class="product-stories__loading" />

    <div
      v-else-if="current"
      class="product-stories__layout"
      @mouseenter="stopTimer"
      @mouseleave="startTimer"
      @focusin="stopTimer"
      @focusout="startTimer"
    >
      <div class="product-stories__visual">
        <img
          v-if="current.image"
          :key="current.image"
          :src="current.image"
          :alt="current.product.name"
          class="product-stories__image"
        />

        <div v-else class="product-stories__image-fallback">
          {{ current.product.name }}
        </div>

        <div v-if="stories.length > 1" class="product-stories__image-nav">
          <button
            type="button"
            aria-label="Produit précédent"
            :disabled="index === 0"
            @click="previous"
          >
            <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path
                d="m15 18-6-6 6-6"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>

          <button
            type="button"
            aria-label="Produit suivant"
            :disabled="index === stories.length - 1"
            @click="next"
          >
            <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path
                d="m9 18 6-6-6-6"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>
        </div>
      </div>

      <div class="product-stories__info">
        <span v-if="current.product.brand" class="product-stories__brand">
          {{ current.product.brand }}
        </span>

        <h2>{{ current.product.name }}</h2>

        <p class="product-stories__price">{{ price }}</p>

        <RouterLink
          :to="`/produits/${current.product.id}`"
          class="product-stories__link"
        >
          Découvrir le produit
          <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M5 12h14m-6-6 6 6-6 6"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </RouterLink>

        <div v-if="stories.length > 1" class="product-stories__controls">
          <div class="product-stories__dots">
            <button
              v-for="(_, i) in stories"
              :key="i"
              type="button"
              class="product-stories__dot"
              :class="{ 'product-stories__dot--active': i === index }"
              :aria-label="`Afficher le produit ${i + 1}`"
              :aria-current="i === index ? 'step' : undefined"
              @click="goTo(i)"
            />
          </div>

          <button
            type="button"
            class="product-stories__pause"
            :aria-label="paused ? 'Reprendre le défilement' : 'Mettre en pause'"
            @click="togglePause"
          >
            <svg
              v-if="!paused"
              viewBox="0 0 24 24"
              fill="none"
              aria-hidden="true"
            >
              <path
                d="M9 6v12M15 6v12"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
              />
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path
                d="m9 6 10 6-10 6V6Z"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linejoin="round"
              />
            </svg>
          </button>
        </div>
      </div>

      <div
        v-if="stories.length > 1"
        class="product-stories__progress"
        aria-hidden="true"
      >
        <span
          v-for="(value, i) in progress"
          :key="i"
          class="product-stories__progress-item"
          :class="{ 'product-stories__progress-item--active': i === index }"
        >
          <span :style="{ width: `${value}%` }" />
        </span>
      </div>
    </div>
  </section>
</template>

<style scoped>
.product-stories {
  width: 100%;
  color: var(--color-ink, #262626);
}

/* Section principale */
.product-stories__layout {
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  width: 100%;
  height: 210px;
  min-height: 0;
  overflow: hidden;
  background: var(--color-surface, #fff);
}

/* Côté image */
.product-stories__visual {
  position: relative;
  display: grid;
  min-width: 0;
  min-height: 0;
  height: 210px;
  place-items: center;
  overflow: hidden;
  background: var(--color-page, #f7f7f5);
}

.product-stories__image {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 8px;
  object-fit: contain;
  object-position: center;
  animation: product-appear 300ms ease both;
}

.product-stories__image-fallback {
  padding: 8px;
  font-family: var(--font-display, Georgia, serif);
  font-size: 18px;
  text-align: center;
}

/* Navigation sur l'image */
.product-stories__image-nav {
  position: absolute;
  right: 8px;
  bottom: 8px;
  display: flex;
  gap: 5px;
}

.product-stories__image-nav button,
.product-stories__pause {
  display: grid;
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  place-items: center;
  border: 1px solid var(--color-line, #deded8);
  border-radius: 50%;
  background: var(--color-surface, #fff);
  color: var(--color-ink, #262626);
  cursor: pointer;
}

.product-stories__image-nav button:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.product-stories__image-nav svg,
.product-stories__pause svg {
  width: 14px;
  height: 14px;
}

/* Côté texte */
.product-stories__info {
  display: flex;
  min-width: 0;
  min-height: 0;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 8px 16px;
  overflow: hidden;
}

.product-stories__brand {
  max-width: 100%;
  margin: 0 0 5px;
  color: var(--color-text-muted, #777);
  font-size: 9px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-stories__info h2 {
  max-width: 100%;
  margin: 0;
  font-family: var(--font-display, Georgia, "Times New Roman", serif);
  font-size: clamp(15px, 1.5vw, 20px);
  font-weight: 400;
  line-height: 1.12;
  letter-spacing: -0.02em;
  overflow-wrap: anywhere;
}

.product-stories__price {
  margin: 6px 0 0;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.product-stories__link {
  display: inline-flex;
  max-width: 100%;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding-bottom: 3px;
  border-bottom: 1px solid var(--color-ink, #262626);
  color: var(--color-ink, #262626);
  font-size: 10px;
  text-decoration: none;
}

.product-stories__link svg {
  width: 13px;
  height: 13px;
  flex: 0 0 auto;
  transition: transform 180ms ease;
}

.product-stories__link:hover svg {
  transform: translateX(3px);
}

/* Commandes et indicateurs */
.product-stories__controls {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.product-stories__dots {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px;
}

.product-stories__dot {
  width: 5px;
  height: 5px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: var(--color-line, #deded8);
  cursor: pointer;
}

.product-stories__dot--active {
  background: var(--color-ink, #262626);
}

.product-stories__pause {
  width: 24px;
  height: 24px;
}

.product-stories__pause svg {
  width: 12px;
  height: 12px;
}

/* Barre de progression */
.product-stories__progress {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  gap: 2px;
  height: 2px;
}

.product-stories__progress-item {
  flex: 1;
  overflow: hidden;
  background: var(--color-line, #deded8);
}

.product-stories__progress-item span {
  display: block;
  height: 100%;
  background: var(--color-ink, #262626);
  transition: width 100ms linear;
}

/* Chargement */
.product-stories__loading {
  width: 100%;
  height: 210px;
  min-height: 0;
  background: var(--color-page, #f7f7f5);
  animation: loading-pulse 1.4s ease-in-out infinite alternate;
}

/* Animations */
@keyframes product-appear {
  from {
    opacity: 0.7;
  }

  to {
    opacity: 1;
  }
}

@keyframes loading-pulse {
  from {
    opacity: 0.6;
  }

  to {
    opacity: 1;
  }
}

/* Tablettes et mobiles */
@media (max-width: 760px) {
  .product-stories__layout {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    height: 210px;
    min-height: 0;
  }

  .product-stories__visual {
    height: 210px;
    min-height: 0;
  }

  .product-stories__image {
    height: 100%;
    min-height: 0;
    padding: 6px;
  }

  .product-stories__image-nav {
    right: 5px;
    bottom: 5px;
    gap: 4px;
  }

  .product-stories__image-nav button {
    width: 24px;
    height: 24px;
  }

  .product-stories__info {
    padding: 7px 10px;
  }

  .product-stories__brand {
    margin-bottom: 4px;
    font-size: 8px;
    letter-spacing: 0.06em;
  }

  .product-stories__info h2 {
    font-size: clamp(13px, 3vw, 18px);
    line-height: 1.1;
  }

  .product-stories__price {
    margin-top: 5px;
    font-size: 11px;
  }

  .product-stories__link {
    gap: 4px;
    margin-top: 6px;
    padding-bottom: 2px;
    font-size: 9px;
  }

  .product-stories__link svg {
    width: 11px;
    height: 11px;
  }

  .product-stories__controls {
    gap: 6px;
    margin-top: 6px;
  }

  .product-stories__dots {
    gap: 4px;
  }

  .product-stories__dot {
    width: 4px;
    height: 4px;
  }

  .product-stories__pause {
    width: 22px;
    height: 22px;
  }

  .product-stories__loading {
    height: 210px;
    min-height: 0;
  }
}

/* Très petits écrans */
@media (max-width: 380px) {
  .product-stories__info {
    padding: 6px 8px;
  }

  .product-stories__info h2 {
    font-size: 13px;
  }

  .product-stories__brand {
    font-size: 7px;
  }

  .product-stories__link {
    gap: 3px;
    font-size: 8px;
  }
}

/* Accessibilité */
@media (prefers-reduced-motion: reduce) {
  .product-stories__image,
  .product-stories__loading {
    animation: none;
  }

  .product-stories__progress-item span,
  .product-stories__link svg {
    transition: none;
  }
}
</style>
