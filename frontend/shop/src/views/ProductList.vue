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

const genderOptions = ["Tous", "Homme", "Femme", "Unisexe"];
const activeGender = ref("Tous");
const priceRanges = [
  "Tous",
  "Moins de 20 €",
  "20 – 30 €",
  "30 – 50 €",
  "50 € +",
];
const activePriceRange = ref("Tous");
const inStockOnly = ref(false);
const sortBy = ref("pertinence");

function matchesPrice(price: number, range: string) {
  if (range === "Tous") return true;
  if (range === "Moins de 20 €") return price < 20;
  if (range === "20 – 30 €") return price >= 20 && price <= 30;
  if (range === "30 – 50 €") return price > 30 && price <= 50;
  if (range === "50 € +") return price > 50;
  return true;
}

onMounted(() => {
  productStore.loadProducts();
  categoryStore.loadCategories();
});

const filteredProducts = computed(() => {
  let list = productStore.products;

  if (searchStore.query.trim()) {
    const q = searchStore.query.toLowerCase().trim();
    list = list.filter((p) => p.name.toLowerCase().includes(q));
  }

  list = list.filter((p) => matchesPrice(p.price, activePriceRange.value));

  if (inStockOnly.value) {
    list = list.filter((p) => p.stockQuantity > 0);
  }

  if (sortBy.value === "price-asc") {
    list = [...list].sort((a, b) => a.price - b.price);
  } else if (sortBy.value === "price-desc") {
    list = [...list].sort((a, b) => b.price - a.price);
  }

  return list;
});

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

      <div class="filter-bar">
        <span class="filter-count">
          {{ filteredProducts.length }}
          produit{{ filteredProducts.length > 1 ? "s" : "" }}
        </span>

        <button class="filter-toggle" @click="toggleFilters">
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

      <div v-if="productStore.error" class="catalog-error">
        <EmptyState
          title="Impossible de charger les produits"
          :message="productStore.error"
        />
      </div>

      <div v-else-if="productStore.loading" class="products-grid">
        <LoadingSkeleton v-for="i in 8" :key="i" />
      </div>

      <div v-else class="shop-body" :class="{ 'no-sidebar': !showFilters }">
        <aside v-if="showFilters" class="filter-sidebar">
          <h4>Filtrer par</h4>

          <div class="filter-group">
            <p class="filter-group-title">Catégorie</p>
            <label v-for="opt in genderOptions" :key="opt" class="filter-radio">
              <input
                type="radio"
                name="gender"
                :value="opt"
                v-model="activeGender"
              />
              {{ opt }}
            </label>
          </div>

          <div class="filter-group">
            <p class="filter-group-title">Prix</p>
            <label
              v-for="range in priceRanges"
              :key="range"
              class="filter-radio"
            >
              <input
                type="radio"
                name="price"
                :value="range"
                v-model="activePriceRange"
              />
              {{ range }}
            </label>
          </div>

          <div class="filter-group">
            <p class="filter-group-title">Disponibilité</p>
            <label class="filter-checkbox">
              <input type="checkbox" v-model="inStockOnly" />
              En stock uniquement
            </label>
          </div>

          <div class="filter-group">
            <p class="filter-group-title">Trier par</p>
            <select v-model="sortBy" class="filter-select">
              <option value="pertinence">Pertinence</option>
              <option value="price-asc">Prix croissant</option>
              <option value="price-desc">Prix décroissant</option>
            </select>
          </div>
        </aside>

        <div class="catalog-products">
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

          <div v-else class="catalog-empty">
            <EmptyState
              title="Aucun produit trouvé"
              message="Essaie une autre catégorie ou modifie ta recherche."
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
