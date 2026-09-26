<script setup lang="ts">
import { ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";

import SearchIcon from "@/components/icons/SearchIcon.vue";
import HeartIcon from "@/components/icons/HeartIcon.vue";
import BagIcon from "@/components/icons/BagIcon.vue";
import MenuIcon from "@/components/icons/MenuIcon.vue";
import { useCartStore } from "@/stores/cartStore";

import MobileMenu from "./MobileMenu.vue";

const router = useRouter();
const route = useRoute();
const cartStore = useCartStore();

const menuOpen = ref(false);
const searchQuery = ref("");

function openMenu() {
  menuOpen.value = true;
}

function closeMenu() {
  menuOpen.value = false;
}

function submitSearch() {
  const query = searchQuery.value.trim();
  if (!query) return;

  // « q » est le paramètre attendu par GET /api/products/search
  router.push({ path: "/produits", query: { q: query } });
}

// Le champ reflète la recherche en cours, y compris après un rechargement
watch(
  () => route.query.q,
  (value) => {
    searchQuery.value = (value as string) || "";
  },
  { immediate: true },
);
</script>

<template>
  <header class="site-header">
    <div class="site-header__inner">
      <!-- MENU + LOGO -->
      <div class="site-header__brand">
        <button
          type="button"
          class="header-icon"
          aria-label="Ouvrir le menu"
          :aria-expanded="menuOpen"
          @click="openMenu"
        >
          <MenuIcon />
        </button>

        <RouterLink
          to="/"
          class="site-header__logo"
          aria-label="SHAHIN - Accueil"
        >
          SHAHIN
        </RouterLink>
      </div>

      <!-- RECHERCHE -->
      <form class="site-header__search" @submit.prevent="submitSearch">
        <SearchIcon class="site-header__search-icon" />

        <input
          v-model="searchQuery"
          type="search"
          name="q"
          placeholder="Rechercher un parfum, une marque"
          aria-label="Rechercher"
        />
      </form>

      <!-- FAVORIS + CONNEXION + PANIER -->
      <div class="site-header__actions">
        <RouterLink to="/favoris" class="header-icon" aria-label="Mes favoris">
          <HeartIcon />
        </RouterLink>

        <RouterLink
          to="/connexion"
          class="header-icon"
          aria-label="Se connecter"
        >
          <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle
              cx="12"
              cy="8"
              r="3.5"
              stroke="currentColor"
              stroke-width="1.5"
            />
            <path
              d="M5.5 20c.8-3.4 3-5.2 6.5-5.2s5.7 1.8 6.5 5.2"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
        </RouterLink>

        <RouterLink
          to="/panier"
          class="header-icon header-icon--cart"
          :aria-label="
            cartStore.count
              ? `Mon panier, ${cartStore.count} article${cartStore.count > 1 ? 's' : ''}`
              : 'Mon panier'
          "
        >
          <BagIcon />

          <span v-if="cartStore.count" class="header-icon__badge">
            {{ cartStore.count }}
          </span>
        </RouterLink>
      </div>
    </div>
  </header>

  <MobileMenu :open="menuOpen" @close="closeMenu" />
</template>

<style scoped>
/* =========================================================
   HEADER
   ========================================================= */

.site-header {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;

  z-index: var(--z-header);

  width: 100%;

  background: #ffffff;

  border-bottom: 1px solid var(--color-border);
}

/* =========================================================
   STRUCTURE PRINCIPALE

   [ ☰ SHAHIN ] [ RECHERCHE FLEXIBLE ] [ ♡ 👤 🛍 ]
   ========================================================= */

.site-header__inner {
  display: grid;

  grid-template-columns:
    auto
    minmax(0, 1fr)
    auto;

  align-items: center;

  width: 100%;
  height: var(--header-height);

  padding-inline: var(--container-padding);

  background: #ffffff;
}

/* =========================================================
   MENU + LOGO
   ========================================================= */

.site-header__brand {
  display: flex;
  align-items: center;

  gap: 8px;

  min-width: max-content;
}

.site-header__logo {
  display: inline-flex;
  align-items: center;

  color: var(--color-black);

  font-family: var(--font-display);

  font-size: 22px;
  font-weight: 600;

  line-height: 1;

  letter-spacing: 0.2em;

  text-decoration: none;

  white-space: nowrap;
}

/* =========================================================
   ICONES
   ========================================================= */

.header-icon {
  position: relative;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 42px;
  height: 42px;

  padding: 0;

  flex-shrink: 0;

  border: none;

  color: var(--color-black);

  background: transparent;
  cursor: pointer;

  transition:
    background var(--transition-fast),
    color var(--transition-fast);
}

.header-icon:hover {
  background: #f5f4f1;
}

.header-icon svg {
  width: 21px;
  height: 21px;
}

/* Compteur du panier */
.header-icon__badge {
  position: absolute;
  top: 4px;
  right: 3px;

  display: grid;
  place-items: center;

  min-width: 16px;
  height: 16px;

  padding-inline: 4px;

  border-radius: 8px;

  background: var(--color-black);
  color: var(--color-white);

  font-size: 9px;
  font-variant-numeric: tabular-nums;
}

/* =========================================================
   RECHERCHE
   ========================================================= */

.site-header__search {
  position: relative;

  display: flex;
  align-items: center;

  width: 100%;
  min-width: 0;

  padding-inline: clamp(20px, 4vw, 64px);
}

.site-header__search-icon {
  position: absolute;

  left: calc(clamp(20px, 4vw, 64px) + 14px);

  width: 18px;
  height: 18px;

  color: var(--color-text-muted);

  pointer-events: none;
}

.site-header__search input {
  display: block;

  width: 100%;
  height: 42px;

  padding: 0 16px 0 44px;

  border: 1px solid var(--color-border);
  border-radius: 0;

  background: #f7f6f3;

  color: var(--color-text);

  font-family: inherit;
  font-size: 13px;

  transition:
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.site-header__search input::placeholder {
  color: var(--color-text-muted);
}

.site-header__search input:focus {
  border-color: var(--color-border-strong);

  background: #ffffff;
}

/* =========================================================
   ACTIONS DROITE
   ========================================================= */

.site-header__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;

  gap: 2px;

  min-width: max-content;
}

/* =========================================================
   TABLETTE
   ========================================================= */

@media (max-width: 1000px) {
  .site-header__logo {
    font-size: 20px;
  }

  .site-header__search {
    padding-inline: 20px;
  }

  .site-header__search-icon {
    left: 34px;
  }

  .header-icon {
    width: 38px;
    height: 38px;
  }

  .header-icon svg {
    width: 19px;
    height: 19px;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .site-header__inner {
    padding-inline: 8px;
  }

  .site-header__brand {
    gap: 3px;
  }

  .site-header__logo {
    font-size: 16px;
    letter-spacing: 0.14em;
  }

  .header-icon {
    width: 33px;
    height: 33px;
  }

  .header-icon svg {
    width: 18px;
    height: 18px;
  }

  .header-icon__badge {
    top: 1px;
    right: 0;
  }

  .site-header__actions {
    gap: 0;
  }

  .site-header__search {
    min-width: 0;
    padding-inline: 7px;
  }

  .site-header__search-icon {
    left: 19px;
    width: 16px;
    height: 16px;
  }

  .site-header__search input {
    height: 38px;
    padding: 0 7px 0 37px;
    font-size: 11px;
  }

  .site-header__search input::placeholder {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* =========================================================
   TRÈS PETITS ÉCRANS
   ========================================================= */

@media (max-width: 380px) {
  .site-header__inner {
    padding-inline: 5px;
  }

  .site-header__brand {
    gap: 1px;
  }

  .site-header__logo {
    font-size: 14px;
    letter-spacing: 0.1em;
  }

  .header-icon {
    width: 30px;
    height: 30px;
  }

  .header-icon svg {
    width: 17px;
    height: 17px;
  }

  .site-header__search {
    padding-inline: 4px;
  }

  .site-header__search-icon {
    left: 14px;
  }

  .site-header__search input {
    height: 36px;
    padding-left: 31px;
    font-size: 10px;
  }
}
</style>
