-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create databases for each microservice
SELECT 'CREATE DATABASE avito_user_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'avito_user_db')\gexec
SELECT 'CREATE DATABASE avito_category_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'avito_category_db')\gexec
SELECT 'CREATE DATABASE avito_post_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'avito_post_db')\gexec
SELECT 'CREATE DATABASE avito_chat_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'avito_chat_db')\gexec
SELECT 'CREATE DATABASE avito_purchase_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'avito_purchase_db')\gexec
