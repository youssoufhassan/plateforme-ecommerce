import { defineStore } from "pinia";
import { ref } from "vue";
import * as cartService from "../services/cartService";
import type { Cart } from "../services/cartService";

export const useCartStore = defineStore("cart", () => {
  const cart = ref<Cart>({ items: [], total: 0 });
  const loading = ref(false);

  async function loadCart() {
    loading.value = true;
    try {
      cart.value = await cartService.fetchCart();
    } finally {
      loading.value = false;
    }
  }

  async function addItem(productId: string, quantity: number) {
    cart.value = await cartService.addToCart(productId, quantity);
  }

  async function updateItem(itemId: string, quantity: number) {
    cart.value = await cartService.updateCartItem(itemId, quantity);
  }

  async function removeItem(itemId: string) {
    cart.value = await cartService.removeCartItem(itemId);
  }

  return { cart, loading, loadCart, addItem, updateItem, removeItem };
});
