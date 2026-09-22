-- Tout le code renseigne désormais la variante : on la rend obligatoire
ALTER TABLE cart_items ALTER COLUMN variant_id SET NOT NULL;
ALTER TABLE order_items ALTER COLUMN variant_id SET NOT NULL;

COMMENT ON COLUMN products.price IS 'Obsolète : synchronisé avec le prix le plus bas des variantes';
COMMENT ON COLUMN products.stock_quantity IS 'Obsolète : synchronisé avec la somme des stocks des variantes';