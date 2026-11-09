package com.academiaapp.backend.progress;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkoutSessionSummaryDTO(
        Long id,
        Long studentId,
        String studentName,
        Long workoutId,
        String workoutName,
        Instant startedAt,
        Instant finishedAt,
        Long durationMinutes,
        int totalSets,
        BigDecimal totalVolumeKg) {
}