-- Добавление полей category_id и subcategory_id в таблицу posts
ALTER TABLE posts ADD COLUMN category_id BIGINT;
ALTER TABLE posts ADD COLUMN subcategory_id BIGINT;

-- Заполняем category_id: для каждого поста берем первую связанную категорию
UPDATE posts p
SET category_id = pc.category_id
FROM (
    SELECT DISTINCT ON (post_id) post_id, category_id
    FROM post_categories
    ORDER BY post_id, category_id
) pc
WHERE p.id = pc.post_id;

-- Заполняем subcategory_id: для каждого поста берем первую подкатегорию, которая принадлежит категории поста
UPDATE posts p
SET subcategory_id = s.id
FROM subcategories s
WHERE s.category_id = p.category_id
  AND s.id = (
      SELECT s2.id 
      FROM subcategories s2 
      WHERE s2.category_id = p.category_id 
      ORDER BY s2.id 
      LIMIT 1
  );

-- Делаем поля NOT NULL после заполнения
ALTER TABLE posts ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE posts ALTER COLUMN subcategory_id SET NOT NULL;

-- Добавляем внешние ключи
ALTER TABLE posts ADD CONSTRAINT fk_posts_category 
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE;
ALTER TABLE posts ADD CONSTRAINT fk_posts_subcategory 
    FOREIGN KEY (subcategory_id) REFERENCES subcategories(id) ON DELETE CASCADE;

-- Удаляем старую таблицу связи
DROP TABLE post_categories;

-- Создаем индексы для производительности
CREATE INDEX idx_posts_category_id ON posts(category_id);
CREATE INDEX idx_posts_subcategory_id ON posts(subcategory_id);
