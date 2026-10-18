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

const rootNode = computed(() => findNode(props.tree, "femme"));

const vetementsNode = computed(
  () =>
    findNode(props.tree, "femme/vetements") ||
    findNode(props.tree, "vetements"),
);

const chaussuresNode = computed(
  () =>
    findNode(props.tree, "femme/chaussures") ||
    findNode(props.tree, "chaussures"),
);

const defaultVetements = [
  { name: "Robes & Combinaisons", slug: "femme/robes" },
  { name: "Manteaux & Trenchs", slug: "femme/manteaux" },
  { name: "Mailles & Cachemires", slug: "femme/mailles" },
  { name: "Chemisiers & Blouses", slug: "femme/chemisiers" },
  { name: "Pantalons & Jupes", slug: "femme/pantalons" },
  { name: "Vestes & Blazers", slug: "femme/blazers" },
];

const defaultChaussures = [
  { name: "Escarpins & Talons", slug: "femme/escarpins" },
  { name: "Bottes & Bottines", slug: "femme/bottes" },
  { name: "Mocassins & Ballerines", slug: "femme/mocassins" },
  { name: "Sandales & Mules", slug: "femme/sandales" },
  { name: "Sneakers Contemporaines", slug: "femme/sneakers" },
];

const vetementsItems = computed(() => {
  if (vetementsNode.value?.children?.length) {
    return vetementsNode.value.children.map((category) => ({
      name: category.name,
      slug: category.path || category.slug || slugify(category.name),
    }));
  }

  return defaultVetements;
});

const chaussuresItems = computed(() => {
  if (chaussuresNode.value?.children?.length) {
    return chaussuresNode.value.children.map((category) => ({
      name: category.name,
      slug: category.path || category.slug || slugify(category.name),
    }));
  }

  return defaultChaussures;
});
</script>

<template>
  <section class="women-section" aria-labelledby="women-title">
    <div class="container">
      <header class="section-head">
        <h2 id="women-title" class="section-title">Mode Femme</h2>

        <RouterLink
          :to="{
            path: '/produits',
            query: { category: rootNode?.path || 'femme' },
          }"
          class="all-link"
        >
          Tout voir
        </RouterLink>
      </header>

      <div class="women-layout">
        <div class="women-pillars">
          <article class="pillar-card">
            <RouterLink
              :to="{
                path: '/produits',
                query: {
                  category: vetementsNode?.path || 'femme/vetements',
                },
              }"
              class="pillar-image"
              aria-label="Découvrir les vêtements femme"
            >
              <img
                src="https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1000&q=85"
                alt="Sélection de vêtements et de pièces de mode féminine"
                loading="lazy"
                decoding="async"
              />

              <span class="pillar-image__label"> Le vestiaire féminin </span>
            </RouterLink>

            <div class="pillar-head">
              <h3 class="pillar-title">Vêtements</h3>

              <RouterLink
                :to="{
                  path: '/produits',
                  query: {
                    category: vetementsNode?.path || 'femme/vetements',
                  },
                }"
                class="pillar-link"
              >
                Tout voir
              </RouterLink>
            </div>

            <div class="category-chips">
              <RouterLink
                v-for="item in vetementsItems"
                :key="item.name"
                :to="{
                  path: '/produits',
                  query: { category: item.slug },
                }"
                class="chip-link"
              >
                {{ item.name }}
              </RouterLink>
            </div>
          </article>

          <article class="pillar-card">
            <RouterLink
              :to="{
                path: '/produits',
                query: {
                  category: chaussuresNode?.path || 'femme/chaussures',
                },
              }"
              class="pillar-image pillar-image--shoes"
              aria-label="Découvrir les chaussures femme"
            >
              <img
                src="https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=1000&q=85"
                alt="Collection de chaussures féminines élégantes"
                loading="lazy"
                decoding="async"
              />

              <span class="pillar-image__label"> Chaussures </span>
            </RouterLink>

            <div class="pillar-head">
              <h3 class="pillar-title">Chaussures</h3>

              <RouterLink
                :to="{
                  path: '/produits',
                  query: {
                    category: chaussuresNode?.path || 'femme/chaussures',
                  },
                }"
                class="pillar-link"
              >
                Tout voir
              </RouterLink>
            </div>

            <div class="category-chips">
              <RouterLink
                v-for="item in chaussuresItems"
                :key="item.name"
                :to="{
                  path: '/produits',
                  query: { category: item.slug },
                }"
                class="chip-link"
              >
                {{ item.name }}
              </RouterLink>
            </div>
          </article>
        </div>

        <article class="women-editorial-card">
          <div class="women-editorial-card__bg" aria-hidden="true">
            <img
              src="https://images.unsplash.com/photo-1539109136881-3be0616acf4b?auto=format&fit=crop&w=1200&q=85"
              alt=""
              loading="lazy"
              decoding="async"
            />
          </div>

          <div class="women-editorial-card__content">
            <h3 class="card-title">La grâce en mouvement</h3>

            <p class="card-text">
              Robes fluides, mailles enveloppantes et souliers raffinés : l'art
              de conjuguer allure moderne et féminité affirmée.
            </p>

            <RouterLink
              :to="{
                path: '/produits',
                query: { category: rootNode?.path || 'femme' },
              }"
              class="btn btn-primary card-btn"
            >
              Voir la sélection
            </RouterLink>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.women-section {
  padding-block: clamp(40px, 6vw, 76px);
  border-top: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-page, #f8f7f5);
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: clamp(28px, 4vw, 44px);
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
  display: inline-flex;
  align-items: center;
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

.women-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(0, 1fr);
  gap: clamp(20px, 3vw, 36px);
  align-items: stretch;
}

.women-pillars {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.pillar-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 18px;
  padding: clamp(16px, 2vw, 24px);
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-surface, #fff);
}

.pillar-image {
  position: relative;
  display: block;
  height: clamp(150px, 15vw, 200px);
  overflow: hidden;
  background: #e9e4df;
  color: #fff;
  text-decoration: none;
}

.pillar-image::after {
  position: absolute;
  inset: 35% 0 0;
  background: linear-gradient(180deg, transparent, rgb(0 0 0 / 55%));
  content: "";
}

.pillar-image img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 500ms ease;
}

.pillar-image:hover img {
  transform: scale(1.05);
}

.pillar-image__label {
  position: absolute;
  right: 16px;
  bottom: 14px;
  left: 16px;
  z-index: 1;
  font-size: 0.78rem;
  font-weight: 500;
  letter-spacing: 0.04em;
}

.pillar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--color-line, #e0dfdb);
}

.pillar-title {
  margin: 0;
  color: var(--color-ink, #000);
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: 1.5rem;
  font-weight: 400;
}

.pillar-link {
  display: inline-flex;
  align-items: center;
  color: var(--color-text-secondary, #3d3d3b);
  font-size: 0.78rem;
  font-weight: 500;
  text-decoration: none;
  white-space: nowrap;
}

.pillar-link:hover {
  color: var(--color-ink, #000);
}

.category-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
}

.chip-link {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-page, #f8f7f5);
  color: var(--color-text-secondary, #3d3d3b);
  font-size: 0.78rem;
  line-height: 1.35;
  text-decoration: none;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    background 0.2s ease;
}

.chip-link:hover {
  border-color: var(--color-ink, #000);
  background: #fff;
  color: var(--color-ink, #000);
}

.women-editorial-card {
  position: relative;
  display: flex;
  min-height: 620px;
  flex-direction: column;
  justify-content: flex-end;
  overflow: hidden;
  background: #332620;
  color: #fff;
}

.women-editorial-card__bg {
  position: absolute;
  inset: 0;
}

.women-editorial-card__bg img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 700ms ease;
}

.women-editorial-card:hover .women-editorial-card__bg img {
  transform: scale(1.04);
}

.women-editorial-card__bg::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgb(0 0 0 / 5%) 4%,
    rgb(0 0 0 / 14%) 38%,
    rgb(0 0 0 / 80%) 100%
  );
  content: "";
}

.women-editorial-card__content {
  position: relative;
  z-index: 1;
  padding: clamp(24px, 3vw, 40px);
}

.card-title {
  margin: 0 0 12px;
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.8rem, 2.8vw, 2.6rem);
  font-weight: 400;
  line-height: 1.12;
}

.card-text {
  max-width: 38ch;
  margin: 0 0 24px;
  color: rgb(255 255 255 / 88%);
  font-size: 0.88rem;
  line-height: 1.7;
}

.btn {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  justify-content: center;
  padding: 0 18px;
  font-size: 0.76rem;
  font-weight: 500;
  text-decoration: none;
  transition:
    background 0.2s ease,
    color 0.2s ease;
}

.btn-primary {
  border: 1px solid #fff;
  background: #fff;
  color: #111;
}

.btn-primary:hover {
  border-color: #e5c28b;
  background: #e5c28b;
  color: #111;
}

@media (max-width: 1000px) {
  .women-layout {
    grid-template-columns: 1fr;
  }

  .women-editorial-card {
    min-height: 460px;
    order: -1;
  }

  .pillar-image {
    height: clamp(180px, 35vw, 280px);
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 767px) {
  .women-section {
    padding-block: 36px;
  }

  .section-head {
    gap: 16px;
    margin-bottom: 24px;
  }

  .all-link {
    font-size: 0.78rem;
  }

  .women-editorial-card {
    min-height: 390px;
  }

  .women-editorial-card__content {
    padding: 24px;
  }

  .card-title {
    font-size: 2rem;
  }

  .women-pillars {
    gap: 16px;
  }

  .pillar-card {
    gap: 16px;
    padding: 14px;
  }

  .pillar-image {
    height: 190px;
  }

  .pillar-title {
    font-size: 1.35rem;
  }

  .category-chips {
    gap: 8px;
  }

  .chip-link {
    padding: 8px 10px;
    font-size: 0.75rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .women-editorial-card__bg img,
  .pillar-image img {
    transition: none;
  }
}
</style>
