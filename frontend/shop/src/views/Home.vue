<script setup lang="ts">
import { onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useProductStore } from "../stores/productStore";
import { useAuthStore } from "../stores/authStore";
import { useCartStore } from "../stores/cartStore";

const router = useRouter();
const productStore = useProductStore();
const authStore = useAuthStore();
const cartStore = useCartStore();

onMounted(() => {
  productStore.loadProducts();
});

const featured = computed(() => productStore.products[0]);

async function handleAdd() {
  if (!featured.value) return;
  if (!authStore.isLoggedIn()) {
    alert("Connecte-toi d'abord pour ajouter au panier");
    return;
  }
  await cartStore.addItem(featured.value.id, 1);
  alert("Ajouté au panier !");
}
</script>

<template>
  <div class="hero">
    <img
      src="https://images.unsplash.com/photo-1758225502621-9102d2856dc8?w=1600&h=900&fit=crop&auto=format"
      alt="Almass"
    />

    <div class="hero-card" v-if="featured">
      <h3>{{ featured.name }}</h3>
      <p>{{ featured.description }}</p>
      <button class="hero-add" @click="handleAdd">
        AJOUTER — {{ featured.price }} €
      </button>
    </div>
  </div>

  <section class="intro-section">
    <h1>Les parfums Almass.<br />Sentez le monde autrement.</h1>
    <p>Trouvez un parfum qui vous ressemble.</p>
    <button class="primary intro-btn" @click="router.push('/produits')">
      Découvrir tous les parfums
    </button>
  </section>
</template>
