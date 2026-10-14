<template>
  <section class="hero" aria-label="Campagne principale SIDRA">
    <div class="hero__campaign">
      <img
        src="/hero.jpg"
        alt="Collection SIDRA — Mode, Beauté & Accessoires"
        class="hero__image"
        loading="eager"
        fetchpriority="high"
      />

      <div class="hero__overlay" aria-hidden="true"></div>

      <div class="hero__content">
        <span class="hero__eyebrow">SIDRA MAISON & STYLE</span>

        <h1 class="hero__title">
          L'élégance contemporaine,<br />
          tous univers confondus.
        </h1>

        <p class="hero__description">
          Mode masculine et féminine, haute parfumerie, maroquinerie et art de vivre :
          explorez les collections d'une grande enseigne contemporaine.
        </p>

        <div class="hero__actions">
          <RouterLink to="/produits" class="hero__button hero__button--primary">
            Explorer le catalogue
          </RouterLink>

          <RouterLink to="/produits?sort=newest" class="hero__button hero__button--outline">
            Nouveautés
          </RouterLink>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  width: 100%;
  background: var(--color-background, #f2f2f1);
}

.hero__campaign {
  position: relative;
  width: 100%;
  min-height: clamp(420px, 46vw, 620px);
  overflow: hidden;
  background: #141414;
  display: flex;
  align-items: center;
}

.hero__image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  object-position: center 38%;
}

.hero__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgba(10, 10, 10, 0.78) 0%,
    rgba(10, 10, 10, 0.52) 48%,
    rgba(10, 10, 10, 0.22) 100%
  );
}

.hero__content {
  position: relative;
  z-index: 2;
  width: min(640px, 100%);
  padding: clamp(36px, 5.5vw, 72px) clamp(24px, 4.5vw, 64px);
  color: #ffffff;
}

.hero__eyebrow {
  display: inline-block;
  margin: 0 0 14px;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #d8b06d;
}

.hero__title {
  margin: 0;
  font-family: var(--font-display, "Bodoni Moda", serif);
  font-size: clamp(2rem, 3.8vw, 3.5rem);
  font-weight: 400;
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: #ffffff;
}

.hero__description {
  max-width: 480px;
  margin: 16px 0 0;
  font-family: var(--font-body, "Jost", sans-serif);
  font-size: clamp(0.92rem, 1.2vw, 1.05rem);
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.86);
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  margin-top: 26px;
}

.hero__button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  padding: 0 28px;
  font-family: var(--font-body, "Jost", sans-serif);
  font-size: 0.82rem;
  font-weight: 500;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  text-decoration: none;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    border-color 0.2s ease,
    transform 0.2s ease;
}

.hero__button--primary {
  background: #ffffff;
  color: #000000;
  border: 1px solid #ffffff;
}

.hero__button--primary:hover {
  background: #000000;
  color: #ffffff;
  border-color: #000000;
  transform: translateY(-2px);
}

.hero__button--outline {
  background: transparent;
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.hero__button--outline:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: #ffffff;
  transform: translateY(-2px);
}

@media (max-width: 767px) {
  .hero__campaign {
    min-height: 380px;
  }

  .hero__overlay {
    background: linear-gradient(
      180deg,
      rgba(10, 10, 10, 0.45) 0%,
      rgba(10, 10, 10, 0.78) 60%,
      rgba(10, 10, 10, 0.92) 100%
    );
  }

  .hero__content {
    padding: 32px 20px;
  }

  .hero__eyebrow {
    font-size: 0.65rem;
    margin-bottom: 10px;
  }

  .hero__title {
    font-size: 1.85rem;
  }

  .hero__description {
    font-size: 0.88rem;
    margin-top: 12px;
  }

  .hero__actions {
    gap: 10px;
    margin-top: 20px;
  }

  .hero__button {
    height: 42px;
    padding: 0 20px;
    font-size: 0.75rem;
  }
}
</style>
