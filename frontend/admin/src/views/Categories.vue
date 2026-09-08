<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import api from "../services/api";

interface Category {
  id: string;
  name: string;
}

const categories = ref<Category[]>([]);
const newName = ref("");
const search = ref("");
const loading = ref(true);
const creating = ref(false);
const deletingId = ref<string | null>(null);

const toast = ref({
  message: "",
  type: "success" as "success" | "error",
});

let toastTimer: ReturnType<typeof setTimeout> | null = null;

function showToast(message: string, type: "success" | "error" = "success") {
  toast.value = { message, type };

  if (toastTimer) {
    clearTimeout(toastTimer);
  }

  toastTimer = setTimeout(() => {
    toast.value.message = "";
  }, 3000);
}

async function loadCategories() {
  loading.value = true;

  try {
    const response = await api.get<Category[]>("/categories");
    categories.value = response.data;
  } catch (e) {
    showToast("Impossible de charger les catégories.", "error");
  } finally {
    loading.value = false;
  }
}

onMounted(loadCategories);

const filteredCategories = computed(() => {
  const query = search.value.trim().toLowerCase();

  if (!query) {
    return categories.value;
  }

  return categories.value.filter((category) =>
    category.name.toLowerCase().includes(query),
  );
});

const canCreate = computed(() => newName.value.trim().length >= 2);

async function createCategory() {
  const name = newName.value.trim();

  if (name.length < 2) {
    showToast("Le nom doit contenir au moins 2 caractères.", "error");
    return;
  }

  creating.value = true;

  try {
    await api.post("/categories", { name });

    newName.value = "";

    showToast("Catégorie créée avec succès.");
    await loadCategories();
  } catch (e: any) {
    showToast(
      e.response?.data?.message ||
        "Erreur lors de la création de la catégorie.",
      "error",
    );
  } finally {
    creating.value = false;
  }
}

async function deleteCategory(category: Category) {
  const confirmed = window.confirm(
    `Supprimer la catégorie « ${category.name} » ?\n\n` +
      "Si elle est utilisée par des produits, la suppression pourra être refusée.",
  );

  if (!confirmed) return;

  deletingId.value = category.id;

  try {
    await api.delete(`/categories/${category.id}`);

    showToast("Catégorie supprimée avec succès.");
    await loadCategories();
  } catch (e: any) {
    showToast(
      e.response?.data?.message ||
        "Impossible de supprimer cette catégorie. Elle est peut-être utilisée par des produits.",
      "error",
    );
  } finally {
    deletingId.value = null;
  }
}
</script>

<template>
  <div class="categories-page">
    <!-- Header -->
    <header class="page-header">
      <div>
        <span class="page-eyebrow">CATALOGUE</span>
        <h1 class="page-title">Catégories</h1>
        <p class="page-subtitle">Organisez les produits de votre boutique.</p>
      </div>

      <div class="category-count">
        <strong>{{ categories.length }}</strong>
        <span>catégorie{{ categories.length > 1 ? "s" : "" }}</span>
      </div>
    </header>

    <!-- Create + Search -->
    <section class="category-toolbar">
      <form class="create-card" @submit.prevent="createCategory">
        <div class="create-content">
          <div class="create-icon">+</div>

          <div class="create-info">
            <h2>Nouvelle catégorie</h2>
            <p>Ajoutez une catégorie au catalogue.</p>
          </div>
        </div>

        <div class="create-form">
          <input
            v-model="newName"
            type="text"
            placeholder="Ex. Parfums orientaux"
            maxlength="100"
            :disabled="creating"
          />

          <button
            type="submit"
            class="btn btn-primary"
            :disabled="!canCreate || creating"
          >
            <span v-if="creating">Création...</span>
            <span v-else>Ajouter</span>
          </button>
        </div>
      </form>

      <div class="search-card">
        <label for="category-search">Rechercher</label>

        <div class="search-wrapper">
          <span class="search-icon">⌕</span>

          <input
            id="category-search"
            v-model="search"
            type="search"
            placeholder="Rechercher une catégorie..."
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
      </div>
    </section>

    <!-- Loading -->
    <section v-if="loading" class="category-grid">
      <div v-for="i in 6" :key="i" class="category-skeleton">
        <div class="skeleton-icon"></div>

        <div class="skeleton-content">
          <div class="skeleton-line skeleton-title"></div>
          <div class="skeleton-line skeleton-small"></div>
        </div>
      </div>
    </section>

    <!-- Empty -->
    <section v-else-if="categories.length === 0" class="empty-state">
      <div class="empty-icon">⌂</div>

      <h2>Aucune catégorie</h2>

      <p>
        Commencez par créer votre première catégorie pour organiser votre
        catalogue.
      </p>

      <button type="button" class="btn btn-primary" @click="newName = ''">
        Créer une catégorie
      </button>
    </section>

    <!-- No search result -->
    <section v-else-if="filteredCategories.length === 0" class="empty-state">
      <div class="empty-icon">⌕</div>

      <h2>Aucun résultat</h2>

      <p>
        Aucune catégorie ne correspond à
        <strong>« {{ search }} »</strong>.
      </p>

      <button type="button" class="btn" @click="search = ''">
        Effacer la recherche
      </button>
    </section>

    <!-- Categories -->
    <section v-else>
      <div class="results-header">
        <span>
          {{ filteredCategories.length }}
          résultat{{ filteredCategories.length > 1 ? "s" : "" }}
        </span>

        <span v-if="search"> Recherche : « {{ search }} » </span>
      </div>

      <div class="category-grid">
        <article
          v-for="category in filteredCategories"
          :key="category.id"
          class="category-card"
        >
          <div class="category-main">
            <div class="category-icon">
              {{ category.name.charAt(0).toUpperCase() }}
            </div>

            <div class="category-info">
              <h2>{{ category.name }}</h2>
              <span>Catégorie du catalogue</span>
            </div>
          </div>

          <button
            type="button"
            class="delete-button"
            :disabled="deletingId === category.id"
            :aria-label="`Supprimer ${category.name}`"
            @click="deleteCategory(category)"
          >
            <span v-if="deletingId === category.id"> Suppression... </span>

            <span v-else> Supprimer </span>
          </button>
        </article>
      </div>
    </section>

    <!-- Toast -->
    <Transition name="toast">
      <div v-if="toast.message" class="toast" :class="toast.type" role="alert">
        <span class="toast-indicator"></span>
        <span>{{ toast.message }}</span>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.categories-page {
  width: 100%;
}

/* Header */

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 1.75rem;
}

.page-eyebrow {
  display: block;
  margin-bottom: 0.35rem;
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: var(--ink-soft);
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

.category-count {
  display: flex;
  align-items: baseline;
  gap: 0.35rem;
  color: var(--ink-soft);
  white-space: nowrap;
}

.category-count strong {
  color: var(--ink);
  font-size: 1.4rem;
}

/* Toolbar */

.category-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.create-card,
.search-card {
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 14px;
  padding: 1.1rem;
}

.create-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.create-content {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.create-icon,
.category-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 11px;
  font-weight: 750;
}

.create-icon {
  width: 42px;
  height: 42px;
  background: var(--ink);
  color: white;
  font-size: 1.3rem;
}

.create-info h2 {
  margin: 0;
  font-size: 0.9rem;
}

.create-info p {
  margin: 0.2rem 0 0;
  color: var(--ink-soft);
  font-size: 0.75rem;
}

.create-form {
  display: flex;
  gap: 0.5rem;
  min-width: 330px;
}

.create-form input,
.search-wrapper input {
  width: 100%;
  min-width: 0;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 9px;
  background: #fafafa;
  color: var(--ink);
  outline: none;
  transition:
    border-color 0.15s,
    box-shadow 0.15s;
}

.create-form input {
  height: 40px;
  padding: 0 0.75rem;
}

.create-form input:focus,
.search-wrapper input:focus {
  border-color: var(--ink);
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.btn {
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: white;
  color: var(--ink);
  border-radius: 9px;
  padding: 0.6rem 0.85rem;
  font-size: 0.8rem;
  font-weight: 650;
  cursor: pointer;
  transition: 0.15s ease;
  white-space: nowrap;
}

.btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.btn-primary {
  background: var(--ink);
  color: white;
  border-color: var(--ink);
}

/* Search */

.search-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.45rem;
}

.search-card label {
  font-size: 0.72rem;
  font-weight: 700;
}

.search-wrapper {
  position: relative;
}

.search-wrapper input {
  height: 40px;
  padding: 0 2.3rem;
}

.search-icon {
  position: absolute;
  left: 0.8rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--ink-soft);
  font-size: 1.1rem;
  pointer-events: none;
}

.clear-search {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--ink-soft);
  cursor: pointer;
  font-size: 1.1rem;
}

/* Results */

.results-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.75rem;
  color: var(--ink-soft);
  font-size: 0.75rem;
}

/* Grid */

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.category-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  min-height: 82px;
  padding: 0.9rem;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 13px;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

.category-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 7px 24px rgba(0, 0, 0, 0.06);
}

.category-main {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
}

.category-icon {
  width: 45px;
  height: 45px;
  background: #f2f2f2;
  color: var(--ink);
}

.category-info {
  min-width: 0;
}

.category-info h2 {
  overflow: hidden;
  margin: 0;
  font-size: 0.9rem;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-info span {
  display: block;
  margin-top: 0.25rem;
  color: var(--ink-soft);
  font-size: 0.7rem;
}

.delete-button {
  flex-shrink: 0;
  border: 0;
  background: transparent;
  color: #a33;
  padding: 0.45rem 0.55rem;
  border-radius: 7px;
  font-size: 0.72rem;
  font-weight: 650;
  cursor: pointer;
}

.delete-button:hover:not(:disabled) {
  background: #fff1f1;
}

.delete-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Skeleton */

.category-skeleton {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-height: 82px;
  padding: 0.9rem;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 13px;
  animation: pulse 1.4s ease-in-out infinite;
}

.skeleton-icon {
  width: 45px;
  height: 45px;
  border-radius: 11px;
  background: #eee;
  flex-shrink: 0;
}

.skeleton-content {
  width: 100%;
}

.skeleton-line {
  border-radius: 5px;
  background: #eee;
}

.skeleton-title {
  width: 45%;
  height: 13px;
}

.skeleton-small {
  width: 30%;
  height: 9px;
  margin-top: 8px;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.55;
  }

  50% {
    opacity: 1;
  }
}

/* Empty */

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 2rem;
  text-align: center;
  background: white;
  border: 1px dashed rgba(0, 0, 0, 0.14);
  border-radius: 14px;
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  margin-bottom: 0.9rem;
  border-radius: 50%;
  background: #f2f2f2;
  color: var(--ink-soft);
  font-size: 1.3rem;
}

.empty-state h2 {
  margin: 0;
  font-size: 1rem;
}

.empty-state p {
  max-width: 420px;
  margin: 0.45rem 0 1rem;
  color: var(--ink-soft);
  font-size: 0.8rem;
  line-height: 1.5;
}

/* Toast */

.toast {
  position: fixed;
  right: 1.25rem;
  bottom: 1.25rem;
  z-index: 500;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  max-width: 360px;
  padding: 0.8rem 1rem;
  background: var(--ink);
  color: white;
  border-radius: 10px;
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.18);
  font-size: 0.8rem;
}

.toast.error {
  background: #8f2929;
}

.toast-indicator {
  width: 7px;
  height: 7px;
  flex-shrink: 0;
  border-radius: 50%;
  background: white;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* Responsive */

@media (max-width: 1000px) {
  .category-toolbar {
    grid-template-columns: 1fr;
  }

  .create-card {
    align-items: stretch;
    flex-direction: column;
  }

  .create-form {
    min-width: 0;
  }
}

@media (max-width: 700px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .category-grid {
    grid-template-columns: 1fr;
  }

  .create-form {
    flex-direction: column;
  }

  .create-form .btn {
    width: 100%;
  }

  .results-header {
    flex-direction: column;
    gap: 0.2rem;
  }
}

@media (max-width: 480px) {
  .category-card {
    align-items: flex-start;
  }

  .category-main {
    min-width: 0;
  }

  .delete-button {
    font-size: 0.68rem;
  }

  .toast {
    right: 0.75rem;
    bottom: 0.75rem;
    left: 0.75rem;
    max-width: none;
  }
}
</style>
