CREATE TABLE product_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    label VARCHAR(100) NOT NULL,
    sku VARCHAR(100) UNIQUE,
    supplier_sku VARCHAR(150),
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    cost_price DECIMAL(10,2),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    position INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_variants_product ON product_variants(product_id);

-- Une variante "Standard" par produit existant, reprenant son prix et son stock
INSERT INTO product_variants (product_id, label, supplier_sku, price, cost_price, stock_quantity, active, position)
SELECT id, 'Standard', supplier_sku, price, cost_price, stock_quantity, COALESCE(active, TRUE), 0
FROM products;

-- Le panier et les commandes pourront référencer une variante (rempli pour l'existant)
ALTER TABLE cart_items ADD COLUMN variant_id UUID REFERENCES product_variants(id) ON DELETE CASCADE;
ALTER TABLE order_items ADD COLUMN variant_id UUID REFERENCES product_variants(id) ON DELETE RESTRICT;
ALTER TABLE order_items ADD COLUMN variant_label VARCHAR(100);

UPDATE cart_items ci SET variant_id = v.id
FROM product_variants v WHERE v.product_id = ci.product_id;

UPDATE order_items oi SET variant_id = v.id, variant_label = v.label
FROM product_variants v WHERE v.product_id = oi.product_id;

-- Deux tailles d'un même parfum doivent pouvoir coexister dans un panier
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS cart_items_cart_id_product_id_key;