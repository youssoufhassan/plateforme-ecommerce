<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import api from "../services/api";

interface OrderItem {
  productName: string;
  quantity: number;
  unitPrice: number;
}

interface Order {
  id: string;
  status: string;
  totalAmount: number;
  createdAt: string;

  customerFirstName: string | null;
  customerLastName: string | null;
  customerEmail: string | null;

  items: OrderItem[];
}

type ToastType = "success" | "error";

const orders = ref<Order[]>([]);
const loading = ref(true);
const search = ref("");
const statusFilter = ref("Tous");
const sortBy = ref<"date-desc" | "date-asc" | "amount-desc" | "amount-asc">(
  "date-desc",
);
const expandedId = ref<string | null>(null);
const updatingId = ref<string | null>(null);

const toast = ref({
  message: "",
  type: "success" as ToastType,
});

const statuses = ["PENDING", "PAID", "PREPARING", "SHIPPED", "DELIVERED"];

const statusLabels: Record<string, string> = {
  PENDING: "En attente",
  PAID: "Payée",
  PREPARING: "Préparation",
  SHIPPED: "Expédiée",
  DELIVERED: "Livrée",
};

let toastTimer: ReturnType<typeof setTimeout> | null = null;

function showToast(message: string, type: ToastType = "success") {
  toast.value = { message, type };

  if (toastTimer) {
    clearTimeout(toastTimer);
  }

  toastTimer = setTimeout(() => {
    toast.value.message = "";
  }, 3000);
}

async function loadOrders() {
  loading.value = true;

  try {
    const response = await api.get<Order[]>("/orders/admin");
    orders.value = response.data;
  } catch (e) {
    showToast("Impossible de charger les commandes.", "error");
  } finally {
    loading.value = false;
  }
}

onMounted(loadOrders);

const totalOrders = computed(() => orders.value.length);

const pendingOrders = computed(
  () => orders.value.filter((o) => o.status === "PENDING").length,
);

const preparingOrders = computed(
  () => orders.value.filter((o) => o.status === "PREPARING").length,
);

const shippedOrders = computed(
  () => orders.value.filter((o) => o.status === "SHIPPED").length,
);

const deliveredOrders = computed(
  () => orders.value.filter((o) => o.status === "DELIVERED").length,
);

const filteredOrders = computed(() => {
  const query = search.value.trim().toLowerCase();

  const result = orders.value.filter((order) => {
    const matchSearch =
      !query ||
      order.id.toLowerCase().includes(query) ||
      order.items.some((item) =>
        item.productName.toLowerCase().includes(query),
      );

    const matchStatus =
      statusFilter.value === "Tous" || order.status === statusFilter.value;

    return matchSearch && matchStatus;
  });

  return [...result].sort((a, b) => {
    switch (sortBy.value) {
      case "date-asc":
        return (
          new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
        );

      case "amount-desc":
        return b.totalAmount - a.totalAmount;

      case "amount-asc":
        return a.totalAmount - b.totalAmount;

      case "date-desc":
      default:
        return (
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
    }
  });
});

function toggleExpand(id: string) {
  expandedId.value = expandedId.value === id ? null : id;
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function formatTime(date: string) {
  return new Date(date).toLocaleTimeString("fr-FR", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatStatus(status: string) {
  return statusLabels[status] || status;
}

function statusClass(status: string) {
  return `status-${status.toLowerCase()}`;
}

function itemCount(order: Order) {
  return order.items.reduce((total, item) => total + item.quantity, 0);
}

async function updateStatus(id: string, status: string) {
  const order = orders.value.find((item) => item.id === id);

  if (!order || order.status === status) {
    return;
  }

  const previousStatus = order.status;

  updatingId.value = id;

  try {
    order.status = status;

    await api.put(`/orders/${id}/status`, { status });

    showToast("Statut de la commande mis à jour.");
  } catch (e: any) {
    order.status = previousStatus;

    showToast(
      e.response?.data?.message || "Erreur lors de la mise à jour du statut.",
      "error",
    );
  } finally {
    updatingId.value = null;
  }
}
</script>

<template>
  <div class="orders-page">
    <!-- Header -->
    <header class="page-header">
      <div>
        <span class="page-eyebrow">VENTES</span>

        <h1 class="page-title">Commandes</h1>

        <p class="page-subtitle">
          Suivez et gérez les commandes de votre boutique.
        </p>
      </div>

      <div class="order-count">
        <strong>{{ totalOrders }}</strong>
        <span>commande{{ totalOrders > 1 ? "s" : "" }}</span>
      </div>
    </header>

    <!-- Stats -->
    <section class="stats-grid">
      <div class="order-stat">
        <div class="stat-icon all">↗</div>

        <div>
          <strong>{{ totalOrders }}</strong>
          <span>Total</span>
        </div>
      </div>

      <div class="order-stat">
        <div class="stat-icon pending">!</div>

        <div>
          <strong>{{ pendingOrders }}</strong>
          <span>En attente</span>
        </div>
      </div>

      <div class="order-stat">
        <div class="stat-icon preparing">◷</div>

        <div>
          <strong>{{ preparingOrders }}</strong>
          <span>En préparation</span>
        </div>
      </div>

      <div class="order-stat">
        <div class="stat-icon shipped">→</div>

        <div>
          <strong>{{ shippedOrders }}</strong>
          <span>Expédiées</span>
        </div>
      </div>

      <div class="order-stat">
        <div class="stat-icon delivered">✓</div>

        <div>
          <strong>{{ deliveredOrders }}</strong>
          <span>Livrées</span>
        </div>
      </div>
    </section>

    <!-- Filters -->
    <section class="filters-card">
      <div class="search-wrapper">
        <span class="search-icon">⌕</span>

        <input
          v-model="search"
          type="search"
          placeholder="Rechercher une commande ou un produit..."
        />

        <button
          v-if="search"
          type="button"
          class="clear-search"
          @click="search = ''"
        >
          ×
        </button>
      </div>

      <select v-model="statusFilter">
        <option value="Tous">Tous les statuts</option>

        <option v-for="status in statuses" :key="status" :value="status">
          {{ statusLabels[status] }}
        </option>
      </select>

      <select v-model="sortBy">
        <option value="date-desc">Plus récentes</option>
        <option value="date-asc">Plus anciennes</option>
        <option value="amount-desc">Montant décroissant</option>
        <option value="amount-asc">Montant croissant</option>
      </select>
    </section>

    <!-- Loading -->
    <section v-if="loading" class="orders-card">
      <div v-for="i in 6" :key="i" class="order-skeleton">
        <div class="skeleton skeleton-main"></div>
        <div class="skeleton skeleton-date"></div>
        <div class="skeleton skeleton-price"></div>
        <div class="skeleton skeleton-status"></div>
        <div class="skeleton skeleton-button"></div>
      </div>
    </section>

    <!-- Empty -->
    <section v-else-if="filteredOrders.length === 0" class="empty-state">
      <div class="empty-icon">⌕</div>

      <h2>
        {{
          orders.length === 0 ? "Aucune commande" : "Aucune commande trouvée"
        }}
      </h2>

      <p v-if="orders.length === 0">
        Les nouvelles commandes apparaîtront ici.
      </p>

      <p v-else>Aucune commande ne correspond à tes filtres actuels.</p>

      <button
        v-if="search || statusFilter !== 'Tous'"
        class="btn"
        @click="
          search = '';
          statusFilter = 'Tous';
        "
      >
        Réinitialiser les filtres
      </button>
    </section>

    <!-- Orders -->
    <section v-else class="orders-card">
      <div class="table-header">
        <span>
          {{ filteredOrders.length }}
          résultat{{ filteredOrders.length > 1 ? "s" : "" }}
        </span>
      </div>

      <!-- Desktop -->
      <div class="desktop-table">
        <table class="orders-table">
          <thead>
            <tr>
              <th>Commande</th>
              <th>Date</th>
              <th>Articles</th>
              <th>Total</th>
              <th>Statut</th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            <template v-for="order in filteredOrders" :key="order.id">
              <tr
                class="order-row"
                :class="{
                  expanded: expandedId === order.id,
                }"
              >
                <td>
                  <div class="order-number">
                    #{{ order.id.slice(0, 8).toUpperCase() }}
                  </div>

                  <div class="customer-name">
                    {{
                      [order.customerFirstName, order.customerLastName]
                        .filter(Boolean)
                        .join(" ") || "Client"
                    }}
                  </div>

                  <div class="customer-email">
                    {{ order.customerEmail || "Email indisponible" }}
                  </div>
                </td>

                <td>
                  <div class="date">
                    {{ formatDate(order.createdAt) }}
                  </div>

                  <div class="time">
                    {{ formatTime(order.createdAt) }}
                  </div>
                </td>

                <td>
                  <span class="items-count">
                    {{ itemCount(order) }}
                    article{{ itemCount(order) > 1 ? "s" : "" }}
                  </span>
                </td>

                <td>
                  <strong class="order-total">
                    {{ order.totalAmount.toFixed(2) }} €
                  </strong>
                </td>

                <td>
                  <div class="status-wrapper">
                    <span
                      class="status-badge"
                      :class="statusClass(order.status)"
                    >
                      <span class="status-dot"></span>
                      {{ formatStatus(order.status) }}
                    </span>

                    <select
                      class="status-select"
                      :value="order.status"
                      :disabled="updatingId === order.id"
                      @change="
                        updateStatus(
                          order.id,
                          ($event.target as HTMLSelectElement).value,
                        )
                      "
                    >
                      <option
                        v-for="status in statuses"
                        :key="status"
                        :value="status"
                      >
                        {{ statusLabels[status] }}
                      </option>
                    </select>
                  </div>
                </td>

                <td class="actions-cell">
                  <button class="detail-button" @click="toggleExpand(order.id)">
                    {{ expandedId === order.id ? "Fermer" : "Détail" }}
                    <span>
                      {{ expandedId === order.id ? "↑" : "↓" }}
                    </span>
                  </button>
                </td>
              </tr>

              <!-- Details -->
              <tr v-if="expandedId === order.id" class="details-row">
                <td colspan="6">
                  <div class="order-details">
                    <div class="details-header">
                      <div>
                        <span class="details-eyebrow">
                          DÉTAIL DE LA COMMANDE
                        </span>

                        <h3>#{{ order.id.slice(0, 8).toUpperCase() }}</h3>
                      </div>

                      <span
                        class="status-badge"
                        :class="statusClass(order.status)"
                      >
                        <span class="status-dot"></span>
                        {{ formatStatus(order.status) }}
                      </span>
                    </div>

                    <div class="items-list">
                      <div
                        v-for="(item, index) in order.items"
                        :key="index"
                        class="order-item"
                      >
                        <div class="item-index">
                          {{ String(index + 1).padStart(2, "0") }}
                        </div>

                        <div class="item-info">
                          <strong>{{ item.productName }}</strong>
                          <span>
                            {{ item.quantity }} ×
                            {{ item.unitPrice.toFixed(2) }} €
                          </span>
                        </div>

                        <strong class="item-total">
                          {{ (item.quantity * item.unitPrice).toFixed(2) }}
                          €
                        </strong>
                      </div>
                    </div>

                    <div class="details-total">
                      <span>Total de la commande</span>

                      <strong> {{ order.totalAmount.toFixed(2) }} € </strong>
                    </div>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Mobile -->
      <div class="mobile-orders">
        <article
          v-for="order in filteredOrders"
          :key="order.id"
          class="mobile-order"
        >
          <div class="mobile-order-top">
            <div>
              <strong> #{{ order.id.slice(0, 8).toUpperCase() }} </strong>

              <span>
                {{ formatDate(order.createdAt) }}
                ·
                {{ formatTime(order.createdAt) }}
              </span>
            </div>

            <strong class="mobile-total">
              {{ order.totalAmount.toFixed(2) }} €
            </strong>
          </div>

          <div class="mobile-order-meta">
            <span class="items-count">
              {{ itemCount(order) }}
              article{{ itemCount(order) > 1 ? "s" : "" }}
            </span>

            <span class="status-badge" :class="statusClass(order.status)">
              <span class="status-dot"></span>
              {{ formatStatus(order.status) }}
            </span>
          </div>

          <select
            class="mobile-status-select"
            :value="order.status"
            :disabled="updatingId === order.id"
            @change="
              updateStatus(order.id, ($event.target as HTMLSelectElement).value)
            "
          >
            <option v-for="status in statuses" :key="status" :value="status">
              {{ statusLabels[status] }}
            </option>
          </select>

          <button class="mobile-detail-button" @click="toggleExpand(order.id)">
            {{
              expandedId === order.id
                ? "Masquer les détails"
                : "Voir les détails"
            }}
          </button>

          <div v-if="expandedId === order.id" class="mobile-details">
            <div
              v-for="(item, index) in order.items"
              :key="index"
              class="order-item"
            >
              <div class="item-info">
                <strong>{{ item.productName }}</strong>

                <span>
                  {{ item.quantity }} × {{ item.unitPrice.toFixed(2) }} €
                </span>
              </div>

              <strong class="item-total">
                {{ (item.quantity * item.unitPrice).toFixed(2) }}
                €
              </strong>
            </div>
          </div>
        </article>
      </div>
    </section>

    <!-- Toast -->
    <Transition name="toast">
      <div v-if="toast.message" class="toast" :class="toast.type" role="alert">
        <span class="toast-indicator"></span>
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.orders-page {
  width: 100%;
}

/* Header */

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.page-eyebrow {
  display: block;
  margin-bottom: 0.35rem;
  color: var(--ink-soft);
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.page-title {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 750;
  letter-spacing: -0.03em;
}

.page-subtitle {
  margin: 0.4rem 0 0;
  color: var(--ink-soft);
  font-size: 0.9rem;
}

.order-count {
  display: flex;
  align-items: baseline;
  gap: 0.35rem;
  color: var(--ink-soft);
}

.order-count strong {
  color: var(--ink);
  font-size: 1.4rem;
}

/* Stats */

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.order-stat {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-height: 76px;
  padding: 0.9rem;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 12px;
}

.order-stat strong {
  display: block;
  font-size: 1.25rem;
  line-height: 1;
}

.order-stat span {
  display: block;
  margin-top: 0.3rem;
  color: var(--ink-soft);
  font-size: 0.7rem;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  border-radius: 10px;
  background: #f2f2f2;
  font-weight: 800;
}

/* Filters */

.filters-card {
  display: grid;
  grid-template-columns: minmax(250px, 1fr) 190px 190px;
  gap: 0.65rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 12px;
}

.search-wrapper {
  position: relative;
}

.search-wrapper input,
.filters-card select {
  width: 100%;
  height: 40px;
  box-sizing: border-box;
  border: 1px solid rgba(0, 0, 0, 0.11);
  border-radius: 8px;
  background: #fafafa;
  color: var(--ink);
  outline: none;
  font-size: 0.78rem;
}

.search-wrapper input {
  padding: 0 2.2rem;
}

.filters-card select {
  padding: 0 0.7rem;
  cursor: pointer;
}

.search-wrapper input:focus,
.filters-card select:focus {
  border-color: var(--ink);
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.04);
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--ink-soft);
  font-size: 1rem;
  pointer-events: none;
}

.clear-search {
  position: absolute;
  right: 0.45rem;
  top: 50%;
  width: 28px;
  height: 28px;
  transform: translateY(-50%);
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--ink-soft);
  cursor: pointer;
  font-size: 1.1rem;
}

/* Orders card */

.orders-card {
  overflow: hidden;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 13px;
}

.table-header {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  color: var(--ink-soft);
  font-size: 0.72rem;
}

/* Table */

.orders-table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th {
  padding: 0.75rem 1rem;
  background: #fafafa;
  color: var(--ink-soft);
  font-size: 0.65rem;
  font-weight: 750;
  letter-spacing: 0.04em;
  text-align: left;
  text-transform: uppercase;
}

.orders-table td {
  padding: 0.9rem 1rem;
  border-top: 1px solid rgba(0, 0, 0, 0.055);
  vertical-align: middle;
}

.order-row {
  transition: background 0.15s ease;
}

.order-row:hover {
  background: #fcfcfc;
}

.order-row.expanded {
  background: #fafafa;
}

.order-number {
  font-weight: 750;
  font-size: 0.8rem;
  letter-spacing: 0.02em;
}

.date {
  font-size: 0.78rem;
  font-weight: 600;
}

.time {
  margin-top: 0.15rem;
  color: var(--ink-soft);
  font-size: 0.68rem;
}

.items-count {
  color: var(--ink-soft);
  font-size: 0.72rem;
}

.order-total {
  font-size: 0.82rem;
}

/* Status */

.status-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.35rem 0.55rem;
  border-radius: 999px;
  font-size: 0.67rem;
  font-weight: 700;
  white-space: nowrap;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-pending {
  background: #fff6df;
  color: #946b00;
}

.status-paid {
  background: #eef8f0;
  color: #31733d;
}

.status-preparing {
  background: #eef3ff;
  color: #4965a0;
}

.status-shipped {
  background: #f2efff;
  color: #6752a2;
}

.status-delivered {
  background: #eaf8f4;
  color: #287764;
}

.status-select {
  position: absolute;
  inset: 0;
  width: 100%;
  opacity: 0;
  cursor: pointer;
}

.status-select:disabled {
  cursor: wait;
}

/* Actions */

.actions-cell {
  text-align: right;
}

.detail-button {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 7px;
  background: white;
  color: var(--ink);
  padding: 0.42rem 0.6rem;
  font-size: 0.7rem;
  font-weight: 650;
  cursor: pointer;
}

.detail-button:hover {
  background: #f5f5f5;
}

.detail-button span {
  margin-left: 0.25rem;
}

/* Details */

.details-row td {
  padding: 0;
  background: #fafafa;
}

.order-details {
  padding: 1.25rem 1.5rem 1.4rem;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.details-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.details-eyebrow {
  color: var(--ink-soft);
  font-size: 0.62rem;
  font-weight: 800;
  letter-spacing: 0.1em;
}

.details-header h3 {
  margin: 0.2rem 0 0;
  font-size: 0.95rem;
}

.items-list {
  border-top: 1px solid rgba(0, 0, 0, 0.07);
}

.order-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.8rem 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.055);
}

.item-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 7px;
  background: #f0f0f0;
  color: var(--ink-soft);
  font-size: 0.62rem;
  font-weight: 750;
}

.item-info {
  min-width: 0;
  flex: 1;
}

.item-info strong {
  display: block;
  overflow: hidden;
  font-size: 0.78rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info span {
  display: block;
  margin-top: 0.18rem;
  color: var(--ink-soft);
  font-size: 0.68rem;
}

.item-total {
  font-size: 0.76rem;
}

.details-total {
  display: flex;
  justify-content: flex-end;
  gap: 1.5rem;
  padding-top: 1rem;
  font-size: 0.76rem;
}

.details-total span {
  color: var(--ink-soft);
}

.details-total strong {
  font-size: 0.9rem;
}

/* Mobile */

.mobile-orders {
  display: none;
}

/* Empty */

.empty-state {
  display: flex;
  min-height: 300px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
  background: white;
  border: 1px dashed rgba(0, 0, 0, 0.13);
  border-radius: 13px;
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  margin-bottom: 0.9rem;
  border-radius: 50%;
  background: #f2f2f2;
  color: var(--ink-soft);
  font-size: 1.3rem;
}

.empty-state h2 {
  margin: 0;
  font-size: 1rem;
}

.empty-state p {
  max-width: 400px;
  margin: 0.45rem 0 1rem;
  color: var(--ink-soft);
  font-size: 0.78rem;
}

.btn {
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  background: white;
  color: var(--ink);
  padding: 0.6rem 0.8rem;
  font-size: 0.75rem;
  font-weight: 650;
  cursor: pointer;
}

/* Skeleton */

.order-skeleton {
  display: grid;
  grid-template-columns: 1.2fr 1fr 0.8fr 1fr 0.7fr;
  gap: 1rem;
  padding: 1rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.skeleton {
  height: 14px;
  border-radius: 5px;
  background: #eeeeee;
  animation: pulse 1.3s ease-in-out infinite;
}

.skeleton-main {
  width: 70%;
}

.skeleton-date {
  width: 65%;
}

.skeleton-price {
  width: 55%;
}

.skeleton-status {
  width: 75%;
}

.skeleton-button {
  width: 60%;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.5;
  }

  50% {
    opacity: 1;
  }
}

/* Toast */

.toast {
  position: fixed;
  right: 1.25rem;
  bottom: 1.25rem;
  z-index: 500;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  max-width: 360px;
  padding: 0.8rem 1rem;
  border-radius: 10px;
  background: var(--ink);
  color: white;
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.18);
  font-size: 0.78rem;
}

.toast.error {
  background: #8f2929;
}

.toast-indicator {
  width: 7px;
  height: 7px;
  flex-shrink: 0;
  border-radius: 50%;
  background: white;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* Responsive */

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .filters-card {
    grid-template-columns: 1fr 180px 180px;
  }
}

@media (max-width: 850px) {
  .desktop-table {
    display: none;
  }

  .mobile-orders {
    display: block;
  }

  .filters-card {
    grid-template-columns: 1fr 1fr;
  }

  .filters-card .search-wrapper {
    grid-column: 1 / -1;
  }

  .mobile-order {
    padding: 1rem;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  }

  .mobile-order:last-child {
    border-bottom: 0;
  }

  .mobile-order-top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
  }

  .mobile-order-top strong {
    display: block;
    font-size: 0.8rem;
  }

  .mobile-order-top span {
    display: block;
    margin-top: 0.25rem;
    color: var(--ink-soft);
    font-size: 0.68rem;
  }

  .mobile-total {
    white-space: nowrap;
  }

  .mobile-order-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    margin-top: 0.8rem;
  }

  .mobile-status-select {
    width: 100%;
    height: 38px;
    margin-top: 0.75rem;
    padding: 0 0.7rem;
    border: 1px solid rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    background: #fafafa;
    color: var(--ink);
    font-size: 0.75rem;
  }

  .mobile-detail-button {
    width: 100%;
    margin-top: 0.55rem;
    padding: 0.6rem;
    border: 1px solid rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    background: white;
    color: var(--ink);
    font-size: 0.72rem;
    font-weight: 650;
    cursor: pointer;
  }

  .mobile-details {
    margin-top: 0.8rem;
    padding: 0.75rem;
    border-radius: 9px;
    background: #fafafa;
  }

  .mobile-details .order-item {
    padding: 0.65rem 0;
  }
}

@media (max-width: 600px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filters-card {
    grid-template-columns: 1fr;
  }

  .filters-card .search-wrapper {
    grid-column: auto;
  }

  .toast {
    right: 0.75rem;
    bottom: 0.75rem;
    left: 0.75rem;
    max-width: none;
  }
}

@media (max-width: 420px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .order-stat {
    min-height: 68px;
  }

  .stat-icon {
    width: 32px;
    height: 32px;
  }
}
</style>
