CREATE TABLE categories(
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(50) NOT NULL,
                           type VARCHAR(50) NOT NULL,
                           user_id BIGINT NOT NULL REFERENCES users(id)
);