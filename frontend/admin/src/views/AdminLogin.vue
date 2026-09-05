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

  console.log("EMAIL:", email.value);
  console.log("PASSWORD:", password.value);

  try {
    await authStore.login(email.value, password.value);
    console.log("LOGIN OK");
    router.push("/");
  } catch (e: any) {
    console.error("LOGIN FAILED");
    console.error("STATUS:", e.response?.status);
    console.error("DATA:", e.response?.data);
    console.error("URL:", e.config?.url);
    console.error("METHOD:", e.config?.method);
    console.error("REQUEST DATA:", e.config?.data);

    error.value = `Erreur ${e.response?.status ?? "réseau"}`;
  }
}
</script>

<template>
  <div
    style="
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: var(--bg);
    "
  >
    <form
      @submit.prevent="handleSubmit"
      class="admin-form"
      style="
        background: white;
        padding: 2rem;
        border-radius: 8px;
        border: 1px solid var(--border);
      "
    >
      <h2 style="margin: 0 0 1rem">SHAHIN Admin</h2>
      <div>
        <label>Email</label>
        <input v-model="email" type="email" required />
      </div>
      <div>
        <label>Mot de passe</label>
        <input v-model="password" type="password" required />
      </div>
      <p v-if="error" style="color: #b3261e; font-size: 0.85rem">{{ error }}</p>
      <button type="submit" class="btn btn-primary">Se connecter</button>
    </form>
  </div>
</template>
