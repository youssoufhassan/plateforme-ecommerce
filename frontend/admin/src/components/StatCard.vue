<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string;
    value: string;
    change?: string;
    description?: string;
    icon?: string;
    trend?: "up" | "down" | "neutral";
    unavailable?: boolean;
  }>(),
  {
    change: "",
    description: "",
    icon: "",
    trend: "neutral",
    unavailable: false,
  },
);
</script>

<template>
  <article class="stat-card" :class="{ unavailable }">
    <div class="stat-top">
      <div class="stat-label">
        {{ label }}
      </div>

      ```
      <div v-if="icon" class="stat-icon">
        {{ icon }}
      </div>
    </div>

    <div class="stat-value">
      {{ unavailable ? "—" : value }}
    </div>

    <div v-if="!unavailable && (change || description)" class="stat-bottom">
      <span v-if="change" class="stat-change" :class="`trend-${trend}`">
        <span v-if="trend === 'up'">↗</span>
        <span v-else-if="trend === 'down'">↘</span>
        <span v-else>—</span>

        {{ change }}
      </span>

      <span v-if="description" class="stat-description">
        {{ description }}
      </span>
    </div>

    <div v-if="unavailable" class="unavailable-note">
      Donnée indisponible pour le moment
    </div>
    ```
  </article>
</template>

<style scoped>
.stat-card {
  min-width: 0;
  padding: 20px;
  border: 1px solid #e8eaed;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: #dfe2e6;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}

.stat-top {
  min-height: 30px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.stat-label {
  color: #737983;
  font-size: 0.78rem;
  font-weight: 600;
}

.stat-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border: 1px solid #e7e9ec;
  border-radius: 8px;
  background: #f8f9fa;
  color: #444950;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.stat-value {
  margin-top: 10px;
  color: #15171a;
  font-size: clamp(1.45rem, 2.2vw, 1.85rem);
  line-height: 1.1;
  font-weight: 750;
  letter-spacing: -0.04em;
}

.stat-bottom {
  min-height: 20px;
  margin-top: 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
  font-size: 0.7rem;
}

.stat-change {
  font-weight: 700;
}

.trend-up {
  color: #16834b;
}

.trend-down {
  color: #c43d3d;
}

.trend-neutral {
  color: #777d86;
}

.stat-description {
  color: #9297a0;
}

.stat-change + .stat-description::before {
  content: "•";
  margin-right: 7px;
  color: #c5c8cd;
}

.stat-card.unavailable {
  background: #fafafa;
  opacity: 0.72;
}

.stat-card.unavailable:hover {
  transform: none;
  box-shadow: none;
}

.unavailable-note {
  margin-top: 10px;
  color: #9297a0;
  font-size: 0.68rem;
  line-height: 1.4;
}

@media (max-width: 640px) {
  .stat-card {
    padding: 17px;
  }
}
</style>
