import { defineStore } from "pinia";
import { ref } from "vue";
import * as cartService from "../services/cartService";
import type { Cart } from "../services/cartService";

export const useCartStore = defineStore("cart", () => {
  const cart = ref<Cart>({
    items: [],
    total: 0,
  });

  const loading = ref(false);

  /* =========================================
     CHARGER LE PANIER
  ========================================= */

  async function loadCart() {
    loading.value = true;

    try {
      cart.value = await cartService.fetchCart();
    } finally {
      loading.value = false;
    }
  }

  /* =========================================
     AJOUTER UN ARTICLE
  ========================================= */

  async function addItem(productId: string, quantity: number) {
    loading.value = true;

    try {
      cart.value = await cartService.addToCart(productId, quantity);
    } finally {
      loading.value = false;
    }
  }

  /* =========================================
     MODIFIER LA QUANTITÉ
  ========================================= */

  async function updateItem(itemId: string, quantity: number) {
    loading.value = true;

    try {
      cart.value = await cartService.updateCartItem(itemId, quantity);
    } finally {
      loading.value = false;
    }
  }

  /* =========================================
     SUPPRIMER UN ARTICLE
  ========================================= */

  async function removeItem(itemId: string) {
    loading.value = true;

    try {
      cart.value = await cartService.removeCartItem(itemId);
    } finally {
      loading.value = false;
    }
  }

  /* =========================================
     VIDER LE PANIER LOCAL
  ========================================= */

  function clearCart() {
    cart.value = {
      items: [],
      total: 0,
    };
  }

  return {
    cart,
    loading,
    loadCart,
    addItem,
    updateItem,
    removeItem,
    clearCart,
  };
});
