import api from "./api";

export interface Customer {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
}

export const customerService = {
  getAll: () => api.get<Customer[]>("/admin/customers").then((r) => r.data),
};
