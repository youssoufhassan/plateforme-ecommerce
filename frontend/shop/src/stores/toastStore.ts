import { defineStore } from "pinia";
import { ref } from "vue";

export type ToastKind = "success" | "error" | "info";

export interface Toast {
  id: number;
  kind: ToastKind;
  message: string;
  /** Lien facultatif proposé dans la notification. */
  action?: { label: string; to: string };
}

const DURATIONS: Record<ToastKind, number> = {
  success: 3500,
  info: 4000,
  // Une erreur reste plus longtemps : elle demande une décision
  error: 6000,
};

export const useToastStore = defineStore("toast", () => {
  const toasts = ref<Toast[]>([]);
  let nextId = 1;

  function dismiss(id: number): void {
    toasts.value = toasts.value.filter((toast) => toast.id !== id);
  }

  function push(
    kind: ToastKind,
    message: string,
    action?: Toast["action"],
  ): number {
    const id = nextId++;

    toasts.value = [...toasts.value, { id, kind, message, action }];

    setTimeout(() => dismiss(id), DURATIONS[kind]);

    return id;
  }

  return {
    toasts,
    dismiss,
    success: (message: string, action?: Toast["action"]) =>
      push("success", message, action),
    error: (message: string) => push("error", message),
    info: (message: string, action?: Toast["action"]) =>
      push("info", message, action),
  };
});
