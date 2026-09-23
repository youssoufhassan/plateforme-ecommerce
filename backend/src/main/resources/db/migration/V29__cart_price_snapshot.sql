ALTER TABLE cart_items ADD COLUMN price_at_add DECIMAL(10,2);

UPDATE cart_items ci SET price_at_add = v.price
FROM product_variants v WHERE v.id = ci.variant_id AND ci.price_at_add IS NULL;