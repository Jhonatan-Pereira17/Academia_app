package com.academiaapp.backend.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

public record StudentDashboardDTO(
        Long studentId,
        String studentName,
        NextWorkout nextWorkout,
        LastSession lastSession,
        PeriodSummary last7Days,
        PeriodSummary last30Days,
        int sessionsThisWeek,
        int weeklyStreak) {

    public record NextWorkout(Long id, String name, int exercisesCount) {
    }

    public record LastSession(
            Long id,
            String workoutName,
            Instant startedAt,
            Long durationMinutes,
            int totalSets,
            BigDecimal totalVolumeKg) {
    }

    public record PeriodSummary(
            int sessions,
            int totalSets,
            BigDecimal totalVolumeKg,
            long trainingMinutes) {
    }
}