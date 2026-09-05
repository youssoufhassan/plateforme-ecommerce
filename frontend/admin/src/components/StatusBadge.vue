<script setup lang="ts">
const props = defineProps<{ status: string }>();

const config: Record<string, { label: string; color: string; bg: string }> = {
  PENDING: { label: "En attente", color: "#946200", bg: "#fff3d9" },
  PAID: { label: "Payée", color: "#0a6b3d", bg: "#e0f5e9" },
  PREPARING: { label: "Préparation", color: "#1a4fa0", bg: "#e3edfb" },
  SHIPPED: { label: "Expédiée", color: "#5b3ba0", bg: "#ece3fb" },
  DELIVERED: { label: "Livrée", color: "#3a3a3a", bg: "#eeeeee" },
};

const c = config[props.status] || {
  label: props.status,
  color: "#666",
  bg: "#eee",
};
</script>

<template>
  <span class="status-badge" :style="{ color: c.color, background: c.bg }">
    {{ c.label }}
  </span>
</template>

<style scoped>
.status-badge {
  display: inline-block;
  padding: 0.25rem 0.65rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}
</style>
