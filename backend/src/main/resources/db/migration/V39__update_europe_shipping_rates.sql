
-- Tarif unique pour les zones de livraison européennes existantes.
UPDATE shipping_zones
SET flat_rate = 5.90,
    free_threshold = 60.00
WHERE name IN ('France', 'Union Européenne');
