import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

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
