<script setup lang="ts">
import { ref, onMounted, computed, watch } from "vue";
import { useAuthStore } from "./stores/authStore";
import { useSearchStore } from "./stores/searchStore";
import { useCartStore } from "./stores/cartStore";
import { useRouter, useRoute } from "vue-router";

const authStore = useAuthStore();
const searchStore = useSearchStore();
const cartStore = useCartStore();

const router = useRouter();
const route = useRoute();

const searchInput = ref("");

/* =========================================
   INITIALISATION
========================================= */

onMounted(async () => {
  // Synchronise le champ de recherche avec le store.
  searchInput.value = searchStore.query;

  if (authStore.isAuthenticated) {
    try {
      await cartStore.loadCart();
    } catch (error) {
      console.error("Impossible de charger le panier :", error);
    }
  }
});

/* =========================================
   AUTHENTIFICATION
========================================= */

/**
 * Lorsque l'utilisateur se déconnecte,
 * on vide le panier local pour éviter d'afficher
 * les données de l'ancien utilisateur.
 */
watch(
  () => authStore.isAuthenticated,
  async (isAuthenticated, wasAuthenticated) => {
    if (isAuthenticated && !wasAuthenticated) {
      try {
        await cartStore.loadCart();
      } catch (error) {
        console.error("Impossible de charger le panier :", error);
      }

      return;
    }

    if (!isAuthenticated && wasAuthenticated) {
      cartStore.clearCart();
    }
  },
);

/* =========================================
   PANIER
========================================= */

const cartCount = computed(() => {
  return cartStore.cart.items.reduce((sum, item) => sum + item.quantity, 0);
});

/* =========================================
   RECHERCHE
========================================= */

/**
 * Lance la recherche.
 */
function handleSearch() {
  const query = searchInput.value.trim();

  searchStore.setQuery(query);

  if (route.path !== "/produits") {
    router.push("/produits");
  } else {
    // Permet de repartir en haut du catalogue
    // lorsque l'utilisateur effectue une nouvelle recherche.
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }
}

/**
 * Synchronise le champ avec le store.
 *
 * Cela permet par exemple d'avoir :
 *
 * /produits
 * ? recherche provenant d'un autre composant
 *
 * sans laisser une ancienne valeur dans l'input.
 */
watch(
  () => searchStore.query,
  (value) => {
    if (value !== searchInput.value) {
      searchInput.value = value;
    }
  },
);

/**
 * Si la valeur du champ est vidée manuellement,
 * on garde le store cohérent.
 */
function handleSearchInput() {
  if (!searchInput.value.trim()) {
    searchStore.clearSearch();
  }
}
</script>

<template>
  <div class="app">
    <!-- =====================================
         BARRE D'ANNONCE
    ====================================== -->

    <div class="announce-bar">
      Livraison offerte dès 40€ d'achat — commandez maintenant.
    </div>

    <!-- =====================================
         HEADER
    ====================================== -->

    <header class="site-header">
      <div class="header-top">
        <!-- LOGO -->

        <router-link to="/" class="brand"> SHAHIN </router-link>

        <!-- RECHERCHE -->

        <form class="search-wrap" @submit.prevent="handleSearch">
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            aria-hidden="true"
          >
            <circle cx="11" cy="11" r="7" />
            <path d="M21 21l-4.3-4.3" />
          </svg>

          <input
            v-model="searchInput"
            type="search"
            placeholder="Essayez parfum, marque..."
            aria-label="Rechercher un produit"
            autocomplete="off"
            @input="handleSearchInput"
          />

          <button
            v-if="searchInput.trim()"
            type="button"
            class="search-clear"
            aria-label="Effacer la recherche"
            @click="
              searchInput = '';
              searchStore.clearSearch();
            "
          >
            ×
          </button>
        </form>

        <!-- ACTIONS -->

        <div class="header-icons">
          <!-- =================================
               COMPTE
          ================================== -->

          <router-link
            v-if="!authStore.isAuthenticated"
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
              aria-hidden="true"
            >
              <circle cx="12" cy="8" r="4" />
              <path d="M4 20c0-4 4-6 8-6s8 2 8 6" />
            </svg>

            <span>Mon compte</span>
          </router-link>

          <router-link v-else to="/account" class="icon-block">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              aria-hidden="true"
            >
              <circle cx="12" cy="8" r="4" />
              <path d="M4 20c0-4 4-6 8-6s8 2 8 6" />
            </svg>

            <span>Mon compte</span>
          </router-link>

          <!-- =================================
               PANIER
          ================================== -->

          <router-link to="/cart" class="icon-block cart-icon">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              aria-hidden="true"
            >
              <path d="M6 6h15l-1.5 9h-13z" />

              <circle cx="9" cy="20" r="1" />

              <circle cx="18" cy="20" r="1" />

              <path d="M6 6l-2-3H2" />
            </svg>

            <span>Mon panier</span>

            <span
              v-if="cartCount > 0"
              class="cart-badge"
              :aria-label="`${cartCount} article${
                cartCount > 1 ? 's' : ''
              } dans le panier`"
            >
              {{ cartCount }}
            </span>
          </router-link>
        </div>
      </div>
    </header>

    <!-- =====================================
         CONTENU DES ROUTES
    ====================================== -->

    <main class="app-content">
      <router-view />
    </main>
  </div>
</template>
