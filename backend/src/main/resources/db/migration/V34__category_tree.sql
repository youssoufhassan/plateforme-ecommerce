-- Translitération simple : suffisante pour des noms de catégories
CREATE OR REPLACE FUNCTION slugify_fr(input TEXT) RETURNS TEXT AS $$
    SELECT regexp_replace(
        regexp_replace(
            lower(translate($1,
                'àâäáãåçéèêëíìîïñóòôöõúùûüýÿÀÂÄÁÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝ',
                'aaaaaaceeeeiiiinooooouuuuyyAAAAAACEEEEIIIINOOOOOUUUUY')),
            '[^a-z0-9]+', '-', 'g'),
        '(^-|-$)', '', 'g');
$$ LANGUAGE SQL IMMUTABLE;

ALTER TABLE categories
    ADD COLUMN parent_id UUID REFERENCES categories(id) ON DELETE RESTRICT,
    ADD COLUMN slug      VARCHAR(80),
    ADD COLUMN path      VARCHAR(500),
    ADD COLUMN position  INT     NOT NULL DEFAULT 0,
    ADD COLUMN active    BOOLEAN NOT NULL DEFAULT TRUE;

-- Les catégories existantes deviennent des racines
UPDATE categories SET slug = slugify_fr(name), path = slugify_fr(name);

ALTER TABLE categories
    ALTER COLUMN slug SET NOT NULL,
    ALTER COLUMN path SET NOT NULL,
    ADD CONSTRAINT uq_categories_path UNIQUE (path);

CREATE INDEX idx_categories_parent ON categories(parent_id, position);

-- varchar_pattern_ops accélère les recherches par préfixe : path LIKE 'homme/%'
CREATE INDEX idx_categories_path ON categories(path varchar_pattern_ops);