package com.academiaapp.backend.workout;

import jakarta.validation.constraints.NotNull;

public record UpdateWorkoutActiveRequest(
        @NotNull(message = "O campo active é obrigatório") Boolean active) {
}