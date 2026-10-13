-- Le genre est une facette, pas une branche : un parfum unisexe
-- doit apparaître côté homme ET côté femme sans être dupliqué.
ALTER TABLE products ADD COLUMN gender VARCHAR(20);

CREATE INDEX idx_products_gender ON products(gender) WHERE gender IS NOT NULL;

-- Reprise : les produits rangés sous homme/ ou femme/ héritent du genre
UPDATE products p SET gender = 'HOMME'
FROM product_categories pc, categories c
WHERE pc.product_id = p.id AND pc.category_id = c.id AND c.path LIKE 'homme%';

UPDATE products p SET gender = 'FEMME'
FROM product_categories pc, categories c
WHERE pc.product_id = p.id AND pc.category_id = c.id AND c.path LIKE 'femme%';