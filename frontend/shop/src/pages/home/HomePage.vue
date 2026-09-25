<script setup lang="ts">
import { onMounted, ref } from "vue";

import HeroSection from "@/components/home/HeroSection.vue";
import NewArrivalsSection from "@/components/home/NewArrivalsSection.vue";
import EditorialSelectionSection from "@/components/home/EditorialSelectionSection.vue";
import BrandsSection from "@/components/home/BrandsSection.vue";
import Footer from "@/components/navigation/Footer.vue";
import { apiMessage, fetchHomepage } from "@/services/productService";
import type { HomePayload } from "@/types/home";

const data = ref<HomePayload | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

/** Un seul appel pour toute la page : évite les décalages successifs. */
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

onMounted(loadHomepage);
</script>

<template>
  <main class="home-page">
    <HeroSection />

    <NewArrivalsSection
      :products="data?.newest ?? []"
      :loading="loading"
      :error="error"
    />

    <!-- Sections configurées depuis le back-office -->
    <EditorialSelectionSection
      v-for="section in data?.sections ?? []"
      :key="section.id"
      :section="section"
    />

    <BrandsSection :products="data?.newest ?? []" />
  </main>

  <Footer />
</template>

<style scoped>
.home-page {
  min-height: 100vh;
}
</style>
