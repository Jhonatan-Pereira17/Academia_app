package com.academiaapp.backend.progress;

import java.math.BigDecimal;
import java.time.Instant;

public record ExerciseHistoryItemDTO(
        Long sessionId,
        Instant performedAt,
        int setsCount,
        int totalReps,
        BigDecimal maxLoadKg,
        BigDecimal totalVolumeKg) {
}