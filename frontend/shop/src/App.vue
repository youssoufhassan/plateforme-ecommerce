<script setup lang="ts">
import { ref } from "vue";
import { useAuthStore } from "./stores/authStore";
import { useSearchStore } from "./stores/searchStore";

const authStore = useAuthStore();
const searchStore = useSearchStore();
const searchInput = ref("");

function handleSearch() {
  searchStore.query = searchInput.value;
}
</script>

<template>
  <nav>
    <router-link to="/" class="brand">Almass</router-link>

    <input
      v-model="searchInput"
      @input="handleSearch"
      type="text"
      placeholder="Rechercher un produit..."
      class="search-input"
    />

    <span class="nav-links">
      <span v-if="authStore.isLoggedIn()">
        <router-link to="/cart" class="icon-link" title="Panier">
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
          >
            <path d="M6 6h15l-1.5 9h-13z" />
            <circle cx="9" cy="20" r="1" />
            <circle cx="18" cy="20" r="1" />
            <path d="M6 6l-2-3H2" />
          </svg>
        </router-link>
        <button
          @click="authStore.logout()"
          class="icon-link"
          title="Déconnexion"
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
          >
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <path d="M16 17l5-5-5-5" />
            <path d="M21 12H9" />
          </svg>
        </button>
      </span>
      <router-link v-else to="/login" class="icon-link" title="Connexion">
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
        >
          <circle cx="12" cy="8" r="4" />
          <path d="M4 20c0-4 4-6 8-6s8 2 8 6" />
        </svg>
      </router-link>
    </span>
  </nav>
  <router-view />
</template>
