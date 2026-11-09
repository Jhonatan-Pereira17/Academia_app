package com.academiaapp.backend.workoutsession;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkoutSessionResponseDTO(
        Long id,
        UUID clientId,
        Long studentId,
        Long workoutId,
        String workoutName,
        Instant startedAt,
        Instant finishedAt,
        String notes,
        List<LogItem> logs) {

    public record LogItem(
            Long id,
            UUID clientId,
            Long exerciseId,
            String exerciseName,
            Integer exerciseOrder,
            Integer setNumber,
            Integer reps,
            BigDecimal loadKg,
            Integer rpe,
            boolean toFailure,
            String notes,
            Instant completedAt) {
    }

    public static WorkoutSessionResponseDTO fromEntity(WorkoutSession session) {
        List<LogItem> logs = session.getLogs().stream()
                .map(log -> new LogItem(
                        log.getId(),
                        log.getClientId(),
                        log.getExercise().getId(),
                        log.getExercise().getName(),
                        log.getExerciseOrder(),
                        log.getSetNumber(),
                        log.getReps(),
                        log.getLoadKg(),
                        log.getRpe(),
                        log.isToFailure(),
                        log.getNotes(),
                        log.getCompletedAt()))
                .toList();

        return new WorkoutSessionResponseDTO(
                session.getId(),
                session.getClientId(),
                session.getStudent().getId(),
                session.getWorkout() == null ? null : session.getWorkout().getId(),
                session.getWorkoutName(),
                session.getStartedAt(),
                session.getFinishedAt(),
                session.getNotes(),
                logs);
    }
}