<script setup lang="ts">
import { useAuthStore } from "./stores/authStore";
const authStore = useAuthStore();
</script>

<template>
  <nav>
    <router-link to="/">Produits</router-link>
    <span v-if="authStore.isLoggedIn()">
      <button @click="authStore.logout()">Déconnexion</button>
    </span>
    <span v-else>
      <router-link to="/login">Connexion</router-link>
      <router-link to="/register">Inscription</router-link>
    </span>
  </nav>
  <router-view />
</template>
