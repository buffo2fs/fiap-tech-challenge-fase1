ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;

ALTER TABLE users DROP CONSTRAINT uk_users_email;
ALTER TABLE users DROP CONSTRAINT uk_users_login;

CREATE UNIQUE INDEX uk_users_email_active ON users (email) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_users_login_active ON users (login) WHERE deleted_at IS NULL;