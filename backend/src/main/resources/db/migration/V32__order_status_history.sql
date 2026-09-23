CREATE TABLE order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    previous_status VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    changed_by VARCHAR(150) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_status_history_order ON order_status_history(order_id, created_at);

-- Les commandes existantes reçoivent une entrée correspondant à leur statut actuel
INSERT INTO order_status_history (order_id, previous_status, status, changed_by, note, created_at)
SELECT id, NULL, status, 'SYSTEM', 'Historique initialisé', created_at FROM orders;