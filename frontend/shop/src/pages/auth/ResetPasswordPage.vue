<script setup lang="ts">
import { computed, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";

import { apiMessage } from "@/services/productService";
import { resetPassword } from "@/services/authService";
const route = useRoute();
const router = useRouter();

const token = computed(() => (route.query.token as string) || "");

const password = ref("");
const confirmation = ref("");
const submitting = ref(false);
const done = ref(false);
const error = ref<string | null>(null);

async function handleSubmit(): Promise<void> {
  error.value = null;

  if (password.value !== confirmation.value) {
    error.value = "Les deux mots de passe ne correspondent pas.";
    return;
  }

  submitting.value = true;

  try {
    await resetPassword(token.value, password.value);
    done.value = true;

    setTimeout(() => router.push("/connexion"), 2500);
  } catch (e: unknown) {
    error.value = apiMessage(e, "Le mot de passe n'a pas pu être modifié.");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <main class="auth">
    <div class="auth__panel">
      <h1 class="auth__title">Nouveau mot de passe</h1>

      <!-- Lien incomplet : inutile d'afficher le formulaire -->
      <template v-if="!token">
        <p class="auth__error" role="alert">
          Ce lien est incomplet. Faites une nouvelle demande.
        </p>

        <RouterLink
          to="/mot-de-passe-oublie"
          class="auth__submit auth__submit--link"
        >
          Demander un nouveau lien
        </RouterLink>
      </template>

      <template v-else-if="done">
        <p class="auth__notice" role="status">
          Votre mot de passe a été modifié. Vous allez être redirigé vers la
          connexion.
        </p>
      </template>

      <form v-else class="auth__form" @submit.prevent="handleSubmit">
        <label class="auth__field">
          <span class="auth__label">Nouveau mot de passe</span>
          <input
            v-model="password"
            type="password"
            autocomplete="new-password"
            minlength="8"
            required
          />
          <span class="auth__hint">8 caractères minimum</span>
        </label>

        <label class="auth__field">
          <span class="auth__label">Confirmer le mot de passe</span>
          <input
            v-model="confirmation"
            type="password"
            autocomplete="new-password"
            minlength="8"
            required
          />
        </label>

        <p v-if="error" class="auth__error" role="alert">{{ error }}</p>

        <button type="submit" class="auth__submit" :disabled="submitting">
          {{
            submitting ? "Modification en cours" : "Modifier mon mot de passe"
          }}
        </button>
      </form>
    </div>
  </main>
</template>

<style scoped>
.auth {
  display: grid;
  place-items: start center;
  min-height: 60vh;
  padding: 48px var(--container-padding) 80px;
}

.auth__panel {
  width: 100%;
  max-width: 420px;
}

.auth__title {
  margin: 0 0 24px;
  font-family: var(--font-display);
  font-size: clamp(26px, 3vw, 34px);
  font-weight: 400;
  line-height: 1.05;
  letter-spacing: -0.03em;
}

.auth__form {
  display: grid;
  gap: 18px;
}

.auth__field {
  display: grid;
  gap: 6px;
}

.auth__label {
  color: var(--color-text-muted);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.auth__field input {
  height: 46px;
  padding-inline: 13px;
  border: 1px solid var(--color-border-strong);
  background: var(--color-white);
  color: var(--color-text);
  font-family: inherit;
  font-size: 14px;
}

.auth__field input:focus {
  border-color: var(--color-text);
  outline: none;
}

.auth__hint {
  color: var(--color-text-muted);
  font-size: 11.5px;
}

.auth__notice {
  margin: 0;
  padding: 14px 16px;
  background: #f5f4f1;
  font-size: 13.5px;
  line-height: 1.5;
}

.auth__error {
  margin: 0;
  color: var(--color-error);
  font-size: 13px;
  line-height: 1.45;
}

.auth__submit {
  display: grid;
  place-items: center;
  height: 48px;
  border: none;
  background: var(--color-black);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.06em;
  text-decoration: none;
  transition: opacity var(--transition-fast);
}

.auth__submit:hover:not(:disabled) {
  opacity: 0.85;
}

.auth__submit:disabled {
  cursor: wait;
  opacity: 0.5;
}

.auth__submit--link {
  margin-top: 22px;
}
</style>
