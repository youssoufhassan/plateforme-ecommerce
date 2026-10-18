<script setup lang="ts">
import { computed } from "vue";
import { RouterLink } from "vue-router";
import type { CategoryNode } from "@/types/catalog";

const props = defineProps<{
  tree: CategoryNode[];
}>();

function slugify(value: string): string {
  return value
    .toLocaleLowerCase("fr-FR")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "");
}

function findNode(
  nodes: CategoryNode[],
  pathOrSlug: string,
): CategoryNode | null {
  const target = slugify(pathOrSlug);

  for (const node of nodes) {
    if (
      slugify(node.path || node.slug || node.name) === target ||
      slugify(node.name) === target
    ) {
      return node;
    }

    const found = findNode(node.children ?? [], pathOrSlug);
    if (found) return found;
  }

  return null;
}

const enfantNode = computed(() => findNode(props.tree, "enfant"));

const kidsCategories = computed(() => {
  const defaults = [
    {
      title: "Bébé (0–36 mois)",
      slug: "enfant/bebe",
      desc: "Des vêtements doux et confortables pour les premiers mois.",
      image:
        "https://images.unsplash.com/photo-1766918780916-228d10b071be?auto=format&fit=crop&w=900&q=85",
      imageAlt: "Vêtements doux pour bébé dans des tons pastel",
    },
    {
      title: "Fille (3–14 ans)",
      slug: "enfant/fille",
      desc: "Robes, mailles et tenues pour le quotidien comme les grandes occasions.",
      image:
        "https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?auto=format&fit=crop&w=900&q=85",
      imageAlt: "Mode enfant et tenues contemporaines",
    },
    {
      title: "Garçon (3–14 ans)",
      slug: "enfant/garcon",
      desc: "Chemises, pantalons, sweats et vestes pour toutes leurs aventures.",
      image:
        "https://images.unsplash.com/photo-1503919005314-30d93d07d823?auto=format&fit=crop&w=900&q=85",
      imageAlt: "Tenue enfant pour un style décontracté",
    },
    {
      title: "Chaussures & Accessoires",
      slug: "enfant/chaussures",
      desc: "Baskets, chaussures et accessoires adaptés à chaque âge.",
      image:
        "https://images.unsplash.com/photo-1514989940723-e8e51635b782?auto=format&fit=crop&w=900&q=85",
      imageAlt: "Chaussures pour compléter une tenue",
    },
  ];

  return defaults.map((item) => {
    const node = findNode(props.tree, item.slug);

    return {
      ...item,
      targetPath: node?.path || node?.slug || item.slug,
    };
  });
});
</script>

<template>
  <section class="kids-section" aria-labelledby="kids-title">
    <div class="container">
      <header class="section-head">
        <h2 id="kids-title" class="section-title">L'univers Enfant</h2>

        <RouterLink
          :to="{
            path: '/produits',
            query: {
              category: enfantNode?.path || 'enfant',
            },
          }"
          class="all-link"
        >
          Tout voir
        </RouterLink>
      </header>

      <div class="kids-grid">
        <article
          v-for="cat in kidsCategories"
          :key="cat.title"
          class="kids-card"
        >
          <RouterLink
            :to="{
              path: '/produits',
              query: { category: cat.targetPath },
            }"
            class="kids-card__image-link"
            :aria-label="`Découvrir ${cat.title}`"
          >
            <img
              :src="cat.image"
              :alt="cat.imageAlt"
              class="kids-card__image"
              loading="lazy"
              decoding="async"
            />

            <span class="kids-card__image-overlay" aria-hidden="true"></span>
          </RouterLink>

          <div class="kids-card__content">
            <h3 class="kids-title">{{ cat.title }}</h3>

            <p class="kids-desc">{{ cat.desc }}</p>

            <RouterLink
              :to="{
                path: '/produits',
                query: { category: cat.targetPath },
              }"
              class="kids-link"
            >
              Découvrir la collection
            </RouterLink>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.kids-section {
  padding-block: clamp(40px, 6vw, 76px);
  border-top: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-page, #f8f7f5);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: clamp(24px, 3vw, 36px);
}

.section-title {
  margin: 0;
  color: var(--color-ink, #000);
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.8rem, 3vw, 2.7rem);
  font-weight: 400;
  letter-spacing: -0.03em;
}

.all-link {
  flex-shrink: 0;
  padding-bottom: 5px;
  border-bottom: 1px solid var(--color-line, #e0dfdb);
  color: var(--color-ink, #000);
  font-size: 0.82rem;
  font-weight: 500;
  text-decoration: none;
  transition: border-color 0.2s ease;
}

.all-link:hover {
  border-color: var(--color-ink, #000);
}

/* Grille */

.kids-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: clamp(14px, 2vw, 24px);
  align-items: stretch;
}

/* Cartes */

.kids-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-surface, #fff);
  transition:
    border-color 0.25s ease,
    box-shadow 0.25s ease,
    transform 0.25s ease;
}

.kids-card:hover {
  border-color: #c9b28d;
  box-shadow: 0 12px 28px rgb(0 0 0 / 5%);
  transform: translateY(-3px);
}

/* Images */

.kids-card__image-link {
  position: relative;
  display: block;
  aspect-ratio: 4 / 4.5;
  overflow: hidden;
  background: #eae5de;
}

.kids-card__image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 600ms ease;
}

.kids-card:hover .kids-card__image {
  transform: scale(1.045);
}

.kids-card__image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgb(0 0 0 / 3%) 0%,
    transparent 55%,
    rgb(0 0 0 / 10%) 100%
  );
  pointer-events: none;
}

/* Contenu */

.kids-card__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: clamp(18px, 2vw, 26px);
}

.kids-title {
  margin: 0 0 12px;
  color: var(--color-ink, #000);
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.2rem, 1.5vw, 1.5rem);
  font-weight: 400;
  line-height: 1.25;
}

.kids-desc {
  flex: 1;
  margin: 0;
  color: var(--color-text-muted, #767672);
  font-size: 0.84rem;
  line-height: 1.65;
}

/* Lien */

.kids-link {
  display: block;
  margin-top: 22px;
  padding-top: 14px;
  border-top: 1px solid var(--color-line, #e0dfdb);
  color: var(--color-ink, #000);
  font-size: 0.78rem;
  font-weight: 500;
  text-decoration: none;
}

.kids-link:hover {
  text-decoration: underline;
  text-underline-offset: 4px;
}

/* Tablettes */

@media (max-width: 1050px) {
  .kids-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
  }

  .kids-card__image-link {
    aspect-ratio: 5 / 3.8;
  }

  .kids-title {
    font-size: 1.45rem;
  }
}

/* Mobiles */

@media (max-width: 600px) {
  .kids-section {
    padding-block: 36px;
  }

  .section-head {
    align-items: baseline;
    gap: 16px;
    margin-bottom: 24px;
  }

  .section-title {
    font-size: 1.8rem;
  }

  .all-link {
    font-size: 0.78rem;
  }

  .kids-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .kids-card__image-link {
    aspect-ratio: 5 / 3.2;
  }

  .kids-card__content {
    padding: 20px;
  }

  .kids-title {
    font-size: 1.4rem;
  }

  .kids-desc {
    font-size: 0.83rem;
  }
}

/* Accessibilité */

@media (prefers-reduced-motion: reduce) {
  .kids-card,
  .kids-card__image {
    transition: none;
  }
}
</style>
