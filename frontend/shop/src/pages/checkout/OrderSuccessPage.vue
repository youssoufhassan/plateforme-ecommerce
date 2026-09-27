<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";

import { fetchOrders } from "@/services/orderService";
import { useCartStore } from "@/stores/cartStore";
import type { Order } from "@/types/order";

const route = useRoute();
const cartStore = useCartStore();

const POLL_INTERVAL = 2000;
const MAX_ATTEMPTS = 15; // 30 secondes

const order = ref<Order | null>(null);
const status = ref<"waiting" | "confirmed" | "timeout" | "error">("waiting");
const attempts = ref(0);

let timer: ReturnType<typeof setTimeout> | null = null;

const orderId = computed(() => (route.query.order as string) || "");

const orderNumber = computed(() =>
  order.value ? order.value.id.slice(0, 8).toUpperCase() : "",
);

function money(value: number | null | undefined): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
  }).format(value ?? 0);
}

/**
 * Le paiement est confirmé par Stripe côté serveur, de façon asynchrone.
 * On interroge donc la commande jusqu'à ce qu'elle passe à PAID.
 */
async function pollOrder(): Promise<void> {
  attempts.value += 1;

  try {
    const orders = await fetchOrders();
    const found = orders.find((o) => o.id === orderId.value);

    if (found) {
      order.value = found;

      if (found.status !== "PENDING") {
        status.value = "confirmed";
        await cartStore.load();
        return;
      }
    }

    if (attempts.value >= MAX_ATTEMPTS) {
      status.value = "timeout";
      return;
    }

    timer = setTimeout(pollOrder, POLL_INTERVAL);
  } catch {
    status.value = "error";
  }
}

onMounted(() => {
  if (!orderId.value) {
    status.value = "error";
    return;
  }
  pollOrder();
});

onUnmounted(() => {
  if (timer) clearTimeout(timer);
});
</script>

<template>
  <main class="success">
    <div class="success__panel">
      <!-- Attente de la confirmation Stripe -->
      <template v-if="status === 'waiting'">
        <div class="success__spinner" aria-hidden="true"></div>
        <h1 class="success__title">Confirmation du paiement</h1>
        <p class="success__text">
          Votre paiement est en cours de validation. Merci de patienter quelques
          instants sans fermer cette page.
        </p>
      </template>

      <template v-else-if="status === 'confirmed'">
        <h1 class="success__title">Merci pour votre commande</h1>

        <p class="success__text">
          Votre commande <strong>#{{ orderNumber }}</strong> a bien été
          enregistrée. Un email de confirmation vient de vous être envoyé.
        </p>

        <dl class="success__recap">
          <div>
            <dt>Montant</dt>
            <dd>{{ money(order?.totalAmount) }}</dd>
          </div>
          <div>
            <dt>Articles</dt>
            <dd>{{ order?.items.length }}</dd>
          </div>
        </dl>

        <p class="success__next">
          Nous préparons votre commande et vous tiendrons informé de son
          expédition.
        </p>

        <div class="success__actions">
          <RouterLink to="/compte" class="success__primary">
            Suivre ma commande
          </RouterLink>

          <RouterLink to="/produits" class="success__secondary">
            Continuer mes achats
          </RouterLink>
        </div>
      </template>

      <!-- Le paiement peut avoir réussi malgré l'attente dépassée -->
      <template v-else-if="status === 'timeout'">
        <h1 class="success__title">Confirmation en attente</h1>

        <p class="success__text">
          Votre paiement est peut-être déjà validé, mais la confirmation tarde à
          nous parvenir. Consultez vos commandes dans quelques minutes, ou
          vérifiez votre boîte mail.
        </p>

        <div class="success__actions">
          <RouterLink to="/compte" class="success__primary">
            Voir mes commandes
          </RouterLink>
        </div>
      </template>

      <template v-else>
        <h1 class="success__title">Commande introuvable</h1>

        <p class="success__text">
          Nous n'avons pas pu retrouver cette commande. Si vous avez été débité,
          contactez-nous.
        </p>

        <div class="success__actions">
          <RouterLink to="/compte" class="success__primary">
            Voir mes commandes
          </RouterLink>
        </div>
      </template>
    </div>
  </main>
</template>

<style scoped>
.success {
  display: grid;
  place-items: center;
  min-height: 60vh;
  padding: 60px var(--container-padding);
}

.success__panel {
  width: 100%;
  max-width: 480px;
  text-align: center;
}

.success__spinner {
  width: 28px;
  height: 28px;
  margin: 0 auto 22px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-text);
  border-radius: 50%;
  animation: spin 900ms linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .success__spinner {
    animation-duration: 3s;
  }
}

.success__title {
  margin: 0 0 14px;
  font-family: var(--font-display);
  font-size: clamp(25px, 3vw, 34px);
  font-weight: 400;
  line-height: 1.08;
  letter-spacing: -0.03em;
}

.success__text {
  margin: 0;
  color: var(--color-text);
  font-size: 14px;
  line-height: 1.6;
}

.success__recap {
  display: grid;
  gap: 10px;
  max-width: 260px;
  margin: 26px auto 0;
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
}

.success__recap > div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 13.5px;
}

.success__recap dt {
  color: var(--color-text-muted);
}

.success__recap dd {
  margin: 0;
  font-variant-numeric: tabular-nums;
}

.success__next {
  margin: 22px 0 0;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.55;
}

.success__actions {
  display: grid;
  gap: 10px;
  margin-top: 30px;
}

.success__primary {
  display: grid;
  place-items: center;
  height: 48px;
  background: var(--color-black);
  color: var(--color-white);
  font-size: 12px;
  text-decoration: none;
}

.success__primary:hover {
  opacity: 0.85;
}

.success__secondary {
  color: var(--color-text-muted);
  font-size: 12.5px;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.success__secondary:hover {
  color: var(--color-text);
}
</style>
