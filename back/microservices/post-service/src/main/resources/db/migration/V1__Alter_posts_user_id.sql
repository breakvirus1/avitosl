-- V1__Alter_posts_user_id.sql
-- Alter posts table to use user_id as keycloak_id column

-- If keycloak_id column exists, drop it
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'posts' AND column_name = 'keycloak_id') THEN
        ALTER TABLE posts DROP COLUMN keycloak_id;
    END IF;
END $$;

-- Rename user_id to keycloak_id
ALTER TABLE posts RENAME COLUMN user_id TO keycloak_id;

-- Ensure keycloak_id is varchar and not null
ALTER TABLE posts ALTER COLUMN keycloak_id TYPE varchar(255);
ALTER TABLE posts ALTER COLUMN keycloak_id SET NOT NULL;