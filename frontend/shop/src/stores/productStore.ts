import { defineStore } from "pinia";
import { ref } from "vue";
import { fetchProducts, type Product } from "../services/productService";

export const useProductStore = defineStore("products", () => {
  const products = ref<Product[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function loadProducts() {
    loading.value = true;
    error.value = null;
    try {
      products.value = await fetchProducts();
    } catch (e) {
      error.value = "Impossible de charger les produits";
    } finally {
      loading.value = false;
    }
  }

  return { products, loading, error, loadProducts };
});
