-- ============================================================
-- V4__seed_data.sql
-- Seed: default admin user
-- Password plain: Admin@123
-- Password BCrypt: $2a$12$3agZ7wIhd.4R5juY7cfF7OpPoLMrXRN2NamdwhFGPZD0avk9j6r0q
-- ============================================================

INSERT INTO
    users (
        id,
        name,
        email,
        password,
        role,
        is_active,
        created_at,
        updated_at
    )
VALUES (
        UUID(),
        'Admin',
        'admin@todoapp.com',
        '$2a$12$3agZ7wIhd.4R5juY7cfF7OpPoLMrXRN2NamdwhFGPZD0avk9j6r0q',
        'ADMIN',
        TRUE,
        NOW(),
        NOW()
    );