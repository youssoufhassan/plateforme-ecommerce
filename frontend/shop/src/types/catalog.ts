import type { Product } from "@/types/product";

/**
 * Réponse paginée générique de l'API.
 */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

/**
 * Filtres généraux disponibles dans le catalogue.
 */
export interface CatalogFilters {
  brands: string[];
  minPrice: number | null;
  maxPrice: number | null;
  sorts: string[];
}

/**
 * Paramètres transmis à la recherche du catalogue.
 *
 * Les attributs spécifiques seront exploités lors du
 * raccordement du front-end au backend.
 */
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
  attributes?: Record<string, string | string[] | number | null>;
}

/**
 * Un nœud de l'arborescence de catégories.
 *
 * Exemple de chemin :
 * "homme/parfums/eaux-de-parfum".
 */
export interface CategoryNode {
  id: string;
  name: string;
  slug: string;
  path: string;
  depth: number;
  position: number;
  active: boolean;
  productCount: number;
  children: CategoryNode[];
}

/**
 * Types des champs de filtres spécifiques.
 */
export type FilterFieldType = "select" | "checkbox" | "range";

/**
 * Option disponible dans un filtre.
 */
export interface FilterOption {
  label: string;
  value: string;
}

/**
 * Configuration d'un filtre spécifique à une catégorie.
 */
export interface CategoryFilterDefinition {
  key: string;
  label: string;
  type: FilterFieldType;
  options?: FilterOption[];
  min?: number;
  max?: number;
  step?: number;
  unit?: string;
  multiple?: boolean;
}

/**
 * Valeurs sélectionnées dans les filtres spécifiques.
 */
export type CategoryFilterValue = string | string[] | number | null;

/**
 * État complet des filtres côté front-end.
 */
export interface CatalogFilterState {
  minPrice: number | null;
  maxPrice: number | null;
  availableOnly: boolean;
  brand: string[];
  sort: string;
  attributes: Record<string, CategoryFilterValue>;
}

/**
 * Paramètres normalisés pour préparer une recherche.
 */
export interface CatalogSearchState {
  category?: string;
  q?: string;
  minPrice?: number;
  maxPrice?: number;
  availableOnly?: boolean;
  brand?: string[];
  sort: string;
  page: number;
  size: number;
  attributes: Record<string, CategoryFilterValue>;
}

/**
 * Résultat paginé de produits.
 */
export type ProductPage = PageResponse<Product>;
