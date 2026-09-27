<script setup lang="ts">
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";

import { apiMessage } from "@/services/productService";
import {
  downloadInvoice,
  fetchShipment,
  type Shipment,
} from "@/services/accountService";
import { fetchOrders } from "@/services/orderService";
import type { Order } from "@/types/order";

const orders = ref<Order[]>([]);
const shipments = ref<Record<string, Shipment>>({});
const expanded = ref<string | null>(null);

const loading = ref(true);
const error = ref<string | null>(null);
const downloading = ref<string | null>(null);

const STATUS_LABELS: Record<string, string> = {
  PENDING: "En attente de paiement",
  PAID: "Payée",
  PREPARING: "En préparation",
  SHIPPED: "Expédiée",
  DELIVERED: "Livrée",
  CANCELLED: "Annulée",
};

/** Étapes affichées en frise, dans l'ordre du parcours. */
const STEPS = ["PAID", "PREPARING", "SHIPPED", "DELIVERED"];

function money(value: number): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(value);
}

function formatDate(value: string): string {
  return new Date(value).toLocaleDateString("fr-FR", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

function orderNumber(order: Order): string {
  return order.id.slice(0, 8).toUpperCase();
}

function stepIndex(status: string): number {
  return STEPS.indexOf(status);
}

async function toggle(order: Order): Promise<void> {
  expanded.value = expanded.value === order.id ? null : order.id;

  // Le suivi n'est chargé qu'à l'ouverture, et une seule fois
  if (
    expanded.value === order.id &&
    ["SHIPPED", "DELIVERED"].includes(order.status) &&
    !shipments.value[order.id]
  ) {
    try {
      shipments.value[order.id] = await fetchShipment(order.id);
    } catch {
      // Pas d'expédition enregistrée : la section reste simplement absente
    }
  }
}

async function handleInvoice(order: Order): Promise<void> {
  downloading.value = order.id;
  error.value = null;

  try {
    await downloadInvoice(order.id, orderNumber(order));
  } catch (e: unknown) {
    error.value = apiMessage(e, "La facture n'est pas disponible.");
  } finally {
    downloading.value = null;
  }
}

onMounted(async () => {
  try {
    orders.value = await fetchOrders();
  } catch (e: unknown) {
    error.value = apiMessage(e, "Vos commandes n'ont pas pu être chargées.");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="orders">
    <h2 class="section-title">Mes commandes</h2>

    <p v-if="loading" class="orders__state">Chargement</p>

    <p
      v-else-if="error"
      class="orders__state orders__state--error"
      role="alert"
    >
      {{ error }}
    </p>

    <div v-else-if="orders.length === 0" class="orders__empty">
      <p>Vous n'avez pas encore passé de commande.</p>
      <RouterLink to="/produits" class="orders__empty-action">
        Découvrir le catalogue
      </RouterLink>
    </div>

    <ul v-else class="orders__list">
      <li v-for="order in orders" :key="order.id" class="order">
        <button type="button" class="order__header" @click="toggle(order)">
          <span class="order__main">
            <span class="order__number"
              >Commande #{{ orderNumber(order) }}</span
            >
            <span class="order__date">{{ formatDate(order.createdAt) }}</span>
          </span>

          <span class="order__right">
            <span
              class="order__status"
              :class="`order__status--${order.status.toLowerCase()}`"
            >
              {{ STATUS_LABELS[order.status] ?? order.status }}
            </span>
            <span class="order__amount">{{ money(order.totalAmount) }}</span>
            <span
              class="order__chevron"
              :class="{ 'order__chevron--open': expanded === order.id }"
            >
              ⌄
            </span>
          </span>
        </button>

        <div v-if="expanded === order.id" class="order__details">
          <!-- Frise de suivi, masquée pour une commande annulée ou impayée -->
          <ol
            v-if="stepIndex(order.status) >= 0"
            class="tracker"
            :aria-label="`Statut : ${STATUS_LABELS[order.status]}`"
          >
            <li
              v-for="(step, index) in STEPS"
              :key="step"
              class="tracker__step"
              :class="{
                'tracker__step--done': index <= stepIndex(order.status),
              }"
            >
              <span class="tracker__dot" aria-hidden="true"></span>
              <span class="tracker__label">{{ STATUS_LABELS[step] }}</span>
            </li>
          </ol>

          <p v-else-if="order.status === 'CANCELLED'" class="order__cancelled">
            Cette commande a été annulée.
          </p>

          <!-- Articles -->
          <ul class="order__items">
            <li v-for="(item, index) in order.items" :key="index">
              <span>
                {{ item.quantity }} × {{ item.productName }}
                <span
                  v-if="item.variantLabel && item.variantLabel !== 'Standard'"
                >
                  — {{ item.variantLabel }}
                </span>
              </span>
              <span>{{ money(item.unitPrice * item.quantity) }}</span>
            </li>
          </ul>

          <dl class="order__amounts">
            <div>
              <dt>Sous-total</dt>
              <dd>{{ money(order.subtotalAmount) }}</dd>
            </div>
            <div>
              <dt>Livraison</dt>
              <dd>
                <span v-if="order.shippingAmount === 0">Offerte</span>
                <span v-else>{{ money(order.shippingAmount) }}</span>
              </dd>
            </div>
            <div class="order__amounts-total">
              <dt>Total</dt>
              <dd>{{ money(order.totalAmount) }}</dd>
            </div>
          </dl>

          <p class="order__vat">
            dont {{ money(order.vatAmount) }} de TVA ({{ order.vatRate }} %)
          </p>

          <!-- Suivi du colis -->
          <div v-if="shipments[order.id]" class="order__shipment">
            <h3 class="order__subtitle">Expédition</h3>
            <p>
              {{ shipments[order.id].carrier }} —
              <strong>{{ shipments[order.id].trackingNumber }}</strong>
            </p>
          </div>

          <div class="order__actions">
            <button
              v-if="order.status !== 'PENDING' && order.status !== 'CANCELLED'"
              type="button"
              class="order__action"
              :disabled="downloading === order.id"
              @click="handleInvoice(order)"
            >
              {{
                downloading === order.id
                  ? "Téléchargement"
                  : "Télécharger la facture"
              }}
            </button>

            <RouterLink
              v-if="order.status === 'PENDING'"
              :to="`/panier`"
              class="order__action order__action--primary"
            >
              Finaliser le paiement
            </RouterLink>
          </div>
        </div>
      </li>
    </ul>
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

.orders__state {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
}

.orders__state--error {
  color: var(--color-error);
}

.orders__empty {
  display: grid;
  justify-items: start;
  gap: 16px;
  padding: 32px 0;
}

.orders__empty p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
}

.orders__empty-action {
  padding: 12px 22px;
  background: var(--color-black);
  color: var(--color-white);
  font-size: 12px;
  text-decoration: none;
}

.orders__list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

/* =========================================================
   COMMANDE
   ========================================================= */

.order {
  border: 1px solid var(--color-border);
  background: var(--color-white);
}

.order__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 16px 18px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
}

.order__header:hover {
  background: #faf9f7;
}

.order__main {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.order__number {
  font-size: 13.5px;
  font-weight: 500;
}

.order__date {
  color: var(--color-text-muted);
  font-size: 12px;
}

.order__right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.order__status {
  padding: 4px 9px;
  border: 1px solid var(--color-border-strong);
  font-size: 11px;
  white-space: nowrap;
}

.order__status--delivered {
  border-color: var(--color-text);
  background: var(--color-text);
  color: var(--color-white);
}

.order__status--cancelled {
  opacity: 0.6;
  text-decoration: line-through;
}

.order__amount {
  font-size: 13.5px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.order__chevron {
  color: var(--color-text-muted);
  font-size: 14px;
  transition: transform var(--transition-fast);
}

.order__chevron--open {
  transform: rotate(180deg);
}

/* =========================================================
   DÉTAIL
   ========================================================= */

.order__details {
  padding: 4px 18px 20px;
  border-top: 1px solid var(--color-border);
}

.order__subtitle {
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

/* Frise de suivi */
.tracker {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  margin: 20px 0 24px;
  padding: 0;
  list-style: none;
}

.tracker__step {
  display: grid;
  gap: 8px;
  text-align: center;
  position: relative;
}

.tracker__dot {
  width: 9px;
  height: 9px;
  margin: 0 auto;
  border: 1px solid var(--color-border-strong);
  border-radius: 50%;
  background: var(--color-white);
}

.tracker__step--done .tracker__dot {
  border-color: var(--color-text);
  background: var(--color-text);
}

/* Trait de liaison entre les étapes */
.tracker__step:not(:last-child)::after {
  content: "";
  position: absolute;
  top: 4px;
  left: calc(50% + 8px);
  right: calc(-50% + 8px);
  height: 1px;
  background: var(--color-border-strong);
}

.tracker__step--done:not(:last-child)::after {
  background: var(--color-text);
}

.tracker__label {
  color: var(--color-text-muted);
  font-size: 10.5px;
  line-height: 1.3;
}

.tracker__step--done .tracker__label {
  color: var(--color-text);
}

.order__cancelled {
  margin: 16px 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.order__items {
  display: grid;
  gap: 8px;
  margin: 0 0 16px;
  padding: 0 0 16px;
  border-bottom: 1px solid var(--color-border);
  list-style: none;
}

.order__items li {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  font-size: 13px;
  line-height: 1.4;
}

.order__amounts {
  display: grid;
  gap: 7px;
  margin: 0;
}

.order__amounts > div {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  font-size: 13px;
}

.order__amounts dt {
  color: var(--color-text-muted);
}

.order__amounts dd {
  margin: 0;
  font-variant-numeric: tabular-nums;
}

.order__amounts-total {
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
  font-weight: 500;
}

.order__amounts-total dt {
  color: var(--color-text) !important;
}

.order__vat {
  margin: 5px 0 0;
  color: var(--color-text-muted);
  font-size: 11.5px;
}

.order__shipment {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.order__shipment p {
  margin: 0;
  font-size: 13px;
}

.order__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.order__action {
  padding: 10px 18px;
  border: 1px solid var(--color-border-strong);
  background: transparent;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
  text-decoration: none;
}

.order__action:hover:not(:disabled) {
  border-color: var(--color-text);
}

.order__action:disabled {
  cursor: wait;
  opacity: 0.5;
}

.order__action--primary {
  border-color: var(--color-black);
  background: var(--color-black);
  color: var(--color-white);
}

@media (max-width: 767px) {
  .order__right {
    gap: 8px;
  }

  .order__status {
    display: none;
  }

  .tracker__label {
    font-size: 9.5px;
  }
}
</style>
