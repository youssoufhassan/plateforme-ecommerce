export interface Product {
  id: string;
  name: string;
  description?: string;
  brand?: string;
  price: number;
  available: boolean;
  imageUrl?: string;
  imageUrls?: string[];
  categoryName?: string;
  variants?: unknown[];
}
