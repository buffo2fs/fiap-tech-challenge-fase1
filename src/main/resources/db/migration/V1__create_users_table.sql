CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    user_type VARCHAR(30) NOT NULL,
    street VARCHAR(150) NOT NULL,
    address_number VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    zip_code VARCHAR(9) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_login UNIQUE (login),
    CONSTRAINT ck_users_user_type CHECK (user_type IN ('CUSTOMER', 'RESTAURANT_OWNER'))
);