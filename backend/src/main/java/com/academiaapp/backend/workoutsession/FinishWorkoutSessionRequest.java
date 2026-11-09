package com.academiaapp.backend.workoutsession;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FinishWorkoutSessionRequest(
        @NotNull(message = "O horário de término é obrigatório") Instant finishedAt,

        String notes) {
}