<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import api from "../services/api";
import PageHeader from "../components/PageHeader.vue";
import StatCard from "../components/StatCard.vue";

interface RecentOrder {
  id: string;
  customerName: string;
  customerEmail: string;
  totalAmount: number;
  status: string;
  createdAt: string;
}
interface DashboardStats {
  totalOrders: number;
  totalCustomers: number;
  totalProducts: number;
  totalRevenue: number;
  pendingOrders: number;
}
interface RevenuePoint {
  date: string;
  revenue: number;
}
const revenueData = ref<RevenuePoint[]>([]);
const revenueLoading = ref(true);
const revenueError = ref("");
const recentOrders = ref<RecentOrder[]>([]);
const recentOrdersLoading = ref(true);
const recentOrdersError = ref("");
const stats = ref<DashboardStats>({
  totalOrders: 0,
  totalCustomers: 0,
  totalProducts: 0,
  totalRevenue: 0,
  pendingOrders: 0,
});

const loading = ref(true);
const error = ref("");

const revenueFormatted = computed(() =>
  new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
    minimumFractionDigits: 2,
  }).format(stats.value.totalRevenue),
);

const pendingPercentage = computed(() => {
  if (!stats.value.totalOrders) return 0;

  return Math.min(
    100,
    Math.round((stats.value.pendingOrders / stats.value.totalOrders) * 100),
  );
});

async function loadDashboard() {
  loading.value = true;
  error.value = "";

  try {
    const response = await api.get("/admin/dashboard/stats");

    stats.value = {
      totalOrders: Number(response.data.totalOrders ?? 0),
      totalCustomers: Number(response.data.totalCustomers ?? 0),
      totalProducts: Number(response.data.totalProducts ?? 0),
      totalRevenue: Number(response.data.totalRevenue ?? 0),
      pendingOrders: Number(response.data.pendingOrders ?? 0),
    };
  } catch (e) {
    error.value = "Impossible de charger les statistiques.";
  } finally {
    loading.value = false;
  }
}
function formatOrderAmount(amount: number) {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
    minimumFractionDigits: 2,
  }).format(amount);
}

function formatOrderDate(date: string) {
  return new Intl.DateTimeFormat("fr-FR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(date));
}

function getStatusLabel(status: string) {
  const labels: Record<string, string> = {
    PENDING: "En attente",
    CONFIRMED: "Confirmée",
    SHIPPED: "Expédiée",
    DELIVERED: "Livrée",
    CANCELLED: "Annulée",
  };

  return labels[status] ?? status;
}

function getStatusClass(status: string) {
  return `status-${status.toLowerCase()}`;
}
async function loadRecentOrders() {
  recentOrdersLoading.value = true;
  recentOrdersError.value = "";

  try {
    const response = await api.get("/admin/dashboard/recent-orders");

    recentOrders.value = response.data.map((order: RecentOrder) => ({
      ...order,
      totalAmount: Number(order.totalAmount ?? 0),
    }));
  } catch (e) {
    recentOrdersError.value = "Impossible de charger les commandes récentes.";
  } finally {
    recentOrdersLoading.value = false;
  }
}
async function loadRevenue() {
  revenueLoading.value = true;
  revenueError.value = "";

  try {
    const response = await api.get("/admin/dashboard/revenue");

    revenueData.value = response.data.map((item: RevenuePoint) => ({
      date: item.date,
      revenue: Number(item.revenue ?? 0),
    }));
  } catch (e) {
    revenueError.value = "Impossible de charger l'historique des ventes.";
  } finally {
    revenueLoading.value = false;
  }
}
function retry() {
  loadDashboard();
  loadRecentOrders();
  loadRevenue();
}

onMounted(() => {
  loadDashboard();
  loadRecentOrders();
  loadRevenue();
});
</script>
<template>
  <div class="dashboard">
    <!-- Header -->
    <PageHeader
      eyebrow="Vue d'ensemble"
      title="Tableau de bord"
      subtitle="Suivez les performances de votre boutique et l'activité de SHAHIN."
    />

    <!-- Loading -->
    <div v-if="loading" class="dashboard-loading">
      <div v-for="i in 5" :key="i" class="skeleton-card">
        <div class="skeleton skeleton-small"></div>
        <div class="skeleton skeleton-large"></div>
        <div class="skeleton skeleton-medium"></div>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-panel">
      <div class="error-icon">!</div>

      <div class="error-content">
        <strong>Impossible de charger le dashboard</strong>
        <p>{{ error }}</p>
      </div>

      <button type="button" class="retry-button" @click="retry">
        Réessayer
      </button>
    </div>

    <!-- Dashboard -->
    <template v-else>
      <!-- KPI -->
      <section class="stats-grid">
        <StatCard
          label="Chiffre d'affaires"
          :value="revenueFormatted"
          description="hors commandes en attente"
          icon="€"
          trend="neutral"
        />

        <StatCard
          label="Commandes"
          :value="String(stats.totalOrders)"
          description="commandes enregistrées"
          icon="□"
          trend="neutral"
        />

        <StatCard
          label="Commandes en attente"
          :value="String(stats.pendingOrders)"
          :description="`${pendingPercentage}% des commandes`"
          icon="◷"
          :trend="stats.pendingOrders > 0 ? 'down' : 'neutral'"
        />

        <StatCard
          label="Clients"
          :value="String(stats.totalCustomers)"
          description="clients enregistrés"
          icon="♙"
          trend="neutral"
        />

        <StatCard
          label="Produits"
          :value="String(stats.totalProducts)"
          description="produits au catalogue"
          icon="◇"
          trend="neutral"
        />
      </section>

      <!-- Main dashboard grid -->
      <section class="dashboard-grid">
        <!-- Revenue -->
        <article class="dashboard-card revenue-card">
          <div class="card-header">
            <div>
              <span class="card-eyebrow">PERFORMANCE</span>
              <h2>Chiffre d'affaires</h2>
            </div>

            <span class="card-period">7 derniers jours</span>
          </div>

          <div class="revenue-content">
            <div class="revenue-value">
              {{ revenueFormatted }}
            </div>

            <p>Chiffre d'affaires généré par les commandes prises en compte.</p>
          </div>

          <!-- Revenue chart -->
          <div class="revenue-chart">
            <!-- Loading -->
            <div v-if="revenueLoading" class="chart-loading">
              Chargement du graphique...
            </div>

            <!-- Error -->
            <div v-else-if="revenueError" class="chart-error">
              <span>!</span>
              <p>{{ revenueError }}</p>
              <button type="button" @click="loadRevenue">Réessayer</button>
            </div>

            <!-- Chart -->
            <div v-else-if="revenueData.length" class="chart">
              <div
                v-for="point in revenueData"
                :key="point.date"
                class="chart-column"
              >
                <div class="chart-value">
                  {{ formatOrderAmount(point.revenue) }}
                </div>

                <div
                  class="chart-bar"
                  :style="{
                    height: `${Math.max(
                      8,
                      (point.revenue /
                        Math.max(...revenueData.map((p) => p.revenue), 1)) *
                        150,
                    )}px`,
                  }"
                ></div>

                <div class="chart-date">
                  {{
                    new Intl.DateTimeFormat("fr-FR", {
                      weekday: "short",
                    }).format(new Date(point.date))
                  }}
                </div>
              </div>
            </div>

            <!-- Empty -->
            <div v-else class="chart-empty">
              Aucune vente enregistrée sur cette période.
            </div>
          </div>

          <!-- Recent orders -->
          <div class="recent-orders">
            <div class="recent-orders-header">
              <div>
                <span class="recent-orders-title"> Dernières activités </span>

                <span class="recent-orders-subtitle">
                  Les 5 commandes les plus récentes
                </span>
              </div>

              <router-link to="/orders" class="view-all-link">
                Tout voir →
              </router-link>
            </div>

            <!-- Loading -->
            <div v-if="recentOrdersLoading" class="recent-orders-loading">
              <div v-for="i in 5" :key="i" class="recent-order-skeleton">
                <div class="skeleton skeleton-avatar"></div>

                <div class="recent-skeleton-content">
                  <div class="skeleton skeleton-order-name"></div>
                  <div class="skeleton skeleton-order-date"></div>
                </div>

                <div class="skeleton skeleton-order-price"></div>
              </div>
            </div>

            <!-- Error -->
            <div v-else-if="recentOrdersError" class="recent-orders-error">
              <span>!</span>

              <p>{{ recentOrdersError }}</p>

              <button type="button" @click="loadRecentOrders">Réessayer</button>
            </div>

            <!-- Empty -->
            <div
              v-else-if="recentOrders.length === 0"
              class="recent-orders-empty"
            >
              <span>□</span>

              <strong>Aucune commande</strong>

              <small> Les nouvelles commandes apparaîtront ici. </small>
            </div>

            <!-- Orders -->
            <div v-else class="recent-orders-list">
              <router-link
                v-for="order in recentOrders"
                :key="order.id"
                to="/orders"
                class="recent-order"
              >
                <div class="order-avatar">
                  {{ order.customerName.charAt(0).toUpperCase() }}
                </div>

                <div class="order-main">
                  <strong>
                    {{ order.customerName }}
                  </strong>

                  <span>
                    #{{ order.id.slice(0, 8) }}
                    ·
                    {{ formatOrderDate(order.createdAt) }}
                  </span>
                </div>

                <div class="order-right">
                  <strong>
                    {{ formatOrderAmount(order.totalAmount) }}
                  </strong>

                  <span
                    class="order-status"
                    :class="getStatusClass(order.status)"
                  >
                    {{ getStatusLabel(order.status) }}
                  </span>
                </div>
              </router-link>
            </div>
          </div>
        </article>

        <!-- Orders -->
        <article class="dashboard-card orders-card">
          <div class="card-header">
            <div>
              <span class="card-eyebrow">COMMANDES</span>
              <h2>État des commandes</h2>
            </div>
          </div>

          <div class="orders-summary">
            <div class="orders-number">
              {{ stats.totalOrders }}
            </div>

            <span>commandes au total</span>
          </div>

          <div class="progress-section">
            <div class="progress-label">
              <span>En attente</span>
              <strong>{{ pendingPercentage }}%</strong>
            </div>

            <div class="progress-track">
              <div
                class="progress-value"
                :style="{ width: `${pendingPercentage}%` }"
              ></div>
            </div>
          </div>

          <div class="pending-box">
            <div class="pending-icon">◷</div>

            <div>
              <strong>
                {{ stats.pendingOrders }} commande{{
                  stats.pendingOrders > 1 ? "s" : ""
                }}
              </strong>

              <span>
                nécessite{{ stats.pendingOrders > 1 ? "nt" : "" }}
                votre attention
              </span>
            </div>
          </div>
        </article>

        <!-- Catalogue -->
        <article class="dashboard-card catalogue-card">
          <div class="card-header">
            <div>
              <span class="card-eyebrow">CATALOGUE</span>
              <h2>Votre catalogue</h2>
            </div>
          </div>

          <div class="catalogue-stat">
            <span class="catalogue-number">
              {{ stats.totalProducts }}
            </span>

            <span class="catalogue-label">
              produits actifs dans le catalogue
            </span>
          </div>

          <div class="catalogue-actions">
            <router-link to="/products" class="dashboard-link">
              Gérer les produits
              <span>→</span>
            </router-link>

            <router-link to="/categories" class="dashboard-link secondary">
              Voir les catégories
              <span>→</span>
            </router-link>
          </div>
        </article>

        <!-- Customers -->
        <article class="dashboard-card customers-card">
          <div class="card-header">
            <div>
              <span class="card-eyebrow">CLIENTS</span>
              <h2>Communauté</h2>
            </div>
          </div>

          <div class="customer-stat">
            <div class="customer-avatar">♙</div>

            <div>
              <strong>{{ stats.totalCustomers }}</strong>
              <span>clients enregistrés</span>
            </div>
          </div>

          <router-link to="/customers" class="dashboard-link">
            Consulter les clients
            <span>→</span>
          </router-link>
        </article>
      </section>

      <!-- Quick actions -->
      <section class="quick-section">
        <div class="section-heading">
          <div>
            <span class="card-eyebrow">RACCOURCIS</span>
            <h2>Actions rapides</h2>
          </div>

          <span>Gestion de la boutique</span>
        </div>

        <div class="quick-grid">
          <router-link to="/products" class="quick-action">
            <span class="quick-icon">+</span>

            <span>
              <strong>Ajouter un produit</strong>
              <small>Créer une nouvelle référence</small>
            </span>

            <span class="quick-arrow">→</span>
          </router-link>

          <router-link to="/categories" class="quick-action">
            <span class="quick-icon">▤</span>

            <span>
              <strong>Gérer les catégories</strong>
              <small>Organiser votre catalogue</small>
            </span>

            <span class="quick-arrow">→</span>
          </router-link>

          <router-link to="/orders" class="quick-action">
            <span class="quick-icon">□</span>

            <span>
              <strong>Voir les commandes</strong>
              <small>Suivre les commandes clients</small>
            </span>

            <span class="quick-arrow">→</span>
          </router-link>

          <router-link to="/customers" class="quick-action">
            <span class="quick-icon">♙</span>

            <span>
              <strong>Voir les clients</strong>
              <small>Consulter votre clientèle</small>
            </span>

            <span class="quick-arrow">→</span>
          </router-link>
        </div>
      </section>
    </template>
  </div>
</template>
<style scoped>
.dashboard {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
}

/* LOADING */

.dashboard-loading {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.skeleton-card {
  height: 150px;
  padding: 20px;
  border: 1px solid #e8eaed;
  border-radius: 12px;
  background: #ffffff;
}

.skeleton {
  border-radius: 6px;
  background: #eceef1;
  animation: skeleton-pulse 1.4s ease-in-out infinite;
}

.skeleton-small {
  width: 35%;
  height: 11px;
}

.skeleton-large {
  width: 65%;
  height: 28px;
  margin-top: 22px;
}

.skeleton-medium {
  width: 45%;
  height: 10px;
  margin-top: 15px;
}

@keyframes skeleton-pulse {
  50% {
    opacity: 0.45;
  }
}

/* ERROR */

.error-panel {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 24px;
  border: 1px solid #f0d6d6;
  border-radius: 12px;
  background: #fffafa;
}

.error-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #f7e1e1;
  color: #b3261e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.error-content {
  flex: 1;
}

.error-content strong {
  display: block;
  color: #63211d;
  font-size: 0.84rem;
}

.error-content p {
  margin: 3px 0 0;
  color: #90635f;
  font-size: 0.75rem;
}

.retry-button {
  min-height: 36px;
  padding: 0 13px;
  border: 1px solid #dfc2c0;
  border-radius: 7px;
  background: #ffffff;
  color: #8c2923;
  cursor: pointer;
  font: inherit;
  font-size: 0.75rem;
  font-weight: 650;
}

/* STATS */

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

/* CARDS */

.dashboard-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
}

.dashboard-card {
  min-width: 0;
  padding: 24px;
  border: 1px solid #e8eaed;
  border-radius: 12px;
  background: #ffffff;
}

.revenue-card {
  min-height: 390px;
}

.orders-card {
  min-height: 390px;
}

.catalogue-card,
.customers-card {
  min-height: 270px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eef0f2;
}

.card-eyebrow {
  display: block;
  margin-bottom: 6px;
  color: #969ba3;
  font-size: 0.62rem;
  font-weight: 750;
  letter-spacing: 0.12em;
}

.card-header h2 {
  margin: 0;
  color: #181a1d;
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.card-period {
  padding: 6px 9px;
  border: 1px solid #e7e9ec;
  border-radius: 6px;
  color: #777d85;
  font-size: 0.65rem;
}

/* REVENUE */

.revenue-content {
  padding-top: 20px;
}

.revenue-value {
  color: #17191c;
  font-size: 2rem;
  font-weight: 750;
  letter-spacing: -0.045em;
}

.revenue-content p {
  margin: 7px 0 0;
  color: #8a9098;
  font-size: 0.73rem;
}

.chart-placeholder {
  position: relative;
  height: 175px;
  margin-top: 25px;
  overflow: hidden;
  border-radius: 8px;
  background: #fafafa;
}

.chart-grid-line {
  height: 1px;
  margin-top: 43px;
  background: #e9ebed;
}

.chart-message {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 5px;
  padding: 20px;
  text-align: center;
}

.chart-icon {
  color: #a0a5ad;
  font-size: 1.4rem;
}

.chart-message strong {
  color: #656b74;
  font-size: 0.76rem;
}

.chart-message span:last-child {
  max-width: 280px;
  color: #9a9fa7;
  font-size: 0.65rem;
  line-height: 1.45;
}

/* ORDERS */

.orders-summary {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 24px 0;
}

.orders-number {
  color: #17191c;
  font-size: 2rem;
  font-weight: 750;
  letter-spacing: -0.04em;
}

.orders-summary > span {
  color: #8a9098;
  font-size: 0.7rem;
}

.progress-section {
  margin-bottom: 22px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #777d85;
  font-size: 0.7rem;
}

.progress-label strong {
  color: #4d5259;
}

.progress-track {
  height: 7px;
  overflow: hidden;
  border-radius: 99px;
  background: #eceef0;
}

.progress-value {
  height: 100%;
  min-width: 2px;
  border-radius: inherit;
  background: #25282c;
  transition: width 0.4s ease;
}

.pending-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px;
  border: 1px solid #ece8df;
  border-radius: 8px;
  background: #fbfaf7;
}

.pending-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 7px;
  background: #efebe3;
  color: #756b5a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pending-box strong,
.pending-box span {
  display: block;
}

.pending-box strong {
  color: #4d4942;
  font-size: 0.72rem;
}

.pending-box span {
  margin-top: 2px;
  color: #9b958a;
  font-size: 0.65rem;
}

/* CATALOGUE */

.catalogue-stat {
  padding: 25px 0;
}

.catalogue-number {
  display: block;
  color: #17191c;
  font-size: 2.2rem;
  font-weight: 750;
  letter-spacing: -0.045em;
}

.catalogue-label {
  display: block;
  margin-top: 4px;
  color: #898f97;
  font-size: 0.72rem;
}

.catalogue-actions {
  display: flex;
  flex-direction: column;
  border-top: 1px solid #eef0f2;
}

.dashboard-link {
  min-height: 45px;
  color: #292c31;
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-decoration: none;
  font-size: 0.73rem;
  font-weight: 650;
  transition: color 0.18s ease;
}

.dashboard-link:hover {
  color: #000000;
}

.dashboard-link.secondary {
  border-top: 1px solid #eef0f2;
  color: #777d85;
}

/* CUSTOMERS */

.customer-stat {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 26px 0;
}

.customer-avatar {
  width: 43px;
  height: 43px;
  border-radius: 10px;
  background: #f1f2f3;
  color: #555b63;
  display: flex;
  align-items: center;
  justify-content: center;
}

.customer-stat strong,
.customer-stat span {
  display: block;
}

.customer-stat strong {
  color: #17191c;
  font-size: 1.35rem;
  font-weight: 750;
}

.customer-stat span {
  margin-top: 3px;
  color: #898f97;
  font-size: 0.7rem;
}

/* QUICK ACTIONS */

.quick-section {
  margin-top: 24px;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-heading h2 {
  margin: 0;
  color: #181a1d;
  font-size: 1rem;
}

.section-heading > span {
  color: #969ba3;
  font-size: 0.68rem;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.quick-action {
  min-width: 0;
  padding: 15px;
  border: 1px solid #e8eaed;
  border-radius: 10px;
  background: #ffffff;
  display: flex;
  align-items: center;
  gap: 11px;
  color: inherit;
  text-decoration: none;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.quick-action:hover {
  transform: translateY(-2px);
  border-color: #d9dce0;
  box-shadow: 0 7px 20px rgba(0, 0, 0, 0.045);
}

.quick-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 8px;
  background: #f2f3f4;
  color: #3f444b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.quick-action > span:nth-child(2) {
  min-width: 0;
  flex: 1;
}

.quick-action strong,
.quick-action small {
  display: block;
}

.quick-action strong {
  overflow: hidden;
  color: #353940;
  font-size: 0.7rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-action small {
  margin-top: 3px;
  overflow: hidden;
  color: #989da5;
  font-size: 0.61rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-arrow {
  color: #a0a5ad;
  font-size: 0.9rem;
}

/* RESPONSIVE */

@media (max-width: 1250px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1000px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .revenue-card,
  .orders-card {
    min-height: auto;
  }
}

@media (max-width: 700px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-card {
    padding: 19px;
  }

  .section-heading > span {
    display: none;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .error-panel {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .error-content {
    min-width: calc(100% - 50px);
  }

  .retry-button {
    width: 100%;
  }
}
/* RECENT ORDERS */

.recent-orders {
  margin-top: 24px;
}

.recent-orders-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef0f2;
}

.recent-orders-title,
.recent-orders-subtitle {
  display: block;
}

.recent-orders-title {
  color: #555b63;
  font-size: 0.7rem;
  font-weight: 700;
}

.recent-orders-subtitle {
  margin-top: 3px;
  color: #9a9fa7;
  font-size: 0.62rem;
}

.view-all-link {
  flex-shrink: 0;
  color: #555b63;
  text-decoration: none;
  font-size: 0.65rem;
  font-weight: 650;
}

.view-all-link:hover {
  color: #111315;
}

/* ORDER */

.recent-orders-list {
  display: flex;
  flex-direction: column;
}

.recent-order {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
  padding: 11px 0;
  border-bottom: 1px solid #f0f1f3;
  color: inherit;
  text-decoration: none;
}

.recent-order:last-child {
  border-bottom: 0;
}

.recent-order:hover {
  background: #fafafa;
}

.order-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 8px;
  background: #f1f2f3;
  color: #555b63;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 750;
}

.order-main {
  min-width: 0;
  flex: 1;
}

.order-main strong,
.order-main span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-main strong {
  color: #353940;
  font-size: 0.69rem;
  font-weight: 650;
}

.order-main span {
  margin-top: 3px;
  color: #989da5;
  font-size: 0.59rem;
}

.order-right {
  flex-shrink: 0;
  text-align: right;
}

.order-right > strong {
  display: block;
  color: #25282c;
  font-size: 0.68rem;
  font-weight: 700;
}

.order-status {
  display: inline-flex;
  margin-top: 4px;
  padding: 3px 6px;
  border-radius: 5px;
  font-size: 0.55rem;
  font-weight: 700;
}

/* STATUS */

.status-pending {
  background: #f5f0e7;
  color: #786b58;
}

.status-confirmed {
  background: #edf2f7;
  color: #52677c;
}

.status-shipped {
  background: #edf1f6;
  color: #52657a;
}

.status-delivered {
  background: #edf5ef;
  color: #4d7459;
}

.status-cancelled {
  background: #f7eded;
  color: #8a5555;
}

/* LOADING */

.recent-orders-loading {
  display: flex;
  flex-direction: column;
}

.recent-order-skeleton {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 0;
  border-bottom: 1px solid #f0f1f3;
}

.skeleton-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 8px;
}

.recent-skeleton-content {
  flex: 1;
}

.skeleton-order-name {
  width: 100px;
  height: 9px;
}

.skeleton-order-date {
  width: 140px;
  height: 7px;
  margin-top: 6px;
}

.skeleton-order-price {
  width: 60px;
  height: 9px;
}

/* ERROR */

.recent-orders-error {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 18px 0;
}

.recent-orders-error > span {
  width: 25px;
  height: 25px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #f7e1e1;
  color: #a8322b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 800;
}

.recent-orders-error p {
  flex: 1;
  margin: 0;
  color: #777d85;
  font-size: 0.65rem;
}

.recent-orders-error button {
  border: 1px solid #dedfe2;
  border-radius: 6px;
  background: #ffffff;
  color: #555b63;
  padding: 6px 9px;
  cursor: pointer;
  font: inherit;
  font-size: 0.6rem;
  font-weight: 650;
}

/* EMPTY */

.recent-orders-empty {
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 5px;
  text-align: center;
}

.recent-orders-empty > span {
  color: #a0a5ad;
  font-size: 1.2rem;
}

.recent-orders-empty strong {
  color: #656b74;
  font-size: 0.7rem;
}

.recent-orders-empty small {
  color: #9a9fa7;
  font-size: 0.6rem;
}
/* =========================
   Revenue chart
========================= */

.revenue-chart {
  width: 100%;
  height: 240px;
  margin-top: 24px;
  padding: 20px 10px 10px;
  border-top: 1px solid #eee;
}

.chart {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.chart-column {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-width: 0;
}

.chart-value {
  font-size: 11px;
  color: #777;
  white-space: nowrap;
}

.chart-bar {
  width: 32px;
  min-height: 8px;
  border-radius: 6px 6px 2px 2px;
  background: #111;
  transition: height 0.3s ease;
}

.chart-date {
  font-size: 11px;
  color: #888;
  text-transform: capitalize;
}

.chart-loading,
.chart-empty {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #777;
  font-size: 14px;
}

.chart-error {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #dc2626;
}

.chart-error p {
  margin: 0;
}

.chart-error button {
  border: 0;
  background: transparent;
  text-decoration: underline;
  cursor: pointer;
  color: inherit;
}
</style>
