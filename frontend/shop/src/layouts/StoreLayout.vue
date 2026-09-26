<script setup lang="ts">
import { onMounted } from "vue";
import { RouterView } from "vue-router";

import SiteHeader from "@/components/navigation/SiteHeader.vue";
import Footer from "@/components/navigation/Footer.vue";
import CartPanel from "@/components/cart/CartPanel.vue";
import { useCartStore } from "@/stores/cartStore";

const cartStore = useCartStore();

// Le panier est chargé une seule fois pour toute l'application
onMounted(() => cartStore.load());
</script>

<template>
  <div class="store-layout">
    <SiteHeader />

    <!-- Les pages fournissent leur propre <main> : ici, un simple conteneur -->
    <div class="store-layout__content">
      <RouterView />
    </div>

    <Footer />

    <!-- Panneau de confirmation après un ajout au panier -->
    <CartPanel />
  </div>
</template>

<style scoped>
.store-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.store-layout__content {
  /* Pousse le pied de page en bas même sur une page courte */
  flex: 1;
}
</style>
