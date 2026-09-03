<script setup lang="ts">
import { onMounted, ref } from "vue";
import api from "../services/api";

interface Category {
  id: string;
  name: string;
}

const categories = ref<Category[]>([]);
const newName = ref("");
const loading = ref(true);
const toast = ref({ message: "", type: "success" as "success" | "error" });

function showToast(message: string, type: "success" | "error" = "success") {
  toast.value = { message, type };
  setTimeout(() => (toast.value.message = ""), 3000);
}

async function loadCategories() {
  loading.value = true;
  try {
    const response = await api.get("/categories");
    categories.value = response.data;
  } catch (e) {
    showToast("Impossible de charger les catégories.", "error");
  } finally {
    loading.value = false;
  }
}

onMounted(loadCategories);

async function createCategory() {
  if (!newName.value.trim()) return;
  try {
    await api.post("/categories", { name: newName.value });
    newName.value = "";
    showToast("Catégorie créée.");
    await loadCategories();
  } catch (e: any) {
    showToast(
      e.response?.data?.message || "Erreur lors de la création.",
      "error",
    );
  }
}

async function deleteCategory(id: string) {
  if (!confirm("Supprimer cette catégorie ?")) return;
  try {
    await api.delete(`/categories/${id}`);
    showToast("Catégorie supprimée.");
    await loadCategories();
  } catch (e: any) {
    showToast(
      e.response?.data?.message ||
        "Erreur — la catégorie est peut-être utilisée par des produits.",
      "error",
    );
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">Catégories</h1>

    <div class="toolbar">
      <div style="display: flex; gap: 0.5rem">
        <input
          v-model="newName"
          type="text"
          placeholder="Nom de la catégorie"
          class="search-input"
          @keyup.enter="createCategory"
        />
        <button class="btn btn-primary" @click="createCategory">Ajouter</button>
      </div>
    </div>

    <p v-if="loading">Chargement...</p>

    <table v-else class="admin-table">
      <thead>
        <tr>
          <th>Nom</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="cat in categories" :key="cat.id">
          <td>{{ cat.name }}</td>
          <td>
            <button class="btn btn-danger" @click="deleteCategory(cat.id)">
              Supprimer
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="toast.message" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
