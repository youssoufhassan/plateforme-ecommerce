import { defineStore } from "pinia";
import { ref } from "vue";

import { fetchCategories, type Category } from "../services/categoryService";

export const useCategoryStore = defineStore("categories", () => {
  /**
   * Liste des catégories récupérées depuis le backend.
   */
  const categories = ref<Category[]>([]);

  /**
   * État de chargement.
   */
  const loading = ref(false);

  /**
   * Message d'erreur éventuel.
   */
  const error = ref<string | null>(null);

  /**
   * Charge les catégories depuis l'API.
   */
  async function loadCategories(force = false) {
    // Évite de refaire inutilement la même requête
    // si les catégories sont déjà disponibles.
    if (categories.value.length > 0 && !force) {
      return;
    }

    loading.value = true;
    error.value = null;

    try {
      const result = await fetchCategories();

      categories.value = Array.isArray(result) ? result : [];
    } catch (err) {
      console.error("Erreur lors du chargement des catégories :", err);

      error.value =
        err instanceof Error
          ? err.message
          : "Impossible de charger les catégories.";

      categories.value = [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * Force le rechargement des catégories.
   */
  async function refreshCategories() {
    await loadCategories(true);
  }

  /**
   * Réinitialise le store.
   */
  function clearCategories() {
    categories.value = [];
    error.value = null;
  }

  return {
    categories,
    loading,
    error,
    loadCategories,
    refreshCategories,
    clearCategories,
  };
});
