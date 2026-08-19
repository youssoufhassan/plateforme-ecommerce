<script setup lang="ts">
import { onMounted } from "vue";
import { useCartStore } from "../stores/cartStore";
import { useRouter } from "vue-router";
import api from "../services/api";

const cartStore = useCartStore();
const router = useRouter();

onMounted(() => {
  cartStore.loadCart();
});

async function handleCheckout() {
  try {
    await api.post("/orders/checkout");
    alert("Commande passée avec succès !");
    router.push("/");
  } catch (e) {
    alert("Erreur lors de la commande");
  }
}
</script>

<template>
  <div>
    <h1>Mon panier</h1>

    <p v-if="cartStore.loading">Chargement...</p>

    <div v-else-if="cartStore.cart.items.length === 0">
      <p>Ton panier est vide.</p>
    </div>

    <div v-else>
      <ul>
        <li v-for="item in cartStore.cart.items" :key="item.itemId">
          {{ item.productName }} — {{ item.unitPrice }} € x
          <input
            type="number"
            min="1"
            :value="item.quantity"
            @change="
              cartStore.updateItem(
                item.itemId,
                Number(($event.target as HTMLInputElement).value),
              )
            "
          />
          <button @click="cartStore.removeItem(item.itemId)">Retirer</button>
        </li>
      </ul>

      <p>
        <strong>Total : {{ cartStore.cart.total }} €</strong>
      </p>

      <button @click="handleCheckout">Passer la commande</button>
    </div>
  </div>
</template>
