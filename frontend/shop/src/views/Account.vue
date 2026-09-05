<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";

import { fetchMyOrders, type Order } from "../services/orderService";
import { fetchProfile, updateProfile } from "../services/profileService";
import {
  fetchAddresses,
  createAddress,
  type Address,
} from "../services/addressService";
import { changePassword as changePasswordApi } from "../services/authService";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();
const router = useRouter();

/* =========================================================
   TYPES
========================================================= */

type AccountTab = "orders" | "profile" | "addresses" | "security";

/* =========================================================
   ONGLET ACTIF
========================================================= */

const activeTab = ref<AccountTab>("orders");

/* =========================================================
   COMMANDES
========================================================= */

const orders = ref<Order[]>([]);
const loadingOrders = ref(false);
const ordersError = ref("");

/* =========================================================
   PROFIL
========================================================= */

const profile = ref({
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
});

const profileMessage = ref("");
const profileMessageType = ref<"success" | "info" | "error">("info");

/* =========================================================
   ADRESSES
========================================================= */

const addresses = ref<Address[]>([]);

const newAddress = ref({
  street: "",
  city: "",
  postalCode: "",
  country: "France",
});

const addressMessage = ref("");
const addressMessageType = ref<"success" | "error">("success");

/* =========================================================
   SÉCURITÉ
========================================================= */

const security = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const securityMessage = ref("");
const securityMessageType = ref<"success" | "error">("success");

/* =========================================================
   STATUT COMMANDES
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
   NOM UTILISATEUR
========================================================= */

const userDisplayName = computed(() => {
  if (profile.value.firstName || profile.value.lastName) {
    return `${profile.value.firstName} ${profile.value.lastName}`.trim();
  }

  return "Mon compte";
});

/* =========================================================
   INITIALES
========================================================= */

const userInitials = computed(() => {
  const first = profile.value.firstName?.charAt(0) || "";
  const last = profile.value.lastName?.charAt(0) || "";

  const initials = `${first}${last}`.toUpperCase();

  return initials || "U";
});

/* =========================================================
   STATUT
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
   FORMAT DATE
========================================================= */

function formatDate(date: string): string {
  if (!date) {
    return "Date inconnue";
  }

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

/* =========================================================
   FORMAT PRIX
========================================================= */

function formatPrice(value: number): string {
  return Number(value || 0)
    .toFixed(2)
    .replace(".", ",");
}

/* =========================================================
   ID COMMANDE
========================================================= */

function getOrderShortId(id: string): string {
  if (!id) {
    return "—";
  }

  return id.slice(0, 8).toUpperCase();
}

/* =========================================================
   CHARGER LES COMMANDES
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

/* =========================================================
   CHARGER LE PROFIL
========================================================= */

async function loadProfile() {
  try {
    const data = await fetchProfile();

    profile.value = {
      firstName: data.firstName || "",
      lastName: data.lastName || "",
      email: data.email || "",
      phone: data.phone || "",
    };
  } catch (error) {
    console.error("Erreur lors du chargement du profil :", error);
  }
}

/* =========================================================
   CHARGER LES ADRESSES
========================================================= */

async function loadAddresses() {
  try {
    addresses.value = await fetchAddresses();
  } catch (error) {
    console.error("Erreur lors du chargement des adresses :", error);
  }
}

/* =========================================================
   CHARGEMENT INITIAL
========================================================= */

onMounted(async () => {
  await Promise.all([loadOrders(), loadProfile(), loadAddresses()]);
});

/* =========================================================
   CHANGER D'ONGLET
========================================================= */

function changeTab(tab: AccountTab) {
  activeTab.value = tab;

  profileMessage.value = "";
  addressMessage.value = "";
  securityMessage.value = "";
}

/* =========================================================
   RETOUR ACCUEIL
========================================================= */

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
   SAUVEGARDER LE PROFIL
========================================================= */

async function saveProfile() {
  profileMessage.value = "";

  try {
    await updateProfile({
      firstName: profile.value.firstName,
      lastName: profile.value.lastName,
      phone: profile.value.phone,
    });

    profileMessageType.value = "success";

    profileMessage.value = "Tes informations ont été enregistrées.";

    await loadProfile();
  } catch (error: any) {
    console.error("Erreur lors de la modification du profil :", error);

    profileMessageType.value = "error";

    profileMessage.value =
      error?.response?.data?.message ||
      "Une erreur est survenue lors de la sauvegarde.";
  }
}

/* =========================================================
   CHANGER LE MOT DE PASSE
========================================================= */

async function changePassword() {
  securityMessage.value = "";

  if (!security.value.currentPassword) {
    securityMessageType.value = "error";
    securityMessage.value = "Veuillez saisir votre mot de passe actuel.";
    return;
  }

  if (!security.value.newPassword) {
    securityMessageType.value = "error";
    securityMessage.value = "Veuillez saisir un nouveau mot de passe.";
    return;
  }

  if (security.value.newPassword !== security.value.confirmPassword) {
    securityMessageType.value = "error";
    securityMessage.value = "Les deux mots de passe ne correspondent pas.";
    return;
  }

  try {
    await changePasswordApi(
      security.value.currentPassword,
      security.value.newPassword,
    );

    securityMessageType.value = "success";

    securityMessage.value = "Mot de passe modifié avec succès.";

    security.value = {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    };
  } catch (error: any) {
    console.error("Erreur lors du changement de mot de passe :", error);

    securityMessageType.value = "error";

    securityMessage.value =
      error?.response?.data?.message || "Le mot de passe actuel est incorrect.";
  }
}

/* =========================================================
   AJOUTER UNE ADRESSE
========================================================= */

async function saveAddress() {
  addressMessage.value = "";

  try {
    await createAddress({
      street: newAddress.value.street,
      city: newAddress.value.city,
      postalCode: newAddress.value.postalCode,
      country: newAddress.value.country,
    });

    await loadAddresses();

    newAddress.value = {
      street: "",
      city: "",
      postalCode: "",
      country: "France",
    };

    addressMessageType.value = "success";

    addressMessage.value = "Adresse ajoutée avec succès.";
  } catch (error: any) {
    console.error("Erreur lors de l'ajout de l'adresse :", error);

    addressMessageType.value = "error";

    addressMessage.value =
      error?.response?.data?.message || "Erreur lors de l'ajout de l'adresse.";
  }
}

/* =========================================================
   STATUT ACTUEL
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
            <!-- HEADER -->

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
                  {{ formatPrice(item.unitPrice * item.quantity) }}
                  €
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

        <form class="profile-form" @submit.prevent="saveProfile">
          <div class="profile-grid">
            <!-- PRÉNOM -->

            <div class="profile-field">
              <label for="firstName"> Prénom </label>

              <input
                id="firstName"
                v-model="profile.firstName"
                type="text"
                placeholder="Ton prénom"
              />
            </div>

            <!-- NOM -->

            <div class="profile-field">
              <label for="lastName"> Nom </label>

              <input
                id="lastName"
                v-model="profile.lastName"
                type="text"
                placeholder="Ton nom"
              />
            </div>

            <!-- EMAIL -->

            <div class="profile-field full">
              <label for="email"> Adresse e-mail </label>

              <input id="email" v-model="profile.email" type="email" disabled />
            </div>

            <!-- TÉLÉPHONE -->

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

          <!-- MESSAGE -->

          <div
            v-if="profileMessage"
            class="account-message"
            :class="profileMessageType"
          >
            {{ profileMessage }}
          </div>

          <!-- BOUTON -->

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

            <p>Gère tes adresses de livraison.</p>
          </div>
        </div>

        <!-- ADRESSES EXISTANTES -->

        <div v-if="addresses.length > 0" class="address-grid">
          <article
            v-for="addr in addresses"
            :key="addr.id"
            class="address-card"
          >
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

                <h3>
                  {{ addr.street }}
                </h3>
              </div>
            </div>

            <p>{{ addr.city }}, {{ addr.postalCode }}</p>

            <p>
              {{ addr.country }}
            </p>
          </article>
        </div>

        <!-- AUCUNE ADRESSE -->

        <p v-else style="color: var(--ink-soft); margin-bottom: var(--space-4)">
          Aucune adresse enregistrée pour l'instant.
        </p>

        <!-- AJOUT ADRESSE -->

        <form class="profile-form" @submit.prevent="saveAddress">
          <div class="profile-grid">
            <!-- RUE -->

            <div class="profile-field full">
              <label for="street"> Rue </label>

              <input
                id="street"
                v-model="newAddress.street"
                type="text"
                required
                placeholder="12 rue de la Paix"
              />
            </div>

            <!-- VILLE -->

            <div class="profile-field">
              <label for="city"> Ville </label>

              <input
                id="city"
                v-model="newAddress.city"
                type="text"
                required
                placeholder="Bordeaux"
              />
            </div>

            <!-- CODE POSTAL -->

            <div class="profile-field">
              <label for="postalCode"> Code postal </label>

              <input
                id="postalCode"
                v-model="newAddress.postalCode"
                type="text"
                required
                placeholder="33000"
              />
            </div>

            <!-- PAYS -->

            <div class="profile-field full">
              <label for="country"> Pays </label>

              <input
                id="country"
                v-model="newAddress.country"
                type="text"
                required
                placeholder="France"
              />
            </div>
          </div>

          <!-- MESSAGE -->

          <div
            v-if="addressMessage"
            class="account-message"
            :class="addressMessageType"
          >
            {{ addressMessage }}
          </div>

          <!-- BOUTON -->

          <button type="submit" class="primary-button">
            Ajouter cette adresse
          </button>
        </form>
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

        <!-- INFO -->

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

            <p>
              Utilise un mot de passe suffisamment sécurisé pour protéger ton
              compte.
            </p>
          </div>
        </div>

        <!-- FORMULAIRE -->

        <form class="security-form" @submit.prevent="changePassword">
          <!-- ANCIEN MOT DE PASSE -->

          <div class="profile-field">
            <label for="currentPassword"> Mot de passe actuel </label>

            <input
              id="currentPassword"
              v-model="security.currentPassword"
              type="password"
              required
              placeholder="••••••••"
            />
          </div>

          <!-- NOUVEAU MOT DE PASSE -->

          <div class="profile-field">
            <label for="newPassword"> Nouveau mot de passe </label>

            <input
              id="newPassword"
              v-model="security.newPassword"
              type="password"
              required
              placeholder="••••••••"
            />
          </div>

          <!-- CONFIRMATION -->

          <div class="profile-field">
            <label for="confirmPassword">
              Confirmer le nouveau mot de passe
            </label>

            <input
              id="confirmPassword"
              v-model="security.confirmPassword"
              type="password"
              required
              placeholder="••••••••"
            />
          </div>

          <!-- MESSAGE -->

          <div
            v-if="securityMessage"
            class="account-message"
            :class="securityMessageType"
          >
            {{ securityMessage }}
          </div>

          <!-- BOUTON -->

          <button type="submit" class="primary-button">
            Modifier le mot de passe
          </button>
        </form>
      </section>
    </div>
  </div>
</template>
```
