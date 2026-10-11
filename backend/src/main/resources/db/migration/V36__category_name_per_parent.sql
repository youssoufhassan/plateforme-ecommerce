-- Le nom n'a plus à être unique globalement : « Parfums » doit pouvoir
-- exister sous Homme et sous Femme. L'unicité porte sur le chemin.
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_name_key;
ALTER TABLE categories DROP CONSTRAINT IF EXISTS uq_categories_name;

-- En revanche, deux sœurs ne peuvent pas porter le même slug.
-- COALESCE traite les racines comme un même groupe.
CREATE UNIQUE INDEX uq_categories_parent_slug
    ON categories (COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'::uuid), slug);