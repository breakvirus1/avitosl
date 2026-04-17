-- Add author name columns to comments table
ALTER TABLE comments ADD COLUMN author_first_name VARCHAR(255);
ALTER TABLE comments ADD COLUMN author_last_name VARCHAR(255);