package com.academiaapp.backend.progress;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record LastPerformanceDTO(
        Long sessionId,
        String workoutName,
        Instant performedAt,
        List<SetItem> sets) {

    public record SetItem(
            Integer setNumber,
            Integer reps,
            BigDecimal loadKg,
            Integer rpe,
            boolean toFailure) {
    }
}