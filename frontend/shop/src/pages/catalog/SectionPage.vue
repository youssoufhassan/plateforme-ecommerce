<script setup lang="ts">
import { ref, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";

import ProductCard from "@/components/product/ProductCard.vue";
import { apiMessage, fetchSection } from "@/services/productService";
import type { HomeSection } from "@/types/home";

const route = useRoute();

const section = ref<HomeSection | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

async function loadSection(slug: string): Promise<void> {
  loading.value = true;
  error.value = null;

  try {
    section.value = await fetchSection(slug);
  } catch (e: unknown) {
    error.value = apiMessage(e, "Cette sélection est introuvable.");
    section.value = null;
  } finally {
    loading.value = false;
  }
}

watch(
  () => route.params.slug,
  (slug) => {
    if (typeof slug === "string") loadSection(slug);
  },
  { immediate: true },
);
</script>

<template>
  <main class="section-page">
    <div class="container">
      <nav class="section-page__breadcrumb" aria-label="Fil d'Ariane">
        <RouterLink to="/">Accueil</RouterLink>
        <span aria-hidden="true">/</span>
        <span>{{ section?.title ?? "Sélection" }}</span>
      </nav>

      <!-- Chargement -->
      <template v-if="loading">
        <header class="section-page__intro">
          <div
            class="section-skeleton__line section-skeleton__line--title"
          ></div>
          <div
            class="section-skeleton__line section-skeleton__line--subtitle"
          ></div>
        </header>

        <div class="section-page__grid" aria-busy="true">
          <div v-for="n in 8" :key="`skeleton-${n}`">
            <div class="section-skeleton__visual"></div>
            <div
              class="section-skeleton__line section-skeleton__line--short"
            ></div>
            <div class="section-skeleton__line"></div>
          </div>
        </div>
      </template>

      <!-- Erreur -->
      <div
        v-else-if="error || !section"
        class="section-page__state"
        role="alert"
      >
        <p class="section-page__state-title">{{ error }}</p>
        <RouterLink to="/produits" class="section-page__state-action">
          Voir tout le catalogue
        </RouterLink>
      </div>

      <template v-else>
        <header class="section-page__intro">
          <h1 class="section-page__title">{{ section.title }}</h1>

          <p v-if="section.subtitle" class="section-page__subtitle">
            {{ section.subtitle }}
          </p>

          <p class="section-page__count">
            {{ section.totalCount }} produit{{
              section.totalCount > 1 ? "s" : ""
            }}
          </p>
        </header>

        <!-- Une section peut avoir été vidée de ses produits disponibles -->
        <div v-if="section.products.length === 0" class="section-page__state">
          <p class="section-page__state-title">
            Cette sélection est momentanément indisponible.
          </p>
          <RouterLink to="/produits" class="section-page__state-action">
            Voir tout le catalogue
          </RouterLink>
        </div>

        <div v-else class="section-page__grid">
          <ProductCard
            v-for="(product, index) in section.products"
            :key="product.id"
            :product="product"
            :eager="index < 8"
          />
        </div>
      </template>
    </div>
  </main>
</template>

<style scoped>
.section-page {
  width: 100%;
  padding-bottom: 72px;
}

.section-page__breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 22px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.section-page__breadcrumb a {
  color: inherit;
  text-decoration: none;
}

.section-page__breadcrumb a:hover {
  color: var(--color-text);
}

.section-page__intro {
  padding-block: 18px 30px;
}

.section-page__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(30px, 3.6vw, 46px);
  font-weight: 400;
  line-height: 1.02;
  letter-spacing: -0.035em;
  color: var(--color-text);
}

.section-page__subtitle {
  max-width: 56ch;
  margin: 10px 0 0;
  color: var(--color-text-muted);
  font-size: 14px;
  line-height: 1.5;
}

.section-page__count {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.section-page__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 34px 18px;
}

/* =========================================================
   ÉTATS
   ========================================================= */

.section-page__state {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: 16px;
  min-height: 320px;
  text-align: center;
}

.section-page__state-title {
  margin: 0;
  color: var(--color-text);
  font-size: 15px;
}

.section-page__state-action {
  padding: 11px 22px;
  border: 1px solid var(--color-text);
  color: var(--color-text);
  font-size: 12px;
  text-decoration: none;
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
}

.section-page__state-action:hover {
  background: var(--color-text);
  color: var(--color-white);
}

/* =========================================================
   SQUELETTE
   ========================================================= */

.section-skeleton__visual {
  aspect-ratio: 1 / 1.05;
  background: #efedea;
}

.section-skeleton__line {
  height: 9px;
  margin-top: 9px;
  background: #efedea;
}

.section-skeleton__line--short {
  width: 38%;
  height: 7px;
  margin-top: 13px;
}

.section-skeleton__line--title {
  width: 45%;
  height: 34px;
  margin-top: 0;
}

.section-skeleton__line--subtitle {
  width: 62%;
  height: 12px;
  margin-top: 14px;
}

@media (prefers-reduced-motion: no-preference) {
  .section-skeleton__visual,
  .section-skeleton__line {
    animation: section-pulse 1.5s ease-in-out infinite;
  }
}

@keyframes section-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.55;
  }
}

/* =========================================================
   RESPONSIVE
   ========================================================= */

@media (min-width: 768px) {
  .section-page__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 38px 20px;
  }
}

@media (min-width: 1001px) {
  .section-page__grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (min-width: 1440px) {
  .section-page__grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .section-page__intro {
    padding-block: 14px 24px;
  }

  .section-page__subtitle {
    font-size: 13px;
  }

  .section-page__grid {
    gap: 28px 14px;
  }
}
</style>
