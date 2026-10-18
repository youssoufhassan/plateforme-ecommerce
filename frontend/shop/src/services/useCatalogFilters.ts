import { computed, reactive, ref, watch } from "vue";

import type { CatalogFilterState } from "@/types/catalog";
import { categoryFilters } from "@/types/catalogFilters";

function createInitialState(): CatalogFilterState {
  return {
    minPrice: null,
    maxPrice: null,
    availableOnly: false,
    brand: [],
    sort: "relevance",
    attributes: {},
  };
}

export function useCatalogFilters(initialCategory = "") {
  const category = ref(initialCategory);
  const state = reactive<CatalogFilterState>(createInitialState());

  const specificFilters = computed(() => categoryFilters[category.value] ?? []);

  const activeCount = computed(() => {
    let count = 0;

    if (state.minPrice !== null) count++;
    if (state.maxPrice !== null) count++;
    if (state.availableOnly) count++;
    count += state.brand.length;

    for (const value of Object.values(state.attributes)) {
      if (
        value !== null &&
        value !== "" &&
        (!Array.isArray(value) || value.length > 0)
      ) {
        count++;
      }
    }

    return count;
  });

  function setCategory(value: string): void {
    if (category.value === value) return;

    category.value = value;

    // Les critères spécifiques ne doivent pas fuiter
    // d'une catégorie à l'autre.
    state.attributes = {};
  }

  function setAttribute(
    key: string,
    value: string | string[] | number | null,
  ): void {
    if (
      value === null ||
      value === "" ||
      (Array.isArray(value) && value.length === 0)
    ) {
      delete state.attributes[key];
      return;
    }

    state.attributes[key] = value;
  }

  function toggleBrand(brand: string): void {
    const index = state.brand.indexOf(brand);

    if (index >= 0) {
      state.brand.splice(index, 1);
    } else {
      state.brand.push(brand);
    }
  }

  function clearAll(): void {
    Object.assign(state, createInitialState());
  }

  function buildParams() {
    return {
      category: category.value || undefined,
      minPrice: state.minPrice ?? undefined,
      maxPrice: state.maxPrice ?? undefined,
      availableOnly: state.availableOnly || undefined,
      brand: state.brand.length ? [...state.brand] : undefined,
      sort: state.sort,
      attributes: { ...state.attributes },
    };
  }

  watch(category, () => {
    // Réinitialisation déjà gérée dans setCategory.
  });

  return {
    category,
    state,
    specificFilters,
    activeCount,
    setCategory,
    setAttribute,
    toggleBrand,
    clearAll,
    buildParams,
  };
}
