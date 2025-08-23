CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100)        NOT NULL,
    last_name    VARCHAR(100)        NOT NULL,
    email        VARCHAR(150) UNIQUE NOT NULL,
    born_date    DATE,
    address      VARCHAR(255),
    phone_number VARCHAR(20),
    base_salary  NUMERIC(15, 2)
);
