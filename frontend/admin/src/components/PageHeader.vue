<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string;
    subtitle?: string;
    eyebrow?: string;
    actionLabel?: string;
    showAction?: boolean;
  }>(),
  {
    subtitle: "",
    eyebrow: "",
    actionLabel: "",
    showAction: false,
  },
);

const emit = defineEmits<{
  action: [];
}>();
</script>

<template>
  <header class="page-header">
    <div class="page-header-content">
      <div class="page-header-text">
        <span v-if="eyebrow" class="eyebrow">
          {{ eyebrow }}
        </span>

        ```
        <h1>{{ title }}</h1>

        <p v-if="subtitle">
          {{ subtitle }}
        </p>
      </div>

      <button
        v-if="showAction && actionLabel"
        class="header-action"
        type="button"
        @click="emit('action')"
      >
        <span class="action-icon">+</span>
        {{ actionLabel }}
      </button>
    </div>
    ```
  </header>
</template>

<style scoped>
.page-header {
  margin-bottom: 28px;
}

.page-header-content {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.page-header-text {
  min-width: 0;
}

.eyebrow {
  display: block;
  margin-bottom: 7px;
  color: #8a9099;
  font-size: 0.67rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.page-header h1 {
  margin: 0;
  color: #15171a;
  font-size: clamp(1.55rem, 2vw, 2rem);
  line-height: 1.15;
  font-weight: 750;
  letter-spacing: -0.035em;
}

.page-header p {
  max-width: 650px;
  margin: 8px 0 0;
  color: #737983;
  font-size: 0.88rem;
  line-height: 1.55;
}

.header-action {
  flex-shrink: 0;
  min-height: 42px;
  padding: 0 16px;
  border: 0;
  border-radius: 8px;
  background: #17191c;
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font: inherit;
  font-size: 0.82rem;
  font-weight: 650;
  cursor: pointer;
  transition:
    transform 0.18s ease,
    background 0.18s ease,
    box-shadow 0.18s ease;
}

.header-action:hover {
  background: #292c31;
  transform: translateY(-1px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.header-action:active {
  transform: translateY(0);
}

.action-icon {
  font-size: 1.1rem;
  line-height: 1;
  font-weight: 400;
}

@media (max-width: 640px) {
  .page-header {
    margin-bottom: 22px;
  }

  .page-header-content {
    align-items: stretch;
    flex-direction: column;
    gap: 16px;
  }

  .header-action {
    width: 100%;
  }
}
</style>
