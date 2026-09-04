CREATE TABLE students (
    id BIGINT PRIMARY KEY REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    personal_id BIGINT NOT NULL REFERENCES personals(id),
    birth_date DATE,
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);