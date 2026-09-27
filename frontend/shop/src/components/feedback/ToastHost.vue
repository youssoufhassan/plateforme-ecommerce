<script setup lang="ts">
import { RouterLink } from "vue-router";

import CloseIcon from "@/components/icons/CloseIcon.vue";
import { useToastStore } from "@/stores/toastStore";

const toastStore = useToastStore();
</script>

<template>
  <Teleport to="body">
    <div class="toasts" role="region" aria-label="Notifications">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toastStore.toasts"
          :key="toast.id"
          class="toast"
          :class="`toast--${toast.kind}`"
          role="status"
        >
          <p class="toast__message">{{ toast.message }}</p>

          <RouterLink
            v-if="toast.action"
            :to="toast.action.to"
            class="toast__action"
            @click="toastStore.dismiss(toast.id)"
          >
            {{ toast.action.label }}
          </RouterLink>

          <button
            type="button"
            class="toast__close"
            aria-label="Fermer la notification"
            @click="toastStore.dismiss(toast.id)"
          >
            <CloseIcon />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toasts {
  position: fixed;
  right: var(--space-4);
  bottom: var(--space-4);
  z-index: var(--z-toast);

  display: grid;
  gap: var(--space-2);
  width: min(380px, calc(100vw - var(--space-8)));

  /* Les notifications ne doivent pas bloquer les clics derrière elles */
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: var(--space-3);

  padding: var(--space-4);

  background: var(--color-ink);
  color: var(--color-text-inverse);
  box-shadow: var(--shadow-overlay);

  pointer-events: auto;
}

.toast--error {
  background: var(--color-error);
}

.toast__message {
  flex: 1;
  min-width: 0;
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
}

.toast__action {
  flex-shrink: 0;
  color: inherit;
  font-size: var(--text-sm);
  text-decoration: underline;
  text-underline-offset: 0.25em;
}

.toast__close {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: none;
  background: transparent;
  color: inherit;
  cursor: pointer;
  opacity: 0.7;
  transition: opacity var(--transition-fast);
}

.toast__close:hover {
  opacity: 1;
}

.toast__close svg {
  width: 15px;
  height: 15px;
}

/* Le mouvement répond à l'apparition d'une information */
.toast-enter-active,
.toast-leave-active {
  transition:
    opacity var(--transition-base),
    transform var(--transition-base);
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 767px) {
  .toasts {
    right: var(--space-3);
    left: var(--space-3);
    bottom: var(--space-3);
    width: auto;
  }
}
</style>
