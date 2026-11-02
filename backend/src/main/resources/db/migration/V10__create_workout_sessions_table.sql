CREATE TABLE workout_sessions (
    id BIGSERIAL PRIMARY KEY,
    client_id UUID NOT NULL,
    student_id BIGINT NOT NULL REFERENCES students(id),
    workout_id BIGINT REFERENCES workouts(id) ON DELETE SET NULL,
    workout_name VARCHAR(255) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_workout_sessions_student_client UNIQUE (student_id, client_id)
);

CREATE INDEX idx_workout_sessions_student_started ON workout_sessions(student_id, started_at DESC);