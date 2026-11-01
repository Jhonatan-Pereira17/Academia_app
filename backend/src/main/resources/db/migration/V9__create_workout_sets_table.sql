CREATE TABLE workout_sets (
    id BIGSERIAL PRIMARY KEY,
    workout_exercise_id BIGINT NOT NULL REFERENCES workout_exercises(id) ON DELETE CASCADE,
    set_number INTEGER NOT NULL,
    planned_reps INTEGER,
    planned_load NUMERIC(6,2),
    target_rpe INTEGER CHECK (target_rpe BETWEEN 1 AND 10),
    to_failure BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_workout_sets_workout_exercise_id ON workout_sets(workout_exercise_id);
