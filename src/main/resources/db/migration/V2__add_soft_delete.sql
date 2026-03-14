-- ============================================================
-- V2__add_soft_delete.sql
-- Add soft delete support to all tables
-- ============================================================

-- Users: sudah pakai is_active, tambah deleted_at untuk timestamp
ALTER TABLE users
ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL AFTER is_active,
ADD INDEX idx_user_deleted (deleted_at);

-- Categories
ALTER TABLE categories
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE AFTER is_default,
ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL AFTER is_deleted,
ADD INDEX idx_category_deleted (user_id, is_deleted);

-- Todos
ALTER TABLE todos
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE AFTER completed,
ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL AFTER is_deleted,
ADD INDEX idx_todo_deleted (user_id, is_deleted);

-- todo_categories: tidak perlu soft delete
-- karena ini pivot table, ikut cascade dari todos/categories