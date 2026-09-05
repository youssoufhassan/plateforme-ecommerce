import api from "./api";

export interface Category {
  id: string;
  name: string;
}

export const categoryService = {
  getAll: () => api.get<Category[]>("/categories").then((r) => r.data),
  create: (name: string) =>
    api.post<Category>("/categories", { name }).then((r) => r.data),
  remove: (id: string) => api.delete(`/categories/${id}`),
};
