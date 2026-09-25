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
  categories: string[];
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

export type ProductPage = PageResponse<Product>;
