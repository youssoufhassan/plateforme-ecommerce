```vue
<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";

import { fetchMyOrders, type Order } from "../services/orderService";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();
const router = useRouter();

/* =========================================================
   TYPES
========================================================= */

type AccountTab = "orders" | "profile" | "addresses" | "security";

/* =========================================================
   ÉTAT
========================================================= */

const activeTab = ref<AccountTab>("orders");

const orders = ref<Order[]>([]);
const loadingOrders = ref(false);
const ordersError = ref("");

/* =========================================================
   PROFIL
   Pour le moment, les données qui ne viennent pas encore
   du backend utilisent des valeurs par défaut.
========================================================= */

const profile = ref({
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  address: "",
  city: "",
  postalCode: "",
});

/* =========================================================
   MESSAGE PROFIL
========================================================= */

const profileMessage = ref("");
const profileMessageType = ref<"success" | "info" | "error">("info");

/* =========================================================
   SÉCURITÉ
   Fonctionnalité préparée pour le futur backend.
========================================================= */

const security = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const securityMessage = ref("");

/* =========================================================
   ÉTAPES COMMANDE
========================================================= */

const statusSteps = ["PENDING", "PAID", "PREPARING", "SHIPPED", "DELIVERED"];

const statusLabels: Record<string, string> = {
  PENDING: "En attente",
  PAID: "Payée",
  PREPARING: "En préparation",
  SHIPPED: "Expédiée",
  DELIVERED: "Livrée",
};

/* =========================================================
   INFORMATIONS UTILISATEUR
========================================================= */

const userDisplayName = computed(() => {
  /*
   * Pour l'instant, on utilise les informations disponibles.
   * On pourra remplacer cela par authStore.user lorsque
   * le backend exposera les informations complètes.
   */

  if (profile.value.firstName || profile.value.lastName) {
    return `${profile.value.firstName} ${profile.value.lastName}`.trim();
  }

  return "Mon compte";
});

const userInitials = computed(() => {
  const first = profile.value.firstName?.charAt(0) || "";
  const last = profile.value.lastName?.charAt(0) || "";

  const initials = `${first}${last}`.toUpperCase();

  return initials || "U";
});

/* =========================================================
   STATUS
========================================================= */

function statusIndex(status: string): number {
  const index = statusSteps.indexOf(status);

  return index >= 0 ? index : 0;
}

function isStatusDone(status: string, currentStatus: string): boolean {
  return statusIndex(status) <= statusIndex(currentStatus);
}

function getStatusLabel(status: string): string {
  return statusLabels[status] || "En cours";
}

/* =========================================================
   FORMATAGE
========================================================= */

function formatDate(date: string): string {
  if (!date) return "Date inconnue";

  const parsedDate = new Date(date);

  if (Number.isNaN(parsedDate.getTime())) {
    return "Date inconnue";
  }

  return parsedDate.toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "long",
    year: "numeric",
  });
}

function formatPrice(value: number): string {
  return Number(value || 0)
    .toFixed(2)
    .replace(".", ",");
}

function getOrderShortId(id: string): string {
  if (!id) return "—";

  return id.slice(0, 8).toUpperCase();
}

/* =========================================================
   CHARGEMENT DES COMMANDES
========================================================= */

async function loadOrders() {
  loadingOrders.value = true;
  ordersError.value = "";

  try {
    orders.value = await fetchMyOrders();
  } catch (error) {
    console.error("Erreur lors du chargement des commandes :", error);

    ordersError.value = "Impossible de charger tes commandes pour le moment.";
  } finally {
    loadingOrders.value = false;
  }
}

onMounted(async () => {
  /*
   * Valeurs par défaut temporaires.
   * Elles seront remplacées par les données du backend
   * lorsque l'API utilisateur sera disponible.
   */

  profile.value = {
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    postalCode: "",
  };

  await loadOrders();
});

/* =========================================================
   NAVIGATION
========================================================= */

function changeTab(tab: AccountTab) {
  activeTab.value = tab;

  profileMessage.value = "";
  securityMessage.value = "";
}

function goToHome() {
  router.push("/");
}

/* =========================================================
   DÉCONNEXION
========================================================= */

function handleLogout() {
  authStore.logout();

  router.push("/");
}

/* =========================================================
   PROFIL
========================================================= */

function saveProfile() {
  /*
   * Le backend ne permet pas encore de sauvegarder ces
   * informations.
   *
   * On simule donc une sauvegarde côté interface.
   */

  profileMessageType.value = "info";

  profileMessage.value =
    "Tes informations sont prêtes. La sauvegarde sera disponible lorsque l'API utilisateur sera mise en place.";
}

/* =========================================================
   SÉCURITÉ
========================================================= */

function changePassword() {
  securityMessage.value =
    "La modification du mot de passe sera disponible lorsque l'API de sécurité sera mise en place.";
}

/* =========================================================
   ADRESSES
========================================================= */

function saveAddress() {
  profileMessageType.value = "info";

  profileMessage.value =
    "La gestion des adresses sera disponible avec la prochaine version du backend.";
}

/* =========================================================
   COMMANDE
========================================================= */

function getCurrentStatusText(status: string): string {
  return getStatusLabel(status);
}
</script>

<template>
  <div class="account-page">
    <div class="container">
      <!-- =====================================================
           HEADER
      ====================================================== -->

      <section class="account-header">
        <div class="account-heading">
          <button
            type="button"
            class="account-avatar"
            aria-label="Avatar utilisateur"
          >
            {{ userInitials }}
          </button>

          <div>
            <p class="account-eyebrow">ESPACE PERSONNEL</p>

            <h1>
              Bonjour{{
                userDisplayName !== "Mon compte" ? `, ${userDisplayName}` : ""
              }}
            </h1>

            <p class="account-subtitle">
              Gère ton compte, tes commandes et tes informations personnelles.
            </p>
          </div>
        </div>

        <button type="button" class="logout-btn" @click="handleLogout">
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
          >
            <path d="M10 17l5-5-5-5" />
            <path d="M15 12H3" />
            <path d="M21 19V5a2 2 0 0 0-2-2h-6" />
          </svg>

          Se déconnecter
        </button>
      </section>

      <!-- =====================================================
           NAVIGATION
      ====================================================== -->

      <nav class="account-navigation">
        <button
          type="button"
          class="account-nav-item"
          :class="{ active: activeTab === 'orders' }"
          @click="changeTab('orders')"
        >
          <span class="account-nav-icon">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
            >
              <path d="M6 2h12v20H6z" />
              <path d="M9 6h6" />
              <path d="M9 10h6" />
              <path d="M9 14h4" />
            </svg>
          </span>

          <span>
            <strong>Mes commandes</strong>
            <small>Suivre mes achats</small>
          </span>
        </button>

        <button
          type="button"
          class="account-nav-item"
          :class="{ active: activeTab === 'profile' }"
          @click="changeTab('profile')"
        >
          <span class="account-nav-icon">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
            >
              <circle cx="12" cy="8" r="3.5" />
              <path d="M5 21c.8-4 3.1-6 7-6s6.2 2 7 6" />
            </svg>
          </span>

          <span>
            <strong>Mes informations</strong>
            <small>Gérer mon profil</small>
          </span>
        </button>

        <button
          type="button"
          class="account-nav-item"
          :class="{ active: activeTab === 'addresses' }"
          @click="changeTab('addresses')"
        >
          <span class="account-nav-icon">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
            >
              <path d="M12 21s7-6.2 7-12a7 7 0 1 0-14 0c0 5.8 7 12 7 12z" />
              <circle cx="12" cy="9" r="2.5" />
            </svg>
          </span>

          <span>
            <strong>Mes adresses</strong>
            <small>Livraison et facturation</small>
          </span>
        </button>

        <button
          type="button"
          class="account-nav-item"
          :class="{ active: activeTab === 'security' }"
          @click="changeTab('security')"
        >
          <span class="account-nav-icon">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
            >
              <rect x="5" y="10" width="14" height="10" rx="2" />
              <path d="M8 10V7a4 4 0 0 1 8 0v3" />
            </svg>
          </span>

          <span>
            <strong>Sécurité</strong>
            <small>Mot de passe</small>
          </span>
        </button>
      </nav>

      <!-- =====================================================
           MES COMMANDES
      ====================================================== -->

      <section v-if="activeTab === 'orders'" class="account-panel">
        <div class="panel-header">
          <div>
            <p class="panel-eyebrow">HISTORIQUE</p>

            <h2>Mes commandes</h2>

            <p>Consulte l'historique et le statut de tes achats.</p>
          </div>

          <span class="order-count">
            {{ orders.length }}
            commande{{ orders.length > 1 ? "s" : "" }}
          </span>
        </div>

        <!-- ERREUR -->

        <div v-if="ordersError" class="account-message error">
          {{ ordersError }}

          <button type="button" @click="loadOrders">Réessayer</button>
        </div>

        <!-- CHARGEMENT -->

        <div v-else-if="loadingOrders" class="orders-loading">
          <div v-for="i in 3" :key="i" class="order-skeleton">
            <div class="skeleton-line large"></div>
            <div class="skeleton-line"></div>
            <div class="skeleton-line"></div>
          </div>
        </div>

        <!-- AUCUNE COMMANDE -->

        <div v-else-if="orders.length === 0" class="orders-empty">
          <div class="empty-icon">
            <svg
              width="36"
              height="36"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M6 2h12v20H6z" />
              <path d="M9 6h6" />
              <path d="M9 10h6" />
            </svg>
          </div>

          <h3>Aucune commande pour le moment</h3>

          <p>Tes prochaines commandes apparaîtront ici.</p>

          <button type="button" class="primary-button" @click="goToHome">
            Découvrir nos produits
          </button>
        </div>

        <!-- COMMANDES -->

        <div v-else class="order-list">
          <article v-for="order in orders" :key="order.id" class="order-card">
            <!-- HEADER COMMANDE -->

            <div class="order-card-header">
              <div>
                <span class="order-number">
                  Commande #{{ getOrderShortId(order.id) }}
                </span>

                <span class="order-date">
                  {{ formatDate(order.createdAt) }}
                </span>
              </div>

              <div class="order-total">
                {{ formatPrice(order.totalAmount) }} €
              </div>
            </div>

            <!-- STATUT -->

            <div class="order-status">
              <div class="status-current">
                <span class="status-current-label"> Statut actuel </span>

                <strong>
                  {{ getCurrentStatusText(order.status) }}
                </strong>
              </div>

              <div class="status-track">
                <div
                  v-for="(step, index) in statusSteps"
                  :key="step"
                  class="status-step"
                  :class="{
                    done: isStatusDone(step, order.status),
                    current: step === order.status,
                  }"
                >
                  <div class="status-dot">
                    <svg
                      v-if="index < statusIndex(order.status)"
                      width="12"
                      height="12"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <path d="M5 12l4 4L19 6" />
                    </svg>
                  </div>

                  <span>
                    {{ statusLabels[step] }}
                  </span>
                </div>
              </div>
            </div>

            <!-- PRODUITS -->

            <div class="order-products">
              <h4>Produits commandés</h4>

              <div
                v-for="(item, index) in order.items"
                :key="index"
                class="order-product"
              >
                <div class="order-product-info">
                  <strong>
                    {{ item.productName }}
                  </strong>

                  <span> Quantité : {{ item.quantity }} </span>
                </div>

                <span class="order-product-price">
                  {{ formatPrice(item.unitPrice * item.quantity) }} €
                </span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- =====================================================
           INFORMATIONS PERSONNELLES
      ====================================================== -->

      <section v-else-if="activeTab === 'profile'" class="account-panel">
        <div class="panel-header">
          <div>
            <p class="panel-eyebrow">MON PROFIL</p>

            <h2>Mes informations</h2>

            <p>Consulte et modifie tes informations personnelles.</p>
          </div>
        </div>

        <div class="account-message info">
          Les informations personnelles seront synchronisées avec ton compte
          lorsque l'API utilisateur sera disponible.
        </div>

        <form class="profile-form" @submit.prevent="saveProfile">
          <div class="profile-grid">
            <div class="profile-field">
              <label for="firstName"> Prénom </label>

              <input
                id="firstName"
                v-model="profile.firstName"
                type="text"
                placeholder="Ton prénom"
              />
            </div>

            <div class="profile-field">
              <label for="lastName"> Nom </label>

              <input
                id="lastName"
                v-model="profile.lastName"
                type="text"
                placeholder="Ton nom"
              />
            </div>

            <div class="profile-field full">
              <label for="email"> Adresse e-mail </label>

              <input
                id="email"
                v-model="profile.email"
                type="email"
                placeholder="ton@email.com"
              />
            </div>

            <div class="profile-field full">
              <label for="phone"> Numéro de téléphone </label>

              <input
                id="phone"
                v-model="profile.phone"
                type="tel"
                placeholder="+33 6 00 00 00 00"
              />
            </div>
          </div>

          <div class="form-section">
            <div class="form-section-heading">
              <h3>Adresse de livraison</h3>

              <p>Utilisée pour tes prochaines commandes.</p>
            </div>

            <div class="profile-grid">
              <div class="profile-field full">
                <label for="address"> Adresse </label>

                <input
                  id="address"
                  v-model="profile.address"
                  type="text"
                  placeholder="Numéro et rue"
                />
              </div>

              <div class="profile-field">
                <label for="city"> Ville </label>

                <input
                  id="city"
                  v-model="profile.city"
                  type="text"
                  placeholder="Ville"
                />
              </div>

              <div class="profile-field">
                <label for="postalCode"> Code postal </label>

                <input
                  id="postalCode"
                  v-model="profile.postalCode"
                  type="text"
                  placeholder="33000"
                />
              </div>
            </div>
          </div>

          <div
            v-if="profileMessage"
            class="account-message"
            :class="profileMessageType"
          >
            {{ profileMessage }}
          </div>

          <button type="submit" class="primary-button">
            Enregistrer mes informations
          </button>
        </form>
      </section>

      <!-- =====================================================
           ADRESSES
      ====================================================== -->

      <section v-else-if="activeTab === 'addresses'" class="account-panel">
        <div class="panel-header">
          <div>
            <p class="panel-eyebrow">LIVRAISON</p>

            <h2>Mes adresses</h2>

            <p>Gère tes adresses de livraison et de facturation.</p>
          </div>
        </div>

        <div class="address-grid">
          <article class="address-card">
            <div class="address-card-header">
              <div>
                <span class="address-icon">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.7"
                  >
                    <path
                      d="M12 21s7-6.2 7-12a7 7 0 1 0-14 0c0 5.8 7 12 7 12z"
                    />
                    <circle cx="12" cy="9" r="2.5" />
                  </svg>
                </span>

                <h3>Adresse de livraison</h3>
              </div>

              <span class="address-badge"> Par défaut </span>
            </div>

            <div class="address-placeholder">
              <p>Aucune adresse enregistrée.</p>

              <span>
                La gestion des adresses sera connectée au backend prochainement.
              </span>
            </div>

            <button type="button" class="secondary-button" @click="saveAddress">
              Ajouter une adresse
            </button>
          </article>

          <article class="address-card">
            <div class="address-card-header">
              <div>
                <span class="address-icon">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.7"
                  >
                    <rect x="3" y="5" width="18" height="14" rx="2" />
                    <path d="M3 10h18" />
                  </svg>
                </span>

                <h3>Adresse de facturation</h3>
              </div>
            </div>

            <div class="address-placeholder">
              <p>Aucune adresse enregistrée.</p>

              <span>
                Cette fonctionnalité sera disponible avec le backend de gestion
                des adresses.
              </span>
            </div>

            <button type="button" class="secondary-button" @click="saveAddress">
              Ajouter une adresse
            </button>
          </article>
        </div>
      </section>

      <!-- =====================================================
           SÉCURITÉ
      ====================================================== -->

      <section v-else-if="activeTab === 'security'" class="account-panel">
        <div class="panel-header">
          <div>
            <p class="panel-eyebrow">SÉCURITÉ</p>

            <h2>Sécurité du compte</h2>

            <p>Gère ton mot de passe et la sécurité de ton compte.</p>
          </div>
        </div>

        <div class="security-box">
          <div class="security-icon">
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.7"
            >
              <rect x="5" y="10" width="14" height="10" rx="2" />
              <path d="M8 10V7a4 4 0 0 1 8 0v3" />
            </svg>
          </div>

          <div>
            <h3>Modifier mon mot de passe</h3>

            <p>Cette fonctionnalité sera reliée à l'API d'authentification.</p>
          </div>
        </div>

        <form class="security-form" @submit.prevent="changePassword">
          <div class="profile-field">
            <label for="currentPassword"> Mot de passe actuel </label>

            <input
              id="currentPassword"
              v-model="security.currentPassword"
              type="password"
              placeholder="••••••••"
            />
          </div>

          <div class="profile-field">
            <label for="newPassword"> Nouveau mot de passe </label>

            <input
              id="newPassword"
              v-model="security.newPassword"
              type="password"
              placeholder="••••••••"
            />
          </div>

          <div class="profile-field">
            <label for="confirmPassword">
              Confirmer le nouveau mot de passe
            </label>

            <input
              id="confirmPassword"
              v-model="security.confirmPassword"
              type="password"
              placeholder="••••••••"
            />
          </div>

          <div v-if="securityMessage" class="account-message info">
            {{ securityMessage }}
          </div>

          <button type="submit" class="primary-button">
            Modifier le mot de passe
          </button>
        </form>
      </section>
    </div>
  </div>
</template>
```
