<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useProductStore } from "../stores/productStore";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";

const productStore = useProductStore();
const cartStore = useCartStore();
const authStore = useAuthStore();
const activeCategory = ref("Tous");

onMounted(() => {
  productStore.loadProducts();
});

const categories = computed(() => {
  const set = new Set(productStore.products.map((p) => p.categoryName));
  return ["Tous", ...Array.from(set)];
});

const filteredProducts = computed(() => {
  if (activeCategory.value === "Tous") return productStore.products;
  return productStore.products.filter(
    (p) => p.categoryName === activeCategory.value,
  );
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
  <div class="container">
    <h1>Découvre nos produits</h1>
    <p style="color: var(--ink-soft)">
      Parfums et produits venus d'ailleurs, sélectionnés pour toi.
    </p>

    <p v-if="productStore.loading">Chargement...</p>
    <p v-else-if="productStore.error">{{ productStore.error }}</p>

    <template v-else>
      <div class="category-pills">
        <button
          v-for="cat in categories"
          :key="cat"
          class="category-pill"
          :class="{ active: activeCategory === cat }"
          @click="activeCategory = cat"
        >
          {{ cat }}
        </button>
      </div>

      <div class="product-grid">
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
    </template>
  </div>
</template>
