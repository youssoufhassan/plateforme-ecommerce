<script setup lang="ts">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from "vue";

import { RouterLink } from "vue-router";

import api from "@/services/http/api";
import type { CategoryNode } from "@/types/catalog";

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
   ARBORESCENCE DU BACKEND
   ========================================================= */

const tree = ref<CategoryNode[]>([]);
const brands = ref<string[]>([]);
const loading = ref(true);

/* =========================================================
   NAVIGATION

   Le chemin parcouru : vide à la racine, un nœud par niveau
   descendu. Permet une profondeur illimitée.
   ========================================================= */

const trail = ref<CategoryNode[]>([]);
const brandsOpen = ref(false);

const current = computed(() => trail.value[trail.value.length - 1] ?? null);

const children = computed(() =>
  current.value ? current.value.children : tree.value,
);

const inSubMenu = computed(() => trail.value.length > 0 || brandsOpen.value);

/* =========================================================
   OUVERTURE / FERMETURE
   ========================================================= */

function enter(node: CategoryNode) {
  trail.value = [...trail.value, node];
}

function openBrands() {
  brandsOpen.value = true;
}

function back() {
  if (brandsOpen.value) {
    brandsOpen.value = false;
    return;
  }
  trail.value = trail.value.slice(0, -1);
}

function close() {
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
   CHARGEMENT
   ========================================================= */

onMounted(async () => {
  try {
    const [treeResponse, filtersResponse] = await Promise.all([
      api.get<CategoryNode[]>("/categories/tree"),
      api.get<{ brands: string[] }>("/products/filters"),
    ]);

    tree.value = treeResponse.data;
    brands.value = filtersResponse.data.brands ?? [];
  } catch {
    // Sans arbre, seules les sections fixes restent disponibles
  } finally {
    loading.value = false;
  }
});

/* =========================================================
   BODY SCROLL
   ========================================================= */

watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen) {
      await nextTick();

      document.addEventListener("keydown", handleEscape);

      document.body.classList.add("no-scroll");
    } else {
      document.removeEventListener("keydown", handleEscape);

      document.body.classList.remove("no-scroll");

      // Le menu rouvre toujours à la racine
      trail.value = [];
      brandsOpen.value = false;
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
            <div v-if="inSubMenu" class="site-menu__submenu">
              <button type="button" class="site-menu__back" @click="back">
                <span>‹</span>

                <span> Retour </span>
              </button>

              <!-- MARQUES -->

              <template v-if="brandsOpen">
                <div class="site-menu__submenu-heading">
                  <span class="site-menu__eyebrow"> SIDRA </span>

                  <h2>Marques</h2>
                </div>

                <nav class="site-menu__links">
                  <RouterLink
                    v-for="brand in brands"
                    :key="brand"
                    :to="{ path: '/produits', query: { brand } }"
                    class="site-menu__link"
                    @click="close"
                  >
                    <span>{{ brand }}</span>

                    <span class="site-menu__chevron"> › </span>
                  </RouterLink>
                </nav>
              </template>

              <!-- CATÉGORIE -->

              <template v-else-if="current">
                <div class="site-menu__submenu-heading">
                  <span class="site-menu__eyebrow"> SIDRA </span>

                  <h2>
                    {{ current.name }}
                  </h2>
                </div>

                <nav class="site-menu__links">
                  <!-- Accès direct à toute la branche -->
                  <RouterLink
                    :to="{
                      path: '/produits',
                      query: { category: current.path },
                    }"
                    class="site-menu__link site-menu__link--all"
                    @click="close"
                  >
                    <span> Tout voir </span>

                    <span class="site-menu__count">
                      {{ current.productCount }}
                    </span>
                  </RouterLink>

                  <template v-for="node in children" :key="node.id">
                    <!-- Un nœud avec enfants ouvre le niveau suivant -->
                    <button
                      v-if="node.children.length"
                      type="button"
                      class="site-menu__link site-menu__link--button"
                      @click="enter(node)"
                    >
                      <span>
                        {{ node.name }}
                      </span>

                      <span class="site-menu__chevron"> › </span>
                    </button>

                    <!-- Une feuille mène au catalogue -->
                    <RouterLink
                      v-else
                      :to="{
                        path: '/produits',
                        query: { category: node.path },
                      }"
                      class="site-menu__link"
                      @click="close"
                    >
                      <span>
                        {{ node.name }}
                      </span>

                      <span class="site-menu__count">
                        {{ node.productCount }}
                      </span>
                    </RouterLink>
                  </template>
                </nav>
              </template>
            </div>
          </Transition>

          <!-- =================================================
               MENU PRINCIPAL
               ================================================= -->

          <div v-if="!inSubMenu" class="site-menu__content">
            <!-- COMPTE -->

            <section class="site-menu__section">
              <span class="site-menu__eyebrow"> COMPTE </span>

              <nav class="site-menu__links">
                <RouterLink
                  to="/connexion"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Se connecter </span>

                  <span class="site-menu__chevron"> › </span>
                </RouterLink>

                <RouterLink
                  to="/favoris"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Mes favoris </span>

                  <span class="site-menu__chevron"> › </span>
                </RouterLink>

                <RouterLink to="/compte" class="site-menu__link" @click="close">
                  <span> Suivre ma commande </span>

                  <span class="site-menu__chevron"> › </span>
                </RouterLink>
              </nav>
            </section>

            <!-- CATÉGORIES -->

            <section class="site-menu__section">
              <span class="site-menu__eyebrow"> CATÉGORIES </span>

              <p v-if="loading" class="site-menu__state">Chargement</p>

              <nav v-else class="site-menu__links">
                <!-- Les racines de l'arborescence -->
                <template v-for="node in tree" :key="node.id">
                  <button
                    v-if="node.children.length"
                    type="button"
                    class="site-menu__link site-menu__link--button"
                    @click="enter(node)"
                  >
                    <span>
                      {{ node.name }}
                    </span>

                    <span class="site-menu__chevron"> › </span>
                  </button>

                  <RouterLink
                    v-else
                    :to="{ path: '/produits', query: { category: node.path } }"
                    class="site-menu__link"
                    @click="close"
                  >
                    <span>
                      {{ node.name }}
                    </span>

                    <span class="site-menu__count">
                      {{ node.productCount }}
                    </span>
                  </RouterLink>
                </template>

                <RouterLink
                  :to="{ path: '/produits', query: { sort: 'newest' } }"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Nouveautés </span>

                  <span class="site-menu__chevron"> › </span>
                </RouterLink>

                <button
                  v-if="brands.length"
                  type="button"
                  class="site-menu__link site-menu__link--button"
                  @click="openBrands"
                >
                  <span> Marques </span>

                  <span class="site-menu__chevron"> › </span>
                </button>

                <RouterLink
                  to="/produits"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Tout le catalogue </span>

                  <span class="site-menu__chevron"> › </span>
                </RouterLink>
              </nav>
            </section>

            <!-- AUTRES -->

            <section class="site-menu__section">
              <span class="site-menu__eyebrow"> AUTRES </span>

              <nav class="site-menu__links">
                <RouterLink
                  to="/legal/retours"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Livraison &amp; retours </span>

                  <span class="site-menu__chevron"> › </span>
                </RouterLink>

                <RouterLink
                  to="/legal/cgv"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Conditions de vente </span>

                  <span class="site-menu__chevron"> › </span>
                </RouterLink>

                <RouterLink
                  to="/legal/mentions-legales"
                  class="site-menu__link"
                  @click="close"
                >
                  <span> Mentions légales </span>

                  <span class="site-menu__chevron"> › </span>
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

  z-index: var(--z-menu);

  width: 100vw;
  height: 100vh;
  /* dvh suit la hauteur réellement visible sur mobile,
     barres du navigateur comprises */
  height: 100dvh;

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
  height: 100dvh;

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

  /* Suffit à passer au-dessus du voile, dans la même couche */
  z-index: 1;

  width: min(430px, 88vw);
  height: 100vh;
  height: 100dvh;

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

  /* Même hauteur que l'en-tête du site, quelle que soit la taille d'écran */
  height: var(--header-height);

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

  background: transparent;
  border: none;
  cursor: pointer;
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
  /* Le défilement du menu ne se propage pas à la page */
  overscroll-behavior: contain;

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

.site-menu__state {
  color: var(--color-text-muted);
  font-size: 13px;
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

  border: none;
  border-bottom: 1px solid #efeeeb;

  background: transparent;
  cursor: pointer;

  color: var(--color-text);

  font-size: 14px;
  font-family: inherit;

  text-align: left;
  text-decoration: none;

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

/* Accès à la branche entière, mis en avant */
.site-menu__link--all {
  font-weight: 500;
  text-decoration: underline;
  text-underline-offset: 4px;
}

/* =========================================================
   CHEVRON ET COMPTEUR
   ========================================================= */

.site-menu__chevron {
  flex-shrink: 0;

  color: var(--color-text-muted);

  font-size: 19px;
  line-height: 1;

  transition:
    transform var(--transition-base),
    color var(--transition-base);
}

.site-menu__link:hover .site-menu__chevron {
  transform: translateX(4px);
  color: var(--color-text);
}

/* Le nombre de produits évite d'ouvrir une rubrique vide */
.site-menu__count {
  flex-shrink: 0;

  color: var(--color-text-muted);

  font-size: 11px;
  font-variant-numeric: tabular-nums;
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

  background: transparent;
  border: none;
  cursor: pointer;

  color: var(--color-text-secondary);

  font-size: 11px;
  font-weight: 600;
  font-family: inherit;

  letter-spacing: 0.1em;

  text-transform: uppercase;
}

.site-menu__back span:first-child {
  font-size: 19px;
  line-height: 1;
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

/* Respecte le réglage système de réduction des animations */
@media (prefers-reduced-motion: reduce) {
  .menu-enter-active,
  .menu-leave-active,
  .submenu-enter-active,
  .submenu-leave-active {
    transition-duration: 1ms;
  }

  .submenu-enter-from,
  .submenu-leave-to {
    transform: none;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .site-menu__panel {
    width: min(92vw, 430px);
  }

  .site-menu__header {
    /* La hauteur vient de --header-height, seul le retrait change */
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
