CREATE TABLE transactions(
                             id BIGSERIAL PRIMARY KEY,
                             amount NUMERIC(19,2) NOT NULL,
                             type VARCHAR(50) NOT NULL,
                             description VARCHAR(255),
                             account_id BIGINT NOT NULL REFERENCES accounts(id),
                             category_id BIGINT NOT NULL REFERENCES categories(id),
                             date DATE NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);