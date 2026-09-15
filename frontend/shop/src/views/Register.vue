<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/authStore";

const email = ref("");
const password = ref("");
const confirmPassword = ref("");

const error = ref("");
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);

const authStore = useAuthStore();
const router = useRouter();

/* ================================
   VALIDATION EMAIL
================================ */

const emailError = computed(() => {
  if (!email.value) return "";

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!emailRegex.test(email.value)) {
    return "Veuillez entrer une adresse e-mail valide.";
  }

  return "";
});

/* ================================
   VALIDATION MOT DE PASSE
================================ */

const passwordError = computed(() => {
  if (!password.value) return "";

  if (password.value.length < 8) {
    return "Le mot de passe doit contenir au moins 8 caractères.";
  }

  return "";
});

/* ================================
   CONFIRMATION
================================ */

const confirmPasswordError = computed(() => {
  if (!confirmPassword.value) return "";

  if (confirmPassword.value !== password.value) {
    return "Les mots de passe ne correspondent pas.";
  }

  return "";
});

/* ================================
   INSCRIPTION
================================ */

async function handleSubmit() {
  error.value = "";

  if (!email.value.trim()) {
    error.value = "Veuillez renseigner votre adresse e-mail.";
    return;
  }

  if (emailError.value) {
    error.value = emailError.value;
    return;
  }

  if (!password.value) {
    error.value = "Veuillez renseigner un mot de passe.";
    return;
  }

  if (passwordError.value) {
    error.value = passwordError.value;
    return;
  }

  if (!confirmPassword.value) {
    error.value = "Veuillez confirmer votre mot de passe.";
    return;
  }

  if (confirmPasswordError.value) {
    error.value = confirmPasswordError.value;
    return;
  }

  loading.value = true;

  try {
    await authStore.register(email.value.trim(), password.value);

    const redirect = typeof router.currentRoute.value.query.redirect === "string"
      ? router.currentRoute.value.query.redirect
      : "/";

    await router.replace(redirect);
  } catch (e) {
    console.error("Erreur lors de l'inscription :", e);

    error.value =
      "Impossible de créer le compte. Cet email est peut-être déjà utilisé.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="register-page">
    <div class="register-container">
      <!-- ================================
           CARTE
      ================================= -->

      <section class="register-card">
        <!-- BRAND -->
        <div class="register-brand">
          <router-link to="/" class="brand-name"> SHAHIN </router-link>

          <span class="brand-line"></span>
        </div>

        <!-- TITRE -->
        <div class="register-heading">
          <span class="register-eyebrow"> ESPACE CLIENT </span>

          <h1>Créer votre compte</h1>

          <p>
            Rejoignez SHAHIN pour retrouver vos commandes et profiter d'une
            expérience personnalisée.
          </p>
        </div>

        <!-- FORMULAIRE -->
        <form class="register-form" @submit.prevent="handleSubmit" novalidate>
          <!-- EMAIL -->
          <div class="form-group">
            <label for="register-email"> Adresse e-mail </label>

            <div class="input-wrapper" :class="{ 'input-error': emailError }">
              <svg
                class="input-icon"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
              >
                <rect x="3" y="5" width="18" height="14" rx="2" />

                <path d="m3 7 9 6 9-6" />
              </svg>

              <input
                id="register-email"
                v-model="email"
                type="email"
                autocomplete="email"
                placeholder="vous@exemple.com"
                :aria-invalid="!!emailError"
              />
            </div>

            <p v-if="emailError" class="field-error">
              {{ emailError }}
            </p>
          </div>

          <!-- MOT DE PASSE -->
          <div class="form-group">
            <label for="register-password"> Mot de passe </label>

            <div
              class="input-wrapper"
              :class="{ 'input-error': passwordError }"
            >
              <svg
                class="input-icon"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
              >
                <rect x="5" y="10" width="14" height="11" rx="2" />

                <path d="M8 10V7a4 4 0 0 1 8 0v3" />
              </svg>

              <input
                id="register-password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="new-password"
                placeholder="8 caractères minimum"
                :aria-invalid="!!passwordError"
              />

              <button
                type="button"
                class="password-toggle"
                :aria-label="
                  showPassword
                    ? 'Masquer le mot de passe'
                    : 'Afficher le mot de passe'
                "
                @click="showPassword = !showPassword"
              >
                <svg
                  v-if="!showPassword"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                >
                  <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" />

                  <circle cx="12" cy="12" r="2.5" />
                </svg>

                <svg
                  v-else
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                >
                  <path d="M3 3l18 18" />

                  <path
                    d="M10.6 6.2A9.8 9.8 0 0 1 12 6c6.5 0 10 6 10 6a17 17 0 0 1-3.2 3.8"
                  />

                  <path
                    d="M6.2 6.8C3.5 8.7 2 12 2 12s3.5 6 10 6c1.6 0 3-.3 4.2-.8"
                  />
                </svg>
              </button>
            </div>

            <p v-if="passwordError" class="field-error">
              {{ passwordError }}
            </p>
          </div>

          <!-- CONFIRMATION -->
          <div class="form-group">
            <label for="register-confirm-password">
              Confirmer le mot de passe
            </label>

            <div
              class="input-wrapper"
              :class="{
                'input-error': confirmPasswordError,
              }"
            >
              <svg
                class="input-icon"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.7"
              >
                <rect x="5" y="10" width="14" height="11" rx="2" />

                <path d="M8 10V7a4 4 0 0 1 8 0v3" />
              </svg>

              <input
                id="register-confirm-password"
                v-model="confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                autocomplete="new-password"
                placeholder="Confirmez votre mot de passe"
                :aria-invalid="!!confirmPasswordError"
              />

              <button
                type="button"
                class="password-toggle"
                :aria-label="
                  showConfirmPassword
                    ? 'Masquer le mot de passe'
                    : 'Afficher le mot de passe'
                "
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <svg
                  v-if="!showConfirmPassword"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                >
                  <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" />

                  <circle cx="12" cy="12" r="2.5" />
                </svg>

                <svg
                  v-else
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                >
                  <path d="M3 3l18 18" />

                  <path
                    d="M10.6 6.2A9.8 9.8 0 0 1 12 6c6.5 0 10 6 10 6a17 17 0 0 1-3.2 3.8"
                  />

                  <path
                    d="M6.2 6.8C3.5 8.7 2 12 2 12s3.5 6 10 6c1.6 0 3-.3 4.2-.8"
                  />
                </svg>
              </button>
            </div>

            <p v-if="confirmPasswordError" class="field-error">
              {{ confirmPasswordError }}
            </p>
          </div>

          <!-- MESSAGE GLOBAL -->
          <div v-if="error" class="register-error" role="alert">
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.8"
            >
              <circle cx="12" cy="12" r="9" />

              <path d="M12 8v5" />
              <path d="M12 16h.01" />
            </svg>

            <span>
              {{ error }}
            </span>
          </div>

          <!-- BOUTON -->
          <button type="submit" class="register-button" :disabled="loading">
            <span v-if="!loading"> Créer mon compte </span>

            <span v-else class="button-loading">
              <span class="spinner"></span>
              Création du compte...
            </span>
          </button>
        </form>

        <!-- CONNEXION -->
        <div class="login-section">
          <span> Vous avez déjà un compte ? </span>

          <router-link to="/login"> Se connecter </router-link>
        </div>
      </section>

      <!-- FOOTER -->
      <p class="register-footer">
        © {{ new Date().getFullYear() }}
        SHAHIN · Tous droits réservés
      </p>
    </div>
  </main>
</template>

<style scoped>
/* =========================================
   PAGE
========================================= */

.register-page {
  min-height: calc(100vh - 80px);

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 4rem 1.5rem;

  background: var(--surface, #fafafa);
  color: var(--ink, #171717);
}

.register-container {
  width: 100%;
  max-width: 460px;
}

/* =========================================
   CARD
========================================= */

.register-card {
  padding: 2.8rem 2.7rem;

  border: 1px solid var(--border, #e5e5e5);
  border-radius: var(--radius-lg, 16px);

  background: #fff;

  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.04);
}

/* =========================================
   BRAND
========================================= */

.register-brand {
  display: flex;
  flex-direction: column;
  align-items: center;

  margin-bottom: 2.2rem;
}

.brand-name {
  color: var(--ink, #171717);

  font-size: 1.45rem;
  font-weight: 700;

  letter-spacing: 0.16em;

  text-decoration: none;
}

.brand-line {
  width: 28px;
  height: 2px;

  margin-top: 0.6rem;

  background: var(--ink, #171717);
}

/* =========================================
   HEADING
========================================= */

.register-heading {
  margin-bottom: 2rem;

  text-align: center;
}

.register-eyebrow {
  display: block;

  margin-bottom: 0.55rem;

  color: var(--ink-soft, #777);

  font-size: 0.68rem;
  font-weight: 600;

  letter-spacing: 0.16em;
}

.register-heading h1 {
  margin: 0;

  font-size: clamp(1.65rem, 4vw, 2rem);
  font-weight: 600;

  letter-spacing: -0.035em;
}

.register-heading p {
  max-width: 360px;

  margin: 0.7rem auto 0;

  color: var(--ink-soft, #777);

  font-size: 0.86rem;

  line-height: 1.6;
}

/* =========================================
   FORM
========================================= */

.register-form {
  display: flex;
  flex-direction: column;

  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 0.45rem;

  color: var(--ink, #171717);

  font-size: 0.8rem;
  font-weight: 500;
}

/* =========================================
   INPUT
========================================= */

.input-wrapper {
  position: relative;

  display: flex;
  align-items: center;

  border: 1px solid var(--border, #ddd);
  border-radius: var(--radius, 8px);

  background: #fff;

  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: var(--ink, #171717);

  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.04);
}

.input-wrapper.input-error {
  border-color: #b42318;
}

.input-icon {
  flex-shrink: 0;

  margin-left: 0.85rem;

  color: var(--ink-soft, #888);
}

.input-wrapper input {
  width: 100%;
  min-width: 0;

  padding: 0.78rem 0.8rem;

  border: 0;
  outline: 0;

  background: transparent;

  color: var(--ink, #171717);

  font-family: inherit;
  font-size: 0.85rem;
}

.input-wrapper input::placeholder {
  color: #aaa;
}

/* =========================================
   PASSWORD TOGGLE
========================================= */

.password-toggle {
  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  width: 42px;
  height: 42px;

  border: 0;

  background: transparent;

  color: var(--ink-soft, #777);

  cursor: pointer;
}

.password-toggle:hover {
  color: var(--ink, #171717);
}

/* =========================================
   FIELD ERROR
========================================= */

.field-error {
  margin: 0.4rem 0 0;

  color: #b42318;

  font-size: 0.72rem;
}

/* =========================================
   GLOBAL ERROR
========================================= */

.register-error {
  display: flex;
  align-items: flex-start;

  gap: 0.65rem;

  padding: 0.75rem 0.85rem;

  border: 1px solid #f0b8b3;
  border-radius: var(--radius, 8px);

  background: #fff7f6;

  color: #9f2117;

  font-size: 0.76rem;

  line-height: 1.45;
}

/* =========================================
   BUTTON
========================================= */

.register-button {
  width: 100%;

  padding: 0.85rem 1rem;

  border: 1px solid var(--ink, #171717);
  border-radius: var(--radius, 8px);

  background: var(--ink, #171717);
  color: #fff;

  font-family: inherit;

  font-size: 0.84rem;
  font-weight: 500;

  cursor: pointer;

  transition:
    opacity 0.2s ease,
    transform 0.1s ease;
}

.register-button:hover:not(:disabled) {
  opacity: 0.88;
}

.register-button:active:not(:disabled) {
  transform: translateY(1px);
}

.register-button:disabled {
  opacity: 0.55;

  cursor: wait;
}

/* =========================================
   LOADING
========================================= */

.button-loading {
  display: inline-flex;

  align-items: center;
  justify-content: center;

  gap: 0.6rem;
}

.spinner {
  width: 14px;
  height: 14px;

  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;

  border-radius: 50%;

  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* =========================================
   LOGIN LINK
========================================= */

.login-section {
  display: flex;
  align-items: center;
  justify-content: center;

  flex-wrap: wrap;

  gap: 0.3rem;

  margin-top: 1.8rem;

  color: var(--ink-soft, #777);

  font-size: 0.78rem;

  text-align: center;
}

.login-section a {
  color: var(--ink, #171717);

  font-weight: 500;

  text-decoration: none;
}

.login-section a:hover {
  text-decoration: underline;

  text-underline-offset: 3px;
}

/* =========================================
   FOOTER
========================================= */

.register-footer {
  margin: 1.5rem 0 0;

  color: var(--ink-soft, #999);

  font-size: 0.68rem;

  text-align: center;
}

/* =========================================
   MOBILE
========================================= */

@media (max-width: 520px) {
  .register-page {
    align-items: flex-start;

    padding: 2rem 1rem;
  }

  .register-card {
    padding: 2.2rem 1.4rem;
  }

  .register-heading h1 {
    font-size: 1.6rem;
  }
}
</style>
