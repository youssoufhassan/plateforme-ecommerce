INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'CLIENT')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'ADMIN')
ON CONFLICT (name) DO NOTHING;