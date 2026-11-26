INSERT INTO users (name, email, login, password_hash, user_type,
                   street, address_number, city, zip_code,
                   created_at, last_modified_at)
SELECT 'Usuário Avaliador', 'avaliador@example.com', 'avaliador', '$2a$10$OhSsSixSXQQ2I39hqGJWK.06KVV8.MgoeKa57J1UOpw7z/K0USgJq', 'CUSTOMER',
       'Avenida Paulista', '1000', 'São Paulo', '01310-100',
       NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE login = 'avaliador' AND deleted_at IS NULL
);