import api from "./api";

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryName: string;
}

export interface ProductPayload {
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
}

export const productService = {
  getAll: () => api.get<Product[]>("/products/admin").then((r) => r.data),
  create: (data: ProductPayload) =>
    api.post<Product>("/products/admin", data).then((r) => r.data),
  update: (id: string, data: ProductPayload) =>
    api.put<Product>(`/products/admin/${id}`, data).then((r) => r.data),
  remove: (id: string) => api.delete(`/products/admin/${id}`),
};
