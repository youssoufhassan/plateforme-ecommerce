<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";

import { apiMessage } from "@/services/productService";
import {
  deleteAccount,
  exportPersonalData,
  updateMarketingConsent,
} from "@/services/accountService";
import { useAuthStore } from "@/stores/authStore";

const router = useRouter();
const authStore = useAuthStore();

const marketing = ref(false);
const confirmDelete = ref(false);
const deletePassword = ref("");

const working = ref(false);
const notice = ref<string | null>(null);
const error = ref<string | null>(null);

async function handleConsent(event: Event): Promise<void> {
  const checked = (event.target as HTMLInputElement).checked;
  error.value = null;

  try {
    await updateMarketingConsent(checked);
    marketing.value = checked;
    notice.value = checked
      ? "Vous recevrez nos actualités."
      : "Vous ne recevrez plus nos actualités.";
  } catch (e: unknown) {
    error.value = apiMessage(e, "Votre choix n'a pas pu être enregistré.");
  }
}

async function handleExport(): Promise<void> {
  working.value = true;
  error.value = null;

  try {
    await exportPersonalData();
  } catch (e: unknown) {
    error.value = apiMessage(e, "L'export n'a pas pu être généré.");
  } finally {
    working.value = false;
  }
}

async function handleDelete(): Promise<void> {
  working.value = true;
  error.value = null;

  try {
    await deleteAccount(deletePassword.value);
    authStore.logout();
    router.push("/");
  } catch (e: unknown) {
    error.value = apiMessage(e, "Le compte n'a pas pu être supprimé.");
    working.value = false;
  }
}

onMounted(() => {
  // Le consentement n'est pas exposé par /api/me : on part de l'état non coché
  marketing.value = false;
});
</script>

<template>
  <section>
    <h2 class="section-title">Mes données personnelles</h2>

    <!-- Consentement marketing -->
    <div class="block">
      <h3 class="block__title">Communications</h3>

      <label class="consent">
        <input type="checkbox" :checked="marketing" @change="handleConsent" />
        <span>
          J'accepte de recevoir les actualités et offres de SHAHIN par email.
          Vous pouvez changer d'avis à tout moment.
        </span>
      </label>
    </div>

    <!-- Export -->
    <div class="block">
      <h3 class="block__title">Exporter mes données</h3>
      <p class="block__text">
        Téléchargez l'ensemble des données que nous conservons à votre sujet :
        profil, adresses et commandes.
      </p>

      <button
        type="button"
        class="action"
        :disabled="working"
        @click="handleExport"
      >
        Télécharger mes données
      </button>
    </div>

    <!-- Suppression -->
    <div class="block block--danger">
      <h3 class="block__title">Supprimer mon compte</h3>

      <p class="block__text">
        Vos données personnelles seront effacées. Vos commandes et factures sont
        conservées dix ans, comme la loi l'exige, mais ne seront plus rattachées
        à votre identité. Cette action est irréversible.
      </p>

      <template v-if="!confirmDelete">
        <button
          type="button"
          class="action action--danger"
          @click="confirmDelete = true"
        >
          Supprimer mon compte
        </button>
      </template>

      <form v-else class="delete-form" @submit.prevent="handleDelete">
        <label v-if="!authStore.isGuest" class="field">
          <span class="field__label">Confirmez avec votre mot de passe</span>
          <input
            v-model="deletePassword"
            type="password"
            autocomplete="current-password"
            required
          />
        </label>

        <p v-else class="block__text">
          Confirmez la suppression définitive de votre compte.
        </p>

        <div class="delete-form__actions">
          <button
            type="submit"
            class="action action--danger"
            :disabled="working"
          >
            {{ working ? "Suppression" : "Confirmer la suppression" }}
          </button>

          <button type="button" class="cancel" @click="confirmDelete = false">
            Annuler
          </button>
        </div>
      </form>
    </div>

    <p v-if="notice" class="notice" role="status">{{ notice }}</p>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
  </section>
</template>

<style scoped>
.section-title {
  margin: 0 0 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.block {
  max-width: 520px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid var(--color-border);
  background: var(--color-white);
}

.block--danger {
  border-color: var(--color-error);
}

.block__title {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 500;
}

.block__text {
  margin: 0 0 14px;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.55;
}

.consent {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  cursor: pointer;
  font-size: 13px;
  line-height: 1.55;
}

.consent input {
  margin-top: 3px;
  width: 16px;
  height: 16px;
  accent-color: var(--color-black);
  cursor: pointer;
}

.action {
  padding: 11px 20px;
  border: 1px solid var(--color-border-strong);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
}

.action:hover:not(:disabled) {
  border-color: var(--color-text);
}

.action:disabled {
  cursor: wait;
  opacity: 0.5;
}

.action--danger {
  border-color: var(--color-error);
  color: var(--color-error);
}

.action--danger:hover:not(:disabled) {
  background: var(--color-error);
  color: var(--color-white);
}

.delete-form {
  display: grid;
  gap: 14px;
}

.field {
  display: grid;
  gap: 5px;
}

.field__label {
  color: var(--color-text-muted);
  font-size: 11px;
  letter-spacing: 0.06em;
}

.field input {
  height: 44px;
  padding-inline: 12px;
  border: 1px solid var(--color-border-strong);
  background: var(--color-white);
  font-family: inherit;
  font-size: 14px;
}

.delete-form__actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

.cancel {
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: 12.5px;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.notice {
  max-width: 520px;
  margin: 16px 0 0;
  padding: 11px 13px;
  background: #f5f4f1;
  font-size: 13px;
}

.error {
  max-width: 520px;
  margin: 16px 0 0;
  color: var(--color-error);
  font-size: 13px;
}
</style>
