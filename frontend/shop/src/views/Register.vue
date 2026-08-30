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
  <div class="auth-page">
    <div class="auth-card">
      <h1>Créer un compte</h1>
      <p class="auth-subtitle">Rejoins SHAHIN en quelques secondes.</p>

      <form @submit.prevent="handleSubmit" class="profile-form">
        <div class="profile-field">
          <label>Email</label>
          <input
            v-model="email"
            type="email"
            required
            placeholder="ton@email.com"
          />
        </div>
        <div class="profile-field">
          <label>Mot de passe</label>
          <input
            v-model="password"
            type="password"
            required
            placeholder="8 caractères minimum"
          />
        </div>
        <p v-if="error" class="auth-error">{{ error }}</p>
        <button type="submit" class="primary">Créer mon compte</button>
      </form>

      <p class="auth-switch">
        Déjà un compte ? <router-link to="/login">Se connecter</router-link>
      </p>
    </div>
  </div>
</template>
