<script setup lang="ts">
/**
 * Trois engagements, écrits en dur : ils ne dépendent d'aucune donnée.
 * Les textes doivent correspondre exactement aux conditions de vente.
 */
const items = [
  {
    title: "Livraison en Europe",
    text: "Offerte dès 40 € d'achat en France. Expédition suivie vers toute l'Union européenne.",
  },
  {
    title: "Paiement sécurisé",
    text: "Carte bancaire via Stripe. Vos données bancaires ne transitent jamais par nos serveurs.",
  },
  {
    title: "Retours sous 14 jours",
    text: "Droit de rétractation de quatorze jours à compter de la réception de votre commande.",
  },
];
</script>

<template>
  <section class="reassurance" aria-label="Nos engagements">
    <div class="container reassurance__row">
      <article
        v-for="item in items"
        :key="item.title"
        class="reassurance__item"
      >
        <h2 class="reassurance__title">{{ item.title }}</h2>
        <p class="reassurance__text">{{ item.text }}</p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.reassurance {
  padding-block: var(--space-12);
  border-top: 1px solid var(--color-line);
}

.reassurance__row {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-8);
}

.reassurance__title {
  margin: 0 0 var(--space-2);
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: var(--weight-medium);
}

.reassurance__text {
  margin: 0;
  max-width: 38ch;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
}

@media (min-width: 768px) {
  .reassurance__row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--space-10);
  }
}

@media (max-width: 767px) {
  .reassurance {
    padding-block: var(--space-8);
  }

  .reassurance__row {
    gap: var(--space-6);
  }
}
</style>
