<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

import HeroStories from "@/components/home/HeroStories.vue";
import UniverseCircles from "@/components/home/UniverseCircles.vue";
import NewArrivalsSection from "@/components/home/NewArrivalsSection.vue";
import EditorialSplit from "@/components/home/EditorialSplit.vue";
import EditorialSelectionSection from "@/components/home/EditorialSelectionSection.vue";
import BrandsBand from "@/components/home/BrandsBand.vue";
import ReassuranceBand from "@/components/home/ReassuranceBand.vue";
import {
  apiMessage,
  fetchCategoryTree,
  fetchHomepage,
} from "@/services/productService";
import type { CategoryNode } from "@/types/catalog";
import type { HomePayload } from "@/types/home";

const data = ref<HomePayload | null>(null);
const tree = ref<CategoryNode[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const sections = computed(() => data.value?.sections ?? []);

/** La première section reçoit le format en deux colonnes. */
const featuredSection = computed(() => sections.value[0] ?? null);

/** Les suivantes restent en carrousel : alterner évite la monotonie. */
const otherSections = computed(() => sections.value.slice(1));

/** Un seul appel pour l'essentiel : évite les décalages successifs. */
async function loadHomepage(): Promise<void> {
  loading.value = true;
  error.value = null;

  try {
    data.value = await fetchHomepage(8);
  } catch (e: unknown) {
    error.value = apiMessage(e, "Impossible de charger la page d'accueil.");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadHomepage();

  fetchCategoryTree()
    .then((nodes) => (tree.value = nodes))
    .catch(() => {
      // Sans arbre, les cercles d'univers disparaissent
    });
});
</script>

<template>
  <main class="home">
    <!-- 1. Sélection du moment, en défilement automatique -->
    <HeroStories :products="data?.featured ?? []" :loading="loading" />

    <!-- 2. Univers -->
    <UniverseCircles :tree="tree" />

    <!-- 3. Nouveautés -->
    <NewArrivalsSection
      :products="data?.newest ?? []"
      :loading="loading"
      :error="error"
    />

    <!-- 4. Première section, en deux colonnes -->
    <EditorialSplit v-if="featuredSection" :section="featuredSection" />

    <!-- 5. Maisons -->
    <BrandsBand :brands="data?.topBrands ?? []" />

    <!-- 6. Meilleures ventes, masquées tant qu'il n'y a pas de ventes -->
    <NewArrivalsSection
      v-if="data?.bestSellers?.length"
      :products="data.bestSellers"
      title="Les plus vendus"
      subtitle="Ce que nos clients choisissent le plus"
    />

    <!-- 7. Sections suivantes, en carrousel -->
    <EditorialSelectionSection
      v-for="section in otherSections"
      :key="section.id"
      :section="section"
    />

    <!-- 8. Engagements -->
    <ReassuranceBand />
  </main>
</template>

<style scoped>
.home {
  min-height: 100vh;
}
</style>
