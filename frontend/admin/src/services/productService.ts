import api from "./api";

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryName: string;

  // Images utilisées par le frontend boutique
  imageUrl?: string;
  imageUrls?: string[];
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

/**
 * Transforme un chemin d'image provenant du backend
 * en URL utilisable par le navigateur.
 *
 * Exemples :
 * "/uploads/products/image.jpg"
 * "uploads/products/image.jpg"
 * "http://localhost:8080/uploads/products/image.jpg"
 */
export function fullImageUrl(path?: string): string {
  if (!path) return "";

  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }

  const baseUrl = api.defaults.baseURL?.replace(/\/api\/?$/, "") ?? "";

  return `${baseUrl}/${path.replace(/^\/+/, "")}`;
}
