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
  items: OrderItem[];
}

const orders = ref<Order[]>([]);
const loading = ref(true);
const search = ref("");
const statusFilter = ref("Tous");
const expandedId = ref<string | null>(null);
const toast = ref({ message: "", type: "success" as "success" | "error" });

const statuses = ["PENDING", "PAID", "PREPARING", "SHIPPED", "DELIVERED"];

function showToast(message: string, type: "success" | "error" = "success") {
  toast.value = { message, type };
  setTimeout(() => (toast.value.message = ""), 3000);
}

async function loadOrders() {
  loading.value = true;
  try {
    const response = await api.get("/orders/admin");
    orders.value = response.data;
  } catch (e) {
    showToast("Impossible de charger les commandes.", "error");
  } finally {
    loading.value = false;
  }
}

onMounted(loadOrders);

const filtered = computed(() =>
  orders.value.filter((o) => {
    const matchSearch = o.id.toLowerCase().includes(search.value.toLowerCase());
    const matchStatus =
      statusFilter.value === "Tous" || o.status === statusFilter.value;
    return matchSearch && matchStatus;
  }),
);

function toggleExpand(id: string) {
  expandedId.value = expandedId.value === id ? null : id;
}

async function updateStatus(id: string, status: string) {
  try {
    await api.put(`/orders/${id}/status`, { status });
    showToast("Statut mis à jour.");
    await loadOrders();
  } catch (e: any) {
    showToast(
      e.response?.data?.message || "Erreur lors de la mise à jour.",
      "error",
    );
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">Commandes</h1>

    <div class="toolbar">
      <input
        v-model="search"
        type="text"
        placeholder="Rechercher par n° de commande..."
        class="search-input"
      />
      <select v-model="statusFilter" class="search-input" style="width: 180px">
        <option>Tous</option>
        <option v-for="s in statuses" :key="s">{{ s }}</option>
      </select>
    </div>

    <p v-if="loading">Chargement...</p>

    <table v-else class="admin-table">
      <thead>
        <tr>
          <th>N° commande</th>
          <th>Date</th>
          <th>Total</th>
          <th>Statut</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <template v-for="order in filtered" :key="order.id">
          <tr>
            <td>#{{ order.id.slice(0, 8) }}</td>
            <td>{{ new Date(order.createdAt).toLocaleDateString("fr-FR") }}</td>
            <td>{{ order.totalAmount.toFixed(2) }} €</td>
            <td>
              <select
                :value="order.status"
                @change="
                  updateStatus(
                    order.id,
                    ($event.target as HTMLSelectElement).value,
                  )
                "
              >
                <option v-for="s in statuses" :key="s" :value="s">
                  {{ s }}
                </option>
              </select>
            </td>
            <td>
              <button class="btn" @click="toggleExpand(order.id)">
                {{ expandedId === order.id ? "Masquer" : "Détail" }}
              </button>
            </td>
          </tr>
          <tr v-if="expandedId === order.id">
            <td colspan="5" style="background: var(--bg)">
              <ul style="margin: 0; padding-left: 1.2rem">
                <li v-for="(item, i) in order.items" :key="i">
                  {{ item.quantity }} × {{ item.productName }} —
                  {{ item.unitPrice.toFixed(2) }} €
                </li>
              </ul>
            </td>
          </tr>
        </template>
      </tbody>
    </table>

    <div v-if="toast.message" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
