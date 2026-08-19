<script setup lang="ts">
import { onMounted } from "vue";
import { useProductStore } from "../stores/productStore";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";

const productStore = useProductStore();
const cartStore = useCartStore();
const authStore = useAuthStore();

onMounted(() => {
  productStore.loadProducts();
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
  <div>
    <h1>Nos produits</h1>

    <p v-if="productStore.loading">Chargement...</p>
    <p v-else-if="productStore.error">{{ productStore.error }}</p>

    <ul v-else>
      <li v-for="product in productStore.products" :key="product.id">
        <h3>{{ product.name }}</h3>
        <p>{{ product.description }}</p>
        <strong>{{ product.price }} €</strong>
        <span> — Stock : {{ product.stockQuantity }}</span>
        <button @click="handleAddToCart(product.id)">Ajouter au panier</button>
      </li>
    </ul>
  </div>
</template>
