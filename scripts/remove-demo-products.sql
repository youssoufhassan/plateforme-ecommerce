-- SIDRA — supprimer les produits de démonstration de la base LOCALE.
-- À lancer uniquement si aucun panier ni aucune commande ne référence ces produits.
-- Cette commande est volontairement séparée du seed.

BEGIN;

DELETE FROM products
WHERE position('[SIDRA_DEMO:' in COALESCE(description, '')) > 0;

COMMIT;
