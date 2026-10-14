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

const sides = computed(() => [
  {
    key: "homme",
    title: "Accessoires Homme",
    eyebrow: "LES DÉTAILS DU VESTIAIRE",
    rootQuery:
      findNode(props.tree, "homme/accessoires")?.path || "homme/accessoires",
    coverImage:
      "https://images.unsplash.com/photo-1568010485396-05c92639b2e9?auto=format&fit=crop&w=1200&q=85",
    coverAlt: "Montre et maroquinerie dans une composition masculine élégante",
    coverTitle: "Le détail fait la différence",
    groups: [
      {
        title: "Montres & Horlogerie",
        query: findNode(props.tree, "homme/montres")?.path || "homme/montres",
        image:
          "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=85",
        imageAlt: "Montre au cadran élégant",
        items: [
          "Montres automatiques",
          "Chronographes",
          "Bracelets cuir & acier",
        ],
      },
      {
        title: "Sacs & Maroquinerie",
        query: findNode(props.tree, "homme/sacs")?.path || "homme/sacs",
        image:
          "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=85",
        imageAlt: "Sac et accessoires de voyage",
        items: [
          "Porte-documents",
          "Sacs week-end",
          "Portefeuilles",
          "Ceintures cuir",
        ],
      },
      {
        title: "Accessoires de Mode",
        query:
          findNode(props.tree, "homme/accessoires")?.path ||
          "homme/accessoires",
        image:
          "https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=600&q=85",
        imageAlt: "Lunettes de soleil et accessoires de mode",
        items: [
          "Boutons de manchette",
          "Écharpes & foulards",
          "Lunettes de soleil",
        ],
      },
    ],
  },
  {
    key: "femme",
    title: "Accessoires Femme",
    eyebrow: "FINITIONS & JOAILLERIE",
    rootQuery:
      findNode(props.tree, "femme/accessoires")?.path || "femme/accessoires",
    coverImage:
      "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=1200&q=85",
    coverAlt: "Bijoux et accessoires dans une composition raffinée",
    coverTitle: "L'élégance dans chaque détail",
    groups: [
      {
        title: "Sacs & Petite Maroquinerie",
        query: findNode(props.tree, "femme/sacs")?.path || "femme/sacs",
        image:
          "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=600&q=85",
        imageAlt: "Sac à main dans un style intemporel",
        items: [
          "Sacs à main",
          "Pochettes du soir",
          "Portefeuilles",
          "Bandoulières",
        ],
      },
      {
        title: "Bijoux & Joaillerie",
        query: findNode(props.tree, "femme/bijoux")?.path || "femme/bijoux",
        image:
          "https://images.unsplash.com/photo-1611652022419-a9419f74343d?auto=format&fit=crop&w=600&q=85",
        imageAlt: "Bijoux délicats aux tons dorés",
        items: [
          "Colliers & pendentifs",
          "Boucles d'oreilles",
          "Bagues",
          "Bracelets fins",
        ],
      },
      {
        title: "Montres & Accessoires",
        query: findNode(props.tree, "femme/montres")?.path || "femme/montres",
        image:
          "https://images.unsplash.com/photo-1523170335258-f5ed11844a49?auto=format&fit=crop&w=600&q=85",
        imageAlt: "Montre élégante aux finitions soignées",
        items: [
          "Montres joaillières",
          "Carrés de soie",
          "Lunettes solaires",
          "Ceintures",
        ],
      },
    ],
  },
]);
</script>

<template>
  <section class="accessories-section" aria-labelledby="accessories-title">
    <div class="container">
      <header class="section-head">
        <div class="section-head__text">
          <span class="eyebrow">LE SENS DU DÉTAIL</span>

          <h2 id="accessories-title" class="section-title">Les accessoires</h2>

          <p class="section-desc">
            Montres, maroquinerie, bijoux et pièces de finition : des
            accessoires choisis pour compléter chaque silhouette.
          </p>
        </div>

        <RouterLink
          :to="{
            path: '/produits',
            query: { category: 'accessoires' },
          }"
          class="all-link"
        >
          Tous les accessoires
          <span aria-hidden="true">↗</span>
        </RouterLink>
      </header>

      <!-- Deux univers, côte à côte sur grand écran -->
      <div class="accessories-columns">
        <article
          v-for="side in sides"
          :key="side.key"
          class="accessory-column"
          :class="`accessory-column--${side.key}`"
        >
          <!-- En-tête de l'univers -->
          <header class="column-header">
            <div>
              <span class="column-eyebrow">
                {{ side.eyebrow }}
              </span>

              <h3 class="column-title">
                {{ side.title }}
              </h3>
            </div>

            <RouterLink
              :to="{
                path: '/produits',
                query: { category: side.rootQuery },
              }"
              class="column-link"
            >
              Tout voir
              <span aria-hidden="true">↗</span>
            </RouterLink>
          </header>

          <!-- Grande photo éditoriale -->
          <RouterLink
            :to="{
              path: '/produits',
              query: { category: side.rootQuery },
            }"
            class="column-editorial"
          >
            <img
              :src="side.coverImage"
              :alt="side.coverAlt"
              class="column-editorial__image"
              loading="lazy"
              decoding="async"
            />

            <span class="column-editorial__overlay" aria-hidden="true"></span>

            <span class="column-editorial__content">
              <span class="column-editorial__title">
                {{ side.coverTitle }}
              </span>

              <span class="column-editorial__link">
                Explorer l'univers
                <span aria-hidden="true">↗</span>
              </span>
            </span>
          </RouterLink>

          <!-- Groupes de catégories avec miniatures -->
          <div class="groups-list">
            <article
              v-for="group in side.groups"
              :key="group.title"
              class="accessory-group"
            >
              <RouterLink
                :to="{
                  path: '/produits',
                  query: { category: group.query },
                }"
                class="group-image-link"
                :aria-label="`Découvrir ${group.title}`"
              >
                <img
                  :src="group.image"
                  :alt="group.imageAlt"
                  class="group-image"
                  loading="lazy"
                  decoding="async"
                />
              </RouterLink>

              <div class="group-content">
                <RouterLink
                  :to="{
                    path: '/produits',
                    query: { category: group.query },
                  }"
                  class="group-title-link"
                >
                  <span>{{ group.title }}</span>

                  <span class="group-arrow" aria-hidden="true">↗</span>
                </RouterLink>

                <div class="group-chips">
                  <RouterLink
                    v-for="item in group.items"
                    :key="item"
                    :to="{
                      path: '/produits',
                      query: { q: item },
                    }"
                    class="group-chip"
                  >
                    {{ item }}
                  </RouterLink>
                </div>
              </div>
            </article>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.accessories-section {
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

/* Deux colonnes principales */

.accessories-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-surface, #fff);
}

.accessory-column {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 24px;
  padding: clamp(20px, 3vw, 36px);
}

.accessory-column + .accessory-column {
  border-left: 1px solid var(--color-line, #e0dfdb);
}

.accessory-column--femme {
  background: var(--color-page, #fcfbf9);
}

/* En-tête des univers */

.column-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--color-line, #e0dfdb);
}

.column-eyebrow {
  display: block;
  margin-bottom: 5px;
  color: var(--color-text-muted, #767672);
  font-size: 0.62rem;
  font-weight: 600;
  letter-spacing: 0.16em;
}

.column-title {
  margin: 0;
  color: var(--color-ink, #000);
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.25rem, 1.8vw, 1.65rem);
  font-weight: 400;
  line-height: 1.25;
}

.column-link {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  gap: 6px;
  color: var(--color-text-secondary, #3d3d3b);
  font-size: 0.78rem;
  font-weight: 500;
  text-decoration: none;
  white-space: nowrap;
}

.column-link:hover {
  color: var(--color-ink, #000);
}

/* Bannières photographiques */

.column-editorial {
  position: relative;
  display: block;
  aspect-ratio: 2.15 / 1;
  overflow: hidden;
  background: #e8e2da;
  color: #fff;
  text-decoration: none;
}

.column-editorial__image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 600ms ease;
}

.column-editorial:hover .column-editorial__image {
  transform: scale(1.045);
}

.column-editorial__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgb(0 0 0 / 3%) 10%,
    rgb(0 0 0 / 12%) 50%,
    rgb(0 0 0 / 70%) 100%
  );
}

.column-editorial__content {
  position: absolute;
  right: 16px;
  bottom: 16px;
  left: 16px;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.column-editorial__title {
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.15rem, 1.7vw, 1.6rem);
  font-weight: 400;
  line-height: 1.2;
}

.column-editorial__link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 0.72rem;
  letter-spacing: 0.04em;
}

.column-editorial__link span {
  transition: transform 0.2s ease;
}

.column-editorial:hover .column-editorial__link span {
  transform: translate(2px, -2px);
}

/* Liste des catégories */

.groups-list {
  display: flex;
  flex-direction: column;
}

.accessory-group {
  display: grid;
  grid-template-columns: 100px minmax(0, 1fr);
  align-items: start;
  gap: 16px;
  padding-block: 18px;
  border-bottom: 1px solid var(--color-line, #e0dfdb);
}

.accessory-group:first-child {
  padding-top: 0;
}

.accessory-group:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

/* Petite photo de chaque groupe */

.group-image-link {
  display: block;
  aspect-ratio: 4 / 5;
  overflow: hidden;
  background: #eeeae4;
}

.group-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 450ms ease;
}

.group-image-link:hover .group-image {
  transform: scale(1.06);
}

.group-content {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 12px;
}

.group-title-link {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  color: var(--color-ink, #000);
  font-size: 0.9rem;
  font-weight: 600;
  line-height: 1.4;
  text-decoration: none;
}

.group-title-link:hover {
  color: var(--color-accent, #b4761a);
}

.group-arrow {
  flex-shrink: 0;
  color: var(--color-text-muted, #767672);
  transition: transform 0.2s ease;
}

.group-title-link:hover .group-arrow {
  transform: translate(2px, -2px);
}

/* Liens secondaires */

.group-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.group-chip {
  padding: 5px 8px;
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-surface, #fff);
  color: var(--color-text-secondary, #3d3d3b);
  font-size: 0.72rem;
  line-height: 1.35;
  text-decoration: none;
  transition:
    border-color 0.2s ease,
    color 0.2s ease;
}

.group-chip:hover {
  border-color: var(--color-ink, #000);
  color: var(--color-ink, #000);
}

/* Tablettes */

@media (max-width: 1000px) {
  .accessories-columns {
    grid-template-columns: 1fr;
  }

  .accessory-column + .accessory-column {
    border-top: 1px solid var(--color-line, #e0dfdb);
    border-left: none;
  }

  .column-editorial {
    aspect-ratio: 2.6 / 1;
  }

  .accessory-group {
    grid-template-columns: 120px minmax(0, 1fr);
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* Mobiles */

@media (max-width: 600px) {
  .accessories-section {
    padding-block: 36px;
  }

  .section-head {
    gap: 16px;
    margin-bottom: 24px;
  }

  .section-desc {
    font-size: 0.86rem;
  }

  .all-link {
    font-size: 0.78rem;
  }

  .accessory-column {
    gap: 20px;
    padding: 16px;
  }

  .column-header {
    gap: 10px;
  }

  .column-title {
    font-size: 1.3rem;
  }

  .column-link {
    font-size: 0.74rem;
  }

  .column-editorial {
    aspect-ratio: 1.8 / 1;
  }

  .column-editorial__content {
    right: 12px;
    bottom: 12px;
    left: 12px;
  }

  .accessory-group {
    grid-template-columns: 84px minmax(0, 1fr);
    gap: 12px;
    padding-block: 15px;
  }

  .group-title-link {
    font-size: 0.84rem;
  }

  .group-chips {
    gap: 6px;
  }

  .group-chip {
    padding: 5px 7px;
    font-size: 0.68rem;
  }
}

/* Accessibilité */

@media (prefers-reduced-motion: reduce) {
  .column-editorial__image,
  .group-image,
  .column-editorial__link span,
  .group-arrow {
    transition: none;
  }
}
</style>
