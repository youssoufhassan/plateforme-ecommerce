<script setup lang="ts">
import { onMounted, ref } from "vue";
import api from "../services/api";

interface Customer {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
}

const customers = ref<Customer[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const response = await api.get("/admin/customers");
    customers.value = response.data;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div>
    <h1 class="page-title">Clients</h1>

    <p v-if="loading">Chargement...</p>

    <table v-else class="admin-table">
      <thead>
        <tr>
          <th>Nom</th>
          <th>Email</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="customer in customers" :key="customer.id">
          <td>{{ customer.firstName || "—" }} {{ customer.lastName || "" }}</td>
          <td>{{ customer.email }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
