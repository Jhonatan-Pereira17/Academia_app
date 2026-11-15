CREATE TABLE progress_photos (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    evaluation_id BIGINT REFERENCES physical_evaluations(id) ON DELETE SET NULL,
    taken_on DATE NOT NULL,
    angle VARCHAR(10),
    notes TEXT,
    storage_key VARCHAR(100) NOT NULL UNIQUE,
    content_type VARCHAR(50) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_progress_photos_student_taken ON progress_photos(student_id, taken_on DESC);