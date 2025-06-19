-- liquibase formatted sql

-- changeset author:create-auth-tables
-- comment: Create tables for authentication system

-- Add auth columns one by one
ALTER TABLE app_user ADD COLUMN password VARCHAR(100) NOT NULL;
ALTER TABLE app_user ADD COLUMN enabled BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE app_user ADD COLUMN account_non_expired BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE app_user ADD COLUMN account_non_locked BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE app_user ADD COLUMN credentials_non_expired BOOLEAN DEFAULT TRUE NOT NULL;

-- Create role table
CREATE TABLE role (
    id UUID PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Create user_role join table
CREATE TABLE user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- rollback DROP TABLE user_role;
-- rollback DROP TABLE role;
-- rollback ALTER TABLE app_user DROP COLUMN credentials_non_expired;
-- rollback ALTER TABLE app_user DROP COLUMN account_non_locked;
-- rollback ALTER TABLE app_user DROP COLUMN account_non_expired;
-- rollback ALTER TABLE app_user DROP COLUMN enabled;
-- rollback ALTER TABLE app_user DROP COLUMN password;
