CREATE TABLE legal_pages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO legal_pages (slug, title, content) VALUES
    ('mentions-legales', 'Mentions légales', 'Contenu à rédiger.'),
    ('cgv', 'Conditions générales de vente', 'Contenu à rédiger.'),
    ('confidentialite', 'Politique de confidentialité', 'Contenu à rédiger.'),
    ('cookies', 'Politique de cookies', 'Contenu à rédiger.'),
    ('retours', 'Retours et droit de rétractation', 'Contenu à rédiger.');

ALTER TABLE users ADD COLUMN marketing_consent BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN marketing_consent_at TIMESTAMP;
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;

ALTER TABLE orders ADD COLUMN terms_accepted_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN terms_version INT;