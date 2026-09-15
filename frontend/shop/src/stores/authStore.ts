import { computed, ref } from "vue";
import { defineStore } from "pinia";
import {
  login as loginApi,
  register as registerApi,
} from "../services/authService";

const TOKEN_KEY = "token";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY));
  const isAuthenticated = computed(() => Boolean(token.value));

  function setToken(newToken: string | null) {
    token.value = newToken;

    if (newToken) {
      localStorage.setItem(TOKEN_KEY, newToken);
    } else {
      localStorage.removeItem(TOKEN_KEY);
    }
  }

  async function login(email: string, password: string) {
    const newToken = await loginApi(email, password);
    setToken(newToken);
  }

  async function register(email: string, password: string) {
    const newToken = await registerApi(email, password);
    setToken(newToken);
  }

  function logout() {
    setToken(null);
  }

  function isLoggedIn() {
    return isAuthenticated.value;
  }

  return {
    token,
    isAuthenticated,
    login,
    register,
    logout,
    isLoggedIn,
  };
});
