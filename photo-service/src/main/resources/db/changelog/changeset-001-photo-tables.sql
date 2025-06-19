-- liquibase formatted sql

-- changeset author:001-create-photo-tables
-- comment: Create initial database tables for photo service

CREATE TABLE tag (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- rollback DROP TABLE tag;

CREATE TABLE photo (
    id UUID PRIMARY KEY,
    caption TEXT,
    image_url VARCHAR(255) NOT NULL,
    upload_time TIMESTAMP NOT NULL,
    privacy VARCHAR(20) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    location_name VARCHAR(100),
    user_id UUID NOT NULL -- This will reference the app_user table created by auth-service
);

-- rollback DROP TABLE photo;

CREATE TABLE photo_tag (
    photo_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (photo_id, tag_id),
    CONSTRAINT fk_photo_tag_photo FOREIGN KEY (photo_id) REFERENCES photo(id) ON DELETE CASCADE,
    CONSTRAINT fk_photo_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

-- rollback DROP TABLE photo_tag;

CREATE TABLE photo_person_tag (
    photo_id UUID NOT NULL,
    user_id UUID NOT NULL, -- This will reference the app_user table
    PRIMARY KEY (photo_id, user_id),
    CONSTRAINT fk_photo_person_tag_photo FOREIGN KEY (photo_id) REFERENCES photo(id) ON DELETE CASCADE
    -- CONSTRAINT fk_photo_person_tag_app_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE -- app_user table is in auth-service schema
);

-- rollback DROP TABLE photo_person_tag;

CREATE TABLE comment (
    id UUID PRIMARY KEY,
    text TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    user_id UUID NOT NULL, -- This will reference the app_user table
    photo_id UUID NOT NULL,
    -- CONSTRAINT fk_comment_app_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE, -- app_user table is in auth-service schema
    CONSTRAINT fk_comment_photo FOREIGN KEY (photo_id) REFERENCES photo(id) ON DELETE CASCADE
);

-- rollback DROP TABLE comment;

CREATE TABLE interaction (
    id UUID PRIMARY KEY,
    type VARCHAR(20) NOT NULL, -- 'LIKE', 'SAVE', 'SHARE', etc.
    timestamp TIMESTAMP NOT NULL,
    user_id UUID NOT NULL, -- This will reference the app_user table
    photo_id UUID NOT NULL,
    -- CONSTRAINT fk_interaction_app_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE, -- app_user table is in auth-service schema
    CONSTRAINT fk_interaction_photo FOREIGN KEY (photo_id) REFERENCES photo(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_photo_interaction UNIQUE (user_id, photo_id, type) -- Prevent duplicate interactions of same type
);

-- rollback DROP TABLE interaction;

-- changeset author:002-add-foreign-key-photo-user-photo-service
-- comment: Add foreign key constraint from photo table to app_user table (if app_user is shared or replicated)
-- For now, this assumes app_user table exists and can be referenced.
-- If using separate databases without direct FKs, this constraint would be managed at the application level or not exist.
-- ALTER TABLE photo
-- ADD CONSTRAINT fk_photo_user_photo_service
-- FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE;

-- rollback ALTER TABLE photo DROP CONSTRAINT fk_photo_user_photo_service;
