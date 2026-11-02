CREATE TABLE exercise_logs (
    id BIGSERIAL PRIMARY KEY,
    client_id UUID NOT NULL,
    session_id BIGINT NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id),
    exercise_order INTEGER NOT NULL,
    set_number INTEGER NOT NULL,
    reps INTEGER NOT NULL CHECK (reps >= 0),
    load_kg NUMERIC(6,2) NOT NULL CHECK (load_kg >= 0),
    rpe INTEGER CHECK (rpe BETWEEN 1 AND 10),
    to_failure BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    completed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_exercise_logs_session_client UNIQUE (session_id, client_id)
);

CREATE INDEX idx_exercise_logs_exercise_id ON exercise_logs(exercise_id);