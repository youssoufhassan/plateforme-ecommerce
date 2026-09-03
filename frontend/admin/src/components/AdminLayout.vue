<script setup lang="ts">
import { useAuthStore } from "../stores/authStore";
import { useRouter } from "vue-router";

const authStore = useAuthStore();
const router = useRouter();

function handleLogout() {
  authStore.logout();
  router.push("/login");
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-logo">SHAHIN <span>Admin</span></div>
      <nav class="admin-nav">
        <router-link to="/">Tableau de bord</router-link>
        <router-link to="/products">Produits</router-link>
        <router-link to="/categories">Catégories</router-link>
        <router-link to="/orders">Commandes</router-link>
        <router-link to="/customers">Clients</router-link>
      </nav>
      <button class="admin-logout" @click="handleLogout">Déconnexion</button>
    </aside>
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>
