<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

import HeroSection from "@/components/home/HeroSection.vue";
import UniverseCircles from "@/components/home/UniverseCircles.vue";
import NewArrivalsSection from "@/components/home/NewArrivalsSection.vue";
import HomeMenFashion from "@/components/home/HomeMenFashion.vue";
import HomeWomenFashion from "@/components/home/HomeWomenFashion.vue";
import HomeBeautyShowcase from "@/components/home/HomeBeautyShowcase.vue";
import HomeKidsShowcase from "@/components/home/HomeKidsShowcase.vue";
import HomeAccessoriesShowcase from "@/components/home/HomeAccessoriesShowcase.vue";
import HomeLifestyleShowcase from "@/components/home/HomeLifestyleShowcase.vue";
import ReassuranceBand from "@/components/home/ReassuranceBand.vue";

import {
  apiMessage,
  fetchCategoryTree,
  fetchHomepage,
} from "@/services/productService";

import type { CategoryNode } from "@/types/catalog";
import type { HomePayload } from "@/types/home";

type ProductCategoryInfo = {
  id?: string | number;
  categoryName?: string | null;
  categoryPath?: string | null;
  category?:
    | string
    | {
        name?: string;
        path?: string;
        slug?: string;
      };
};

const data = ref<HomePayload | null>(null);
const tree = ref<CategoryNode[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

/**
 * Recommandé pour vous : sélection variée de vrais produits du catalogue,
 * en privilégiant un produit par catégorie sans inventer de données.
 */
const recommendedProducts = computed(() => {
  const candidates = [
    ...(data.value?.featured ?? []),
    ...(data.value?.newest ?? []),
    ...(data.value?.bestSellers ?? []),
  ];

  const seenIds = new Set<string>();
  const seenCategories = new Set<string>();
  const result: typeof candidates = [];

  for (const product of candidates) {
    const meta = product as unknown as ProductCategoryInfo;
    const id = String(meta.id ?? "");

    if (id && seenIds.has(id)) continue;
    if (id) seenIds.add(id);

    const categoryValue =
      meta.categoryName ??
      meta.categoryPath ??
      (typeof meta.category === "string"
        ? meta.category
        : (meta.category?.path ?? meta.category?.slug ?? meta.category?.name));

    const categoryKey = categoryValue?.trim().toLocaleLowerCase("fr-FR");

    if (categoryKey && seenCategories.has(categoryKey)) continue;
    if (categoryKey) seenCategories.add(categoryKey);

    result.push(product);
    if (result.length >= 12) break;
  }

  // Si le filtrage par catégorie a trop réduit la liste, compléter avec les autres candidats uniques
  if (result.length < 6) {
    for (const product of candidates) {
      const id = String(product.id ?? "");
      if (id && !seenIds.has(id)) {
        seenIds.add(id);
        result.push(product);
        if (result.length >= 12) break;
      }
    }
  }

  return result;
});

async function loadHomepage(): Promise<void> {
  loading.value = true;
  error.value = null;

  try {
    const [homeData, treeData] = await Promise.all([
      fetchHomepage(12),
      fetchCategoryTree().catch(() => [] as CategoryNode[]),
    ]);

    data.value = homeData;
    tree.value = treeData;
  } catch (e: unknown) {
    error.value = apiMessage(e, "Impossible de charger la page d'accueil.");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadHomepage();
});
</script>

<template>
  <main class="home">
    <!-- 1. Hero principal : grande composition éditoriale avec image et CTA -->
    <HeroSection />

    <!-- 2. Toutes les catégories : cercles compacts et élégants alignés sur une ligne -->
    <UniverseCircles :tree="tree" />

    <!-- 3. Recommandé pour vous : carrousel horizontal avec flèches fonctionnelles -->
    <NewArrivalsSection
      :products="recommendedProducts"
      :loading="loading"
      :error="error"
      title="Recommandé pour vous"
      subtitle="Une sélection soignée pour découvrir les différents univers du catalogue SIDRA."
    />

    <!-- 4. Mode Homme : section éditoriale vestiaire masculin -->
    <HomeMenFashion :tree="tree" />

    <!-- 5. Mode Femme : section distincte avec son propre rythme et catégories -->
    <HomeWomenFashion :tree="tree" />

    <!-- 6. Beauté : parfums, soins visage, soins capillaires, poudres et huiles -->
    <HomeBeautyShowcase :tree="tree" />

    <!-- 7. Enfant : bébé, junior, vêtements, chaussures et accessoires -->
    <HomeKidsShowcase :tree="tree" />

    <!-- 8. Accessoires Homme et Femme : deux colonnes équilibrées côte à côte sur écran large -->
    <HomeAccessoriesShowcase :tree="tree" />

    <!-- 9. Maison et Électronique : univers lifestyle et high-tech -->
    <HomeLifestyleShowcase :tree="tree" />

    <!-- 10. Réassurance : engagements applicables livraison, Stripe, retours, service -->
    <ReassuranceBand />
  </main>
</template>

<style scoped>
.home {
  min-height: 100vh;
  overflow: clip;
}
</style>
