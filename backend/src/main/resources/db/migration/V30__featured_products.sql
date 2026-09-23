ALTER TABLE products ADD COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_products_featured ON products(featured) WHERE featured = TRUE;