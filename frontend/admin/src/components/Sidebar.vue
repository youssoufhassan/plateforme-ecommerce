<script setup lang="ts">
import { ref } from "vue";
import { useAuthStore } from "../stores/authStore";
import { useRouter } from "vue-router";

const authStore = useAuthStore();
const router = useRouter();
const mobileOpen = ref(false);

const links = [
  { to: "/", label: "Tableau de bord" },
  { to: "/products", label: "Produits" },
  { to: "/categories", label: "Catégories" },
  { to: "/orders", label: "Commandes" },
  { to: "/customers", label: "Clients" },
];

function handleLogout() {
  authStore.logout();
  router.push("/login");
}
</script>

<template>
  <button class="mobile-toggle" @click="mobileOpen = !mobileOpen">☰</button>

  <aside class="admin-sidebar" :class="{ open: mobileOpen }">
    <div class="admin-logo">SHAHIN <span>Admin</span></div>
    <nav class="admin-nav">
      <router-link
        v-for="link in links"
        :key="link.to"
        :to="link.to"
        @click="mobileOpen = false"
      >
        {{ link.label }}
      </router-link>
    </nav>
    <button class="admin-logout" @click="handleLogout">Déconnexion</button>
  </aside>

  <div
    v-if="mobileOpen"
    class="sidebar-overlay"
    @click="mobileOpen = false"
  ></div>
</template>

<style scoped>
.mobile-toggle {
  display: none;
}

@media (max-width: 900px) {
  .mobile-toggle {
    display: block;
    position: fixed;
    top: 1rem;
    left: 1rem;
    z-index: 250;
    background: var(--ink);
    color: white;
    border: none;
    border-radius: 6px;
    width: 40px;
    height: 40px;
    font-size: 1.1rem;
    cursor: pointer;
  }
  .admin-sidebar {
    position: fixed;
    left: -240px;
    top: 0;
    height: 100vh;
    z-index: 240;
    transition: left 0.2s ease;
  }
  .admin-sidebar.open {
    left: 0;
  }
  .sidebar-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: 230;
  }
}
</style>
