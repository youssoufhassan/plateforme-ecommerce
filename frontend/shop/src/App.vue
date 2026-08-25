<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useAuthStore } from "./stores/authStore";
import { useSearchStore } from "./stores/searchStore";
import { useCartStore } from "./stores/cartStore";

const authStore = useAuthStore();
const searchStore = useSearchStore();
const cartStore = useCartStore();
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
}
</script>

<template>
  <nav>
    <div class="nav-left">
      <router-link to="/" class="brand">parfum</router-link>
      <router-link to="/?gender=homme" class="gender-link">Homme</router-link>
      <router-link to="/?gender=femme" class="gender-link">Femme</router-link>
    </div>

    <input
      v-model="searchInput"
      @input="handleSearch"
      type="text"
      placeholder="Rechercher un article, une catégorie..."
      class="search-input"
    />

    <div class="nav-right">
      <router-link
        v-if="!authStore.isLoggedIn()"
        to="/login"
        class="icon-link"
        title="Connexion"
      >
        <svg
          width="22"
          height="22"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.6"
        >
          <circle cx="12" cy="8" r="4" />
          <path d="M4 20c0-4 4-6 8-6s8 2 8 6" />
        </svg>
      </router-link>
      <button
        v-else
        @click="authStore.logout()"
        class="icon-link"
        title="Déconnexion"
      >
        <svg
          width="22"
          height="22"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.6"
        >
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <path d="M16 17l5-5-5-5" />
          <path d="M21 12H9" />
        </svg>
      </button>

      <router-link to="/cart" class="icon-link cart-icon" title="Panier">
        <svg
          width="22"
          height="22"
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
        <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
      </router-link>
    </div>
  </nav>
  <router-view />
</template>
