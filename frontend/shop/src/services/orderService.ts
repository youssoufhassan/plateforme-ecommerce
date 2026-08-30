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

export async function fetchMyOrders(): Promise<Order[]> {
  const response = await api.get<Order[]>("/orders");
  return response.data;
}
