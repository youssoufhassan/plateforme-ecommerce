<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { fetchMyOrders, type Order } from "../services/orderService";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();
const router = useRouter();
const activeTab = ref<"orders" | "profile">("orders");
const orders = ref<Order[]>([]);
const loadingOrders = ref(false);

const profile = ref({
  firstName: "",
  lastName: "",
  phone: "",
  address: "",
  city: "",
  postalCode: "",
});

const statusSteps = ["PENDING", "PAID", "PREPARING", "SHIPPED", "DELIVERED"];
const statusLabels: Record<string, string> = {
  PENDING: "En attente",
  PAID: "Payée",
  PREPARING: "En préparation",
  SHIPPED: "Expédiée",
  DELIVERED: "Livrée",
};

function statusIndex(status: string) {
  return statusSteps.indexOf(status);
}

onMounted(async () => {
  loadingOrders.value = true;
  try {
    orders.value = await fetchMyOrders();
  } catch (e) {
    console.error(e);
  } finally {
    loadingOrders.value = false;
  }
});

function saveProfile() {
  alert(
    "Ces informations ne sont pas encore sauvegardées côté serveur — fonctionnalité à venir.",
  );
}

function handleLogout() {
  authStore.logout();
  router.push("/");
}
</script>

<template>
  <div class="container">
    <div class="account-header">
      <h1>Mon compte</h1>
      <button class="logout-btn" @click="handleLogout">Se déconnecter</button>
    </div>

    <div class="account-tabs">
      <button
        class="account-tab"
        :class="{ active: activeTab === 'orders' }"
        @click="activeTab = 'orders'"
      >
        Mes commandes
      </button>
      <button
        class="account-tab"
        :class="{ active: activeTab === 'profile' }"
        @click="activeTab = 'profile'"
      >
        Mes informations
      </button>
    </div>

    <div v-if="activeTab === 'orders'" class="account-panel">
      <p v-if="loadingOrders">Chargement de tes commandes...</p>
      <p v-else-if="orders.length === 0">
        Tu n'as pas encore passé de commande.
      </p>

      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-card-header">
            <div>
              <span class="order-number"
                >Commande #{{ order.id.slice(0, 8) }}</span
              >
              <span class="order-date">{{
                new Date(order.createdAt).toLocaleDateString("fr-FR")
              }}</span>
            </div>
            <span class="order-total"
              >{{ order.totalAmount.toFixed(2) }} €</span
            >
          </div>

          <div class="order-status-track">
            <div
              v-for="(step, i) in statusSteps"
              :key="step"
              class="status-step"
              :class="{ done: i <= statusIndex(order.status) }"
            >
              <span class="status-dot" />
              <span class="status-label">{{ statusLabels[step] }}</span>
            </div>
          </div>

          <ul class="order-items">
            <li v-for="(item, i) in order.items" :key="i">
              {{ item.quantity }} × {{ item.productName }} —
              {{ item.unitPrice.toFixed(2) }} €
            </li>
          </ul>
        </div>
      </div>
    </div>

    <div v-else class="account-panel">
      <p class="profile-note">
        Ces informations ne sont pas encore enregistrées de façon permanente —
        fonctionnalité en cours de développement.
      </p>

      <div class="profile-field">
        <label>Email (compte)</label>
        <input
          :value="authStore.token ? 'Connecté' : ''"
          disabled
          type="text"
        />
      </div>

      <form @submit.prevent="saveProfile" class="profile-form">
        <div class="profile-field">
          <label>Prénom</label>
          <input v-model="profile.firstName" type="text" />
        </div>
        <div class="profile-field">
          <label>Nom</label>
          <input v-model="profile.lastName" type="text" />
        </div>
        <div class="profile-field">
          <label>Téléphone</label>
          <input v-model="profile.phone" type="tel" />
        </div>
        <div class="profile-field">
          <label>Adresse de livraison</label>
          <input
            v-model="profile.address"
            type="text"
            placeholder="Numéro et rue"
          />
        </div>
        <div class="profile-field-row">
          <div class="profile-field">
            <label>Ville</label>
            <input v-model="profile.city" type="text" />
          </div>
          <div class="profile-field">
            <label>Code postal</label>
            <input v-model="profile.postalCode" type="text" />
          </div>
        </div>
        <button
          type="submit"
          class="primary"
          style="width: auto; padding: 0.7rem 1.5rem"
        >
          Enregistrer
        </button>
      </form>
    </div>
  </div>
</template>
