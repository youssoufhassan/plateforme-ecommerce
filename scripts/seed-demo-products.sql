-- SIDRA — catalogue de démonstration LOCAL uniquement.
-- Ne pas exécuter sur une base de production.
-- Crée 3 produits fictifs par catégorie active, avec 3 variantes par produit.
-- Les URLs d'images servent uniquement à la prévisualisation et doivent être
-- remplacées par des visuels dont SIDRA détient les droits avant mise en vente.
-- Script idempotent : les marqueurs [SIDRA_DEMO:path:index] évitent les doublons.

BEGIN;

DO $seed$
DECLARE
    category_row RECORD;
    product_index INTEGER;
    variant_index INTEGER;
    product_id UUID;
    option_id UUID;
    option_value_id UUID;
    variant_id UUID;
    marker TEXT;
    product_name TEXT;
    product_description TEXT;
    product_type TEXT;
    option_name TEXT;
    option_values TEXT[];
    image_candidates TEXT[];
    selected_image TEXT;
    base_price NUMERIC(10,2);
    gender_value VARCHAR(20);
BEGIN
    IF to_regclass('public.categories') IS NULL
       OR to_regclass('public.products') IS NULL
       OR to_regclass('public.product_variants') IS NULL
       OR to_regclass('public.product_categories') IS NULL
       OR to_regclass('public.product_options') IS NULL
       OR to_regclass('public.product_option_values') IS NULL
       OR to_regclass('public.variant_option_values') IS NULL THEN
        RAISE EXCEPTION 'Le schéma catalogue SIDRA est incomplet. Vérifie que les migrations ont été appliquées.';
    END IF;

    FOR category_row IN
        SELECT id, name, path, parent_id
        FROM categories
        WHERE active IS TRUE
        ORDER BY path
    LOOP
        FOR product_index IN 1..3 LOOP
            marker := '[SIDRA_DEMO:' || category_row.path || ':' || product_index::TEXT || ']';

            -- Permet de relancer le script sans recréer les produits existants.
            IF EXISTS (
                SELECT 1
                FROM products p
                WHERE strpos(COALESCE(p.description, ''), marker) > 0
            ) THEN
                CONTINUE;
            END IF;

            -- Nom descriptif pour visualiser les cartes, sans fausse marque fournisseur.
            IF category_row.path ~* '(parfum|fragrance|oud|musc|encens|bakhour|doukhoun)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Eau de parfum ambrée'
                    WHEN 2 THEN 'Parfum floral'
                    ELSE 'Eau de parfum boisée'
                END;
            ELSIF category_row.path ~* '(montre|watch)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Montre minimaliste'
                    WHEN 2 THEN 'Montre bracelet classique'
                    ELSE 'Montre cadran épuré'
                END;
            ELSIF category_row.path ~* '(bijou|bague|boucle|bracelet|collier|joaillerie)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Boucles d’oreilles dorées'
                    WHEN 2 THEN 'Collier fin'
                    ELSE 'Bracelet minimaliste'
                END;
            ELSIF category_row.path ~* '(sac|maroquinerie|portefeuille|bag)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Sac à main structuré'
                    WHEN 2 THEN 'Sac bandoulière'
                    ELSE 'Petit accessoire de maroquinerie'
                END;
            ELSIF category_row.path ~* '(chaussure|sneaker|basket|sandale|botte|mule)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Sneakers minimalistes'
                    WHEN 2 THEN 'Chaussures de ville'
                    ELSE 'Baskets du quotidien'
                END;
            ELSIF category_row.path ~* '(maquillage|cosmetique|soin|creme|serum|huile|capillaire|chebe|beaute|pommade)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Sérum hydratant'
                    WHEN 2 THEN 'Soin visage'
                    ELSE 'Crème de soin'
                END;
            ELSIF category_row.path ~* '(electronique|casque|ecouteur|audio|smartphone|ordinateur|camera|tech)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Casque audio sans fil'
                    WHEN 2 THEN 'Écouteurs sans fil'
                    ELSE 'Accessoire technologique'
                END;
            ELSIF category_row.path ~* '(maison|deco|decoration|vase|cuisine|luminaire|meuble|linge)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Vase en céramique'
                    WHEN 2 THEN 'Objet décoratif'
                    ELSE 'Accessoire pour la maison'
                END;
            ELSIF category_row.path ~* '(enfant|bebe|junior|kids)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Sneakers enfant'
                    WHEN 2 THEN 'Vêtement enfant'
                    ELSE 'Accessoire enfant'
                END;
            ELSIF category_row.path ~* '(lunette|sunglass|accessoire)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'Lunettes de soleil'
                    WHEN 2 THEN 'Accessoire de mode'
                    ELSE 'Sac du quotidien'
                END;
            ELSIF category_row.path ~* '(vetement|robe|tshirt|chemise|pantalon|veste|pull|jean|jupe|homme|femme)' THEN
                product_type := CASE product_index
                    WHEN 1 THEN 'T-shirt en coton'
                    WHEN 2 THEN 'Pièce de vestiaire'
                    ELSE 'Essentiel du quotidien'
                END;
            ELSE
                product_type := 'Sélection ' || category_row.name;
            END IF;

            product_name := LEFT(
                product_type || ' — ' || REPLACE(category_row.path, '/', ' · ')
                || ' ' || LPAD(product_index::TEXT, 2, '0'),
                100
            );

            -- Visuels trouvés en ligne : utilisés seulement pour la démo locale.
            -- Pexels perfume: https://www.pexels.com/photo/bottle-of-perfume-on-white-background-16266287/
            IF category_row.path ~* '(parfum|fragrance|oud|musc|encens|bakhour|doukhoun)' THEN
                image_candidates := ARRAY[
                    'https://images.pexels.com/photos/16266287/pexels-photo-16266287.jpeg?auto=compress&cs=tinysrgb&w=900',
                    'https://images.shotbg.com/pages/tools/white-background-generator-hero.webp',
                    'https://images.unsplash.com/photo-1594035910387-fea47794261f?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSIF category_row.path ~* '(montre|watch)' THEN
                image_candidates := ARRAY[
                    'https://pixeladmin.com/_next/static/media/tile-watch.2p3exowiux_--.png',
                    'https://images.unsplash.com/photo-1524592094714-0f0654e20314?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1523170335258-f5ed11844a49?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSIF category_row.path ~* '(bijou|bague|boucle|bracelet|collier|joaillerie)' THEN
                image_candidates := ARRAY[
                    'https://images.unsplash.com/photo-1611652022419-a9419f74343d?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1617038220319-276d3cfab638?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSIF category_row.path ~* '(sac|maroquinerie|portefeuille|bag)' THEN
                image_candidates := ARRAY[
                    'https://images.unsplash.com/photo-1559563458-527698bf5295?auto=format&fit=crop&w=1000&q=80',
                    'https://img.29cm.co.kr/item/202411/11efa0d707491f7b9876933c551b8a7d.jpg?format=webp&width=1200',
                    'https://cdn.salla.sa/XoKyj/1efe9381-1f11-4e93-9efc-d632d54bb361-1000x1000-01KYcfoo1EcWRg4wh0fLKChGBa9p8nNTJg5SPmWv.jpg'
                ];
            ELSIF category_row.path ~* '(chaussure|sneaker|basket|sandale|botte|mule)' THEN
                image_candidates := ARRAY[
                    'https://images.squarespace-cdn.com/content/v1/5b8c557c8f51308965aa6dd8/1626272853910-352ZLN7AKORK91LLKD1B/ESGRAINEDWHITE.jpg',
                    'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1552346154-21d32810aba3?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSIF category_row.path ~* '(maquillage|cosmetique|soin|creme|serum|huile|capillaire|chebe|beaute|pommade)' THEN
                image_candidates := ARRAY[
                    'https://glowelle.in/cdn/shop/files/02_c88e27ce-1667-45b8-905b-af78f0aa9047.jpg?v=1725257741&width=1000',
                    'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSIF category_row.path ~* '(electronique|casque|ecouteur|audio|smartphone|ordinateur|camera|tech)' THEN
                image_candidates := ARRAY[
                    'https://cdn.shopify.com/s/files/1/0608/8589/3342/files/good_1.png?v=1770971927',
                    'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSIF category_row.path ~* '(maison|deco|decoration|vase|cuisine|luminaire|meuble|linge)' THEN
                image_candidates := ARRAY[
                    'https://images.squarespace-cdn.com/content/v1/63189b362e7d871645511d46/1769195232399-C7XWQKSHK5DL9GDRMME8/imgg-demo-jBarXbPF.png',
                    'https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSIF category_row.path ~* '(enfant|bebe|junior|kids)' THEN
                image_candidates := ARRAY[
                    'https://cdn.sanity.io/images/c00ur4bn/production/dccbddd05f4f71591f5fa20af26c1b63a1572318-1400x1400.jpg?auto=format&q=75&w=500',
                    'https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1514090458221-65bb69cf63e6?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSIF category_row.path ~* '(lunette|sunglass|accessoire)' THEN
                image_candidates := ARRAY[
                    'https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=1000&q=80',
                    'https://images.unsplash.com/photo-1559563458-527698bf5295?auto=format&fit=crop&w=1000&q=80',
                    'https://pixeladmin.com/_next/static/media/tile-watch.2p3exowiux_--.png'
                ];
            ELSIF category_row.path ~* '(vetement|robe|tshirt|chemise|pantalon|veste|pull|jean|jupe|homme|femme)' THEN
                image_candidates := ARRAY[
                    'https://fairjeans.de/cdn/shop/files/T-shirt-weiss-creator-01.jpg?v=1726223825&width=1200',
                    'https://images.squarespace-cdn.com/content/v1/6032e5aa4a2da747346bf920/b960e407-c3ee-4d64-a61c-0cf8928dd80d/TIMELESS-0169.jpg',
                    'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=1000&q=80'
                ];
            ELSE
                image_candidates := ARRAY[
                    'https://images.pexels.com/photos/16266287/pexels-photo-16266287.jpeg?auto=compress&cs=tinysrgb&w=900',
                    'https://images.squarespace-cdn.com/content/v1/63189b362e7d871645511d46/1769195232399-C7XWQKSHK5DL9GDRMME8/imgg-demo-jBarXbPF.png',
                    'https://images.unsplash.com/photo-1559563458-527698bf5295?auto=format&fit=crop&w=1000&q=80'
                ];
            END IF;

            selected_image := image_candidates[product_index];

            -- Valeurs et tarifs de démonstration uniquement.
            base_price := (12.00 + ((CHAR_LENGTH(category_row.path) + product_index * 7) % 35) * 2.25)::NUMERIC(10,2);

            IF category_row.path = 'homme' OR category_row.path LIKE 'homme/%' THEN
                gender_value := 'HOMME';
            ELSIF category_row.path = 'femme' OR category_row.path LIKE 'femme/%' THEN
                gender_value := 'FEMME';
            ELSIF category_row.path = 'enfant' OR category_row.path LIKE 'enfant/%' THEN
                gender_value := 'ENFANT';
            ELSE
                gender_value := 'UNISEXE';
            END IF;

            IF category_row.path ~* '(chaussure|sneaker|basket|sandale|botte|mule)' THEN
                option_name := 'Pointure';
                option_values := ARRAY['38', '40', '42'];
            ELSIF category_row.path ~* '(vetement|robe|tshirt|chemise|pantalon|veste|pull|jean|jupe|homme|femme|enfant)' THEN
                option_name := 'Taille';
                option_values := ARRAY['S', 'M', 'L'];
            ELSIF category_row.path ~* '(parfum|fragrance|oud|musc|encens|bakhour|doukhoun)' THEN
                option_name := 'Contenance';
                option_values := ARRAY['50 ml', '100 ml', '150 ml'];
            ELSIF category_row.path ~* '(maquillage|cosmetique|soin|creme|serum|huile|capillaire|chebe|beaute|pommade)' THEN
                option_name := 'Format';
                option_values := ARRAY['30 ml', '50 ml', '100 ml'];
            ELSIF category_row.path ~* '(maison|deco|decoration|vase|cuisine|luminaire|meuble|linge)' THEN
                option_name := 'Finition';
                option_values := ARRAY['Ivoire', 'Sable', 'Noir'];
            ELSE
                option_name := 'Couleur';
                option_values := ARRAY['Noir', 'Beige', 'Doré'];
            END IF;

            product_id := gen_random_uuid();

            product_description :=
                'Produit fictif de démonstration SIDRA. Prix, stock et variantes sont des données de test, à ne pas utiliser pour la vente. '
                || marker;

            INSERT INTO products (
                id, name, description, price, category_id, stock_quantity,
                active, image_url, brand, fulfillment_type, created_at,
                featured, gender
            ) VALUES (
                product_id,
                product_name,
                product_description,
                base_price,
                category_row.id,
                15,
                TRUE,
                selected_image,
                'SIDRA Démo',
                'OWN_STOCK',
                CURRENT_TIMESTAMP - ((product_index - 1) * INTERVAL '1 day'),
                (category_row.parent_id IS NULL AND product_index = 1),
                gender_value
            );

            INSERT INTO product_categories (product_id, category_id)
            VALUES (product_id, category_row.id)
            ON CONFLICT DO NOTHING;

            option_id := gen_random_uuid();
            INSERT INTO product_options (id, product_id, name, position)
            VALUES (option_id, product_id, option_name, 0);

            FOR variant_index IN 1..3 LOOP
                INSERT INTO product_option_values (id, option_id, value, position)
                VALUES (
                    gen_random_uuid(),
                    option_id,
                    option_values[variant_index],
                    variant_index - 1
                )
                RETURNING id INTO option_value_id;

                INSERT INTO product_variants (
                    id, product_id, label, sku, price, stock_quantity,
                    active, position
                ) VALUES (
                    gen_random_uuid(),
                    product_id,
                    option_values[variant_index],
                    'SIDRADEMO-' || UPPER(SUBSTRING(MD5(category_row.path || ':' || product_index::TEXT || ':' || variant_index::TEXT), 1, 24)),
                    base_price + ((variant_index - 1) * 2.00),
                    5,
                    TRUE,
                    variant_index - 1
                )
                RETURNING id INTO variant_id;

                INSERT INTO variant_option_values (variant_id, option_value_id)
                VALUES (variant_id, option_value_id)
                ON CONFLICT DO NOTHING;
            END LOOP;
        END LOOP;
    END LOOP;
END
$seed$;

COMMIT;

-- Résultat attendu : 3 produits par catégorie active. Le nombre exact dépend
-- de l'arborescence présente dans la base au moment de l'exécution.
SELECT COUNT(*) AS produits_demo
FROM products
WHERE position('[SIDRA_DEMO:' in COALESCE(description, '')) > 0;
