<script setup lang="ts">
import { RouterLink } from "vue-router";

import ProductCarousel from "@/components/product/ProductCarousel.vue";
import type { HomeSection } from "@/types/home";

defineProps<{
  section: HomeSection;
  onAdd?: (payload: { productId: string; variantId: string }) => Promise<void>;
}>();
</script>

<template>
  <section class="editorial">
    <div class="editorial__container container">
      <div class="editorial__header">
        <div class="editorial__content">
          <p class="editorial__eyebrow">La sélection SHAHIN</p>

          <h2 class="editorial__title">{{ section.title }}</h2>

          <p v-if="section.subtitle" class="editorial__description">
            {{ section.subtitle }}
          </p>
        </div>

        <!-- Le lien n'apparaît que s'il reste des produits à découvrir -->
        <RouterLink
          v-if="section.totalCount > section.products.length"
          :to="`/selection/${section.slug}`"
          class="editorial__link"
        >
          <span>Voir tout</span>
          <span class="editorial__arrow" aria-hidden="true">→</span>
        </RouterLink>
      </div>

      <ProductCarousel :products="section.products" :on-add="onAdd" />
    </div>
  </section>
</template>

<style scoped>
.editorial {
  width: 100%;
  padding-block: 40px 20px;
}

.editorial__container {
  width: 100%;
}

.editorial__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
  margin-bottom: 24px;
}

.editorial__content {
  min-width: 0;
}

.editorial__eyebrow {
  margin: 0 0 7px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.editorial__title {
  margin: 0;
  font-size: clamp(26px, 2.2vw, 34px);
  font-weight: 500;
  line-height: 1.1;
  letter-spacing: -0.025em;
  color: var(--color-text);
}

.editorial__description {
  max-width: 560px;
  margin: 9px 0 0;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.5;
  color: var(--color-text-muted);
}

.editorial__link {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 3px;
  border-bottom: 1px solid var(--color-text);
  color: var(--color-text);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
  text-decoration: none;
  transition:
    opacity 0.2s ease,
    gap 0.2s ease;
}

.editorial__link:hover {
  opacity: 0.65;
  gap: 11px;
}

.editorial__arrow {
  font-size: 15px;
  line-height: 1;
}

/* Tablet */
@media (max-width: 1000px) {
  .editorial {
    padding-block: 36px 40px;
  }

  .editorial__header {
    margin-bottom: 22px;
  }

  .editorial__title {
    font-size: 30px;
  }
}

/* Mobile */
@media (max-width: 767px) {
  .editorial {
    padding-block: 32px 14px;
  }

  .editorial__header {
    align-items: flex-start;
    gap: 18px;
    margin-bottom: 20px;
  }

  .editorial__eyebrow {
    margin-bottom: 6px;
    font-size: 10px;
  }

  .editorial__title {
    font-size: 26px;
  }

  .editorial__description {
    max-width: 100%;
    margin-top: 8px;
    font-size: 13px;
  }

  .editorial__link {
    margin-top: 2px;
    font-size: 12px;
  }

  .editorial__arrow {
    font-size: 14px;
  }
}

/* Très petits écrans */
@media (max-width: 420px) {
  .editorial {
    padding-block: 28px 32px;
  }

  .editorial__header {
    gap: 12px;
  }

  .editorial__title {
    font-size: 24px;
  }

  .editorial__description {
    font-size: 12px;
  }

  .editorial__link {
    font-size: 11px;
  }
}
</style>
