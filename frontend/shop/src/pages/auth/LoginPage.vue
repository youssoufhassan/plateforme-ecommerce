<script setup lang="ts">
import { computed, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";

import { apiMessage } from "@/services/productService";
import { requestGuestCode } from "@/services/authService";
import { useAuthStore } from "@/stores/authStore";
type Mode = "login" | "register" | "guest";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const mode = ref<Mode>((route.query.mode as Mode) || "login");

const email = ref("");
const password = ref("");
const code = ref("");

const codeSent = ref(false);
const cooldown = ref(0);

const submitting = ref(false);
const error = ref<string | null>(null);
const notice = ref<string | null>(null);

/** Où renvoyer après identification : le tunnel de commande, ou l'accueil. */
const redirectTo = computed(() => (route.query.redirect as string) || "/");

const title = computed(() => {
  if (mode.value === "register") return "Créer un compte";
  if (mode.value === "guest") return "Commander sans compte";
  return "Se connecter";
});

function switchMode(value: Mode): void {
  mode.value = value;
  error.value = null;
  notice.value = null;
  codeSent.value = false;
  code.value = "";
}

function startCooldown(): void {
  cooldown.value = 60;

  const timer = setInterval(() => {
    cooldown.value -= 1;
    if (cooldown.value <= 0) clearInterval(timer);
  }, 1000);
}

async function handleSubmit(): Promise<void> {
  error.value = null;
  notice.value = null;
  submitting.value = true;

  try {
    if (mode.value === "login") {
      await authStore.login(email.value.trim(), password.value);
      router.push(redirectTo.value);
      return;
    }

    if (mode.value === "register") {
      await authStore.register(email.value.trim(), password.value);
      router.push(redirectTo.value);
      return;
    }

    // Mode invité : demande du code, puis vérification
    if (!codeSent.value) {
      const response = await requestGuestCode(email.value.trim());
      notice.value = response.message;
      codeSent.value = true;
      startCooldown();
      return;
    }

    await authStore.loginWithCode(email.value.trim(), code.value.trim());
    router.push(redirectTo.value);
  } catch (e: unknown) {
    error.value = apiMessage(e, "Une erreur est survenue.");
  } finally {
    submitting.value = false;
  }
}

async function resendCode(): Promise<void> {
  if (cooldown.value > 0) return;

  error.value = null;

  try {
    const response = await requestGuestCode(email.value.trim());
    notice.value = response.message;
    startCooldown();
  } catch (e: unknown) {
    error.value = apiMessage(e, "Le code n'a pas pu être renvoyé.");
  }
}
</script>

<template>
  <main class="auth">
    <div class="auth__panel">
      <h1 class="auth__title">{{ title }}</h1>

      <!-- Choix du mode -->
      <div class="auth__tabs" role="tablist">
        <button
          type="button"
          class="auth__tab"
          :class="{ 'auth__tab--active': mode === 'login' }"
          role="tab"
          :aria-selected="mode === 'login'"
          @click="switchMode('login')"
        >
          Connexion
        </button>

        <button
          type="button"
          class="auth__tab"
          :class="{ 'auth__tab--active': mode === 'register' }"
          role="tab"
          :aria-selected="mode === 'register'"
          @click="switchMode('register')"
        >
          Inscription
        </button>

        <button
          type="button"
          class="auth__tab"
          :class="{ 'auth__tab--active': mode === 'guest' }"
          role="tab"
          :aria-selected="mode === 'guest'"
          @click="switchMode('guest')"
        >
          Sans compte
        </button>
      </div>

      <form class="auth__form" @submit.prevent="handleSubmit">
        <label class="auth__field">
          <span class="auth__label">Adresse email</span>
          <input
            v-model="email"
            type="email"
            name="email"
            autocomplete="email"
            required
            :disabled="mode === 'guest' && codeSent"
          />
        </label>

        <!-- Mot de passe : connexion et inscription -->
        <label v-if="mode !== 'guest'" class="auth__field">
          <span class="auth__label">Mot de passe</span>
          <input
            v-model="password"
            type="password"
            name="password"
            :autocomplete="
              mode === 'register' ? 'new-password' : 'current-password'
            "
            :minlength="mode === 'register' ? 8 : undefined"
            required
          />
          <span v-if="mode === 'register'" class="auth__hint">
            8 caractères minimum
          </span>
        </label>

        <!-- Code : mode invité, après envoi -->
        <label v-if="mode === 'guest' && codeSent" class="auth__field">
          <span class="auth__label">Code reçu par email</span>
          <input
            v-model="code"
            type="text"
            inputmode="numeric"
            autocomplete="one-time-code"
            maxlength="6"
            pattern="[0-9]{6}"
            class="auth__code"
            required
          />
          <span class="auth__hint">Valable 10 minutes</span>
        </label>

        <p v-if="notice" class="auth__notice" role="status">{{ notice }}</p>
        <p v-if="error" class="auth__error" role="alert">{{ error }}</p>

        <button type="submit" class="auth__submit" :disabled="submitting">
          <template v-if="submitting">Un instant</template>
          <template v-else-if="mode === 'login'">Se connecter</template>
          <template v-else-if="mode === 'register'">Créer mon compte</template>
          <template v-else-if="!codeSent">Recevoir un code</template>
          <template v-else>Valider le code</template>
        </button>

        <button
          v-if="mode === 'guest' && codeSent"
          type="button"
          class="auth__link-button"
          :disabled="cooldown > 0"
          @click="resendCode"
        >
          {{
            cooldown > 0
              ? `Renvoyer le code dans ${cooldown} s`
              : "Renvoyer le code"
          }}
        </button>

        <RouterLink
          v-if="mode === 'login'"
          to="/mot-de-passe-oublie"
          class="auth__link"
        >
          Mot de passe oublié
        </RouterLink>
      </form>

      <p v-if="mode === 'guest'" class="auth__explain">
        Vous recevrez un code à six chiffres pour confirmer votre adresse. Aucun
        mot de passe n'est nécessaire : vous pourrez en créer un après votre
        commande.
      </p>

      <p v-else-if="mode === 'register'" class="auth__explain">
        Un email de confirmation vous sera envoyé. Votre adresse doit être
        vérifiée avant de pouvoir commander.
      </p>
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

/* =========================================================
   MODES
   ========================================================= */

.auth__tabs {
  display: flex;
  gap: 0;
  margin-bottom: 28px;
  border-bottom: 1px solid var(--color-border);
}

.auth__tab {
  flex: 1;
  padding: 11px 6px;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: 12.5px;
  transition:
    color var(--transition-fast),
    border-color var(--transition-fast);
}

.auth__tab:hover {
  color: var(--color-text);
}

.auth__tab--active {
  border-bottom-color: var(--color-text);
  color: var(--color-text);
}

/* =========================================================
   FORMULAIRE
   ========================================================= */

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

.auth__field input:disabled {
  background: #f5f4f1;
  color: var(--color-text-muted);
}

.auth__code {
  font-size: 20px;
  letter-spacing: 0.4em;
  text-align: center;
}

.auth__hint {
  color: var(--color-text-muted);
  font-size: 11.5px;
}

.auth__notice {
  margin: 0;
  padding: 11px 13px;
  background: #f5f4f1;
  font-size: 13px;
  line-height: 1.45;
}

.auth__error {
  margin: 0;
  color: var(--color-error);
  font-size: 13px;
  line-height: 1.45;
}

.auth__submit {
  height: 48px;
  border: none;
  background: var(--color-black);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.06em;
  transition: opacity var(--transition-fast);
}

.auth__submit:hover:not(:disabled) {
  opacity: 0.85;
}

.auth__submit:disabled {
  cursor: wait;
  opacity: 0.5;
}

.auth__link,
.auth__link-button {
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: 12.5px;
  text-align: center;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.auth__link:hover,
.auth__link-button:hover:not(:disabled) {
  color: var(--color-text);
}

.auth__link-button:disabled {
  cursor: default;
  opacity: 0.55;
  text-decoration: none;
}

.auth__explain {
  margin: 24px 0 0;
  color: var(--color-text-muted);
  font-size: 12.5px;
  line-height: 1.55;
}
</style>
