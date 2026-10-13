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
  to: { path: string; query: Record<string, string> };
}

/**
 * Une entrée par univers, plus les nouveautés.
 * Les branches vides sont écartées : un cercle qui mène nulle part
 * déçoit plus qu'il n'attire.
 */
const circles = computed<Circle[]>(() => {
  const fromTree = props.tree
    .filter((node) => node.productCount > 0)
    .map((node) => ({
      key: node.id,
      label: node.name,
      to: { path: "/produits", query: { category: node.path } },
    }));

  return [
    ...fromTree,
    {
      key: "newest",
      label: "Nouveautés",
      to: { path: "/produits", query: { sort: "newest" } },
    },
  ];
});
</script>

<template>
  <nav v-if="circles.length > 1" class="universes" aria-label="Univers">
    <div class="container universes__row">
      <RouterLink
        v-for="circle in circles"
        :key="circle.key"
        :to="circle.to"
        class="universe"
      >
        <span class="universe__disc">
          <span class="universe__initial">{{ circle.label.charAt(0) }}</span>
        </span>

        <span class="universe__label">{{ circle.label }}</span>
      </RouterLink>
    </div>
  </nav>
</template>

<style scoped>
.universes {
  padding-block: var(--space-10);
  border-bottom: 1px solid var(--color-line);
}

.universes__row {
  display: flex;
  justify-content: center;
  gap: var(--space-8);
  overflow-x: auto;
  scrollbar-width: none;
}

.universes__row::-webkit-scrollbar {
  display: none;
}

.universe {
  display: grid;
  justify-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
  text-decoration: none;
}

.universe__disc {
  display: grid;
  place-items: center;

  width: 96px;
  height: 96px;

  border-radius: var(--radius-pill);
  background: var(--color-plate);

  transition:
    background var(--transition-base),
    transform var(--transition-base);
}

.universe:hover .universe__disc {
  background: var(--color-ink);
  transform: translateY(-3px);
}

.universe__initial {
  font-family: var(--font-display);
  font-size: 34px;
  color: var(--color-text);
  transition: color var(--transition-base);
}

.universe:hover .universe__initial {
  color: var(--color-text-inverse);
}

.universe__label {
  color: var(--color-text);
  font-size: var(--text-sm);
}

@media (max-width: 767px) {
  .universes {
    padding-block: var(--space-6);
  }

  .universes__row {
    justify-content: flex-start;
    gap: var(--space-5);
    margin-right: calc(var(--container-padding) * -1);
    padding-right: var(--container-padding);
  }

  .universe__disc {
    width: 72px;
    height: 72px;
  }

  .universe__initial {
    font-size: 26px;
  }

  .universe__label {
    font-size: var(--text-xs);
  }
}
</style>
