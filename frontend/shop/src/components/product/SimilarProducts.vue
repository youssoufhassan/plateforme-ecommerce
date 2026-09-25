<script setup lang="ts">
import { ref, watch } from "vue";

import ProductCarousel from "@/components/product/ProductCarousel.vue";
import { fetchSimilarProducts } from "@/services/productService";
import type { Product } from "@/types/product";

const props = defineProps<{ productId: string }>();

const products = ref<Product[]>([]);
const loading = ref(false);

watch(
  () => props.productId,
  async (id) => {
    if (!id) return;

    loading.value = true;

    try {
      products.value = await fetchSimilarProducts(id, 6);
    } catch {
      // Section secondaire : en cas d'échec, on la masque simplement
      products.value = [];
    } finally {
      loading.value = false;
    }
  },
  { immediate: true },
);
</script>

<template>
  <!-- Masquée si aucune suggestion : mieux vaut rien qu'un bloc vide -->
  <section v-if="loading || products.length" class="similar">
    <div class="container">
      <h2 class="similar__title">Vous aimerez aussi</h2>
      <ProductCarousel
        :products="products"
        :loading="loading"
        :skeleton-count="5"
      />
    </div>
  </section>
</template>

<style scoped>
.similar {
  margin-top: 64px;
  padding-top: 40px;
  border-top: 1px solid var(--color-border);
}

.similar__title {
  margin: 0 0 22px;
  font-family: var(--font-display);
  font-size: clamp(22px, 2.2vw, 30px);
  font-weight: 400;
  line-height: 1.1;
  letter-spacing: -0.025em;
  color: var(--color-text);
}

@media (max-width: 767px) {
  .similar {
    margin-top: 48px;
    padding-top: 32px;
  }

  .similar__title {
    margin-bottom: 18px;
  }
}
</style>
