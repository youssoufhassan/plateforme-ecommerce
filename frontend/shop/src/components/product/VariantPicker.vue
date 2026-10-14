<script setup lang="ts">
import { computed } from "vue";

import type { Product } from "@/types/product";

const props = defineProps<{
  product: Product;
  /** Valeur retenue pour chaque axe, indexée par identifiant d'axe. */
  selection: Record<string, string>;
}>();

const emit = defineEmits<{
  select: [payload: { optionId: string; valueId: string }];
}>();

const options = computed(() => props.product.options ?? []);
const variants = computed(() => props.product.variants ?? []);

/**
 * Une valeur est proposable si au moins une variante disponible la combine
 * avec les choix déjà faits sur les autres axes.
 *
 * Exemple : taille M sélectionnée, la couleur Beige est grisée s'il n'existe
 * aucune variante M + Beige en stock.
 */
function isValueAvailable(optionId: string, valueId: string): boolean {
  const others = Object.entries(props.selection).filter(
    ([key]) => key !== optionId,
  );

  return variants.value.some((variant) => {
    if (!variant.available) return false;
    if (!variant.optionValueIds?.includes(valueId)) return false;

    return others.every(([, otherValueId]) =>
      variant.optionValueIds?.includes(otherValueId),
    );
  });
}

/** Valeur sélectionnée pour un axe donné. */
function isSelected(optionId: string, valueId: string): boolean {
  return props.selection[optionId] === valueId;
}

function select(optionId: string, valueId: string): void {
  emit("select", { optionId, valueId });
}

/** Libellé de la valeur retenue, affiché à côté du nom de l'axe. */
function selectedLabel(optionId: string): string | null {
  const valueId = props.selection[optionId];
  if (!valueId) return null;

  const option = options.value.find((o) => o.id === optionId);
  return option?.values.find((v) => v.id === valueId)?.value ?? null;
}
</script>

<template>
  <div v-if="options.length" class="variant-picker">
    <fieldset v-for="option in options" :key="option.id" class="axis">
      <legend class="axis__legend">
        {{ option.name }}
        <span v-if="selectedLabel(option.id)" class="axis__selected">
          {{ selectedLabel(option.id) }}
        </span>
      </legend>

      <div class="axis__values">
        <button
          v-for="value in option.values"
          :key="value.id"
          type="button"
          class="axis__value"
          :class="{
            'axis__value--active': isSelected(option.id, value.id),
            'axis__value--unavailable': !isValueAvailable(option.id, value.id),
          }"
          :aria-pressed="isSelected(option.id, value.id)"
          @click="select(option.id, value.id)"
        >
          {{ value.value }}
        </button>
      </div>
    </fieldset>
  </div>
</template>

<style scoped>
.variant-picker {
  display: grid;
  gap: 22px;
}

.axis {
  margin: 0;
  padding: 0;
  border: none;
}

.axis__legend {
  padding: 0;
  margin-bottom: 10px;
  color: var(--color-text-muted);
  font-size: 12px;
}

/* La valeur retenue reste lisible sans ouvrir le sélecteur */
.axis__selected {
  color: var(--color-text);
  font-weight: 500;
}

.axis__values {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.axis__value {
  min-width: 52px;
  padding: 11px 16px;

  border: 1px solid var(--color-border-strong);
  background: var(--color-white);
  cursor: pointer;

  color: var(--color-text);
  font-family: inherit;
  font-size: 13px;

  transition:
    border-color var(--transition-fast),
    background var(--transition-fast),
    color var(--transition-fast);
}

.axis__value:hover:not(.axis__value--unavailable) {
  border-color: var(--color-text);
}

.axis__value--active {
  border-color: var(--color-text);
  background: var(--color-text);
  color: var(--color-white);
}

/*
  Désactivée, pas masquée : le client voit que la taille existe,
  mais pas dans la couleur choisie.
*/
.axis__value--unavailable {
  color: var(--color-text-muted);
  border-color: var(--color-border);
  background: repeating-linear-gradient(
    -45deg,
    transparent,
    transparent 5px,
    var(--color-border) 5px,
    var(--color-border) 6px
  );
}

.axis__value--unavailable.axis__value--active {
  border-color: var(--color-text-muted);
  background: var(--color-border);
  color: var(--color-text);
}
</style>
