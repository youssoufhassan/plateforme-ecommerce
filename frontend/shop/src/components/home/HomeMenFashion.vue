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

const rootNode = computed(() => findNode(props.tree, "homme"));

const vetementsNode = computed(
  () =>
    findNode(props.tree, "homme/vetements") ||
    findNode(props.tree, "vetements"),
);

const chaussuresNode = computed(
  () =>
    findNode(props.tree, "homme/chaussures") ||
    findNode(props.tree, "chaussures"),
);

const defaultVetements = [
  { name: "Manteaux & Blousons", slug: "homme/manteaux" },
  { name: "Chemises", slug: "homme/chemises" },
  { name: "Pulls & Mailles", slug: "homme/mailles" },
  { name: "Pantalons & Chinos", slug: "homme/pantalons" },
  { name: "Costumes & Vestes", slug: "homme/costumes" },
  { name: "T-shirts & Polos", slug: "homme/polos" },
];

const defaultChaussures = [
  { name: "Derbies & Richelieus", slug: "homme/derbies" },
  { name: "Sneakers & Baskets", slug: "homme/sneakers" },
  { name: "Bottines & Boots", slug: "homme/bottines" },
  { name: "Mocassins", slug: "homme/mocassins" },
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
  <section class="men-section" aria-labelledby="men-title">
    <div class="container">
      <header class="section-head">
        <h2 id="men-title" class="section-title">Mode Homme</h2>

        <RouterLink
          :to="{
            path: '/produits',
            query: { category: rootNode?.path || 'homme' },
          }"
          class="all-link"
        >
          Tout voir
        </RouterLink>
      </header>

      <div class="men-layout">
        <article class="men-editorial-card">
          <div class="men-editorial-card__bg" aria-hidden="true">
            <img
              src="https://images.unsplash.com/photo-1617137968427-85924c800a22?auto=format&fit=crop&w=1200&q=85"
              alt=""
              loading="lazy"
              decoding="async"
            />
          </div>

          <div class="men-editorial-card__content">
            <h3 class="card-title">L'allure essentielle</h3>

            <p class="card-text">
              Une sélection rigoureuse de pièces intemporelles, du costume
              sartorial aux essentiels décontractés.
            </p>

            <RouterLink
              :to="{
                path: '/produits',
                query: { category: rootNode?.path || 'homme' },
              }"
              class="btn btn-outline card-btn"
            >
              Voir la sélection
            </RouterLink>
          </div>
        </article>

        <div class="men-pillars">
          <article class="pillar-card">
            <RouterLink
              :to="{
                path: '/produits',
                query: {
                  category: vetementsNode?.path || 'homme/vetements',
                },
              }"
              class="pillar-image"
              aria-label="Découvrir les vêtements homme"
            >
              <img
                src="https://images.unsplash.com/photo-1525507119028-ed4c629a60a3?auto=format&fit=crop&w=1000&q=85"
                alt="Sélection de vêtements et de pièces de mode"
                loading="lazy"
                decoding="async"
              />
              <span class="pillar-image__label">Le vestiaire</span>
            </RouterLink>

            <div class="pillar-head">
              <h3 class="pillar-title">Vêtements</h3>

              <RouterLink
                :to="{
                  path: '/produits',
                  query: {
                    category: vetementsNode?.path || 'homme/vetements',
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
                  category: chaussuresNode?.path || 'homme/chaussures',
                },
              }"
              class="pillar-image"
              aria-label="Découvrir les chaussures homme"
            >
              <img
                src="https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1000&q=85"
                alt="Chaussure de sport dans une composition éditoriale"
                loading="lazy"
                decoding="async"
              />
              <span class="pillar-image__label"> Le choix des chaussures </span>
            </RouterLink>

            <div class="pillar-head">
              <h3 class="pillar-title">Chaussures</h3>

              <RouterLink
                :to="{
                  path: '/produits',
                  query: {
                    category: chaussuresNode?.path || 'homme/chaussures',
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
      </div>
    </div>
  </section>
</template>

<style scoped>
.men-section {
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

.men-layout {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.4fr);
  gap: clamp(20px, 3vw, 36px);
  align-items: stretch;
}

.men-editorial-card {
  position: relative;
  display: flex;
  min-height: 570px;
  flex-direction: column;
  justify-content: flex-end;
  overflow: hidden;
  background: #242321;
  color: #fff;
}

.men-editorial-card__bg {
  position: absolute;
  inset: 0;
}

.men-editorial-card__bg img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 700ms ease;
}

.men-editorial-card:hover .men-editorial-card__bg img {
  transform: scale(1.04);
}

.men-editorial-card__bg::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgb(0 0 0 / 5%) 5%,
    rgb(0 0 0 / 15%) 38%,
    rgb(0 0 0 / 82%) 100%
  );
  content: "";
}

.men-editorial-card__content {
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

.btn-outline {
  border: 1px solid rgb(255 255 255 / 75%);
  color: #fff;
}

.btn-outline:hover {
  border-color: #fff;
  background: #fff;
  color: #111;
}

.men-pillars {
  display: grid;
  grid-template-rows: repeat(2, minmax(0, 1fr));
  gap: 22px;
}

.pillar-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 18px;
  padding: clamp(16px, 2vw, 24px);
  border: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-page, #f7f6f3);
}

.pillar-image {
  position: relative;
  display: block;
  height: clamp(150px, 17vw, 205px);
  overflow: hidden;
  background: #e6e3de;
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
  background: var(--color-surface, #fff);
  color: var(--color-text-secondary, #3d3d3b);
  font-size: 0.78rem;
  line-height: 1.35;
  text-decoration: none;
  transition:
    border-color 0.2s ease,
    color 0.2s ease;
}

.chip-link:hover {
  border-color: var(--color-ink, #000);
  color: var(--color-ink, #000);
}

@media (max-width: 1000px) {
  .men-layout {
    grid-template-columns: 1fr;
  }

  .men-editorial-card {
    min-height: 440px;
  }

  .men-pillars {
    grid-template-rows: auto;
  }

  .pillar-image {
    height: clamp(180px, 35vw, 280px);
  }
}

@media (max-width: 767px) {
  .men-section {
    padding-block: 36px;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 24px;
  }

  .all-link {
    font-size: 0.78rem;
  }

  .men-editorial-card {
    min-height: 380px;
  }

  .men-editorial-card__content {
    padding: 24px;
  }

  .card-title {
    font-size: 2rem;
  }

  .men-pillars {
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
  .men-editorial-card__bg img,
  .pillar-image img {
    transition: none;
  }
}
</style>
