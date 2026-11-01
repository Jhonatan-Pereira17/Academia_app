package com.academiaapp.backend.workout;

import com.academiaapp.backend.exercise.MuscleGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record WorkoutResponseDTO(
        Long id,
        Long studentId,
        String studentName,
        Long createdByUserId,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        List<ExerciseItem> exercises) {

    public record ExerciseItem(
            Long id,
            Long exerciseId,
            String exerciseName,
            MuscleGroup muscleGroup,
            Integer order,
            Integer restSeconds,
            String notes,
            List<SetItem> sets) {
    }

    public record SetItem(
            Integer setNumber,
            Integer plannedReps,
            BigDecimal plannedLoad,
            Integer targetRpe,
            boolean toFailure) {
    }

    public static WorkoutResponseDTO fromEntity(Workout workout) {
        List<ExerciseItem> exercises = workout.getExercises().stream()
                .map(we -> new ExerciseItem(
                        we.getId(),
                        we.getExercise().getId(),
                        we.getExercise().getName(),
                        we.getExercise().getMuscleGroup(),
                        we.getExerciseOrder(),
                        we.getRestSeconds(),
                        we.getNotes(),
                        we.getSets().stream()
                                .map(ws -> new SetItem(
                                        ws.getSetNumber(),
                                        ws.getPlannedReps(),
                                        ws.getPlannedLoad(),
                                        ws.getTargetRpe(),
                                        ws.isToFailure()))
                                .toList()))
                .toList();

        return new WorkoutResponseDTO(
                workout.getId(),
                workout.getStudent().getId(),
                workout.getStudent().getName(),
                workout.getCreatedBy().getId(),
                workout.getName(),
                workout.getDescription(),
                workout.isActive(),
                workout.getCreatedAt(),
                exercises);
    }
}