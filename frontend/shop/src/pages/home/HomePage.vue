<script setup lang="ts">
import HeroSection from "@/components/home/HeroSection.vue";
import NewArrivalsSection from "@/components/home/NewArrivalsSection.vue";
import EditorialSelectionSection from "@/components/home/EditorialSelectionSection.vue";
import BrandsSection from "@/components/home/BrandsSection.vue";
import Footer from "@/components/navigation/Footer.vue";
</script>

<template>
  <main class="home-page">
    <HeroSection />

    <NewArrivalsSection />

    <EditorialSelectionSection />

    <BrandsSection />
    <Footer />
  </main>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
}
</style>
