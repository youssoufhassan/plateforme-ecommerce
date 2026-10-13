export interface ProductOptionValue {
  id: string;
  value: string;
  position: number;
}

export interface ProductOption {
  id: string;
  name: string;
  position: number;
  values: ProductOptionValue[];
}

export interface ProductVariant {
  id: string;
  label: string;
  price: number;
  available: boolean;
  /** Les valeurs qui composent cette variante : une par axe. */
  optionValueIds?: string[];
}

export interface Product {
  id: string;
  name: string;
  description?: string;
  brand?: string | null;
  /** Prix le plus bas des variantes : « à partir de » si elles diffèrent. */
  price: number;
  /** Au moins une variante achetable. Le stock exact n'est jamais exposé. */
  available: boolean;
  imageUrl?: string;
  imageUrls?: string[];
  categoryName?: string;
  variants?: ProductVariant[];
  /** Axes de variation : vide pour un produit sans déclinaison. */
  options?: ProductOption[];
}
