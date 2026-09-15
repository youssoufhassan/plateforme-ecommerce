import { defineStore } from "pinia";
import { ref } from "vue";

export const useSearchStore = defineStore("search", () => {
  /**
   * Texte actuellement recherché.
   */
  const query = ref("");

  /**
   * Définit la recherche.
   */
  function setQuery(value: string) {
    query.value = value;
  }

  /**
   * Réinitialise la recherche.
   */
  function clearSearch() {
    query.value = "";
  }

  return {
    query,
    setQuery,
    clearSearch,
  };
});
