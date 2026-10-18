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

const maisonNode = computed(() => findNode(props.tree, "maison"));
const electroniqueNode = computed(() => findNode(props.tree, "electronique"));

const maisonSubcategories = computed(() => {
  if (maisonNode.value?.children?.length) {
    return maisonNode.value.children.map((category) => ({
      name: category.name,
      slug: category.path || category.slug || slugify(category.name),
    }));
  }

  return [
    { name: "Senteurs & Encens d'intérieur", slug: "maison/senteurs" },
    { name: "Art de la Table", slug: "maison/art-de-la-table" },
    { name: "Objets Décoratifs", slug: "maison/decoration" },
    { name: "Linge de Maison", slug: "maison/linge" },
  ];
});

const electroniqueSubcategories = computed(() => {
  if (electroniqueNode.value?.children?.length) {
    return electroniqueNode.value.children.map((category) => ({
      name: category.name,
      slug: category.path || category.slug || slugify(category.name),
    }));
  }

  return [
    { name: "Audio & Écoute Nomade", slug: "electronique/audio" },
    { name: "Accessoires Connectés", slug: "electronique/accessoires" },
    { name: "High-Tech d'Exception", slug: "electronique/high-tech" },
  ];
});
</script>

<template>
  <section class="lifestyle-section" aria-labelledby="lifestyle-title">
    <div class="container">
      <header class="section-head">
        <h2 id="lifestyle-title" class="section-title">
          Maison & Électronique
        </h2>
      </header>

      <div class="lifestyle-grid">
        <!-- UNIVERS MAISON -->
        <article class="lifestyle-card">
          <RouterLink
            :to="{
              path: '/produits',
              query: {
                category: maisonNode?.path || 'maison',
              },
            }"
            class="lifestyle-card__visual"
            aria-label="Découvrir l'univers Maison"
          >
            <img
              src="https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=1400&q=85"
              alt="Intérieur contemporain aux matières naturelles et raffinées"
              class="lifestyle-card__image"
              loading="lazy"
              decoding="async"
            />

            <span class="lifestyle-card__overlay" aria-hidden="true"></span>

            <span class="lifestyle-card__visual-content">
              <span class="visual-title">La beauté du quotidien</span>
            </span>
          </RouterLink>

          <div class="card-inner">
            <h3 class="card-title">Maison & Art de vivre</h3>

            <p class="card-desc">
              Senteurs raffinées, objets décoratifs et linge de maison pour
              apporter une touche singulière à votre intérieur.
            </p>

            <nav class="card-links" aria-label="Catégories Maison">
              <RouterLink
                v-for="sub in maisonSubcategories"
                :key="sub.name"
                :to="{
                  path: '/produits',
                  query: { category: sub.slug },
                }"
                class="lifestyle-link"
              >
                {{ sub.name }}
              </RouterLink>
            </nav>

            <RouterLink
              :to="{
                path: '/produits',
                query: {
                  category: maisonNode?.path || 'maison',
                },
              }"
              class="card-cta"
            >
              Tout voir
            </RouterLink>
          </div>
        </article>

        <!-- UNIVERS ÉLECTRONIQUE -->
        <article class="lifestyle-card">
          <RouterLink
            :to="{
              path: '/produits',
              query: {
                category: electroniqueNode?.path || 'electronique',
              },
            }"
            class="lifestyle-card__visual"
            aria-label="Découvrir l'univers Électronique"
          >
            <img
              src="https://images.unsplash.com/photo-1498049794561-7780e7231661?auto=format&fit=crop&w=1400&q=85"
              alt="Sélection de produits électroniques et accessoires technologiques"
              class="lifestyle-card__image"
              loading="lazy"
              decoding="async"
            />

            <span class="lifestyle-card__overlay" aria-hidden="true"></span>

            <span class="lifestyle-card__visual-content">
              <span class="visual-title">La technologie au quotidien</span>
            </span>
          </RouterLink>

          <div class="card-inner">
            <h3 class="card-title">Électronique & High-Tech</h3>

            <p class="card-desc">
              Audio, accessoires connectés et équipements technologiques pour
              accompagner votre quotidien.
            </p>

            <nav class="card-links" aria-label="Catégories Électronique">
              <RouterLink
                v-for="sub in electroniqueSubcategories"
                :key="sub.name"
                :to="{
                  path: '/produits',
                  query: { category: sub.slug },
                }"
                class="lifestyle-link"
              >
                {{ sub.name }}
              </RouterLink>
            </nav>

            <RouterLink
              :to="{
                path: '/produits',
                query: {
                  category: electroniqueNode?.path || 'electronique',
                },
              }"
              class="card-cta"
            >
              Tout voir
            </RouterLink>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.lifestyle-section {
  padding-block: clamp(40px, 6vw, 76px);
  border-top: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-page, #f8f7f5);
}

.section-head {
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

.lifestyle-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(18px, 2.5vw, 32px);
  align-items: stretch;
}

.lifestyle-card {
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

.lifestyle-card:hover {
  border-color: #c9b28d;
  box-shadow: 0 12px 30px rgb(0 0 0 / 5%);
  transform: translateY(-3px);
}

/* Photographies */

.lifestyle-card__visual {
  position: relative;
  display: block;
  aspect-ratio: 1.85 / 1;
  overflow: hidden;
  background: #e6e1d8;
  color: #fff;
  text-decoration: none;
}

.lifestyle-card__image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 700ms ease;
}

.lifestyle-card:hover .lifestyle-card__image {
  transform: scale(1.045);
}

.lifestyle-card__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgb(0 0 0 / 3%) 0%,
    rgb(0 0 0 / 8%) 35%,
    rgb(0 0 0 / 60%) 100%
  );
}

.lifestyle-card__visual-content {
  position: absolute;
  right: clamp(18px, 3vw, 32px);
  bottom: clamp(18px, 3vw, 30px);
  left: clamp(18px, 3vw, 32px);
  z-index: 1;
}

.visual-title {
  display: block;
  max-width: 18ch;
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.6rem, 2.7vw, 2.5rem);
  font-weight: 400;
  line-height: 1.1;
}

/* Contenu */

.card-inner {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: clamp(22px, 3vw, 36px);
}

.card-title {
  margin: 0;
  color: var(--color-ink, #000);
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.4rem, 2vw, 1.8rem);
  font-weight: 400;
  line-height: 1.2;
}

.card-desc {
  max-width: 50ch;
  margin: 14px 0 22px;
  color: var(--color-text-muted, #767672);
  font-size: 0.86rem;
  line-height: 1.7;
}

/* Sous-catégories */

.card-links {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 8px;
}

.lifestyle-link {
  display: block;
  padding: 11px 13px;
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-page, #f8f7f5);
  color: var(--color-text-secondary, #3d3d3b);
  font-size: 0.8rem;
  line-height: 1.45;
  text-decoration: none;
  transition:
    border-color 0.2s ease,
    background 0.2s ease,
    color 0.2s ease;
}

.lifestyle-link:hover {
  border-color: var(--color-ink, #000);
  background: var(--color-surface, #fff);
  color: var(--color-ink, #000);
}

/* Lien principal */

.card-cta {
  display: block;
  margin-top: 24px;
  padding: 14px 16px;
  border: 1px solid var(--color-ink, #000);
  background: var(--color-ink, #000);
  color: #fff;
  font-size: 0.76rem;
  font-weight: 500;
  text-align: center;
  text-decoration: none;
  transition:
    background 0.2s ease,
    color 0.2s ease;
}

.card-cta:hover {
  background: transparent;
  color: var(--color-ink, #000);
}

/* Tablettes */

@media (max-width: 900px) {
  .lifestyle-grid {
    grid-template-columns: 1fr;
    gap: 22px;
  }

  .lifestyle-card__visual {
    aspect-ratio: 2.3 / 1;
  }

  .visual-title {
    font-size: clamp(1.8rem, 4vw, 2.4rem);
  }
}

/* Mobiles */

@media (max-width: 600px) {
  .lifestyle-section {
    padding-block: 36px;
  }

  .section-head {
    margin-bottom: 24px;
  }

  .lifestyle-grid {
    gap: 18px;
  }

  .lifestyle-card__visual {
    aspect-ratio: 1.5 / 1;
  }

  .lifestyle-card__visual-content {
    right: 18px;
    bottom: 18px;
    left: 18px;
  }

  .visual-title {
    font-size: 1.7rem;
  }

  .card-inner {
    padding: 20px;
  }

  .card-title {
    font-size: 1.45rem;
  }

  .card-desc {
    font-size: 0.83rem;
  }
}

/* Accessibilité */

@media (prefers-reduced-motion: reduce) {
  .lifestyle-card,
  .lifestyle-card__image {
    transition: none;
  }
}
</style>
