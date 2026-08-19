ALTER TABLE products ADD COLUMN image_url VARCHAR(500);

INSERT INTO categories (id, name, description) VALUES
  (gen_random_uuid(), 'Parfums', 'Parfums orientaux, arabes et internationaux'),
  (gen_random_uuid(), 'Beauté', 'Produits de beauté'),
  (gen_random_uuid(), 'Pommades', 'Pommades traditionnelles'),
  (gen_random_uuid(), 'Crèmes', 'Crèmes corporelles et visage'),
  (gen_random_uuid(), 'Huiles', 'Huiles capillaires et corporelles'),
  (gen_random_uuid(), 'Produits capillaires', 'Soins pour cheveux'),
  (gen_random_uuid(), 'Chébé', 'Poudre capillaire traditionnelle'),
  (gen_random_uuid(), 'Doukhoun', 'Encens traditionnel en poudre'),
  (gen_random_uuid(), 'Encens', 'Encens et bakhour'),
  (gen_random_uuid(), 'Produits culturels', 'Produits culturels divers')
ON CONFLICT (name) DO NOTHING;

INSERT INTO products (id, name, description, price, category_id, stock_quantity, active, image_url)
SELECT gen_random_uuid(), v.name, v.description, v.price, c.id, v.stock, true, v.image
FROM categories c,
(VALUES
  ('Oud Al Malik', 'Parfum oriental boisé, notes de oud et santal.', 34.99, 50, 'https://picsum.photos/seed/parfum1/500/500'),
  ('Ambre Doré', 'Notes ambrées et vanillées, sillage chaud et enveloppant.', 29.99, 40, 'https://picsum.photos/seed/parfum2/500/500'),
  ('Musc Blanc', 'Parfum musqué doux, fraîcheur poudrée.', 24.99, 60, 'https://picsum.photos/seed/parfum3/500/500'),
  ('Rose de Damas', 'Bouquet floral intense, rose et épices douces.', 32.99, 35, 'https://picsum.photos/seed/parfum4/500/500'),
  ('Encens Royal', 'Notes fumées et boisées, inspiré du bakhour traditionnel.', 27.99, 45, 'https://picsum.photos/seed/parfum5/500/500'),
  ('Safran Mystique', 'Épicé et chaud, notes de safran et cuir.', 39.99, 25, 'https://picsum.photos/seed/parfum6/500/500'),
  ('Jasmin de Nuit', 'Floral blanc envoûtant, notes de jasmin et fleur d''oranger.', 28.99, 50, 'https://picsum.photos/seed/parfum7/500/500'),
  ('Cuir Noble', 'Boisé et cuiré, sillage puissant et masculin.', 36.99, 30, 'https://picsum.photos/seed/parfum8/500/500'),
  ('Fleur d''Oranger', 'Frais et lumineux, notes hespéridées et florales.', 22.99, 55, 'https://picsum.photos/seed/parfum9/500/500'),
  ('Vanille d''Orient', 'Gourmand et sucré, vanille et ambre.', 26.99, 48, 'https://picsum.photos/seed/parfum10/500/500'),
  ('Bois de Santal', 'Boisé crémeux, notes de santal et cèdre.', 31.99, 38, 'https://picsum.photos/seed/parfum11/500/500'),
  ('Nuit d''Arabie', 'Oriental complexe, épices, oud et résines précieuses.', 42.99, 20, 'https://picsum.photos/seed/parfum12/500/500')
) AS v(name, description, price, stock, image)
WHERE c.name = 'Parfums';