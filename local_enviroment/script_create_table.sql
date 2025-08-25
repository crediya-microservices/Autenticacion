CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE users
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(100)        NOT NULL,
    last_name    VARCHAR(100)        NOT NULL,
    email        VARCHAR(150) UNIQUE NOT NULL,
    born_date    DATE,
    address      VARCHAR(255),
    phone_number VARCHAR(20),
    base_salary  NUMERIC(15, 2)
    identity_document VARCHAR(50) UNIQUE NOT NULL,
    idrol        INTEGER REFERENCES roles(id)
);

INSERT INTO roles (name, description) VALUES
('Admin', 'Administrator with full access'),
('User', 'Regular user with limited access'),
('adviser', 'adviser user with limited access');
