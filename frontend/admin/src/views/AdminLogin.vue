<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/authStore";

const email = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);
const authStore = useAuthStore();
const router = useRouter();

async function handleSubmit() {
  if (loading.value) return;
  error.value = "";
  loading.value = true;

  try {
    await authStore.login(email.value, password.value);
    router.push("/");
  } catch (e: any) {
    if (e.response?.status === 401 || e.response?.status === 403) {
      error.value = "Identifiants invalides ou droits d'accès insuffisants.";
    } else {
      error.value = e.response?.data?.message || `Erreur de connexion (${e.response?.status ?? "serveur injoignable"}).`;
    }
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-badge">S</div>
        <h1 class="brand-title">SIDRA</h1>
        <span class="brand-subtitle">PORTAIL D'ADMINISTRATION</span>
      </div>

      <div class="login-header">
        <h2>Connexion</h2>
        <p>Identifiez-vous pour gérer le catalogue, les commandes et les stocks.</p>
      </div>

      <form @submit.prevent="handleSubmit" class="login-form">
        <div class="form-group">
          <label for="admin-email">Adresse e-mail</label>
          <input
            id="admin-email"
            v-model="email"
            type="email"
            placeholder="admin@sidra.com"
            autocomplete="email"
            required
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="admin-password">Mot de passe</label>
          <input
            id="admin-password"
            v-model="password"
            type="password"
            placeholder="••••••••••••"
            autocomplete="current-password"
            required
            class="form-input"
          />
        </div>

        <div v-if="error" class="login-error" role="alert">
          <span class="error-icon">!</span>
          <span>{{ error }}</span>
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          <span v-if="loading">Connexion en cours...</span>
          <span v-else>Accéder à l'administration</span>
        </button>
      </form>

      <div class="login-footer">
        <span>Boutique SIDRA • Espace réservé aux administrateurs</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background: #0e1012;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.login-card {
  width: 100%;
  max-width: 440px;
  background: #17191d;
  border: 1px solid #282b31;
  border-radius: 12px;
  padding: 36px 32px 28px;
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.4);
}

.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 24px;
}

.brand-badge {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #ffffff;
  color: #111214;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.35rem;
  font-weight: 900;
  margin-bottom: 12px;
}

.brand-title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: #ffffff;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: #8b919b;
}

.login-header {
  margin-bottom: 24px;
  text-align: center;
}

.login-header h2 {
  margin: 0 0 6px;
  font-size: 1.15rem;
  font-weight: 600;
  color: #f3f4f6;
}

.login-header p {
  margin: 0;
  font-size: 0.82rem;
  color: #8b919b;
  line-height: 1.45;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.78rem;
  font-weight: 600;
  color: #d1d5db;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  background: #0e1012;
  border: 1px solid #2d3139;
  border-radius: 8px;
  color: #ffffff;
  font-size: 0.9rem;
  transition: border-color 0.15s ease;
}

.form-input:focus {
  outline: none;
  border-color: #ffffff;
}

.form-input::placeholder {
  color: #525866;
}

.login-error {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: rgba(239, 68, 68, 0.12);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #fca5a5;
  font-size: 0.8rem;
}

.error-icon {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #ef4444;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}

.login-button {
  height: 46px;
  margin-top: 8px;
  background: #ffffff;
  color: #111214;
  border: none;
  border-radius: 8px;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease, opacity 0.15s ease;
}

.login-button:hover:not(:disabled) {
  background: #e5e7eb;
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #23272e;
  text-align: center;
  font-size: 0.72rem;
  color: #6b7280;
}
</style>
