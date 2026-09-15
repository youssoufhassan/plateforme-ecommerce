<script setup lang="ts">
import { onMounted, computed, ref } from "vue";
import { useRouter } from "vue-router";

import api from "../services/api";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";

const cartStore = useCartStore();
const authStore = useAuthStore();
const router = useRouter();

const updatingItem = ref<string | null>(null);
const removingItem = ref<string | null>(null);
const checkoutLoading = ref(false);
const checkoutError = ref<string | null>(null);

/* =========================================
   INITIALISATION
========================================= */

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.replace({
      path: "/login",
      query: {
        redirect: "/cart",
      },
    });

    return;
  }

  try {
    await cartStore.loadCart();
  } catch (error) {
    console.error("Impossible de charger le panier :", error);
  }
});

/* =========================================
   COMPUTED
========================================= */

const cartItemsCount = computed(() => {
  return cartStore.cart.items.reduce((total, item) => total + item.quantity, 0);
});

const formattedTotal = computed(() => {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(Number(cartStore.cart.total || 0));
});

const isEmpty = computed(() => {
  return cartStore.cart.items.length === 0;
});

/* =========================================
   QUANTITÉ
========================================= */

async function updateQuantity(itemId: string, quantity: number) {
  if (quantity < 1) {
    return;
  }

  if (updatingItem.value || removingItem.value) {
    return;
  }

  updatingItem.value = itemId;

  try {
    await cartStore.updateItem(itemId, quantity);
  } catch (error) {
    console.error("Erreur lors de la modification de la quantité :", error);
  } finally {
    updatingItem.value = null;
  }
}

/* =========================================
   RETIRER
========================================= */

async function removeItem(itemId: string) {
  if (removingItem.value || updatingItem.value) {
    return;
  }

  removingItem.value = itemId;

  try {
    await cartStore.removeItem(itemId);
  } catch (error) {
    console.error("Erreur lors de la suppression :", error);
  } finally {
    removingItem.value = null;
  }
}

/* =========================================
   CHECKOUT
========================================= */

async function handleCheckout() {
  if (checkoutLoading.value || isEmpty.value) {
    return;
  }

  checkoutLoading.value = true;
  checkoutError.value = null;

  try {
    /*
     * 1. Création de la commande
     */
    const checkoutResponse = await api.post("/orders/checkout");

    const orderId = checkoutResponse.data?.id;

    if (!orderId) {
      throw new Error(
        "La commande a été créée mais son identifiant est introuvable.",
      );
    }

    /*
     * 2. Paiement
     */
    try {
      await api.post(`/orders/${orderId}/pay`);
    } catch (paymentError: any) {
      const paymentMessage =
        paymentError?.response?.data?.message ||
        "Le paiement de la commande a échoué.";

      checkoutError.value = paymentMessage;

      /*
       * La commande existe déjà.
       * On redirige donc vers le compte afin que
       * l'utilisateur puisse retrouver sa commande.
       */
      setTimeout(() => {
        router.push("/account");
      }, 1200);

      return;
    }

    /*
     * 3. Succès
     */
    await cartStore.loadCart();

    router.push({
      path: "/account",
      query: {
        order: orderId,
        success: "true",
      },
    });
  } catch (error: any) {
    console.error("Erreur lors du checkout :", error);

    checkoutError.value =
      error?.response?.data?.message ||
      error?.message ||
      "Impossible de passer la commande. Veuillez réessayer.";
  } finally {
    checkoutLoading.value = false;
  }
}

/* =========================================
   NAVIGATION
========================================= */

function goToProducts() {
  router.push("/produits");
}
</script>

<template>
  <main class="cart-page">
    <div class="container cart-container">
      <!-- =================================
           HEADER
      ================================== -->

      <header class="cart-header">
        <div>
          <span class="cart-eyebrow"> VOTRE SÉLECTION </span>

          <h1>Mon panier</h1>

          <p>Retrouvez les produits que vous avez sélectionnés.</p>
        </div>
      </header>

      <!-- =================================
           LOADING
      ================================== -->

      <div
        v-if="cartStore.loading && !cartStore.cart.items.length"
        class="cart-loading"
        aria-label="Chargement du panier"
      >
        <div class="cart-loading-line"></div>
        <div class="cart-loading-line"></div>
        <div class="cart-loading-line"></div>
      </div>

      <!-- =================================
           PANIER VIDE
      ================================== -->

      <section v-else-if="isEmpty" class="cart-empty">
        <div class="cart-empty-icon">
          <svg
            width="42"
            height="42"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            aria-hidden="true"
          >
            <path d="M6 8h12l1 12H5L6 8Z" />
            <path d="M9 8a3 3 0 0 1 6 0" />
          </svg>
        </div>

        <h2>Ton panier est vide</h2>

        <p>
          Découvre notre sélection de parfums et ajoute tes produits préférés à
          ton panier.
        </p>

        <button type="button" class="cart-primary-button" @click="goToProducts">
          Découvrir les produits
        </button>
      </section>

      <!-- =================================
           PANIER
      ================================== -->

      <section v-else class="cart-layout">
        <!-- =================================
             PRODUITS
        ================================== -->

        <div class="cart-products">
          <div class="cart-products-header">
            <h2>
              Produits

              <span>
                {{ cartItemsCount }}
              </span>
            </h2>
          </div>

          <article
            v-for="item in cartStore.cart.items"
            :key="item.itemId"
            class="cart-item"
            :class="{
              'is-updating':
                updatingItem === item.itemId || removingItem === item.itemId,
            }"
          >
            <!-- IMAGE -->
            <div class="cart-item-image">
              <span>SHAHIN</span>
            </div>

            <!-- INFORMATIONS -->
            <div class="cart-item-info">
              <h3>
                {{ item.productName }}
              </h3>

              <p class="cart-item-price">
                {{
                  new Intl.NumberFormat("fr-FR", {
                    style: "currency",
                    currency: "EUR",
                  }).format(Number(item.unitPrice))
                }}
                <span> / unité</span>
              </p>

              <div class="cart-item-actions">
                <!-- QUANTITÉ -->

                <div class="quantity-control" aria-label="Modifier la quantité">
                  <button
                    type="button"
                    aria-label="Diminuer la quantité"
                    :disabled="
                      updatingItem === item.itemId ||
                      removingItem === item.itemId ||
                      item.quantity <= 1
                    "
                    @click="updateQuantity(item.itemId, item.quantity - 1)"
                  >
                    −
                  </button>

                  <span>
                    {{ item.quantity }}
                  </span>

                  <button
                    type="button"
                    aria-label="Augmenter la quantité"
                    :disabled="
                      updatingItem === item.itemId ||
                      removingItem === item.itemId
                    "
                    @click="updateQuantity(item.itemId, item.quantity + 1)"
                  >
                    +
                  </button>
                </div>

                <!-- RETIRER -->

                <button
                  type="button"
                  class="remove-button"
                  :disabled="
                    updatingItem === item.itemId || removingItem === item.itemId
                  "
                  @click="removeItem(item.itemId)"
                >
                  <span v-if="removingItem === item.itemId">
                    Suppression...
                  </span>

                  <span v-else> Retirer </span>
                </button>
              </div>
            </div>

            <!-- SOUS-TOTAL -->

            <div class="cart-item-total">
              {{
                new Intl.NumberFormat("fr-FR", {
                  style: "currency",
                  currency: "EUR",
                }).format(Number(item.unitPrice) * Number(item.quantity))
              }}
            </div>
          </article>
        </div>

        <!-- =================================
             RÉSUMÉ
        ================================== -->

        <aside class="cart-summary">
          <h2>Résumé de la commande</h2>

          <div class="summary-row">
            <span> Sous-total </span>

            <strong>
              {{ formattedTotal }}
            </strong>
          </div>

          <div class="summary-row">
            <span> Livraison </span>

            <span class="summary-muted"> À calculer </span>
          </div>

          <div class="summary-divider"></div>

          <div class="summary-total">
            <span> Total </span>

            <strong>
              {{ formattedTotal }}
            </strong>
          </div>

          <!-- ERREUR CHECKOUT -->

          <div v-if="checkoutError" class="checkout-error" role="alert">
            {{ checkoutError }}
          </div>

          <!-- CHECKOUT -->

          <button
            type="button"
            class="checkout-button"
            :disabled="checkoutLoading || cartStore.loading || isEmpty"
            @click="handleCheckout"
          >
            <span v-if="checkoutLoading"> Traitement... </span>

            <span v-else> Passer la commande </span>
          </button>

          <!-- CONTINUER -->

          <button type="button" class="continue-shopping" @click="goToProducts">
            Continuer mes achats
          </button>
        </aside>
      </section>
    </div>
  </main>
</template>

<style scoped>
.cart-page {
  min-height: calc(100vh - 80px);
  padding: 3.5rem 0 5rem;
  background: var(--surface, #fafafa);
  color: var(--ink, #171717);
}

.cart-container {
  max-width: 1180px;
  margin: 0 auto;
}

/* ================================
   HEADER
================================ */

.cart-header {
  margin-bottom: 2.5rem;
}

.cart-eyebrow {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--ink-soft, #777);
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.16em;
}

.cart-header h1 {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 600;
  letter-spacing: -0.04em;
}

.cart-header p {
  margin: 0.7rem 0 0;
  color: var(--ink-soft, #777);
  font-size: 0.95rem;
}

/* ================================
   LAYOUT
================================ */

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 350px;
  gap: 2rem;
  align-items: start;
}

/* ================================
   PRODUCTS
================================ */

.cart-products {
  border: 1px solid var(--border, #e5e5e5);
  border-radius: var(--radius-lg, 14px);
  background: #fff;
  overflow: hidden;
}

.cart-products-header {
  padding: 1.3rem 1.5rem;
  border-bottom: 1px solid var(--border, #e5e5e5);
}

.cart-products-header h2 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.cart-products-header h2 span {
  margin-left: 0.35rem;
  color: var(--ink-soft, #888);
  font-weight: 400;
}

/* ================================
   ITEM
================================ */

.cart-item {
  display: grid;
  grid-template-columns: 100px minmax(0, 1fr) auto;
  gap: 1.25rem;
  align-items: center;
  padding: 1.4rem 1.5rem;
  border-bottom: 1px solid var(--border, #e5e5e5);
  transition: opacity 0.2s ease;
}

.cart-item:last-child {
  border-bottom: 0;
}

.cart-item.is-updating {
  opacity: 0.55;
}

.cart-item-image {
  width: 100px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: var(--surface-soft, #f3f3f3);
  color: var(--ink-soft, #999);
  font-size: 0.62rem;
  font-weight: 600;
  letter-spacing: 0.12em;
}

.cart-item-info {
  min-width: 0;
}

.cart-item-info h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.cart-item-price {
  margin: 0.35rem 0 1rem;
  color: var(--ink-soft, #777);
  font-size: 0.88rem;
}

.cart-item-price span {
  color: var(--ink-soft, #999);
  font-size: 0.78rem;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* ================================
   QUANTITY
================================ */

.quantity-control {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border, #ddd);
  border-radius: 7px;
  overflow: hidden;
  background: #fff;
}

.quantity-control button {
  width: 32px;
  height: 32px;
  border: 0;
  background: #fff;
  color: var(--ink, #171717);
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.15s ease;
}

.quantity-control button:hover:not(:disabled) {
  background: var(--surface-soft, #f5f5f5);
}

.quantity-control button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.quantity-control span {
  min-width: 32px;
  text-align: center;
  font-size: 0.85rem;
  font-weight: 500;
}

/* ================================
   REMOVE
================================ */

.remove-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--ink-soft, #777);
  font-size: 0.78rem;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.remove-button:hover:not(:disabled) {
  color: var(--ink, #111);
}

.remove-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ================================
   ITEM TOTAL
================================ */

.cart-item-total {
  align-self: start;
  padding-top: 0.2rem;
  white-space: nowrap;
  font-size: 0.95rem;
  font-weight: 600;
}

/* ================================
   SUMMARY
================================ */

.cart-summary {
  position: sticky;
  top: 1.5rem;
  padding: 1.5rem;
  border: 1px solid var(--border, #e5e5e5);
  border-radius: var(--radius-lg, 14px);
  background: #fff;
}

.cart-summary h2 {
  margin: 0 0 1.5rem;
  font-size: 1rem;
  font-weight: 600;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.8rem;
  color: var(--ink-soft, #777);
  font-size: 0.85rem;
}

.summary-row strong {
  color: var(--ink, #171717);
}

.summary-muted {
  color: var(--ink-soft, #999);
}

.summary-divider {
  height: 1px;
  margin: 1.2rem 0;
  background: var(--border, #e5e5e5);
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.3rem;
}

.summary-total span {
  font-size: 0.95rem;
  font-weight: 500;
}

.summary-total strong {
  font-size: 1.2rem;
}

/* ================================
   CHECKOUT ERROR
================================ */

.checkout-error {
  margin-bottom: 1rem;
  padding: 0.75rem 0.85rem;
  border: 1px solid #e3caca;
  border-radius: 8px;
  background: #faf3f3;
  color: #8a3333;
  font-size: 0.8rem;
  line-height: 1.5;
}

/* ================================
   BUTTONS
================================ */

.checkout-button,
.cart-primary-button {
  width: 100%;
  padding: 0.85rem 1rem;
  border: 1px solid var(--ink, #171717);
  border-radius: var(--radius, 8px);
  background: var(--ink, #171717);
  color: #fff;
  font-size: 0.86rem;
  font-weight: 500;
  cursor: pointer;
  transition:
    opacity 0.2s ease,
    transform 0.15s ease;
}

.checkout-button:hover:not(:disabled),
.cart-primary-button:hover {
  opacity: 0.88;
}

.checkout-button:active:not(:disabled),
.cart-primary-button:active {
  transform: translateY(1px);
}

.checkout-button:disabled {
  opacity: 0.5;
  cursor: wait;
}

.continue-shopping {
  width: 100%;
  margin-top: 0.75rem;
  padding: 0.8rem 1rem;
  border: 1px solid var(--border, #ddd);
  border-radius: var(--radius, 8px);
  background: #fff;
  color: var(--ink, #171717);
  font-size: 0.82rem;
  cursor: pointer;
}

.continue-shopping:hover {
  background: var(--surface-soft, #f5f5f5);
}

/* ================================
   EMPTY
================================ */

.cart-empty {
  max-width: 520px;
  margin: 3rem auto;
  padding: 3rem 2rem;
  border: 1px solid var(--border, #e5e5e5);
  border-radius: var(--radius-lg, 14px);
  background: #fff;
  text-align: center;
}

.cart-empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  margin: 0 auto 1.5rem;
  border-radius: 50%;
  background: var(--surface-soft, #f3f3f3);
  color: var(--ink-soft, #777);
}

.cart-empty h2 {
  margin: 0;
  font-size: 1.3rem;
}

.cart-empty p {
  margin: 0.7rem auto 1.5rem;
  max-width: 390px;
  color: var(--ink-soft, #777);
  font-size: 0.88rem;
  line-height: 1.6;
}

/* ================================
   LOADING
================================ */

.cart-loading {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-loading-line {
  height: 110px;
  border-radius: var(--radius-lg, 14px);
  background: var(--surface-soft, #f2f2f2);
  animation: cart-loading 1.2s ease-in-out infinite;
}

@keyframes cart-loading {
  0%,
  100% {
    opacity: 0.5;
  }

  50% {
    opacity: 1;
  }
}

/* ================================
   RESPONSIVE
================================ */

@media (max-width: 850px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }

  .cart-summary {
    position: static;
  }
}

@media (max-width: 600px) {
  .cart-page {
    padding: 2rem 0 3rem;
  }

  .cart-item {
    grid-template-columns: 70px minmax(0, 1fr);
    gap: 1rem;
    padding: 1rem;
  }

  .cart-item-image {
    width: 70px;
    height: 90px;
  }

  .cart-item-total {
    grid-column: 2;
    padding-top: 0;
  }

  .cart-item-actions {
    flex-wrap: wrap;
  }

  .cart-products-header {
    padding: 1rem;
  }
}
</style>
