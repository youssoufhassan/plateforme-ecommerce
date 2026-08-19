import api from "./api";

export interface CartItem {
  itemId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
}

export interface Cart {
  items: CartItem[];
  total: number;
}

export async function fetchCart(): Promise<Cart> {
  const response = await api.get<Cart>("/cart");
  return response.data;
}

export async function addToCart(
  productId: string,
  quantity: number,
): Promise<Cart> {
  const response = await api.post<Cart>("/cart/items", { productId, quantity });
  return response.data;
}

export async function updateCartItem(
  itemId: string,
  quantity: number,
): Promise<Cart> {
  const response = await api.put<Cart>(`/cart/items/${itemId}`, { quantity });
  return response.data;
}

export async function removeCartItem(itemId: string): Promise<Cart> {
  const response = await api.delete<Cart>(`/cart/items/${itemId}`);
  return response.data;
}
