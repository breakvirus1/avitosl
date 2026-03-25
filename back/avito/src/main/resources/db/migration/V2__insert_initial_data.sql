INSERT INTO roles (name) VALUES ('USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;

-- Создание корневых категорий
INSERT INTO categories (name) VALUES ('Транспорт') ON CONFLICT DO NOTHING;
INSERT INTO categories (name) VALUES ('Недвижимость') ON CONFLICT DO NOTHING;
INSERT INTO categories (name) VALUES ('Электроника') ON CONFLICT DO NOTHING;
INSERT INTO categories (name) VALUES ('Одежда, обувь, аксессуары') ON CONFLICT DO NOTHING;
INSERT INTO categories (name) VALUES ('Хобби и отдых') ON CONFLICT DO NOTHING;
