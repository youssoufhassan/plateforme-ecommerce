<script setup lang="ts">
import { computed } from "vue";
import { RouterLink } from "vue-router";

import { useCartStore } from "@/stores/cartStore";

const cartStore = useCartStore();

const cart = computed(() => cartStore.cart);

function money(value: number | null | undefined): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(value ?? 0);
}

const addedLabel = computed(() => {
  const added = cartStore.lastAdded;
  if (!added) return "";
  if (!added.label || added.label === "Standard") return added.name;
  return `${added.name} — ${added.label}`;
});
</script>

<template>
  <Teleport to="body">
    <Transition name="cart-panel">
      <div v-if="cartStore.panelOpen" class="cart-panel">
        <div class="cart-panel__backdrop" @click="cartStore.closePanel()"></div>

        <aside
          class="cart-panel__content"
          role="dialog"
          aria-label="Article ajouté"
        >
          <header class="cart-panel__header">
            <p class="cart-panel__confirmation">Ajouté à votre panier</p>

            <button
              type="button"
              class="cart-panel__close"
              aria-label="Fermer"
              @click="cartStore.closePanel()"
            >
              ✕
            </button>
          </header>

          <p class="cart-panel__product">{{ addedLabel }}</p>

          <dl class="cart-panel__summary">
            <div>
              <dt>Articles</dt>
              <dd>{{ cartStore.count }}</dd>
            </div>
            <div>
              <dt>Sous-total</dt>
              <dd>{{ money(cart?.subtotal) }}</dd>
            </div>
          </dl>

          <p
            v-if="cart && cart.amountUntilFreeShipping > 0"
            class="cart-panel__hint"
          >
            Plus que {{ money(cart.amountUntilFreeShipping) }} pour la livraison
            offerte.
          </p>

          <RouterLink
            to="/panier"
            class="cart-panel__primary"
            @click="cartStore.closePanel()"
          >
            Voir mon panier
          </RouterLink>

          <button
            type="button"
            class="cart-panel__secondary"
            @click="cartStore.closePanel()"
          >
            Continuer mes achats
          </button>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.cart-panel {
  position: fixed;
  inset: 0;
  z-index: var(--z-modal);
}

.cart-panel__backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
}

.cart-panel__content {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: min(380px, 90vw);
  padding: 24px;
  background: var(--color-white);
  box-shadow: -12px 0 40px rgba(0, 0, 0, 0.12);
  overflow-y: auto;
}

.cart-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.cart-panel__confirmation {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.cart-panel__close {
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-size: 15px;
  line-height: 1;
}

.cart-panel__product {
  margin: 14px 0 0;
  font-size: 15px;
  line-height: 1.4;
}

.cart-panel__summary {
  display: grid;
  gap: 8px;
  margin: 22px 0 0;
  padding-top: 18px;
  border-top: 1px solid var(--color-border);
}

.cart-panel__summary > div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 13px;
}

.cart-panel__summary dt {
  color: var(--color-text-muted);
}

.cart-panel__summary dd {
  margin: 0;
  font-variant-numeric: tabular-nums;
}

.cart-panel__hint {
  margin: 14px 0 0;
  padding: 9px 11px;
  background: #f5f4f1;
  font-size: 12px;
  line-height: 1.4;
}

.cart-panel__primary {
  display: block;
  margin-top: 22px;
  padding: 14px;
  background: var(--color-black);
  color: var(--color-white);
  font-size: 12px;
  text-align: center;
  text-decoration: none;
}

.cart-panel__secondary {
  display: block;
  width: 100%;
  margin-top: 10px;
  padding: 13px;
  border: 1px solid var(--color-border-strong);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
}

.cart-panel__secondary:hover {
  background: #f5f4f1;
}

/* Animation d'ouverture : elle répond à une action, donc elle est justifiée */
.cart-panel-enter-active .cart-panel__content,
.cart-panel-leave-active .cart-panel__content {
  transition: transform var(--transition-base);
}

.cart-panel-enter-from .cart-panel__content,
.cart-panel-leave-to .cart-panel__content {
  transform: translateX(100%);
}

.cart-panel-enter-active .cart-panel__backdrop,
.cart-panel-leave-active .cart-panel__backdrop {
  transition: opacity var(--transition-base);
}

.cart-panel-enter-from .cart-panel__backdrop,
.cart-panel-leave-to .cart-panel__backdrop {
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .cart-panel-enter-active .cart-panel__content,
  .cart-panel-leave-active .cart-panel__content,
  .cart-panel-enter-active .cart-panel__backdrop,
  .cart-panel-leave-active .cart-panel__backdrop {
    transition-duration: 1ms;
  }
}
</style>
