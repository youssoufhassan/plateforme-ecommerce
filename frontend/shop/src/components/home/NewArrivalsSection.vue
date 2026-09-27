<script setup lang="ts">
import ProductCarousel from "@/components/product/ProductCarousel.vue";
import type { Product } from "@/types/product";

withDefaults(
  defineProps<{
    products: Product[];
    loading?: boolean;
    error?: string | null;
  }>(),
  { loading: false, error: null },
);
</script>

<template>
  <section class="new-arrivals">
    <div class="new-arrivals__container container">
      <div class="new-arrivals__header">
        <div class="new-arrivals__content">
          <p class="new-arrivals__eyebrow">La sélection SIDRA</p>

          <h2 class="new-arrivals__title">Nouveautés</h2>

          <p class="new-arrivals__description">
            Découvrez les dernières fragrances ajoutées à notre sélection.
          </p>
        </div>

        <RouterLink to="/produits?sort=newest" class="new-arrivals__link">
          <span>Voir tout</span>
          <span class="new-arrivals__arrow" aria-hidden="true">→</span>
        </RouterLink>
      </div>

      <ProductCarousel :products="products" :loading="loading" :error="error" />
    </div>
  </section>
</template>

<style scoped>
.new-arrivals {
  width: 100%;
  padding-block: 40px 20px;
}

.new-arrivals__container {
  width: 100%;
}

.new-arrivals__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
  margin-bottom: 24px;
}

.new-arrivals__content {
  min-width: 0;
}

.new-arrivals__eyebrow {
  margin: 0 0 7px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.new-arrivals__title {
  margin: 0;
  font-size: clamp(26px, 2.2vw, 34px);
  font-weight: 500;
  line-height: 1.1;
  letter-spacing: -0.025em;
  color: var(--color-text);
}

.new-arrivals__description {
  max-width: 560px;
  margin: 9px 0 0;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.5;
  color: var(--color-text-muted);
}

.new-arrivals__link {
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

.new-arrivals__link:hover {
  opacity: 0.65;
  gap: 11px;
}

.new-arrivals__arrow {
  font-size: 15px;
  line-height: 1;
}

/* Tablet */
@media (max-width: 1000px) {
  .new-arrivals {
    padding-block: 36px 40px;
  }

  .new-arrivals__header {
    margin-bottom: 22px;
  }

  .new-arrivals__title {
    font-size: 30px;
  }
}

/* Mobile */
@media (max-width: 767px) {
  .new-arrivals {
    padding-block: 32px 14px;
  }

  .new-arrivals__header {
    align-items: flex-start;
    gap: 18px;
    margin-bottom: 20px;
  }

  .new-arrivals__eyebrow {
    margin-bottom: 6px;
    font-size: 10px;
  }

  .new-arrivals__title {
    font-size: 26px;
  }

  .new-arrivals__description {
    max-width: 100%;
    margin-top: 8px;
    font-size: 13px;
  }

  .new-arrivals__link {
    margin-top: 2px;
    font-size: 12px;
  }

  .new-arrivals__arrow {
    font-size: 14px;
  }
}

/* Très petits écrans */
@media (max-width: 420px) {
  .new-arrivals {
    padding-block: 28px 32px;
  }

  .new-arrivals__header {
    gap: 12px;
  }

  .new-arrivals__title {
    font-size: 24px;
  }

  .new-arrivals__description {
    font-size: 12px;
  }

  .new-arrivals__link {
    font-size: 11px;
  }
}
</style>
