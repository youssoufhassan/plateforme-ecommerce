import api from "./api";

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrl: string;
  imageUrls: string[];
  categoryName: string;
}

export function fullImageUrl(path: string): string {
  if (!path) return "";
  if (path.startsWith("http")) return path;
  return `http://localhost:8080${path}`;
}

export async function fetchProducts(): Promise<Product[]> {
  const response = await api.get<Product[]>("/products");
  return response.data;
}
