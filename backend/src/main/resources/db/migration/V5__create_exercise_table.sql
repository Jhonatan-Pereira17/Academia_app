CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    muscle_group VARCHAR(50) NOT NULL,
    primary_muscle VARCHAR(100),
    secondary_muscles VARCHAR(255),
    equipment VARCHAR(100),
    description TEXT,
    instructions TEXT,
    image_url VARCHAR(500),
    created_by_personal_id BIGINT REFERENCES personals(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_exercises_muscle_group ON exercises(muscle_group);