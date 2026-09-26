<script setup lang="ts">
import { computed, onMounted } from "vue";
import { RouterLink } from "vue-router";

import { useCartStore } from "@/stores/cartStore";
import { fullImageUrl } from "@/services/productService";
import type { CartItem } from "@/types/cart";

const cartStore = useCartStore();

const cart = computed(() => cartStore.cart);

function money(value: number | null | undefined): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(value ?? 0);
}

function lineTotal(item: CartItem): number {
  return item.unitPrice * item.quantity;
}

function displayName(item: CartItem): string {
  if (!item.variantLabel || item.variantLabel === "Standard")
    return item.productName;
  return `${item.productName} — ${item.variantLabel}`;
}

onMounted(() => cartStore.load());
</script>

<template>
  <main class="cart">
    <div class="container">
      <nav class="cart__breadcrumb" aria-label="Fil d'Ariane">
        <RouterLink to="/">Accueil</RouterLink>
        <span aria-hidden="true">/</span>
        <span>Panier</span>
      </nav>

      <h1 class="cart__title">Votre panier</h1>

      <!-- Chargement -->
      <div v-if="cartStore.loading && !cart" class="cart__loading">
        Chargement du panier
      </div>

      <!-- Panier vide -->
      <div v-else-if="cartStore.isEmpty" class="cart__empty">
        <p class="cart__empty-title">Votre panier est vide.</p>
        <p class="cart__empty-hint">
          Parcourez le catalogue pour trouver votre prochaine fragrance.
        </p>
        <RouterLink to="/produits" class="cart__empty-action">
          Découvrir le catalogue
        </RouterLink>
      </div>

      <template v-else>
        <!-- Avertissements du serveur -->
        <div
          v-if="cartStore.warnings.length"
          class="cart__warnings"
          role="status"
        >
          <p v-for="warning in cartStore.warnings" :key="warning">
            {{ warning }}
          </p>
        </div>

        <p v-if="cartStore.error" class="cart__error" role="alert">
          {{ cartStore.error }}
        </p>

        <div class="cart__layout">
          <!-- Articles -->
          <section class="cart__items" aria-label="Articles du panier">
            <article
              v-for="item in cartStore.items"
              :key="item.itemId"
              class="cart-line"
              :class="{ 'cart-line--unavailable': !item.available }"
            >
              <RouterLink
                :to="`/produits/${item.productId}`"
                class="cart-line__visual"
              >
                <span class="cart-line__initials">
                  {{ item.productName.slice(0, 2).toUpperCase() }}
                </span>
              </RouterLink>

              <div class="cart-line__info">
                <RouterLink
                  :to="`/produits/${item.productId}`"
                  class="cart-line__name"
                >
                  {{ displayName(item) }}
                </RouterLink>

                <p class="cart-line__unit">
                  {{ money(item.unitPrice) }} l'unité
                </p>

                <p v-if="!item.available" class="cart-line__notice">
                  Cet article n'est plus disponible.
                </p>

                <p
                  v-else-if="
                    item.maxQuantity !== null &&
                    item.quantity >= item.maxQuantity
                  "
                  class="cart-line__notice"
                >
                  Dernier{{ item.maxQuantity > 1 ? "s" : "" }} exemplaire{{
                    item.maxQuantity > 1 ? "s" : ""
                  }}
                  en stock.
                </p>

                <button
                  type="button"
                  class="cart-line__remove"
                  @click="cartStore.remove(item)"
                >
                  Retirer
                </button>
              </div>

              <div class="cart-line__controls">
                <div class="quantity">
                  <button
                    type="button"
                    class="quantity__button"
                    aria-label="Diminuer la quantité"
                    :disabled="item.quantity <= 1"
                    @click="cartStore.updateQuantity(item, item.quantity - 1)"
                  >
                    −
                  </button>

                  <span class="quantity__value">{{ item.quantity }}</span>

                  <button
                    type="button"
                    class="quantity__button"
                    aria-label="Augmenter la quantité"
                    :disabled="
                      !item.available ||
                      (item.maxQuantity !== null &&
                        item.quantity >= item.maxQuantity) ||
                      item.quantity >= 20
                    "
                    @click="cartStore.updateQuantity(item, item.quantity + 1)"
                  >
                    +
                  </button>
                </div>

                <p class="cart-line__total">{{ money(lineTotal(item)) }}</p>
              </div>
            </article>
          </section>

          <!-- Récapitulatif -->
          <aside class="summary" aria-label="Récapitulatif">
            <h2 class="summary__title">Récapitulatif</h2>

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

            <p
              v-if="cart && cart.amountUntilFreeShipping > 0"
              class="summary__shipping-hint"
            >
              Plus que {{ money(cart.amountUntilFreeShipping) }} pour la
              livraison offerte.
            </p>

            <div class="summary__total">
              <span>Total</span>
              <strong>{{ money(cart?.total) }}</strong>
            </div>

            <p class="summary__vat">
              dont {{ money(cart?.vat) }} de TVA ({{ cart?.vatRate }} %)
            </p>

            <RouterLink
              v-if="!cartStore.checkoutBlocked"
              to="/commande"
              class="summary__checkout"
            >
              Passer la commande
            </RouterLink>

            <button v-else type="button" class="summary__checkout" disabled>
              Corrigez votre panier pour continuer
            </button>

            <RouterLink to="/produits" class="summary__continue">
              Continuer mes achats
            </RouterLink>

            <p class="summary__note">
              Les frais de livraison définitifs dépendent du pays de livraison,
              indiqué à l'étape suivante.
            </p>
          </aside>
        </div>
      </template>
    </div>
  </main>
</template>

<style scoped>
.cart {
  width: 100%;
  padding-bottom: 72px;
}

.cart__breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 22px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.cart__breadcrumb a {
  color: inherit;
  text-decoration: none;
}

.cart__breadcrumb a:hover {
  color: var(--color-text);
}

.cart__title {
  margin: 18px 0 26px;
  font-family: var(--font-display);
  font-size: clamp(28px, 3.2vw, 40px);
  font-weight: 400;
  line-height: 1.05;
  letter-spacing: -0.03em;
}

/* =========================================================
   ÉTATS
   ========================================================= */

.cart__loading {
  min-height: 240px;
  display: grid;
  place-items: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.cart__empty {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: 12px;
  min-height: 320px;
  text-align: center;
}

.cart__empty-title {
  margin: 0;
  font-size: 16px;
}

.cart__empty-hint {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.cart__empty-action {
  margin-top: 8px;
  padding: 13px 26px;
  background: var(--color-black);
  color: var(--color-white);
  font-size: 12px;
  text-decoration: none;
}

.cart__warnings {
  padding: 14px 16px;
  margin-bottom: 20px;
  border-left: 2px solid var(--color-text);
  background: #f5f4f1;
}

.cart__warnings p {
  margin: 0 0 4px;
  font-size: 13px;
  line-height: 1.45;
}

.cart__warnings p:last-child {
  margin-bottom: 0;
}

.cart__error {
  margin: 0 0 18px;
  color: var(--color-error);
  font-size: 13px;
}

/* =========================================================
   MISE EN PAGE
   ========================================================= */

.cart__layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 36px;
}

/* =========================================================
   LIGNE D'ARTICLE
   ========================================================= */

.cart-line {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 16px;
  padding-block: 20px;
  border-bottom: 1px solid var(--color-border);
}

.cart-line:first-child {
  border-top: 1px solid var(--color-border);
}

.cart-line--unavailable {
  opacity: 0.55;
}

.cart-line__visual {
  display: grid;
  place-items: center;
  aspect-ratio: 1 / 1.1;
  background: #f4f3f1;
  text-decoration: none;
}

.cart-line__initials {
  color: var(--color-text-muted);
  font-size: 18px;
  letter-spacing: 0.12em;
}

.cart-line__info {
  min-width: 0;
}

.cart-line__name {
  display: block;
  color: var(--color-text);
  font-size: 14px;
  line-height: 1.35;
  text-decoration: none;
}

.cart-line__name:hover {
  opacity: 0.65;
}

.cart-line__unit {
  margin: 5px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
}

.cart-line__notice {
  margin: 6px 0 0;
  color: var(--color-text);
  font-size: 12px;
}

.cart-line__remove {
  margin-top: 10px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: 12px;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.cart-line__remove:hover {
  color: var(--color-text);
}

.cart-line__controls {
  grid-column: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 12px;
}

.quantity {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--color-border-strong);
}

.quantity__button {
  width: 34px;
  height: 36px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-size: 15px;
  line-height: 1;
}

.quantity__button:disabled {
  cursor: not-allowed;
  opacity: 0.3;
}

.quantity__value {
  min-width: 28px;
  text-align: center;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.cart-line__total {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

/* =========================================================
   RÉCAPITULATIF
   ========================================================= */

.summary {
  padding: 22px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
}

.summary__title {
  margin: 0 0 18px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.summary__lines {
  display: grid;
  gap: 10px;
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

.summary__shipping-hint {
  margin: 12px 0 0;
  padding: 9px 11px;
  background: #f5f4f1;
  color: var(--color-text);
  font-size: 12px;
  line-height: 1.4;
}

.summary__total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding-top: 16px;
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

.summary__checkout {
  display: block;
  width: 100%;
  margin-top: 20px;
  padding: 15px;
  border: none;
  background: var(--color-black);
  cursor: pointer;
  color: var(--color-white);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.06em;
  text-align: center;
  text-decoration: none;
  transition: opacity var(--transition-fast);
}

.summary__checkout:hover:not(:disabled) {
  opacity: 0.85;
}

.summary__checkout:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.summary__continue {
  display: block;
  margin-top: 12px;
  color: var(--color-text-muted);
  font-size: 12px;
  text-align: center;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.summary__continue:hover {
  color: var(--color-text);
}

.summary__note {
  margin: 16px 0 0;
  color: var(--color-text-muted);
  font-size: 11px;
  line-height: 1.45;
}

/* =========================================================
   DESKTOP
   ========================================================= */

@media (min-width: 1001px) {
  .cart__layout {
    grid-template-columns: minmax(0, 1fr) 340px;
    gap: 48px;
    align-items: start;
  }

  .summary {
    position: sticky;
    top: calc(var(--header-height) + 24px);
  }

  .cart-line {
    grid-template-columns: 96px minmax(0, 1fr) auto;
    align-items: center;
  }

  .cart-line__controls {
    grid-column: 3;
    margin-top: 0;
    gap: 28px;
  }
}
</style>
