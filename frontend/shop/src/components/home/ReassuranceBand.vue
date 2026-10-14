<script setup lang="ts">
/**
 * Engagements applicables : livraison, paiement sécurisé Stripe, retours, service client.
 * Conforme aux conditions réelles de vente de la boutique.
 */
const items = [
  {
    icon: "✦",
    title: "Livraison en Europe",
    text: "Offerte dès 40 € d'achat. Expédition soignée et suivie vers toute l'Union européenne.",
  },
  {
    icon: "🔒",
    title: "Paiement 100% sécurisé",
    text: "Transactions cryptées via Stripe. Cartes bancaires Visa, Mastercard, American Express.",
  },
  {
    icon: "↺",
    title: "Retours sous 14 jours",
    text: "Droit de rétractation légal de 14 jours à compter de la réception de vos articles.",
  },
  {
    icon: "💬",
    title: "Service client attentif",
    text: "Une assistance réactive pour vous renseigner et suivre vos commandes à tout moment.",
  },
];
</script>

<template>
  <section class="reassurance" aria-label="Engagements et garanties SIDRA">
    <div class="container reassurance__row">
      <article
        v-for="item in items"
        :key="item.title"
        class="reassurance__item"
      >
        <span class="reassurance__icon" aria-hidden="true">{{ item.icon }}</span>
        <h2 class="reassurance__title">{{ item.title }}</h2>
        <p class="reassurance__text">{{ item.text }}</p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.reassurance {
  padding-block: clamp(36px, 5vw, 64px);
  border-top: 1px solid var(--color-line, #e0dfdb);
  background: var(--color-surface, #ffffff);
}

.reassurance__row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: clamp(20px, 3vw, 36px);
}

.reassurance__item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reassurance__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-page, #f2f2f1);
  color: var(--color-ink, #000);
  font-size: 0.95rem;
  margin-bottom: 4px;
}

.reassurance__title {
  margin: 0;
  font-family: var(--font-body, "Jost", sans-serif);
  font-size: 0.92rem;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--color-ink, #000);
}

.reassurance__text {
  margin: 0;
  color: var(--color-text-muted, #767672);
  font-size: 0.82rem;
  line-height: 1.55;
}

@media (max-width: 1024px) {
  .reassurance__row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 28px 20px;
  }
}

@media (max-width: 580px) {
  .reassurance__row {
    grid-template-columns: 1fr;
    gap: 22px;
  }
}
</style>
