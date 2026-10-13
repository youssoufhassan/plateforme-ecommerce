import type { Product } from "@/types/product";

export interface HomeSection {
  id: string;
  slug: string;
  title: string;
  subtitle: string | null;
  position: number;
  active: boolean;
  totalCount: number;
  products: Product[];
}

export interface HomeCategory {
  id: string;
  name: string;
}

export interface HomePayload {
  featured: Product[];
  newest: Product[];
  bestSellers: Product[];
  sections: HomeSection[];
  categories: HomeCategory[];
  /** Marques les mieux représentées, pour le bandeau de maisons. */
  topBrands: string[];
}
