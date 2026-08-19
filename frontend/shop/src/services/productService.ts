import api from "./api";

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
}

export async function fetchProducts(): Promise<Product[]> {
  const response = await api.get<Product[]>("/products");
  return response.data;
}
