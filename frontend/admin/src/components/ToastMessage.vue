<script setup lang="ts">
defineProps<{ message: string; type: "success" | "error" }>();
</script>

<template>
  <Transition name="toast-fade">
    <div v-if="message" class="toast" :class="type">{{ message }}</div>
  </Transition>
</template>

<style scoped>
.toast {
  position: fixed;
  bottom: 1.5rem;
  right: 1.5rem;
  background: var(--ink);
  color: white;
  padding: 0.8rem 1.2rem;
  border-radius: 8px;
  font-size: 0.88rem;
  z-index: 1000;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}
.toast.error {
  background: #b3261e;
}
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}
.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
