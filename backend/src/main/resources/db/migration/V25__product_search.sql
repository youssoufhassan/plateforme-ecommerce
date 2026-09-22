-- Nécessaire pour le tri "nouveautés"
ALTER TABLE products ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Index pour les filtres et tris fréquents
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_created ON products(created_at);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_orders_status_created ON orders(status, created_at);