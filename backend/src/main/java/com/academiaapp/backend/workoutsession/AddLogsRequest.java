package com.academiaapp.backend.workoutsession;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddLogsRequest(
        @NotEmpty(message = "Envie pelo menos uma série") @Valid List<CreateWorkoutSessionRequest.LogItem> logs) {
}