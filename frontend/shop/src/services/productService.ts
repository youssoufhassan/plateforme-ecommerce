import axios from "axios";

import api from "@/services/http/api";
import type {
  CatalogFilters,
  ProductPage,
  SearchParams,
} from "@/types/catalog";
import type { HomePayload } from "@/types/home";
import type { Product } from "@/types/product";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

/** Origine du backend, pour préfixer les images servies en chemin relatif. */
export const BACKEND_ORIGIN = API_URL.replace(/\/api\/?$/, "");

/**
 * Les images arrivent sous trois formes :
 *   /images/...  fichiers historiques
 *   /media/...   envoyées depuis le back-office
 *   https://...  hébergées chez un fournisseur
 */
export function fullImageUrl(path?: string | null): string {
  if (!path) return "";
  if (/^https?:\/\//i.test(path)) return path;
  return `${BACKEND_ORIGIN}${path.startsWith("/") ? "" : "/"}${path}`;
}

/** Message d'erreur lisible, en privilégiant celui renvoyé par le backend. */
export function apiMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message;
    if (typeof message === "string" && message.trim()) {
      return message;
    }
    if (!error.response) {
      return "Serveur injoignable. Vérifiez votre connexion.";
    }
  }
  return fallback;
}

/** Toute la page d'accueil en un seul appel. */
export async function fetchHomepage(limit = 8): Promise<HomePayload> {
  const { data } = await api.get<HomePayload>("/products/home", {
    params: { limit },
  });
  return data;
}

/** Catalogue paginé et filtré. */
export async function searchProducts(
  params: SearchParams = {},
): Promise<ProductPage> {
  // Les paramètres vides ne doivent pas apparaître dans l'URL
  const cleaned = Object.fromEntries(
    Object.entries(params).filter(
      ([, value]) => value !== undefined && value !== null && value !== "",
    ),
  );

  const { data } = await api.get<ProductPage>("/products/search", {
    params: cleaned,
  });
  return data;
}

/** Valeurs disponibles pour construire les filtres du catalogue. */
export async function fetchCatalogFilters(): Promise<CatalogFilters> {
  const { data } = await api.get<CatalogFilters>("/products/filters");
  return data;
}

/** Contenu complet d'une section éditoriale. */
export async function fetchSection(slug: string) {
  const { data } = await api.get(`/sections/${slug}`);
  return data;
}

export async function fetchProduct(id: string): Promise<Product> {
  const { data } = await api.get<Product>(`/products/${id}`);
  return data;
}

export async function fetchSimilarProducts(
  id: string,
  limit = 4,
): Promise<Product[]> {
  const { data } = await api.get<Product[]>(`/products/${id}/similar`, {
    params: { limit },
  });
  return data;
}

/** Ancien endpoint, sans pagination. Conservé pour compatibilité. */
export async function fetchProducts(): Promise<Product[]> {
  const { data } = await api.get<Product[]>("/products");
  return data;
}

export type { Product };
