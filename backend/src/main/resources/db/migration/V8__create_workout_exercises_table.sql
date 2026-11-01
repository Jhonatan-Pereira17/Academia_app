CREATE TABLE workout_exercises (
    id BIGSERIAL PRIMARY KEY,
    workout_id BIGINT NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id),
    exercise_order INTEGER NOT NULL,
    rest_seconds INTEGER,
    notes TEXT
);

CREATE INDEX idx_workout_exercises_workout_id ON workout_exercises(workout_id);
