CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    api_url VARCHAR(500),
    contact_email VARCHAR(150)
);

ALTER TABLE products ADD COLUMN brand VARCHAR(100);
ALTER TABLE products ADD COLUMN fulfillment_type VARCHAR(20) NOT NULL DEFAULT 'OWN_STOCK';
ALTER TABLE products ADD COLUMN supplier_id UUID REFERENCES suppliers(id);
ALTER TABLE products ADD COLUMN supplier_sku VARCHAR(150);
ALTER TABLE products ADD COLUMN cost_price DECIMAL(10,2);

CREATE INDEX idx_products_supplier_sku ON products(supplier_sku);