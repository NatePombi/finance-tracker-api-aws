CREATE TABLE accounts(
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(50) NOT NULL,
                         account_type VARCHAR(50) NOT NULL,
                         user_id BIGINT NOT NULL REFERENCES users(id)
);