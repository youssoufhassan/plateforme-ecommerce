CREATE TABLE home_sections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(80) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    subtitle VARCHAR(255),
    position INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE home_section_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id UUID NOT NULL REFERENCES home_sections(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    position INT NOT NULL DEFAULT 0,
    UNIQUE (section_id, product_id)
);

CREATE INDEX idx_section_products ON home_section_products(section_id, position);

-- Deux sections d'exemple, vides et inactives : à compléter depuis le back-office
INSERT INTO home_sections (slug, title, subtitle, position, active) VALUES
    ('niche', 'Parfums niche', 'Des créations rares et singulières', 1, FALSE),
    ('epices', 'Parfums épicés', 'Chaleur et caractère', 2, FALSE);