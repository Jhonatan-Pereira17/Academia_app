package com.academiaapp.backend.exercise;

public record ExerciseResponseDTO(
        Long id,
        String name,
        MuscleGroup muscleGroup,
        String primaryMuscle,
        String secondaryMuscles,
        String equipment,
        String description,
        String instructions,
        String imageUrl,
        Long createdByPersonalId) {

    public static ExerciseResponseDTO fromEntity(Exercise exercise) {
        return new ExerciseResponseDTO(
                exercise.getId(),
                exercise.getName(),
                exercise.getMuscleGroup(),
                exercise.getPrimaryMuscle(),
                exercise.getSecondaryMuscles(),
                exercise.getEquipment(),
                exercise.getDescription(),
                exercise.getInstructions(),
                exercise.getImageUrl(),
                exercise.getCreatedBy() == null ? null : exercise.getCreatedBy().getId());
    }
}