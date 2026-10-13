<script setup lang="ts">
import { computed } from "vue";
import { RouterLink } from "vue-router";

import { fullImageUrl } from "@/services/productService";
import type { HomeSection } from "@/types/home";

const props = defineProps<{
  section: HomeSection;
}>();

/** Trois produits suffisent : au-delà, la colonne devient une liste. */
const products = computed(() => props.section.products.slice(0, 3));

/** Le visuel du bloc vient du premier produit, faute d'image de section. */
const coverImage = computed(() => {
  const first = props.section.products[0];
  if (!first) return "";

  const path = first.imageUrls?.find((i) => i?.trim()) || first.imageUrl || "";
  return fullImageUrl(path);
});

function money(value: number): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(value);
}

function thumbnail(imageUrls?: string[], imageUrl?: string): string {
  const path = imageUrls?.find((i) => i?.trim()) || imageUrl || "";
  return fullImageUrl(path);
}
</script>

<template>
  <section v-if="products.length" class="split">
    <div class="container split__inner">
      <!-- Visuel et accroche -->
      <RouterLink :to="`/selection/${section.slug}`" class="split__cover">
        <img
          v-if="coverImage"
          :src="coverImage"
          :alt="section.title"
          class="split__image"
          loading="lazy"
        />

        <div class="split__overlay">
          <h2 class="split__title">{{ section.title }}</h2>

          <p v-if="section.subtitle" class="split__subtitle">
            {{ section.subtitle }}
          </p>

          <span class="split__cta">Découvrir la sélection</span>
        </div>
      </RouterLink>

      <!-- Trois produits en colonne -->
      <ul class="split__list">
        <li v-for="product in products" :key="product.id">
          <RouterLink :to="`/produits/${product.id}`" class="split__product">
            <span class="split__thumb">
              <img
                :src="thumbnail(product.imageUrls, product.imageUrl)"
                :alt="''"
                loading="lazy"
              />
            </span>

            <span class="split__info">
              <span v-if="product.brand" class="split__brand">
                {{ product.brand }}
              </span>

              <span class="split__name">{{ product.name }}</span>

              <span class="split__price">{{ money(product.price) }}</span>
            </span>
          </RouterLink>
        </li>
      </ul>
    </div>
  </section>
</template>

<style scoped>
.split {
  padding-block: var(--space-12);
}

.split__inner {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-6);
}

/* =========================================================
   VISUEL
   ========================================================= */

.split__cover {
  position: relative;
  display: block;
  aspect-ratio: 4 / 3;
  background: var(--color-plate);
  overflow: hidden;
  text-decoration: none;
}

.split__image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: var(--space-8);
  transition: transform var(--transition-slow);
}

.split__cover:hover .split__image {
  transform: scale(1.03);
}

/* Le texte repose sur un voile : les visuels produit sont clairs */
.split__overlay {
  position: absolute;
  right: var(--space-6);
  bottom: var(--space-6);
  left: var(--space-6);
  padding: var(--space-5);
  background: rgba(255, 255, 255, 0.94);
}

.split__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--weight-regular);
  line-height: var(--leading-tight);
  letter-spacing: var(--tracking-tight);
}

.split__subtitle {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
}

.split__cta {
  display: inline-block;
  margin-top: var(--space-4);
  color: var(--color-text);
  font-size: var(--text-sm);
  text-decoration: underline;
  text-underline-offset: 0.25em;
}

/* =========================================================
   PRODUITS
   ========================================================= */

.split__list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin: 0;
  padding: 0;
  list-style: none;
}

.split__product {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3);
  text-decoration: none;
  transition: background var(--transition-fast);
}

.split__product:hover {
  background: var(--color-page);
}

.split__thumb {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 84px;
  height: 92px;
  background: var(--color-plate);
}

.split__thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: var(--space-2);
}

.split__info {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.split__brand {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.split__name {
  color: var(--color-text);
  font-size: var(--text-base);
  line-height: var(--leading-snug);
}

.split__price {
  margin-top: var(--space-1);
  font-size: var(--text-sm);
  font-variant-numeric: tabular-nums;
}

/* =========================================================
   DESKTOP
   ========================================================= */

@media (min-width: 1001px) {
  .split__inner {
    grid-template-columns: minmax(0, 1.3fr) minmax(0, 1fr);
    gap: var(--space-10);
    align-items: center;
  }

  .split__cover {
    aspect-ratio: 1 / 1;
  }
}

@media (max-width: 767px) {
  .split {
    padding-block: var(--space-8);
  }

  .split__overlay {
    right: var(--space-4);
    bottom: var(--space-4);
    left: var(--space-4);
    padding: var(--space-4);
  }
}
</style>
