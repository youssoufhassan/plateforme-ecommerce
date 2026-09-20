CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id) ON DELETE RESTRICT,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    sequence_number BIGINT NOT NULL UNIQUE,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Montants figés au moment de l'émission
    subtotal_amount DECIMAL(10,2) NOT NULL,
    shipping_amount DECIMAL(10,2) NOT NULL,
    vat_amount DECIMAL(10,2) NOT NULL,
    vat_rate DECIMAL(5,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,

    -- Identité du client figée
    customer_name VARCHAR(200) NOT NULL,
    customer_email VARCHAR(150) NOT NULL,
    billing_address TEXT NOT NULL
);

CREATE SEQUENCE invoice_sequence START WITH 1 INCREMENT BY 1;

CREATE INDEX idx_invoices_order ON invoices(order_id);