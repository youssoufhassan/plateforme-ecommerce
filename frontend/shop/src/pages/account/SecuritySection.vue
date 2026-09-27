<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

import { apiMessage } from "@/services/productService";
import { changePassword } from "@/services/accountService";
import { resendVerification } from "@/services/authService";
import { useAuthStore } from "@/stores/authStore";

const router = useRouter();
const authStore = useAuthStore();

const currentPassword = ref("");
const newPassword = ref("");
const confirmation = ref("");

const guestPassword = ref("");

const saving = ref(false);
const error = ref<string | null>(null);
const notice = ref<string | null>(null);

/** Un compte invité n'a pas de mot de passe connu : il en crée un directement. */
async function handleCreatePassword(): Promise<void> {
  if (guestPassword.value.length < 8) {
    error.value = "Le mot de passe doit contenir au moins 8 caractères.";
    return;
  }

  saving.value = true;
  error.value = null;

  try {
    await authStore.createPassword(guestPassword.value);
    notice.value =
      "Votre mot de passe a été créé. Vous pouvez désormais vous connecter avec.";
    guestPassword.value = "";
  } catch (e: unknown) {
    error.value = apiMessage(e, "Le mot de passe n'a pas pu être créé.");
  } finally {
    saving.value = false;
  }
}

/**
 * Le changement de mot de passe invalide le jeton courant :
 * le client doit se reconnecter.
 */
async function handleChangePassword(): Promise<void> {
  error.value = null;

  if (newPassword.value !== confirmation.value) {
    error.value = "Les deux mots de passe ne correspondent pas.";
    return;
  }

  saving.value = true;

  try {
    await changePassword(currentPassword.value, newPassword.value);

    notice.value =
      "Mot de passe modifié. Vous allez être redirigé vers la connexion.";
    setTimeout(() => {
      authStore.logout();
      router.push("/connexion");
    }, 2000);
  } catch (e: unknown) {
    error.value = apiMessage(e, "Le mot de passe n'a pas pu être modifié.");
  } finally {
    saving.value = false;
  }
}

async function handleResendVerification(): Promise<void> {
  error.value = null;

  try {
    await resendVerification();
    notice.value = "Un email de confirmation vient de vous être envoyé.";
  } catch (e: unknown) {
    error.value = apiMessage(e, "L'email n'a pas pu être renvoyé.");
  }
}
</script>

<template>
  <section>
    <h2 class="section-title">Sécurité</h2>

    <!-- Vérification de l'adresse email -->
    <div v-if="!authStore.emailVerified" class="block">
      <h3 class="block__title">Adresse email non confirmée</h3>
      <p class="block__text">
        Vous ne pourrez pas commander tant que votre adresse n'est pas vérifiée.
      </p>
      <button
        type="button"
        class="form__submit"
        @click="handleResendVerification"
      >
        Renvoyer l'email de confirmation
      </button>
    </div>

    <!-- Compte invité : création d'un mot de passe -->
    <form
      v-if="authStore.isGuest"
      class="form"
      @submit.prevent="handleCreatePassword"
    >
      <h3 class="block__title">Créer un mot de passe</h3>
      <p class="block__text">
        Vous avez commandé sans compte. Choisissez un mot de passe pour vous
        connecter directement la prochaine fois.
      </p>

      <label class="field">
        <span class="field__label">Nouveau mot de passe</span>
        <input
          v-model="guestPassword"
          type="password"
          autocomplete="new-password"
          minlength="8"
          required
        />
        <span class="field__hint">8 caractères minimum</span>
      </label>

      <button type="submit" class="form__submit" :disabled="saving">
        {{ saving ? "Création" : "Créer mon mot de passe" }}
      </button>
    </form>

    <!-- Compte classique : changement de mot de passe -->
    <form v-else class="form" @submit.prevent="handleChangePassword">
      <h3 class="block__title">Changer de mot de passe</h3>

      <label class="field">
        <span class="field__label">Mot de passe actuel</span>
        <input
          v-model="currentPassword"
          type="password"
          autocomplete="current-password"
          required
        />
      </label>

      <label class="field">
        <span class="field__label">Nouveau mot de passe</span>
        <input
          v-model="newPassword"
          type="password"
          autocomplete="new-password"
          minlength="8"
          required
        />
        <span class="field__hint">8 caractères minimum</span>
      </label>

      <label class="field">
        <span class="field__label">Confirmer le nouveau mot de passe</span>
        <input
          v-model="confirmation"
          type="password"
          autocomplete="new-password"
          minlength="8"
          required
        />
      </label>

      <p class="block__note">
        Pour votre sécurité, vous serez déconnecté après la modification.
      </p>

      <button type="submit" class="form__submit" :disabled="saving">
        {{ saving ? "Modification" : "Modifier mon mot de passe" }}
      </button>
    </form>

    <p v-if="notice" class="form__notice" role="status">{{ notice }}</p>
    <p v-if="error" class="form__error" role="alert">{{ error }}</p>
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
  max-width: 480px;
  padding: 18px;
  margin-bottom: 26px;
  border-left: 2px solid var(--color-text);
  background: #f5f4f1;
}

.block__title {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 500;
}

.block__text {
  margin: 0 0 14px;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.5;
}

.block__note {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
  line-height: 1.5;
}

.form {
  display: grid;
  gap: 16px;
  max-width: 480px;
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
  color: var(--color-text);
  font-family: inherit;
  font-size: 14px;
}

.field input:focus {
  border-color: var(--color-text);
  outline: none;
}

.field__hint {
  color: var(--color-text-muted);
  font-size: 11.5px;
}

.form__submit {
  justify-self: start;
  height: 44px;
  padding-inline: 26px;
  border: none;
  background: var(--color-black);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 12px;
}

.form__submit:disabled {
  cursor: wait;
  opacity: 0.5;
}

.form__notice {
  max-width: 480px;
  margin: 18px 0 0;
  padding: 11px 13px;
  background: #f5f4f1;
  font-size: 13px;
  line-height: 1.45;
}

.form__error {
  max-width: 480px;
  margin: 18px 0 0;
  color: var(--color-error);
  font-size: 13px;
}
</style>
