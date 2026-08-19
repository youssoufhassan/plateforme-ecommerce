import { defineStore } from "pinia";
import { ref } from "vue";
import {
  login as loginApi,
  register as registerApi,
} from "../services/authService";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | null>(localStorage.getItem("token"));

  async function login(email: string, password: string) {
    const newToken = await loginApi(email, password);
    token.value = newToken;
    localStorage.setItem("token", newToken);
  }

  async function register(email: string, password: string) {
    const newToken = await registerApi(email, password);
    token.value = newToken;
    localStorage.setItem("token", newToken);
  }

  function logout() {
    token.value = null;
    localStorage.removeItem("token");
  }

  function isLoggedIn() {
    return token.value !== null;
  }

  return { token, login, register, logout, isLoggedIn };
});
