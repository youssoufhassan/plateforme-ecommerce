import api from "@/services/http/api";

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

export function fullImageUrl(path?: string): string {
  if (!path) {
    return "";
  }

  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }

  return `http://localhost:8080${path}`;
}

export async function fetchProducts(): Promise<Product[]> {
  const response = await api.get<Product[]>("/products");

  return response.data;
}
