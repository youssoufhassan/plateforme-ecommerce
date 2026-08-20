import { defineStore } from "pinia";
import { ref } from "vue";
import { fetchCategories, type Category } from "../services/categoryService";

export const useCategoryStore = defineStore("categories", () => {
  const categories = ref<Category[]>([]);

  async function loadCategories() {
    categories.value = await fetchCategories();
  }

  return { categories, loadCategories };
});
