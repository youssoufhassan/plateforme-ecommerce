import { defineStore } from "pinia";
import { ref } from "vue";
import { login as loginApi } from "../services/authService";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | null>(localStorage.getItem("admin_token"));

  async function login(email: string, password: string) {
    const newToken = await loginApi(email, password);
    token.value = newToken;
    localStorage.setItem("admin_token", newToken);
  }

  function logout() {
    token.value = null;
    localStorage.removeItem("admin_token");
  }

  function isLoggedIn() {
    return token.value !== null;
  }

  return { token, login, logout, isLoggedIn };
});
