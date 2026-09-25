<script setup lang="ts">
import { computed } from "vue";

import type { Product } from "@/services/productService";
import { fullImageUrl } from "@/services/productService";

const props = defineProps<{
  brand: string;
  product: Product;
}>();

const imageUrl = computed(() => {
  const image =
    props.product.imageUrls?.find((item) => item?.trim()) ||
    props.product.imageUrl ||
    "";

  return fullImageUrl(image);
});
</script>

<template>
  <article class="brand-card">
    <div class="brand-card__image-wrapper">
      <img
        v-if="imageUrl"
        :src="imageUrl"
        :alt="`Découvrez ${brand}`"
        class="brand-card__image"
        loading="lazy"
      />

      <div v-else class="brand-card__placeholder">
        {{ brand.charAt(0) }}
      </div>

      <div class="brand-card__overlay"></div>

      <div class="brand-card__content">
        <span class="brand-card__eyebrow"> MAISON </span>

        <h3 class="brand-card__name">
          {{ brand }}
        </h3>

        <span class="brand-card__discover">
          Découvrir
          <span>→</span>
        </span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.brand-card {
  width: 100%;
}

.brand-card__image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 0.82;
  overflow: hidden;
  background: #ebe9e5;
}

.brand-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 600ms cubic-bezier(0.22, 1, 0.36, 1);
}

.brand-card:hover .brand-card__image {
  transform: scale(1.04);
}

.brand-card__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;

  font-family: var(--font-display);
  font-size: 70px;
  font-weight: 300;
  color: var(--color-text-muted);
}

.brand-card__overlay {
  position: absolute;
  inset: 0;

  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.72) 0%,
    rgba(0, 0, 0, 0.18) 55%,
    rgba(0, 0, 0, 0) 100%
  );
}

.brand-card__content {
  position: absolute;
  right: 24px;
  bottom: 24px;
  left: 24px;

  color: var(--color-white);
}

.brand-card__eyebrow {
  display: block;
  margin-bottom: 8px;

  font-size: 9px;
  font-weight: 600;
  letter-spacing: 0.18em;
  opacity: 0.8;
}

.brand-card__name {
  margin-bottom: 16px;

  font-family: var(--font-display);
  font-size: clamp(24px, 2.2vw, 32px);
  font-weight: 400;
  line-height: 1.05;
  letter-spacing: -0.025em;
}

.brand-card__discover {
  display: inline-flex;
  align-items: center;
  gap: 9px;

  padding-bottom: 5px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.75);

  font-size: 9px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.brand-card__discover span {
  font-size: 14px;

  transition: transform var(--transition-base);
}

.brand-card:hover .brand-card__discover span {
  transform: translateX(4px);
}

@media (max-width: 767px) {
  .brand-card__image-wrapper {
    aspect-ratio: 0.9;
  }

  .brand-card__content {
    right: 20px;
    bottom: 20px;
    left: 20px;
  }

  .brand-card__name {
    font-size: 25px;
  }
}
</style>
