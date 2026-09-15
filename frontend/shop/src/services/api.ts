import axios from "axios";
import router from "../router";
import { useAuthStore } from "../stores/authStore";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

api.interceptors.request.use((config) => {
  const authStore = useAuthStore();

  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }

  return config;
});

let redirectingOnUnauthorized = false;

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      const currentPath = router.currentRoute.value.fullPath;

      authStore.logout();

      if (!redirectingOnUnauthorized && currentPath !== "/login" && currentPath !== "/register") {
        redirectingOnUnauthorized = true;

        try {
          await router.push({
            name: "login",
            query: { redirect: currentPath },
          });
        } finally {
          redirectingOnUnauthorized = false;
        }
      }
    }

    return Promise.reject(error);
  },
);

export default api;
