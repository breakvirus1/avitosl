
INSERT INTO roles (name) VALUES ('USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, parent_id) VALUES ('Транспорт', NULL) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Автомобили', (SELECT id FROM categories WHERE name = 'Транспорт')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Мотоциклы и мототехника', (SELECT id FROM categories WHERE name = 'Транспорт')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Грузовики и спецтехника', (SELECT id FROM categories WHERE name = 'Транспорт')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Запчасти и аксессуары', (SELECT id FROM categories WHERE name = 'Транспорт')) ON CONFLICT DO NOTHING;

INSERT INTO categories (name, parent_id) VALUES ('Недвижимость', NULL) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Квартиры', (SELECT id FROM categories WHERE name = 'Недвижимость')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Комнаты', (SELECT id FROM categories WHERE name = 'Недвижимость')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Дома, дачи, коттеджи', (SELECT id FROM categories WHERE name = 'Недвижимость')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Земельные участки', (SELECT id FROM categories WHERE name = 'Недвижимость')) ON CONFLICT DO NOTHING;

INSERT INTO categories (name, parent_id) VALUES ('Электроника', NULL) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Смартфоны и телефоны', (SELECT id FROM categories WHERE name = 'Электроника')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Компьютеры и комплектующие', (SELECT id FROM categories WHERE name = 'Электроника')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Телевизоры и мультимедиа', (SELECT id FROM categories WHERE name = 'Электроника')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Бытовая техника', (SELECT id FROM categories WHERE name = 'Электроника')) ON CONFLICT DO NOTHING;

INSERT INTO categories (name, parent_id) VALUES ('Одежда, обувь, аксессуары', NULL) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Женская одежда', (SELECT id FROM categories WHERE name = 'Одежда, обувь, аксессуары')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Мужская одежда', (SELECT id FROM categories WHERE name = 'Одежда, обувь, аксессуары')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Обувь', (SELECT id FROM categories WHERE name = 'Одежда, обувь, аксессуары')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Аксессуары', (SELECT id FROM categories WHERE name = 'Одежда, обувь, аксессуары')) ON CONFLICT DO NOTHING;

INSERT INTO categories (name, parent_id) VALUES ('Хобби и отдых', NULL) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Спорт и фитнес', (SELECT id FROM categories WHERE name = 'Хобби и отдых')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Книги и журналы', (SELECT id FROM categories WHERE name = 'Хобби и отдых')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Музыкальные инструменты', (SELECT id FROM categories WHERE name = 'Хобби и отдых')) ON CONFLICT DO NOTHING;
INSERT INTO categories (name, parent_id) VALUES ('Коллекционирование', (SELECT id FROM categories WHERE name = 'Хобби и отдых')) ON CONFLICT DO NOTHING;