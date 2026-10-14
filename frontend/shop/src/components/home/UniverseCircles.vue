<script setup lang="ts">
import { computed, ref } from "vue";
import { RouterLink } from "vue-router";

import type { CategoryNode } from "@/types/catalog";

const props = defineProps<{
  tree: CategoryNode[];
}>();

interface Circle {
  key: string;
  label: string;
  imageUrl: string;
  to: { path: string; query: Record<string, string> };
}

/**
 * Photos éditoriales temporaires pour rendre les univers plus visuels.
 * Remplacer ces visuels de démonstration par les photos SIDRA validées
 * avant une mise en production commerciale.
 */
const categoryVisuals = [
  {
    terms: ["homme", "men", "mens", "masculin"],
    imageUrl:
      "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=480&q=80",
  },
  {
    terms: ["femme", "women", "womens", "feminin", "féminin"],
    imageUrl:
      "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=480&q=80",
  },
  {
    terms: ["beaute", "beauté", "parfum", "cosmetique", "cosmétique", "soin"],
    imageUrl:
      "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=480&q=80",
  },
  {
    terms: ["enfant", "kids", "bebe", "bébé", "junior"],
    imageUrl:
      "https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?auto=format&fit=crop&w=480&q=80",
  },
  {
    terms: ["maison", "deco", "déco", "interieur", "intérieur", "mobilier"],
    imageUrl:
      "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=480&q=80",
  },
  {
    terms: ["electronique", "électronique", "tech", "informatique", "audio"],
    imageUrl:
      "https://images.unsplash.com/photo-1498049794561-7780e7231661?auto=format&fit=crop&w=480&q=80",
  },
  {
    terms: ["montre", "bijou", "bijoux", "accessoire", "accessoires", "sac"],
    imageUrl:
      "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=480&q=80",
  },
  {
    terms: ["mode", "vetement", "vêtement", "fashion", "vetements", "vêtements"],
    imageUrl:
      "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=480&q=80",
  },
];

const fallbackImage =
  "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=480&q=80";

function normalize(value: string | undefined): string {
  return (value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLocaleLowerCase("fr-FR");
}

function getCategoryImage(node: CategoryNode): string {
  const search = normalize(`${node.name} ${node.slug} ${node.path}`);
  const match = categoryVisuals.find((visual) =>
    visual.terms.some((term) => search.includes(normalize(term))),
  );

  return match?.imageUrl ?? fallbackImage;
}

const failedImages = ref<Record<string, boolean>>({});

/**
 * Affiche une entrée par univers actif, avec une photo plutôt qu'une initiale.
 * Les liens conservent le chemin réel de la catégorie fourni par l'API.
 */
const circles = computed<Circle[]>(() => {
  const fromTree = props.tree
    .filter((node) => node.active !== false && node.productCount > 0)
    .map((node) => ({
      key: node.id,
      label: node.name,
      imageUrl: getCategoryImage(node),
      to: { path: "/produits", query: { category: node.path } },
    }));

  return [
    ...fromTree,
    {
      key: "newest",
      label: "Nouveautés",
      imageUrl:
        "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=480&q=80",
      to: { path: "/produits", query: { sort: "newest" } },
    },
  ];
});
</script>

<template>
  <section v-if="circles.length > 1" class="universes" aria-label="Toutes les catégories">
    <div class="container">
      <div class="universes__header">
        <p class="universes__eyebrow">À explorer</p>
        <h2 class="universes__title">Toutes les catégories</h2>
      </div>

      <nav class="universes__row" aria-label="Univers SIDRA">
        <RouterLink
          v-for="circle in circles"
          :key="circle.key"
          :to="circle.to"
          class="universe"
        >
          <span class="universe__disc">
            <img
              v-if="!failedImages[circle.key]"
              :src="circle.imageUrl"
              :alt="`Univers ${circle.label}`"
              class="universe__image"
              loading="lazy"
              decoding="async"
              @error="failedImages[circle.key] = true"
            />
            <span v-else class="universe__initial" aria-hidden="true">
              {{ circle.label.charAt(0) }}
            </span>
          </span>

          <span class="universe__label">{{ circle.label }}</span>
        </RouterLink>
      </nav>
    </div>
  </section>
</template>

<style scoped>
.universes {
  padding-block: var(--space-8);
  border-bottom: 1px solid var(--color-line);
}

.universes__header {
  margin-bottom: var(--space-5);
}

.universes__eyebrow {
  margin: 0 0 4px;
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.universes__title {
  margin: 0;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: clamp(22px, 2.2vw, 30px);
  font-weight: var(--weight-regular);
  letter-spacing: var(--tracking-tight);
  line-height: 1.15;
}

.universes__row {
  display: flex;
  justify-content: flex-start;
  gap: clamp(16px, 2.2vw, 32px);
  overflow-x: auto;
  padding: 2px 0 8px;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-x: contain;
}

.universes__row::-webkit-scrollbar {
  display: none;
}

.universe {
  display: grid;
  justify-items: center;
  align-content: start;
  gap: 10px;
  flex: 0 0 82px;
  color: inherit;
  text-decoration: none;
}

.universe__disc {
  display: block;
  width: 82px;
  height: 82px;
  overflow: hidden;
  border-radius: 50%;
  background: var(--color-plate);
  transition:
    transform var(--transition-base),
    box-shadow var(--transition-base);
}

.universe__image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 450ms ease;
}

.universe:hover .universe__disc {
  box-shadow: 0 5px 18px rgb(0 0 0 / 12%);
  transform: translateY(-2px);
}

.universe:hover .universe__image {
  transform: scale(1.07);
}

.universe__initial {
  display: grid;
  width: 100%;
  height: 100%;
  place-items: center;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: 28px;
}

.universe__label {
  max-width: 100%;
  color: var(--color-text);
  font-size: 12px;
  line-height: 1.3;
  text-align: center;
  overflow-wrap: anywhere;
}

@media (min-width: 1200px) {
  .universes__row {
    justify-content: space-between;
  }
}

@media (max-width: 767px) {
  .universes {
    padding-block: var(--space-6);
  }

  .universes__header {
    margin-bottom: var(--space-4);
  }

  .universes__row {
    gap: 16px;
    margin-right: calc(var(--container-padding) * -1);
    padding-right: var(--container-padding);
  }

  .universe {
    flex-basis: 70px;
    gap: 8px;
  }

  .universe__disc {
    width: 70px;
    height: 70px;
  }

  .universe__label {
    font-size: 11px;
  }
}
</style>
