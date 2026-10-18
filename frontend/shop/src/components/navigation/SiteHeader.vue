<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";

import SearchIcon from "@/components/icons/SearchIcon.vue";
import HeartIcon from "@/components/icons/HeartIcon.vue";
import BagIcon from "@/components/icons/BagIcon.vue";
import MenuIcon from "@/components/icons/MenuIcon.vue";
import UserIcon from "@/components/icons/UserIcon.vue";

import { useAuthStore } from "@/stores/authStore";
import { useCartStore } from "@/stores/cartStore";
import { useFavorites } from "@/composables/useFavorites";

import { fetchCategoryTree } from "@/services/productService";
import type { CategoryNode } from "@/types/catalog";

import MobileMenu from "./MobileMenu.vue";
import SearchOverlay from "./SearchOverlay.vue";

const authStore = useAuthStore();
const cartStore = useCartStore();
const { count: favoritesCount } = useFavorites();

const menuOpen = ref(false);
const searchOpen = ref(false);

const categoryTree = ref<CategoryNode[]>([]);
const hoveredUniverse = ref("");
const hoveredCategory = ref<string | null>(null);

// Univers disponibles dans la navigation desktop.
const universes = [
  { label: "Mode Homme", slug: "homme" },
  { label: "Mode Femme", slug: "femme" },
  { label: "Beauté", slug: "beaute" },
  { label: "Enfant", slug: "enfant" },
  { label: "Accessoires", slug: "accessoires" },
  { label: "Maison", slug: "maison" },
  { label: "Électronique", slug: "electronique" },
];

function normalize(value: string): string {
  return value
    .toLocaleLowerCase("fr-FR")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "");
}

// Charge l'arborescence via le service existant.
onMounted(async () => {
  try {
    categoryTree.value = await fetchCategoryTree();
  } catch (error) {
    console.error("Impossible de charger les catégories SIDRA :", error);
    categoryTree.value = [];
  }
});

// Cherche uniquement parmi les catégories racines.
// Un chemin comme "homme/montres/automatiques" commence par "homme".
function findUniverse(slug: string): CategoryNode | null {
  const target = normalize(slug);

  return (
    categoryTree.value.find((node) => {
      const rootPath = normalize(node.path.split("/")[0] ?? "");
      return (
        normalize(node.slug) === target ||
        normalize(node.name) === target ||
        rootPath === target
      );
    }) ?? null
  );
}

const activeUniverse = computed(() =>
  hoveredUniverse.value ? findUniverse(hoveredUniverse.value) : null,
);

const activeChildren = computed(() => activeUniverse.value?.children ?? []);

// Sous-catégorie actuellement survolée dans la première colonne.
const activeSubcategory = computed(
  () =>
    activeChildren.value.find(
      (category) => category.id === hoveredCategory.value,
    ) ?? null,
);

// Affiche uniquement les enfants de la sous-catégorie active.
const activeRays = computed(() => activeSubcategory.value?.children ?? []);

function openUniverse(slug: string) {
  if (hoveredUniverse.value !== slug) {
    hoveredUniverse.value = slug;
    hoveredCategory.value = null;
  }
}

function closeMenu() {
  hoveredUniverse.value = "";
  hoveredCategory.value = null;
}

function categoryLink(category: CategoryNode) {
  return {
    path: "/produits",
    query: {
      category: category.path || category.slug || normalize(category.name),
    },
  };
}
</script>

<template>
  <header class="header" @keydown.esc="closeMenu">
    <div class="header__top">
      <div class="container header__top-inner">
        <div class="header__left">
          <button
            type="button"
            class="icon-button header__mobile-toggle"
            aria-label="Ouvrir le menu"
            :aria-expanded="menuOpen"
            @click="menuOpen = true"
          >
            <MenuIcon />
          </button>

          <RouterLink to="/" class="header__logo" aria-label="SIDRA — Accueil">
            SIDRA
          </RouterLink>
        </div>

        <button
          type="button"
          class="header__search"
          aria-label="Ouvrir la recherche"
          @click="searchOpen = true"
        >
          <SearchIcon />
          <span>Rechercher un produit, une marque, une catégorie…</span>
        </button>

        <div class="header__actions">
          <button
            type="button"
            class="icon-button header__search-mobile"
            aria-label="Rechercher"
            @click="searchOpen = true"
          >
            <SearchIcon />
          </button>

          <RouterLink
            to="/favoris"
            class="icon-button header__action"
            aria-label="Mes favoris"
          >
            <HeartIcon />
            <span v-if="favoritesCount" class="header__badge">
              {{ favoritesCount }}
            </span>
          </RouterLink>

          <RouterLink
            :to="authStore.isAuthenticated ? '/compte' : '/connexion'"
            class="icon-button header__action"
            aria-label="Mon compte"
          >
            <UserIcon />
          </RouterLink>

          <RouterLink
            to="/panier"
            class="icon-button header__action"
            aria-label="Mon panier"
          >
            <BagIcon />
            <span v-if="cartStore.count" class="header__badge">
              {{ cartStore.count }}
            </span>
          </RouterLink>
        </div>
      </div>
    </div>

    <!-- Navigation desktop -->
    <nav
      class="header__nav"
      aria-label="Navigation principale"
      @mouseleave="closeMenu"
    >
      <div class="header__nav-inner">
        <div
          v-for="universe in universes"
          :key="universe.slug"
          class="nav-item"
          @mouseenter="openUniverse(universe.slug)"
        >
          <RouterLink
            :to="{
              path: '/produits',
              query: {
                category:
                  activeUniverse?.path && hoveredUniverse === universe.slug
                    ? activeUniverse.path
                    : universe.slug,
              },
            }"
            class="nav-link"
            :class="{ 'nav-link--active': hoveredUniverse === universe.slug }"
            @focus="openUniverse(universe.slug)"
          >
            {{ universe.label }}
          </RouterLink>
        </div>

        <RouterLink
          to="/produits?sort=newest"
          class="nav-link nav-link--new"
          @mouseenter="closeMenu"
        >
          Nouveautés
        </RouterLink>
      </div>

      <!-- Méga-menu : il ne s'affiche que pour l'univers survolé -->
      <div
        v-if="activeUniverse && activeChildren.length"
        class="mega-menu"
        @mouseenter="hoveredUniverse = hoveredUniverse"
      >
        <div class="mega-menu__inner">
          <!-- Colonne des sous-catégories -->
          <div class="mega-menu__categories">
            <p class="mega-menu__label">DÉCOUVRIR</p>

            <RouterLink
              :to="categoryLink(activeUniverse)"
              class="mega-menu__all"
              @click="closeMenu"
            >
              Tout l'univers {{ activeUniverse.name }}
            </RouterLink>

            <button
              v-for="category in activeChildren"
              :key="category.id"
              type="button"
              class="mega-menu__category"
              :class="{
                'mega-menu__category--active': hoveredCategory === category.id,
              }"
              @mouseenter="hoveredCategory = category.id"
              @focus="hoveredCategory = category.id"
              @click="hoveredCategory = category.id"
            >
              {{ category.name }}
            </button>
          </div>

          <!-- Colonne des rayons : visible seulement si une sous-catégorie est sélectionnée -->
          <div v-if="activeSubcategory" class="mega-menu__rays">
            <p class="mega-menu__label">{{ activeSubcategory.name }}</p>

            <RouterLink
              :to="categoryLink(activeSubcategory)"
              class="mega-menu__all"
              @click="closeMenu"
            >
              Tout voir
            </RouterLink>

            <RouterLink
              v-for="ray in activeRays"
              :key="ray.id"
              :to="categoryLink(ray)"
              class="mega-menu__ray"
              @click="closeMenu"
            >
              {{ ray.name }}
            </RouterLink>

            <p v-if="!activeRays.length" class="mega-menu__empty">
              Découvrez tous les produits de cette catégorie.
            </p>
          </div>

          <!-- Petit panneau éditorial -->
          <aside class="mega-menu__editorial">
            <span class="mega-menu__editorial-label">LA SÉLECTION SIDRA</span>
            <h3>Le sens du détail.</h3>
            <p>
              Des collections choisies pour leur style, leur qualité et leur
              singularité.
            </p>
            <RouterLink
              :to="categoryLink(activeUniverse)"
              class="mega-menu__editorial-link"
              @click="closeMenu"
            >
              Explorer la collection
            </RouterLink>
          </aside>
        </div>
      </div>
    </nav>

    <MobileMenu :open="menuOpen" @close="menuOpen = false" />
    <SearchOverlay :open="searchOpen" @close="searchOpen = false" />
  </header>
</template>

<style scoped>
.header {
  position: fixed;
  inset: 0 0 auto;
  z-index: 1000;
  isolation: isolate;
  background: #fff;
  border-bottom: 1px solid #e7e5e1;
}

.header__top {
  border-bottom: 1px solid #e7e5e1;
}

.header__top-inner {
  display: grid;
  grid-template-columns: auto minmax(220px, 1fr) auto;
  align-items: center;
  gap: 28px;
  min-height: 70px;
}

.header__left,
.header__actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header__logo {
  color: #111;
  font-family: "Bodoni Moda", Georgia, serif;
  font-size: 27px;
  font-weight: 500;
  letter-spacing: 0.16em;
  text-decoration: none;
}

.header__search {
  display: flex;
  align-items: center;
  gap: 12px;
  width: min(100%, 480px);
  height: 42px;
  padding: 0 14px;
  margin: auto;
  border: 1px solid #e3e1dd;
  background: #f8f7f5;
  color: #777;
  text-align: left;
  cursor: pointer;
}

.header__search span {
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.icon-button {
  position: relative;
  display: inline-grid;
  place-items: center;
  width: 38px;
  height: 38px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #171717;
  cursor: pointer;
  text-decoration: none;
}

.icon-button :deep(svg) {
  width: 20px;
  height: 20px;
}

.header__badge {
  position: absolute;
  top: 0;
  right: 0;
  display: grid;
  place-items: center;
  min-width: 17px;
  height: 17px;
  padding: 0 4px;
  border-radius: 50%;
  background: #111;
  color: white;
  font-size: 10px;
}

.header__mobile-toggle,
.header__search-mobile {
  display: none;
}

.header__nav {
  position: relative;
  z-index: 1;
  background: #fff;
}
.header__nav-inner {
  display: flex;
  align-items: stretch;
  justify-content: center;
  gap: clamp(18px, 2.5vw, 38px);
  min-height: 46px;
}

.nav-item {
  display: flex;
  align-items: stretch;
}

.nav-link {
  display: inline-flex;
  align-items: center;
  border-bottom: 2px solid transparent;
  color: #383735;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition:
    color 0.15s ease,
    border-color 0.15s ease;
}

.nav-link:hover,
.nav-link--active {
  color: #111;
  border-bottom-color: #111;
}

.nav-link--new {
  color: #a36b1a;
}

/* Méga-menu sans flèches ni lignes décoratives superflues */
.mega-menu {
  position: absolute;
  top: 100%;
  right: 0;
  left: 0;
  z-index: 2;
  background: #fff;
  border-top: 1px solid #eeeae5;
  border-bottom: 1px solid #dedbd5;
  box-shadow: 0 18px 35px rgb(0 0 0 / 8%);
}

.mega-menu__inner {
  display: grid;
  grid-template-columns: minmax(210px, 260px) minmax(200px, 1fr) minmax(
      220px,
      300px
    );
  gap: 38px;
  width: min(1240px, calc(100% - 48px));
  min-height: 260px;
  padding: 32px 0 36px;
  margin: 0 auto;
}

.mega-menu__categories,
.mega-menu__rays {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 2px;
}

.mega-menu__label,
.mega-menu__editorial-label {
  margin: 0 0 12px;
  color: #8b8983;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.mega-menu__all {
  display: block;
  margin-bottom: 10px;
  color: #111;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
}

.mega-menu__category {
  width: 100%;
  padding: 10px 0;
  border: 0;
  background: transparent;
  color: #55534f;
  font: inherit;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition: color 0.15s ease;
}

.mega-menu__category:hover,
.mega-menu__category--active {
  color: #a36b1a;
}

.mega-menu__ray {
  padding: 10px 0;
  color: #55534f;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.15s ease;
}

.mega-menu__ray:hover {
  color: #a36b1a;
}

.mega-menu__empty {
  color: #777;
  font-size: 13px;
  line-height: 1.6;
}

.mega-menu__editorial {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 26px;
  background: #f7f5f1;
}

.mega-menu__editorial h3 {
  margin: 0 0 10px;
  font-family: "Bodoni Moda", Georgia, serif;
  font-size: 27px;
  font-weight: 400;
}

.mega-menu__editorial p {
  margin: 0 0 20px;
  color: #6e6b65;
  font-size: 13px;
  line-height: 1.7;
}

.mega-menu__editorial-link {
  color: #111;
  font-size: 12px;
  font-weight: 600;
  text-decoration: underline;
  text-underline-offset: 4px;
}

@media (max-width: 1000px) {
  .header__nav {
    display: none;
  }

  .header__top-inner {
    grid-template-columns: auto 1fr auto;
    gap: 10px;
    min-height: 66px;
  }

  .header__mobile-toggle,
  .header__search-mobile {
    display: inline-grid;
  }

  .header__search {
    display: none;
  }

  .header__left {
    gap: 6px;
  }

  .header__logo {
    font-size: 22px;
  }

  .header__actions {
    gap: 0;
  }

  .header__action {
    width: 34px;
  }
}

@media (max-width: 480px) {
  .header__top-inner {
    min-height: 60px;
  }

  .header__logo {
    font-size: 19px;
    letter-spacing: 0.1em;
  }

  .header__actions {
    gap: 0;
  }
}
</style>
