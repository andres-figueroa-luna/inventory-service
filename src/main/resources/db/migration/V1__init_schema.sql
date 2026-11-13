CREATE TABLE products
(
    id    BIGSERIAL PRIMARY KEY,
    sku   VARCHAR(255) UNIQUE NOT NULL,
    stock INTEGER             NOT NULL
);