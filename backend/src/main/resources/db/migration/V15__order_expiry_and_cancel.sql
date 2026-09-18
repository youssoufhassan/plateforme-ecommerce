ALTER TABLE orders ADD COLUMN expires_at TIMESTAMP;

CREATE INDEX idx_orders_status_expires ON orders(status, expires_at);