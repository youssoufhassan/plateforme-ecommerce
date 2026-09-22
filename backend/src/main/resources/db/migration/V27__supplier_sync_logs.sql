CREATE TABLE supplier_sync_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_key VARCHAR(50) NOT NULL,
    operation VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_type VARCHAR(30),
    message VARCHAR(500),
    items_processed INT NOT NULL DEFAULT 0,
    items_skipped INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP
);

CREATE INDEX idx_sync_logs_supplier ON supplier_sync_logs(supplier_key, started_at);