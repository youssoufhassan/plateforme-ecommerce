<script setup lang="ts">
import { onMounted, ref } from "vue";

import { apiMessage } from "@/services/productService";
import { updateProfile } from "@/services/accountService";
import { useAuthStore } from "@/stores/authStore";

const authStore = useAuthStore();

const firstName = ref("");
const lastName = ref("");
const phone = ref("");

const saving = ref(false);
const saved = ref(false);
const error = ref<string | null>(null);

async function handleSubmit(): Promise<void> {
  saving.value = true;
  saved.value = false;
  error.value = null;

  try {
    await updateProfile({
      firstName: firstName.value.trim(),
      lastName: lastName.value.trim(),
      phone: phone.value.trim(),
    });

    await authStore.loadProfile();
    saved.value = true;
    setTimeout(() => (saved.value = false), 3000);
  } catch (e: unknown) {
    error.value = apiMessage(
      e,
      "Vos informations n'ont pas pu être enregistrées.",
    );
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  firstName.value = authStore.profile?.firstName ?? "";
  lastName.value = authStore.profile?.lastName ?? "";
  phone.value = authStore.profile?.phone ?? "";
});
</script>

<template>
  <section>
    <h2 class="section-title">Informations personnelles</h2>

    <form class="form" @submit.prevent="handleSubmit">
      <div class="form__row">
        <label class="field">
          <span class="field__label">Prénom</span>
          <input v-model="firstName" type="text" autocomplete="given-name" />
        </label>

        <label class="field">
          <span class="field__label">Nom</span>
          <input v-model="lastName" type="text" autocomplete="family-name" />
        </label>
      </div>

      <label class="field">
        <span class="field__label">Téléphone</span>
        <input v-model="phone" type="tel" autocomplete="tel" />
      </label>

      <label class="field">
        <span class="field__label">Adresse email</span>
        <input :value="authStore.profile?.email" type="email" disabled />
        <span class="field__hint"
          >L'adresse email ne peut pas être modifiée.</span
        >
      </label>

      <p v-if="saved" class="form__notice" role="status">
        Vos informations ont été enregistrées.
      </p>

      <p v-if="error" class="form__error" role="alert">{{ error }}</p>

      <button type="submit" class="form__submit" :disabled="saving">
        {{ saving ? "Enregistrement" : "Enregistrer" }}
      </button>
    </form>
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

.form {
  display: grid;
  gap: 18px;
  max-width: 480px;
}

.form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field {
  display: grid;
  gap: 5px;
  min-width: 0;
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

.field input:disabled {
  background: #f5f4f1;
  color: var(--color-text-muted);
}

.field__hint {
  color: var(--color-text-muted);
  font-size: 11.5px;
}

.form__notice {
  margin: 0;
  padding: 11px 13px;
  background: #f5f4f1;
  font-size: 13px;
}

.form__error {
  margin: 0;
  color: var(--color-error);
  font-size: 13px;
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

@media (max-width: 767px) {
  .form__row {
    grid-template-columns: 1fr;
  }
}
</style>
