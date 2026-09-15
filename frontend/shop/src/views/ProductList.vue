<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

import { useProductStore } from "../stores/productStore";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";
import { useSearchStore } from "../stores/searchStore";
import { useCategoryStore } from "../stores/categoryStore";

import SectionTitle from "../components/SectionTitle.vue";
import ProductCard from "../components/ProductCard.vue";
import LoadingSkeleton from "../components/LoadingSkeleton.vue";
import EmptyState from "../components/EmptyState.vue";

const productStore = useProductStore();
const cartStore = useCartStore();
const authStore = useAuthStore();
const searchStore = useSearchStore();
const categoryStore = useCategoryStore();

const showFilters = ref(true);

const activeCategory = ref("Toutes");
const activePriceRange = ref("Tous");
const inStockOnly = ref(false);
const sortBy = ref("pertinence");

const priceRanges = [
  "Tous",
  "Moins de 20 €",
  "20 – 30 €",
  "30 – 50 €",
  "50 € +",
];

onMounted(async () => {
  await Promise.all([
    productStore.loadProducts(),
    categoryStore.loadCategories(),
  ]);
});

/**
 * Catégories disponibles.
 *
 * Les catégories viennent maintenant directement du backend
 * au lieu d'être écrites en dur dans le frontend.
 */
const categoryOptions = computed(() => {
  const names = categoryStore.categories
    .map((category) => category.name)
    .filter((name) => name && name.trim());

  const uniqueNames = [...new Set(names)];

  return ["Toutes", ...uniqueNames];
});

function matchesPrice(price: number, range: string) {
  if (range === "Tous") return true;

  if (range === "Moins de 20 €") {
    return price < 20;
  }

  if (range === "20 – 30 €") {
    return price >= 20 && price <= 30;
  }

  if (range === "30 – 50 €") {
    return price > 30 && price <= 50;
  }

  if (range === "50 € +") {
    return price > 50;
  }

  return true;
}

/**
 * Recherche + filtres + tri.
 */
const filteredProducts = computed(() => {
  let list = [...productStore.products];

  const query = searchStore.query.trim().toLowerCase();

  /**
   * Recherche globale :
   * - nom
   * - description
   * - catégorie
   */
  if (query) {
    list = list.filter((product) => {
      const name = product.name?.toLowerCase() ?? "";
      const description = product.description?.toLowerCase() ?? "";
      const category = product.categoryName?.toLowerCase() ?? "";

      return (
        name.includes(query) ||
        description.includes(query) ||
        category.includes(query)
      );
    });
  }

  /**
   * Filtre par catégorie.
   */
  if (activeCategory.value !== "Toutes") {
    list = list.filter(
      (product) =>
        product.categoryName?.toLowerCase() ===
        activeCategory.value.toLowerCase(),
    );
  }

  /**
   * Filtre par prix.
   */
  list = list.filter((product) =>
    matchesPrice(product.price, activePriceRange.value),
  );

  /**
   * Filtre disponibilité.
   */
  if (inStockOnly.value) {
    list = list.filter((product) => product.stockQuantity > 0);
  }

  /**
   * Tri.
   */
  if (sortBy.value === "price-asc") {
    list.sort((a, b) => a.price - b.price);
  }

  if (sortBy.value === "price-desc") {
    list.sort((a, b) => b.price - a.price);
  }

  return list;
});

/**
 * Nombre total de produits affichés.
 */
const resultCountLabel = computed(() => {
  const count = filteredProducts.value.length;

  if (count === 0) return "Aucun produit";

  return `${count} produit${count > 1 ? "s" : ""}`;
});

/**
 * Vérifie si des filtres sont actuellement actifs.
 */
const hasActiveFilters = computed(() => {
  return (
    activeCategory.value !== "Toutes" ||
    activePriceRange.value !== "Tous" ||
    inStockOnly.value ||
    Boolean(searchStore.query.trim())
  );
});

/**
 * Réinitialise tous les filtres.
 */
function resetFilters() {
  activeCategory.value = "Toutes";
  activePriceRange.value = "Tous";
  inStockOnly.value = false;
  sortBy.value = "pertinence";
  searchStore.query = "";
}

/**
 * Ajout au panier.
 */
async function handleAddToCart(productId: string) {
  if (!authStore.isLoggedIn()) {
    alert("Connecte-toi d'abord pour ajouter au panier");
    return;
  }

  try {
    await cartStore.addItem(productId, 1);
    alert("Ajouté au panier !");
  } catch (error) {
    console.error("Erreur lors de l'ajout au panier :", error);
    alert("Impossible d'ajouter le produit au panier.");
  }
}

function toggleFilters() {
  showFilters.value = !showFilters.value;
}
</script>

<template>
  <div class="product-list-page">
    <div class="container">
      <section class="catalog-header">
        <SectionTitle
          eyebrow="COLLECTION SHAHIN"
          title="Découvre nos produits"
          subtitle="Parfums et produits venus d'ailleurs, sélectionnés pour toi."
        />
      </section>

      <!-- Barre supérieure -->
      <div class="filter-bar">
        <span class="filter-count">
          {{ resultCountLabel }}
        </span>

        <button class="filter-toggle" type="button" @click="toggleFilters">
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
          >
            <line x1="4" y1="6" x2="20" y2="6" />
            <circle cx="9" cy="6" r="2" fill="white" />
            <line x1="4" y1="12" x2="20" y2="12" />
            <circle cx="15" cy="12" r="2" fill="white" />
            <line x1="4" y1="18" x2="20" y2="18" />
            <circle cx="11" cy="18" r="2" fill="white" />
          </svg>

          {{ showFilters ? "Masquer les filtres" : "Afficher les filtres" }}
        </button>
      </div>

      <!-- Erreur produits -->
      <div v-if="productStore.error" class="catalog-error">
        <EmptyState
          title="Impossible de charger les produits"
          :message="productStore.error"
        />
      </div>

      <!-- Chargement -->
      <div v-else-if="productStore.loading" class="products-grid">
        <LoadingSkeleton v-for="i in 8" :key="i" />
      </div>

      <!-- Catalogue -->
      <div v-else class="shop-body" :class="{ 'no-sidebar': !showFilters }">
        <!-- Sidebar -->
        <aside v-if="showFilters" class="filter-sidebar">
          <div class="filter-sidebar-header">
            <h4>Filtrer par</h4>

            <button
              v-if="hasActiveFilters"
              type="button"
              class="filter-reset"
              @click="resetFilters"
            >
              Réinitialiser
            </button>
          </div>

          <!-- Catégories -->
          <div class="filter-group">
            <p class="filter-group-title">Catégorie</p>

            <label
              v-for="category in categoryOptions"
              :key="category"
              class="filter-radio"
            >
              <input
                v-model="activeCategory"
                type="radio"
                name="category"
                :value="category"
              />

              <span>{{ category }}</span>
            </label>
          </div>

          <!-- Prix -->
          <div class="filter-group">
            <p class="filter-group-title">Prix</p>

            <label
              v-for="range in priceRanges"
              :key="range"
              class="filter-radio"
            >
              <input
                v-model="activePriceRange"
                type="radio"
                name="price"
                :value="range"
              />

              <span>{{ range }}</span>
            </label>
          </div>

          <!-- Disponibilité -->
          <div class="filter-group">
            <p class="filter-group-title">Disponibilité</p>

            <label class="filter-checkbox">
              <input v-model="inStockOnly" type="checkbox" />

              <span>En stock uniquement</span>
            </label>
          </div>

          <!-- Tri -->
          <div class="filter-group">
            <p class="filter-group-title">Trier par</p>

            <select v-model="sortBy" class="filter-select">
              <option value="pertinence">Pertinence</option>
              <option value="price-asc">Prix croissant</option>
              <option value="price-desc">Prix décroissant</option>
            </select>
          </div>
        </aside>

        <!-- Produits -->
        <div class="catalog-products">
          <!-- Résultats -->
          <div v-if="filteredProducts.length" class="products-grid">
            <ProductCard
              v-for="product in filteredProducts"
              :key="product.id"
              :id="product.id"
              :name="product.name"
              :description="product.description"
              :price="product.price"
              :image-url="product.imageUrl"
              :image-urls="product.imageUrls"
              :stock-quantity="product.stockQuantity"
              @add="handleAddToCart"
            />
          </div>

          <!-- Aucun résultat -->
          <div v-else class="catalog-empty">
            <EmptyState
              title="Aucun produit trouvé"
              message="Essaie une autre catégorie, modifie ta recherche ou réinitialise les filtres."
            />

            <button
              v-if="hasActiveFilters"
              type="button"
              class="filter-reset"
              @click="resetFilters"
            >
              Réinitialiser les filtres
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
