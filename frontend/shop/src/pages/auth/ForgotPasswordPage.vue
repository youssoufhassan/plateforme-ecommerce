<script setup lang="ts">
import { ref } from "vue";
import { RouterLink } from "vue-router";

import { apiMessage } from "@/services/productService";
import { forgotPassword } from "@/services/authService";
const email = ref("");
const submitting = ref(false);
const sent = ref(false);
const message = ref("");
const error = ref<string | null>(null);

async function handleSubmit(): Promise<void> {
  submitting.value = true;
  error.value = null;

  try {
    const response = await forgotPassword(email.value.trim());
    message.value = response.message;
    sent.value = true;
  } catch (e: unknown) {
    error.value = apiMessage(e, "La demande n'a pas pu être envoyée.");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <main class="auth">
    <div class="auth__panel">
      <h1 class="auth__title">Mot de passe oublié</h1>

      <template v-if="!sent">
        <p class="auth__intro">
          Indiquez votre adresse email : vous recevrez un lien pour choisir un
          nouveau mot de passe.
        </p>

        <form class="auth__form" @submit.prevent="handleSubmit">
          <label class="auth__field">
            <span class="auth__label">Adresse email</span>
            <input v-model="email" type="email" autocomplete="email" required />
          </label>

          <p v-if="error" class="auth__error" role="alert">{{ error }}</p>

          <button type="submit" class="auth__submit" :disabled="submitting">
            {{ submitting ? "Envoi en cours" : "Envoyer le lien" }}
          </button>

          <RouterLink to="/connexion" class="auth__link">
            Revenir à la connexion
          </RouterLink>
        </form>
      </template>

      <template v-else>
        <p class="auth__notice" role="status">{{ message }}</p>

        <p class="auth__explain">
          Pensez à vérifier vos courriers indésirables. Le lien reste valable
          une heure.
        </p>

        <RouterLink to="/connexion" class="auth__submit auth__submit--link">
          Revenir à la connexion
        </RouterLink>
      </template>
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
  margin: 0 0 16px;
  font-family: var(--font-display);
  font-size: clamp(26px, 3vw, 34px);
  font-weight: 400;
  line-height: 1.05;
  letter-spacing: -0.03em;
}

.auth__intro {
  margin: 0 0 26px;
  color: var(--color-text-muted);
  font-size: 13.5px;
  line-height: 1.55;
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

.auth__link {
  color: var(--color-text-muted);
  font-size: 12.5px;
  text-align: center;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.auth__link:hover {
  color: var(--color-text);
}

.auth__explain {
  margin: 16px 0 0;
  color: var(--color-text-muted);
  font-size: 12.5px;
  line-height: 1.55;
}
</style>
