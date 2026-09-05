import api from "./api";

export interface OrderItem {
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: string;
  status: string;
  totalAmount: number;
  createdAt: string;
  items: OrderItem[];
}

export const orderService = {
  getAll: () => api.get<Order[]>("/orders/admin").then((r) => r.data),
  updateStatus: (id: string, status: string) =>
    api.put<Order>(`/orders/${id}/status`, { status }).then((r) => r.data),
};
