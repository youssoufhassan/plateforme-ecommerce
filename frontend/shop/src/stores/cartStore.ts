import { defineStore } from "pinia";
import { computed, ref } from "vue";

import {
  addCartItem,
  clearCart as clearServerCart,
  fetchCart,
  removeCartItem,
  updateCartItem,
} from "@/services/cartService";
import { apiMessage, fetchProduct } from "@/services/productService";
import type { Cart, CartItem, LocalCartLine } from "@/types/cart";

const STORAGE_KEY = "shahin:cart";
const FREE_SHIPPING_THRESHOLD = 40;
const FLAT_SHIPPING = 4.9;
const VAT_RATE = 20;

function readLocal(): LocalCartLine[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function writeLocal(lines: LocalCartLine[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(lines));
  } catch {
    // Stockage indisponible (navigation privée) : le panier reste en mémoire
  }
}

export const useCartStore = defineStore("cart", () => {
  const cart = ref<Cart | null>(null);
  const localLines = ref<LocalCartLine[]>(readLocal());
  const authenticated = ref(false);

  const loading = ref(false);
  const error = ref<string | null>(null);

  /** Dernier article ajouté, pour le panneau de confirmation. */
  const lastAdded = ref<{ name: string; label: string | null } | null>(null);
  const panelOpen = ref(false);

  const items = computed<CartItem[]>(() => cart.value?.items ?? []);

  const count = computed(() =>
    items.value.reduce((total, item) => total + item.quantity, 0),
  );

  const isEmpty = computed(() => items.value.length === 0);

  const warnings = computed(() => cart.value?.warnings ?? []);
  const checkoutBlocked = computed(() => cart.value?.checkoutBlocked ?? false);

  /**
   * Recalcule les montants d'un panier local.
   * Ce sont des estimations : le serveur reste seul juge au moment de commander.
   */
  function computeLocalTotals(lines: CartItem[]): Cart {
    const subtotal = lines
      .filter((line) => line.available)
      .reduce((total, line) => total + line.unitPrice * line.quantity, 0);

    const shipping =
      subtotal === 0 || subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : FLAT_SHIPPING;

    const total = subtotal + shipping;
    const vat = total - total / (1 + VAT_RATE / 100);

    return {
      items: lines,
      subtotal: round(subtotal),
      shipping: round(shipping),
      vat: round(vat),
      vatRate: VAT_RATE,
      total: round(total),
      freeShippingThreshold: FREE_SHIPPING_THRESHOLD,
      amountUntilFreeShipping: round(
        Math.max(0, FREE_SHIPPING_THRESHOLD - subtotal),
      ),
      checkoutBlocked: lines.some((line) => !line.available),
      warnings: lines
        .filter((line) => !line.available)
        .map((line) => `${line.productName} n'est plus disponible`),
    };
  }

  function round(value: number): number {
    return Math.round(value * 100) / 100;
  }

  /** Reconstruit le panier local à partir des produits réels du catalogue. */
  async function hydrateLocalCart(): Promise<void> {
    if (localLines.value.length === 0) {
      cart.value = computeLocalTotals([]);
      return;
    }

    const lines: CartItem[] = [];

    for (const line of localLines.value) {
      try {
        const product = await fetchProduct(line.productId);
        const variant = product.variants?.find((v) => v.id === line.variantId);

        if (!variant) continue; // variante supprimée entre-temps

        lines.push({
          itemId: `${line.productId}:${line.variantId}`,
          productId: line.productId,
          variantId: line.variantId,
          productName: product.name,
          variantLabel: variant.label,
          unitPrice: variant.price,
          quantity: line.quantity,
          available: variant.available,
          maxQuantity: null,
        });
      } catch {
        // Produit retiré du catalogue : on l'oublie silencieusement
      }
    }

    // Les lignes obsolètes disparaissent aussi du navigateur
    localLines.value = lines.map((line) => ({
      productId: line.productId,
      variantId: line.variantId,
      quantity: line.quantity,
    }));
    writeLocal(localLines.value);

    cart.value = computeLocalTotals(lines);
  }

  async function load(): Promise<void> {
    loading.value = true;
    error.value = null;

    try {
      if (authenticated.value) {
        cart.value = await fetchCart();
      } else {
        await hydrateLocalCart();
      }
    } catch (e: unknown) {
      error.value = apiMessage(e, "Le panier n'a pas pu être chargé.");
    } finally {
      loading.value = false;
    }
  }

  async function add(payload: {
    productId: string;
    variantId: string;
    quantity?: number;
    productName?: string;
    variantLabel?: string | null;
  }): Promise<void> {
    const quantity = payload.quantity ?? 1;
    error.value = null;

    if (authenticated.value) {
      // Le serveur vérifie le stock : une erreur ici doit remonter au bouton
      cart.value = await addCartItem({
        productId: payload.productId,
        variantId: payload.variantId,
        quantity,
      });
    } else {
      const existing = localLines.value.find(
        (line) => line.variantId === payload.variantId,
      );

      if (existing) {
        existing.quantity = Math.min(20, existing.quantity + quantity);
      } else {
        localLines.value.push({
          productId: payload.productId,
          variantId: payload.variantId,
          quantity,
        });
      }

      writeLocal(localLines.value);
      await hydrateLocalCart();
      lastAdded.value = {
        name: payload.productName ?? "Article",
        label: payload.variantLabel ?? null,
      };
      panelOpen.value = true;
    }

    lastAdded.value = {
      name: payload.productName ?? "Article",
      label: payload.variantLabel ?? null,
    };
    panelOpen.value = true;
  }

  async function updateQuantity(
    item: CartItem,
    quantity: number,
  ): Promise<void> {
    if (quantity < 1) return;
    error.value = null;

    try {
      if (authenticated.value) {
        cart.value = await updateCartItem(item.itemId, quantity);
      } else {
        const line = localLines.value.find(
          (l) => l.variantId === item.variantId,
        );
        if (line) line.quantity = quantity;

        writeLocal(localLines.value);
        await hydrateLocalCart();
      }
    } catch (e: unknown) {
      error.value = apiMessage(e, "La quantité n'a pas pu être modifiée.");
      await load(); // on resynchronise pour ne pas afficher une valeur fausse
    }
  }

  async function remove(item: CartItem): Promise<void> {
    error.value = null;

    try {
      if (authenticated.value) {
        cart.value = await removeCartItem(item.itemId);
      } else {
        localLines.value = localLines.value.filter(
          (line) => line.variantId !== item.variantId,
        );
        writeLocal(localLines.value);
        await hydrateLocalCart();
      }
    } catch (e: unknown) {
      error.value = apiMessage(e, "L'article n'a pas pu être retiré.");
    }
  }

  async function clear(): Promise<void> {
    if (authenticated.value) {
      cart.value = await clearServerCart();
    } else {
      localLines.value = [];
      writeLocal([]);
      cart.value = computeLocalTotals([]);
    }
  }

  /**
   * Appelé dès qu'un jeton est obtenu : le panier du navigateur
   * est envoyé au serveur, article par article, puis vidé localement.
   */
  async function mergeAfterLogin(): Promise<void> {
    authenticated.value = true;

    const pending = [...localLines.value];

    for (const line of pending) {
      try {
        await addCartItem({
          productId: line.productId,
          variantId: line.variantId,
          quantity: line.quantity,
        });
      } catch {
        // Stock insuffisant ou variante retirée : l'article est simplement ignoré
      }
    }

    localLines.value = [];
    writeLocal([]);

    await load();
  }

  function setAuthenticated(value: boolean): void {
    authenticated.value = value;
  }

  function closePanel(): void {
    panelOpen.value = false;
  }

  return {
    cart,
    items,
    count,
    isEmpty,
    warnings,
    checkoutBlocked,
    loading,
    error,
    lastAdded,
    panelOpen,
    load,
    add,
    updateQuantity,
    remove,
    clear,
    mergeAfterLogin,
    setAuthenticated,
    closePanel,
  };
});
