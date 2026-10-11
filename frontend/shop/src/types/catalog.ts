import type { Product } from "@/types/product";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface CatalogFilters {
  brands: string[];
  minPrice: number | null;
  maxPrice: number | null;
  sorts: string[];
}

export interface SearchParams {
  q?: string;
  category?: string;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  availableOnly?: boolean;
  sort?: string;
  page?: number;
  size?: number;
}

/** Un nœud de l'arborescence de catégories, avec ses enfants. */
export interface CategoryNode {
  id: string;
  name: string;
  slug: string;
  /** Chemin complet : "homme/parfums/eaux-de-parfum". */
  path: string;
  depth: number;
  position: number;
  active: boolean;
  /** Produits disponibles dans toute la branche, sous-niveaux compris. */
  productCount: number;
  children: CategoryNode[];
}

export type ProductPage = PageResponse<Product>;
