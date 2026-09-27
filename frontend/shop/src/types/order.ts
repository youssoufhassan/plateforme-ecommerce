export interface Address {
  id: string;
  firstName: string;
  lastName: string;
  street: string;
  complement: string | null;
  city: string;
  postalCode: string;
  countryCode: string;
  phone: string | null;
}

export interface AddressPayload {
  firstName: string;
  lastName: string;
  street: string;
  complement?: string;
  city: string;
  postalCode: string;
  countryCode: string;
  phone?: string;
}

export interface OrderItem {
  productName: string;
  variantLabel: string | null;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: string;
  status: string;
  subtotalAmount: number;
  shippingAmount: number;
  vatAmount: number;
  vatRate: number;
  totalAmount: number;
  createdAt: string;
  customerFirstName: string | null;
  customerLastName: string | null;
  customerEmail: string | null;
  items: OrderItem[];
}

export interface CheckoutSession {
  sessionId: string;
  checkoutUrl: string;
}
