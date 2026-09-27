<script setup lang="ts">
import { ref } from "vue";
import { RouterLink } from "vue-router";

import SearchIcon from "@/components/icons/SearchIcon.vue";
import HeartIcon from "@/components/icons/HeartIcon.vue";
import BagIcon from "@/components/icons/BagIcon.vue";
import MenuIcon from "@/components/icons/MenuIcon.vue";
import UserIcon from "@/components/icons/UserIcon.vue";
import { useAuthStore } from "@/stores/authStore";
import { useCartStore } from "@/stores/cartStore";

import CategoryNav from "./CategoryNav.vue";
import MobileMenu from "./MobileMenu.vue";
import SearchOverlay from "./SearchOverlay.vue";

const authStore = useAuthStore();
const cartStore = useCartStore();

const menuOpen = ref(false);
const searchOpen = ref(false);
</script>

<template>
  <header class="header">
    <div class="container header__inner">
      <div class="header__left">
        <button
          type="button"
          class="btn-icon header__menu"
          aria-label="Ouvrir le menu"
          :aria-expanded="menuOpen"
          @click="menuOpen = true"
        >
          <MenuIcon />
        </button>

        <RouterLink to="/" class="header__logo" aria-label="SIDRA, accueil">
          SIDRA
        </RouterLink>
      </div>

      <!--
        La recherche est un déclencheur, pas un champ : l'ouverture
        donne accès aux suggestions et à l'historique.
      -->
      <button type="button" class="header__search" @click="searchOpen = true">
        <SearchIcon />
        <span>Rechercher un parfum, une maison</span>
      </button>

      <div class="header__actions">
        <button
          type="button"
          class="btn-icon header__search-icon"
          aria-label="Rechercher"
          @click="searchOpen = true"
        >
          <SearchIcon />
        </button>

        <RouterLink to="/favoris" class="btn-icon" aria-label="Mes favoris">
          <HeartIcon />
        </RouterLink>

        <RouterLink
          :to="authStore.isAuthenticated ? '/compte' : '/connexion'"
          class="btn-icon"
          :aria-label="
            authStore.isAuthenticated ? 'Mon compte' : 'Se connecter'
          "
        >
          <UserIcon />
        </RouterLink>

        <RouterLink
          to="/panier"
          class="btn-icon header__cart"
          :aria-label="
            cartStore.count
              ? `Mon panier, ${cartStore.count} article${cartStore.count > 1 ? 's' : ''}`
              : 'Mon panier'
          "
        >
          <BagIcon />

          <span v-if="cartStore.count" class="header__badge numeric">
            {{ cartStore.count }}
          </span>
        </RouterLink>
      </div>
    </div>

    <CategoryNav />
  </header>

  <MobileMenu :open="menuOpen" @close="menuOpen = false" />
  <SearchOverlay :open="searchOpen" @close="searchOpen = false" />
</template>

<style scoped>
.header {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: var(--z-header);

  background: var(--color-paper);
  border-bottom: 1px solid var(--color-line);
}

.header__inner {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--space-6);
  height: var(--header-height);
}

/* =========================================================
   MARQUE
   ========================================================= */

.header__left {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: max-content;
}

.header__logo {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: var(--weight-medium);
  line-height: 1;
  letter-spacing: 0.14em;
  color: var(--color-ink);
  white-space: nowrap;
}

/* =========================================================
   RECHERCHE
   ========================================================= */

.header__search {
  display: flex;
  align-items: center;
  gap: var(--space-3);

  width: 100%;
  max-width: 460px;
  height: 42px;
  padding-inline: var(--space-4);

  margin-inline: auto;

  border: 1px solid var(--color-line);
  background: var(--color-page);
  cursor: text;

  color: var(--color-text-muted);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  text-align: left;

  transition:
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.header__search:hover {
  border-color: var(--color-text-muted);
  background: var(--color-paper);
}

.header__search svg {
  flex-shrink: 0;
  width: 17px;
  height: 17px;
}

.header__search span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* En mobile, la recherche devient une icône */
.header__search-icon {
  display: none;
}

/* =========================================================
   ACTIONS
   ========================================================= */

.header__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-1);
  min-width: max-content;
}

.header__cart {
  position: relative;
}

.header__badge {
  position: absolute;
  top: 3px;
  right: 2px;

  display: grid;
  place-items: center;

  min-width: 17px;
  height: 17px;
  padding-inline: 4px;

  border-radius: var(--radius-pill);
  background: var(--color-ink);
  color: var(--color-text-inverse);

  font-size: 10px;
  line-height: 1;
}

/* =========================================================
   TABLETTE ET MOBILE
   ========================================================= */

@media (max-width: 1000px) {
  .header__inner {
    grid-template-columns: auto 1fr auto;
    gap: var(--space-3);
  }

  /* Le champ de recherche cède la place à une icône */
  .header__search {
    display: none;
  }

  .header__search-icon {
    display: grid;
  }

  .header__logo {
    font-size: 22px;
  }
}

@media (max-width: 767px) {
  .header__logo {
    font-size: 19px;
    letter-spacing: 0.1em;
  }

  .header__actions {
    gap: 0;
  }
}

@media (max-width: 380px) {
  .header__logo {
    font-size: 17px;
  }
}
</style>
