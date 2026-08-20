-- Create enum type for user roles
CREATE TYPE user_role AS ENUM ('CLIENT', 'BARBER', 'ADMIN');

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id          SERIAL PRIMARY KEY,
    telegram_id BIGINT UNIQUE,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    username    VARCHAR(255) UNIQUE,
    photo_url   TEXT,
    role        user_role DEFAULT 'CLIENT',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for frequently queried columns
CREATE INDEX IF NOT EXISTS idx_users_telegram_id ON users(telegram_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
