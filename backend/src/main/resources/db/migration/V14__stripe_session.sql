ALTER TABLE payments ADD COLUMN stripe_session_id VARCHAR(255);
CREATE INDEX idx_payments_stripe_session ON payments(stripe_session_id);