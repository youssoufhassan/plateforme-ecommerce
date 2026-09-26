<script setup lang="ts">
import { onMounted, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";

import { apiMessage } from "@/services/productService";
import { verifyEmail } from "@/services/authService";
import { useAuthStore } from "@/stores/authStore";
const route = useRoute();
const authStore = useAuthStore();

const status = ref<"pending" | "success" | "error">("pending");
const message = ref("");

onMounted(async () => {
  const token = route.query.token as string;

  if (!token) {
    status.value = "error";
    message.value = "Ce lien est incomplet.";
    return;
  }

  try {
    const response = await verifyEmail(token);
    message.value = response.message;
    status.value = "success";

    // Le bandeau d'avertissement doit disparaître immédiatement
    if (authStore.isAuthenticated) await authStore.loadProfile();
  } catch (e: unknown) {
    message.value = apiMessage(e, "Ce lien n'est plus valable.");
    status.value = "error";
  }
});
</script>

<template>
  <main class="verify">
    <div class="verify__panel">
      <p v-if="status === 'pending'" class="verify__state">
        Vérification en cours
      </p>

      <template v-else-if="status === 'success'">
        <h1 class="verify__title">Adresse confirmée</h1>
        <p class="verify__text">{{ message }}</p>

        <RouterLink to="/produits" class="verify__action">
          Découvrir le catalogue
        </RouterLink>
      </template>

      <template v-else>
        <h1 class="verify__title">Lien invalide</h1>
        <p class="verify__text">{{ message }}</p>
        <p class="verify__hint">
          Connectez-vous pour demander un nouveau lien de vérification.
        </p>

        <RouterLink to="/connexion" class="verify__action"
          >Se connecter</RouterLink
        >
      </template>
    </div>
  </main>
</template>

<style scoped>
.verify {
  display: grid;
  place-items: center;
  min-height: 55vh;
  padding: 60px var(--container-padding);
}

.verify__panel {
  width: 100%;
  max-width: 420px;
  text-align: center;
}

.verify__state {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
}

.verify__title {
  margin: 0 0 12px;
  font-family: var(--font-display);
  font-size: clamp(24px, 2.8vw, 32px);
  font-weight: 400;
  letter-spacing: -0.03em;
}

.verify__text {
  margin: 0;
  color: var(--color-text);
  font-size: 14px;
  line-height: 1.55;
}

.verify__hint {
  margin: 10px 0 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.verify__action {
  display: inline-grid;
  place-items: center;
  min-width: 220px;
  height: 46px;
  margin-top: 26px;
  background: var(--color-black);
  color: var(--color-white);
  font-size: 12px;
  text-decoration: none;
}

.verify__action:hover {
  opacity: 0.85;
}
</style>
