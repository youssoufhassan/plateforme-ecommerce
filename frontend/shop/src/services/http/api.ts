import axios from "axios";

const TOKEN_KEY = "shahin:token";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

export function getStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function storeToken(token: string | null): void {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {
    // Stockage indisponible : la session ne survivra pas au rechargement
  }
}

/** Le jeton accompagne chaque requête, sans avoir à y penser ailleurs. */
api.interceptors.request.use((config) => {
  const token = getStoredToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Un jeton refusé signifie une session expirée ou invalidée
 * (changement de mot de passe, suppression de compte).
 */
let onUnauthorized: (() => void) | null = null;

export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler;
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const url: string = error.config?.url ?? "";

    // Un échec de connexion n'est pas une session expirée
    const isAuthAttempt =
      url.includes("/auth/login") || url.includes("/auth/guest/");

    if ((status === 401 || status === 403) && !isAuthAttempt) {
      storeToken(null);
      onUnauthorized?.();
    }

    return Promise.reject(error);
  },
);

export default api;
