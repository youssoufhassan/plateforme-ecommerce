<script setup lang="ts">
import { computed } from "vue";
import { RouterLink } from "vue-router";

import type { CategoryNode } from "@/types/catalog";

const props = defineProps<{
  tree: CategoryNode[];
}>();

interface Circle {
  key: string;
  label: string;
  initial: string;
  to: {
    path: string;
    query: Record<string, string>;
  };
}

function slugify(value: string): string {
  return value
    .toLocaleLowerCase("fr-FR")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "");
}

const defaultUniverses = [
  { slug: "femme", label: "Mode Femme" },
  { slug: "homme", label: "Mode Homme" },
  { slug: "beaute", label: "Beauté" },
  { slug: "enfant", label: "Enfant" },
  { slug: "accessoires", label: "Accessoires" },
  { slug: "maison", label: "Maison" },
  { slug: "electronique", label: "Électronique" },
];

const circles = computed<Circle[]>(() => {
  if (props.tree && props.tree.length > 0) {
    const orderMap: Record<string, number> = {
      femme: 1,
      homme: 2,
      beaute: 3,
      enfant: 4,
      accessoires: 5,
      maison: 6,
      electronique: 7,
    };

    const fromTree: Circle[] = [...props.tree]
      .sort((a, b) => {
        const slugA = slugify(a.slug || a.path || a.name);
        const slugB = slugify(b.slug || b.path || b.name);
        const orderA = orderMap[slugA] ?? 99;
        const orderB = orderMap[slugB] ?? 99;
        if (orderA !== orderB) return orderA - orderB;
        return a.name.localeCompare(b.name, "fr-FR");
      })
      .slice(0, 7)
      .map((node) => ({
        key: String(node.id),
        label: node.name,
        initial: node.name.charAt(0).toUpperCase(),
        to: {
          path: "/produits",
          query: {
            category: node.path || node.slug || slugify(node.name),
          },
        },
      }));

    return [
      ...fromTree,
      {
        key: "newest",
        label: "Nouveautés",
        initial: "N",
        to: {
          path: "/produits",
          query: { sort: "newest" },
        },
      },
    ];
  }

  return [
    ...defaultUniverses.map((item) => ({
      key: item.slug,
      label: item.label,
      initial: item.label.charAt(0).toUpperCase(),
      to: {
        path: "/produits",
        query: { category: item.slug },
      },
    })),
    {
      key: "newest",
      label: "Nouveautés",
      initial: "N",
      to: {
        path: "/produits",
        query: { sort: "newest" },
      },
    },
  ];
});
</script>

<template>
  <section class="universes" aria-labelledby="universes-title">
    <div class="container universes__inner">
      <div class="universes__header">
        <h2 id="universes-title" class="universes__title">
          Toutes les catégories
        </h2>
      </div>

      <nav class="universes__row" aria-label="Toutes les catégories SIDRA">
        <RouterLink
          v-for="circle in circles"
          :key="circle.key"
          :to="circle.to"
          class="universe"
        >
          <span class="universe__disc">
            <span class="universe__initial">
              {{ circle.initial }}
            </span>
          </span>

          <span class="universe__label">
            {{ circle.label }}
          </span>
        </RouterLink>
      </nav>
    </div>
  </section>
</template>

<style scoped>
.universes {
  padding-block: clamp(24px, 3.5vw, 36px);
  border-bottom: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-surface, #ffffff);
}

.universes__inner {
  width: 100%;
}

.universes__header {
  margin-bottom: clamp(16px, 2.5vw, 24px);
}

.universes__title {
  margin: 0;
  color: var(--color-ink, #000);
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.4rem, 2vw, 1.85rem);
  font-weight: 400;
  letter-spacing: -0.025em;
  line-height: 1.2;
}

.universes__row {
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  align-items: start;
  gap: clamp(10px, 1.5vw, 20px);
  width: 100%;
}

.universe {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--color-ink, #000);
  transition: transform 0.2s ease;
}

.universe:hover {
  transform: translateY(-3px);
}

.universe__disc {
  position: relative;
  display: grid;
  place-items: center;
  width: clamp(56px, 5.2vw, 76px);
  height: clamp(56px, 5.2vw, 76px);
  border: 1px solid var(--color-line, #e0dfdb);
  border-radius: 50%;
  background: var(--color-page, #f2f2f1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition:
    background 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.universe__initial {
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(22px, 2.2vw, 28px);
  font-weight: 400;
  line-height: 1;
  color: var(--color-ink, #000);
  transition: color 0.2s ease;
}

.universe:hover .universe__disc {
  background: var(--color-ink, #000);
  border-color: var(--color-ink, #000);
}

.universe:hover .universe__initial {
  color: #ffffff;
}

.universe__label {
  display: block;
  font-family: var(--font-body, "Jost", sans-serif);
  font-size: clamp(0.75rem, 0.95vw, 0.88rem);
  font-weight: 500;
  text-align: center;
  line-height: 1.3;
  color: var(--color-text-secondary, #3d3d3b);
  max-width: 100px;
  overflow-wrap: break-word;
}

.universe:hover .universe__label {
  color: var(--color-ink, #000);
}

@media (max-width: 900px) {
  .universes__row {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 20px 14px;
  }

  .universe__disc {
    width: 62px;
    height: 62px;
  }
}

@media (max-width: 480px) {
  .universes__row {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px 8px;
  }

  .universe__disc {
    width: 50px;
    height: 50px;
  }

  .universe__initial {
    font-size: 19px;
  }

  .universe__label {
    font-size: 0.72rem;
  }
}
</style>
