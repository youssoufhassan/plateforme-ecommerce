<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import api from "../services/api";

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryName: string;
}

const products = ref<Product[]>([]);
const loading = ref(true);
const search = ref("");
const toast = ref({ message: "", type: "success" as "success" | "error" });

const showForm = ref(false);
const editingId = ref<string | null>(null);
const form = ref({ name: "", description: "", price: 0, stockQuantity: 0 });

const page = ref(1);
const perPage = 10;

async function loadProducts() {
  loading.value = true;
  try {
    const response = await api.get("/products/admin");
    products.value = response.data;
  } catch (e) {
    showToast("Impossible de charger les produits.", "error");
  } finally {
    loading.value = false;
  }
}

onMounted(loadProducts);

function showToast(message: string, type: "success" | "error" = "success") {
  toast.value = { message, type };
  setTimeout(() => (toast.value.message = ""), 3000);
}

const filtered = computed(() =>
  products.value.filter((p) =>
    p.name.toLowerCase().includes(search.value.toLowerCase()),
  ),
);

const paginated = computed(() => {
  const start = (page.value - 1) * perPage;
  return filtered.value.slice(start, start + perPage);
});

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filtered.value.length / perPage)),
);

function openCreate() {
  editingId.value = null;
  form.value = { name: "", description: "", price: 0, stockQuantity: 0 };
  showForm.value = true;
}

function openEdit(product: Product) {
  editingId.value = product.id;
  form.value = {
    name: product.name,
    description: product.description,
    price: product.price,
    stockQuantity: product.stockQuantity,
  };
  showForm.value = true;
}

async function submitForm() {
  try {
    if (editingId.value) {
      await api.put(`/products/admin/${editingId.value}`, form.value);
      showToast("Produit modifié.");
    } else {
      await api.post("/products/admin", form.value);
      showToast("Produit créé.");
    }
    showForm.value = false;
    await loadProducts();
  } catch (e: any) {
    showToast(
      e.response?.data?.message || "Erreur lors de l'enregistrement.",
      "error",
    );
  }
}

async function deleteProduct(id: string) {
  if (!confirm("Supprimer définitivement ce produit ?")) return;
  try {
    await api.delete(`/products/admin/${id}`);
    showToast("Produit supprimé.");
    await loadProducts();
  } catch (e: any) {
    showToast(
      e.response?.data?.message || "Erreur lors de la suppression.",
      "error",
    );
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">Produits</h1>

    <div class="toolbar">
      <input
        v-model="search"
        type="text"
        placeholder="Rechercher un produit..."
        class="search-input"
      />
      <button class="btn btn-primary" @click="openCreate">
        + Nouveau produit
      </button>
    </div>

    <p v-if="loading">Chargement...</p>

    <table v-else class="admin-table">
      <thead>
        <tr>
          <th>Nom</th>
          <th>Catégorie</th>
          <th>Prix</th>
          <th>Stock</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="product in paginated" :key="product.id">
          <td>{{ product.name }}</td>
          <td>{{ product.categoryName || "—" }}</td>
          <td>{{ product.price.toFixed(2) }} €</td>
          <td>{{ product.stockQuantity }}</td>
          <td style="display: flex; gap: 0.4rem">
            <button class="btn" @click="openEdit(product)">Modifier</button>
            <button class="btn btn-danger" @click="deleteProduct(product.id)">
              Supprimer
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="pagination" v-if="totalPages > 1">
      <button
        v-for="p in totalPages"
        :key="p"
        class="btn"
        :class="{ 'btn-primary': p === page }"
        @click="page = p"
      >
        {{ p }}
      </button>
    </div>

    <div
      v-if="showForm"
      style="
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.4);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 200;
      "
    >
      <form
        @submit.prevent="submitForm"
        class="admin-form"
        style="background: white; padding: 2rem; border-radius: 8px"
      >
        <h3 style="margin: 0">
          {{ editingId ? "Modifier" : "Nouveau" }} produit
        </h3>
        <div>
          <label>Nom</label>
          <input v-model="form.name" type="text" required />
        </div>
        <div>
          <label>Description</label>
          <textarea v-model="form.description" rows="3"></textarea>
        </div>
        <div>
          <label>Prix (€)</label>
          <input
            v-model.number="form.price"
            type="number"
            step="0.01"
            required
          />
        </div>
        <div>
          <label>Stock</label>
          <input v-model.number="form.stockQuantity" type="number" required />
        </div>
        <div style="display: flex; gap: 0.5rem">
          <button type="submit" class="btn btn-primary">Enregistrer</button>
          <button type="button" class="btn" @click="showForm = false">
            Annuler
          </button>
        </div>
      </form>
    </div>

    <div v-if="toast.message" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
