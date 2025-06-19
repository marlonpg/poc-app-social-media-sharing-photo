-- liquibase formatted sql

-- changeset author:insert-default-roles
-- comment: Insert default user roles

INSERT INTO role (id, name) VALUES
    ('11111111-1111-1111-1111-111111111111', 'ROLE_USER'),
    ('22222222-2222-2222-2222-222222222222', 'ROLE_ADMIN');

-- rollback DELETE FROM role WHERE id IN (
--     '11111111-1111-1111-1111-111111111111',
--     '22222222-2222-2222-2222-222222222222'
-- );
