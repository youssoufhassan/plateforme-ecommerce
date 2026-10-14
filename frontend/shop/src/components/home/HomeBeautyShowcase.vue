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

const beauteNode = computed(
  () => findNode(props.tree, "beaute") || findNode(props.tree, "parfums"),
);

const beautyCards = computed(() => {
  const definitions = [
    {
      title: "Haute Parfumerie",
      categoryKey: "parfums",
      tag: "SIGNATURES",
      desc: "Des sillages mémorables, des notes ambrées, florales et boisées pour révéler votre signature.",
      image:
        "https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=900&q=85",
      alt: "Flacons de parfum dans une composition élégante",
      sublinks: [
        "Eaux de parfum",
        "Extraits de parfum",
        "Huiles parfumées",
        "Bakhour & Encens",
      ],
    },
    {
      title: "Soins du Visage",
      categoryKey: "cremes",
      tag: "RITUELS",
      desc: "Crèmes, sérums et soins essentiels pour intégrer un rituel de beauté à votre quotidien.",
      image:
        "https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?auto=format&fit=crop&w=900&q=85",
      alt: "Produits cosmétiques pour le soin du visage",
      sublinks: ["Crèmes hydratantes", "Sérums", "Masques", "Nettoyants doux"],
    },
    {
      title: "Soins Capillaires",
      categoryKey: "produits-capillaires",
      tag: "EXPERTISE",
      desc: "Des huiles, masques et soins nourrissants pour prendre soin de vos cheveux.",
      image:
        "https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?auto=format&fit=crop&w=900&q=85",
      alt: "Produits de beauté et soins capillaires",
      sublinks: [
        "Shampoings doux",
        "Masques capillaires",
        "Poudre de Chébé",
        "Sérums pointes",
      ],
    },
    {
      title: "Pommades & Huiles",
      categoryKey: "huiles",
      tag: "TRADITION",
      desc: "Huiles corporelles, baumes et textures enveloppantes inspirés des rituels de soin.",
      image:
        "https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=900&q=85",
      alt: "Huiles et soins pour le corps",
      sublinks: [
        "Huiles corporelles",
        "Pommades traditionnelles",
        "Baumes nourrissants",
      ],
    },
  ];

  return definitions.map((def) => {
    const node =
      findNode(props.tree, def.categoryKey) ||
      findNode(props.tree, `beaute/${def.categoryKey}`);

    return {
      ...def,
      node,
      targetPath: node?.path || node?.slug || def.categoryKey,
    };
  });
});
</script>

<template>
  <section class="beauty-section" aria-labelledby="beauty-title">
    <div class="container">
      <header class="section-head">
        <div class="section-head__text">
          <span class="eyebrow">RITUELS & FRAGRANCES</span>

          <h2 id="beauty-title" class="section-title">L'univers Beauté</h2>

          <p class="section-desc">
            Des sillages mémorables aux soins bienfaisants : découvrez une
            vision singulière de la beauté et du bien-être.
          </p>
        </div>

        <RouterLink
          :to="{
            path: '/produits',
            query: {
              category: beauteNode?.path || 'beaute',
            },
          }"
          class="all-link"
        >
          Explorer toute la Beauté
          <span aria-hidden="true">↗</span>
        </RouterLink>
      </header>

      <!-- Quatre univers illustrés -->
      <div class="beauty-grid">
        <article
          v-for="card in beautyCards"
          :key="card.title"
          class="beauty-card"
        >
          <!-- Image éditoriale -->
          <RouterLink
            :to="{
              path: '/produits',
              query: { category: card.targetPath },
            }"
            class="beauty-card__image-link"
            :aria-label="`Découvrir ${card.title}`"
          >
            <img
              :src="card.image"
              :alt="card.alt"
              class="beauty-card__image"
              loading="lazy"
              decoding="async"
            />

            <span class="beauty-card__image-overlay" aria-hidden="true"></span>

            <span class="beauty-card__image-arrow" aria-hidden="true"> ↗ </span>
          </RouterLink>

          <!-- Contenu -->
          <div class="beauty-card__content">
            <span class="beauty-tag">{{ card.tag }}</span>

            <RouterLink
              :to="{
                path: '/produits',
                query: { category: card.targetPath },
              }"
              class="beauty-card__title-link"
            >
              <h3>{{ card.title }}</h3>
              <span class="arrow-icon" aria-hidden="true">↗</span>
            </RouterLink>

            <p class="beauty-card__desc">
              {{ card.desc }}
            </p>

            <div class="beauty-sublinks">
              <RouterLink
                v-for="sub in card.sublinks"
                :key="sub"
                :to="{
                  path: '/produits',
                  query: { q: sub },
                }"
                class="sublink"
              >
                {{ sub }}
              </RouterLink>
            </div>

            <RouterLink
              :to="{
                path: '/produits',
                query: { category: card.targetPath },
              }"
              class="beauty-card__discover"
            >
              Découvrir la sélection
              <span aria-hidden="true">→</span>
            </RouterLink>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.beauty-section {
  padding-block: clamp(40px, 6vw, 76px);
  border-top: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-surface, #fff);
}

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
  max-width: 52ch;
  margin: 10px 0 0;
  color: var(--color-text-muted, #767672);
  font-size: 0.92rem;
  line-height: 1.6;
}

.all-link {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--color-line, #e0dfdb);
  color: var(--color-ink, #000);
  font-size: 0.82rem;
  font-weight: 500;
  text-decoration: none;
  white-space: nowrap;
  transition: border-color 0.2s ease;
}

.all-link:hover {
  border-color: var(--color-ink, #000);
}

/* Grille principale */

.beauty-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: clamp(14px, 2vw, 24px);
  align-items: stretch;
}

.beauty-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-page, #f8f7f5);
  transition:
    border-color 0.25s ease,
    box-shadow 0.25s ease,
    transform 0.25s ease;
}

.beauty-card:hover {
  border-color: #c9b28d;
  box-shadow: 0 12px 28px rgb(0 0 0 / 5%);
  transform: translateY(-3px);
}

/* Photographies */

.beauty-card__image-link {
  position: relative;
  display: block;
  aspect-ratio: 4 / 4.5;
  overflow: hidden;
  background: #e8e4dc;
  color: #fff;
}

.beauty-card__image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 600ms ease;
}

.beauty-card:hover .beauty-card__image {
  transform: scale(1.045);
}

.beauty-card__image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgb(0 0 0 / 8%) 0%,
    transparent 42%,
    rgb(0 0 0 / 12%) 100%
  );
  pointer-events: none;
}

.beauty-card__image-arrow {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border: 1px solid rgb(255 255 255 / 75%);
  border-radius: 50%;
  background: rgb(255 255 255 / 90%);
  color: #171714;
  font-size: 15px;
  transition:
    color 0.2s ease,
    background 0.2s ease,
    transform 0.2s ease;
}

.beauty-card:hover .beauty-card__image-arrow {
  transform: translate(2px, -2px);
  background: #171714;
  color: #fff;
}

/* Textes et catégories */

.beauty-card__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: clamp(16px, 2vw, 24px);
}

.beauty-tag {
  display: inline-block;
  margin-bottom: 10px;
  color: #a66b23;
  font-size: 0.62rem;
  font-weight: 600;
  letter-spacing: 0.16em;
}

.beauty-card__title-link {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
  color: var(--color-ink, #000);
  text-decoration: none;
}

.beauty-card__title-link h3 {
  margin: 0;
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.15rem, 1.5vw, 1.45rem);
  font-weight: 400;
  letter-spacing: -0.02em;
  line-height: 1.25;
}

.arrow-icon {
  flex-shrink: 0;
  color: var(--color-text-muted, #767672);
  font-size: 0.85rem;
  transition:
    transform 0.2s ease,
    color 0.2s ease;
}

.beauty-card:hover .arrow-icon {
  transform: translate(3px, -3px);
  color: var(--color-ink, #000);
}

.beauty-card__desc {
  margin: 0;
  color: var(--color-text-muted, #767672);
  font-size: 0.82rem;
  line-height: 1.65;
}

/* Sous-catégories */

.beauty-sublinks {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-line, #e0dfdb);
}

.sublink {
  padding: 6px 8px;
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-surface, #fff);
  color: var(--color-text-secondary, #3d3d3b);
  font-size: 0.7rem;
  line-height: 1.3;
  text-decoration: none;
  transition:
    border-color 0.2s ease,
    color 0.2s ease;
}

.sublink:hover {
  border-color: var(--color-ink, #000);
  color: var(--color-ink, #000);
}

/* Lien inférieur */

.beauty-card__discover {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: auto;
  padding-top: 20px;
  color: var(--color-ink, #000);
  font-size: 0.74rem;
  font-weight: 500;
  text-decoration: none;
}

.beauty-card__discover span {
  transition: transform 0.2s ease;
}

.beauty-card__discover:hover span {
  transform: translateX(4px);
}

/* Tablettes */

@media (max-width: 1100px) {
  .beauty-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
  }

  .beauty-card__image-link {
    aspect-ratio: 5 / 3.5;
  }

  .beauty-card__title-link h3 {
    font-size: 1.4rem;
  }
}

/* Mobiles */

@media (max-width: 640px) {
  .beauty-section {
    padding-block: 36px;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 24px;
  }

  .section-desc {
    font-size: 0.86rem;
  }

  .beauty-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .beauty-card__image-link {
    aspect-ratio: 5 / 3.2;
  }

  .beauty-card__content {
    padding: 20px;
  }

  .beauty-card__title-link h3 {
    font-size: 1.45rem;
  }

  .beauty-card__desc {
    font-size: 0.85rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .beauty-card,
  .beauty-card__image,
  .beauty-card__image-arrow,
  .arrow-icon,
  .beauty-card__discover span {
    transition: none;
  }
}
</style>
