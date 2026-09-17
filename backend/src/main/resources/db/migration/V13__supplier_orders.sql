CREATE TABLE supplier_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    status VARCHAR(30) NOT NULL DEFAULT 'TO_SEND',
    external_reference VARCHAR(150),
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (order_id, supplier_id)
);

CREATE TABLE supplier_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_order_id UUID NOT NULL REFERENCES supplier_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    supplier_sku VARCHAR(150),
    quantity INT NOT NULL,
    unit_cost DECIMAL(10,2)
);

CREATE INDEX idx_supplier_orders_status ON supplier_orders(status);