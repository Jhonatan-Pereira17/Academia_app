package com.academiaapp.backend.workoutsession;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateWorkoutSessionRequest(
        @NotNull(message = "O clientId da sessão é obrigatório") UUID clientId,

        Long workoutId,

        String workoutName,

        @NotNull(message = "O horário de início é obrigatório") Instant startedAt,

        Instant finishedAt,

        String notes,

        @Valid List<LogItem> logs) {

    public record LogItem(
            @NotNull(message = "O clientId da série é obrigatório") UUID clientId,

            @NotNull(message = "O exercício é obrigatório") Long exerciseId,

            @NotNull(message = "A ordem do exercício é obrigatória") @Positive(message = "A ordem do exercício deve ser maior que zero") Integer exerciseOrder,

            @NotNull(message = "O número da série é obrigatório") @Positive(message = "O número da série deve ser maior que zero") Integer setNumber,

            @NotNull(message = "As repetições são obrigatórias") @PositiveOrZero(message = "As repetições não podem ser negativas") Integer reps,

            @NotNull(message = "A carga é obrigatória") @DecimalMin(value = "0.0", message = "A carga não pode ser negativa") BigDecimal loadKg,

            @Min(value = 1, message = "O RPE mínimo é 1") @Max(value = 10, message = "O RPE máximo é 10") Integer rpe,

            Boolean toFailure,

            String notes,

            @NotNull(message = "O horário de conclusão da série é obrigatório") Instant completedAt) {
    }
}