<script setup lang="ts">
import { onMounted, ref } from "vue";

import { apiMessage } from "@/services/productService";
import {
  createAddress,
  fetchAddresses,
  fetchShippingCountries,
} from "@/services/orderService";
import type { Address, AddressPayload } from "@/types/order";

const addresses = ref<Address[]>([]);
const countries = ref<string[]>([]);
const showForm = ref(false);

const form = ref<AddressPayload>({
  firstName: "",
  lastName: "",
  street: "",
  complement: "",
  city: "",
  postalCode: "",
  countryCode: "FR",
  phone: "",
});

const loading = ref(true);
const saving = ref(false);
const error = ref<string | null>(null);

const countryNames = new Intl.DisplayNames(["fr"], { type: "region" });

function countryLabel(code: string): string {
  try {
    return countryNames.of(code) ?? code;
  } catch {
    return code;
  }
}

async function handleSubmit(): Promise<void> {
  saving.value = true;
  error.value = null;

  try {
    const created = await createAddress({
      ...form.value,
      complement: form.value.complement || undefined,
      phone: form.value.phone || undefined,
    });

    addresses.value = [...addresses.value, created];
    showForm.value = false;

    form.value = {
      firstName: "",
      lastName: "",
      street: "",
      complement: "",
      city: "",
      postalCode: "",
      countryCode: "FR",
      phone: "",
    };
  } catch (e: unknown) {
    error.value = apiMessage(e, "L'adresse n'a pas pu être enregistrée.");
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  try {
    const [list, countryList] = await Promise.all([
      fetchAddresses(),
      fetchShippingCountries(),
    ]);
    addresses.value = list;
    countries.value = countryList;
  } catch (e: unknown) {
    error.value = apiMessage(e, "Vos adresses n'ont pas pu être chargées.");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section>
    <h2 class="section-title">Mes adresses</h2>

    <p v-if="loading" class="state">Chargement</p>

    <template v-else>
      <ul v-if="addresses.length" class="addresses">
        <li v-for="address in addresses" :key="address.id" class="address">
          <p class="address__name">
            {{ address.firstName }} {{ address.lastName }}
          </p>
          <p class="address__lines">
            {{ address.street }}<br />
            <template v-if="address.complement"
              >{{ address.complement }}<br
            /></template>
            {{ address.postalCode }} {{ address.city }}<br />
            {{ countryLabel(address.countryCode) }}
          </p>
          <p v-if="address.phone" class="address__phone">{{ address.phone }}</p>
        </li>
      </ul>

      <p v-else class="state">Vous n'avez pas encore enregistré d'adresse.</p>

      <p v-if="error" class="form__error" role="alert">{{ error }}</p>

      <button
        v-if="!showForm"
        type="button"
        class="form__submit"
        @click="showForm = true"
      >
        Ajouter une adresse
      </button>

      <form v-else class="form" @submit.prevent="handleSubmit">
        <div class="form__row">
          <label class="field">
            <span class="field__label">Prénom</span>
            <input v-model="form.firstName" type="text" required />
          </label>

          <label class="field">
            <span class="field__label">Nom</span>
            <input v-model="form.lastName" type="text" required />
          </label>
        </div>

        <label class="field">
          <span class="field__label">Adresse</span>
          <input v-model="form.street" type="text" required />
        </label>

        <label class="field">
          <span class="field__label">Complément (facultatif)</span>
          <input v-model="form.complement" type="text" />
        </label>

        <div class="form__row">
          <label class="field">
            <span class="field__label">Code postal</span>
            <input v-model="form.postalCode" type="text" required />
          </label>

          <label class="field">
            <span class="field__label">Ville</span>
            <input v-model="form.city" type="text" required />
          </label>
        </div>

        <label class="field">
          <span class="field__label">Pays</span>
          <select v-model="form.countryCode" required>
            <option v-for="code in countries" :key="code" :value="code">
              {{ countryLabel(code) }}
            </option>
          </select>
        </label>

        <label class="field">
          <span class="field__label">Téléphone (facultatif)</span>
          <input v-model="form.phone" type="tel" />
        </label>

        <div class="form__actions">
          <button type="submit" class="form__submit" :disabled="saving">
            {{ saving ? "Enregistrement" : "Enregistrer" }}
          </button>

          <button type="button" class="form__cancel" @click="showForm = false">
            Annuler
          </button>
        </div>
      </form>

      <p class="note">
        Les adresses enregistrées ne peuvent pas être modifiées ni supprimées.
        Ajoutez-en une nouvelle si vos coordonnées changent.
      </p>
    </template>
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

.state {
  margin: 0 0 20px;
  color: var(--color-text-muted);
  font-size: 14px;
}

.addresses {
  display: grid;
  gap: 12px;
  margin: 0 0 22px;
  padding: 0;
  list-style: none;
}

.address {
  padding: 16px;
  border: 1px solid var(--color-border);
  background: var(--color-white);
  max-width: 420px;
}

.address__name {
  margin: 0 0 6px;
  font-size: 13.5px;
  font-weight: 500;
}

.address__lines {
  margin: 0;
  color: var(--color-text);
  font-size: 13px;
  line-height: 1.55;
}

.address__phone {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: 12.5px;
}

.form {
  display: grid;
  gap: 16px;
  max-width: 480px;
  margin-top: 8px;
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

.field input,
.field select {
  height: 44px;
  padding-inline: 12px;
  border: 1px solid var(--color-border-strong);
  background: var(--color-white);
  color: var(--color-text);
  font-family: inherit;
  font-size: 14px;
}

.field input:focus,
.field select:focus {
  border-color: var(--color-text);
  outline: none;
}

.form__actions {
  display: flex;
  align-items: center;
  gap: 18px;
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

.form__cancel {
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

.form__error {
  margin: 0 0 16px;
  color: var(--color-error);
  font-size: 13px;
}

.note {
  margin: 24px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 767px) {
  .form__row {
    grid-template-columns: 1fr;
  }
}
</style>
