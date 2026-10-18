<script setup lang="ts">
import type { CatalogFilterState } from "@/types/catalog";

defineProps<{
  modelValue: CatalogFilterState;
  brands: string[];
}>();

const emit = defineEmits<{
  "update:minPrice": [value: number | null];
  "update:maxPrice": [value: number | null];
  "update:availableOnly": [value: boolean];
  "toggle-brand": [value: string];
  "update:sort": [value: string];
}>();

const sortOptions = [
  { label: "Pertinence", value: "relevance" },
  { label: "Prix croissant", value: "price_asc" },
  { label: "Prix décroissant", value: "price_desc" },
  { label: "Nouveautés", value: "newest" },
  { label: "Nom : A à Z", value: "name" },
];
</script>

<template>
  <section class="global-filters">
    <div class="global-filters__section">
      <h3 class="global-filters__heading">Prix</h3>

      <div class="global-filters__price">
        <label>
          <span>Minimum (€)</span>
          <input
            type="number"
            min="0"
            :value="modelValue.minPrice ?? ''"
            placeholder="0"
            @change="
              emit(
                'update:minPrice',
                ($event.target as HTMLInputElement).value === ''
                  ? null
                  : Math.max(
                      0,
                      Number(($event.target as HTMLInputElement).value),
                    ),
              )
            "
          />
        </label>

        <label>
          <span>Maximum (€)</span>
          <input
            type="number"
            min="0"
            :value="modelValue.maxPrice ?? ''"
            placeholder="Sans limite"
            @change="
              emit(
                'update:maxPrice',
                ($event.target as HTMLInputElement).value === ''
                  ? null
                  : Math.max(
                      0,
                      Number(($event.target as HTMLInputElement).value),
                    ),
              )
            "
          />
        </label>
      </div>
    </div>

    <div class="global-filters__section">
      <h3 class="global-filters__heading">Disponibilité</h3>

      <label class="global-filters__check">
        <input
          type="checkbox"
          :checked="modelValue.availableOnly"
          @change="
            emit(
              'update:availableOnly',
              ($event.target as HTMLInputElement).checked,
            )
          "
        />
        <span>En stock uniquement</span>
      </label>
    </div>

    <div v-if="brands.length" class="global-filters__section">
      <h3 class="global-filters__heading">Maisons</h3>

      <div class="global-filters__brands">
        <label
          v-for="brand in brands"
          :key="brand"
          class="global-filters__check"
        >
          <input
            type="checkbox"
            :checked="modelValue.brand.includes(brand)"
            @change="emit('toggle-brand', brand)"
          />
          <span>{{ brand }}</span>
        </label>
      </div>
    </div>

    <div class="global-filters__section">
      <label for="catalog-sort" class="global-filters__heading">
        Trier par
      </label>

      <select
        id="catalog-sort"
        class="global-filters__select"
        :value="modelValue.sort"
        @change="
          emit('update:sort', ($event.target as HTMLSelectElement).value)
        "
      >
        <option
          v-for="option in sortOptions"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>
    </div>
  </section>
</template>

<style scoped>
.global-filters {
  display: grid;
  gap: 22px;
}

.global-filters__section {
  display: grid;
  gap: 12px;
}

.global-filters__heading {
  margin: 0;
  color: #202020;
  font-size: 13px;
  font-weight: 600;
}

.global-filters__price {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.global-filters__price label {
  display: grid;
  gap: 7px;
  color: #666;
  font-size: 11px;
}

.global-filters__price input,
.global-filters__select {
  width: 100%;
  min-width: 0;
  height: 40px;
  padding: 0 10px;
  border: 1px solid #e5e2dd;
  border-radius: 3px;
  background: #fff;
  color: #222;
  font: inherit;
  font-size: 12px;
}

.global-filters__check {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 12px;
  color: #333;
}

.global-filters__check input {
  accent-color: #202020;
}

.global-filters__brands {
  display: grid;
  gap: 10px;
  max-height: 220px;
  overflow: auto;
}

.global-filters__select {
  cursor: pointer;
}
</style>
