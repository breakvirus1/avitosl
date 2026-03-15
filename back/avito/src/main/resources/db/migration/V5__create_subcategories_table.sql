-- Создание таблицы subcategories
CREATE TABLE IF NOT EXISTS subcategories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Перенос дочерних категорий (те, у которых parent_id не NULL) в subcategories
INSERT INTO subcategories (name, category_id, created_at, updated_at)
SELECT c.name, c.parent_id, c.created_at, c.updated_at
FROM categories c
WHERE c.parent_id IS NOT NULL;

-- Создание подкатегорий по умолчанию для категорий, у которых нет подкатегорий
INSERT INTO subcategories (name, category_id, created_at, updated_at)
SELECT 
    c.name || ' (общие)', 
    c.id, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
FROM categories c
WHERE NOT EXISTS (
    SELECT 1 FROM subcategories s WHERE s.category_id = c.id
);

-- Удаление дочерних категорий из таблицы categories
DELETE FROM categories c
WHERE c.parent_id IS NOT NULL;

-- Удаление колонки parent_id из categories
ALTER TABLE categories DROP COLUMN parent_id;

-- Добавление индекса для ускорения поиска subcategories по category_id
CREATE INDEX idx_subcategories_category_id ON subcategories(category_id);
