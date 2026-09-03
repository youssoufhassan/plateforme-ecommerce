<script setup lang="ts">
import { onMounted, ref } from "vue";
import api from "../services/api";

const stats = ref({
  totalOrders: 0,
  totalCustomers: 0,
  totalProducts: 0,
  totalRevenue: 0,
  pendingOrders: 0,
});
const loading = ref(true);
const error = ref("");

onMounted(async () => {
  try {
    const response = await api.get("/admin/dashboard/stats");
    stats.value = response.data;
  } catch (e) {
    error.value = "Impossible de charger les statistiques.";
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div>
    <h1 class="page-title">Tableau de bord</h1>

    <p v-if="loading">Chargement...</p>
    <p v-else-if="error" style="color: #b3261e">{{ error }}</p>

    <div v-else class="stat-grid">
      <div class="stat-card">
        <div class="value">{{ stats.totalRevenue.toFixed(2) }} €</div>
        <div class="label">Chiffre d'affaires (hors en attente)</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.totalOrders }}</div>
        <div class="label">Commandes totales</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.pendingOrders }}</div>
        <div class="label">Commandes en attente</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.totalCustomers }}</div>
        <div class="label">Clients</div>
      </div>
    </div>

    <div class="stat-grid" style="grid-template-columns: 1fr">
      <div class="stat-card">
        <div class="value">{{ stats.totalProducts }}</div>
        <div class="label">Produits au catalogue</div>
      </div>
    </div>
  </div>
</template>
