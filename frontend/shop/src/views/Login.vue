```vue
<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/authStore";

const email = ref("");
const password = ref("");
const error = ref("");
const showPassword = ref(false);
const rememberMe = ref(false);
const loading = ref(false);

const authStore = useAuthStore();
const router = useRouter();

/* ================================
   VALIDATION
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
   CONNEXION
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
    error.value = "Veuillez renseigner votre mot de passe.";
    return;
  }

  loading.value = true;

  try {
    await authStore.login(email.value.trim(), password.value);

    router.push("/");
  } catch (e) {
    console.error("Erreur de connexion :", e);

    error.value =
      "Impossible de vous connecter. Vérifiez votre adresse e-mail et votre mot de passe.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="login-page">
    <div class="login-container">
      <!-- ================================
           CARTE DE CONNEXION
      ================================= -->

      <section class="login-card">
        <!-- BRAND -->
        <div class="login-brand">
          <router-link to="/" class="brand-name"> SHAHIN </router-link>

          <span class="brand-line"></span>
        </div>

        <!-- TITRE -->
        <div class="login-heading">
          <span class="login-eyebrow">ESPACE CLIENT</span>

          <h1>Bienvenue chez SHAHIN</h1>

          <p>
            Connectez-vous pour accéder à votre compte et suivre vos commandes.
          </p>
        </div>

        <!-- FORMULAIRE -->
        <form class="login-form" @submit.prevent="handleSubmit" novalidate>
          <!-- EMAIL -->
          <div class="form-group">
            <label for="email"> Adresse e-mail </label>

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
                id="email"
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
            <div class="field-header">
              <label for="password"> Mot de passe </label>

              <router-link to="/forgot-password" class="forgot-link">
                Mot de passe oublié ?
              </router-link>
            </div>

            <div class="input-wrapper">
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
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="Votre mot de passe"
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
                <!-- ŒIL -->
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

                <!-- ŒIL FERMÉ -->
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
          </div>

          <!-- REMEMBER ME -->
          <label class="remember-row">
            <input v-model="rememberMe" type="checkbox" />

            <span class="custom-checkbox"></span>

            <span> Se souvenir de moi </span>
          </label>

          <!-- ERREUR -->
          <div v-if="error" class="login-error" role="alert">
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

            <span>{{ error }}</span>
          </div>

          <!-- SUBMIT -->
          <button type="submit" class="login-button" :disabled="loading">
            <span v-if="!loading"> Se connecter </span>

            <span v-else class="button-loading">
              <span class="spinner"></span>
              Connexion...
            </span>
          </button>
        </form>

        <!-- REGISTER -->
        <div class="register-section">
          <span> Vous n'avez pas encore de compte ? </span>

          <router-link to="/register"> Créer un compte </router-link>
        </div>
      </section>

      <!-- FOOTER -->
      <p class="login-footer">
        © {{ new Date().getFullYear() }} SHAHIN · Tous droits réservés
      </p>
    </div>
  </main>
</template>

<style scoped>
/* =========================================
   PAGE
========================================= */

.login-page {
  min-height: calc(100vh - 80px);
  display: flex;
  align-items: center;
  justify-content: center;

  padding: 4rem 1.5rem;

  background: var(--surface, #fafafa);
  color: var(--ink, #171717);
}

.login-container {
  width: 100%;
  max-width: 460px;
}

/* =========================================
   CARD
========================================= */

.login-card {
  padding: 2.8rem 2.7rem;

  border: 1px solid var(--border, #e5e5e5);
  border-radius: var(--radius-lg, 16px);

  background: #fff;

  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.04);
}

/* =========================================
   BRAND
========================================= */

.login-brand {
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

.login-heading {
  text-align: center;
  margin-bottom: 2rem;
}

.login-eyebrow {
  display: block;

  margin-bottom: 0.55rem;

  color: var(--ink-soft, #777);

  font-size: 0.68rem;
  font-weight: 600;

  letter-spacing: 0.16em;
}

.login-heading h1 {
  margin: 0;

  font-size: clamp(1.65rem, 4vw, 2rem);
  font-weight: 600;

  letter-spacing: -0.035em;
}

.login-heading p {
  max-width: 350px;

  margin: 0.7rem auto 0;

  color: var(--ink-soft, #777);

  font-size: 0.86rem;
  line-height: 1.6;
}

/* =========================================
   FORM
========================================= */

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label,
.field-header label {
  margin-bottom: 0.45rem;

  color: var(--ink, #171717);

  font-size: 0.8rem;
  font-weight: 500;
}

.field-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  gap: 1rem;
}

.field-header label {
  margin-bottom: 0.45rem;
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
   PASSWORD
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
   LINKS
========================================= */

.forgot-link {
  margin-bottom: 0.45rem;

  color: var(--ink-soft, #666);

  font-size: 0.72rem;

  text-decoration: none;
}

.forgot-link:hover {
  color: var(--ink, #171717);
  text-decoration: underline;
  text-underline-offset: 3px;
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
   REMEMBER
========================================= */

.remember-row {
  position: relative;

  display: flex;
  align-items: center;

  gap: 0.55rem;

  color: var(--ink-soft, #666);

  font-size: 0.76rem;

  cursor: pointer;
}

.remember-row input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.custom-checkbox {
  width: 16px;
  height: 16px;

  flex-shrink: 0;

  border: 1px solid var(--border-strong, #bbb);
  border-radius: 4px;

  background: #fff;

  transition: all 0.15s ease;
}

.remember-row input:checked + .custom-checkbox {
  border-color: var(--ink, #171717);
  background: var(--ink, #171717);

  box-shadow: inset 0 0 0 3px #fff;
}

/* =========================================
   ERROR
========================================= */

.login-error {
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

.login-button {
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

.login-button:hover:not(:disabled) {
  opacity: 0.88;
}

.login-button:active:not(:disabled) {
  transform: translateY(1px);
}

.login-button:disabled {
  opacity: 0.55;
  cursor: wait;
}

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
   REGISTER
========================================= */

.register-section {
  display: flex;
  justify-content: center;
  align-items: center;

  flex-wrap: wrap;

  gap: 0.3rem;

  margin-top: 1.8rem;

  color: var(--ink-soft, #777);

  font-size: 0.78rem;

  text-align: center;
}

.register-section a {
  color: var(--ink, #171717);

  font-weight: 500;

  text-decoration: none;
}

.register-section a:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* =========================================
   FOOTER
========================================= */

.login-footer {
  margin: 1.5rem 0 0;

  color: var(--ink-soft, #999);

  font-size: 0.68rem;

  text-align: center;
}

/* =========================================
   MOBILE
========================================= */

@media (max-width: 520px) {
  .login-page {
    align-items: flex-start;

    padding: 2rem 1rem;
  }

  .login-card {
    padding: 2.2rem 1.4rem;
  }

  .login-heading h1 {
    font-size: 1.6rem;
  }

  .field-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 0;
  }

  .forgot-link {
    margin-top: 0.1rem;
  }
}
</style>
