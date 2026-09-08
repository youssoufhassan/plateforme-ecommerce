<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import api from "../services/api";

interface Customer {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
}

const customers = ref<Customer[]>([]);
const loading = ref(true);
const error = ref("");
const search = ref("");
const sortOrder = ref<"az" | "za">("az");

function getFullName(customer: Customer) {
  const name = [customer.firstName, customer.lastName]
    .filter(Boolean)
    .join(" ")
    .trim();

  return name || "Client sans nom";
}

function getInitials(customer: Customer) {
  const first = customer.firstName?.trim().charAt(0) || "";
  const last = customer.lastName?.trim().charAt(0) || "";

  const initials = `${first}${last}`.toUpperCase();

  if (initials) return initials;

  return customer.email.charAt(0).toUpperCase();
}

async function loadCustomers() {
  loading.value = true;
  error.value = "";

  try {
    const response = await api.get<Customer[]>("/admin/customers");
    customers.value = response.data;
  } catch (e) {
    error.value = "Impossible de charger les clients.";
  } finally {
    loading.value = false;
  }
}

onMounted(loadCustomers);

const filteredCustomers = computed(() => {
  const query = search.value.trim().toLowerCase();

  const result = customers.value.filter((customer) => {
    if (!query) return true;

    const fullName = getFullName(customer).toLowerCase();

    return (
      fullName.includes(query) || customer.email.toLowerCase().includes(query)
    );
  });

  return [...result].sort((a, b) => {
    const nameA = getFullName(a).toLowerCase();
    const nameB = getFullName(b).toLowerCase();

    return sortOrder.value === "az"
      ? nameA.localeCompare(nameB, "fr")
      : nameB.localeCompare(nameA, "fr");
  });
});
</script>

<template>
  <div class="customers-page">
    <!-- Header -->
    <header class="page-header">
      <div>
        <span class="page-eyebrow">UTILISATEURS</span>

        <h1 class="page-title">Clients</h1>

        <p class="page-subtitle">
          Consultez les clients enregistrés sur votre boutique.
        </p>
      </div>

      <div class="customer-count">
        <strong>{{ customers.length }}</strong>

        <span> client{{ customers.length > 1 ? "s" : "" }} </span>
      </div>
    </header>

    <!-- Toolbar -->
    <section class="toolbar-card">
      <div class="search-wrapper">
        <span class="search-icon">⌕</span>

        <input
          v-model="search"
          type="search"
          placeholder="Rechercher par nom ou email..."
        />

        <button
          v-if="search"
          type="button"
          class="clear-search"
          aria-label="Effacer la recherche"
          @click="search = ''"
        >
          ×
        </button>
      </div>

      <select v-model="sortOrder">
        <option value="az">Nom : A → Z</option>
        <option value="za">Nom : Z → A</option>
      </select>
    </section>

    <!-- Error -->
    <section v-if="error" class="error-state">
      <div class="error-icon">!</div>

      <h2>Impossible de charger les clients</h2>

      <p>{{ error }}</p>

      <button class="btn btn-primary" @click="loadCustomers">Réessayer</button>
    </section>

    <!-- Loading -->
    <section v-else-if="loading" class="customers-card">
      <div v-for="i in 7" :key="i" class="customer-skeleton">
        <div class="skeleton-avatar"></div>

        <div class="skeleton-info">
          <div class="skeleton-line skeleton-name"></div>
          <div class="skeleton-line skeleton-email"></div>
        </div>

        <div class="skeleton-id"></div>
      </div>
    </section>

    <!-- Empty -->
    <section v-else-if="customers.length === 0" class="empty-state">
      <div class="empty-icon">♙</div>

      <h2>Aucun client</h2>

      <p>Aucun client n'est actuellement enregistré sur la boutique.</p>
    </section>

    <!-- No results -->
    <section v-else-if="filteredCustomers.length === 0" class="empty-state">
      <div class="empty-icon">⌕</div>

      <h2>Aucun résultat</h2>

      <p>
        Aucun client ne correspond à
        <strong>« {{ search }} »</strong>.
      </p>

      <button class="btn" @click="search = ''">Effacer la recherche</button>
    </section>

    <!-- Customers -->
    <section v-else class="customers-card">
      <div class="results-header">
        <span>
          {{ filteredCustomers.length }}
          résultat{{ filteredCustomers.length > 1 ? "s" : "" }}
        </span>

        <span v-if="search"> Recherche : « {{ search }} » </span>
      </div>

      <!-- Desktop -->
      <div class="desktop-table">
        <table class="customers-table">
          <thead>
            <tr>
              <th>Client</th>
              <th>Email</th>
              <th>Identifiant</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="customer in filteredCustomers" :key="customer.id">
              <td>
                <div class="customer-cell">
                  <div class="avatar">
                    {{ getInitials(customer) }}
                  </div>

                  <div class="customer-name">
                    <strong>
                      {{ getFullName(customer) }}
                    </strong>

                    <span v-if="!customer.firstName && !customer.lastName">
                      Nom non renseigné
                    </span>
                  </div>
                </div>
              </td>

              <td>
                <a class="email" :href="`mailto:${customer.email}`">
                  {{ customer.email }}
                </a>
              </td>

              <td>
                <code class="customer-id">
                  {{ customer.id.slice(0, 8).toUpperCase() }}
                </code>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile -->
      <div class="mobile-customers">
        <article
          v-for="customer in filteredCustomers"
          :key="customer.id"
          class="mobile-customer"
        >
          <div class="mobile-customer-header">
            <div class="customer-cell">
              <div class="avatar">
                {{ getInitials(customer) }}
              </div>

              <div class="customer-name">
                <strong>
                  {{ getFullName(customer) }}
                </strong>

                <span>{{ customer.email }}</span>
              </div>
            </div>
          </div>

          <a class="mobile-email" :href="`mailto:${customer.email}`">
            {{ customer.email }}
          </a>

          <code class="customer-id">
            ID : {{ customer.id.slice(0, 8).toUpperCase() }}
          </code>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.customers-page {
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

.customer-count {
  display: flex;
  align-items: baseline;
  gap: 0.35rem;
  color: var(--ink-soft);
  white-space: nowrap;
}

.customer-count strong {
  color: var(--ink);
  font-size: 1.4rem;
}

/* Toolbar */

.toolbar-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 190px;
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
.toolbar-card select {
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

.toolbar-card select {
  padding: 0 0.7rem;
}

.search-wrapper input:focus,
.toolbar-card select:focus {
  border-color: var(--ink);
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.04);
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--ink-soft);
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

/* Card */

.customers-card {
  overflow: hidden;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 13px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.85rem 1rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  color: var(--ink-soft);
  font-size: 0.72rem;
}

/* Table */

.customers-table {
  width: 100%;
  border-collapse: collapse;
}

.customers-table th {
  padding: 0.75rem 1rem;
  background: #fafafa;
  color: var(--ink-soft);
  font-size: 0.65rem;
  font-weight: 750;
  letter-spacing: 0.04em;
  text-align: left;
  text-transform: uppercase;
}

.customers-table td {
  padding: 0.85rem 1rem;
  border-top: 1px solid rgba(0, 0, 0, 0.055);
  vertical-align: middle;
}

.customers-table tbody tr {
  transition: background 0.15s ease;
}

.customers-table tbody tr:hover {
  background: #fcfcfc;
}

/* Customer */

.customer-cell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 11px;
  background: #eeeeee;
  color: var(--ink);
  font-size: 0.75rem;
  font-weight: 800;
}

.customer-name {
  min-width: 0;
}

.customer-name strong {
  display: block;
  font-size: 0.8rem;
}

.customer-name span {
  display: block;
  margin-top: 0.2rem;
  color: var(--ink-soft);
  font-size: 0.68rem;
}

.email {
  color: var(--ink);
  font-size: 0.77rem;
  text-decoration: none;
}

.email:hover {
  text-decoration: underline;
}

.customer-id {
  padding: 0.3rem 0.45rem;
  border-radius: 6px;
  background: #f4f4f4;
  color: var(--ink-soft);
  font-size: 0.62rem;
}

/* Mobile */

.mobile-customers {
  display: none;
}

.mobile-customer {
  padding: 1rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.mobile-customer:last-child {
  border-bottom: 0;
}

.mobile-email {
  display: block;
  margin: 0.8rem 0;
  color: var(--ink);
  font-size: 0.75rem;
  text-decoration: none;
}

.mobile-customer .customer-id {
  display: inline-block;
}

/* Empty */

.empty-state,
.error-state {
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

.empty-icon,
.error-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  margin-bottom: 0.9rem;
  border-radius: 50%;
  background: #f2f2f2;
  color: var(--ink-soft);
  font-size: 1.2rem;
  font-weight: 800;
}

.error-icon {
  background: #fff0f0;
  color: #9a3030;
}

.empty-state h2,
.error-state h2 {
  margin: 0;
  font-size: 1rem;
}

.empty-state p,
.error-state p {
  max-width: 420px;
  margin: 0.45rem 0 1rem;
  color: var(--ink-soft);
  font-size: 0.78rem;
}

/* Button */

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

.btn-primary {
  border-color: var(--ink);
  background: var(--ink);
  color: white;
}

/* Skeleton */

.customer-skeleton {
  display: grid;
  grid-template-columns: 40px 1fr 120px;
  align-items: center;
  gap: 0.75rem;
  padding: 0.9rem 1rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.skeleton-avatar,
.skeleton-line {
  background: #eeeeee;
  animation: pulse 1.3s ease-in-out infinite;
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 11px;
}

.skeleton-info {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.skeleton-line {
  height: 11px;
  border-radius: 5px;
}

.skeleton-name {
  width: 35%;
}

.skeleton-email {
  width: 50%;
}

.skeleton-id {
  width: 70%;
  height: 20px;
  border-radius: 5px;
  background: #eeeeee;
  animation: pulse 1.3s ease-in-out infinite;
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

/* Responsive */

@media (max-width: 800px) {
  .desktop-table {
    display: none;
  }

  .mobile-customers {
    display: block;
  }
}

@media (max-width: 600px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .toolbar-card {
    grid-template-columns: 1fr;
  }

  .results-header {
    flex-direction: column;
    gap: 0.2rem;
  }
}
</style>
