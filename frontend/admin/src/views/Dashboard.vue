<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import api from "../services/api";
import PageHeader from "../components/PageHeader.vue";
import StatCard from "../components/StatCard.vue";

interface DashboardStats {
  totalOrders: number;
  totalCustomers: number;
  totalProducts: number;
  totalRevenue: number;
  pendingOrders: number;
}

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

function retry() {
  loadDashboard();
}

onMounted(loadDashboard);
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

            <span class="card-period">Vue actuelle</span>
          </div>

          <div class="revenue-content">
            <div class="revenue-value">
              {{ revenueFormatted }}
            </div>

            <p>Chiffre d'affaires généré par les commandes prises en compte.</p>
          </div>

          <div class="chart-placeholder">
            <div class="chart-grid-line"></div>
            <div class="chart-grid-line"></div>
            <div class="chart-grid-line"></div>

            <div class="chart-message">
              <span class="chart-icon">⌁</span>
              <strong>Historique des ventes</strong>
              <span>
                Les données d'évolution seront disponibles avec l'endpoint
                analytics.
              </span>
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
              <strong
                >{{ stats.pendingOrders }} commande{{
                  stats.pendingOrders > 1 ? "s" : ""
                }}</strong
              >
              <span
                >nécessite{{ stats.pendingOrders > 1 ? "nt" : "" }} votre
                attention</span
              >
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
    ```
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
</style>
