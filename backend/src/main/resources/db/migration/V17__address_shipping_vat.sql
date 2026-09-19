-- Enrichissement des adresses
ALTER TABLE addresses ADD COLUMN first_name VARCHAR(100);
ALTER TABLE addresses ADD COLUMN last_name VARCHAR(100);
ALTER TABLE addresses ADD COLUMN complement VARCHAR(255);
ALTER TABLE addresses ADD COLUMN phone VARCHAR(30);
ALTER TABLE addresses ADD COLUMN country_code VARCHAR(2) NOT NULL DEFAULT 'FR';

-- Adresse obligatoire sur les commandes
ALTER TABLE orders ALTER COLUMN address_id SET NOT NULL;

-- Détail des montants
ALTER TABLE orders ADD COLUMN subtotal_amount DECIMAL(10,2);
ALTER TABLE orders ADD COLUMN shipping_amount DECIMAL(10,2);
ALTER TABLE orders ADD COLUMN vat_amount DECIMAL(10,2);
ALTER TABLE orders ADD COLUMN vat_rate DECIMAL(5,2);

UPDATE orders SET
    subtotal_amount = total_amount,
    shipping_amount = 0.00,
    vat_amount = ROUND(total_amount - (total_amount / 1.20), 2),
    vat_rate = 20.00
WHERE subtotal_amount IS NULL;

-- Zones de livraison UE
CREATE TABLE shipping_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    flat_rate DECIMAL(10,2) NOT NULL,
    free_threshold DECIMAL(10,2),
    vat_rate DECIMAL(5,2) NOT NULL DEFAULT 20.00
);

CREATE TABLE shipping_zone_countries (
    zone_id UUID NOT NULL REFERENCES shipping_zones(id) ON DELETE CASCADE,
    country_code VARCHAR(2) NOT NULL,
    PRIMARY KEY (country_code)
);

-- Zones par défaut (montants à ajuster)
INSERT INTO shipping_zones (id, name, flat_rate, free_threshold, vat_rate) VALUES
    (gen_random_uuid(), 'France', 4.90, 40.00, 20.00),
    (gen_random_uuid(), 'Union Européenne', 9.90, 80.00, 20.00);

INSERT INTO shipping_zone_countries (zone_id, country_code)
SELECT id, 'FR' FROM shipping_zones WHERE name = 'France';

INSERT INTO shipping_zone_countries (zone_id, country_code)
SELECT z.id, c.code
FROM shipping_zones z,
(VALUES ('BE'),('DE'),('ES'),('IT'),('NL'),('LU'),('PT'),('AT'),('IE'),('PL'),
        ('SE'),('DK'),('FI'),('CZ'),('GR'),('HU'),('RO'),('BG'),('HR'),('SK'),
        ('SI'),('LT'),('LV'),('EE'),('CY'),('MT')) AS c(code)
WHERE z.name = 'Union Européenne';