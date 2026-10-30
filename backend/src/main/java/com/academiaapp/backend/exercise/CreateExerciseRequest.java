package com.academiaapp.backend.exercise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateExerciseRequest(
        @NotBlank(message = "O nome é obrigatório") String name,

        @NotNull(message = "O grupo muscular é obrigatório") MuscleGroup muscleGroup,

        String primaryMuscle,
        String secondaryMuscles,
        String equipment,
        String description,
        String instructions,
        String imageUrl) {
}