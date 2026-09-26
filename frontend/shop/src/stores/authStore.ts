import { defineStore } from "pinia";
import { computed, ref } from "vue";

import { getStoredToken, storeToken } from "@/services/http/api";
import {
  fetchProfile,
  login as loginRequest,
  register as registerRequest,
  setGuestPassword,
  verifyGuestCode,
} from "@/services/authService";
import { useCartStore } from "@/stores/cartStore";
import type { UserProfile } from "@/types/auth";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | null>(getStoredToken());
  const profile = ref<UserProfile | null>(null);
  const loading = ref(false);

  const isAuthenticated = computed(() => !!token.value);
  const isGuest = computed(() => profile.value?.guest ?? false);
  const emailVerified = computed(() => profile.value?.emailVerified ?? false);

  const displayName = computed(() => {
    if (!profile.value) return "";
    const { firstName, lastName, email } = profile.value;
    const name = `${firstName ?? ""} ${lastName ?? ""}`.trim();
    return name || email;
  });

  /** Après toute obtention de jeton : profil chargé, panier local fusionné. */
  async function applyToken(value: string): Promise<void> {
    token.value = value;
    storeToken(value);

    await loadProfile();

    const cartStore = useCartStore();
    await cartStore.mergeAfterLogin();
  }

  async function loadProfile(): Promise<void> {
    if (!token.value) return;

    try {
      profile.value = await fetchProfile();
    } catch {
      // Jeton invalide : l'intercepteur a déjà déclenché la déconnexion
      profile.value = null;
    }
  }

  async function login(email: string, password: string): Promise<void> {
    loading.value = true;
    try {
      const response = await loginRequest(email, password);
      await applyToken(response.token);
    } finally {
      loading.value = false;
    }
  }

  async function register(email: string, password: string): Promise<void> {
    loading.value = true;
    try {
      const response = await registerRequest(email, password);
      await applyToken(response.token);
    } finally {
      loading.value = false;
    }
  }

  /** Identification par code : crée un compte invité si l'email est inconnu. */
  async function loginWithCode(email: string, code: string): Promise<void> {
    loading.value = true;
    try {
      const response = await verifyGuestCode(email, code);
      await applyToken(response.token);
    } finally {
      loading.value = false;
    }
  }

  /** Transforme un compte invité en compte complet. */
  async function createPassword(password: string): Promise<void> {
    const response = await setGuestPassword(password);
    token.value = response.token;
    storeToken(response.token);
    await loadProfile();
  }

  function logout(): void {
    token.value = null;
    profile.value = null;
    storeToken(null);

    const cartStore = useCartStore();
    cartStore.setAuthenticated(false);
    cartStore.load();
  }

  /** Appelé au démarrage : restaure la session si un jeton existe. */
  async function restore(): Promise<void> {
    if (!token.value) return;

    const cartStore = useCartStore();
    cartStore.setAuthenticated(true);

    await loadProfile();
  }

  return {
    token,
    profile,
    loading,
    isAuthenticated,
    isGuest,
    emailVerified,
    displayName,
    login,
    register,
    loginWithCode,
    createPassword,
    loadProfile,
    logout,
    restore,
  };
});
