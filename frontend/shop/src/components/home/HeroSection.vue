<template>
  <section class="hero">
    <div class="hero__campaign">
      <img src="/hero.jpg" alt="Campagne parfum SIDRA" class="hero__image" />

      <div class="hero__overlay"></div>

      <div class="hero__content">
        <p class="hero__eyebrow">SIDRA COLLECTION</p>

        <h1 class="hero__title">
          Une signature<br />
          élégante et durable.
        </h1>

        <p class="hero__price">80,99 €</p>

        <p class="hero__description">
          Une fragrance pensée pour laisser une signature élégante et durable.
        </p>

        <RouterLink to="/produits" class="hero__button"> Découvrir </RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  width: 100%;
  background: var(--color-background);
}

/* =========================================================
   CAMPAGNE
   ========================================================= */

.hero__campaign {
  position: relative;
  width: 100%;
  height: clamp(360px, 32vw, 430px);
  overflow: hidden;
  background: #252525;
}

/* =========================================================
   IMAGE
   ========================================================= */

.hero__image {
  position: absolute;
  inset: 0;

  width: 100%;
  height: 100%;

  display: block;

  object-fit: cover;
  object-position: center;
}

/* =========================================================
   OVERLAY
   ========================================================= */

.hero__overlay {
  position: absolute;
  inset: 0;

  background: linear-gradient(
    90deg,
    rgba(0, 0, 0, 0.48) 0%,
    rgba(0, 0, 0, 0.18) 55%,
    rgba(0, 0, 0, 0.05) 100%
  );
}

/* =========================================================
   CONTENU
   ========================================================= */

.hero__content {
  position: absolute;
  inset: 0;
  z-index: 2;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;

  width: min(420px, 45%);

  padding: 32px 48px;

  color: var(--color-white);
}

.hero__eyebrow {
  margin: 0 0 10px;

  font-size: 10px;
  font-weight: 500;
  line-height: 1.2;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.hero__title {
  margin: 0;

  font-size: clamp(26px, 3vw, 40px);
  font-weight: 400;
  line-height: 1.08;
  letter-spacing: -0.015em;
}

.hero__price {
  margin: 14px 0 0;

  font-size: 18px;
  font-weight: 500;
  line-height: 1.3;
}

.hero__description {
  max-width: 330px;

  margin: 8px 0 0;

  font-size: 13px;
  line-height: 1.5;
}

.hero__button {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  min-height: 40px;

  margin-top: 18px;
  padding: 0 24px;

  background: var(--color-white);
  color: var(--color-black);

  font-size: 10px;
  font-weight: 500;
  line-height: 1;

  letter-spacing: 0.12em;
  text-transform: uppercase;

  transition:
    background var(--transition-base),
    color var(--transition-base),
    transform var(--transition-base);
}

.hero__button:hover {
  background: var(--color-black);
  color: var(--color-white);
  transform: translateY(-1px);
}

/* =========================================================
   TABLET
   ========================================================= */

@media (max-width: 900px) {
  .hero__campaign {
    height: 320px;
  }

  .hero__content {
    width: 50%;
    padding: 28px 32px;
  }

  .hero__title {
    font-size: 30px;
  }
}

/* =========================================================
   MOBILE
   ========================================================= */

@media (max-width: 767px) {
  .hero__campaign {
    height: 250px;
  }

  .hero__image {
    object-position: center;
  }

  .hero__overlay {
    background: linear-gradient(
      90deg,
      rgba(0, 0, 0, 0.58) 0%,
      rgba(0, 0, 0, 0.25) 65%,
      rgba(0, 0, 0, 0.05) 100%
    );
  }

  .hero__content {
    width: 72%;
    padding: 24px;
  }

  .hero__eyebrow {
    margin-bottom: 7px;

    font-size: 8px;
    letter-spacing: 0.18em;
  }

  .hero__title {
    font-size: 23px;
    line-height: 1.08;
  }

  .hero__price {
    margin-top: 8px;
    font-size: 15px;
  }

  .hero__description {
    max-width: 240px;

    margin-top: 6px;

    font-size: 10px;
    line-height: 1.45;
  }

  .hero__button {
    min-height: 34px;

    margin-top: 12px;
    padding: 0 18px;

    font-size: 8px;
  }
}

/* =========================================================
   PETITS MOBILES
   ========================================================= */

@media (max-width: 380px) {
  .hero__campaign {
    height: 240px;
  }

  .hero__content {
    width: 78%;
    padding: 20px;
  }

  .hero__title {
    font-size: 21px;
  }

  .hero__description {
    max-width: 220px;
  }
}
</style>
