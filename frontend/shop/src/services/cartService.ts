import api from "@/services/http/api";
import type { Cart } from "@/types/cart";

export async function fetchCart(): Promise<Cart> {
  const { data } = await api.get<Cart>("/cart");
  return data;
}

export async function addCartItem(payload: {
  productId: string;
  variantId: string;
  quantity: number;
}): Promise<Cart> {
  const { data } = await api.post<Cart>("/cart/items", payload);
  return data;
}

export async function updateCartItem(
  itemId: string,
  quantity: number,
): Promise<Cart> {
  const { data } = await api.put<Cart>(`/cart/items/${itemId}`, { quantity });
  return data;
}

export async function removeCartItem(itemId: string): Promise<Cart> {
  const { data } = await api.delete<Cart>(`/cart/items/${itemId}`);
  return data;
}

export async function clearCart(): Promise<Cart> {
  const { data } = await api.delete<Cart>("/cart");
  return data;
}
