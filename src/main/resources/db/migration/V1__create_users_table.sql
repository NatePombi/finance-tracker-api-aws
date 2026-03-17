CREATE TABLE users(
                      id BIGSERIAL PRIMARY KEY,
                      email VARCHAR(50) NOT NULL,
                      password VARCHAR(100) NOT NULL,
                      role VARCHAR(50) NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);