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
    await authStore.register(email.value, password.value);
    router.push("/");
  } catch (e) {
    error.value = "Cet email est peut-être déjà utilisé";
  }
}
</script>

<template>
  <div>
    <h1>Créer un compte</h1>
    <form @submit.prevent="handleSubmit">
      <input v-model="email" type="email" placeholder="Email" required />
      <input
        v-model="password"
        type="password"
        placeholder="Mot de passe (8 caractères min.)"
        required
      />
      <button type="submit">S'inscrire</button>
    </form>
    <p v-if="error">{{ error }}</p>
    <router-link to="/login">Déjà un compte ?</router-link>
  </div>
</template>
