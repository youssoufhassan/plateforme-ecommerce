<script setup lang="ts">
import { onMounted } from "vue";
import { useProductStore } from "./stores/productStore";

const productStore = useProductStore();

onMounted(() => {
  productStore.loadProducts();
});
</script>

<template>
  <main>
    <h1>Nos produits</h1>

    <p v-if="productStore.loading">Chargement...</p>
    <p v-else-if="productStore.error">{{ productStore.error }}</p>

    <ul v-else>
      <li v-for="product in productStore.products" :key="product.id">
        <h3>{{ product.name }}</h3>
        <p>{{ product.description }}</p>
        <strong>{{ product.price }} €</strong>
        <span> — Stock : {{ product.stockQuantity }}</span>
      </li>
    </ul>
  </main>
</template>
