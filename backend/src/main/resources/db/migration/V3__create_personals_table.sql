CREATE TABLE personals (
    id BIGINT PRIMARY KEY REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    cref VARCHAR(50),
    phone VARCHAR(20)
);