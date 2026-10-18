<script setup lang="ts">
import { computed } from "vue";

import type { CategoryFilterDefinition } from "@/types/catalog";

import CatalogFilterField from "./CatalogFilterField.vue";

const props = defineProps<{
  fields: CategoryFilterDefinition[];
  modelValue: Record<string, string | string[] | number | null>;
}>();

const emit = defineEmits<{
  "update:attribute": [
    payload: {
      key: string;
      value: string | string[] | number | null;
    },
  ];
}>();

const hasFields = computed(() => props.fields.length > 0);

function updateField(
  key: string,
  value: string | string[] | number | null,
): void {
  emit("update:attribute", { key, value });
}
</script>

<template>
  <section v-if="hasFields" class="category-filters">
    <h3 class="category-filters__title">Affiner par catégorie</h3>

    <CatalogFilterField
      v-for="field in fields"
      :key="field.key"
      :field="field"
      :model-value="modelValue[field.key] ?? null"
      @update:model-value="updateField(field.key, $event)"
    />
  </section>
</template>

<style scoped>
.category-filters {
  display: grid;
  gap: 18px;
}

.category-filters__title {
  margin: 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #ece9e4;
  color: #202020;
  font-size: 13px;
  font-weight: 600;
}
</style>
