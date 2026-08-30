```vue
<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useRouter } from "vue-router";

import { useProductStore } from "../stores/productStore";
import { useAuthStore } from "../stores/authStore";
import { useCartStore } from "../stores/cartStore";

import { fullImageUrl } from "../services/productService";

import ProductCard from "../components/ProductCard.vue";
import CategorieCard from "../components/CategorieCard.vue";
import LoadingSkeleton from "../components/LoadingSkeleton.vue";
import EmptyState from "../components/EmptyState.vue";
import Footer from "../components/Footer.vue";

const router = useRouter();

const productStore = useProductStore();
const authStore = useAuthStore();
const cartStore = useCartStore();

onMounted(() => {
  productStore.loadProducts();
});

/* =========================================================
   PRODUIT HERO
========================================================= */

const heroProduct = computed(() => {
  const afnan = productStore.products.find((product) =>
    product.name?.toLowerCase().includes("afnan"),
  );

  return afnan || productStore.products[0];
});

const heroImage = computed(() => {
  if (!heroProduct.value) return "";

  return fullImageUrl(
    heroProduct.value.imageUrls?.[0] || heroProduct.value.imageUrl,
  );
});

/* =========================================================
   PRODUITS VEDETTES
========================================================= */

const featuredProducts = computed(() => {
  return productStore.products
    .filter((product) => product.id !== heroProduct.value?.id)
    .slice(0, 4);
});

/* =========================================================
   CATEGORIES
========================================================= */

const categories = computed(() => {
  const products = productStore.products;

  return [
    {
      id: 1,
      name: "Parfums homme",
      imageUrl: products[0]
        ? fullImageUrl(products[0].imageUrls?.[0] || products[0].imageUrl)
        : undefined,
    },
    {
      id: 2,
      name: "Parfums femme",
      imageUrl: products[1]
        ? fullImageUrl(products[1].imageUrls?.[0] || products[1].imageUrl)
        : undefined,
    },
    {
      id: 3,
      name: "Parfums unisexes",
      imageUrl: products[2]
        ? fullImageUrl(products[2].imageUrls?.[0] || products[2].imageUrl)
        : undefined,
    },
  ];
});

/* =========================================================
   AUTRES PRODUITS
========================================================= */

const discoveryProducts = computed(() => {
  const usedIds = new Set([
    heroProduct.value?.id,
    ...featuredProducts.value.map((product) => product.id),
  ]);

  return productStore.products
    .filter((product) => !usedIds.has(product.id))
    .slice(0, 4);
});

/* =========================================================
   NAVIGATION
========================================================= */

function goToProducts() {
  router.push("/produits");
}

function goToProduct(id: string) {
  router.push(`/produits/${id}`);
}

function goToCategory(id: number) {
  router.push({
    path: "/produits",
    query: {
      category: String(id),
    },
  });
}

/* =========================================================
   PANIER
========================================================= */

async function handleAdd(id: string) {
  if (!authStore.isLoggedIn()) {
    alert("Connecte-toi d'abord pour ajouter au panier.");
    return;
  }

  try {
    await cartStore.addItem(id, 1);
  } catch (error) {
    console.error("Erreur lors de l'ajout au panier :", error);

    alert("Impossible d'ajouter le produit au panier.");
  }
}
</script>

<template>
  <div class="home">
    <!-- =====================================================
         HERO
    ====================================================== -->

    <section v-if="heroProduct" class="home-hero">
      <img :src="heroImage" :alt="heroProduct.name" class="home-hero-image" />

      <div class="home-hero-gradient"></div>

      <div class="home-hero-content">
        <span class="home-eyebrow"> SHAHIN · PARFUMERIE </span>

        <h1>
          Laissez votre<br />
          parfum parler.
        </h1>

        <p>
          Découvrez {{ heroProduct.name }}, une fragrance sélectionnée parmi nos
          incontournables.
        </p>

        <div class="home-hero-actions">
          <button
            class="home-btn home-btn-light"
            @click="goToProduct(heroProduct.id)"
          >
            Découvrir
          </button>

          <button class="home-btn home-btn-outline" @click="goToProducts">
            Explorer la collection
          </button>
        </div>
      </div>

      <div class="home-hero-product">
        <span>À L'HONNEUR</span>

        <strong>
          {{ heroProduct.name }}
        </strong>

        <b> {{ heroProduct.price.toFixed(2) }} € </b>
      </div>
    </section>

    <section
      v-else-if="productStore.loading"
      class="home-hero home-hero-loading"
    >
      <LoadingSkeleton />
    </section>

    <!-- =====================================================
         INTRO
    ====================================================== -->

    <section class="home-intro">
      <div class="home-container home-intro-grid">
        <div>
          <span class="home-section-eyebrow"> NOTRE UNIVERS </span>

          <h2>
            Le parfum comme<br />
            signature.
          </h2>
        </div>

        <div class="home-intro-text">
          <p>
            Chez SHAHIN, nous croyons qu'un parfum ne se choisit pas simplement
            pour son odeur. Il accompagne une personnalité, une histoire et des
            moments qui restent.
          </p>

          <p>
            Découvrez une sélection de fragrances orientales et contemporaines
            choisies pour leur caractère.
          </p>

          <button class="home-text-link" @click="goToProducts">
            Découvrir SHAHIN →
          </button>
        </div>
      </div>
    </section>

    <!-- =====================================================
         CATEGORIES
    ====================================================== -->

    <section class="home-categories">
      <div class="home-container">
        <div class="home-section-heading">
          <div>
            <span class="home-section-eyebrow"> LA COLLECTION </span>

            <h2>Trouvez votre univers</h2>
          </div>

          <button class="home-text-link" @click="goToProducts">
            Voir tout →
          </button>
        </div>

        <div class="home-category-grid">
          <div
            v-for="category in categories"
            :key="category.id"
            @click="goToCategory(category.id)"
          >
            <CategorieCard
              :name="category.name"
              :image-url="category.imageUrl"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- =====================================================
         PRODUITS VEDETTES
    ====================================================== -->

    <section class="home-products">
      <div class="home-container">
        <div class="home-section-heading">
          <div>
            <span class="home-section-eyebrow"> LA SÉLECTION SHAHIN </span>

            <h2>Nos incontournables</h2>

            <p>Des fragrances qui méritent une place dans votre collection.</p>
          </div>

          <button class="home-text-link" @click="goToProducts">
            Voir la collection →
          </button>
        </div>

        <div v-if="productStore.loading" class="home-product-grid">
          <LoadingSkeleton v-for="i in 4" :key="i" />
        </div>

        <div v-else-if="featuredProducts.length" class="home-product-grid">
          <ProductCard
            v-for="product in featuredProducts"
            :key="product.id"
            :id="product.id"
            :name="product.name"
            :description="product.description"
            :price="product.price"
            :image-url="product.imageUrl"
            :image-urls="product.imageUrls"
            :stock-quantity="product.stockQuantity"
            @add="handleAdd"
          />
        </div>

        <EmptyState
          v-else
          title="Aucun parfum disponible"
          message="Notre collection arrive bientôt."
        />
      </div>
    </section>

    <!-- =====================================================
         PARFUM À L'HONNEUR
    ====================================================== -->

    <section v-if="heroProduct" class="home-feature">
      <div class="home-feature-image">
        <img :src="heroImage" :alt="heroProduct.name" />
      </div>

      <div class="home-feature-content">
        <span class="home-section-eyebrow"> LE PARFUM DU MOMENT </span>

        <h2>
          {{ heroProduct.name }}
        </h2>

        <p>
          {{ heroProduct.description }}
        </p>

        <div class="home-feature-price">
          {{ heroProduct.price.toFixed(2) }} €
        </div>

        <button
          class="home-btn home-btn-dark"
          @click="goToProduct(heroProduct.id)"
        >
          Voir le parfum
        </button>
      </div>
    </section>

    <!-- =====================================================
         AUTRES FRAGRANCES
    ====================================================== -->

    <section v-if="discoveryProducts.length" class="home-discovery">
      <div class="home-container">
        <div class="home-section-heading">
          <div>
            <span class="home-section-eyebrow"> POUR ALLER PLUS LOIN </span>

            <h2>Laissez-vous tenter</h2>
          </div>
        </div>

        <div class="home-product-grid">
          <ProductCard
            v-for="product in discoveryProducts"
            :key="product.id"
            :id="product.id"
            :name="product.name"
            :description="product.description"
            :price="product.price"
            :image-url="product.imageUrl"
            :image-urls="product.imageUrls"
            :stock-quantity="product.stockQuantity"
            @add="handleAdd"
          />
        </div>
      </div>
    </section>

    <!-- =====================================================
         CTA FINAL
    ====================================================== -->

    <section class="home-final">
      <div class="home-final-content">
        <span class="home-section-eyebrow"> SHAHIN </span>

        <h2>
          Quelle sera<br />
          votre signature ?
        </h2>

        <p>
          Explorez notre collection et trouvez la fragrance qui vous ressemble.
        </p>

        <button class="home-btn home-btn-light" @click="goToProducts">
          Explorer les parfums
        </button>
      </div>
    </section>

    <!-- =====================================================
         FOOTER
    ====================================================== -->

    <Footer />
  </div>
</template>
```
