<script setup lang="ts">
import { RouterView } from "vue-router";

import SiteHeader from "@/components/navigation/SiteHeader.vue";
</script>

<template>
  <div class="store-layout">
    <SiteHeader />

    <main class="store-layout__main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.store-layout {
  min-height: 100vh;
}

/*
 * Desktop :
 * le header fait exactement 82px.
 */
.store-layout__main {
  padding-top: 82px;
}

/*
 * Mobile :
 *
 * 64px = ligne du header
 * 54px = recherche + marge
 *
 * Total ≈ 118px
 */
@media (max-width: 767px) {
  .store-layout__main {
    padding-top: 118px;
  }
}
</style>
