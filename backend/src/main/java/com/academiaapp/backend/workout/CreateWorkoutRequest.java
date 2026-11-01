package com.academiaapp.backend.workout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record CreateWorkoutRequest(
        Long studentId,

        @NotBlank(message = "O nome do treino é obrigatório") String name,

        String description,

        @NotEmpty(message = "O treino precisa ter pelo menos um exercício") @Valid List<ExerciseItem> exercises) {

    public record ExerciseItem(
            @NotNull(message = "O exercício é obrigatório") Long exerciseId,

            @PositiveOrZero(message = "O descanso não pode ser negativo") Integer restSeconds,

            String notes,

            @NotEmpty(message = "O exercício precisa ter pelo menos uma série") @Valid List<SetItem> sets) {
    }

    public record SetItem(
            @Positive(message = "As repetições devem ser maiores que zero") Integer plannedReps,

            @DecimalMin(value = "0.0", message = "A carga não pode ser negativa") BigDecimal plannedLoad,

            @Min(value = 1, message = "O RPE mínimo é 1") @Max(value = 10, message = "O RPE máximo é 10") Integer targetRpe,

            Boolean toFailure) {
    }
}