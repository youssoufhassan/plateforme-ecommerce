export interface CartItem {
  itemId: string;
  productId: string;
  variantId: string;
  productName: string;
  variantLabel: string | null;
  unitPrice: number;
  quantity: number;
  available: boolean;
  /** Plafond imposé par le stock. null = pas de limite connue. */
  maxQuantity: number | null;
}

export interface Cart {
  items: CartItem[];
  subtotal: number;
  shipping: number;
  vat: number;
  vatRate: number;
  total: number;
  freeShippingThreshold: number | null;
  amountUntilFreeShipping: number;
  /** Au moins un article empêche la commande. */
  checkoutBlocked: boolean;
  /** Messages à afficher : indisponibilité, stock réduit, prix modifié. */
  warnings: string[];
}

/** Ligne conservée dans le navigateur tant que le visiteur n'est pas identifié. */
export interface LocalCartLine {
  productId: string;
  variantId: string;
  quantity: number;
}
