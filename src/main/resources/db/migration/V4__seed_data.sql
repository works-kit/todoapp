-- ============================================================
-- V4__seed_data.sql
-- Seed: default admin user
-- Password plain: Admin@123
-- Password BCrypt: $2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
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
        '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'ADMIN',
        TRUE,
        NOW(),
        NOW()
    );