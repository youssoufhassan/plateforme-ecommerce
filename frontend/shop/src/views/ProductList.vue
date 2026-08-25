<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useProductStore } from "../stores/productStore";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";
import { useSearchStore } from "../stores/searchStore";
import { useCategoryStore } from "../stores/categoryStore";

const productStore = useProductStore();
const cartStore = useCartStore();
const authStore = useAuthStore();
const searchStore = useSearchStore();
const categoryStore = useCategoryStore();
const activeCategory = ref("Tous");

onMounted(() => {
  productStore.loadProducts();
  categoryStore.loadCategories();
});

const categories = computed(() => [
  "Tous",
  ...categoryStore.categories.map((c) => c.name),
]);

const filteredProducts = computed(() => {
  let list = productStore.products;
  if (activeCategory.value !== "Tous") {
    list = list.filter((p) => p.categoryName === activeCategory.value);
  }
  if (searchStore.query.trim()) {
    const q = searchStore.query.toLowerCase();
    list = list.filter((p) => p.name.toLowerCase().includes(q));
  }
  return list;
});

async function handleAddToCart(productId: string) {
  if (!authStore.isLoggedIn()) {
    alert("Connecte-toi d'abord pour ajouter au panier");
    return;
  }
  await cartStore.addItem(productId, 1);
  alert("Ajouté au panier !");
}
</script>

<template>
  <div class="shop-layout">
    <aside class="filter-sidebar">
      <h4>Catégorie</h4>
      <button
        v-for="cat in categories"
        :key="cat"
        class="filter-item"
        :class="{ active: activeCategory === cat }"
        @click="activeCategory = cat"
      >
        {{ cat }}
      </button>
    </aside>

    <main>
      <h1>Découvre nos produits</h1>
      <p style="color: var(--ink-soft); margin-top: 0.4rem">
        Parfums et produits venus d'ailleurs, sélectionnés pour toi.
      </p>

      <p v-if="productStore.loading">Chargement...</p>
      <p v-else-if="productStore.error">{{ productStore.error }}</p>

      <div v-else class="product-grid" style="margin-top: 1.5rem">
        <div
          class="product-card"
          v-for="product in filteredProducts"
          :key="product.id"
        >
          <img :src="product.imageUrl" :alt="product.name" />
          <div class="product-card-body">
            <h3>{{ product.name }}</h3>
            <p>{{ product.description }}</p>
            <span class="product-price">{{ product.price }} €</span>
            <button class="primary" @click="handleAddToCart(product.id)">
              Ajouter au panier
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
