-- V3__Clean_up_user_id_column.sql
-- Remove the old user_id column since we migrated to keycloak_id

ALTER TABLE posts DROP COLUMN IF EXISTS user_id;