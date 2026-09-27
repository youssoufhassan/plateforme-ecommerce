import api from "@/services/http/api";
import type {
  Address,
  AddressPayload,
  CheckoutSession,
  Order,
} from "@/types/order";

export async function fetchAddresses(): Promise<Address[]> {
  const { data } = await api.get<Address[]>("/addresses");
  return data;
}

export async function createAddress(payload: AddressPayload): Promise<Address> {
  const { data } = await api.post<Address>("/addresses", payload);
  return data;
}

/** Pays vers lesquels la livraison est possible. */
export async function fetchShippingCountries(): Promise<string[]> {
  const { data } = await api.get<string[]>("/shipping/countries");
  return data;
}

export async function checkout(addressId: string): Promise<Order> {
  const { data } = await api.post<Order>("/orders/checkout", {
    addressId,
    acceptTerms: true,
  });
  return data;
}

export async function createPaymentSession(
  orderId: string,
): Promise<CheckoutSession> {
  const { data } = await api.post<CheckoutSession>(`/orders/${orderId}/pay`);
  return data;
}

export async function fetchOrders(): Promise<Order[]> {
  const { data } = await api.get<Order[]>("/orders");
  return data;
}
