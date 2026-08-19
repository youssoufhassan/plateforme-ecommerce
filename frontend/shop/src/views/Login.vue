<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/authStore";

const email = ref("");
const password = ref("");
const error = ref("");
const authStore = useAuthStore();
const router = useRouter();

async function handleSubmit() {
  error.value = "";
  try {
    await authStore.login(email.value, password.value);
    router.push("/");
  } catch (e) {
    error.value = "Email ou mot de passe incorrect";
  }
}
</script>

<template>
  <div>
    <h1>Connexion</h1>
    <form @submit.prevent="handleSubmit">
      <input v-model="email" type="email" placeholder="Email" required />
      <input
        v-model="password"
        type="password"
        placeholder="Mot de passe"
        required
      />
      <button type="submit">Se connecter</button>
    </form>
    <p v-if="error">{{ error }}</p>
    <router-link to="/register">Pas encore de compte ?</router-link>
  </div>
</template>
