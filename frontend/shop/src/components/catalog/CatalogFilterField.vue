<script setup lang="ts">
import type { CategoryFilterDefinition } from "@/types/catalog";

defineProps<{
  field: CategoryFilterDefinition;
  modelValue: string | string[] | number | null;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string | string[] | number | null];
}>();
</script>

<template>
  <div class="filter-field">
    <label class="filter-field__label" :for="`filter-${field.key}`">
      {{ field.label }}
    </label>

    <select
      v-if="field.type === 'select'"
      :id="`filter-${field.key}`"
      class="filter-field__select"
      :value="modelValue == null ? '' : String(modelValue)"
      @change="
        emit(
          'update:modelValue',
          ($event.target as HTMLSelectElement).value || null,
        )
      "
    >
      <option value="">Tous</option>

      <option
        v-for="option in field.options ?? []"
        :key="option.value"
        :value="option.value"
      >
        {{ option.label }}
      </option>
    </select>

    <div v-else-if="field.type === 'checkbox'" class="filter-field__checkboxes">
      <label
        v-for="option in field.options ?? []"
        :key="option.value"
        class="filter-field__checkbox"
      >
        <input
          type="checkbox"
          :checked="
            Array.isArray(modelValue)
              ? modelValue.includes(option.value)
              : modelValue === option.value
          "
          @change="
            emit(
              'update:modelValue',
              ($event.target as HTMLInputElement).checked
                ? [
                    ...(Array.isArray(modelValue)
                      ? modelValue
                      : modelValue == null
                        ? []
                        : [String(modelValue)]),
                    option.value,
                  ]
                : Array.isArray(modelValue)
                  ? modelValue.filter((v) => v !== option.value)
                  : null,
            )
          "
        />

        <span>{{ option.label }}</span>
      </label>
    </div>

    <div v-else-if="field.type === 'range'" class="filter-field__range">
      <input
        type="number"
        class="filter-field__input"
        :min="field.min"
        :max="field.max"
        :step="field.step ?? 1"
        :value="modelValue ?? ''"
        :placeholder="field.min != null ? String(field.min) : 'Valeur'"
        @change="
          emit(
            'update:modelValue',
            ($event.target as HTMLInputElement).value === ''
              ? null
              : Number(($event.target as HTMLInputElement).value),
          )
        "
      />

      <span v-if="field.unit" class="filter-field__unit">
        {{ field.unit }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.filter-field {
  display: grid;
  gap: 9px;
}

.filter-field__label {
  color: #252525;
  font-size: 12px;
  font-weight: 500;
}

.filter-field__select,
.filter-field__input {
  width: 100%;
  min-height: 42px;
  padding: 0 12px;
  border: 1px solid #e5e2dd;
  border-radius: 3px;
  background: #fff;
  color: #202020;
  font: inherit;
  font-size: 13px;
}

.filter-field__select:focus,
.filter-field__input:focus {
  outline: 1px solid #222;
  outline-offset: 1px;
}

.filter-field__checkboxes {
  display: grid;
  gap: 10px;
}

.filter-field__checkbox {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 13px;
}

.filter-field__checkbox input {
  accent-color: #202020;
}

.filter-field__range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-field__unit {
  color: #666;
  font-size: 12px;
}
</style>
