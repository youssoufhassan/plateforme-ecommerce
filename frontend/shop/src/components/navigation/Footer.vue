<script setup lang="ts">
import { ref } from "vue";
import { RouterLink } from "vue-router";

const email = ref("");
const subscribed = ref(false);

function subscribe() {
  const value = email.value.trim();
  if (!value) return;

  subscribed.value = true;
  email.value = "";
}
</script>

<template>
  <footer class="site-footer">
    <!-- NEWSLETTER ÉDITORIALE -->
    <section class="site-footer__newsletter">
      <div class="site-footer__newsletter-inner container">
        <div class="site-footer__newsletter-content">
          <span class="site-footer__newsletter-eyebrow">LETTRE D'INFORMATION</span>
          <h2>Entrez dans l'univers SIDRA.</h2>
          <p>
            Recevez nos nouveautés de saison, nos sélections exclusives et nos inspirations éditoriales
            directement dans votre boîte mail.
          </p>
        </div>

        <form class="site-footer__newsletter-form" @submit.prevent="subscribe">
          <div class="site-footer__input-wrapper">
            <input
              v-model="email"
              type="email"
              placeholder="Votre adresse e-mail"
              aria-label="Votre adresse e-mail"
              required
            />
            <button type="submit">S'inscrire</button>
          </div>

          <p v-if="subscribed" class="site-footer__success" role="status">
            Merci. Vous êtes désormais inscrit(e) aux communications exclusives SIDRA.
          </p>
        </form>
      </div>
    </section>

    <!-- FOOTER PRINCIPAL -->
    <div class="site-footer__main container">
      <div class="site-footer__top">
        <!-- MARQUE -->
        <div class="site-footer__brand-column">
          <RouterLink to="/" class="site-footer__logo" aria-label="SIDRA — Accueil">
            SIDRA
          </RouterLink>

          <p class="site-footer__brand-description">
            Enseigne e-commerce contemporaine dédiée aux créations d'exception :
            mode masculine et féminine, haute parfumerie, joaillerie, maroquinerie, maison et électronique.
          </p>
        </div>

        <!-- BOUTIQUE & UNIVERS -->
        <div class="site-footer__column">
          <h3>Boutique</h3>
          <nav aria-label="Navigation univers">
            <RouterLink to="/produits">Tous les produits</RouterLink>
            <RouterLink to="/produits?category=homme">Mode Homme</RouterLink>
            <RouterLink to="/produits?category=femme">Mode Femme</RouterLink>
            <RouterLink to="/produits?category=beaute">Beauté & Parfums</RouterLink>
            <RouterLink to="/produits?category=enfant">Enfant</RouterLink>
            <RouterLink to="/produits?category=accessoires">Accessoires</RouterLink>
            <RouterLink to="/produits?category=maison">Maison & High-Tech</RouterLink>
            <RouterLink to="/produits?sort=newest">Nouveautés</RouterLink>
          </nav>
        </div>

        <!-- SERVICES CLIENT -->
        <div class="site-footer__column">
          <h3>Service Client</h3>
          <nav aria-label="Services client">
            <RouterLink to="/compte">Suivre ma commande</RouterLink>
            <RouterLink to="/favoris">Mes favoris</RouterLink>
            <RouterLink to="/panier">Mon panier</RouterLink>
            <RouterLink to="/legal/retours">Livraison & Retours</RouterLink>
            <RouterLink to="/legal/cgv">Conditions de vente</RouterLink>
          </nav>
        </div>

        <!-- INFORMATIONS & LÉGAL -->
        <div class="site-footer__column">
          <h3>Informations</h3>
          <nav aria-label="Informations légales">
            <RouterLink to="/legal/mentions-legales">Mentions légales</RouterLink>
            <RouterLink to="/legal/confidentialite">Confidentialité & RGPD</RouterLink>
            <RouterLink to="/legal/cookies">Gestion des cookies</RouterLink>
            <RouterLink to="/legal/retours">Droit de rétractation</RouterLink>
          </nav>
        </div>
      </div>

      <!-- SÉPARATEUR -->
      <div class="site-footer__separator"></div>

      <!-- RÉASSURANCE EN PIED DE PAGE -->
      <div class="site-footer__info">
        <div class="site-footer__shipping">
          <span class="site-footer__info-title">Livraison en Europe</span>
          <span>Expédition rapide et suivie. Offerte dès 40 € d'achat.</span>
        </div>

        <div class="site-footer__payments">
          <span class="site-footer__info-title">Paiement sécurisé</span>
          <div class="site-footer__payment-list">
            <span>CB</span>
            <span>VISA</span>
            <span>Mastercard</span>
            <span>American Express</span>
            <span>Stripe</span>
          </div>
        </div>
      </div>

      <!-- COPYRIGHT -->
      <div class="site-footer__bottom">
        <p>© {{ new Date().getFullYear() }} SIDRA. Tous droits réservés.</p>
        <nav aria-label="Liens réglementaires">
          <RouterLink to="/legal/mentions-legales">Mentions légales</RouterLink>
          <RouterLink to="/legal/confidentialite">Données personnelles</RouterLink>
          <RouterLink to="/legal/cgv">CGV</RouterLink>
        </nav>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  margin-top: clamp(48px, 6vw, 96px);
  background: #111111;
  color: #ffffff;
  font-family: var(--font-body, "Jost", sans-serif);
}

/* =========================================================
   NEWSLETTER
   ========================================================= */

.site-footer__newsletter {
  padding-block: clamp(48px, 6vw, 80px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  background: #161616;
}

.site-footer__newsletter-inner {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  gap: clamp(32px, 5vw, 64px);
}

.site-footer__newsletter-eyebrow {
  display: block;
  font-size: 0.68rem;
  font-weight: 600;
  letter-spacing: 0.2em;
  color: #d8b06d;
  margin-bottom: 10px;
}

.site-footer__newsletter-content h2 {
  margin: 0 0 12px;
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(1.6rem, 2.5vw, 2.4rem);
  font-weight: 400;
  letter-spacing: -0.025em;
  line-height: 1.2;
}

.site-footer__newsletter-content p {
  margin: 0;
  font-size: var(--text-sm, 0.88rem);
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
  max-width: 48ch;
}

.site-footer__newsletter-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.site-footer__input-wrapper {
  display: flex;
  gap: 0;
  width: 100%;
}

.site-footer__input-wrapper input {
  flex: 1;
  height: 48px;
  padding: 0 16px;
  background: #202020;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-right: none;
  border-radius: 0;
  color: #ffffff;
  font-family: inherit;
  font-size: 0.9rem;
}

.site-footer__input-wrapper input:focus {
  outline: none;
  border-color: #ffffff;
  background: #282828;
}

.site-footer__input-wrapper button {
  height: 48px;
  padding: 0 24px;
  background: #ffffff;
  color: #000000;
  border: 1px solid #ffffff;
  border-radius: 0;
  font-family: inherit;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  cursor: pointer;
  transition: all 0.2s ease;
}

.site-footer__input-wrapper button:hover {
  background: #d8b06d;
  border-color: #d8b06d;
  color: #000000;
}

.site-footer__success {
  margin: 4px 0 0;
  font-size: 0.82rem;
  color: #86efac;
}

/* =========================================================
   CORPS DU FOOTER
   ========================================================= */

.site-footer__main {
  padding-block: clamp(48px, 6vw, 72px) 36px;
}

.site-footer__top {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: clamp(28px, 4vw, 56px);
}

.site-footer__logo {
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: 28px;
  font-weight: 500;
  letter-spacing: 0.16em;
  color: #ffffff;
  text-decoration: none;
  display: inline-block;
  margin-bottom: 16px;
}

.site-footer__brand-description {
  margin: 0;
  font-size: 0.86rem;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.65);
  max-width: 36ch;
}

.site-footer__column h3 {
  margin: 0 0 20px;
  font-family: var(--font-body, "Jost", sans-serif);
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #ffffff;
}

.site-footer__column nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.site-footer__column a {
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  font-size: 0.86rem;
  transition: color 0.15s ease;
}

.site-footer__column a:hover {
  color: #ffffff;
}

.site-footer__separator {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin-block: 44px 32px;
}

/* =========================================================
   INFORMATIONS & PAIEMENTS
   ========================================================= */

.site-footer__info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 24px;
  padding-bottom: 28px;
}

.site-footer__info-title {
  display: block;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #ffffff;
  margin-bottom: 6px;
}

.site-footer__shipping span:last-child {
  font-size: 0.82rem;
  color: rgba(255, 255, 255, 0.65);
}

.site-footer__payment-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.site-footer__payment-list span {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  font-size: 0.72rem;
  font-weight: 500;
  letter-spacing: 0.06em;
  color: rgba(255, 255, 255, 0.85);
}

/* =========================================================
   BAS DU FOOTER
   ========================================================= */

.site-footer__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 0.78rem;
  color: rgba(255, 255, 255, 0.5);
}

.site-footer__bottom p {
  margin: 0;
}

.site-footer__bottom nav {
  display: flex;
  gap: 20px;
}

.site-footer__bottom a {
  color: inherit;
  text-decoration: none;
  transition: color 0.15s ease;
}

.site-footer__bottom a:hover {
  color: #ffffff;
}

/* =========================================================
   RESPONSIVE
   ========================================================= */

@media (max-width: 900px) {
  .site-footer__newsletter-inner {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .site-footer__top {
    grid-template-columns: 1fr 1fr;
    gap: 36px 24px;
  }

  .site-footer__brand-column {
    grid-column: span 2;
  }
}

@media (max-width: 560px) {
  .site-footer__top {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .site-footer__brand-column {
    grid-column: span 1;
  }

  .site-footer__input-wrapper {
    flex-direction: column;
  }

  .site-footer__input-wrapper input {
    border-right: 1px solid rgba(255, 255, 255, 0.2);
  }

  .site-footer__info {
    flex-direction: column;
    align-items: flex-start;
  }

  .site-footer__bottom {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
