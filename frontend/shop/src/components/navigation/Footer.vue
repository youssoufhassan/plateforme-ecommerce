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
    <!-- NEWSLETTER -->
    <section class="site-footer__newsletter">
      <div class="site-footer__newsletter-inner">
        <div class="site-footer__newsletter-content">
          <h2>Entrez dans l'univers SIDRA.</h2>

          <p>
            Recevez nos nouveautés, nos sélections et nos inspirations parfumées
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

          <p v-if="subscribed" class="site-footer__success">
            Merci. Vous êtes maintenant inscrit à la newsletter SIDRA.
          </p>
        </form>
      </div>
    </section>

    <!-- FOOTER PRINCIPAL -->
    <div class="site-footer__main">
      <div class="site-footer__top">
        <!-- MARQUE -->
        <div class="site-footer__brand-column">
          <RouterLink
            to="/"
            class="site-footer__logo"
            aria-label="SIDRA - Accueil"
          >
            SIDRA
          </RouterLink>

          <p class="site-footer__brand-description">
            Une sélection de parfums et de produits de beauté pensée pour celles
            et ceux qui recherchent une signature différente.
          </p>
        </div>

        <!-- BOUTIQUE -->
        <div class="site-footer__column">
          <h3>Boutique</h3>

          <nav>
            <RouterLink to="/produits"> Tous les produits </RouterLink>

            <RouterLink to="/produits?category=homme"> Homme </RouterLink>

            <RouterLink to="/produits?category=femme"> Femme </RouterLink>

            <RouterLink to="/produits?category=unisex"> Unisexe </RouterLink>

            <RouterLink to="/produits?sort=newest"> Nouveautés </RouterLink>
          </nav>
        </div>

        <!-- SERVICE -->
        <div class="site-footer__column">
          <h3>Service</h3>

          <nav>
            <RouterLink to="/help"> Aide & contact </RouterLink>

            <RouterLink to="/stores"> Livraison & retours </RouterLink>

            <RouterLink to="/account/orders"> Suivre ma commande </RouterLink>

            <RouterLink to="/account/favorites"> Mes favoris </RouterLink>
          </nav>
        </div>

        <!-- SIDRA -->
        <div class="site-footer__column">
          <h3>SIDRA</h3>

          <nav>
            <RouterLink to="/about"> Notre histoire </RouterLink>

            <RouterLink to="/contact"> Nous contacter </RouterLink>

            <RouterLink to="/faq"> FAQ </RouterLink>
          </nav>
        </div>
      </div>

      <!-- SEPARATION -->
      <div class="site-footer__separator"></div>

      <!-- INFORMATIONS -->
      <div class="site-footer__info">
        <div class="site-footer__shipping">
          <span class="site-footer__info-title"> Livraison en Europe </span>

          <span> Expédition suivie selon destination </span>
        </div>

        <div class="site-footer__payments">
          <span class="site-footer__info-title"> Paiement sécurisé </span>

          <div class="site-footer__payment-list">
            <span>VISA</span>
            <span>Mastercard</span>
            <span>AMEX</span>
            <span>STRIPE</span>
          </div>
        </div>
      </div>

      <!-- BAS DU FOOTER -->
      <div class="site-footer__bottom">
        <p>© {{ new Date().getFullYear() }} SIDRA. Tous droits réservés.</p>

        <nav>
          <RouterLink to="/legal"> Mentions légales </RouterLink>

          <RouterLink to="/privacy"> Confidentialité </RouterLink>

          <RouterLink to="/terms"> CGV </RouterLink>
        </nav>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  margin-top: 36px;
  background: #111111;
  color: #ffffff;
}

/* =========================================================
   NEWSLETTER
   ========================================================= */

.site-footer__newsletter {
  border-bottom: 1px solid rgba(255, 255, 255, 0.16);
}

.site-footer__newsletter-inner {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 500px);
  align-items: end;
  gap: 60px;

  width: min(calc(100% - (var(--container-padding) * 2)), var(--container-max));

  margin-inline: auto;
  padding-block: 48px;
}

.site-footer__newsletter-content h2 {
  max-width: 650px;

  margin: 0;

  font-family: var(--font-display);
  font-size: clamp(30px, 3.5vw, 48px);
  font-weight: 400;
  line-height: 1.05;
  letter-spacing: -0.035em;
}

.site-footer__newsletter-content p {
  max-width: 540px;
  margin: 14px 0 0;

  color: rgba(255, 255, 255, 0.62);

  font-size: 13px;
  line-height: 1.6;
}

/* =========================================================
   NEWSLETTER FORM
   ========================================================= */

.site-footer__newsletter-form {
  width: 100%;
}

.site-footer__input-wrapper {
  display: flex;
  width: 100%;

  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.site-footer__input-wrapper input {
  min-width: 0;
  flex: 1;

  height: 46px;
  padding: 0;

  border: 0;

  background: transparent;
  color: #ffffff;

  font-size: 13px;
}

.site-footer__input-wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.45);
}

.site-footer__input-wrapper input:focus {
  outline: none;
}

.site-footer__input-wrapper button {
  flex-shrink: 0;

  padding: 0 0 0 18px;

  color: #ffffff;

  font-size: 10px;
  font-weight: 600;

  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.site-footer__success {
  margin-top: 10px;

  color: rgba(255, 255, 255, 0.7);

  font-size: 10px;
}

/* =========================================================
   MAIN
   ========================================================= */

.site-footer__main {
  width: min(calc(100% - (var(--container-padding) * 2)), var(--container-max));

  margin-inline: auto;
  padding-block: 48px 22px;
}

.site-footer__top {
  display: grid;

  grid-template-columns:
    2fr
    1fr
    1fr
    1fr;

  gap: 40px;
}

/* =========================================================
   BRAND
   ========================================================= */

.site-footer__logo {
  display: inline-block;

  color: #ffffff;

  font-family: var(--font-display);

  font-size: 26px;
  font-weight: 600;

  line-height: 1;
  letter-spacing: 0.2em;
}

.site-footer__brand-description {
  max-width: 330px;
  margin-top: 18px;

  color: rgba(255, 255, 255, 0.52);

  font-size: 12px;
  line-height: 1.7;
}

/* =========================================================
   COLUMNS
   ========================================================= */

.site-footer__column h3 {
  margin: 0 0 16px;

  color: #ffffff;

  font-size: 10px;
  font-weight: 600;

  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.site-footer__column nav {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.site-footer__column nav a {
  color: rgba(255, 255, 255, 0.58);

  font-size: 12px;

  transition:
    color var(--transition-fast),
    padding-left var(--transition-fast);
}

.site-footer__column nav a:hover {
  padding-left: 4px;
  color: #ffffff;
}

/* =========================================================
   SEPARATOR
   ========================================================= */

.site-footer__separator {
  width: 100%;
  height: 1px;

  margin-top: 46px;

  background: rgba(255, 255, 255, 0.16);
}

/* =========================================================
   INFORMATIONS
   ========================================================= */

.site-footer__info {
  display: flex;

  align-items: center;
  justify-content: space-between;

  gap: 30px;

  padding-block: 22px;
}

.site-footer__shipping,
.site-footer__payments {
  display: flex;

  align-items: center;

  gap: 10px;

  color: rgba(255, 255, 255, 0.45);

  font-size: 10px;
}

.site-footer__info-title {
  color: rgba(255, 255, 255, 0.8);

  font-weight: 600;
  letter-spacing: 0.07em;
  text-transform: uppercase;
}

.site-footer__payment-list {
  display: flex;
  align-items: center;
  gap: 6px;
}

.site-footer__payment-list span {
  padding: 4px 7px;

  border: 1px solid rgba(255, 255, 255, 0.18);

  color: rgba(255, 255, 255, 0.55);

  font-size: 7px;
  letter-spacing: 0.04em;
}

/* =========================================================
   BOTTOM
   ========================================================= */

.site-footer__bottom {
  display: flex;

  align-items: center;
  justify-content: space-between;

  gap: 24px;

  padding-top: 16px;

  border-top: 1px solid rgba(255, 255, 255, 0.08);

  color: rgba(255, 255, 255, 0.35);

  font-size: 9px;
}

.site-footer__bottom nav {
  display: flex;
  align-items: center;
  gap: 20px;
}

.site-footer__bottom nav a {
  transition: color var(--transition-fast);
}

.site-footer__bottom nav a:hover {
  color: #ffffff;
}

/* =========================================================
   TABLET
   ========================================================= */

@media (max-width: 900px) {
  .site-footer__newsletter-inner {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .site-footer__top {
    grid-template-columns:
      1.5fr
      1fr
      1fr;
  }

  .site-footer__brand-column {
    grid-column: 1 / -1;
  }

  .site-footer__info {
    align-items: flex-start;
    flex-direction: column;
    gap: 18px;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 600px) {
  .site-footer {
    margin-top: 28px;
  }

  .site-footer__newsletter-inner {
    width: calc(100% - 40px);
    padding-block: 38px;
  }

  .site-footer__newsletter-content h2 {
    font-size: 32px;
  }

  .site-footer__newsletter-content p {
    font-size: 12px;
  }

  .site-footer__main {
    width: calc(100% - 40px);
    padding-block: 38px 20px;
  }

  .site-footer__top {
    grid-template-columns: 1fr 1fr;
    gap: 32px 20px;
  }

  .site-footer__brand-column {
    grid-column: 1 / -1;
  }

  .site-footer__brand-description {
    max-width: 100%;
  }

  .site-footer__separator {
    margin-top: 36px;
  }

  .site-footer__info {
    gap: 20px;
    padding-block: 20px;
  }

  .site-footer__shipping,
  .site-footer__payments {
    align-items: flex-start;
    flex-direction: column;
    gap: 7px;
  }

  .site-footer__bottom {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .site-footer__bottom nav {
    flex-wrap: wrap;
    gap: 10px 18px;
  }
}
</style>
