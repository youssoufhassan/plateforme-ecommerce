<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";

import api from "@/services/http/api";

interface Category {
  id: string;
  name: string;
}

const categories = ref<Category[]>([]);

/**
 * Ordre d'affichage souhaité. Les catégories absentes de cette liste
 * sont ajoutées à la suite, par ordre alphabétique.
 */
const PREFERRED_ORDER = [
  "Parfums",
  "Homme",
  "Femme",
  "Unisexe",
  "Beauté",
  "Huiles",
  "Encens",
];

const ordered = computed(() => {
  const known = PREFERRED_ORDER.map((name) =>
    categories.value.find((category) => category.name === name),
  ).filter(Boolean) as Category[];

  const rest = categories.value
    .filter((category) => !PREFERRED_ORDER.includes(category.name))
    .sort((a, b) => a.name.localeCompare(b.name, "fr"));

  return [...known, ...rest];
});

onMounted(async () => {
  try {
    const { data } = await api.get<Category[]>("/categories");
    categories.value = data;
  } catch {
    // Sans catégories, la barre disparaît : la recherche reste disponible
  }
});
</script>

<template>
  <nav v-if="ordered.length" class="category-nav" aria-label="Catégories">
    <div class="container category-nav__inner">
      <RouterLink to="/produits" class="category-nav__link"
        >Tout le catalogue</RouterLink
      >

      <RouterLink
        v-for="category in ordered"
        :key="category.id"
        :to="{ path: '/produits', query: { category: category.name } }"
        class="category-nav__link"
      >
        {{ category.name }}
      </RouterLink>

      <RouterLink
        :to="{ path: '/produits', query: { sort: 'newest' } }"
        class="category-nav__link category-nav__link--accent"
      >
        Nouveautés
      </RouterLink>
    </div>
  </nav>
</template>

<style scoped>
.category-nav {
  border-bottom: 1px solid var(--color-line);
  background: var(--color-paper);
}

.category-nav__inner {
  display: flex;
  align-items: center;
  gap: var(--space-6);
  height: 46px;
  overflow-x: auto;
  scrollbar-width: none;
}

.category-nav__inner::-webkit-scrollbar {
  display: none;
}

.category-nav__link {
  position: relative;
  flex-shrink: 0;
  padding-block: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  text-decoration: none;
  white-space: nowrap;
  transition: color var(--transition-fast);
}

.category-nav__link:hover {
  color: var(--color-text);
}

/* Le trait se révèle au survol, plutôt que d'être toujours présent */
.category-nav__link::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: currentColor;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform var(--transition-base);
}

.category-nav__link:hover::after,
.category-nav__link:focus-visible::after {
  transform: scaleX(1);
}

.category-nav__link--accent {
  color: var(--color-saffron);
}

/* La barre de catégories disparaît sur mobile : le menu prend le relais */
@media (max-width: 1000px) {
  .category-nav {
    display: none;
  }
}
</style>
