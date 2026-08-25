import api from "./api";

export interface Category {
  id: string;
  name: string;
}

export async function fetchCategories(): Promise<Category[]> {
  const response = await api.get<Category[]>("/categories");
  return response.data;
}
