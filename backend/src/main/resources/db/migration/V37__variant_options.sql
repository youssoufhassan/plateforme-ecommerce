-- Les axes de variation d'un produit : « Contenance », ou « Taille » et « Couleur »
CREATE TABLE product_options (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    position   INT NOT NULL DEFAULT 0,
    UNIQUE (product_id, name)
);

CREATE INDEX idx_product_options_product ON product_options(product_id, position);

-- Les valeurs de chaque axe : « 100 ml », ou « M » et « Noir »
CREATE TABLE product_option_values (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    option_id UUID NOT NULL REFERENCES product_options(id) ON DELETE CASCADE,
    value     VARCHAR(100) NOT NULL,
    position  INT NOT NULL DEFAULT 0,
    UNIQUE (option_id, value)
);

CREATE INDEX idx_option_values_option ON product_option_values(option_id, position);

-- Une variante est une combinaison : M × Noir
CREATE TABLE variant_option_values (
    variant_id      UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    option_value_id UUID NOT NULL REFERENCES product_option_values(id) ON DELETE CASCADE,
    PRIMARY KEY (variant_id, option_value_id)
);

CREATE INDEX idx_variant_values_variant ON variant_option_values(variant_id);

-- Photos propres à une couleur
ALTER TABLE product_images ADD COLUMN variant_id UUID REFERENCES product_variants(id) ON DELETE SET NULL;

-- Reprise de l'existant : chaque produit reçoit un axe « Contenance »
-- dont les valeurs sont les libellés de ses variantes actuelles
INSERT INTO product_options (product_id, name, position)
SELECT DISTINCT product_id, 'Contenance', 0 FROM product_variants;

INSERT INTO product_option_values (option_id, value, position)
SELECT DISTINCT o.id, v.label, v.position
FROM product_variants v
JOIN product_options o ON o.product_id = v.product_id AND o.name = 'Contenance';

INSERT INTO variant_option_values (variant_id, option_value_id)
SELECT v.id, ov.id
FROM product_variants v
JOIN product_options o ON o.product_id = v.product_id AND o.name = 'Contenance'
JOIN product_option_values ov ON ov.option_id = o.id AND ov.value = v.label;