
ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';


UPDATE users SET password = 'temp123' WHERE password = '';
ALTER TABLE users ALTER COLUMN password SET NOT NULL;