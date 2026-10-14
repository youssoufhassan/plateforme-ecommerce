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
        <div class="section-head__text">
          <span class="eyebrow">ART DE VIVRE & TECHNOLOGIE</span>

          <h2 id="lifestyle-title" class="section-title">
            Maison & Électronique
          </h2>

          <p class="section-desc">
            Des intérieurs inspirés et des innovations soigneusement
            sélectionnées pour leur esthétique et leurs performances.
          </p>
        </div>
      </header>

      <div class="lifestyle-grid">
        <!-- UNIVERS MAISON -->
        <article class="lifestyle-card lifestyle-card--home">
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
              <span class="visual-eyebrow">ART DE VIVRE</span>
              <span class="visual-title">La beauté du quotidien</span>
              <span class="visual-link">
                Explorer la Maison <span aria-hidden="true">↗</span>
              </span>
            </span>
          </RouterLink>

          <div class="card-inner">
            <div class="card-heading">
              <div>
                <span class="card-eyebrow">CADRE DE VIE</span>
                <h3 class="card-title">Maison & Art de vivre</h3>
              </div>

              <span class="card-index" aria-hidden="true">01</span>
            </div>

            <p class="card-desc">
              Senteurs raffinées, diffuseurs, objets décoratifs et linge de
              maison pour apporter une touche singulière à votre intérieur.
            </p>

            <div class="card-links">
              <RouterLink
                v-for="sub in maisonSubcategories"
                :key="sub.name"
                :to="{
                  path: '/produits',
                  query: { category: sub.slug },
                }"
                class="lifestyle-link"
              >
                <span>{{ sub.name }}</span>
                <span class="link-arrow" aria-hidden="true">↗</span>
              </RouterLink>
            </div>

            <RouterLink
              :to="{
                path: '/produits',
                query: {
                  category: maisonNode?.path || 'maison',
                },
              }"
              class="card-cta"
            >
              <span>Explorer l'univers Maison</span>
              <span class="cta-arrow" aria-hidden="true">→</span>
            </RouterLink>
          </div>
        </article>

        <!-- UNIVERS ÉLECTRONIQUE -->
        <article class="lifestyle-card lifestyle-card--tech">
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
              <span class="visual-eyebrow">TECHNOLOGIE & INNOVATION</span>
              <span class="visual-title">L'essentiel, autrement</span>
              <span class="visual-link">
                Explorer la Technologie <span aria-hidden="true">↗</span>
              </span>
            </span>
          </RouterLink>

          <div class="card-inner">
            <div class="card-heading">
              <div>
                <span class="card-eyebrow">PRÉCISION & PERFORMANCE</span>
                <h3 class="card-title">Électronique & High-Tech</h3>
              </div>

              <span class="card-index" aria-hidden="true">02</span>
            </div>

            <p class="card-desc">
              Audio, accessoires connectés et équipements technologiques : des
              solutions pensées pour accompagner votre quotidien.
            </p>

            <div class="card-links">
              <RouterLink
                v-for="sub in electroniqueSubcategories"
                :key="sub.name"
                :to="{
                  path: '/produits',
                  query: { category: sub.slug },
                }"
                class="lifestyle-link"
              >
                <span>{{ sub.name }}</span>
                <span class="link-arrow" aria-hidden="true">↗</span>
              </RouterLink>
            </div>

            <RouterLink
              :to="{
                path: '/produits',
                query: {
                  category: electroniqueNode?.path || 'electronique',
                },
              }"
              class="card-cta"
            >
              <span>Explorer l'Électronique</span>
              <span class="cta-arrow" aria-hidden="true">→</span>
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

/* En-tête de section */

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: clamp(28px, 4vw, 44px);
}

.eyebrow {
  display: block;
  margin-bottom: 8px;
  color: var(--color-text-muted, #767672);
  font-size: 0.68rem;
  font-weight: 600;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.section-title {
  margin: 0;
  color: var(--color-ink, #000);
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.8rem, 3vw, 2.7rem);
  font-weight: 400;
  letter-spacing: -0.03em;
}

.section-desc {
  max-width: 56ch;
  margin: 10px 0 0;
  color: var(--color-text-muted, #767672);
  font-size: 0.92rem;
  line-height: 1.65;
}

/* Deux univers équilibrés */

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

/* Grande photographie */

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
    rgb(0 0 0 / 5%) 0%,
    rgb(0 0 0 / 4%) 35%,
    rgb(0 0 0 / 66%) 100%
  );
}

.lifestyle-card__visual-content {
  position: absolute;
  right: clamp(18px, 3vw, 32px);
  bottom: clamp(18px, 3vw, 30px);
  left: clamp(18px, 3vw, 32px);
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 9px;
}

.visual-eyebrow {
  font-size: 0.62rem;
  font-weight: 600;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.visual-title {
  max-width: 18ch;
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.6rem, 2.7vw, 2.5rem);
  font-weight: 400;
  line-height: 1.1;
}

.visual-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 5px;
  padding-bottom: 5px;
  border-bottom: 1px solid rgb(255 255 255 / 65%);
  font-size: 0.76rem;
  font-weight: 500;
}

.visual-link span {
  transition: transform 0.2s ease;
}

.lifestyle-card__visual:hover .visual-link span {
  transform: translate(2px, -2px);
}

/* Contenu des cartes */

.card-inner {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: clamp(22px, 3vw, 36px);
}

.card-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--color-line, #e0dfdb);
}

.card-eyebrow {
  display: block;
  margin-bottom: 6px;
  color: var(--color-text-muted, #767672);
  font-size: 0.62rem;
  font-weight: 600;
  letter-spacing: 0.16em;
}

.card-title {
  margin: 0;
  color: var(--color-ink, #000);
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.4rem, 2vw, 1.8rem);
  font-weight: 400;
  line-height: 1.2;
}

.card-index {
  color: #b7a68b;
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: 1.5rem;
  line-height: 1;
}

.card-desc {
  max-width: 50ch;
  margin: 18px 0 22px;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
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

.link-arrow {
  flex-shrink: 0;
  color: var(--color-text-muted, #767672);
  transition: transform 0.2s ease;
}

.lifestyle-link:hover .link-arrow {
  transform: translate(2px, -2px);
}

/* Bouton de fin de carte */

.card-cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
  padding: 14px 16px;
  border: 1px solid var(--color-ink, #000);
  background: var(--color-ink, #000);
  color: #fff;
  font-size: 0.76rem;
  font-weight: 500;
  text-decoration: none;
  transition:
    background 0.2s ease,
    color 0.2s ease;
}

.card-cta:hover {
  background: transparent;
  color: var(--color-ink, #000);
}

.cta-arrow {
  font-size: 1rem;
  transition: transform 0.2s ease;
}

.card-cta:hover .cta-arrow {
  transform: translateX(4px);
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

  .section-desc {
    font-size: 0.86rem;
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

/* Respect des préférences d'accessibilité */

@media (prefers-reduced-motion: reduce) {
  .lifestyle-card,
  .lifestyle-card__image,
  .visual-link span,
  .link-arrow,
  .cta-arrow {
    transition: none;
  }
}
</style>
