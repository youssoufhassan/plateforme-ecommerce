<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { useProductStore } from "../stores/productStore";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";
import { fullImageUrl } from "../services/productService";

const route = useRoute();
const productStore = useProductStore();
const cartStore = useCartStore();
const authStore = useAuthStore();

onMounted(() => {
  if (productStore.products.length === 0) {
    productStore.loadProducts();
  }
});

const product = computed(() =>
  productStore.products.find((p) => p.id === route.params.id),
);

const gallery = computed(() => {
  if (!product.value) return [];
  return product.value.imageUrls?.length
    ? product.value.imageUrls
    : [product.value.imageUrl];
});

const activeImage = ref(0);

async function handleAdd() {
  if (!authStore.isLoggedIn()) {
    alert("Connecte-toi d'abord pour ajouter au panier");
    return;
  }
  if (!product.value) return;
  await cartStore.addItem(product.value.id, 1);
  alert("Ajouté au panier !");
}
</script>

<template>
  <div class="container">
    <router-link to="/produits" class="back-link"
      >← Retour aux produits</router-link
    >

    <div v-if="product" class="product-detail">
      <div class="gallery">
        <img
          :src="fullImageUrl(gallery[activeImage])"
          :alt="product.name"
          class="gallery-main"
        />
        <div v-if="gallery.length > 1" class="gallery-thumbs">
          <button
            v-for="(img, i) in gallery"
            :key="i"
            class="gallery-thumb"
            :class="{ active: activeImage === i }"
            @click="activeImage = i"
          >
            <img
              :src="fullImageUrl(img)"
              :alt="`${product.name} - vue ${i + 1}`"
            />
          </button>
        </div>
      </div>

      <div class="product-detail-info">
        <span class="section-eyebrow">{{ product.categoryName }}</span>
        <h1>{{ product.name }}</h1>
        <p class="product-detail-price">{{ product.price.toFixed(2) }} €</p>
        <p class="product-detail-description">{{ product.description }}</p>
        <p class="product-detail-stock">
          {{
            product.stockQuantity > 0
              ? `En stock (${product.stockQuantity})`
              : "Épuisé"
          }}
        </p>
        <button
          class="primary"
          :disabled="product.stockQuantity <= 0"
          @click="handleAdd"
        >
          Ajouter au panier
        </button>
      </div>
    </div>

    <p v-else>Produit introuvable.</p>
  </div>
</template>
