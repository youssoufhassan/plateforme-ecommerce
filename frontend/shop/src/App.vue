<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useAuthStore } from "./stores/authStore";
import { useSearchStore } from "./stores/searchStore";
import { useCartStore } from "./stores/cartStore";
import { useRouter } from "vue-router";

const authStore = useAuthStore();
const searchStore = useSearchStore();
const cartStore = useCartStore();
const router = useRouter();
const searchInput = ref("");

onMounted(() => {
  if (authStore.isLoggedIn()) {
    cartStore.loadCart();
  }
});

const cartCount = computed(() =>
  cartStore.cart.items.reduce((sum, item) => sum + item.quantity, 0),
);

function handleSearch() {
  searchStore.query = searchInput.value;
  router.push("/produits");
}
</script>

<template>
  <div class="announce-bar">
    Livraison offerte dès 40€ d'achat — commandez maintenant.
  </div>

  <header>
    <div class="header-top">
      <router-link to="/" class="brand">Almass</router-link>

      <div class="search-wrap">
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle cx="11" cy="11" r="7" />
          <path d="M21 21l-4.3-4.3" />
        </svg>
        <input
          v-model="searchInput"
          @keyup.enter="handleSearch"
          type="text"
          placeholder="Essayez parfum, marque..."
        />
      </div>

      <div class="header-icons">
        <router-link
          v-if="!authStore.isLoggedIn()"
          to="/login"
          class="icon-block"
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.6"
          >
            <circle cx="12" cy="8" r="4" />
            <path d="M4 20c0-4 4-6 8-6s8 2 8 6" />
          </svg>
          <span>Mon compte</span>
        </router-link>
        <button v-else @click="authStore.logout()" class="icon-block">
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.6"
          >
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <path d="M16 17l5-5-5-5" />
            <path d="M21 12H9" />
          </svg>
          <span>Déconnexion</span>
        </button>

        <router-link to="/cart" class="icon-block cart-icon">
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.6"
          >
            <path d="M6 6h15l-1.5 9h-13z" />
            <circle cx="9" cy="20" r="1" />
            <circle cx="18" cy="20" r="1" />
            <path d="M6 6l-2-3H2" />
          </svg>
          <span>Mon panier</span>
          <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
        </router-link>
      </div>
    </div>

    <nav class="category-nav">
      <router-link to="/produits" class="category-link active"
        >Parfums</router-link
      >
    </nav>
  </header>

  <router-view />
</template>
