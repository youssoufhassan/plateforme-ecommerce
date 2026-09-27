<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";

import { apiMessage } from "@/services/productService";
import { resendVerification } from "@/services/authService";
import {
  checkout,
  createAddress,
  createPaymentSession,
  fetchAddresses,
  fetchShippingCountries,
} from "@/services/orderService";
import { useAuthStore } from "@/stores/authStore";
import { useCartStore } from "@/stores/cartStore";
import type { Address, AddressPayload } from "@/types/order";

const router = useRouter();
const authStore = useAuthStore();
const cartStore = useCartStore();

const addresses = ref<Address[]>([]);
const countries = ref<string[]>([]);
const selectedAddressId = ref<string | null>(null);
const showAddressForm = ref(false);

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

const acceptTerms = ref(false);

const loading = ref(true);
const submitting = ref(false);
const savingAddress = ref(false);
const error = ref<string | null>(null);
const verificationSent = ref(false);

const cart = computed(() => cartStore.cart);

/** Noms de pays en français, à partir des codes fournis par le backend. */
const countryNames = new Intl.DisplayNames(["fr"], { type: "region" });

function countryLabel(code: string): string {
  try {
    return countryNames.of(code) ?? code;
  } catch {
    return code;
  }
}

function money(value: number | null | undefined): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(value ?? 0);
}

function formatAddress(address: Address): string {
  const parts = [
    `${address.firstName} ${address.lastName}`.trim(),
    address.street,
    address.complement,
    `${address.postalCode} ${address.city}`,
    countryLabel(address.countryCode),
  ];
  return parts.filter(Boolean).join(", ");
}

/** Toutes les conditions exigées par le backend, vérifiées avant l'envoi. */
const canOrder = computed(
  () =>
    authStore.emailVerified &&
    !!selectedAddressId.value &&
    acceptTerms.value &&
    !cartStore.isEmpty &&
    !cartStore.checkoutBlocked &&
    !submitting.value,
);

async function loadData(): Promise<void> {
  loading.value = true;
  error.value = null;

  try {
    await cartStore.load();

    const [addressList, countryList] = await Promise.all([
      fetchAddresses(),
      fetchShippingCountries(),
    ]);

    addresses.value = addressList;
    countries.value = countryList;

    if (addressList.length > 0) {
      selectedAddressId.value = addressList[0].id;
    } else {
      showAddressForm.value = true;
    }
  } catch (e: unknown) {
    error.value = apiMessage(
      e,
      "Le formulaire de commande n'a pas pu être chargé.",
    );
  } finally {
    loading.value = false;
  }
}

async function handleSaveAddress(): Promise<void> {
  savingAddress.value = true;
  error.value = null;

  try {
    const created = await createAddress({
      ...form.value,
      complement: form.value.complement || undefined,
      phone: form.value.phone || undefined,
    });

    addresses.value = [...addresses.value, created];
    selectedAddressId.value = created.id;
    showAddressForm.value = false;
  } catch (e: unknown) {
    error.value = apiMessage(e, "L'adresse n'a pas pu être enregistrée.");
  } finally {
    savingAddress.value = false;
  }
}

async function handleResendVerification(): Promise<void> {
  error.value = null;

  try {
    await resendVerification();
    verificationSent.value = true;
  } catch (e: unknown) {
    error.value = apiMessage(e, "L'email n'a pas pu être renvoyé.");
  }
}

/**
 * Deux appels successifs : la commande est créée, puis la session de paiement.
 * Le client est ensuite envoyé sur la page Stripe.
 */
async function handleCheckout(): Promise<void> {
  if (!canOrder.value || !selectedAddressId.value) return;

  submitting.value = true;
  error.value = null;

  try {
    const order = await checkout(selectedAddressId.value);
    const session = await createPaymentSession(order.id);

    window.location.href = session.checkoutUrl;
  } catch (e: unknown) {
    error.value = apiMessage(e, "La commande n'a pas pu être validée.");
    submitting.value = false;

    // Le panier a pu changer entre-temps : on resynchronise
    await cartStore.load();
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.replace("/connexion?redirect=/commande");
    return;
  }

  await authStore.loadProfile();
  await loadData();
});
</script>

<template>
  <main class="checkout">
    <div class="container">
      <h1 class="checkout__title">Finaliser ma commande</h1>

      <div v-if="loading" class="checkout__loading">Chargement</div>

      <!-- Panier vide : inutile d'aller plus loin -->
      <div v-else-if="cartStore.isEmpty" class="checkout__empty">
        <p>Votre panier est vide.</p>
        <RouterLink to="/produits" class="checkout__empty-action">
          Découvrir le catalogue
        </RouterLink>
      </div>

      <div v-else class="checkout__layout">
        <div class="checkout__steps">
          <!-- Vérification de l'email -->
          <section v-if="!authStore.emailVerified" class="step step--blocking">
            <h2 class="step__title">Confirmez votre adresse email</h2>

            <p class="step__text">
              Un email de confirmation a été envoyé à
              <strong>{{ authStore.profile?.email }}</strong
              >. Vous pourrez commander dès que votre adresse sera vérifiée.
            </p>

            <p v-if="verificationSent" class="step__notice" role="status">
              Un nouvel email vient de vous être envoyé.
            </p>

            <button
              v-else
              type="button"
              class="step__secondary"
              @click="handleResendVerification"
            >
              Renvoyer l'email
            </button>
          </section>

          <!-- Adresse de livraison -->
          <section
            class="step"
            :class="{ 'step--muted': !authStore.emailVerified }"
          >
            <h2 class="step__title">Adresse de livraison</h2>

            <div v-if="addresses.length && !showAddressForm" class="addresses">
              <label
                v-for="address in addresses"
                :key="address.id"
                class="address"
                :class="{
                  'address--selected': selectedAddressId === address.id,
                }"
              >
                <input
                  v-model="selectedAddressId"
                  type="radio"
                  name="address"
                  :value="address.id"
                />
                <span class="address__text">{{ formatAddress(address) }}</span>
              </label>

              <button
                type="button"
                class="step__secondary"
                @click="showAddressForm = true"
              >
                Utiliser une autre adresse
              </button>
            </div>

            <!-- Saisie d'une nouvelle adresse -->
            <form
              v-else
              class="address-form"
              @submit.prevent="handleSaveAddress"
            >
              <div class="address-form__row">
                <label class="field">
                  <span class="field__label">Prénom</span>
                  <input
                    v-model="form.firstName"
                    type="text"
                    autocomplete="given-name"
                    required
                  />
                </label>

                <label class="field">
                  <span class="field__label">Nom</span>
                  <input
                    v-model="form.lastName"
                    type="text"
                    autocomplete="family-name"
                    required
                  />
                </label>
              </div>

              <label class="field">
                <span class="field__label">Adresse</span>
                <input
                  v-model="form.street"
                  type="text"
                  autocomplete="street-address"
                  required
                />
              </label>

              <label class="field">
                <span class="field__label">Complément (facultatif)</span>
                <input v-model="form.complement" type="text" />
              </label>

              <div class="address-form__row">
                <label class="field field--small">
                  <span class="field__label">Code postal</span>
                  <input
                    v-model="form.postalCode"
                    type="text"
                    autocomplete="postal-code"
                    required
                  />
                </label>

                <label class="field">
                  <span class="field__label">Ville</span>
                  <input
                    v-model="form.city"
                    type="text"
                    autocomplete="address-level2"
                    required
                  />
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
                <input v-model="form.phone" type="tel" autocomplete="tel" />
                <span class="field__hint"
                  >Utile en cas de problème de livraison</span
                >
              </label>

              <div class="address-form__actions">
                <button
                  type="submit"
                  class="step__primary"
                  :disabled="savingAddress"
                >
                  {{
                    savingAddress
                      ? "Enregistrement"
                      : "Enregistrer cette adresse"
                  }}
                </button>

                <button
                  v-if="addresses.length"
                  type="button"
                  class="step__secondary"
                  @click="showAddressForm = false"
                >
                  Annuler
                </button>
              </div>
            </form>
          </section>

          <!-- Conditions générales -->
          <section class="step" :class="{ 'step--muted': !selectedAddressId }">
            <h2 class="step__title">Conditions de vente</h2>

            <label class="terms">
              <input v-model="acceptTerms" type="checkbox" />
              <span>
                J'accepte les
                <RouterLink to="/legal/cgv" target="_blank">
                  conditions générales de vente
                </RouterLink>
                et la
                <RouterLink to="/legal/confidentialite" target="_blank">
                  politique de confidentialité </RouterLink
                >.
              </span>
            </label>

            <p class="step__legal">
              Vous disposez d'un délai de rétractation de 14 jours à compter de
              la réception de votre commande.
            </p>
          </section>
        </div>

        <!-- Récapitulatif -->
        <aside class="summary">
          <h2 class="summary__title">Votre commande</h2>

          <ul class="summary__items">
            <li v-for="item in cartStore.items" :key="item.itemId">
              <span class="summary__item-name">
                {{ item.quantity }} × {{ item.productName }}
                <span
                  v-if="item.variantLabel && item.variantLabel !== 'Standard'"
                >
                  — {{ item.variantLabel }}
                </span>
              </span>
              <span class="summary__item-price">
                {{ money(item.unitPrice * item.quantity) }}
              </span>
            </li>
          </ul>

          <dl class="summary__lines">
            <div>
              <dt>Sous-total</dt>
              <dd>{{ money(cart?.subtotal) }}</dd>
            </div>
            <div>
              <dt>Livraison</dt>
              <dd>
                <span v-if="cart?.shipping === 0">Offerte</span>
                <span v-else>{{ money(cart?.shipping) }}</span>
              </dd>
            </div>
          </dl>

          <div class="summary__total">
            <span>Total</span>
            <strong>{{ money(cart?.total) }}</strong>
          </div>

          <p class="summary__vat">
            dont {{ money(cart?.vat) }} de TVA ({{ cart?.vatRate }} %)
          </p>

          <p v-if="error" class="summary__error" role="alert">{{ error }}</p>

          <button
            type="button"
            class="summary__pay"
            :disabled="!canOrder"
            @click="handleCheckout"
          >
            {{
              submitting ? "Redirection vers le paiement" : "Payer par carte"
            }}
          </button>

          <p v-if="!canOrder && !submitting" class="summary__blocked">
            <span v-if="!authStore.emailVerified">
              Confirmez votre adresse email pour continuer.
            </span>
            <span v-else-if="!selectedAddressId">
              Choisissez une adresse de livraison.
            </span>
            <span v-else-if="cartStore.checkoutBlocked">
              Corrigez votre panier avant de commander.
            </span>
            <span v-else-if="!acceptTerms">
              Acceptez les conditions générales de vente.
            </span>
          </p>

          <p class="summary__secure">
            Paiement sécurisé par Stripe. Vos données bancaires ne transitent
            pas par nos serveurs.
          </p>

          <RouterLink to="/panier" class="summary__back">
            Modifier mon panier
          </RouterLink>
        </aside>
      </div>
    </div>
  </main>
</template>

<style scoped>
.checkout {
  width: 100%;
  padding-block: 28px 72px;
}

.checkout__title {
  margin: 0 0 28px;
  font-family: var(--font-display);
  font-size: clamp(26px, 3.2vw, 38px);
  font-weight: 400;
  line-height: 1.05;
  letter-spacing: -0.03em;
}

.checkout__loading,
.checkout__empty {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: 16px;
  min-height: 280px;
  color: var(--color-text-muted);
  font-size: 14px;
}

.checkout__empty-action {
  padding: 12px 24px;
  background: var(--color-black);
  color: var(--color-white);
  font-size: 12px;
  text-decoration: none;
}

.checkout__layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 36px;
}

.checkout__steps {
  display: grid;
  gap: 24px;
  min-width: 0;
}

/* =========================================================
   ÉTAPES
   ========================================================= */

.step {
  padding: 24px;
  border: 1px solid var(--color-border);
  background: var(--color-white);
}

/* Une étape dont la précédente n'est pas franchie */
.step--muted {
  opacity: 0.55;
}

/* Une étape qui empêche la commande */
.step--blocking {
  border-color: var(--color-text);
  border-left-width: 2px;
}

.step__title {
  margin: 0 0 16px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.step__text {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.55;
}

.step__notice {
  margin: 14px 0 0;
  padding: 10px 12px;
  background: #f5f4f1;
  font-size: 13px;
}

.step__legal {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
  line-height: 1.5;
}

.step__primary {
  height: 44px;
  padding-inline: 22px;
  border: none;
  background: var(--color-black);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 12px;
}

.step__primary:disabled {
  cursor: wait;
  opacity: 0.5;
}

.step__secondary {
  margin-top: 14px;
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

.step__secondary:hover {
  color: var(--color-text);
}

/* =========================================================
   ADRESSES
   ========================================================= */

.addresses {
  display: grid;
  gap: 10px;
}

.address {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  padding: 14px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.address:hover {
  border-color: var(--color-border-strong);
}

.address--selected {
  border-color: var(--color-text);
}

.address input {
  margin-top: 2px;
  accent-color: var(--color-black);
}

.address__text {
  font-size: 13.5px;
  line-height: 1.5;
}

/* =========================================================
   FORMULAIRE D'ADRESSE
   ========================================================= */

.address-form {
  display: grid;
  gap: 16px;
}

.address-form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.field--small {
  max-width: 140px;
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

.field__hint {
  color: var(--color-text-muted);
  font-size: 11.5px;
}

.address-form__actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

.address-form__actions .step__secondary {
  margin-top: 0;
}

/* =========================================================
   CONDITIONS
   ========================================================= */

.terms {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  cursor: pointer;
  font-size: 13.5px;
  line-height: 1.55;
}

.terms input {
  margin-top: 3px;
  width: 16px;
  height: 16px;
  accent-color: var(--color-black);
  cursor: pointer;
}

.terms a {
  color: var(--color-text);
}

/* =========================================================
   RÉCAPITULATIF
   ========================================================= */

.summary {
  padding: 22px;
  border: 1px solid var(--color-border);
  background: var(--color-white);
}

.summary__title {
  margin: 0 0 16px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.summary__items {
  display: grid;
  gap: 10px;
  margin: 0 0 18px;
  padding: 0 0 18px;
  border-bottom: 1px solid var(--color-border);
  list-style: none;
}

.summary__items li {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  font-size: 13px;
  line-height: 1.4;
}

.summary__item-name {
  min-width: 0;
}

.summary__item-price {
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}

.summary__lines {
  display: grid;
  gap: 9px;
  margin: 0;
}

.summary__lines > div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 13.5px;
}

.summary__lines dt {
  color: var(--color-text-muted);
}

.summary__lines dd {
  margin: 0;
  font-variant-numeric: tabular-nums;
}

.summary__total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-top: 16px;
  padding-top: 15px;
  border-top: 1px solid var(--color-border);
  font-size: 15px;
}

.summary__total strong {
  font-size: 19px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.summary__vat {
  margin: 5px 0 0;
  color: var(--color-text-muted);
  font-size: 11.5px;
}

.summary__error {
  margin: 14px 0 0;
  color: var(--color-error);
  font-size: 13px;
  line-height: 1.45;
}

.summary__pay {
  width: 100%;
  margin-top: 18px;
  padding: 16px;
  border: none;
  background: var(--color-black);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 12.5px;
  letter-spacing: 0.06em;
  transition: opacity var(--transition-fast);
}

.summary__pay:hover:not(:disabled) {
  opacity: 0.85;
}

.summary__pay:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.summary__blocked {
  margin: 10px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
  text-align: center;
}

.summary__secure {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  font-size: 11px;
  line-height: 1.45;
  text-align: center;
}

.summary__back {
  display: block;
  margin-top: 14px;
  color: var(--color-text-muted);
  font-size: 12px;
  text-align: center;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.summary__back:hover {
  color: var(--color-text);
}

/* =========================================================
   DESKTOP
   ========================================================= */

@media (min-width: 1001px) {
  .checkout__layout {
    grid-template-columns: minmax(0, 1fr) 360px;
    gap: 48px;
    align-items: start;
  }

  .summary {
    position: sticky;
    top: calc(var(--header-height) + 24px);
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .step {
    padding: 18px;
  }

  .address-form__row {
    grid-template-columns: 1fr;
  }

  .field--small {
    max-width: none;
  }
}
</style>
