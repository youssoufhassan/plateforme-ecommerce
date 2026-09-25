<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from "vue";

import { RouterLink } from "vue-router";

import { useProductStore } from "@/stores/productStore";

/* =========================================================
   PROPS / EVENTS
   ========================================================= */

const props = defineProps<{
  open: boolean;
}>();

const emit = defineEmits<{
  close: [];
}>();

/* =========================================================
   STORE
   ========================================================= */

const productStore = useProductStore();

/* =========================================================
   MENU
   ========================================================= */

type MenuItem = {
  label: string;
  to?: string;
  children?: MenuItem[];
};

const activeMenuLabel = ref<string | null>(null);

/* =========================================================
   MARQUES DU BACKEND
   ========================================================= */

const brands = computed(() => {
  const map = new Map<string, string>();

  for (const product of productStore.products) {
    const brand = product.brand?.trim();

    if (!brand) {
      continue;
    }

    const key = brand.toLowerCase();

    if (!map.has(key)) {
      map.set(key, brand);
    }
  }

  return Array.from(map.values()).slice(0, 30);
});

/* =========================================================
   MENU PRINCIPAL
   ========================================================= */

const menuItems = computed<MenuItem[]>(() => [
  {
    label: "Parfum",

    children: [
      {
        label: "Tous les parfums",
        to: "/produits?category=parfum",
      },
      {
        label: "Homme",
        to: "/produits?category=homme",
      },
      {
        label: "Femme",
        to: "/produits?category=femme",
      },
      {
        label: "Unisexe",
        to: "/produits?category=unisex",
      },
    ],
  },

  {
    label: "Nouveautés",
    to: "/produits?sort=newest",
  },

  {
    label: "Marques",

    children: brands.value.map((brand) => ({
      label: brand,
      to: `/produits?brand=${encodeURIComponent(brand)}`,
    })),
  },

  {
    label: "Bons plans & cadeaux",
    to: "/produits?promotion=true",
  },
]);

/* =========================================================
   MENU ACTIF
   ========================================================= */

const activeMenu = computed<MenuItem | null>(() => {
  if (!activeMenuLabel.value) {
    return null;
  }

  return (
    menuItems.value.find((item) => item.label === activeMenuLabel.value) || null
  );
});

/* =========================================================
   OUVERTURE / FERMETURE
   ========================================================= */

function openSubMenu(label: string) {
  activeMenuLabel.value = label;
}

function backToMainMenu() {
  activeMenuLabel.value = null;
}

function close() {
  activeMenuLabel.value = null;

  emit("close");
}

/* =========================================================
   ESCAPE
   ========================================================= */

function handleEscape(event: KeyboardEvent) {
  if (event.key === "Escape" && props.open) {
    close();
  }
}

/* =========================================================
   BODY SCROLL
   ========================================================= */

watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen) {
      if (productStore.products.length === 0) {
        productStore.fetchProducts();
      }

      await nextTick();

      document.addEventListener("keydown", handleEscape);

      document.body.classList.add("no-scroll");
    } else {
      document.removeEventListener("keydown", handleEscape);

      document.body.classList.remove("no-scroll");

      activeMenuLabel.value = null;
    }
  },
);

onBeforeUnmount(() => {
  document.removeEventListener("keydown", handleEscape);

  document.body.classList.remove("no-scroll");
});
</script>

<template>
  <!-- =====================================================
       MENU GLOBAL
       TELEPORT = SORT DU HEADER ET DE LA HOME
       ===================================================== -->

  <Teleport to="body">
    <Transition name="menu">
      <div v-if="open" class="site-menu">
        <!-- =================================================
             OVERLAY
             ================================================= -->

        <div class="site-menu__backdrop" aria-hidden="true" @click="close" />

        <!-- =================================================
             PANNEAU
             ================================================= -->

        <aside class="site-menu__panel" aria-label="Menu principal">
          <!-- =================================================
               HEADER DU MENU
               ================================================= -->

          <header class="site-menu__header">
            <div class="site-menu__brand">SIDRA</div>

            <button
              type="button"
              class="site-menu__close"
              aria-label="Fermer le menu"
              @click="close"
            >
              <span></span>
              <span></span>
            </button>
          </header>

          <!-- =================================================
               SOUS-MENU
               ================================================= -->

          <Transition name="submenu">
            <div v-if="activeMenu" class="site-menu__submenu">
              <button
                type="button"
                class="site-menu__back"
                @click="backToMainMenu"
              >
                <span>←</span>

                <span> Retour </span>
              </button>

              <div class="site-menu__submenu-heading">
                <span class="site-menu__eyebrow"> SIDRA </span>

                <h2>
                  {{ activeMenu.label }}
                </h2>
              </div>

              <nav class="site-menu__links">
                <RouterLink
                  v-for="child in activeMenu.children"
                  :key="child.label"
                  :to="child.to || '#'"
                  class="site-menu__link"
                  @click="close"
                >
                  <span>
                    {{ child.label }}
                  </span>

                  <span class="site-menu__arrow"> → </span>
                </RouterLink>
              </nav>
            </div>
          </Transition>

          <!-- =================================================
               MENU PRINCIPAL
               ================================================= -->

          <div v-if="!activeMenu" class="site-menu__content">
            <!-- COMPTE -->

            <section class="site-menu__section">
              <span class="site-menu__eyebrow"> COMPTE </span>

              <nav class="site-menu__links">
                <RouterLink to="/login" class="site-menu__link" @click="close">
                  <span> Se connecter </span>

                  <span class="site-menu__arrow"> → </span>
                </RouterLink>

                <RouterLink
                  to="/account/favorites"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Mes favoris </span>

                  <span class="site-menu__arrow"> → </span>
                </RouterLink>

                <RouterLink
                  to="/account/orders"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Suivre ma commande </span>

                  <span class="site-menu__arrow"> → </span>
                </RouterLink>
              </nav>
            </section>

            <!-- CATÉGORIES -->

            <section class="site-menu__section">
              <span class="site-menu__eyebrow"> CATÉGORIES </span>

              <nav class="site-menu__links">
                <template v-for="item in menuItems" :key="item.label">
                  <!-- AVEC SOUS-MENU -->

                  <button
                    v-if="item.children"
                    type="button"
                    class="site-menu__link site-menu__link--button"
                    @click="openSubMenu(item.label)"
                  >
                    <span>
                      {{ item.label }}
                    </span>

                    <span class="site-menu__arrow"> → </span>
                  </button>

                  <!-- LIEN SIMPLE -->

                  <RouterLink
                    v-else
                    :to="item.to || '#'"
                    class="site-menu__link"
                    @click="close"
                  >
                    <span>
                      {{ item.label }}
                    </span>

                    <span class="site-menu__arrow"> → </span>
                  </RouterLink>
                </template>
              </nav>
            </section>

            <!-- AUTRES -->

            <section class="site-menu__section">
              <span class="site-menu__eyebrow"> AUTRES </span>

              <nav class="site-menu__links">
                <RouterLink to="/help" class="site-menu__link" @click="close">
                  <span> Aide & contact </span>

                  <span class="site-menu__arrow"> → </span>
                </RouterLink>

                <RouterLink to="/stores" class="site-menu__link" @click="close">
                  <span> Livraison & retours </span>

                  <span class="site-menu__arrow"> → </span>
                </RouterLink>
              </nav>
            </section>
          </div>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* =========================================================
   CONTENEUR GLOBAL
   ========================================================= */

.site-menu {
  position: fixed;

  inset: 0;

  z-index: 9999;

  width: 100vw;
  height: 100vh;

  pointer-events: none;
}

/* =========================================================
   OVERLAY
   ========================================================= */

.site-menu__backdrop {
  position: fixed;

  inset: 0;

  width: 100vw;
  height: 100vh;

  background: rgba(0, 0, 0, 0.42);

  pointer-events: auto;
}

/* =========================================================
   PANNEAU
   ========================================================= */

.site-menu__panel {
  position: fixed;

  top: 0;
  bottom: 0;
  left: 0;

  z-index: 10000;

  width: min(430px, 88vw);
  height: 100vh;

  display: flex;
  flex-direction: column;

  background: #ffffff;

  box-shadow: 12px 0 40px rgba(0, 0, 0, 0.12);

  pointer-events: auto;

  overflow: hidden;
}

/* =========================================================
   HEADER DU MENU
   ========================================================= */

.site-menu__header {
  display: flex;

  align-items: center;
  justify-content: space-between;

  flex-shrink: 0;

  height: 82px;

  padding: 0 32px;

  border-bottom: 1px solid var(--color-border);

  background: #ffffff;
}

.site-menu__brand {
  font-family: var(--font-display);

  font-size: 21px;
  font-weight: 600;

  line-height: 1;

  letter-spacing: 0.2em;
}

/* =========================================================
   BOUTON FERMER
   ========================================================= */

.site-menu__close {
  position: relative;

  width: 42px;
  height: 42px;

  padding: 0;
}

.site-menu__close span {
  position: absolute;

  top: 50%;
  left: 50%;

  width: 21px;
  height: 1px;

  background: var(--color-black);
}

.site-menu__close span:first-child {
  transform: translate(-50%, -50%) rotate(45deg);
}

.site-menu__close span:last-child {
  transform: translate(-50%, -50%) rotate(-45deg);
}

/* =========================================================
   CONTENU
   ========================================================= */

.site-menu__content,
.site-menu__submenu {
  flex: 1;

  overflow-y: auto;

  padding: 34px 32px 50px;
}

/* =========================================================
   SECTIONS
   ========================================================= */

.site-menu__section {
  margin-bottom: 38px;
}

.site-menu__section:last-child {
  margin-bottom: 0;
}

/* =========================================================
   PETIT TITRE
   ========================================================= */

.site-menu__eyebrow {
  display: block;

  margin-bottom: 13px;

  color: var(--color-text-muted);

  font-size: 9px;
  font-weight: 600;

  line-height: 1;

  letter-spacing: 0.18em;
}

/* =========================================================
   LIENS
   ========================================================= */

.site-menu__links {
  display: flex;
  flex-direction: column;
}

.site-menu__link {
  display: flex;

  align-items: center;
  justify-content: space-between;

  width: 100%;
  min-height: 48px;

  border-bottom: 1px solid #efeeeb;

  color: var(--color-text);

  font-size: 14px;

  text-align: left;

  transition:
    padding-left var(--transition-fast),
    background var(--transition-fast);
}

.site-menu__link:hover {
  padding-left: 7px;

  background: #faf9f7;
}

.site-menu__link--button {
  padding-top: 0;
  padding-bottom: 0;
}

.site-menu__arrow {
  font-size: 16px;

  transition: transform var(--transition-base);
}

.site-menu__link:hover .site-menu__arrow {
  transform: translateX(4px);
}

/* =========================================================
   SOUS-MENU
   ========================================================= */

.site-menu__back {
  display: inline-flex;

  align-items: center;

  gap: 10px;

  margin-bottom: 34px;

  padding: 0;

  color: var(--color-text-secondary);

  font-size: 11px;
  font-weight: 600;

  letter-spacing: 0.1em;

  text-transform: uppercase;
}

.site-menu__back span:first-child {
  font-size: 17px;
}

.site-menu__submenu-heading {
  margin-bottom: 28px;
}

.site-menu__submenu-heading h2 {
  font-family: var(--font-display);

  font-size: 38px;
  font-weight: 400;

  line-height: 1;

  letter-spacing: -0.035em;
}

/* =========================================================
   ANIMATION MENU
   ========================================================= */

.menu-enter-active,
.menu-leave-active {
  transition: opacity 260ms ease;
}

.menu-enter-from,
.menu-leave-to {
  opacity: 0;
}

/* =========================================================
   ANIMATION SOUS-MENU
   ========================================================= */

.submenu-enter-active,
.submenu-leave-active {
  transition:
    opacity 220ms ease,
    transform 220ms ease;
}

.submenu-enter-from {
  opacity: 0;

  transform: translateX(25px);
}

.submenu-leave-to {
  opacity: 0;

  transform: translateX(-25px);
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .site-menu__panel {
    width: min(92vw, 430px);
  }

  .site-menu__header {
    height: 64px;

    padding-inline: 20px;
  }

  .site-menu__content,
  .site-menu__submenu {
    padding: 28px 20px 40px;
  }

  .site-menu__section {
    margin-bottom: 32px;
  }

  .site-menu__submenu-heading h2 {
    font-size: 34px;
  }
}
</style>
