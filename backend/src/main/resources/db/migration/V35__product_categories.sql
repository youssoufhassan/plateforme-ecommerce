-- Un produit peut vivre dans plusieurs branches :
-- un parfum unisexe est à la fois sous Homme et sous Femme, sans duplication
CREATE TABLE product_categories (
    product_id  UUID NOT NULL REFERENCES products(id)   ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, category_id)
);

CREATE INDEX idx_product_categories_category ON product_categories(category_id);

-- Reprise de l'existant : la catégorie actuelle devient le premier rattachement
INSERT INTO product_categories (product_id, category_id)
SELECT id, category_id FROM products WHERE category_id IS NOT NULL;