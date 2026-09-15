<script setup lang="ts">
import { computed, ref } from "vue";
import { fullImageUrl } from "../services/productService";

const props = defineProps<{
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  imageUrls?: string[];
  stockQuantity: number;
}>();

const emit = defineEmits<{
  add: [id: string];
}>();

const adding = ref(false);
const imgError = ref(false);

const displayImage = computed(() => {
  const path =
    props.imageUrls?.find((image) => image?.trim()) || props.imageUrl || "";

  return fullImageUrl(path);
});

const isOutOfStock = computed(() => props.stockQuantity <= 0);

const formattedPrice = computed(() => {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(props.price);
});

async function handleAdd(event: MouseEvent) {
  event.preventDefault();
  event.stopPropagation();

  if (isOutOfStock.value || adding.value) {
    return;
  }

  adding.value = true;

  try {
    emit("add", props.id);

    // Feedback visuel court.
    // Le résultat réel de l'ajout est géré par le parent/store.
    await new Promise((resolve) => setTimeout(resolve, 600));
  } finally {
    adding.value = false;
  }
}
</script>

<template>
  <article class="pcard">
    <!-- Image -->
    <router-link
      :to="`/produits/${id}`"
      class="pcard-image"
      :aria-label="`Voir ${name}`"
    >
      <img
        v-if="!imgError && displayImage"
        :src="displayImage"
        :alt="name"
        loading="lazy"
        @error="imgError = true"
      />

      <div v-else class="pcard-fallback">
        {{ name.slice(0, 2).toUpperCase() }}
      </div>

      <span v-if="isOutOfStock" class="pcard-badge"> Épuisé </span>
    </router-link>

    <!-- Informations -->
    <div class="pcard-body">
      <router-link :to="`/produits/${id}`" class="pcard-title-link">
        <h3>{{ name }}</h3>
      </router-link>

      <p>{{ description }}</p>

      <!-- Prix + action -->
      <div class="pcard-footer">
        <span class="pcard-price">
          {{ formattedPrice }}
        </span>

        <button
          type="button"
          class="pcard-add"
          :class="{ adding }"
          :disabled="isOutOfStock || adding"
          :aria-label="
            isOutOfStock ? `${name} est épuisé` : `Ajouter ${name} au panier`
          "
          @click="handleAdd"
        >
          <span v-if="adding">Ajouté ✓</span>
          <span v-else-if="isOutOfStock">Épuisé</span>
          <span v-else>Ajouter</span>
        </button>
      </div>
    </div>
  </article>
</template>
