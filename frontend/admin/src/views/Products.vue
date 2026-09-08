<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import api from "../services/api";
import PageHeader from "../components/PageHeader.vue";
import { fullImageUrl } from "../services/productService";

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryName: string;
  categoryId?: string;
  imageUrl?: string;
  imageUrls?: string[];
}

interface Category {
  id: string;
  name: string;
}

type StockFilter = "all" | "available" | "low" | "out";

const products = ref<Product[]>([]);
const allCategories = ref<Category[]>([]);

const loading = ref(true);
const submitting = ref(false);

const search = ref("");
const categoryFilter = ref("all");
const stockFilter = ref<StockFilter>("all");
const sortBy = ref("name-asc");

const toast = ref({
  message: "",
  type: "success" as "success" | "error",
});

const showForm = ref(false);
const editingId = ref<string | null>(null);

const form = ref({
  name: "",
  description: "",
  price: 0,
  stockQuantity: 0,
  categoryId: "",
});

const page = ref(1);
const perPage = 10;

let toastTimer: ReturnType<typeof setTimeout> | undefined;

/* ================================
   LOAD DATA
================================ */

async function loadProducts() {
  loading.value = true;

  try {
    const response = await api.get("/products/admin");

    products.value = Array.isArray(response.data) ? response.data : [];
  } catch (e) {
    showToast("Impossible de charger les produits.", "error");
  } finally {
    loading.value = false;
  }
}

async function loadCategories() {
  try {
    const response = await api.get("/categories");

    allCategories.value = Array.isArray(response.data) ? response.data : [];
  } catch (e) {
    showToast("Impossible de charger les catégories.", "error");
  }
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadCategories()]);
});

/* ================================
   TOAST
================================ */

function showToast(message: string, type: "success" | "error" = "success") {
  toast.value = {
    message,
    type,
  };

  if (toastTimer) {
    clearTimeout(toastTimer);
  }

  toastTimer = setTimeout(() => {
    toast.value.message = "";
  }, 3500);
}

/* ================================
   FORM
================================ */

function resetForm() {
  editingId.value = null;

  form.value = {
    name: "",
    description: "",
    price: 0,
    stockQuantity: 0,
    categoryId: "",
  };
}

function openCreate() {
  resetForm();
  showForm.value = true;
}

function openEdit(product: Product) {
  editingId.value = product.id;

  const category = allCategories.value.find(
    (item) => item.name === product.categoryName,
  );

  form.value = {
    name: product.name,
    description: product.description || "",
    price: Number(product.price),
    stockQuantity: Number(product.stockQuantity),
    categoryId: category?.id || "",
  };

  showForm.value = true;
}

function closeForm() {
  if (submitting.value) return;

  showForm.value = false;
  resetForm();
}

function validateForm() {
  if (!form.value.name.trim()) {
    showToast("Le nom du produit est obligatoire.", "error");

    return false;
  }

  if (form.value.price <= 0) {
    showToast("Le prix doit être supérieur à 0.", "error");

    return false;
  }

  if (form.value.stockQuantity < 0) {
    showToast("Le stock ne peut pas être négatif.", "error");

    return false;
  }

  if (!form.value.categoryId) {
    showToast("Veuillez sélectionner une catégorie.", "error");

    return false;
  }

  return true;
}

async function submitForm() {
  if (submitting.value || !validateForm()) {
    return;
  }

  submitting.value = true;

  const payload = {
    name: form.value.name.trim(),
    description: form.value.description.trim(),
    price: Number(form.value.price),
    stockQuantity: Number(form.value.stockQuantity),
    categoryId: form.value.categoryId || null,
  };

  try {
    if (editingId.value) {
      await api.put(`/products/admin/${editingId.value}`, payload);

      showToast("Produit modifié avec succès.");
    } else {
      await api.post("/products/admin", payload);

      showToast("Produit créé avec succès.");
    }

    showForm.value = false;
    resetForm();

    await loadProducts();
  } catch (e: any) {
    showToast(
      e.response?.data?.message ||
        "Erreur lors de l'enregistrement du produit.",
      "error",
    );
  } finally {
    submitting.value = false;
  }
}

/* ================================
   DELETE
================================ */

async function deleteProduct(product: Product) {
  const confirmed = window.confirm(
    `Supprimer définitivement « ${product.name} » ?`,
  );

  if (!confirmed) return;

  try {
    await api.delete(`/products/admin/${product.id}`);

    showToast("Produit supprimé avec succès.");

    if (paginated.value.length === 1 && page.value > 1) {
      page.value--;
    }

    await loadProducts();
  } catch (e: any) {
    showToast(
      e.response?.data?.message || "Erreur lors de la suppression du produit.",
      "error",
    );
  }
}

/* ================================
   STOCK
================================ */

function getStockStatus(stock: number) {
  if (stock <= 0) {
    return {
      label: "Rupture",
      className: "stock-out",
    };
  }

  if (stock <= 5) {
    return {
      label: "Stock faible",
      className: "stock-low",
    };
  }

  return {
    label: "Disponible",
    className: "stock-ok",
  };
}

/* ================================
   FORMAT
================================ */

function formatPrice(price: number) {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(Number(price) || 0);
}

/* ================================
   CATEGORIES
================================ */

const categories = computed(() => {
  const values = products.value
    .map((product) => product.categoryName)
    .filter(Boolean);

  return [...new Set(values)].sort((a, b) => a.localeCompare(b, "fr"));
});

/* ================================
   FILTERING / SORTING
================================ */

const filtered = computed(() => {
  const query = search.value.trim().toLowerCase();

  const result = products.value.filter((product) => {
    const matchesSearch =
      !query ||
      product.name.toLowerCase().includes(query) ||
      product.description?.toLowerCase().includes(query) ||
      product.categoryName?.toLowerCase().includes(query);

    const matchesCategory =
      categoryFilter.value === "all" ||
      product.categoryName === categoryFilter.value;

    const matchesStock =
      stockFilter.value === "all" ||
      (stockFilter.value === "available" && product.stockQuantity > 5) ||
      (stockFilter.value === "low" &&
        product.stockQuantity > 0 &&
        product.stockQuantity <= 5) ||
      (stockFilter.value === "out" && product.stockQuantity <= 0);

    return matchesSearch && matchesCategory && matchesStock;
  });

  return result.sort((a, b) => {
    switch (sortBy.value) {
      case "name-desc":
        return b.name.localeCompare(a.name, "fr");

      case "price-asc":
        return a.price - b.price;

      case "price-desc":
        return b.price - a.price;

      case "stock-asc":
        return a.stockQuantity - b.stockQuantity;

      case "stock-desc":
        return b.stockQuantity - a.stockQuantity;

      default:
        return a.name.localeCompare(b.name, "fr");
    }
  });
});

/* ================================
   PAGINATION
================================ */

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filtered.value.length / perPage)),
);

const paginated = computed(() => {
  const start = (page.value - 1) * perPage;

  return filtered.value.slice(start, start + perPage);
});

const pageStart = computed(() => {
  if (!filtered.value.length) {
    return 0;
  }

  return (page.value - 1) * perPage + 1;
});

const pageEnd = computed(() =>
  Math.min(page.value * perPage, filtered.value.length),
);

/* ================================
   STATISTICS
================================ */

const totalStock = computed(() =>
  products.value.reduce(
    (total, product) => total + Number(product.stockQuantity || 0),
    0,
  ),
);

const lowStockCount = computed(
  () =>
    products.value.filter(
      (product) => product.stockQuantity > 0 && product.stockQuantity <= 5,
    ).length,
);

const outOfStockCount = computed(
  () => products.value.filter((product) => product.stockQuantity <= 0).length,
);

const availableCount = computed(
  () => products.value.filter((product) => product.stockQuantity > 5).length,
);

/* ================================
   PAGINATION / FILTERS
================================ */

function changePage(newPage: number) {
  if (newPage < 1 || newPage > totalPages.value) {
    return;
  }

  page.value = newPage;
}

function resetFilters() {
  search.value = "";
  categoryFilter.value = "all";
  stockFilter.value = "all";
  sortBy.value = "name-asc";
  page.value = 1;
}

function handleSearch() {
  page.value = 1;
}

function handleFilterChange() {
  page.value = 1;
}
</script>

<template>
  <div class="products-page">
    <!-- HEADER -->

    <PageHeader
      eyebrow="Catalogue"
      title="Produits"
      subtitle="Gérez votre catalogue, vos prix et les niveaux de stock."
      :show-action="true"
      action-label="Ajouter un produit"
      @action="openCreate"
    />

    <!-- STATISTICS -->

    <section class="catalogue-stats">
      <article class="catalogue-stat">
        <div class="stat-icon">◇</div>

        <div>
          <span>Total produits</span>
          <strong>
            {{ products.length }}
          </strong>
        </div>
      </article>

      <article class="catalogue-stat">
        <div class="stat-icon">▣</div>

        <div>
          <span>Stock total</span>
          <strong>
            {{ totalStock }}
          </strong>
        </div>
      </article>

      <article class="catalogue-stat">
        <div class="stat-icon warning">!</div>

        <div>
          <span>Stock faible</span>
          <strong>
            {{ lowStockCount }}
          </strong>
        </div>
      </article>

      <article class="catalogue-stat">
        <div class="stat-icon danger">×</div>

        <div>
          <span>En rupture</span>
          <strong>
            {{ outOfStockCount }}
          </strong>
        </div>
      </article>
    </section>

    <!-- FILTERS -->

    <section class="products-toolbar">
      <div class="search-wrapper">
        <span class="search-icon">⌕</span>

        <input
          v-model="search"
          type="search"
          placeholder="Rechercher un produit, une catégorie..."
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <div class="filters">
        <select
          v-model="categoryFilter"
          class="filter-select"
          @change="handleFilterChange"
        >
          <option value="all">Toutes les catégories</option>

          <option
            v-for="category in categories"
            :key="category"
            :value="category"
          >
            {{ category }}
          </option>
        </select>

        <select
          v-model="stockFilter"
          class="filter-select"
          @change="handleFilterChange"
        >
          <option value="all">Tous les stocks</option>

          <option value="available">Disponible</option>

          <option value="low">Stock faible</option>

          <option value="out">Rupture</option>
        </select>

        <select
          v-model="sortBy"
          class="filter-select"
          @change="handleFilterChange"
        >
          <option value="name-asc">Nom : A → Z</option>

          <option value="name-desc">Nom : Z → A</option>

          <option value="price-asc">Prix croissant</option>

          <option value="price-desc">Prix décroissant</option>

          <option value="stock-asc">Stock croissant</option>

          <option value="stock-desc">Stock décroissant</option>
        </select>
      </div>
    </section>

    <!-- RESULTS -->

    <div class="results-bar">
      <div>
        <strong>
          {{ filtered.length }}
        </strong>

        produit{{ filtered.length > 1 ? "s" : "" }}

        <span
          v-if="search || categoryFilter !== 'all' || stockFilter !== 'all'"
        >
          correspondant aux filtres
        </span>
      </div>

      <button
        v-if="search || categoryFilter !== 'all' || stockFilter !== 'all'"
        type="button"
        class="reset-button"
        @click="resetFilters"
      >
        Réinitialiser les filtres
      </button>
    </div>

    <!-- LOADING -->

    <section v-if="loading" class="products-table-card">
      <div class="loading-table">
        <div v-for="i in 7" :key="i" class="loading-row">
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </section>

    <!-- EMPTY -->

    <section v-else-if="filtered.length === 0" class="empty-products">
      <div class="empty-icon">◇</div>

      <h2>
        {{ products.length === 0 ? "Aucun produit" : "Aucun résultat" }}
      </h2>

      <p>
        {{
          products.length === 0
            ? "Votre catalogue est actuellement vide."
            : "Aucun produit ne correspond aux critères sélectionnés."
        }}
      </p>

      <button
        v-if="products.length === 0"
        type="button"
        class="primary-button"
        @click="openCreate"
      >
        + Ajouter le premier produit
      </button>

      <button
        v-else
        type="button"
        class="secondary-button"
        @click="resetFilters"
      >
        Réinitialiser les filtres
      </button>
    </section>

    <!-- PRODUCTS -->

    <section v-else class="products-table-card">
      <div class="table-wrapper">
        <table class="products-table">
          <thead>
            <tr>
              <th>Produit</th>
              <th>Catégorie</th>
              <th>Prix</th>
              <th>Stock</th>
              <th>Statut</th>
              <th class="actions-column">Actions</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="product in paginated" :key="product.id">
              <td>
                <img
                  v-if="product.imageUrl"
                  :src="fullImageUrl(product.imageUrl)"
                  :alt="product.name"
                  class="product-thumb"
                />

                <div v-else class="product-thumb-placeholder">
                  {{ product.name.charAt(0).toUpperCase() }}
                </div>
              </td>

              <td>
                <div class="product-cell">
                  <div class="product-placeholder">
                    {{ product.name.charAt(0).toUpperCase() }}
                  </div>

                  <div class="product-info">
                    <strong>
                      {{ product.name }}
                    </strong>

                    <span>
                      {{ product.description || "Aucune description" }}
                    </span>
                  </div>
                </div>
              </td>

              <td>
                <span class="category-badge">
                  {{ product.categoryName || "Sans catégorie" }}
                </span>
              </td>

              <td>
                <strong class="price">
                  {{ formatPrice(product.price) }}
                </strong>
              </td>

              <td>
                <strong
                  class="stock-number"
                  :class="{
                    'stock-number-low':
                      product.stockQuantity > 0 && product.stockQuantity <= 5,

                    'stock-number-out': product.stockQuantity <= 0,
                  }"
                >
                  {{ product.stockQuantity }}
                </strong>
              </td>

              <td>
                <span
                  class="stock-badge"
                  :class="getStockStatus(product.stockQuantity).className"
                >
                  <span class="badge-dot"></span>

                  {{ getStockStatus(product.stockQuantity).label }}
                </span>
              </td>

              <td>
                <div class="row-actions">
                  <button
                    type="button"
                    class="icon-button"
                    title="Modifier"
                    aria-label="Modifier le produit"
                    @click="openEdit(product)"
                  >
                    ✎
                  </button>

                  <button
                    type="button"
                    class="icon-button danger"
                    title="Supprimer"
                    aria-label="Supprimer le produit"
                    @click="deleteProduct(product)"
                  >
                    ×
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- MOBILE -->

      <div class="mobile-products">
        <article
          v-for="product in paginated"
          :key="product.id"
          class="mobile-product-card"
        >
          <div class="mobile-product-top">
            <div class="product-cell">
              <div class="product-placeholder">
                {{ product.name.charAt(0).toUpperCase() }}
              </div>

              <div class="product-info">
                <strong>
                  {{ product.name }}
                </strong>

                <span>
                  {{ product.categoryName || "Sans catégorie" }}
                </span>
              </div>
            </div>

            <div class="row-actions">
              <button
                type="button"
                class="icon-button"
                @click="openEdit(product)"
              >
                ✎
              </button>

              <button
                type="button"
                class="icon-button danger"
                @click="deleteProduct(product)"
              >
                ×
              </button>
            </div>
          </div>

          <div class="mobile-product-details">
            <div>
              <span>Prix</span>

              <strong>
                {{ formatPrice(product.price) }}
              </strong>
            </div>

            <div>
              <span>Stock</span>

              <strong>
                {{ product.stockQuantity }}
              </strong>
            </div>

            <span
              class="stock-badge"
              :class="getStockStatus(product.stockQuantity).className"
            >
              <span class="badge-dot"></span>

              {{ getStockStatus(product.stockQuantity).label }}
            </span>
          </div>
        </article>
      </div>

      <!-- PAGINATION -->

      <footer class="pagination-footer">
        <span class="pagination-info">
          {{ pageStart }}–{{ pageEnd }} sur {{ filtered.length }}
        </span>

        <div class="pagination">
          <button
            type="button"
            class="pagination-button"
            :disabled="page === 1"
            @click="changePage(page - 1)"
          >
            ←
          </button>

          <button
            v-for="p in totalPages"
            :key="p"
            type="button"
            class="pagination-button"
            :class="{ active: p === page }"
            @click="changePage(p)"
          >
            {{ p }}
          </button>

          <button
            type="button"
            class="pagination-button"
            :disabled="page === totalPages"
            @click="changePage(page + 1)"
          >
            →
          </button>
        </div>
      </footer>
    </section>

    <!-- PRODUCT MODAL -->

    <Teleport to="body">
      <div v-if="showForm" class="modal-backdrop" @click.self="closeForm">
        <div class="product-modal">
          <header class="modal-header">
            <div>
              <span class="modal-eyebrow">
                {{ editingId ? "CATALOGUE" : "NOUVEAU" }}
              </span>

              <h2>
                {{ editingId ? "Modifier le produit" : "Ajouter un produit" }}
              </h2>

              <p>
                {{
                  editingId
                    ? "Modifiez les informations du produit."
                    : "Ajoutez une nouvelle référence à votre catalogue."
                }}
              </p>
            </div>

            <button
              type="button"
              class="modal-close"
              aria-label="Fermer"
              @click="closeForm"
            >
              ×
            </button>
          </header>

          <form class="product-form" @submit.prevent="submitForm">
            <!-- NAME -->

            <div class="form-field">
              <label for="product-name">
                Nom du produit
                <span>*</span>
              </label>

              <input
                id="product-name"
                v-model="form.name"
                type="text"
                placeholder="Ex. Khamrah"
                maxlength="150"
                required
              />
            </div>

            <!-- DESCRIPTION -->

            <div class="form-field">
              <label for="product-description"> Description </label>

              <textarea
                id="product-description"
                v-model="form.description"
                rows="4"
                placeholder="Décrivez brièvement le produit..."
              ></textarea>
            </div>

            <!-- CATEGORY -->

            <div class="form-field">
              <label for="product-category">
                Catégorie
                <span>*</span>
              </label>

              <select
                id="product-category"
                v-model="form.categoryId"
                class="form-select"
                required
              >
                <option value="" disabled>Sélectionner une catégorie</option>

                <option
                  v-for="category in allCategories"
                  :key="category.id"
                  :value="category.id"
                >
                  {{ category.name }}
                </option>
              </select>
            </div>

            <!-- PRICE / STOCK -->

            <div class="form-grid">
              <div class="form-field">
                <label for="product-price">
                  Prix
                  <span>*</span>
                </label>

                <div class="input-with-suffix">
                  <input
                    id="product-price"
                    v-model.number="form.price"
                    type="number"
                    min="0"
                    step="0.01"
                    required
                  />

                  <span>€</span>
                </div>
              </div>

              <div class="form-field">
                <label for="product-stock">
                  Stock
                  <span>*</span>
                </label>

                <input
                  id="product-stock"
                  v-model.number="form.stockQuantity"
                  type="number"
                  min="0"
                  step="1"
                  required
                />
              </div>
            </div>

            <!-- FOOTER -->

            <footer class="modal-footer">
              <button
                type="button"
                class="secondary-button"
                :disabled="submitting"
                @click="closeForm"
              >
                Annuler
              </button>

              <button
                type="submit"
                class="primary-button"
                :disabled="submitting"
              >
                <span v-if="submitting" class="button-loader"></span>

                {{
                  submitting
                    ? "Enregistrement..."
                    : editingId
                      ? "Enregistrer les modifications"
                      : "Créer le produit"
                }}
              </button>
            </footer>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- TOAST -->

    <Transition name="toast">
      <div v-if="toast.message" class="toast" :class="toast.type" role="alert">
        <span class="toast-icon">
          {{ toast.type === "success" ? "✓" : "!" }}
        </span>

        <span>
          {{ toast.message }}
        </span>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.products-page {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
}

/* ================================
   STATS
================================ */

.catalogue-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 22px;
}

.catalogue-stat {
  min-width: 0;
  padding: 18px;
  border: 1px solid #e8eaed;
  border-radius: 11px;
  background: #ffffff;
  display: flex;
  align-items: center;
  gap: 13px;
}

.stat-icon {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  border-radius: 9px;
  background: #f1f2f4;
  color: #4e535a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  font-weight: 700;
}

.stat-icon.warning {
  background: #f4f0e8;
  color: #796c58;
}

.stat-icon.danger {
  background: #f5ebeb;
  color: #9a4545;
}

.catalogue-stat span,
.catalogue-stat strong {
  display: block;
}

.catalogue-stat span {
  color: #858b94;
  font-size: 0.69rem;
}

.catalogue-stat strong {
  margin-top: 4px;
  color: #17191c;
  font-size: 1.25rem;
  font-weight: 750;
}

/* ================================
   TOOLBAR
================================ */

.products-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 13px;
}

.search-wrapper {
  position: relative;
  flex: 1;
  min-width: 240px;
}

.search-icon {
  position: absolute;
  left: 13px;
  top: 50%;
  transform: translateY(-50%);
  color: #969ba3;
  font-size: 1.1rem;
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 42px;
  box-sizing: border-box;
  padding: 0 14px 0 38px;
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  outline: none;
  background: #ffffff;
  color: #272a2f;
  font: inherit;
  font-size: 0.78rem;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.search-input::placeholder {
  color: #a2a6ad;
}

.search-input:focus {
  border-color: #aeb3ba;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.04);
}

.filters {
  display: flex;
  gap: 8px;
}

.filter-select {
  height: 42px;
  padding: 0 30px 0 11px;
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  outline: none;
  background: #ffffff;
  color: #555b63;
  font: inherit;
  font-size: 0.73rem;
  cursor: pointer;
}

.filter-select:focus {
  border-color: #aeb3ba;
}

/* ================================
   RESULTS
================================ */

.results-bar {
  min-height: 31px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
  color: #8b9098;
  font-size: 0.68rem;
}

.results-bar strong {
  color: #555a62;
}

.reset-button {
  padding: 0;
  border: 0;
  background: none;
  color: #555a62;
  font: inherit;
  font-size: 0.68rem;
  font-weight: 650;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* ================================
   TABLE
================================ */

.products-table-card {
  overflow: hidden;
  border: 1px solid #e7e9ec;
  border-radius: 12px;
  background: #ffffff;
}

.table-wrapper {
  overflow-x: auto;
}

.products-table {
  width: 100%;
  min-width: 850px;
  border-collapse: collapse;
}

.products-table th {
  height: 45px;
  padding: 0 18px;
  border-bottom: 1px solid #e8eaed;
  background: #fafbfc;
  color: #858b93;
  font-size: 0.63rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-align: left;
  text-transform: uppercase;
}

.products-table td {
  height: 70px;
  padding: 10px 18px;
  border-bottom: 1px solid #f0f1f3;
  color: #535860;
  font-size: 0.75rem;
}

.products-table tbody tr {
  transition: background 0.15s ease;
}

.products-table tbody tr:hover {
  background: #fafbfc;
}

.products-table tbody tr:last-child td {
  border-bottom: 0;
}

.actions-column {
  width: 100px;
  text-align: right !important;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 200px;
}

.product-placeholder {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border: 1px solid #e4e6e9;
  border-radius: 8px;
  background: #f4f5f6;
  color: #676c74;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 750;
}

.product-info {
  min-width: 0;
}

.product-info strong,
.product-info span {
  display: block;
}

.product-info strong {
  max-width: 250px;
  overflow: hidden;
  color: #282b30;
  font-size: 0.76rem;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-info span {
  max-width: 260px;
  margin-top: 4px;
  overflow: hidden;
  color: #9a9fa7;
  font-size: 0.64rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-badge {
  display: inline-flex;
  padding: 5px 8px;
  border-radius: 5px;
  background: #f3f4f5;
  color: #666c74;
  font-size: 0.64rem;
}

.price {
  color: #292c31;
  font-size: 0.76rem;
}

.stock-number {
  color: #4b5057;
}

.stock-number-low {
  color: #8a7047;
}

.stock-number-out {
  color: #a04343;
}

.stock-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 8px;
  border-radius: 99px;
  font-size: 0.61rem;
  font-weight: 650;
  white-space: nowrap;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.stock-ok {
  background: #edf7f1;
  color: #28724b;
}

.stock-low {
  background: #f8f3e9;
  color: #806b45;
}

.stock-out {
  background: #f9eeee;
  color: #9a4646;
}

.row-actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
}

.icon-button {
  width: 31px;
  height: 31px;
  border: 1px solid #e4e6e9;
  border-radius: 7px;
  background: #ffffff;
  color: #686e76;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font: inherit;
  font-size: 0.8rem;
  transition:
    background 0.15s ease,
    color 0.15s ease,
    border-color 0.15s ease;
}

.icon-button:hover {
  border-color: #cdd1d6;
  background: #f6f7f8;
  color: #22252a;
}

.icon-button.danger:hover {
  border-color: #e4caca;
  background: #fcf3f3;
  color: #a13f3f;
}

/* ================================
   MOBILE PRODUCTS
================================ */

.mobile-products {
  display: none;
}

/* ================================
   PAGINATION
================================ */

.pagination-footer {
  min-height: 61px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
}

.pagination-info {
  color: #8c9199;
  font-size: 0.68rem;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination-button {
  min-width: 31px;
  height: 31px;
  padding: 0 8px;
  border: 1px solid #e3e5e8;
  border-radius: 7px;
  background: #ffffff;
  color: #70757d;
  cursor: pointer;
  font: inherit;
  font-size: 0.68rem;
}

.pagination-button:hover:not(:disabled) {
  background: #f5f6f7;
}

.pagination-button.active {
  border-color: #17191c;
  background: #17191c;
  color: #ffffff;
}

.pagination-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ================================
   EMPTY
================================ */

.empty-products {
  min-height: 330px;
  padding: 40px 20px;
  border: 1px solid #e7e9ec;
  border-radius: 12px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.empty-icon {
  width: 52px;
  height: 52px;
  border-radius: 13px;
  background: #f2f3f4;
  color: #777d85;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
}

.empty-products h2 {
  margin: 16px 0 5px;
  color: #303339;
  font-size: 1rem;
}

.empty-products p {
  max-width: 400px;
  margin: 0 0 18px;
  color: #92979f;
  font-size: 0.72rem;
  line-height: 1.5;
}

/* ================================
   LOADING
================================ */

.loading-table {
  padding: 8px 0;
}

.loading-row {
  height: 69px;
  padding: 0 18px;
  border-bottom: 1px solid #f0f1f3;
  display: grid;
  grid-template-columns: 2fr 1fr 0.7fr 0.7fr 0.8fr;
  align-items: center;
  gap: 20px;
}

.loading-row span {
  height: 10px;
  border-radius: 5px;
  background: #eceef0;
  animation: loading-pulse 1.4s ease-in-out infinite;
}

.loading-row span:first-child {
  width: 65%;
}

.loading-row span:nth-child(2) {
  width: 55%;
}

.loading-row span:nth-child(3) {
  width: 45%;
}

.loading-row span:nth-child(4) {
  width: 30%;
}

.loading-row span:nth-child(5) {
  width: 50%;
}

@keyframes loading-pulse {
  50% {
    opacity: 0.4;
  }
}

/* ================================
   BUTTONS
================================ */

.primary-button,
.secondary-button {
  min-height: 40px;
  padding: 0 14px;
  border-radius: 7px;
  font: inherit;
  font-size: 0.72rem;
  font-weight: 650;
  cursor: pointer;
}

.primary-button {
  border: 0;
  background: #17191c;
  color: #ffffff;
}

.primary-button:hover:not(:disabled) {
  background: #292c31;
}

.primary-button:disabled,
.secondary-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.secondary-button {
  border: 1px solid #dedfe2;
  background: #ffffff;
  color: #555a62;
}

.secondary-button:hover:not(:disabled) {
  background: #f7f8f9;
}

/* ================================
   MODAL
================================ */

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 500;
  padding: 20px;
  background: rgba(12, 13, 15, 0.55);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-modal {
  width: 100%;
  max-width: 570px;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.2);
}

.modal-header {
  padding: 24px 25px 20px;
  border-bottom: 1px solid #eceef0;
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.modal-eyebrow {
  color: #969ba3;
  font-size: 0.61rem;
  font-weight: 750;
  letter-spacing: 0.12em;
}

.modal-header h2 {
  margin: 7px 0 4px;
  color: #191b1e;
  font-size: 1.15rem;
  letter-spacing: -0.025em;
}

.modal-header p {
  margin: 0;
  color: #8d929a;
  font-size: 0.7rem;
}

.modal-close {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border: 1px solid #e3e5e8;
  border-radius: 7px;
  background: #ffffff;
  color: #777c84;
  cursor: pointer;
  font-size: 1.1rem;
}

.modal-close:hover {
  background: #f5f6f7;
  color: #22252a;
}

.product-form {
  padding: 23px 25px 25px;
}

.form-field {
  margin-bottom: 17px;
}

.form-field label {
  display: block;
  margin-bottom: 7px;
  color: #4e535a;
  font-size: 0.7rem;
  font-weight: 650;
}

.form-field label span {
  color: #a44444;
}

.form-field input,
.form-field textarea,
.form-select {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #dfe2e6;
  border-radius: 7px;
  outline: none;
  background: #ffffff;
  color: #282b30;
  font: inherit;
  font-size: 0.76rem;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.form-field input {
  height: 42px;
  padding: 0 12px;
}

.form-field textarea {
  padding: 11px 12px;
  resize: vertical;
}

.form-select {
  height: 42px;
  padding: 0 12px;
  cursor: pointer;
}

.form-field input:focus,
.form-field textarea:focus,
.form-select:focus {
  border-color: #aeb3ba;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.035);
}

.form-field input::placeholder,
.form-field textarea::placeholder {
  color: #b0b4ba;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 13px;
}

.input-with-suffix {
  position: relative;
}

.input-with-suffix input {
  padding-right: 35px;
}

.input-with-suffix span {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #8b9098;
  font-size: 0.72rem;
}

.modal-footer {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid #eceef0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.button-loader {
  width: 12px;
  height: 12px;
  margin-right: 7px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #ffffff;
  border-radius: 50%;
  display: inline-block;
  vertical-align: -2px;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ================================
   TOAST
================================ */

.toast {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 700;
  max-width: 380px;
  min-height: 46px;
  padding: 0 14px;
  border: 1px solid #e1e3e6;
  border-radius: 9px;
  background: #ffffff;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.12);
  display: flex;
  align-items: center;
  gap: 10px;
  color: #454a52;
  font-size: 0.73rem;
}

.toast-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 750;
}

.toast.success .toast-icon {
  background: #eaf6ef;
  color: #28724b;
}

.toast.error .toast-icon {
  background: #f8eaea;
  color: #9a4646;
}

.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* ================================
   RESPONSIVE
================================ */

@media (max-width: 1200px) {
  .catalogue-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .products-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .filters {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
  }

  .filter-select {
    width: 100%;
  }
}

@media (max-width: 800px) {
  .table-wrapper {
    display: none;
  }

  .mobile-products {
    display: flex;
    flex-direction: column;
  }

  .mobile-product-card {
    padding: 16px;
    border-bottom: 1px solid #eef0f2;
  }

  .mobile-product-card:last-child {
    border-bottom: 0;
  }

  .mobile-product-top {
    display: flex;
    justify-content: space-between;
    gap: 12px;
  }

  .mobile-product-details {
    margin-top: 15px;
    padding-top: 13px;
    border-top: 1px solid #f0f1f3;
    display: flex;
    align-items: center;
    gap: 22px;
  }

  .mobile-product-details > div span,
  .mobile-product-details > div strong {
    display: block;
  }

  .mobile-product-details > div span {
    color: #969ba3;
    font-size: 0.61rem;
  }

  .mobile-product-details > div strong {
    margin-top: 3px;
    color: #41464d;
    font-size: 0.74rem;
  }

  .mobile-product-details .stock-badge {
    margin-left: auto;
  }
}

@media (max-width: 600px) {
  .catalogue-stats {
    grid-template-columns: 1fr 1fr;
    gap: 9px;
  }

  .catalogue-stat {
    padding: 14px;
  }

  .stat-icon {
    width: 33px;
    height: 33px;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .results-bar {
    align-items: flex-start;
    flex-direction: column;
    gap: 5px;
  }

  .pagination-footer {
    padding: 10px 13px;
    align-items: flex-start;
    flex-direction: column;
  }

  .pagination {
    width: 100%;
    justify-content: center;
  }

  .pagination-button:nth-child(n + 7):not(:last-child) {
    display: none;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .product-modal {
    max-height: calc(100vh - 20px);
  }

  .modal-backdrop {
    padding: 10px;
  }

  .modal-header,
  .product-form {
    padding-left: 18px;
    padding-right: 18px;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .modal-footer button {
    width: 100%;
  }

  .toast {
    right: 12px;
    bottom: 12px;
    left: 12px;
    max-width: none;
  }
}

@media (max-width: 420px) {
  .catalogue-stats {
    grid-template-columns: 1fr;
  }

  .mobile-product-details {
    gap: 15px;
  }

  .mobile-product-details .stock-badge {
    margin-left: 0;
  }
}

/* ================================
   PRODUCT IMAGE
================================ */

.product-thumb,
.product-thumb-placeholder {
  width: 52px;
  height: 52px;
  border-radius: 10px;
}

.product-thumb {
  object-fit: cover;
  display: block;
}

.product-thumb-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f1f1;
  font-weight: 700;
}
</style>
