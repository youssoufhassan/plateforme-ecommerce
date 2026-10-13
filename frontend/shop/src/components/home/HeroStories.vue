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
const TICK = 100;

const index = ref(0);
const elapsed = ref(0);
const paused = ref(false);

let timer: ReturnType<typeof setInterval> | null = null;
let motionQuery: MediaQueryList | null = null;

const current = computed(() => props.products[index.value] ?? null);

const progress = computed(() =>
  Math.min(100, (elapsed.value / DURATION) * 100),
);

const image = computed(() => {
  const product = current.value;
  if (!product) return "";

  const path =
    product.imageUrls?.find((item) => item?.trim()) || product.imageUrl || "";

  return path ? fullImageUrl(path) : "";
});

const formattedPrice = computed(() => {
  if (!current.value) return "";

  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(current.value.price);
});

function goTo(position: number): void {
  const length = props.products.length;
  if (!length) return;

  index.value = (position + length) % length;
  elapsed.value = 0;
}

function next(): void {
  goTo(index.value + 1);
}

function previous(): void {
  goTo(index.value - 1);
}

function stop(): void {
  if (timer !== null) {
    clearInterval(timer);
    timer = null;
  }
}

function start(): void {
  stop();

  if (props.loading || props.products.length < 2 || motionQuery?.matches) {
    return;
  }

  timer = setInterval(() => {
    // Ni pendant un survol, ni quand l'onglet est en arrière-plan
    if (paused.value || document.hidden) return;

    elapsed.value += TICK;

    if (elapsed.value >= DURATION) {
      next();
    }
  }, TICK);
}

function handleMotionChange(): void {
  if (motionQuery?.matches) {
    stop();
    elapsed.value = 0;
  } else {
    start();
  }
}

/**
 * Les flèches ne pilotent le bandeau que lorsqu'il a le focus :
 * sinon elles gêneraient le défilement de la page.
 */
function handleKeydown(event: KeyboardEvent): void {
  if (event.key === "ArrowLeft") {
    event.preventDefault();
    previous();
  }

  if (event.key === "ArrowRight") {
    event.preventDefault();
    next();
  }
}

watch(
  () => [props.products, props.loading] as const,
  () => {
    if (index.value >= props.products.length) {
      index.value = 0;
    }

    elapsed.value = 0;
    start();
  },
);

onMounted(() => {
  motionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
  motionQuery.addEventListener("change", handleMotionChange);
  start();
});

onBeforeUnmount(() => {
  stop();
  motionQuery?.removeEventListener("change", handleMotionChange);
});
</script>

<template>
  <section
    v-if="loading || products.length"
    class="stories"
    aria-label="Sélection du moment"
    tabindex="-1"
    @mouseenter="paused = true"
    @mouseleave="paused = false"
    @focusin="paused = true"
    @focusout="paused = false"
    @keydown="handleKeydown"
  >
    <div v-if="loading" class="stories__skeleton" aria-hidden="true">
      <span></span>
    </div>

    <template v-else-if="current">
      <div class="stories__topline">
        <span class="stories__label">Notre sélection</span>

        <span class="stories__counter numeric">
          {{ String(index + 1).padStart(2, "0") }}
          <span aria-hidden="true">/</span>
          {{ String(products.length).padStart(2, "0") }}
        </span>
      </div>

      <div class="stories__bars" aria-hidden="true">
        <span
          v-for="(product, i) in products"
          :key="product.id"
          class="stories__bar"
        >
          <span
            class="stories__bar-fill"
            :style="{
              width: i < index ? '100%' : i === index ? `${progress}%` : '0%',
            }"
          ></span>
        </span>
      </div>

      <div class="container stories__inner">
        <!-- L'image occupe tout le cadre : c'est elle qui porte le bandeau -->
        <RouterLink
          :to="`/produits/${current.id}`"
          class="stories__visual"
          :aria-label="`Découvrir ${current.name}`"
        >
          <img
            v-if="image"
            :key="current.id"
            :src="image"
            :alt="current.name"
            class="stories__image"
          />

          <span v-else class="stories__fallback">
            {{ current.name.slice(0, 2).toUpperCase() }}
          </span>
        </RouterLink>

        <div :key="`text-${current.id}`" class="stories__text">
          <div class="stories__info">
            <p v-if="current.brand" class="stories__brand">
              {{ current.brand }}
            </p>

            <h2 class="stories__name">{{ current.name }}</h2>

            <p class="stories__price numeric">{{ formattedPrice }}</p>
          </div>

          <RouterLink :to="`/produits/${current.id}`" class="stories__cta">
            <span>Découvrir</span>

            <svg
              class="stories__cta-icon"
              viewBox="0 0 20 20"
              fill="none"
              aria-hidden="true"
            >
              <path
                d="M3.5 10H16.5M10.5 4L16.5 10L10.5 16"
                stroke="currentColor"
                stroke-width="1.3"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </RouterLink>
        </div>
      </div>

      <div class="stories__navigation">
        <button
          type="button"
          class="stories__nav"
          aria-label="Produit précédent"
          :disabled="products.length < 2"
          @click="previous"
        >
          <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M15 5L8 12L15 19"
              stroke="currentColor"
              stroke-width="1.4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>

        <button
          type="button"
          class="stories__nav"
          aria-label="Produit suivant"
          :disabled="products.length < 2"
          @click="next"
        >
          <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M9 5L16 12L9 19"
              stroke="currentColor"
              stroke-width="1.4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>
    </template>
  </section>
</template>

<style scoped>
.stories {
  position: relative;
  overflow: hidden;
  color: var(--color-text);
  background: var(--color-paper);
  border-bottom: 1px solid var(--color-line);
}

.stories:focus {
  outline: none;
}

/* =========================================================
   EN-TÊTE
   ========================================================= */

.stories__topline {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-4);

  max-width: var(--container-max);
  margin-inline: auto;
  padding: var(--space-4) var(--container-padding) 0;
}

.stories__label {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.stories__counter {
  display: flex;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

/* =========================================================
   PROGRESSION
   ========================================================= */

.stories__bars {
  display: flex;
  gap: 4px;

  max-width: var(--container-max);
  margin-inline: auto;
  padding: var(--space-3) var(--container-padding) 0;
}

.stories__bar {
  flex: 1;
  min-width: 0;
  height: 2px;
  overflow: hidden;
  background: var(--color-line);
}

.stories__bar-fill {
  display: block;
  height: 100%;
  background: var(--color-saffron);
  transition: width 100ms linear;
}

/* =========================================================
   MISE EN PAGE
   ========================================================= */

.stories__inner {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr);
  align-items: center;
  gap: clamp(24px, 4vw, 56px);
  padding-block: var(--space-5);
  padding-right: 110px;
}

/* =========================================================
   VISUEL — l'image remplit le cadre
   ========================================================= */

.stories__visual {
  position: relative;
  display: grid;
  place-items: center;

  width: 150px;
  height: 170px;

  overflow: hidden;
  background: var(--color-plate);
  text-decoration: none;
}

.stories__image {
  width: 100%;
  height: 100%;
  /* cover : le visuel porte le bandeau, quitte à rogner les bords */
  object-fit: cover;
  animation: stories-appear 450ms ease both;
  transition: transform 500ms ease;
}

.stories__visual:hover .stories__image {
  transform: scale(1.04);
}

.stories__fallback {
  font-family: var(--font-display);
  font-size: 34px;
  color: var(--color-text-muted);
  letter-spacing: 0.08em;
}

/* =========================================================
   TEXTE
   ========================================================= */

.stories__text {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-8);
  min-width: 0;
  animation: stories-appear 450ms ease both;
}

.stories__info {
  min-width: 0;
}

.stories__brand {
  margin: 0 0 var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.stories__name {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;

  font-family: var(--font-display);
  font-size: clamp(24px, 2.4vw, 34px);
  font-weight: var(--weight-regular);
  line-height: 1.15;
  letter-spacing: var(--tracking-tight);

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow-wrap: anywhere;
}

.stories__price {
  margin: var(--space-3) 0 0;
  font-size: var(--text-md);
}

/* =========================================================
   APPEL À L'ACTION
   ========================================================= */

.stories__cta {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);

  min-height: 48px;
  padding-inline: var(--space-5);

  border: 1px solid var(--color-ink);
  color: var(--color-text);
  background: transparent;

  font-size: var(--text-base);
  text-decoration: none;

  transition:
    color var(--transition-fast),
    background var(--transition-fast);
}

.stories__cta:hover {
  color: var(--color-text-inverse);
  background: var(--color-ink);
}

.stories__cta-icon {
  width: 17px;
  height: 17px;
  transition: transform var(--transition-fast);
}

.stories__cta:hover .stories__cta-icon {
  transform: translateX(3px);
}

/* =========================================================
   NAVIGATION
   ========================================================= */

.stories__navigation {
  position: absolute;
  top: 50%;
  right: max(
    var(--container-padding),
    calc((100% - var(--container-max)) / 2 + var(--container-padding))
  );
  display: flex;
  gap: var(--space-2);
  transform: translateY(-50%);
}

.stories__nav {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  padding: 0;

  border: 1px solid var(--color-line);
  border-radius: var(--radius-pill);
  color: var(--color-text);
  background: transparent;
  cursor: pointer;

  transition:
    background var(--transition-fast),
    color var(--transition-fast),
    border-color var(--transition-fast);
}

.stories__nav svg {
  width: 18px;
  height: 18px;
}

.stories__nav:hover:not(:disabled) {
  border-color: var(--color-ink);
  color: var(--color-text-inverse);
  background: var(--color-ink);
}

.stories__nav:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

/* =========================================================
   CHARGEMENT
   ========================================================= */

.stories__skeleton {
  position: relative;
  height: 250px;
  overflow: hidden;
  background: var(--color-plate);
}

.stories__skeleton span {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    100deg,
    transparent 20%,
    rgb(255 255 255 / 65%) 45%,
    transparent 70%
  );
  animation: stories-shimmer 1.5s infinite;
}

/* =========================================================
   ANIMATIONS
   ========================================================= */

@keyframes stories-appear {
  from {
    opacity: 0;
    transform: translateY(6px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes stories-shimmer {
  to {
    transform: translateX(100%);
  }
}

/* =========================================================
   TABLETTE
   ========================================================= */

@media (max-width: 900px) {
  .stories__inner {
    grid-template-columns: 125px minmax(0, 1fr);
    gap: var(--space-6);
    padding-right: var(--container-padding);
  }

  .stories__visual {
    width: 125px;
    height: 145px;
  }

  .stories__text {
    gap: var(--space-5);
  }

  .stories__cta {
    gap: var(--space-2);
    padding-inline: var(--space-4);
  }

  .stories__navigation {
    position: static;
    justify-content: flex-end;
    padding: 0 var(--container-padding) var(--space-5);
    transform: none;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 600px) {
  .stories__topline {
    padding-top: var(--space-3);
  }

  .stories__label,
  .stories__counter {
    font-size: var(--text-2xs);
  }

  .stories__bars {
    gap: 3px;
  }

  .stories__inner {
    grid-template-columns: 96px minmax(0, 1fr);
    gap: var(--space-4);
    padding-block: var(--space-4);
    padding-right: var(--container-padding);
  }

  .stories__visual {
    width: 96px;
    height: 118px;
  }

  /* Le bouton passe sous le texte : la largeur manque */
  .stories__text {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }

  .stories__brand {
    margin-bottom: var(--space-1);
    font-size: var(--text-xs);
  }

  .stories__name {
    font-size: clamp(19px, 5vw, 25px);
  }

  .stories__price {
    margin-top: var(--space-2);
    font-size: var(--text-base);
  }

  .stories__cta {
    min-height: 40px;
    padding-inline: var(--space-4);
    font-size: var(--text-sm);
  }

  .stories__cta-icon {
    width: 15px;
    height: 15px;
  }

  .stories__navigation {
    gap: var(--space-2);
    padding: 0 var(--container-padding) var(--space-4);
  }

  .stories__nav {
    width: 32px;
    height: 32px;
  }

  .stories__skeleton {
    height: 190px;
  }
}

/* =========================================================
   PRÉFÉRENCES D'ACCESSIBILITÉ
   ========================================================= */

@media (prefers-reduced-motion: reduce) {
  .stories *,
  .stories *::before,
  .stories *::after {
    animation: none !important;
    transition: none !important;
  }
}
</style>
