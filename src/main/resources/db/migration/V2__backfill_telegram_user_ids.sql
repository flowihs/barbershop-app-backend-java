-- Older application versions wrote the Telegram ID into users.id and left
-- telegram_id empty. Keep those accounts usable after aligning the entity
-- with the schema introduced in V1.
UPDATE users
SET telegram_id = id
WHERE telegram_id IS NULL;

ALTER TABLE users
    ALTER COLUMN telegram_id SET NOT NULL;
