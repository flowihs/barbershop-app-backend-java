UPDATE users
SET telegram_id = id
WHERE telegram_id IS NULL;

ALTER TABLE users
    ALTER COLUMN telegram_id SET NOT NULL;
