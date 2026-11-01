package com.academiaapp.backend.workout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateWorkoutRequest(
        @NotBlank(message = "O nome do treino é obrigatório") String name,

        String description,

        @NotEmpty(message = "O treino precisa ter pelo menos um exercício") @Valid List<CreateWorkoutRequest.ExerciseItem> exercises) {
}