<script setup lang="ts">
import { ref, computed } from "vue";
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

const emit = defineEmits<{ add: [id: string] }>();
const adding = ref(false);
const imgError = ref(false);

const displayImage = computed(() => {
  const path = props.imageUrls?.[0] || props.imageUrl;
  return fullImageUrl(path);
});

async function handleAdd() {
  adding.value = true;
  emit("add", props.id);
  setTimeout(() => (adding.value = false), 600);
}
</script>

<template>
  <article class="pcard">
    <router-link :to="`/produits/${id}`" class="pcard-image">
      <img
        v-if="!imgError"
        :src="displayImage"
        :alt="name"
        loading="lazy"
        @error="imgError = true"
      />
      <div v-else class="pcard-fallback">
        {{ name.slice(0, 2).toUpperCase() }}
      </div>
      <span v-if="stockQuantity <= 0" class="pcard-badge">Épuisé</span>
    </router-link>

    <div class="pcard-body">
      <router-link :to="`/produits/${id}`" class="pcard-title-link">
        <h3>{{ name }}</h3>
      </router-link>
      <p>{{ description }}</p>
      <div class="pcard-footer">
        <span class="pcard-price">{{ price.toFixed(2) }} €</span>
        <button
          class="pcard-add"
          :class="{ adding }"
          :disabled="stockQuantity <= 0"
          @click="handleAdd"
        >
          {{ adding ? "Ajouté ✓" : "Ajouter" }}
        </button>
      </div>
    </div>
  </article>
</template>
