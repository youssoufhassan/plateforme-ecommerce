import { defineStore } from "pinia";
import { ref } from "vue";

import { fetchProducts, type Product } from "@/services/productService";

export const useProductStore = defineStore("products", () => {
  const products = ref<Product[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function fetchProductsFromApi(): Promise<void> {
    loading.value = true;
    error.value = null;

    try {
      products.value = await fetchProducts();

      console.log("Produits chargés depuis le backend :", products.value);
    } catch (err) {
      console.error("Erreur lors du chargement des produits :", err);

      error.value = "Impossible de charger les produits.";
    } finally {
      loading.value = false;
    }
  }

  return {
    products,
    loading,
    error,
    fetchProducts: fetchProductsFromApi,
  };
});
