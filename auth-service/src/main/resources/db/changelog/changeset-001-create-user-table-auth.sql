-- liquibase formatted sql

-- changeset author:001-create-user-table-auth
-- comment: Create app_user table for auth service
CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    avatar_url VARCHAR(255),
    bio TEXT
);

-- rollback DROP TABLE app_user;
